package edu.university.ecs.lab.semantics.entity.graph;

import lombok.Data;

/** An object representing a method call in code */
@Data
public class MsMethodCall {
  private MsId msId;
  private String parentPackageName;
  private String parentClassName;
  private String parentMethodName;
  private String parentClassId; // packageName + className
  private int lineNumber;
  private String calledMethodName;
  private String calledServiceId;
  private String statementDeclaration;

  public MsMethodCall() {}

  public void setParentClassId() {
    this.parentClassId = parentPackageName + "." + parentClassName;
  }

  public void setMsParentMethod(MsParentMethod msParentMethod) {
    this.parentPackageName = msParentMethod.getParentPackageName();
    this.parentClassName = msParentMethod.getParentClassName();
    this.parentMethodName = msParentMethod.getParentMethodName();
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
