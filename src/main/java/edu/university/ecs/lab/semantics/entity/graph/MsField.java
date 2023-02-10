package edu.university.ecs.lab.semantics.entity.graph;

import lombok.Data;

@Data
public class MsField extends MsParentMethod {
    private MsId msId;
    private String fieldClass;
    private String fieldVariable;
    private MsParentMethod parentMethod;
    private int line;

//    @Override
//    public String toString() {
//        return "[L" + line + "] " +
//                parentMethod.getParentPackageName() + '.' +
//                parentMethod.getParentClassName() + '.' +
//                parentMethod.getParentMethodName() +
//                " : " +
//                fieldClass + '.' +
//                fieldVariable + '.'
//                ;
//    }
}
