package com.example.feedprocessorv2;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.text.SimpleDateFormat;

@Slf4j
class JoshlongMarkdownRendererTest {

	private final JoshlongMarkdownRenderer renderer = new JoshlongMarkdownRenderer();

	private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

	@Test
	void springTip() throws Exception {
		var sourceDate = "2022-06-21";
		var date = this.simpleDateFormat.parse(sourceDate);
		var st = new SpringTip(new URL("http://youtube.com/springtips/1"), date, 10, "The spring tips title",
				"242fdshks922se", new URL("https://youtube.com/embed/90WRtrbRi0Y"));
		var stMd = this.renderer.render(st);
		var stHtml = this.renderer.renderMarkdownAsHtml(stMd);
		log.info("html: " + stHtml);
		Assertions.assertTrue(stHtml.contains("<h2>The spring tips title</h2>"));
		Assertions.assertTrue(stHtml.contains("<p><strong>2022-06-21</strong></p>"));
	}

	@Test
	void podcast() throws Exception {
		var sourceDate = "2022-06-21";
		var date = this.simpleDateFormat.parse(sourceDate);
		var podcast = new Podcast("id", "uid", "the podcast title", new URL("http://applepodcasts.com"),
				new URL("https://spotify.com/podcasts/photo"), "this is a description", date);
		var podcastMd = this.renderer.render(podcast);
		var podcastHtml = this.renderer.renderMarkdownAsHtml(podcastMd);
		log.info("html:" + podcastHtml);
		Assertions.assertTrue(podcastHtml.contains("""
				<a href="http://adobe.com">listen</a>
				""".trim().strip().stripIndent().stripLeading().stripTrailing()));
		Assertions.assertTrue(podcastHtml.contains("<p>this is a description</p>"));
		Assertions.assertTrue(podcastHtml.contains(sourceDate));
	}

	@Test
	void appearance() throws Exception {
		var eventTitle = "event title";
		var now = this.simpleDateFormat.parse("2022-06-21");
		var later = this.simpleDateFormat.parse("2022-06-22");
		var description = "Hi, Spring fans! It's going to be an amazing time as I return to fabulous Berlin, Germany, for a quick look at the latest-and-greatest";
		var md = this.renderer.render(new Appearance(eventTitle, now, later, "", description.strip()));
		var html = this.renderer.renderMarkdownAsHtml(md);
		Assertions.assertTrue(html.contains("<h2>" + eventTitle + "</h2>"));
		Assertions.assertTrue(html.contains("<p><strong>2022-06-21</strong></p>"));
		Assertions.assertTrue(html.contains("<p>" + description + "</p>"));
	}

}