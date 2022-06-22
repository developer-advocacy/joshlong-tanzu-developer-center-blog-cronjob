package com.joshlong.client;

import reactor.core.publisher.Flux;

import java.util.List;

public interface JoshLongClient {

	List<BlogPost> getBlogPosts();

	List<SpringTip> getSpringTips();

	List<TalkAbstract> getAbstracts();

	List<Appearance> getAppearances();

	List<Podcast> getPodcasts();

}
