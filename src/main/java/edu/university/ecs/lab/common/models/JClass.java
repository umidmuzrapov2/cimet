package edu.university.ecs.lab.common.models;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * Represents a class in Java. It holds all information regarding that class including all method
 * declarations, method calls, fields, etc.
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
