package edu.university.ecs.lab.semantics.entity.graph;

import lombok.Data;

/**
 * An object representing a parent method in code?
 */
@Data
public class MsParentMethod {
    private String parentMethodName;
    private String parentClassName;
    private String parentPackageName;
}
