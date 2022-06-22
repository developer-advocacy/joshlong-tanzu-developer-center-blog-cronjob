package com.example.feedprocessorv2;

import com.rometools.rome.io.SyndFeedInput;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jsoup.Jsoup;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This provides a client to get access to all of my blogs, spring tips videos, talk
 * abstracts, appearances, and podcasts
 *
 * @author Josh Long
 */
@SpringBootApplication
public class FeedProcessorV2Application {

	public static void main(String[] args) {
		SpringApplication.run(FeedProcessorV2Application.class, args);
	}

	@Bean
	ApplicationRunner rendererRunner(JoshlongMarkdownRenderer renderer) {
		return new ApplicationRunner() {
			@Override
			public void run(ApplicationArguments args) throws Exception {

			}
		};
	}

	@Bean
	HttpGraphQlClient httpGraphQlClient() {
		return HttpGraphQlClient.builder().url("https://api.joshlong.com/graphql").build();
	}

}

@Component
class JoshlongMarkdownRenderer {

	private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

	private String renderDate(Date date) {
		synchronized (this.simpleDateFormat) {
			return simpleDateFormat.format(date);
		}
	}

	String renderMarkdownAsHtml(String markdown) {
		var parser = Parser.builder().build();//
		var document = parser.parse(markdown);//
		var renderer = HtmlRenderer.builder()//
				.escapeHtml(false).build();
		return renderer.render(document);
	}

	String render(Appearance appearance) {

		var start = appearance.startDate();
		var event = appearance.event();
		var blurb = appearance.marketingBlurb();

		var md = """
				## %s

				**%s**

				%s
				""";
		return String.format(md, event, renderDate(start), blurb);
	}

	String render(Podcast podcast) {
		return null;
	}

	String render(SpringTip tip) {
		return null;
	}

	String render(TalkAbstract talkAbstract) {
		return null;
	}

	String render(BlogPost post) {
		return null;
	}

}

interface JoshlongService {

	Flux<BlogPost> getBlogPosts();

	Flux<SpringTip> getSpringTips();

	Flux<TalkAbstract> getAbstracts();

	Flux<Appearance> getAppearances();

	Flux<Podcast> getPodcasts();

}

record BlogPost(String title, Date published, String description) {
}

record SpringTip(URL blogUrl, Date date, int seasonNumber, String title, String youtubeId, URL youtubeEmbedUrl) {
}

record TalkAbstract(String title, String description) {
}

record Podcast(String id, String uid, URL episodeUri, URL episodePhotoUri, String description, Date date) {
}

record Appearance(String event, Date startDate, Date endDate, String time, String marketingBlurb) {
}

@Slf4j
@Service
class DefaultJoshlongService implements JoshlongService {

	private final HttpGraphQlClient client;

	private final List<BlogPost> posts;

	DefaultJoshlongService(HttpGraphQlClient client) {
		this.client = client;
		this.posts = initFeed();
	}

	private record StringyAppearance(String event, String startDate, String endDate, String time,
			String marketingBlurb) {
	}

	private record StringySpringTip(String blogUrl, String date, int seasonNumber, String title, String youtubeId,
			String youtubeEmbedUrl) {
	}

	private record StringyPodcast(String id, String uid, URL episodeUri, URL episodePhotoUri, String description,
			String date) {
	}

	@Override
	public Flux<BlogPost> getBlogPosts() {
		return Flux.fromIterable(this.posts).doOnNext(bp -> log.info(bp.title() + " " + bp.published().toString()));
	}

	@Override
	public Flux<TalkAbstract> getAbstracts() {
		var query = """
				query { abstracts }
				""";
		var results = this.client//
				.document(query) //
				.retrieve("abstracts")//
				.toEntity(String.class);
		return results.flatMapMany(html -> Flux.fromIterable(parseAbstracts(html)));
	}

	@Override
	public Flux<SpringTip> getSpringTips() {
		var query = """
				query {
				  springTipsEpisodes {
				    blogUrl, date, seasonNumber, title, youtubeId, youtubeEmbedUrl
				  }
				}
				""";
		return this.client//
				.document(query)//
				.retrieve("springTipsEpisodes")//
				.toEntityList(StringySpringTip.class)//
				.flatMapMany(list -> Flux.fromIterable(buildSpringTipsList(list)));
	}

