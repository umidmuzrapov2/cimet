package edu.university.ecs.lab.common.utils;

import edu.university.ecs.lab.common.models.JavaClass;
import edu.university.ecs.lab.common.models.JavaMethod;
import edu.university.ecs.lab.common.models.JavaVariable;
import edu.university.ecs.lab.common.models.rest.RestCall;
import edu.university.ecs.lab.common.models.rest.RestController;
import edu.university.ecs.lab.common.models.rest.RestEndpoint;
import edu.university.ecs.lab.common.models.rest.RestService;
import edu.university.ecs.lab.rest.calls.models.*;
import edu.university.ecs.lab.semantics.models.CodeClone;
import edu.university.ecs.lab.semantics.models.Flow;

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
      JsonObjectBuilder msObjectBuilder = Json.createObjectBuilder();
      String msName = microservice.getKey();

      if (microservice.getKey().contains("\\")) {
        msName = microservice.getKey().substring(microservice.getKey().lastIndexOf("\\") + 1);
      } else if (microservice.getKey().contains("/")) {
        msName = microservice.getKey().substring(microservice.getKey().lastIndexOf("/") + 1);
      }

      msObjectBuilder.add("id", microservice.getValue().getId().replaceAll("\\\\", "/"));
      msObjectBuilder.add("msName", msName);
      // msObjectBuilder.add("msPath", microservice.getKey().replaceAll("\\\\", "/"));
      msObjectBuilder.add("commitId", microservice.getValue().getCommit());

      msObjectBuilder.add(
          "controllers",
          buildRestControllers(msName, microservice.getValue().getRestControllers()));
      msObjectBuilder.add("restCalls", buildRestCalls(microservice.getValue().getRestCalls()));

      msObjectBuilder.add("services", buildRestServices(microservice.getValue().getRestServices()));
      msObjectBuilder.add("dtos", buildJavaClass(microservice.getValue().getRestDTOs()));
      msObjectBuilder.add(
          "repositories", buildJavaClass(microservice.getValue().getRestRepositories()));
      msObjectBuilder.add("entities", buildJavaClass(microservice.getValue().getRestEntities()));

      jsonArrayBuilder.add(msObjectBuilder.build());
    }

    parentBuilder.add("services", jsonArrayBuilder.build());
    return parentBuilder.build();
  }

  /**
   * Write the given endpoint list to the given json list
   *
   * @param msName microservice system name
   * @param restControllers list of rest endpoints
   * @return rest endpoint json list
   */
  public static JsonArray buildRestControllers(
      String msName, List<RestController> restControllers) {
    JsonArrayBuilder controllerArrayBuilder = Json.createArrayBuilder();

    for (RestController restController : restControllers) {
      JsonObjectBuilder controllerBuilder = Json.createObjectBuilder();
      controllerBuilder.add("className", restController.getClassName());
      controllerBuilder.add("classPath", restController.getSourceFile().replaceAll("\\\\", "/"));
      controllerBuilder.add("variables", addVariableArray(restController.getVariables()));

      JsonArrayBuilder endpointArrayBuilder = Json.createArrayBuilder();

      for (RestEndpoint restEndpoint : restController.getRestEndpoints()) {
        restEndpoint.setId(
            restEndpoint.getHttpMethod()
                + ":"
                + msName
                + "."
                + restEndpoint.getMethod().getMethodName()
                + "#"
                + Math.abs(restEndpoint.getMethod().getArguments().hashCode()));

        JsonObjectBuilder endpointBuilder = Json.createObjectBuilder();

        endpointBuilder.add("id", restEndpoint.getId());
        endpointBuilder.add("api", restEndpoint.getUrl());
        endpointBuilder.add("type", restEndpoint.getDecorator());
        endpointBuilder.add("httpMethod", restEndpoint.getHttpMethod());
        endpointBuilder.add("parent-method", restEndpoint.getParentMethod());
        endpointBuilder.add(
            "method-variables", addVariableArray(restEndpoint.getMethodVariables()));

        JsonObjectBuilder methodBuilder = Json.createObjectBuilder();
        methodBuilder.add("methodName", restEndpoint.getMethod().getMethodName());
        methodBuilder.add("arguments", restEndpoint.getMethod().getArguments());
        methodBuilder.add("returnType", restEndpoint.getMethod().getReturnType());

        endpointBuilder.add("method", methodBuilder.build());

        endpointArrayBuilder.add(endpointBuilder.build());
      }

      controllerBuilder.add("restEndpoints", endpointArrayBuilder.build());
      controllerArrayBuilder.add(controllerBuilder.build());
    }

    return controllerArrayBuilder.build();
  }

  /**
   * Write the given service list to the given json list.
   *
   * @param restServices list of rest services
   * @return rest service json list
   */
  public static JsonArray buildRestServices(List<RestService> restServices) {
    JsonArrayBuilder serviceArrayBuilder = Json.createArrayBuilder();

    for (RestService restService : restServices) {
      JsonObjectBuilder serviceBuilder = Json.createObjectBuilder();
      serviceBuilder.add("className", restService.getClassName());
      serviceBuilder.add("classPath", restService.getClassPath().replaceAll("\\\\", "/"));

      // write service methods
      serviceBuilder.add("methods", addMethodArray(restService.getMethods()));

      // write service variables
      serviceBuilder.add("variables", addVariableArray(restService.getVariables()));

      serviceArrayBuilder.add(serviceBuilder.build());
    }

    return serviceArrayBuilder.build();
  }

  /**
   * Write the given dto list to the given json list
   *
   * @param classList list of generic java classes
   * @return class json list
   */
  public static JsonArray buildJavaClass(List<? extends JavaClass> classList) {
    JsonArrayBuilder dtoArrayBuilder = Json.createArrayBuilder();

    for (JavaClass javaClass : classList) {
      JsonObjectBuilder dtoBuilder = Json.createObjectBuilder();
      dtoBuilder.add("className", javaClass.getClassName());
      dtoBuilder.add("classPath", javaClass.getClassPath().replaceAll("\\\\", "/"));

      dtoBuilder.add("variables", addVariableArray(javaClass.getVariables()));
      dtoBuilder.add("methods", addMethodArray(javaClass.getMethods()));

      dtoArrayBuilder.add(dtoBuilder.build());
    }

    return dtoArrayBuilder.build();
  }

  /**
   * Write the given endpoint list to the given json list.
   *
   * @param restCalls the list of calls
   * @return array of call objects
   */
  public static JsonArray buildRestCalls(List<RestCall> restCalls) {
    JsonArrayBuilder endpointsArrayBuilder = Json.createArrayBuilder();

    for (RestCall restCall : restCalls) {
      JsonObjectBuilder restCallBuilder = Json.createObjectBuilder();

      restCallBuilder.add("api", restCall.getApi());
      restCallBuilder.add("sourceFile", restCall.getSourceFile().replaceAll("\\\\", "/"));
      restCallBuilder.add("callDest", restCall.getCallDest().replaceAll("\\\\", "/"));
      restCallBuilder.add("callMethod", restCall.getCallMethod() + "()");
      restCallBuilder.add("callClass", restCall.getCallClass());
      restCallBuilder.add("httpMethod", restCall.getHttpMethod());

      endpointsArrayBuilder.add(restCallBuilder.build());
    }

    return endpointsArrayBuilder.build();
  }

  public static JsonArray addMethodArray(List<JavaMethod> methodList) {
    JsonArrayBuilder methodArrayBuilder = Json.createArrayBuilder();

    for (JavaMethod method : methodList) {
      JsonObjectBuilder methodObjectBuilder = Json.createObjectBuilder();

      methodObjectBuilder.add("methodName", method.getMethodName());
      methodObjectBuilder.add("arguments", method.getArguments());
      methodObjectBuilder.add("returnType", method.getReturnType());

      methodArrayBuilder.add(methodObjectBuilder.build());
    }

    return methodArrayBuilder.build();
  }

  public static JsonArray addVariableArray(List<JavaVariable> variableList) {
    JsonArrayBuilder variableArrayBuilder = Json.createArrayBuilder();

    for (JavaVariable javaVariable : variableList) {
      JsonObjectBuilder variableObjectBuilder = Json.createObjectBuilder();

      variableObjectBuilder.add("variableName", javaVariable.getVariableName());
      variableObjectBuilder.add("variableType", javaVariable.getVariableType());

      variableArrayBuilder.add(variableObjectBuilder);
    }

    return variableArrayBuilder.build();
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
  public static JsonObject constructJsonClonesSystem(
      String systemName, String version, Map<String, List<CodeClone>> clonesMap) {
    JsonObjectBuilder parentBuilder = Json.createObjectBuilder();

    parentBuilder.add("systemName", systemName);
    parentBuilder.add("version", version);

    JsonArrayBuilder microserviceArrayBuilder = Json.createArrayBuilder();

    for (Map.Entry<String, List<CodeClone>> microservice : clonesMap.entrySet()) {

      JsonObjectBuilder microserviceBuilder = Json.createObjectBuilder();
      String msName = microservice.getKey();
      if (microservice.getKey().contains(File.separator)) {
        msName =
            microservice.getKey().substring(microservice.getKey().lastIndexOf(File.separator) + 1);
      }
      microserviceBuilder.add("msName", msName);

      JsonArrayBuilder cloneArrayBuilder = Json.createArrayBuilder();
      boolean hasClones = false;

      for (CodeClone clone : microservice.getValue()) {
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

      for (edu.university.ecs.lab.semantics.models.RestCall restCall : flow.getRestCalls()) {
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
}
