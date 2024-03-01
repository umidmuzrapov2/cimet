package edu.university.ecs.lab.semantics.models;

import lombok.Data;

/** An object representing an annotation in program code */
@Data
public class Annotation {
  private boolean isHttpAnnotation;
  private String annotationName;
  private String key;
  private String value;
}
