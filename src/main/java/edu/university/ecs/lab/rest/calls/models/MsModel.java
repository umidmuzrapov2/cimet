package edu.university.ecs.lab.rest.calls.models;

import com.google.gson.annotations.SerializedName;
import edu.university.ecs.lab.common.models.rest.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** Model to represent the microservice object as seen in IR output */
@Data
public class MsModel {
  @SerializedName("id")
  private String id;

  @SerializedName("commitId")
  private String commit;

  /** List of rest endpoints found in the microservice */
  @SerializedName("controllers")
  private List<RestController> restControllers;

  /** List of rest services found in microservice */
  @SerializedName("services")
  private List<RestService> restServices;

  /** List of rest dtos found in microservice */
  @SerializedName("dtos")
  private List<RestDTO> restDTOs;

  /** List of rest repositories found in microservice */
  @SerializedName("repositories")
  private List<RestRepository> restRepositories;

  /** List of rest entities found in microservice */
  @SerializedName("entities")
  private List<RestEntity> restEntities;

  /** Direct API calls that this ms has to another ms */
  @SerializedName("restCalls")
  private List<RestCall> restCalls;

  // TODO remove
  @Deprecated private List<RestCall> externalCalls;

  /** Default constructor, init lists as empty */
  public MsModel() {
    restControllers = new ArrayList<>();
    restCalls = new ArrayList<>();
    externalCalls = new ArrayList<>();
  }

  /** Add an endpoint to the list of endpoints */
  public void addEndpoint(RestController restController) {
    restControllers.add(restController);
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
