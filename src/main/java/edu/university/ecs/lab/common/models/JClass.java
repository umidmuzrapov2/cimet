package edu.university.ecs.lab.common.models;

import com.github.javaparser.ast.body.MethodDeclaration;
import edu.university.ecs.lab.common.models.enums.ClassRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/** A object representing a class definition in code */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class JClass {
  private String fileName;
  private String packageName;
  private String className;
  private ClassRole role;
  private List<Method> methods;
  private List<Field> fields;
  private List<MethodCall> methodCalls;
}
