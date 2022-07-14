package feeds;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.io.SyndFeedInput;
import lombok.SneakyThrows;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class FeedClient {

	@SneakyThrows
	public <T> List<T> getBlogs(URL url, int count, Predicate<SyndEntry> filter,
			Function<SyndEntry, T> syndEntryTFunction) {

		try (var in = url.openStream(); var is = new InputStreamReader(in)) {
			var feed = new SyndFeedInput().build(is);
			return feed.getEntries()//
					.stream()//
					.filter(filter)//
					.sorted(Comparator.comparing(SyndEntry::getUpdatedDate))//
					.map(syndEntryTFunction)//
					.limit(count)//
					.toList();
		}
	}

}
