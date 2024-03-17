package edu.university.ecs.lab.intermediate.merger.models;

import com.google.gson.annotations.SerializedName;
import edu.university.ecs.lab.common.models.rest.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class Change {
  @SerializedName("restControllers")
  private List<RestController> restControllers;

  @SerializedName("restCalls")
  private List<RestCall> restCalls;

  @SerializedName("services")
  private List<RestService> restServices;

  @SerializedName("dtos")
  private List<RestDTO> restDTOs;

  @SerializedName("repositories")
  private List<RestRepository> restRepositories;

  @SerializedName("entities")
  private List<RestEntity> restEntities;
}
