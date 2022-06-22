package com.joshlong.client;

import java.util.Date;

public record BlogPost(String title, Date published, String description) {
}
