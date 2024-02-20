package edu.university.ecs.lab.common.models;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MsModel {
  private List<Endpoint> endpoints;
  private List<Dependency> dependencies;

  private List<Dependency> externalDependencies;

  public MsModel() {
    endpoints = new ArrayList<>();
    dependencies = new ArrayList<>();
    externalDependencies = new ArrayList<>();
  }

  public void addEndpoint(Endpoint endpoint) {
    endpoints.add(endpoint);
  }

  public void addDependency(Dependency dependency) {
    dependencies.add(dependency);
  }

  public void addExternalDependency(Dependency dependency) {
    externalDependencies.add(dependency);
  }
}
