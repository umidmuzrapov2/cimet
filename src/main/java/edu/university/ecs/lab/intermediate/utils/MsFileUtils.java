package edu.university.ecs.lab.intermediate.utils;

import edu.university.ecs.lab.common.models.RestDependency;
import edu.university.ecs.lab.common.models.Endpoint;
import edu.university.ecs.lab.common.models.MsModel;
import edu.university.ecs.lab.semantics.models.CodeClone;
import edu.university.ecs.lab.semantics.models.Flow;
import edu.university.ecs.lab.semantics.models.RestCall;

import javax.json.*;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

      List<Endpoint> endpoints = microservice.getValue().getEndpoints();
      for (Endpoint endpoint : endpoints) {
        JsonObjectBuilder endpointBuilder = Json.createObjectBuilder();
        endpoint.setId(
            endpoint.getHttpMethod()
                + ":"
                + msName
                + "."
                + endpoint.getMethodName()
                + "#"
                + Math.abs(endpoint.getParameter().hashCode()));
        endpointBuilder.add("id", endpoint.getId());
        endpointBuilder.add("api", endpoint.getUrl());
        endpointBuilder.add("source-file", endpoint.getSourceFile().replaceAll("\\\\", "/"));
        endpointBuilder.add("type", endpoint.getDecorator());
        endpointBuilder.add("httpMethod", endpoint.getHttpMethod());
        endpointBuilder.add("parent-method", endpoint.getParentMethod());
        endpointBuilder.add("methodName", endpoint.getMethodName());
        endpointBuilder.add("arguments", endpoint.getParameter());
        endpointBuilder.add("return", endpoint.getReturnType());

        endpointsArrayBuilder.add(endpointBuilder.build());
      }
      jsonObjectBuilder.add("endpoints", endpointsArrayBuilder.build());

      List<RestDependency> dependencies = microservice.getValue().getRestDependencies();
      writeDependency(endpointsArrayBuilder, dependencies);
      jsonObjectBuilder.add("dependencies", endpointsArrayBuilder.build());

      jsonArrayBuilder.add(jsonObjectBuilder.build());
    }

    parentBuilder.add("services", jsonArrayBuilder.build());
    return parentBuilder.build();
  }

  /**
   * Construct a JSON object representing the given ms system name, version, and microservice data
   * map.
   *
   * @param systemName the name of the system
   * @param version the version of the system
   * @param clonesMap the map of microservices to their clones
   * @return the constructed JSON object
   */
  public static JsonObject constructJsonClonesSystem(String systemName, String version, Map<String, List<CodeClone>> clonesMap) {
    JsonObjectBuilder parentBuilder = Json.createObjectBuilder();

    parentBuilder.add("systemName", systemName);
    parentBuilder.add("version", version);

    JsonArrayBuilder microserviceArrayBuilder = Json.createArrayBuilder();

    for (Map.Entry<String, List<CodeClone>> microservice: clonesMap.entrySet()) {

      JsonObjectBuilder microserviceBuilder = Json.createObjectBuilder();
      String msName = microservice.getKey();
      if (microservice.getKey().contains(File.separator)) {
        msName = microservice.getKey().substring(microservice.getKey().lastIndexOf(File.separator) + 1);
      }
      microserviceBuilder.add("msName", msName);

      JsonArrayBuilder cloneArrayBuilder = Json.createArrayBuilder();
      boolean hasClones = false;

      for (CodeClone clone: microservice.getValue()) {
        JsonObjectBuilder cloneBuilder = Json.createObjectBuilder();

        cloneBuilder.add("global-similarity", clone.getGlobalSimilarity());
        cloneBuilder.add("controller-similarity", clone.getSimilarityController());
        cloneBuilder.add("service-similarity", clone.getSimilarityService());
        cloneBuilder.add("repository-similarity", clone.getSimilarityRepository());
        cloneBuilder.add("restCalls-similarity", clone.getSimilarityRestCalls());
        cloneBuilder.add("flowA", constructFlowJson(clone.getFlowA()));
        cloneBuilder.add("flowB", constructFlowJson(clone.getFlowB()));

        cloneArrayBuilder.add(cloneBuilder.build());
        hasClones = true;
      }
      microserviceBuilder.add("clones", cloneArrayBuilder.build());

      if (hasClones) {
        microserviceArrayBuilder.add(microserviceBuilder.build());
      }

    }

    parentBuilder.add("services", microserviceArrayBuilder.build());

    return parentBuilder.build();
  }

  private static JsonObject constructFlowJson(Flow flow) {
      JsonObjectBuilder flowBuilder = Json.createObjectBuilder();

      // add Controller to Json
      if (Objects.nonNull(flow.getController())) {
        flowBuilder.add("microservice", flow.getController().getId().getProject());
        flowBuilder.add("controller", flow.getController().getClassName());
        flowBuilder.add("controller-method", flow.getControllerMethod().getMethodName());
      }


      // Add service to Json
      if (Objects.nonNull(flow.getService())) {
        flowBuilder.add("service", flow.getService().getClassName());
        flowBuilder.add("service-method", flow.getServiceMethod().getMethodName());
      }


      // Add repository to Json
      if (Objects.nonNull(flow.getRepository())) {
        flowBuilder.add("repository", flow.getRepository().getClassName());
        flowBuilder.add("repository-method", flow.getRepositoryMethod().getMethodName());
      }


      // Add Rest calls to Json
      if (Objects.nonNull(flow.getRestCalls())) {
        JsonArrayBuilder restCallArrayBuilder = Json.createArrayBuilder();

        for (RestCall restCall: flow.getRestCalls()) {
          JsonObjectBuilder restCallBuilder = Json.createObjectBuilder();
          restCallBuilder.add("api-enpoint", restCall.getApiEndpoint());
          restCallBuilder.add("http-method", restCall.getHttpMethod());
          restCallBuilder.add("return-type", restCall.getReturnType());

          restCallArrayBuilder.add(restCallBuilder.build());
        }

        flowBuilder.add("rest-calls", restCallArrayBuilder.build());
      }

      return flowBuilder.build();
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
      endpointBuilder.add("source-file", endpoint.getSourceFile().replaceAll("\\\\", "/"));
      endpointBuilder.add("call-dest", endpoint.getDestFile().replaceAll("\\\\", "/"));
      endpointBuilder.add("call-method", endpoint.getParentMethod() + "()");
      endpointBuilder.add("httpMethod", endpoint.getHttpMethod());

      endpointsArrayBuilder.add(endpointBuilder.build());
    }
  }
}
