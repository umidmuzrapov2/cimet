package edu.university.ecs.lab.semantics.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A object representing a method argument in code? */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Parameter {
  private String returnType;
}
