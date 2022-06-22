package com.example.feedprocessorv2;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;

@Slf4j
class JoshlongMarkdownRendererTest {

	private final JoshlongMarkdownRenderer renderer = new JoshlongMarkdownRenderer();

	private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

	@Test
	void appearance() throws Exception {
		var eventTitle = "event title";
		var now = this.simpleDateFormat.parse("2022-06-21");
		var later = this.simpleDateFormat.parse("2022-06-22");
		var description = "Hi, Spring fans! It's going to be an amazing time as I return to fabulous Berlin, Germany, for a quick look at the latest-and-greatest";
		var md = renderer.render(new Appearance(eventTitle, now, later, "", description.strip()));
		var html = renderer.renderMarkdownAsHtml(md);
		Assertions.assertTrue(html.contains("<h2>" + eventTitle + "</h2>"));
		Assertions.assertTrue(html.contains("<p><strong>2022-06-21</strong></p>"));
		Assertions.assertTrue(html.contains("<p>" + description + "</p>"));
	}

}