package edu.university.ecs.lab.common.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class JService extends JClass {
  public JService(@NonNull JClass jClass) {
    classPath = jClass.getClassPath();
    packageName = jClass.getPackageName();
    className = jClass.getClassName();
    methods = jClass.getMethods();
    fields = jClass.getFields();
    methodCalls = jClass.getMethodCalls();

    restCalls = new ArrayList<>();
  }

  private List<RestCall> restCalls;
}
