package edu.university.ecs.lab.intermediate.merge.models;

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
  @SerializedName("controllers")
  private List<JController> controllers;

  @SerializedName("services")
  private List<JService> services;

  @SerializedName("dtos")
  private List<JClass> dtos;

  @SerializedName("repositories")
  private List<JClass> repositories;

  @SerializedName("entities")
  private List<JClass> entities;
}
