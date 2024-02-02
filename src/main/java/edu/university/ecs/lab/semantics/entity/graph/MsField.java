package edu.university.ecs.lab.semantics.entity.graph;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class MsField extends MsParentMethod {
    private MsId msId;
    private String fieldClass;
    private String fieldVariable;
    private MsParentMethod parentMethod;
    private int line;

}
