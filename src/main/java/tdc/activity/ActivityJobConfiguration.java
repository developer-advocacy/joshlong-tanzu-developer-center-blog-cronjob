package tdc.activity;

import joshlong.client.JoshLongClient;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tdc.TdcProperties;
import tdc.blogs.BlogPostProducer;

import java.text.SimpleDateFormat;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "tdc.activity.enabled", havingValue = "true", matchIfMissing = true)
class ActivityJobConfiguration {

	private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

	@Bean
	ActivityJobRunner activityJobRunner(CredentialsProvider credentialsProvider, BlogPostProducer postProducer,
			JoshLongClient client, ActivityRenderer renderer, TdcProperties properties) {
		return new ActivityJobRunner(credentialsProvider, client, renderer, properties.activity().recentCount(),
				properties.activity().localClonePath(), properties.activity().githubFeedRepository(), postProducer);
	}

	@Bean
	ActivityRenderer renderer() {
		return new ActivityRenderer(this.simpleDateFormat);
	}

}