	@Override
	public Flux<Appearance> getAppearances() {
		var query = """
				{ appearances { event, startDate, endDate, time, marketingBlurb } }
				""";
		var stringy = client //
				.document(query)//
				.retrieve("appearances") //
				.toEntityList(StringyAppearance.class) //
				.map(sa -> sa.stream()//
						.map(DefaultJoshlongService::fromStringAppearance) //
						.sorted(Comparator.comparing(Appearance::startDate))//
						.toList());
		return stringy.flatMapMany(Flux::fromIterable);
	}

	@Override
	public Flux<Podcast> getPodcasts() {
		var podcasts = """
				{
				  podcasts {
				    id
				    uid
				    episodeUri
				    episodePhotoUri
				    description
				    date
				  }
				}
				""";
		return client.document(podcasts)//
				.retrieve("podcasts")//
				.toEntityList(StringyPodcast.class).map(list -> list.stream()//
						.map(DefaultJoshlongService::fromStringyPodcast)//
						.collect(Collectors.toList()))
				.flatMapMany(Flux::fromIterable);
	}

	private static List<SpringTip> buildSpringTipsList(List<StringySpringTip> stringySpringTips) {
		return stringySpringTips.stream()
				.map(st -> new SpringTip(buildUrlFrom(st.blogUrl()), buildDateFrom(st.date()), st.seasonNumber(),
						st.title(), st.youtubeId(), buildUrlFrom(st.youtubeEmbedUrl())))
				.sorted(Comparator.comparing(SpringTip::date)).toList();
	}

	private static List<TalkAbstract> parseAbstracts(String html) {
		var document = Jsoup.parse(html);
		var accumulator = new HashMap<String, List<String>>();
		var seenH2 = false;
		var body = document.getElementsByTag("body").iterator().next();
		var recentH2 = "";
		for (var e : body.children()) {

			var tagName = e.tagName().trim();

			if (tagName.equalsIgnoreCase("body")) {
				continue;
			}

			var isH2 = tagName.toLowerCase(Locale.ROOT).equals("h2");
			if (isH2) {
				seenH2 = true;
			}

			if (seenH2) {
				if (isH2) {
					recentH2 = e.text();
					accumulator.put(recentH2, new ArrayList<>());
				} //
				else {
					accumulator.get(recentH2).add("<" + e.tagName() + ">" + e.html() + "</" + e.tagName() + ">");
				}
			}
		}

		var list = new ArrayList<TalkAbstract>();
		for (var e : accumulator.entrySet()) {
			var values = e.getValue();
			var joinedHtml = Strings.join(values.stream().iterator(), '\n');
			list.add(new TalkAbstract(e.getKey(), joinedHtml));
		}
		return list;
	}

	private static Appearance fromStringAppearance(StringyAppearance appearance) {
		return new Appearance(appearance.event(), buildDateFrom(appearance.startDate()),
				buildDateFrom(appearance.endDate()), appearance.time(), appearance.marketingBlurb());
	}

	private static Podcast fromStringyPodcast(StringyPodcast podcast) {
		return new Podcast(podcast.id(), podcast.uid(), podcast.episodeUri(), podcast.episodePhotoUri(),
				podcast.description(), buildDateFrom(podcast.date()));
	}

	@SneakyThrows
	private List<BlogPost> initFeed() {
		var url = buildUrlFrom("https://spring.io/blog/category/engineering.atom");
		try (var in = url.openStream(); var is = new InputStreamReader(in)) {
			var feed = new SyndFeedInput().build(is);
			return feed.getEntries().stream()
					.filter(se -> se.getAuthors().stream().anyMatch(s -> s.getName().contains("Josh Long")))
					.map(se -> new BlogPost(se.getTitle(), se.getUpdatedDate(), ""))
					.sorted(Comparator.comparing(BlogPost::published).reversed()).toList();
		}
	}

	@SneakyThrows
	private static URL buildUrlFrom(String href) {
		return new URL(href);
	}

	@SneakyThrows
	private static Date buildDateFrom(String date) {
		if (StringUtils.hasText(date)) {
			var parts = date.split("T")[0];
			var delim = parts.split("-");
			var year = Integer.parseInt(delim[0]);
			var month = Integer.parseInt(delim[1]);
			var day = Integer.parseInt(delim[2]);
			return new GregorianCalendar(year, month - 1, day).getTime();
		}
		return null;
	}

}
