package com.joshlong.client.render;

import com.joshlong.client.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
class DefaultJoshLongMarkupRenderer implements JoshLongMarkupRenderer {

	private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

	@Override
	public <T> String renderGroup(String title, List<T> list) {

		var accumulatedMd = list//
				.stream()//
				.map(o -> {
					if (o instanceof Podcast p)
						return render(p);
					if (o instanceof Appearance a)
						return render(a);
					if (o instanceof BlogPost bp)
						return render(bp);
					if (o instanceof SpringTip st)
						return render(st);
					if (o instanceof TalkAbstract ta)
						return render(ta);
					throw new RuntimeException("can't render the type, " + o.getClass().getName() + "!");
				})//
				.collect(Collectors.joining(System.lineSeparator() + System.lineSeparator()));
		return String.format("""
				## %s

				%s
				""", title, accumulatedMd);
	}

	@Override
	public Mono<String> renderGroup(String title, Flux<?> list) {
		return list.collectList()//
				.flatMap(objects -> this.renderGroup(title, list));
	}

	@Override
	public String renderMarkdownAsHtml(String markdown) {
		var parser = Parser.builder().build();//
		var document = parser.parse(markdown);//
		var renderer = HtmlRenderer.builder().escapeHtml(false).build();
		return renderer.render(document);
	}

	@Override
	public String render(Appearance appearance) {
		var start = appearance.startDate();
		var event = appearance.event();
		var blurb = appearance.marketingBlurb();
		var md = """
				### %s

				**%s**

				%s
				""";
		return String.format(md, event, renderDate(start), blurb);
	}

	@Override
	public String render(Podcast podcast) {
		var md = """
				### %s
				**%s**

				[listen](%s)

				%s
				""";
		return String.format(md, podcast.title(), this.simpleDateFormat.format(podcast.date()), podcast.episodeUri(),
				podcast.description());
	}

	@Override
	public String render(SpringTip tip) {
		var url = tip.blogUrl();
		var title = tip.title();
		var seasonNo = tip.seasonNumber();
		var ytUrl = tip.youtubeEmbedUrl();
		var date = tip.date();
		var ytUrlHtml = String
				.format("""
						<iframe width="560" height="315" src="%s" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>
						"""
						.strip().trim().stripIndent(), tip.youtubeEmbedUrl());
		var md = """
				### %s

				**%s**

				%s
				""";
		return String.format(md, title, this.simpleDateFormat.format(date), ytUrlHtml);
	}

	@Override
	public String render(TalkAbstract talkAbstract) {
		return String.format("""
				### %s

				%s
				""", talkAbstract.title(), talkAbstract.description());
	}

	@Override
	public String render(BlogPost post) {
		return String.format("""
				### %s

				**%s**

				%s
				""", post.title(), this.simpleDateFormat.format(post.published()), post.description());
	}

	private String renderDate(Date date) {
		synchronized (this.simpleDateFormat) {
			return simpleDateFormat.format(date);
		}
	}

}