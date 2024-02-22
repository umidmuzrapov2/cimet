package edu.university.ecs.lab.common.models;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Model to represent the microservice object as seen in IR output
 */
@Data
public class MsModel {
  /** List of rest endpoints found in the service */
  private List<Endpoint> endpoints;
  /** Direct API dependencies that this service has to another service */
  private List<RestDependency> restDependencies;

  private String commit;

  private String id;

  //TODO remove
  @Deprecated
  private List<RestDependency> externalDependencies;

  /**
   * Default constructor, init lists as empty
   */
  public MsModel() {
    endpoints = new ArrayList<>();
    restDependencies = new ArrayList<>();
    externalDependencies = new ArrayList<>();
  }

  /** Add an endpoint to the list of endpoints */
  public void addEndpoint(Endpoint endpoint) {
    endpoints.add(endpoint);
  }

    /** Add a direct call dependency to the list of dependencies */
  public void addDependency(RestDependency restDependency) {
    restDependencies.add(restDependency);
  }

  @Deprecated
  public void addExternalDependency(RestDependency restDependency) {
    externalDependencies.add(restDependency);
  }
}
