package github;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GitHub;
import org.springframework.util.Assert;

import java.io.File;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

import static org.springframework.util.FileSystemUtils.deleteRecursively;

/**
 * This client <a href="https://github-api.kohsuke.org/"uses this library extensively<a/>
 */
@Slf4j
@RequiredArgsConstructor
public class GithubPullRequestClient {

	private final GitHub github;

	private final UsernamePasswordCredentialsProvider credentials;

	public interface PullRequestProcessor {

		/**
		 * A callback in which the client can modify the cloned repository
		 * @param root the directory in which the cloned repository lives
		 * @param git a live {@link Git} reference for the newly-cloned {@code git}
		 * repository
		 * @throws Throwable feel free to throw your {@link Throwable}s here and the
		 * framework will handle them centrally.
		 */
		boolean modifyFileSystem(File root, Git git, Date date) throws Throwable;

	}

	/**
	 * This method does the work of forking a repo, cloning it, allowing the client to
	 * manipulate the filesystem, commit them, then push those changes to a branch and
	 * then send the changes as a pull-request to the origin repository that was forked.
	 * @param originRepo the URI of the repo to which you'd like to send a pull-request
	 * @param forkRepo the URI of the repo containing the changes you'd like to send in a
	 * pull-request
	 * @param head the name of the user whose forked version of this repository we want to
	 * draw changes from
	 * @param base the target to which we want to send the changes
	 * @param pullRequestTitle what should the title of the pull-request be in the github
	 * ui?
	 * @param pullRequestBody what should the body of the pull-request be in the github
	 * ui?
	 * @param cloneDirectory the directory to which the forked version of the repository
	 * should be cloned locally
	 * @param processor a callback that gives the client a chance to modify the file
	 * system and contribute changes
	 * @return a pull request signalling the proposed pull-request
	 */
	@SneakyThrows
	public GHPullRequest createPullRequest(URI originRepo, URI forkRepo, String head, String base,
			String pullRequestTitle, String pullRequestBody, File cloneDirectory, PullRequestProcessor processor) {

		// delete the fork of the repository if it exists
		var forkReference = buildValidRepositoryReferenceFrom(forkRepo);
		deleteForkIfRequired(github, forkReference);

		// fork the origin repository so we have the guaranteed latest, most-up-to-date
		// repo in which to make our changes
		var originReference = buildValidRepositoryReferenceFrom(originRepo);
		var originRepository = github.getRepository(originReference);
		originRepository.fork();

		// clone the fork onto local machine so we can manipulate it
		Assert.isTrue((!cloneDirectory.exists() || deleteRecursively(cloneDirectory)) && !cloneDirectory.exists(),
				"we can't clone if there's already a fully fleshed");
		var git = buildGit(forkRepo, cloneDirectory, credentials);
		Assert.isTrue(cloneDirectory.exists() && Objects.requireNonNull(cloneDirectory.list()).length > 0,
				() -> "the directory must be cloned locally");

		// manipulate local clone in a new branch
		var date = new Date();
		var newBranchName = "proposed-changes-" + buildTimestampFor(date);
		git.branchCreate().setName(newBranchName).call();
		git.checkout().setName(newBranchName).call();
		var sendPrQuestion = processor.modifyFileSystem(cloneDirectory, git, date);
		if (sendPrQuestion) {
			// commit all changes
			git.push().setPushAll().setCredentialsProvider(credentials).call();
			Assert.isTrue(github.isCredentialValid(), () -> "the Github repository is valid");

			// send PR
			var pullRequest = originRepository.createPullRequest(pullRequestTitle, head + ":" + newBranchName, base,
					pullRequestBody, true);
			log("pull request state? " + pullRequest.toString());
			return pullRequest;
		}
		log.info("not sending a pull-request as nothing has changed. ");
		return null;
	}

	private static String buildTimestampFor(Date date) {
		return buildTimestampFor(date, ZoneId.systemDefault());
	}

	private static String buildTimestampFor(Date date, ZoneId zoneId) {
		var i = date.toInstant();
		var now = LocalDateTime.ofInstant(i, zoneId);
		var year = now.getYear();
		var month = now.getMonthValue();
		var day = now.getDayOfMonth();
		var hour = now.getHour();
		var minute = now.getMinute();
		var second = now.getSecond();
		return String.format("%s_%s_%s_%s_%s_%s", year, month, day, hour, minute, second);
	}

	private static Git buildGit(URI repositoryToClone, File localDirectory, CredentialsProvider provider)
			throws Exception {
		var uri = repositoryToClone.toString();
		log("the uri is " + uri);
		return Git//
				.cloneRepository()//
				.setCredentialsProvider(provider)//
				.setURI(uri)//
				.setBranch("main")//
				.setDirectory(localDirectory)//
				.call();
	}

	@SneakyThrows
	private static String buildValidRepositoryReferenceFrom(URI uri) {
		var uriPath = uri.getPath();
		var suffix = ".git";
		if (uriPath.startsWith("/")) {
			uriPath = uriPath.substring(1);
		}
		if (uriPath.endsWith(suffix)) {
			uriPath = uriPath.substring(0, uriPath.length() - suffix.length());
		}
		log("building reference " + uriPath + " from uri " + uri);
		return uriPath;
	}

	private static void log(String message) {
		if (log.isDebugEnabled())
			log.debug(message);
	}

	private static void log(Throwable throwable) {
		if (log.isDebugEnabled())
			log.debug("oops!", throwable);
	}

	/**
	 * deletes the repository if it already exists. don't want any problems when we later
	 * try to fork the origin repository.
	 */
	@SneakyThrows
	private static void deleteForkIfRequired(GitHub github, String forkReference) {
		try {
			log("the fork reference is " + forkReference);
			var repo = github.getRepository(forkReference);
			if (repo != null) {
				github.getRepository(forkReference).delete();
				log("deleted " + forkReference);
			}
		} //
		catch (Exception exception) {
			log(exception);
		}
	}

}
