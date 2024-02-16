package edu.university.ecs.lab.radsource.context;

import edu.university.ecs.lab.radsource.model.RestFlow;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RadSourceResponseContext {
  private RadSourceRequestContext request;
  private List<RestEntityContext> restEntityContexts;
  private List<RestFlow> restFlows;
}
