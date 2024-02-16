package edu.university.ecs.lab.radsource.context;

import edu.university.ecs.lab.radsource.model.RestCall;
import edu.university.ecs.lab.radsource.model.RestEndpoint;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RestEntityContext {
    String pathToMsRoot;
    private List<RestCall> restCalls;
    private List<RestEndpoint> restEndpoints;
}
