package edu.university.ecs.lab.common.models;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.gson.annotations.SerializedName;
import edu.university.ecs.lab.common.models.enums.ClassRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a class in Java. It holds all information
 * regarding that class including all method declarations,
 * method calls, fields, etc.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class JClass {
  protected String className;
  protected String classPath;
  protected String packageName;

  protected List<Method> methods;

  @SerializedName("variables")
  protected List<Field> fields;

  protected List<MethodCall> methodCalls;
}
