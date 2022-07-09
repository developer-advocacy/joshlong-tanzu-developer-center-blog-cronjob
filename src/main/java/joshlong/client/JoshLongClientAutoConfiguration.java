package joshlong.client;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.client.HttpGraphQlClient;

@Configuration
class JoshLongClientAutoConfiguration {

	@Bean
	HttpGraphQlClient httpGraphQlClient() {
		return HttpGraphQlClient.builder().url("https://api.joshlong.com/graphql")//
				.build();
	}

	@Bean
	@ConditionalOnMissingBean
	JoshLongClient joshLongClient() {
		return new DefaultJoshLongClient(this.httpGraphQlClient());
	}

}
