package joshlong.client;

import java.net.URL;
import java.util.Date;

public record Podcast(String id, String uid, String title, URL episodeUri, URL episodePhotoUri, String description,
		Date date) {
}
