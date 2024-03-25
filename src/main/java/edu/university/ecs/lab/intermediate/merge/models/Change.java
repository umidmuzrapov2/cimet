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
  private List<JClass> controllers;

  @SerializedName("services")
  private List<JClass> services;

  @SerializedName("dtos")
  private List<JClass> restDTOs;

  @SerializedName("repositories")
  private List<JClass> repositories;

  @SerializedName("entities")
  private List<JClass> entities;
}
