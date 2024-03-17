//package edu.university.ecs.lab.common.models.rest;
//
//import edu.university.ecs.lab.common.config.InputConfig;
//import edu.university.ecs.lab.common.models.JavaVariable;
//import lombok.*;
//
//import java.util.ArrayList;
//import java.util.List;
//
///** Model to represent a Java controller class containing endpoints */
//@Data
//@ToString
//@AllArgsConstructor
//@NoArgsConstructor
//public class RestController {
//  private String className;
//
//  /**
//   * The java source code file the endpoint was found in, path after {@link
//   * InputConfig#getClonePath()}
//   */
//  private String sourceFile;
//
//  private List<RestEndpoint> restEndpoints = new ArrayList<>(); /* composes endpoints */
//  private List<JavaVariable> variables;
//
//  public void addEndpoint(RestEndpoint restEndpoint) {
//    restEndpoints.add(restEndpoint);
//  }
//}
