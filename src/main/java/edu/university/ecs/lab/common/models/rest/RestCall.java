package edu.university.ecs.lab.common.models.rest;

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
}
