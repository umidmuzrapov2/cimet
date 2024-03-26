package edu.university.ecs.lab.common.models;

import com.github.javaparser.ast.body.VariableDeclarator;
import com.google.gson.annotations.SerializedName;
import lombok.*;

/** Represents a field attribute in a Java class or in our case a JClass. */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Field {
  @SerializedName("variableType")
  private String fieldType;

  @SerializedName("variableName")
  private String fieldName;

  public Field(VariableDeclarator variable) {
    setFieldName(variable.getNameAsString());
    setFieldType(variable.getTypeAsString());
  }
}
