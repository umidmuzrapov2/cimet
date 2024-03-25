package edu.university.ecs.lab.intermediate.merge.models;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Delta {
  private String localPath;
  private String changeType;

  @SerializedName("changes")
  private Change change;
}
