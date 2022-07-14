package joshlong.client;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.jsoup.Jsoup;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
class DefaultJoshLongClient implements JoshLongClient {

	private final HttpGraphQlClient client;

	DefaultJoshLongClient(HttpGraphQlClient client) {
		this.client = client;
	}

	/*
	 * @Override public List<BlogPost> getBlogPosts(int count) { return
	 * this.posts.stream()// .limit(count)//
	 * .sorted(Comparator.comparing(BlogPost::published).reversed())//
	 * .collect(Collectors.toList()); }
	 */

	@Override
	public List<TalkAbstract> getAbstracts() {
		var query = """
				query { abstracts }
				""";
		var results = this.client//
				.document(query) //
				.retrieve("abstracts")//
				.toEntity(String.class);
		return results//
				.flatMapMany(html -> Flux.fromIterable(parseAbstracts(html)))//
				.collectList()//
				.block();
	}

	@Override
	public List<SpringTip> getSpringTips(int count) {
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
				.flatMapMany(list -> Flux.fromIterable(buildSpringTipsList(count, list)))//
				.collectList() //
				.block();
	}

	@Override
	public List<Appearance> getAppearances(int count) {
		var query = """
				{ appearances { event, startDate, endDate, time, marketingBlurb } }
				""";
		var sevenDaysAgo = Instant.now().minus(Duration.ofDays(7));
		var ldt = Date.from(sevenDaysAgo);
		var stringy = client //
				.document(query)//
				.retrieve("appearances") //
				.toEntityList(StringyAppearance.class) //
				.map(sa -> sa.stream()//
						.map(DefaultJoshLongClient::fromStringAppearance) //
						.takeWhile(appearance -> appearance.startDate().after(ldt))//
						.sorted(Comparator.comparing(Appearance::startDate).reversed())//
						.limit(count)//
						.sorted(Comparator.comparing(Appearance::startDate))//
						.toList()//
				);
		return stringy.flatMapMany(Flux::fromIterable).collectList().block();
	}

	@Override
	public List<Podcast> getPodcasts(int count) {
		var podcasts = """
				{
				  podcasts {
				    id
				    uid
				    episodeUri
				    episodePhotoUri
				    description
				    date
				    title
				  }
				}
				""";
		return client//
				.document(podcasts)//
				.retrieve("podcasts")//
				.toEntityList(StringyPodcast.class)//
				.map(list -> list.stream()//
						.map(DefaultJoshLongClient::fromStringyPodcast)//
						.sorted(Comparator.comparing(Podcast::date).reversed()).limit(count) //
						.collect(Collectors.toList())) //
				.flatMapMany(Flux::fromIterable) //
				.collectList()//
				.block();
	}

	private static List<SpringTip> buildSpringTipsList(int count, List<StringySpringTip> stringySpringTips) {
		return stringySpringTips//
				.stream()//
				.map(st -> new SpringTip(//
						buildUrlFrom(st.blogUrl()), //
						buildDateFrom(st.date()), //
						st.seasonNumber(), //
						st.title(), //
						st.youtubeId(), //
						buildUrlFrom(st.youtubeEmbedUrl())//
				))//
				.sorted(Comparator.comparing(SpringTip::date).reversed())//
				.limit(count) //
				.toList();
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
		return new Podcast(podcast.id(), podcast.uid(), podcast.title(), podcast.episodeUri(),
				podcast.episodePhotoUri(), podcast.description(), buildDateFrom(podcast.date()));
	}
	/*
	 * @SneakyThrows private List<BlogPost> initFeed() { var url =
	 * buildUrlFrom("https://spring.io/blog/category/engineering.atom"); try (var in =
	 * url.openStream(); var is = new InputStreamReader(in)) { var feed = new
	 * SyndFeedInput().build(is); return feed.getEntries().stream() .filter(se ->
	 * se.getAuthors().stream().anyMatch(s -> s.getName().contains("Josh Long"))) .map(se
	 * -> new BlogPost(se.getTitle(), buildUrlFrom(se.getLink()), se.getUpdatedDate(),
	 * "")) .sorted(Comparator.comparing(BlogPost::published).reversed()).toList(); } }
	 */

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

	record StringyAppearance(String event, String startDate, String endDate, String time, String marketingBlurb) {
	}

	record StringySpringTip(String blogUrl, String date, int seasonNumber, String title, String youtubeId,
			String youtubeEmbedUrl) {
	}

	record StringyPodcast(String id, String uid, String title, URL episodeUri, URL episodePhotoUri, String description,
			String date) {
	}

}
