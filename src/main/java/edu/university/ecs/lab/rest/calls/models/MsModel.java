package edu.university.ecs.lab.rest.calls.models;

import com.google.gson.annotations.SerializedName;
import edu.university.ecs.lab.common.models.JClass;
import lombok.Data;

import java.util.List;

/** Model to represent the microservice object as seen in IR output */
@Data
public class MsModel {
  @SerializedName("id")
  private String id;

  @SerializedName("commitId")
  private String commit;

  /** List of classes */
  private List<JClass> classList;

  /** Default constructor, init lists as empty */
  public MsModel() {}


}
