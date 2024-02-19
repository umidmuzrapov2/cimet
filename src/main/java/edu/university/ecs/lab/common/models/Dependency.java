package edu.university.ecs.lab.common.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.ToString;

@Data @ToString
@AllArgsConstructor
public class Dependency {
  @NonNull
  private String url;

  @NonNull
  private String sourceFile;

  @NonNull
  private String destFile;

  @NonNull
  private String callType;
}
