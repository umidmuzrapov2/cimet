package edu.university.ecs.lab.radsource.rad.context;

import edu.university.ecs.lab.radsource.rad.model.RestFlow;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains a list of {@link edu.university.ecs.lab.radsource.rad.model.RestFlow}
 * resulted after performing the REST flow analysis for all microservices.
 *
 * @author Dipta Das
 */

@Getter
@ToString
public class RestFlowContext {
    private List<RestFlow> restFlows = new ArrayList<>();
}
