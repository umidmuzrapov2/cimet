package edu.university.ecs.lab.common.models;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** An object representing a method declaration in code */
@Data
public class Method {
  private String methodName;
  private String protection;
  private String parameterList;
  private String returnType;
//  private List<Annotation> annotations;

}
