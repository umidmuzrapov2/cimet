package edu.university.ecs.lab.semantics.util.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import edu.university.ecs.lab.semantics.entity.*;
import edu.university.ecs.lab.semantics.entity.graph.*;
import edu.university.ecs.lab.semantics.util.MsCache;


/**
 * 1. get controllers associated with the module
 * 2. create flows per each controller
 * 2a. per each controller find methods
 * 3. per each controller method find method calls
 * 4. per each method call find ms field
 * 5. ms field get type of the service
 * 6. find the service in the methods
 * 7. for each service find method calls
 * 8. for each service find rest calls -> add to the flow
 * 9. for each service method call find the field
 * 10. for each field find
 */
public class FlowsFactory {

    public static void createFlows(){

        for (String module: MsCache.modules
             ) {
            // 1. get controllers associated with the module
            List<MsClass> controllers = MsCache.msClassList
                    .stream()
                    .filter(n -> n.getRole().equals(MsClassRoles.CONTROLLER) && n.getMsId().getDirectoryName().contains(module))
                    .collect(Collectors.toList());
            // 2. create flows per each controller
            List<MsFlow> flows = new ArrayList<>();
            for (MsClass controller: controllers
                 ) {
                // Flows: init
                MsFlow msFlow = new MsFlow();
                msFlow.setModule(module);
                msFlow.setController(controller);
                // 2a. per each controller find method calls
                List<MsMethod> controllerMethods = MsCache.msMethodList
                        .stream()
                        .filter(n -> n.getMsId().getDirectoryName().equals(controller.getMsId().getDirectoryName()))
                        .collect(Collectors.toList());
                controllerMethods.forEach(cm -> {
                    // 2.b set controller method
                    msFlow.setControllerMethod(cm);
                    // 3. per each controller method find service method call(s)
                    List<MsMethodCall> controllerServiceCalls = MsCache.msMethodCallList
                            .stream()
                            .filter(n -> (n.getMsId().getPath()+n.getParentMethodName()).equals(cm.getMsId().getPath()+cm.getMethodName()))
                            .collect(Collectors.toList());
                    // 4. per each method call find ms field
                    List<MsField> controllerServiceFields = MsCache.msFieldList
                            .stream()
                            .filter(n -> n.getMsId().getPath().equals(cm.getMsId().getPath()))
                            .collect(Collectors.toList());

                    List<MsMethod> msServices = new ArrayList<>();
                    for (MsField msField: controllerServiceFields
                         ) {
//                        msServices.addAll(MsCache.msMethodList.stream().filter(n -> n.getM))
                    }

                    msFlow.setControllerServiceFields(controllerServiceFields);
                    flows.add(msFlow);
                });

            }
        }

        // for each controller


//        controllers.forEach(c -> {
//            // find method calls
//            List<MsMethodCall> serviceMethodCalls = MsCache.msMethodCallList
//                    .stream()
//                    .filter(mc -> c.getClassId().equals(mc.getParentClassId()))
//                    .collect(Collectors.toList());
//            // msMethodCall.getCalledServiceId() msMethodCall.getCalledClassId
//        });
        // find services

        // find method calls from the service

        // find repositories based on mc


    }
}
