package edu.university.ecs.lab.semantics.entity.graph;

import lombok.Data;

/** An object representing an annotation in program code */
@Data
public class MsAnnotation {
  private boolean isHttpAnnotation;
  private String annotationName;
  private String key;
  private String value;
}
