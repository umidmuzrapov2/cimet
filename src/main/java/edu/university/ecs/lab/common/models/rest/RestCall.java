package edu.university.ecs.lab.common.models.rest;

import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RestCall {
  private String api;
  private String sourceFile;
  private String callDest;
  private String httpMethod;
  private String callMethod;
  private String callClass;
}
