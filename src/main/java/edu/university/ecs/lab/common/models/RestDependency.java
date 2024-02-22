package edu.university.ecs.lab.common.models;

import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RestDependency {
  private String url;
  private String sourceFile;
  private String destFile;
  private String httpMethod;
  private String parentMethod;
}
