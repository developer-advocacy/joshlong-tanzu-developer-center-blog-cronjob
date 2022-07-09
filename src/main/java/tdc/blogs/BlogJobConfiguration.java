package tdc.blogs;

import joshlong.client.JoshLongClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import tdc.TdcProperties;

import java.io.File;

@Slf4j
@Configuration
class BlogJobConfiguration {

	@Bean
	Runner runner(TdcProperties properties, JoshLongClient client) {
		return new Runner(properties.blog().localClonePath(), properties.blog().recentCount(), client);
	}

}

/*
 * this gets all the blogs
 */
@RequiredArgsConstructor
class Runner implements ApplicationRunner {

	private final File root;

	private final int recentCount;

	private final JoshLongClient client;

	@Override
	public void run(ApplicationArguments args) throws Exception {

		var blogContent = new File(this.root, "content/blog");
		Assert.isTrue(blogContent.exists(), () -> "the blog content folder does not exist!");

		var blogs = this.client.getBlogPosts(this.recentCount);

	}

}