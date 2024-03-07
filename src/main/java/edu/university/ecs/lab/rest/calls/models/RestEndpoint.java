package edu.university.ecs.lab.rest.calls.models;

import edu.university.ecs.lab.common.config.InputConfig;
import edu.university.ecs.lab.common.models.JavaMethod;
import lombok.*;

import java.util.List;

/**
 * Model to represent a Java Spring api endpoint as extracted from a service controller and written
 * to IR JSON
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RestEndpoint {
  /** URL of the endpoint including base from class: (ex: /api/v1/users/{id}) */
  private String url;

  /**
   * The java source code file the endpoint was found in, path after {@link
   * InputConfig#getClonePath()}
   */
  private String sourceFile;

  /** JSF Mapping annotation */
  private String decorator;

  /** The HTTP method of the endpoint */
  private String httpMethod;

  /** The method that the endpoint is a part of as full class path a.b.c.methodName */
  private String parentMethod;

  private String id;
  private JavaMethod method;
  private List<String> services;
  private String className;
}
