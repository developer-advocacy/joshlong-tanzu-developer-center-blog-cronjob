package tdc.blogs;

import joshlong.client.BlogPost;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;

import java.net.URL;
import java.text.SimpleDateFormat;

@Slf4j
@RequiredArgsConstructor
class BlogPostRenderer {

	private final SimpleDateFormat simpleDateFormat;

	private final String template = """
			---
			canonical: %s
			date: %s
			description: %s
			featured: false
			patterns:
			- Deployment
			tags:
			- Spring
			- Kubernetes
			- DevOps
			- Microservices
			- Integration
			- Data
			- Batch
			- Cloud
			team:
			- Josh Long
			title: '%s'
			---

			%s

			""";

	@SneakyThrows
	private String readBlogPostContentFrom(URL url) {
		var doc = Jsoup.parse(url, 5000);
		var body = doc.getElementsByClass("blog--post");
		var bodyHtml = body.html();
		if (log.isDebugEnabled()) {
			log.debug("trying to open the url " + url.toString());
			log.debug("the body is " + bodyHtml);
		}
		return bodyHtml;
	}

	@SneakyThrows
	public String render(BlogPost post) {
		var description = post.description();
		var title = this.cleanTitleForMetadata(post.title());
		var content = this.readBlogPostContentFrom(post.url());
		var templateDate = this.simpleDateFormat.format(post.published());
		return String.format(this.template, post.url().toExternalForm(), templateDate, description, title, content);
	}

	private String cleanTitleForMetadata(String title) {
		var nb = new StringBuilder();
		for (var c : title.toCharArray()) {
			if (c == '\'')
				nb.append("");
			else
				nb.append(c);
		}
		return nb.toString();
	}

}
