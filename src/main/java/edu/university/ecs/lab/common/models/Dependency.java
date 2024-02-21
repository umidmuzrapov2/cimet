package edu.university.ecs.lab.common.models;

import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Dependency {
  private String url;
  private String sourceFile;
  private String destFile;
  private String httpMethod;
  private String parentMethod;
}
