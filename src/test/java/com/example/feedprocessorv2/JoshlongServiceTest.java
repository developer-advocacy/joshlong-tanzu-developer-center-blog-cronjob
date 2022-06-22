package com.example.feedprocessorv2;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StringUtils;
import reactor.test.StepVerifier;

import java.util.function.Predicate;

@Slf4j
@SpringBootTest
class JoshlongServiceTest {

	private final JoshlongService service;

	JoshlongServiceTest(@Autowired JoshlongService service) {
		this.service = service;
	}

	@Test
	void abstracts() throws Exception {
		var abstracts = this.service.getAbstracts()//
				.doOnNext(anAbstract -> log
						.info(anAbstract.title() + "\n" + anAbstract.description() + "\n" + "\n" + "\n"));
		StepVerifier.create(abstracts).expectNextCount(13).verifyComplete();
	}

	@Test
	void springTips() throws Exception {
		var springTips = this.service.getSpringTips();
		StepVerifier//
				.create(springTips.take(1))//
				.expectNextMatches(springTip -> springTip.date() != null && StringUtils.hasText(springTip.title())
						&& springTip.blogUrl() != null && springTip.youtubeEmbedUrl() != null)
				.verifyComplete();
	}

	@Test
	void podcasts() {
		var anyInvalidRecords = this.service.getPodcasts().any(podcast -> {
			var valid = podcast.date() != null && StringUtils.hasText(podcast.description())
					&& podcast.episodePhotoUri() != null && podcast.episodeUri() != null
					&& StringUtils.hasText(podcast.uid());
			return !valid;
		});
		StepVerifier.create(anyInvalidRecords).expectNext(false).verifyComplete();
	}

	@Test
	void appearances() {
		var appearancePredicate = (Predicate<Appearance>) appearance -> appearance.startDate().getTime() < appearance
				.endDate().getTime();
		var filter = this.service.getAppearances()
				.filter(appearance -> appearance.startDate() != null && appearance.endDate() != null)
				.any(appearancePredicate);
		StepVerifier.create(filter).expectNext(true).verifyComplete();
	}

}