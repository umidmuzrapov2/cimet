package edu.university.ecs.lab.common.models;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MsModel {
  private List<Endpoint> endpoints;
  private List<Endpoint> dependencies;

  public MsModel() {
    endpoints = new ArrayList<>();
    dependencies = new ArrayList<>();
  }

  public void addEndpoint(Endpoint endpoint) {
    endpoints.add(endpoint);
  }

  public void addDependency(Endpoint dependency) {
    dependencies.add(dependency);
  }
}
