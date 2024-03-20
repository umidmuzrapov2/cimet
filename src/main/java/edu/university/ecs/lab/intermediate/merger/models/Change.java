package edu.university.ecs.lab.intermediate.merger.models;

import com.google.gson.annotations.SerializedName;
import edu.university.ecs.lab.common.models.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class Change {
  @SerializedName("restControllers")
  private List<JClass> restControllers;

  @SerializedName("restCalls")
  private List<RestCall> restCalls;

  @SerializedName("services")
  private List<JClass> restServices;

  @SerializedName("dtos")
  private List<JClass> restDTOs;

  @SerializedName("repositories")
  private List<JClass> restRepositories;

  @SerializedName("entities")
  private List<JClass> restEntities;
}
