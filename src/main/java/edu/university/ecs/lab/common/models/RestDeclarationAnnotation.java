package edu.university.ecs.lab.common.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RestDeclarationAnnotation {
  PATH ("@Path"),
  REQUEST_MAPPING ("@RequestMapping"),
  GET_MAPPING ("@GetMapping"),
  POST_MAPPING ("@PostMapping"),
  PUT_MAPPING ("@PutMapping"),
  DELETE_MAPPING ("@DeleteMapping"),
  GET ("@GET"),
  POST ("@POST"),
  PUT ("@PUT"),
  DELETE ("@DELETE");

  private final String annotation;
}
