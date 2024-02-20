package edu.university.ecs.lab.common.config;

import lombok.Getter;

import java.util.List;

@Getter
public class InputConfig {
  private String outputPath;
  private String clonePath;
  private List<Microservice> microservices;
}
