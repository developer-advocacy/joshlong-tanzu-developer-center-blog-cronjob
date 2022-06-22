package com.joshlong.client;

import java.net.URL;
import java.util.Date;

public record SpringTip(URL blogUrl, Date date, int seasonNumber, String title, String youtubeId, URL youtubeEmbedUrl) {
}
