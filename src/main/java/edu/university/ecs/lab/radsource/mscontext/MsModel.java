package edu.university.ecs.lab.radsource.mscontext;

import lombok.Data;
import java.util.HashSet;
import java.util.Set;

@Data
public class MsModel {
  private Set<MsNode> nodes = new HashSet<>();
  private Set<MsEdge> edges = new HashSet<>();
}
