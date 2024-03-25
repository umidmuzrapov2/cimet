package edu.university.ecs.lab.common.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Represents a method call in Java.
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class MethodCall {
  protected String methodName;
  // TODO Rename this? Represents if the called method object e.g. test.test()
  protected String calledFieldName;
  protected String parentMethod;
}
