package edu.university.ecs.lab.common.models;

import com.google.gson.annotations.SerializedName;
import edu.university.ecs.lab.common.models.enums.ClassRole;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the overarching structure of a
 * microservice system. It is composed of classes
 * which hold all information in that class.
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
  private List<JClass> classList;

  /** Default constructor, init lists as empty */
  public MsModel() {}

  public List<Endpoint> getAllEndpoints() {
    return getClassList().stream().flatMap(jClass -> jClass.getMethods().stream()).filter(mc -> mc instanceof Endpoint).map(mc -> ((Endpoint) mc)).collect(Collectors.toList());
  }

  public List<RestCall> getAllRestCalls() {
    return getClassList().stream().flatMap(jClass -> jClass.getMethodCalls().stream()).filter(mc -> mc instanceof RestCall).map(mc -> ((RestCall) mc)).collect(Collectors.toList());
  }

  public List<JClass> getClassesByRoles(ClassRole... classRoles) {
    return getClassList().stream().filter(jClass -> {
      for(ClassRole classRole : classRoles) {
        if(jClass.getRole().equals(classRole)) {
          return true;
        }
      }
      return false;
    }).collect(Collectors.toList());
  }


}
