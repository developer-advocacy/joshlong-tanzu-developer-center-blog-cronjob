package com.joshlong.client;

import joshlong.client.Appearance;
import joshlong.client.JoshLongClient;
import joshlong.client.Podcast;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StringUtils;

import java.util.function.Predicate;

@Slf4j
@SpringBootTest
class JoshLongClientTest {

	private final int max = 10;

	private final JoshLongClient service;

	JoshLongClientTest(@Autowired JoshLongClient service) {
		this.service = service;
	}

	@Test
	void blogs() {
		var blogs = this.service.getBlogPosts(this.max);
		Assertions.assertTrue(blogs.size() == 10, "there should only be 10 blogs");
		var next = blogs.iterator().next();
		Assertions.assertNotNull(next.published(), () -> "there should be a next entry");
		Assertions.assertTrue(StringUtils.hasText(next.title()), "the title should not be empty");
	}

	@Test
	void abstracts() {
		var abstracts = this.service.getAbstracts().stream()
				.anyMatch(ta -> StringUtils.hasText(ta.title()) && StringUtils.hasText(ta.description()));
		Assertions.assertTrue(abstracts);
	}

	@Test
	void springTips() throws Exception {
		var springTips = this.service.getSpringTips(this.max);
		var next = springTips.iterator().next();
		Assertions.assertEquals(springTips.size(), this.max);
		Assertions.assertNotNull(next.date());
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
		Assertions.assertEquals(appearances.size(), this.max);
		Assertions.assertTrue(appearancePredicate.test(appearances.iterator().next()));
	}

}