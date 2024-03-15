package edu.university.ecs.lab.intermediate.merger.utils;

import com.google.gson.Gson;
import edu.university.ecs.lab.intermediate.merger.models.Delta;
import edu.university.ecs.lab.intermediate.merger.models.MsSystem;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

public class IRParserUtils {
  private static final Gson gson = new Gson();

  public static MsSystem parseIRSystem(String irFileName) throws IOException {
    Reader irReader = new FileReader(irFileName);

    MsSystem msSystem = gson.fromJson(irReader, MsSystem.class);
    irReader.close();

    return msSystem;
  }

  public static List<Delta> parseDelta(String deltaFileName) throws IOException {
    Reader deltaReader = new FileReader(deltaFileName);

    Delta[] deltas = gson.fromJson(deltaReader, Delta[].class);
    deltaReader.close();

    return Arrays.asList(deltas);
  }
}
