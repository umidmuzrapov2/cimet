package edu.university.ecs.lab.common.models.rest;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** Model to represent the microservice object as seen in IR output */
@Data
public class MsModel {
  /** List of rest endpoints found in the service */
  private List<RestEndpoint> restEndpoints;

  /** Direct API calls that this service has to another service */
  private List<RestCall> restCalls;

  private String commit;

  private String id;

  // TODO remove
  @Deprecated private List<RestCall> externalCalls;

  /** Default constructor, init lists as empty */
  public MsModel() {
    restEndpoints = new ArrayList<>();
    restCalls = new ArrayList<>();
    externalCalls = new ArrayList<>();
  }

  /** Add an endpoint to the list of endpoints */
  public void addEndpoint(RestEndpoint restEndpoint) {
    restEndpoints.add(restEndpoint);
  }

  /** Add a direct call dependency to the list of dependencies */
  public void addRestCall(RestCall restCall) {
    restCalls.add(restCall);
  }

  @Deprecated
  public void addExternalCall(RestCall restCall) {
    externalCalls.add(restCall);
  }
}
