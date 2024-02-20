package edu.university.ecs.lab.common.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
public class Endpoint {
  @NonNull private String url;

  @NonNull private String sourceFile;

  @NonNull private String restType;

  @NonNull private String httpMethod;
}
