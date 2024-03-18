package edu.university.ecs.lab.common.models;

import com.github.javaparser.ast.body.VariableDeclarator;
import lombok.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Field  {
  private String fieldType;
  private String fieldName;

  public Field(VariableDeclarator variable) {
    setFieldName(variable.getNameAsString());
    setFieldType(variable.getTypeAsString());
  }
}
