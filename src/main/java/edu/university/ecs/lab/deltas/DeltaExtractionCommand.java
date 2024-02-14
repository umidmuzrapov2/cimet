package edu.university.ecs.lab.deltas;

import com.google.gson.*;
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

import javax.json.*;
import javax.json.stream.JsonGenerator;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class DeltaExtractionCommand {
  private static final String GITHUB_API_URL = "https://api.github.com/repos/";
  private static final Gson gson = new Gson();

  /**
   * main method entry to delta extraction
   *
   * @param args command line args list containing /path/to/repo(s)
   */
  public static void main(String[] args) throws Exception {
    for (String path : args) {
      Repository localRepo = establishLocalEndpoint(path);
      List<DiffEntry> differences = fetchRemoteDifferences(localRepo, "main");
      processDifferences(path, localRepo, differences);
    }
  }

  private static Repository establishLocalEndpoint(String path) throws IOException {
    File localRepoDir = new File(path);

    return new FileRepositoryBuilder()
            .setGitDir(new File(localRepoDir, ".git"))
            .build();
  }

  private static List<DiffEntry> fetchRemoteDifferences(Repository repo, String branch) throws Exception {
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

  private static CanonicalTreeParser prepareTreeParser(ObjectReader reader,
                                                       Repository repo, String ref) throws IOException {
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

  private static void processDifferences(String path, Repository repo, List<DiffEntry> diffEntries) throws IOException {
    // process each difference
    for (DiffEntry entry : diffEntries) {
      System.out.println("Change impact of type " + entry.getChangeType() + " detected in " + entry.getNewPath());

      String changeURL = getGithubFileUrl(repo, entry);
      System.out.println("Extracting changes from: " + changeURL);

      // fetch changed file
      String fileContents = fetchAndDecodeFile(changeURL);

      // compare differences with local path
      String localPath = path + "/" + entry.getOldPath();
      javax.json.JsonArray deltaChanges = extractDeltaChanges(fileContents, localPath);

      JsonObjectBuilder jout = Json.createObjectBuilder();
      jout.add("local-file", localPath);
      jout.add("remote-api", changeURL);
      jout.add("changes", deltaChanges);

      // write differences to output file
      String outputName = "delta-changes-["+ (new Date()).getTime() + "]-" + entry.getNewId().name() + ".json";
      writeJsonToFile(jout.build(), outputName);

      System.out.println("Delta extracted: " + outputName);
    }
  }

  private static String getGithubFileUrl(Repository repo, DiffEntry entry) {
    Config config = repo.getConfig();
    String remoteUrl = config.getString("remote", "origin", "url");
    String[] urlSegments = remoteUrl.split("/");

    // extract owner/repo name
    String owner = urlSegments[urlSegments.length - 2];
    String repoName = urlSegments[urlSegments.length - 1].replace(".git", "");

    return GITHUB_API_URL + owner + "/" + repoName + "/contents/" + entry.getNewPath();
  }

  private static String fetchAndDecodeFile(String url) throws IOException {
    String changeData = fetchJsonFromUrl(url);
    JsonObject changeDetails = gson.fromJson(changeData, JsonObject.class);
    String encodedContent = changeDetails.getAsJsonObject().get("content").getAsString().replaceAll("\\s", "");
    return new String(Base64.getDecoder().decode(encodedContent), StandardCharsets.UTF_8);
  }

  private static String fetchJsonFromUrl(String url) throws IOException {
    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
      HttpGet request = new HttpGet(url);
//      request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + ACCESS_TOKEN);
//      request.setHeader(HttpHeaders.ACCEPT, "application/vnd.github.v3+json");

      return EntityUtils.toString(httpClient.execute(request).getEntity());
    }
  }

  private static javax.json.JsonArray extractDeltaChanges(String decodedFile, String pathToLocal) throws IOException {
    JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

    BufferedReader reader = new BufferedReader(new FileReader(pathToLocal));
    String line;
    int i = 0;

    String[] decodedLines = decodedFile.split("\n");

    while ((line = reader.readLine()) != null) {
      // record each line-by-line difference
      while (!line.equals(decodedLines[i])) {
        jsonArrayBuilder.add("line "+(i+1));
        i++;
      }

      i++;
    }

    return jsonArrayBuilder.build();
  }

  private static void writeJsonToFile(javax.json.JsonObject jsonOut, String filePath) throws IOException {
    try (FileWriter writer = new FileWriter(filePath)) {
      Map<String, Object> properties = new HashMap<>();
      properties.put(JsonGenerator.PRETTY_PRINTING, true);
      JsonWriterFactory writerFactory = Json.createWriterFactory(properties);

      JsonWriter jsonWriter = writerFactory.createWriter(writer);
      jsonWriter.write(jsonOut);
      jsonWriter.close();
    }
  }
}
