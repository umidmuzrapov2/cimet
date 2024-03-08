package edu.university.ecs.lab.semantics.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Field extends MethodLocation {
  private Id id;
  private String fieldClass;
  private String fieldVariable;
  private MethodLocation methodLocation;
  private int line;
}
