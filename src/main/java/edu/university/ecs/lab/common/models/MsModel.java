package edu.university.ecs.lab.common.models;

import com.google.gson.annotations.SerializedName;
import edu.university.ecs.lab.common.models.JClass;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

/** Model to represent the microservice object as seen in IR output */
@Data
public class MsModel {
  @SerializedName("id")
  private String id;

  @SerializedName("commitId")
  private String commit;

  @SerializedName("msPath")
  private String msPath;

  /** List of classes */
  private List<JClass> classList;

  /** Default constructor, init lists as empty */
  public MsModel() {}

  public List<Endpoint> getAllEndpoints() {
    return getClassList().stream().flatMap(jClass -> jClass.getMethods().stream()).filter(mc -> mc instanceof Endpoint).map(mc -> ((Endpoint) mc)).collect(Collectors.toList());
  }

  public List<RestCall> getAllRestCalls() {
    return getClassList().stream().flatMap(jClass -> jClass.getMethodCalls().stream()).filter(mc -> mc instanceof RestCall).map(mc -> ((RestCall) mc)).collect(Collectors.toList());
  }


}
