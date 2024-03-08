package edu.university.ecs.lab.semantics.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Here "parent" means encapsulating, each parent is the associated method that the current method
 * call is encapsulated by. Same for the class and package.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class MethodLocation {
  private String parentMethodName;
  private String parentClassName;
  private String parentPackageName;

  public String getParentMethodFullName() {
    return parentPackageName + "." + parentClassName + "." + parentMethodName;
  }
}
