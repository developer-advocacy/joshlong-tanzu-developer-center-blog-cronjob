/*
 * package com.joshlong.github;
 *
 * import static org.junit.jupiter.api.Assertions.*;
 *
 *
 * import com.joshlong.PromoterProperties; import github.GithubClient; import
 * lombok.extern.slf4j.Slf4j; import org.junit.jupiter.api.Test; import
 * org.kohsuke.github.GHPullRequest; import
 * org.springframework.beans.factory.annotation.Autowired; import
 * org.springframework.boot.test.context.SpringBootTest; import
 * org.springframework.util.FileCopyUtils;
 *
 * import java.io.File; import java.io.FileWriter; import java.net.URI; import
 * java.util.Objects;
 *
 * @Slf4j
 *
 * @SpringBootTest class GithubClientTest {
 *
 * private final GithubClient client;
 *
 * private final PromoterProperties properties;
 *
 * GithubClientTest(@Autowired PromoterProperties properties, @Autowired GithubClient
 * client) { this.client = client; this.properties = properties; }
 *
 * @Test void createPullRequest() throws Exception { var localClonePath =
 * this.properties.localClonePath(); var githubProperties = this.properties.gitHub(); var
 * pr = this.client.createPullRequest(// githubProperties.username(), //
 * githubProperties.personalAccessToken(), // new
 * URI("https://github.com/developer-advocacy/github-test.git"), // new
 * URI("https://github.com/joshlong/github-test.git"), // "joshlong", // "main", //
 * "Please add this new content to the repository", // "# This new blog is an appearance",
 * // localClonePath, // (rootDirectory, git, date) -> {// var newFile = new
 * File(rootDirectory, "test.md"); try (var writer = new FileWriter(newFile)) {
 * FileCopyUtils.copy("the time is " + date.toInstant().toString(), writer); }
 * log.info("the file is " + rootDirectory.getAbsolutePath()); for (var f :
 * Objects.requireNonNull(rootDirectory.listFiles())) log.info("file: " +
 * f.getAbsolutePath()); git.add().addFilepattern(newFile.getName()).call();
 * git.commit().setMessage("adding " + newFile.getName() + '.').call(); });
 * assertNotNull(pr, "the " + GHPullRequest.class.getName() + " is not null");
 * assertEquals(1, pr.getCommits(), "there should be 1 commit in the PR"); }
 *
 * }
 */
