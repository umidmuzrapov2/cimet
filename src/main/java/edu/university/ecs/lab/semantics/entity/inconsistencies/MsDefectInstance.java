package edu.university.ecs.lab.semantics.entity.inconsistencies;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MsDefectInstance {
    private String msPackage;
    private String msClass;
    private String msMethod;
    private String lineNumber;
    private String variableType;
    private String variableName;
    private String code;
}