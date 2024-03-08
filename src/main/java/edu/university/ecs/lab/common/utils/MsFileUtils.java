package edu.university.ecs.lab.common.utils;

import edu.university.ecs.lab.common.models.JavaClass;
import edu.university.ecs.lab.common.models.JavaMethod;
import edu.university.ecs.lab.common.models.JavaVariable;
import edu.university.ecs.lab.common.models.rest.RestCall;
import edu.university.ecs.lab.common.models.rest.RestEndpoint;
import edu.university.ecs.lab.common.models.rest.RestService;
import edu.university.ecs.lab.rest.calls.models.*;

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
        msName =
            microservice.getKey().substring(microservice.getKey().lastIndexOf(File.separator) + 1);
      }

      msObjectBuilder.add("id", microservice.getValue().getId().replaceAll("\\\\", "/"));
      msObjectBuilder.add("msName", msName);
      msObjectBuilder.add("msPath", microservice.getKey().replaceAll("\\\\", "/"));
      msObjectBuilder.add("commitId", microservice.getValue().getCommit());

      msObjectBuilder.add(
          "restEndpoints", buildRestEndpoints(msName, microservice.getValue().getRestEndpoints()));
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
   * @param restEndpoints list of rest endpoints
   * @return rest endpoint json list
   */
  public static JsonArray buildRestEndpoints(String msName, List<RestEndpoint> restEndpoints) {
    JsonArrayBuilder endpointsArrayBuilder = Json.createArrayBuilder();

    for (RestEndpoint restEndpoint : restEndpoints) {
      restEndpoint.setId(
          restEndpoint.getHttpMethod()
              + ":"
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
  public static JsonArray buildRestServices(List<RestService> restServices) {
    JsonArrayBuilder serviceArrayBuilder = Json.createArrayBuilder();

    for (RestService restService : restServices) {
      JsonObjectBuilder serviceBuilder = Json.createObjectBuilder();
      serviceBuilder.add("className", restService.getClassName());
      serviceBuilder.add("classPath", restService.getSourceFile().replaceAll("\\\\", "/"));

      // write service methods
      serviceBuilder.add("methods", addMethodArray(restService.getMethods()));

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
      dtoBuilder.add("classPath", javaClass.getSourceFile().replaceAll("\\\\", "/"));

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

  public static JsonArray addMethodArray(List<JavaMethod> methodList) {
    JsonArrayBuilder methodArrayBuilder = Json.createArrayBuilder();

    for (JavaMethod method : methodList) {
      JsonObjectBuilder methodObjectBuilder = Json.createObjectBuilder();

      methodObjectBuilder.add("methodName", method.getMethodName());
      methodObjectBuilder.add("parameter", method.getParameter());
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
}
