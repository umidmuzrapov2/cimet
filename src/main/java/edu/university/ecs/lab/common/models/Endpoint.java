package edu.university.ecs.lab.common.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.ToString;

@Data @ToString
@AllArgsConstructor
public class Endpoint {
  @NonNull
  private final String url;

  @NonNull
  private final String sourceFile;
}
