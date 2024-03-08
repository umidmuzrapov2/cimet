package edu.university.ecs.lab.common.models.rest;

import edu.university.ecs.lab.common.models.HttpMethod;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RestCallMethod {
  private String methodName;
  private HttpMethod httpMethod;
  private int responseTypeIndex;

  private static final RestCallMethod[] restTemplateMethods = {
    new RestCallMethod("getForObject", HttpMethod.GET, 1),
    new RestCallMethod("getForEntity", HttpMethod.GET, 1),
    new RestCallMethod("postForObject", HttpMethod.POST, 2),
    new RestCallMethod("postForEntity", HttpMethod.POST, 2),
    new RestCallMethod("put", HttpMethod.PUT, 1),
    new RestCallMethod("exchange", HttpMethod.GET, 3),
    new RestCallMethod("delete", HttpMethod.DELETE, 0), // TODO: delete doesn't work
  };

  public static RestCallMethod findByName(String methodName) {
    for (RestCallMethod restTemplateMethod : restTemplateMethods) {
      if (restTemplateMethod.methodName.equals(methodName)) {
        return restTemplateMethod;
      }
    }
    return null;
  }
}
