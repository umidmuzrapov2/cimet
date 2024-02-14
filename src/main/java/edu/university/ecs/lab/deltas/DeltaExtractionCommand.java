package edu.university.ecs.lab.deltas;

import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

public class DeltaExtractionCommand {
  private static final String GITHUB_API_URL = "https://api.github.com/repos/";
  private static final Gson gson = new Gson();

  /**
   * main method entry to delta extraction
   *
   * @param args command line args list of "owner/user" repo format
   */
  public static void main(String[] args) throws Exception {
    for (String ownerRepo : args) {
      String[] repoSplit = ownerRepo.split("/");
      String owner = repoSplit[0];
      String repo = repoSplit[1];

      try {
        String commitsURL = GITHUB_API_URL + owner + "/" + repo + "/commits/main";
        System.out.println("Extracting remote commits: " + commitsURL);

        // get latest commit information
        String commitsData = fetchJsonFromUrl(commitsURL);
        JsonObject latestCommit = gson.fromJson(commitsData, JsonObject.class);

        // write commit info to json
        writeJsonToFile(latestCommit, owner + "-" + repo + "-["+(new Date()).getTime()+"].json");

        // extract latest commit contents
        String latestCommitUrl = latestCommit.getAsJsonObject()
                .get("files").getAsJsonArray()
                .get(0).getAsJsonObject()
                .get("contents_url").getAsString();

        String latestCommitData = fetchJsonFromUrl(latestCommitUrl);
        JsonObject latestCommitDetails = gson.fromJson(latestCommitData, JsonObject.class);
        String encodedContent = latestCommitDetails.getAsJsonObject().get("content").getAsString().replaceAll("\\s", "");;

        String fileContents = new String(Base64.getDecoder().decode(encodedContent), StandardCharsets.UTF_8);

        // TODO: compare differences with current file

      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private static String fetchJsonFromUrl(String url) throws IOException {
    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
      HttpGet request = new HttpGet(url);
//      request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + ACCESS_TOKEN);
//      request.setHeader(HttpHeaders.ACCEPT, "application/vnd.github.v3+json");

      return EntityUtils.toString(httpClient.execute(request).getEntity());
    }
  }

  private static void writeJsonToFile(JsonObject jsonObject, String fileName) throws IOException {
    Gson gsonWithIndentation = new GsonBuilder().setPrettyPrinting().create();

    try (FileWriter fileWriter = new FileWriter(fileName)) {
      // Create a JsonWriter with indentation
      JsonWriter jsonWriter = gsonWithIndentation.newJsonWriter(fileWriter);

      // Start writing the JSON array
      jsonWriter.beginArray();

      // Iterate through files and write them to JSON
      gson.toJson(jsonObject, jsonWriter);

      // End writing the JSON array
      jsonWriter.endArray();

    }
  }
}
