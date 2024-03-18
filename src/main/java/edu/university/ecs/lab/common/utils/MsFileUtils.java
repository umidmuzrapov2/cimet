package edu.university.ecs.lab.common.utils;

import edu.university.ecs.lab.common.models.*;
import edu.university.ecs.lab.common.models.enums.ClassRole;
import edu.university.ecs.lab.rest.calls.models.*;
import edu.university.ecs.lab.semantics.models.CodeClone;
import edu.university.ecs.lab.semantics.models.Flow;
import javax.json.*;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
          "controllers",
          buildRestControllers(msName, microservice.getValue().getClassList().stream()
                  .filter(jClass -> jClass.getRole() == ClassRole.CONTROLLER).collect(Collectors.toList())));

      msObjectBuilder.add("restCalls", buildRestCalls(getRestCalls(microservice.getValue().getClassList().stream().filter(jClass -> jClass.getRole() == ClassRole.SERVICE).collect(Collectors.toList()))));

      msObjectBuilder.add("services", buildJavaClass(microservice.getValue().getClassList().stream()
              .filter(jClass -> jClass.getRole() == ClassRole.SERVICE).collect(Collectors.toList())));
      msObjectBuilder.add("dtos", buildJavaClass(microservice.getValue().getClassList().stream()
              .filter(jClass -> jClass.getRole() == ClassRole.REPOSITORY).collect(Collectors.toList())));
      msObjectBuilder.add(
          "repositories", buildJavaClass(microservice.getValue().getClassList().stream()
                      .filter(jClass -> jClass.getRole() == ClassRole.REPOSITORY).collect(Collectors.toList())));
      msObjectBuilder.add("entities", buildJavaClass(microservice.getValue().getClassList().stream()
              .filter(jClass -> jClass.getRole() == ClassRole.ENTITY).collect(Collectors.toList())));

      jsonArrayBuilder.add(msObjectBuilder.build());
    }

    parentBuilder.add("services", jsonArrayBuilder.build());
    return parentBuilder.build();
  }

  /**
   * Write the given endpoint list to the given json list
   *
   * @param msName microservice system name
   * @return rest endpoint json list
   */
  public static JsonArray buildRestControllers(
      String msName, List<JClass> controllers) {
    JsonArrayBuilder controllerArrayBuilder = Json.createArrayBuilder();

    for (JClass controller : controllers) {
      JsonObjectBuilder controllerBuilder = Json.createObjectBuilder();
      controllerBuilder.add("className", controller.getClassName());
      controllerBuilder.add("classPath", controller.getFileName().replaceAll("\\\\", "/"));
      controllerBuilder.add("fields", buildFieldArray(controller.getFields()));

      JsonArrayBuilder endpointArrayBuilder = Json.createArrayBuilder();

      // Get "endpoint" methods in controller
      for (Method endpoint : controller.getMethods().stream().filter(m -> m instanceof Endpoint).collect(Collectors.toList())) {
        String id = ((Endpoint) endpoint).getMapping()
                + ":"
                + msName
                + "."
                + endpoint.getMethodName()
                + "#"
                + Math.abs(endpoint.getParameterList().hashCode());

        JsonObjectBuilder endpointBuilder = Json.createObjectBuilder();

        endpointBuilder.add("id", id);
        endpointBuilder.add("api", ((Endpoint) endpoint).getUrl());
        endpointBuilder.add("type", ((Endpoint) endpoint).getDecorator());
        endpointBuilder.add("httpMethod", ((Endpoint) endpoint).getHttpMethod());
        endpointBuilder.add("parent-method", endpoint.getMethodName());
        endpointBuilder.add("methodName", endpoint.getMethodName());
        endpointBuilder.add("arguments", endpoint.getParameterList());
        endpointBuilder.add("return", endpoint.getReturnType());
//        endpointBuilder.add(
//            "method-variables", addVariableArray(restEndpoint.getMethodVariables()));

        endpointArrayBuilder.add(endpointBuilder.build());
      }

      controllerBuilder.add("restEndpoints", endpointArrayBuilder.build());
      controllerArrayBuilder.add(controllerBuilder.build());
    }

    return controllerArrayBuilder.build();
  }

  /**
   * Write the given class list to the given json list
   *
   * @param classList list of generic java classes
   * @return class json list
   */
  public static JsonArray buildJavaClass(List<JClass> classList) {
    JsonArrayBuilder jclassArrayBuilder = Json.createArrayBuilder();

    for (JClass javaClass : classList) {
      JsonObjectBuilder dtoBuilder = Json.createObjectBuilder();
      dtoBuilder.add("className", javaClass.getClassName());
      dtoBuilder.add("classPath", javaClass.getFileName().replaceAll("\\\\", "/"));

      dtoBuilder.add("variables", buildFieldArray(javaClass.getFields()));
      dtoBuilder.add("methods", buildMethodArray(javaClass.getMethods()));

      jclassArrayBuilder.add(dtoBuilder.build());
    }

    return jclassArrayBuilder.build();
  }

  /**
   * Write the given endpoint list to the given json list.
   *
   * @param restCalls the list of calls
   * @return array of call objects
   */
  public static JsonArray buildRestCalls(List<RestCall> restCalls) {
    JsonArrayBuilder restCallArrayBuilder = Json.createArrayBuilder();

    // Get "restCall" methodCalls in service
    for (RestCall restCall : restCalls) {
      JsonObjectBuilder restCallBuilder = Json.createObjectBuilder();

      // TODO source this issue
      if(restCall.getDestFile() == null) {
        restCall.setDestFile("");
      }

      restCallBuilder.add("api", restCall.getUrl());
      restCallBuilder.add("source-file", restCall.getSourceFile().replaceAll("\\\\", "/"));
      restCallBuilder.add("call-dest", restCall.getDestFile().replaceAll("\\\\", "/"));
      restCallBuilder.add("call-method", restCall.getMethodName() + "()");
//      restCallBuilder.add("call-class", restCall.getCallClass());
      restCallBuilder.add("httpMethod", restCall.getHttpMethod());

      restCallArrayBuilder.add(restCallBuilder.build());
    }

    return restCallArrayBuilder.build();
  }

  public static JsonArray buildMethodArray(List<Method> methodList) {
    // TODO find cause of this
    if(methodList == null) {
      return JsonObject.EMPTY_JSON_ARRAY;
    }
    JsonArrayBuilder methodArrayBuilder = Json.createArrayBuilder();

    for (Method method : methodList) {
      JsonObjectBuilder methodObjectBuilder = Json.createObjectBuilder();

      methodObjectBuilder.add("methodName", method.getMethodName());
      methodObjectBuilder.add("parameter", method.getParameterList());
      methodObjectBuilder.add("returnType", method.getReturnType());

      methodArrayBuilder.add(methodObjectBuilder.build());
    }

    return methodArrayBuilder.build();
  }

  public static JsonArray buildFieldArray(List<Field> fieldList) {
    // TODO find cause of this
    if(fieldList == null) {
      return JsonObject.EMPTY_JSON_ARRAY;
    }

    JsonArrayBuilder variableArrayBuilder = Json.createArrayBuilder();
    for (Field field : fieldList) {
      JsonObjectBuilder variableObjectBuilder = Json.createObjectBuilder();

      variableObjectBuilder.add("fieldName", field.getFieldName());
      variableObjectBuilder.add("fieldType", field.getFieldType());

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

  private static List<RestCall> getRestCalls(List<JClass> classList) {
    List<MethodCall> methodCalls = classList.stream().flatMap(jClass -> jClass.getMethodCalls().stream()).collect(Collectors.toList());
    return methodCalls.stream().filter(methodCall -> methodCall instanceof RestCall).map(methodCall -> (RestCall) methodCall).collect(Collectors.toList());
  }
}
