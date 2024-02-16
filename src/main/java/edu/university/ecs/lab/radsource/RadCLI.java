package edu.university.ecs.lab.radsource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.university.ecs.lab.radsource.context.RadSourceRequestContext;
import edu.university.ecs.lab.radsource.context.RadSourceResponseContext;
import edu.university.ecs.lab.radsource.service.RadSourceService;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * This class is the CLI runner for the RAD CLI.
 *
 * @author Dipta Das
 */
public class RadCLI {

  public static void main(String[] args) throws IOException {
    RadSourceService restDiscoveryService = new RadSourceService();
    RadSourceRequestContext request = new RadSourceRequestContext(List.of(args));
    RadSourceResponseContext responseContext = restDiscoveryService.generateRadSourceResponseContext(request);

    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    FileWriter fw = new FileWriter("output.json");

    gson.toJson(responseContext, fw);
    fw.close();
  }
}
