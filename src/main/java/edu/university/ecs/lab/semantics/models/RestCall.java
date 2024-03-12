package edu.university.ecs.lab.semantics.models;

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
  private String apiEndpoint;

  /**
   * The httpMethod of the api endpoint e.g. GET, POST, PUT see semantics.models.enums.httpMethod
   */
  private String httpMethod;

  /** Expected return type of the api call */
  private String returnType;

  /** The actual line of code with api call */
  private String statementDeclaration;

  @Override
  public String toString() {
    return super.toString();
  }
}
