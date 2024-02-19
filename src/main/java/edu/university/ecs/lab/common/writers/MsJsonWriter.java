package edu.university.ecs.lab.common.writers;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MsJsonWriter {
  public static void writeJsonToFile(JsonObject jout, String filePath) throws IOException {
    try (FileWriter writer = new FileWriter(filePath)) {
      Map<String, Object> properties = new HashMap<>();
      properties.put(JsonGenerator.PRETTY_PRINTING, true);
      JsonWriterFactory writerFactory = Json.createWriterFactory(properties);

      JsonWriter jsonWriter = writerFactory.createWriter(writer);
      jsonWriter.write(jout);
      jsonWriter.close();
    }
  }
}
