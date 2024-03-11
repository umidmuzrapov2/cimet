package edu.university.ecs.lab.common.models.rest;

import edu.university.ecs.lab.common.models.JavaMethod;
import edu.university.ecs.lab.common.models.JavaVariable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Model to represent a Java api endpoint extracted from a service controller
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RestEndpoint {
  private String id;

  /** URL of the endpoint including base from class: (ex: /api/v1/users/{id}) */
  private String url;

  /** JSF Mapping annotation */
  private String decorator;

  /** The HTTP method of the endpoint */
  private String httpMethod;

  /** The method that the endpoint is a part of as full class path a.b.c.methodName */
  private String parentMethod;

  private JavaMethod method;
  private List<JavaVariable> methodVariables;
}
