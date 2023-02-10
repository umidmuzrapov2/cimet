package edu.university.ecs.lab.semantics.entity.graph;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class MsRestCall extends MsMethodCall{
    private String api;
    private String httpMethod;
    private String returnType;
    
    @Override
    public String toString() {
        return super.toString(); 
        
    }
}
