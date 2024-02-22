package edu.university.ecs.lab.common.models;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MsModel {
  private List<Endpoint> endpoints;
  private List<RestDependency> dependencies;

  private List<RestDependency> externalDependencies;

  public MsModel() {
    endpoints = new ArrayList<>();
    dependencies = new ArrayList<>();
    externalDependencies = new ArrayList<>();
  }

  public void addEndpoint(Endpoint endpoint) {
    endpoints.add(endpoint);
  }

  public void addDependency(RestDependency restDependency) {
    dependencies.add(restDependency);
  }

  public void addExternalDependency(RestDependency restDependency) {
    externalDependencies.add(restDependency);
  }
}
