package edu.university.ecs.lab.intermediate.utils;

import edu.university.ecs.lab.common.models.RestDependency;
import edu.university.ecs.lab.common.models.Endpoint;
import edu.university.ecs.lab.common.models.MsModel;

import javax.json.*;
import java.io.File;
import java.util.List;
import java.util.Map;

/** Utility class for handling microservice files. */
public class MsFileUtils {
  /** Private constructor to prevent instantiation. */
  private MsFileUtils() {}

  /**
   * Construct a JSON object representing the given ms system name, version, and microservice data
   * map.
   *
   * @param systemName the name of the system
   * @param version the version of the system
   * @param msDataMap the map of microservices to their data models
   * @return the constructed JSON object
   */
  public static JsonObject constructJsonMsSystem(
      String systemName, String version, Map<String, MsModel> msDataMap) {
    JsonObjectBuilder parentBuilder = Json.createObjectBuilder();

    parentBuilder.add("systemName", systemName);
    parentBuilder.add("version", version);

    JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

    for (Map.Entry<String, MsModel> microservice : msDataMap.entrySet()) {
      JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
      String msName = microservice.getKey();
      if (microservice.getKey().contains(File.separator)) {
        msName =
            microservice.getKey().substring(microservice.getKey().lastIndexOf(File.separator) + 1);
      }
      jsonObjectBuilder.add("msName", msName);
      jsonObjectBuilder.add("msPath", microservice.getKey());

      JsonArrayBuilder endpointsArrayBuilder = Json.createArrayBuilder();

      List<Endpoint> endpoints = microservice.getValue().getEndpoints();
      for (Endpoint endpoint : endpoints) {
        JsonObjectBuilder endpointBuilder = Json.createObjectBuilder();

        endpointBuilder.add("api", endpoint.getUrl());
        endpointBuilder.add("source-file", endpoint.getSourceFile());
        endpointBuilder.add("decorator", endpoint.getDecorator());
        endpointBuilder.add("httpMethod", endpoint.getHttpMethod());
        endpointBuilder.add("parent-method", endpoint.getParentMethod());

        endpointsArrayBuilder.add(endpointBuilder.build());
      }
      jsonObjectBuilder.add("endpoints", endpointsArrayBuilder.build());

      List<RestDependency> dependencies = microservice.getValue().getDependencies();
      writeDependency(endpointsArrayBuilder, dependencies);
      jsonObjectBuilder.add("dependencies", endpointsArrayBuilder.build());

      jsonArrayBuilder.add(jsonObjectBuilder.build());
    }

    parentBuilder.add("services", jsonArrayBuilder.build());
    return parentBuilder.build();
  }

  /**
   * Write the given endpoint list to the given json list.
   *
   * @param endpointsArrayBuilder the endpoints array builder
   * @param dependencies the list of dependencies
   */
  private static void writeDependency(
      JsonArrayBuilder endpointsArrayBuilder, List<RestDependency> dependencies) {
    for (RestDependency endpoint : dependencies) {
      JsonObjectBuilder endpointBuilder = Json.createObjectBuilder();

      endpointBuilder.add("api", endpoint.getUrl());
      endpointBuilder.add("source-file", endpoint.getSourceFile());
      endpointBuilder.add("call-dest", endpoint.getDestFile());
      endpointBuilder.add("call-method", endpoint.getParentMethod() + "()");
      endpointBuilder.add("httpMethod", endpoint.getHttpMethod());

      endpointsArrayBuilder.add(endpointBuilder.build());
    }
  }
}
