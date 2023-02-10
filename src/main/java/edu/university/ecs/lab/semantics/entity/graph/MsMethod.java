package edu.university.ecs.lab.semantics.entity.graph;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MsMethod {
    private MsId msId;
    private String protection;
    private String returnType;
    private String methodName;
    private String className;
    private String packageName;
    private String methodId;
    private String classId;
    private int line;
    private List<MsArgument> msArgumentList;
    private String mapping;
    private String mappingPath;
    private List<MsAnnotation> msAnnotations;

    public MsMethod(){
        this.msArgumentList = new ArrayList<>();
    }

    public void addArgument(MsArgument msArgument) {
        msArgumentList.add(msArgument);
    }

    public void setIds(){
        StringBuilder sb = new StringBuilder();
        sb.append(this.getPackageName());
        sb.append(".");
        sb.append(this.getClassName());
        sb.append(".");
        sb.append(this.getMethodName());
        this.methodId = sb.toString();
        sb = new StringBuilder();
        sb.append(this.getPackageName());
        sb.append(".");
        sb.append(this.getClassName());
        this.classId = sb.toString();
    }

    @Override
    public String toString() {
        return " [L " + this.getLine() + "] " + this.getMethodId();
    }
}
