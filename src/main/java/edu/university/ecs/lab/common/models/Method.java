package edu.university.ecs.lab.common.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a method declaration in Java.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Method {
  private String methodName;
  private String protection;
  private String parameterList;
  private String returnType;
//  private List<Annotation> annotations;

}
