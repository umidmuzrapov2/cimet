package edu.university.ecs.lab.common.models;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** Represents a method declaration in Java. */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Method {
  private String methodName;
  private String protection;

  @SerializedName("parameter")
  private String parameterList;

  private String returnType;
  //  private List<Annotation> annotations;

}
