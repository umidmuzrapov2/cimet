package edu.university.ecs.lab.semantics.models;

import edu.university.ecs.lab.semantics.utils.UniqueIdGenerator;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

/**
 * This class identifies any particular model in the program, its location
 * and its associated project and gives an exact identifier to the model
 */
@Setter
@Getter
public class Id {
  private final long id;
  private String project;
  private String location;

  public Id() {
    id = UniqueIdGenerator.getUniqueID();
  }

}
