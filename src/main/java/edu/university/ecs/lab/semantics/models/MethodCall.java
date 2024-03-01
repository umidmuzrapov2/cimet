package edu.university.ecs.lab.semantics.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** An object representing a method call in code */
@Data
public class MethodCall {
  private Id id;
  private String parentPackageName;
  private String parentClassName;
  private String parentMethodName;
  private String parentClassId; // packageName + className
  private int lineNumber;
  private String calledMethodName;
  private String calledServiceId;
  private String statementDeclaration;

  public void setParentClassId() {
    this.parentClassId = parentPackageName + "." + parentClassName;
  }

  public void setMsParentMethod(ParentMethod parentMethod) {
    this.parentPackageName = parentMethod.getParentPackageName();
    this.parentClassName = parentMethod.getParentClassName();
    this.parentMethodName = parentMethod.getParentMethodName();
  }

  public String getParentMethodFullName() {
    return this.parentClassId + "." + this.parentMethodName;
  }

  @Override
  public String toString() {
    return "[L"
        + lineNumber
        + "] "
        + parentPackageName
        + '.'
        + parentClassName
        + '.'
        + parentMethodName
        + " -> "
        + calledServiceId
        + '.'
        + calledMethodName
        + '.';
  }
}
