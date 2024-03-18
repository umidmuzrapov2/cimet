package edu.university.ecs.lab.common.models;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
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
