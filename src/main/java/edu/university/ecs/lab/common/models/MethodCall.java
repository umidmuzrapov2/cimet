package edu.university.ecs.lab.common.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** An object representing a method call in code */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class MethodCall {
  private String methodName;
  // TODO Rename this? Represents if the called method object e.g. test.test()
  private String calledFieldName;
  private String parentMethod;
}
