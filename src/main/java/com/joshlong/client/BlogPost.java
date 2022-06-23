package com.joshlong.client;

import java.net.URL;
import java.util.Date;

public record BlogPost(String title, URL url, Date published, String description) {
}
