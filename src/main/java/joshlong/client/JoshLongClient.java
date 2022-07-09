package joshlong.client;

import java.util.List;

public interface JoshLongClient {

	List<BlogPost> getBlogPosts(int count);

	List<SpringTip> getSpringTips(int count);

	List<TalkAbstract> getAbstracts();

	List<Appearance> getAppearances(int count);

	List<Podcast> getPodcasts(int count);

}
