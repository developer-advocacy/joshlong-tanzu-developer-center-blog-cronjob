package tdc.blogs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tdc.TdcProperties;

import java.io.File;

@Slf4j
@Configuration
class BlogJobConfiguration {

	@Bean
	Runner runner(TdcProperties properties) {
		return new Runner(properties.blog().localClonePath());
	}

}

@RequiredArgsConstructor
class Runner implements ApplicationRunner {

	private final File root;

	@Override
	public void run(ApplicationArguments args) throws Exception {

	}

}