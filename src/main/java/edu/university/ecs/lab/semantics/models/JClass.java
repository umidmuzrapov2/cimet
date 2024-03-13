package edu.university.ecs.lab.semantics.models;

import edu.university.ecs.lab.semantics.models.enums.ClassRole;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/** A object representing a class definition in code */
@Data
public class JClass implements Serializable {
  private Id id;
  private String packageName;
  private String className;
  //  private List<String> fieldNames;
  private ClassRole role;
  //  private String requestMapping;
  private List<Annotation> annotations;
}
