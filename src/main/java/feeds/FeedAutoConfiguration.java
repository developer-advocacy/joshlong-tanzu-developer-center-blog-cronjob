package feeds;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class FeedAutoConfiguration {

	@Bean
	FeedClient feedClient() {
		return new FeedClient();
	}

}
