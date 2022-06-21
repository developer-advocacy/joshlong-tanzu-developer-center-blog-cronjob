package com.example.feedprocessorv2;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StringUtils;
import reactor.test.StepVerifier;

import java.util.function.Predicate;

@SpringBootTest
class JoshlongServiceTest {

    private final JoshlongService service;

    JoshlongServiceTest(@Autowired JoshlongService service) {
        this.service = service;
    }

    @Test
    void podcasts() {
        var anyInvalidRecords = this.service.getPodcasts()
                .any(podcast -> {
                    var valid = podcast.date() != null &&
                            StringUtils.hasText(podcast.description()) &&
                            podcast.episodePhotoUri() != null &&
                            podcast.episodeUri() != null &&
                            StringUtils.hasText(podcast.uid());
                    return !valid;
                });
        StepVerifier.create(anyInvalidRecords).expectNext(false).verifyComplete();
    }

    @Test
    void appearances() {
        var appearancePredicate =
                (Predicate<Appearance>) appearance -> appearance.startDate().getTime() < appearance.endDate().getTime();
        var filter = this.service
                .getAppearances()
                .filter(appearance -> appearance.startDate() != null &&
                        appearance.endDate() != null)
                .any(appearancePredicate);
        StepVerifier.create(filter).expectNext(true).verifyComplete();
    }
}