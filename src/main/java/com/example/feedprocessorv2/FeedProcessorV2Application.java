package com.example.feedprocessorv2;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.jsoup.Jsoup;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * todo blogs todo spring tips
 *
 * <h2>Supported</h2>
 * <ol>
 * <LI>abstracts</LI>
 * <LI>appearances</LI>
 * <LI>podcasts</LI>
 * </ol>
 *
 * @author Josh Long
 */
@SpringBootApplication
public class FeedProcessorV2Application {

	public static void main(String[] args) {
		SpringApplication.run(FeedProcessorV2Application.class, args);
	}

	@Bean
	HttpGraphQlClient httpGraphQlClient() {
		return HttpGraphQlClient.builder().url("https://api.joshlong.com/graphql").build();
	}

}

interface JoshlongService {

	Flux<SpringTip> getSpringTips();

	Flux<Abstract> getAbstracts();

	Flux<Appearance> getAppearances();

	Flux<Podcast> getPodcasts();

}

record SpringTip(URL blogUrl, Date date, int seasonNumber, String title, String youtubeId, URL youtubeEmbedUrl) {
}

record Abstract(String title, String description) {
}

record Podcast(String id, String uid, URL episodeUri, URL episodePhotoUri, String description, Date date) {
}

record Appearance(String event, Date startDate, Date endDate, String time, String marketingBlurb) {
}

@Slf4j
@Service
class DefaultJoshlongService implements JoshlongService {

	private final HttpGraphQlClient client;

	DefaultJoshlongService(HttpGraphQlClient client) {
		this.client = client;
	}

	private record StringySpringTip(String blogUrl, String date, int seasonNumber, String title, String youtubeId,
			String youtubeEmbedUrl) {
	}

	private record StringyPodcast(String id, String uid, URL episodeUri, URL episodePhotoUri, String description,
			String date) {
	}

	@SneakyThrows
	private static URL buildUrlFrom(String href) {
		return new URL(href);
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

	private static List<SpringTip> buildSpringTipsList(List<StringySpringTip> stringySpringTips) {
		return stringySpringTips.stream()
				.map(st -> new SpringTip(buildUrlFrom(st.blogUrl()), buildDateFrom(st.date()), st.seasonNumber(),
						st.title(), st.youtubeId(), buildUrlFrom(st.youtubeEmbedUrl())))
				.sorted(Comparator.comparing(SpringTip::date)).toList();
	}

	@Override
	public Flux<Abstract> getAbstracts() {
		var query = """
				query { abstracts }
				""";
		var results = this.client//
				.document(query) //
				.retrieve("abstracts")//
				.toEntity(String.class);
		return results.flatMapMany(html -> Flux.fromIterable(parseAbstracts(html)));
	}

	private static List<Abstract> parseAbstracts(String html) {
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

		var list = new ArrayList<Abstract>();
		for (var e : accumulator.entrySet()) {
			var values = e.getValue();
			var joinedHtml = Strings.join(values.stream().iterator(), '\n');
			list.add(new Abstract(e.getKey(), joinedHtml));
		}
		return list;
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

	private static Podcast fromStringyPodcast(StringyPodcast podcast) {
		return new Podcast(podcast.id(), podcast.uid(), podcast.episodeUri(), podcast.episodePhotoUri(),
				podcast.description(), buildDateFrom(podcast.date()));
	}

	private record StringyAppearance(String event, String startDate, String endDate, String time,
			String marketingBlurb) {
	}

	private static Appearance fromStringAppearance(StringyAppearance appearance) {
		return new Appearance(appearance.event(), buildDateFrom(appearance.startDate()),
				buildDateFrom(appearance.endDate()), appearance.time(), appearance.marketingBlurb());
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
