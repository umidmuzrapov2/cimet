package edu.university.ecs.lab.common.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RestCallAnnotation {
  HTTP_GET ("HttpGet"),
  HTTP_POST ("HttpPost"),
  HTTP_PUT ("HttpPut"),
  HTTP_PATCH ("HttpPatch"),
  HTTP_DELETE ("HttpDelete"),
  HTTP_HEAD ("HttpHead"),
  HTTP_OPTIONS ("HttpOptions"),
  HTTP_TRACE ("HttpTrace"),
  GET_FOR_OBJECT ("getForObject"),
  GET_FOR_ENTITY ("getForEntity"),
  POST_FOR_OBJECT ("postForObject"),
  POST_FOR_ENTITY ("postForEntity"),
  EXCHANGE ("exchange"),
  NEW_CALL("newCall"),
  EXECUTE("execute"),
  ENQUEUE("enqueue"),
  CREATE("create");

  private final String annotation;
}
