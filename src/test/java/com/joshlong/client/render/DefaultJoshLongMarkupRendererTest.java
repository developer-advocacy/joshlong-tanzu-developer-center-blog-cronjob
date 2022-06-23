package com.joshlong.client.render;

import com.joshlong.client.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Slf4j
class DefaultJoshLongMarkupRendererTest {

	private final JoshLongMarkupRenderer renderer = new DefaultJoshLongMarkupRenderer();

	private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

	@Test
	void renderGroup() throws Exception {
		var accumulatedMd = this.renderer.renderGroup("Podcasts",
				List.of(new Podcast("id1", "uid1", "title1", new URL("http://episode1.com"),
						new URL("http://photo1.com"), "description1", new Date()),
						new Podcast("id2", "uid2", "title2", new URL("http://episode2.com"),
								new URL("http://photo2.com"), "description2", new Date())));
		log.info("md: " + accumulatedMd);
		var html = this.renderer.renderMarkdownAsHtml(accumulatedMd);
		log.info("html: " + html);
		Assertions.assertTrue(html.contains("<p>description2</p>"));
		Assertions.assertTrue(html.contains("<p>description1</p>"));
		Assertions.assertTrue(html.contains("<h3>title2</h3>"));
		Assertions.assertTrue(html.contains("<h3>title1</h3>"));
		Assertions.assertTrue(html.contains("<strong>2022-06-21</strong>"));
	}

	@Test
	void blogPost() throws Exception {
		var blogPost = new BlogPost("the title", new URL("http://www.google.com"),
				this.simpleDateFormat.parse("2022-03-16"), "the description");
		var md = this.renderer.render(blogPost);
		var html = this.renderer.renderMarkdownAsHtml(md);
		Assertions.assertTrue(html.contains("<h3>the title</h3>"));
		Assertions.assertTrue(html.contains("<p>the description</p>"));
	}

	@Test
	void talkAbstract() {
		var talkAbstract = new TalkAbstract("the title", "<p>this is the test</P><p>this is another test</p>");
		var md = this.renderer.render(talkAbstract);
		var html = this.renderer.renderMarkdownAsHtml(md);
		Assertions.assertTrue(html.contains("<h3>" + talkAbstract.title() + "</h3>"));
		Assertions.assertTrue(html.contains("" + talkAbstract.description() + ""));
	}

	@Test
	void springTip() throws Exception {
		var sourceDate = "2022-06-21";
		var date = this.simpleDateFormat.parse(sourceDate);
		var st = new SpringTip(new URL("http://youtube.com/springtips/1"), date, 10, "The spring tips title",
				"242fdshks922se", new URL("https://youtube.com/embed/90WRtrbRi0Y"));
		var stMd = this.renderer.render(st);
		var stHtml = this.renderer.renderMarkdownAsHtml(stMd);
		log.info("html: " + stHtml);
		Assertions.assertTrue(stHtml.contains("<h3>The spring tips title</h3>"));
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
				<a href="http://applepodcasts.com">listen</a>
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
		Assertions.assertTrue(html.contains("<h3>" + eventTitle + "</h3>"));
		Assertions.assertTrue(html.contains("<p><strong>2022-06-21</strong></p>"));
		Assertions.assertTrue(html.contains("<p>" + description + "</p>"));
	}

}