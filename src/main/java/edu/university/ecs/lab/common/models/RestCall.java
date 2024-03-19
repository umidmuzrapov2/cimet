package edu.university.ecs.lab.common.models;

import edu.university.ecs.lab.common.models.enums.HttpMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** An object representing a rest call (api call) in code */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RestCall extends MethodCall {
  /** The api url that is targeted in rest call */
  private String url = "";

  /**
   * The httpMethod of the api endpoint e.g. GET, POST, PUT see semantics.models.enums.httpMethod
   */
  private String httpMethod = "";

  /** Expected return type of the api call */
//  private String returnType = "";

  private int responseTypeIndex = -1;

  private String sourceFile = "";
  private String destFile = "";


  private static final RestCall[] restTemplates = {
          new RestCall("getForObject", HttpMethod.GET, 1),
          new RestCall("getForEntity", HttpMethod.GET, 1),
          new RestCall("postForObject", HttpMethod.POST, 2),
          new RestCall("postForEntity", HttpMethod.POST, 2),
          new RestCall("put", HttpMethod.PUT, 1),
          new RestCall("exchange", HttpMethod.GET, 3),
          new RestCall("delete", HttpMethod.DELETE, 0), // TODO: delete doesn't work
  };

  public RestCall(String methodName, HttpMethod httpMethod, int responseTypeIndex) {
    setMethodName(methodName);
    setHttpMethod(httpMethod.toString());
    setResponseTypeIndex(responseTypeIndex);
  }

  public static RestCall findByName(String methodName) {
    for (RestCall template : restTemplates) {
      if (template.getMethodName().equals(methodName)) {
        return template;
      }
    }
    return null;
  }

  public static RestCall getRestCallFromName(String methodName) {
    switch(methodName){
      case "getForObject":
        return new RestCall("getForObject", HttpMethod.GET, 1);
      case "getForEntity":
        return new RestCall("getForEntity", HttpMethod.GET, 1);
      case "postForObject":
        return new RestCall("postForObject", HttpMethod.POST, 2);
      case "postForEntity":
        return new RestCall("postForEntity", HttpMethod.POST, 2);
      case "put":
        return new RestCall("put", HttpMethod.PUT, 1);
      case "exchange":
        return new RestCall("exchange", HttpMethod.GET, 3);
      case "delete":
        new RestCall("delete", HttpMethod.DELETE, 0);
    }

    return null;
  }
}
