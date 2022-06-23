package com.joshlong.client.render;

import com.joshlong.client.*;

import java.util.List;

public interface JoshLongMarkupRenderer {

	<T> String renderGroup(String title, List<T> list);

	String renderMarkdownAsHtml(String markdown);

	String render(Appearance appearance);

	String render(Podcast podcast);

	String render(SpringTip tip);

	String render(TalkAbstract talkAbstract);

	String render(BlogPost post);

}
