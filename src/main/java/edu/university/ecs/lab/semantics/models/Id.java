package edu.university.ecs.lab.semantics.models;

import edu.university.ecs.lab.semantics.utils.UniqueIdGenerator;
import lombok.Data;

/**
 * This class identifies any particular model in the program, its location and its associated
 * project and gives an exact identifier to the model
 */
@Data
public class Id {
  private final long id;
  private String project;
  private String location;

  public Id() {
    id = UniqueIdGenerator.getUniqueID();
  }

  public Id(String project, String location) {
    id = UniqueIdGenerator.getUniqueID();
    this.project = project;
    this.location = location;
  }
}
