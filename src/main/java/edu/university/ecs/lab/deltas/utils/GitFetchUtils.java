package edu.university.ecs.lab.deltas.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

public class GitFetchUtils {
  private static final String GITHUB_API_URL = "https://api.github.com/repos/";
  private static final Gson gson = new Gson();

  public Repository establishLocalEndpoint(String path) throws IOException {
    File localRepoDir = new File(path);

    return new FileRepositoryBuilder().setGitDir(new File(localRepoDir, ".git")).build();
  }

  public List<DiffEntry> fetchRemoteDifferences(Repository repo, String branch) throws Exception {
    try (Git git = new Git(repo)) {
      // fetch latest changes from remote
      git.fetch().call();

      try (ObjectReader reader = repo.newObjectReader()) {
        // get the difference between local main and origin/main
        return git.diff()
            .setOldTree(prepareTreeParser(reader, repo, "refs/remotes/origin/" + branch))
            .setNewTree(prepareTreeParser(reader, repo, "refs/heads/" + branch))
            .call();
      }
    }
  }

  private CanonicalTreeParser prepareTreeParser(ObjectReader reader, Repository repo, String ref)
      throws IOException {
    try (RevWalk walk = new RevWalk(reader)) {
      Ref head = repo.exactRef(ref);
      RevCommit commit = repo.parseCommit(head.getObjectId());
      RevTree tree = walk.parseTree(commit.getTree().getId());

      CanonicalTreeParser treeParser = new CanonicalTreeParser();
      try (ObjectReader newReader = walk.getObjectReader()) {
        treeParser.reset(newReader, tree.getId());
      }

      walk.dispose();
      return treeParser;
    }
  }

  public String getGithubFileUrl(Repository repo, DiffEntry entry) {
    Config config = repo.getConfig();
    String remoteUrl = config.getString("remote", "origin", "url");
    String[] urlSegments = remoteUrl.split("/");

    // extract owner/repo name
    String owner = urlSegments[urlSegments.length - 2];
    String repoName = urlSegments[urlSegments.length - 1].replace(".git", "");

    return GITHUB_API_URL + owner + "/" + repoName + "/contents/" + entry.getNewPath();
  }

  public String fetchAndDecodeFile(String url) throws IOException {
    String changeData = fetchJsonFromUrl(url);
    JsonObject changeDetails = gson.fromJson(changeData, JsonObject.class);
    String encodedContent =
        changeDetails.getAsJsonObject().get("content").getAsString().replaceAll("\\s", "");
    return new String(Base64.getDecoder().decode(encodedContent), StandardCharsets.UTF_8);
  }

  private String fetchJsonFromUrl(String url) throws IOException {
    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
      HttpGet request = new HttpGet(url);
      //      request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + ACCESS_TOKEN);
      //      request.setHeader(HttpHeaders.ACCEPT, "application/vnd.github.v3+json");

      return EntityUtils.toString(httpClient.execute(request).getEntity());
    }
  }
}
