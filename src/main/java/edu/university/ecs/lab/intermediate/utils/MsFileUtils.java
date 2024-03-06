package edu.university.ecs.lab.intermediate.utils;

import edu.university.ecs.lab.common.models.rest.RestCall;
import edu.university.ecs.lab.common.models.rest.RestEndpoint;
import edu.university.ecs.lab.common.models.rest.MsModel;

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
      jsonObjectBuilder.add("id", microservice.getValue().getId().replaceAll("\\\\", "/"));
      jsonObjectBuilder.add("msName", msName);
      jsonObjectBuilder.add("msPath", microservice.getKey().replaceAll("\\\\", "/"));
      jsonObjectBuilder.add("commitId", microservice.getValue().getCommit());

      JsonArrayBuilder endpointsArrayBuilder = Json.createArrayBuilder();

      List<RestEndpoint> restEndpoints = microservice.getValue().getRestEndpoints();
      for (RestEndpoint restEndpoint : restEndpoints) {
        JsonObjectBuilder endpointBuilder = Json.createObjectBuilder();
        restEndpoint.setId(
            restEndpoint.getHttpMethod()
                + ":"
                + msName
                + "."
                + restEndpoint.getMethodName()
                + "#"
                + Math.abs(restEndpoint.getParameter().hashCode()));
        endpointBuilder.add("id", restEndpoint.getId());
        endpointBuilder.add("api", restEndpoint.getUrl());
        endpointBuilder.add("source-file", restEndpoint.getSourceFile().replaceAll("\\\\", "/"));
        endpointBuilder.add("type", restEndpoint.getDecorator());
        endpointBuilder.add("httpMethod", restEndpoint.getHttpMethod());
        endpointBuilder.add("parent-method", restEndpoint.getParentMethod());
        endpointBuilder.add("methodName", restEndpoint.getMethodName());
        endpointBuilder.add("arguments", restEndpoint.getParameter());
        endpointBuilder.add("return", restEndpoint.getReturnType());

        endpointsArrayBuilder.add(endpointBuilder.build());
      }
      jsonObjectBuilder.add("restEndpoints", endpointsArrayBuilder.build());

      List<RestCall> restCalls = microservice.getValue().getRestCalls();
      writeRestCall(endpointsArrayBuilder, restCalls);
      jsonObjectBuilder.add("restCalls", endpointsArrayBuilder.build());

      jsonArrayBuilder.add(jsonObjectBuilder.build());
    }

    parentBuilder.add("services", jsonArrayBuilder.build());
    return parentBuilder.build();
  }

  /**
   * Write the given endpoint list to the given json list.
   *
   * @param endpointsArrayBuilder the endpoints array builder
   * @param restCalls the list of calls
   */
  private static void writeRestCall(
      JsonArrayBuilder endpointsArrayBuilder, List<RestCall> restCalls) {
    for (RestCall restCall : restCalls) {
      JsonObjectBuilder restCallBuilder = Json.createObjectBuilder();

      restCallBuilder.add("api", restCall.getUrl());
      restCallBuilder.add("source-file", restCall.getSourceFile().replaceAll("\\\\", "/"));
      restCallBuilder.add("call-dest", restCall.getDestFile().replaceAll("\\\\", "/"));
      restCallBuilder.add("call-method", restCall.getCallMethod() + "()");
      restCallBuilder.add("httpMethod", restCall.getHttpMethod());

      endpointsArrayBuilder.add(restCallBuilder.build());
    }
  }
}
