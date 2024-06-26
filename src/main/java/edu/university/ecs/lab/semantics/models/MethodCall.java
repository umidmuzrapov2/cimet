package edu.university.ecs.lab.semantics.models;

import lombok.Data;

/** An object representing a method call in code */
@Data
public class MethodCall {
  private Id id;
  private MethodContext methodContext;
  private int lineNumber;
  private String calledMethodName;
  private String calledServiceId;
}
