package com.example.feedprocessorv2;

import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.net.URL;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.stream.Collectors;

/**
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

    Flux<Appearance> getAppearances();

    Flux<Podcast> getPodcasts();
}

record Podcast(String id, String uid,
               URL episodeUri,
               URL episodePhotoUri,
               String description,
               Date date) {
}


@Service
class DefaultJoshlongService implements JoshlongService {

    private final HttpGraphQlClient client;

    DefaultJoshlongService(HttpGraphQlClient client) {
        this.client = client;
    }

    private record StringyPodcast(String id, String uid,
                          URL episodeUri,
                          URL episodePhotoUri,
                          String description,
                          String  date) {
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
                .map(sa -> sa.stream().map(DefaultJoshlongService::fromStringAppearance) //
                        .sorted(Comparator.comparing(Appearance::startDate)).toList());
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
        return client
                .document(podcasts)
                .retrieve("podcasts")
                .toEntityList(StringyPodcast.class)
                .map(list -> list.stream().map(DefaultJoshlongService::fromStringyPodcast).collect(Collectors.toList()))
                .flatMapMany(Flux::fromIterable);
    }

    private static Podcast fromStringyPodcast(StringyPodcast podcast) {
        return  new Podcast(  podcast.id() , podcast.uid(),
                 podcast.episodeUri() ,  podcast.episodePhotoUri() , podcast.description(),
                buildDateFrom(podcast.date())
                );
    }


    private record StringyAppearance(String event, String startDate, String endDate, String time,
                                     String marketingBlurb) {
    }

    private static Appearance fromStringAppearance(StringyAppearance appearance) {
        return new Appearance(appearance.event(),
                buildDateFrom(appearance.startDate()),
                buildDateFrom(appearance.endDate()),
                appearance.time(), appearance.marketingBlurb());
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

record Appearance(String event, Date startDate, Date endDate, String time, String marketingBlurb) {
}