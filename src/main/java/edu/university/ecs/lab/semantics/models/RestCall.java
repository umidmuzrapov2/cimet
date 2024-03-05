package edu.university.ecs.lab.semantics.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** A object representing a rest API call in code */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class RestCall extends MethodCall {
  private String apiEndpoint;
  private String httpMethod;
  private String returnType;

  @Override
  public String toString() {
    return super.toString();
  }
}
