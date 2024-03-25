package edu.university.ecs.lab.common.models;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

/**
 * Represents the overarching structure of a microservice system. It is composed of classes which
 * hold all information in that class.
 */
@Data
public class MsModel {
  @SerializedName("id")
  private String id;

  @SerializedName("commitId")
  private String commit;

  @SerializedName("msPath")
  private String msPath;

  /** List of classes */
  private List<JController> controllers;

  private List<JService> services;
  private List<JClass> dtos;
  private List<JClass> repositories;
  private List<JClass> entities;

  /** Default constructor, init lists as empty */
  public MsModel() {}
}
