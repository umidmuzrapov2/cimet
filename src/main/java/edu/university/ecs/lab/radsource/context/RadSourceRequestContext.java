package edu.university.ecs.lab.radsource.context;

import lombok.*;

import java.util.List;

// TODO: Adapt to take our standard json input
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RadSourceRequestContext {
  /** List of MS root folder paths */
  private List<String> pathToMsRoots;
}
