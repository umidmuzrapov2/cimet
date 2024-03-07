package edu.university.ecs.lab.rest.calls.utils;

import edu.university.ecs.lab.common.models.JavaMethod;
import edu.university.ecs.lab.rest.calls.models.RestCall;
import edu.university.ecs.lab.rest.calls.models.RestEndpoint;
import edu.university.ecs.lab.rest.calls.models.MsModel;
import edu.university.ecs.lab.rest.calls.models.RestService;

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
      JsonObjectBuilder msObjectBuilder = Json.createObjectBuilder();
      String msName = microservice.getKey();

      if (microservice.getKey().contains(File.separator)) {
        msName = microservice.getKey().substring(microservice.getKey().lastIndexOf(File.separator) + 1);
      }

      msObjectBuilder.add("id", microservice.getValue().getId().replaceAll("\\\\", "/"));
      msObjectBuilder.add("msName", msName);
      msObjectBuilder.add("msPath", microservice.getKey().replaceAll("\\\\", "/"));
      msObjectBuilder.add("commitId", microservice.getValue().getCommit());

      msObjectBuilder.add("restEndpoints", buildRestEndpoints(msName, microservice.getValue().getRestEndpoints()));
      msObjectBuilder.add("restServices", buildRestServices(microservice.getValue().getRestServices()));
      msObjectBuilder.add("restCalls", writeRestCall(microservice.getValue().getRestCalls()));

      jsonArrayBuilder.add(msObjectBuilder.build());
    }

    parentBuilder.add("services", jsonArrayBuilder.build());
    return parentBuilder.build();
  }

  /**
   * Write the given endpoint list to the given json list
   *
   * @param restEndpoints list of rest endpoints
   * @return rest endpoint json list
   */
  private static JsonArray buildRestEndpoints(String msName, List<RestEndpoint> restEndpoints) {
    JsonArrayBuilder endpointsArrayBuilder = Json.createArrayBuilder();

    for (RestEndpoint restEndpoint : restEndpoints) {
      restEndpoint.setId(
              restEndpoint.getHttpMethod() + ":"
                      + msName
                      + "."
                      + restEndpoint.getMethod().getMethodName()
                      + "#"
                      + Math.abs(restEndpoint.getMethod().getParameter().hashCode()));


      JsonObjectBuilder endpointBuilder = Json.createObjectBuilder();

      endpointBuilder.add("id", restEndpoint.getId());
      endpointBuilder.add("api", restEndpoint.getUrl());
      endpointBuilder.add("source-file", restEndpoint.getSourceFile().replaceAll("\\\\", "/"));
      endpointBuilder.add("type", restEndpoint.getDecorator());
      endpointBuilder.add("httpMethod", restEndpoint.getHttpMethod());
      endpointBuilder.add("className", restEndpoint.getClassName());
      endpointBuilder.add("parent-method", restEndpoint.getParentMethod());
      endpointBuilder.add("methodName", restEndpoint.getMethod().getMethodName());
      endpointBuilder.add("arguments", restEndpoint.getMethod().getParameter());
      endpointBuilder.add("return", restEndpoint.getMethod().getReturnType());

      JsonArrayBuilder serviceArrayBuilder = Json.createArrayBuilder();
      restEndpoint.getServices().forEach(serviceArrayBuilder::add);

      endpointBuilder.add("service-dependencies", serviceArrayBuilder.build());

      endpointsArrayBuilder.add(endpointBuilder.build());
    }

    return endpointsArrayBuilder.build();
  }

  /**
   * Write the given service list to the given json list.
   *
   * @param restServices list of rest services
   * @return rest service json list
   */
  private static JsonArray buildRestServices(List<RestService> restServices) {
    JsonArrayBuilder serviceArrayBuilder = Json.createArrayBuilder();

    for (RestService restService : restServices) {
      JsonObjectBuilder serviceBuilder = Json.createObjectBuilder();
      serviceBuilder.add("className", restService.getClassName());
      serviceBuilder.add("classPath", restService.getSourceFile().replaceAll("\\\\", "/"));

      JsonArrayBuilder methodArrayBuilder = Json.createArrayBuilder();

      // write service methods
      for (JavaMethod method : restService.getMethods()) {
        JsonObjectBuilder methodObjectBuilder = Json.createObjectBuilder();

        methodObjectBuilder.add("methodName", method.getMethodName());
        methodObjectBuilder.add("parameter", method.getParameter());
        methodObjectBuilder.add("returnType", method.getReturnType());

        methodArrayBuilder.add(methodObjectBuilder.build());
      }
      serviceBuilder.add("methods", methodArrayBuilder.build());

      JsonArrayBuilder dtoArrayBuilder = Json.createArrayBuilder();

      // write dto class names
      for (String dtoName : restService.getDtos()) {
        dtoArrayBuilder.add(dtoName);
      }
      serviceBuilder.add("dto-dependencies", dtoArrayBuilder.build());

      serviceArrayBuilder.add(serviceBuilder.build());
    }

    return serviceArrayBuilder.build();
  }

    /**
     * Write the given endpoint list to the given json list.
     *
     * @param restCalls the list of calls
     * @return array of call objects
     */
  private static JsonArray writeRestCall(List<RestCall> restCalls) {
    JsonArrayBuilder endpointsArrayBuilder = Json.createArrayBuilder();

    for (RestCall restCall : restCalls) {
      JsonObjectBuilder restCallBuilder = Json.createObjectBuilder();

      restCallBuilder.add("api", restCall.getUrl());
      restCallBuilder.add("source-file", restCall.getSourceFile().replaceAll("\\\\", "/"));
      restCallBuilder.add("call-dest", restCall.getDestFile().replaceAll("\\\\", "/"));
      restCallBuilder.add("call-method", restCall.getCallMethod() + "()");
      restCallBuilder.add("call-class", restCall.getCallClass());
      restCallBuilder.add("httpMethod", restCall.getHttpMethod());

      endpointsArrayBuilder.add(restCallBuilder.build());
    }

    return endpointsArrayBuilder.build();
  }
}
