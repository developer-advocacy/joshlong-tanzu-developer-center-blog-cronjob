package com.joshlong;

import com.joshlong.client.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.nativex.hint.TypeHint;

/**
 * This provides a client to get access to all of my blogs, spring tips videos, talk
 * abstracts, appearances, and podcasts
 *
 * @author Josh Long
 */
@TypeHint(types = { Appearance.class, Podcast.class, SpringTip.class, TalkAbstract.class, BlogPost.class, },
		typeNames = { "com.joshlong.client.StringyAppearance", "com.joshlong.client.StringyPodcast",
				"com.joshlong.client.StringySpringTip" })
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
