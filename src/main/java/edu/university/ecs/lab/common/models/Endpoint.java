package edu.university.ecs.lab.common.models;

import lombok.*;

/**
 * Represents an extension of a method declaration.
 * An endpoint exists at the controller level and
 * signifies an open mapping that can be the target
 * of a rest call.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Endpoint extends Method {
    private String url;
    private String decorator;
    private String httpMethod;
    private String mapping;
    private String mappingPath;

    public Endpoint(Method method) {
        setMethodName(method.getMethodName());
        setProtection(method.getProtection());
        setParameterList(method.getParameterList());
        setReturnType(method.getReturnType());
    }
}
