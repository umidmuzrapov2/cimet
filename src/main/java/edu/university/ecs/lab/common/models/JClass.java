package edu.university.ecs.lab.common.models;

import com.github.javaparser.ast.body.MethodDeclaration;
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
  private String fileName;
  private String packageName;
  private String className;
  private ClassRole role;
  private List<Method> methods;
  private List<Field> fields;
  private List<MethodCall> methodCalls;

  public List<Endpoint> getEndpoints() {
    return getMethods().stream().filter(mc -> mc instanceof Endpoint).map(mc -> ((Endpoint) mc)).collect(Collectors.toList());
  }

  public List<RestCall> getRestCalls() {
    return getMethodCalls().stream().filter(mc -> mc instanceof RestCall).map(mc -> ((RestCall) mc)).collect(Collectors.toList());
  }
}
