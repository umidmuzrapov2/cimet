package edu.university.ecs.lab.common.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChangeInformation {
  private String localLine;
  private String remoteLine;
  private int lineNumber;
}