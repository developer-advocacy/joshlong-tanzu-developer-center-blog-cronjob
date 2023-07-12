package joshlong.client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.function.Predicate;

class JoshLongClientTest {

	private final int max = 10;

	private final HttpGraphQlClient graphQlClient = HttpGraphQlClient //
			.builder(WebClient.builder().baseUrl("https://api.joshlong.com/graphql").build()) //
			.build();

	private final JoshLongClient service = new DefaultJoshLongClient(this.graphQlClient);

	@Test
	void abstracts() {
		var abstracts = this.service.getAbstracts().stream()
				.anyMatch(ta -> StringUtils.hasText(ta.title()) && StringUtils.hasText(ta.description()));
		Assertions.assertTrue(abstracts);
	}

	@Test
	void springTips() {
		var springTips = this.service.getSpringTips(this.max);
		var next = springTips.iterator().next();
		Assertions.assertEquals(springTips.size(), this.max);
		Assertions.assertNotNull(next.published());
		Assertions.assertTrue(StringUtils.hasText(next.title()));
	}

	@Test
	void podcasts() {
		var podcasts = this.service.getPodcasts(this.max);
		Assertions.assertEquals(podcasts.size(), this.max);
		var matcher = (Predicate<Podcast>) podcast -> //
		podcast.date() != null && //
				StringUtils.hasText(podcast.description()) && //
				podcast.episodePhotoUri() != null && //
				podcast.episodeUri() != null && //
				StringUtils.hasText(podcast.uid());
		Assertions.assertTrue(matcher.test(podcasts.iterator().next()));
	}

	@Test
	void appearances() {
		var appearancePredicate = (Predicate<Appearance>) appearance -> //
		appearance.startDate().getTime() > 0;
		var appearances = this.service.getAppearances(this.max);
		Assertions.assertTrue(appearances.size() > 0);
		Assertions.assertTrue(appearancePredicate.test(appearances.iterator().next()));
	}

}
