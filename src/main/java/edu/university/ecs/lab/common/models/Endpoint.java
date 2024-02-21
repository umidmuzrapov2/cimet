package edu.university.ecs.lab.common.models;

import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Endpoint {
  private String url;
  private String sourceFile;
  private String decorator;
  private String httpMethod;
  private String parentMethod;
}
