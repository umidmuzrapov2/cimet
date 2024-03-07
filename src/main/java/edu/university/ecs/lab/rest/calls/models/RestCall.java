package edu.university.ecs.lab.rest.calls.models;

import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RestCall {
  private String url;
  private String sourceFile;
  private String destFile;
  private String httpMethod;
  private String callMethod;
  private String callClass;
}
