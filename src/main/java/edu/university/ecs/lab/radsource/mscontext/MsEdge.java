package edu.university.ecs.lab.radsource.mscontext;

import lombok.Data;

@Data
public class MsEdge {
  private MsNode from;
  private MsNode to;
  private Integer width = 1;
  private Integer length = 200;
  private MsLabel label;

  @Override
  public String toString() {
    return String.format("{ from: %s, to: %s, width: %d, length: %d, label: %s }", from, to, width, length, label);
  }
}