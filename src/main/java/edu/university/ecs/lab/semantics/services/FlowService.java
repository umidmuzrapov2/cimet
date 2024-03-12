package edu.university.ecs.lab.semantics.services;

import edu.university.ecs.lab.semantics.models.enums.ClassRole;
import edu.university.ecs.lab.semantics.models.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FlowService {

    /** Main function called to build flows and store them in Cache */
    public void buildFlows() {
        List<Flow> FlowEntities = findControllerMethods();
        for (Flow flow : FlowEntities) {
            // 1. get controller
            flow.setController(findController(flow));

            //            System.err.println("CONTROLLER : " +
            // Flow.getControllerMethod().getMethodName());
            // 2. get service method call in controller method
            Optional<MethodCall> serviceMethodCall =
                    findServiceCall(flow.getControllerMethod());
            if (serviceMethodCall.isPresent()) {
                flow.setServiceMethodCall(serviceMethodCall.get());

                // 3. get service field variable in controller class by method call
                Optional<Field> serviceField = findServiceField(flow.getServiceMethodCall());
                if (serviceField.isPresent()) {
                    flow.setControllerServiceField(serviceField.get());
                    // 4. get service class
                    Optional<JClass> ServiceClass =
                            findService(flow.getControllerServiceField());
                    if (ServiceClass.isPresent()) {
                        flow.setService(ServiceClass.get());
                        // 5. find service method name
                        Optional<Method> ServiceMethod =
                                findServiceMethod(
                                        flow.getService(), flow.getServiceMethodCall());
                        if (ServiceMethod.isPresent()) {
                            flow.setServiceMethod(ServiceMethod.get());
                            //                            System.err.println("SERVICE : " +
                            // ServiceMethod.get().getMethodName());
                            // 5. get rest calls
                            List<RestCall> restCalls = findRestCalls(flow.getServiceMethod());
                            //                            System.err.println("REST CALLS = " + restCalls.size());
                            flow.setRestCalls(restCalls);
                            // 6. find method call in the service
                            Optional<MethodCall> repositoryMethodCall =
                                    findRepositoryMethodCall(
                                            flow.getService(), flow.getServiceMethod());
                            if (repositoryMethodCall.isPresent()) {
                                flow.setRepositoryMethodCall(repositoryMethodCall.get());
                                // 7. find repository variable
                                Optional<Field> repositoryField =
                                        findRepositoryField(
                                                flow.getService(), flow.getRepositoryMethodCall());
                                if (repositoryField.isPresent()) {
                                    flow.setServiceRepositoryField(repositoryField.get());
                                    // 8. find repository class
                                    Optional<JClass> repositoryClass =
                                            findRepositoryClass(flow.getServiceRepositoryField());
                                    if (repositoryClass.isPresent()) {
                                        flow.setRepository(repositoryClass.get());
                                        // 9. find repository method
                                        Optional<Method> repositoryMethod =
                                                findRepositoryMethod(
                                                        flow.getRepository(),
                                                        flow.getRepositoryMethodCall());
                                        if (repositoryMethod.isPresent()) {
                                            flow.setRepositoryMethod(repositoryMethod.get());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            //            if (Flow.getController() != null) {
            //            	System.err.println(Flow.getController().getPackageName());
            //            }
            CachingService.getCache().getFlowList().add(flow);
        }
    }

    /**
     * Finds the Method (repository) of a given MethodCall (service)
     *
     * @param repository the repository associated with the RepositoryMethodCall (service?)
     * @param RepositoryMethodCall the MethodCall (service?) being searched
     * @return the Method (repository) if found
     */
    private Optional<Method> findRepositoryMethod(JClass repository, MethodCall RepositoryMethodCall) {
        return CachingService.getCache().getMethodList().stream()
                .filter(n ->
                        n.getId().getLocation().equals(repository.getId().getLocation()) && n.getMethodName().equals(RepositoryMethodCall.getCalledMethodName()))
                .findFirst();
    }

    /**
     * Finds the Class (repository) of a given Field (service) and returns it
     *
     * @param ServiceRepositoryField the Field (service) being searched
     * @return the Class (repository) if found
     */
    private Optional<JClass> findRepositoryClass(Field ServiceRepositoryField) {

        return CachingService.getCache().getClassList().stream()
                .filter(n -> n.getClassName().equals(ServiceRepositoryField.getFieldClass()))
                .findFirst();
    }

    /**
     * Finds the Field (repository) associated with a MethodCall (repository)
     *
     * @param Service the Class (service) being searched
     * @param repositoryMethodCall the MethodCall (repository) being searched
     * @return the Field (repository) if found
     */
    private Optional<Field> findRepositoryField(
            JClass Service, MethodCall repositoryMethodCall) {
        return CachingService.getCache().getFieldList().stream()
                .filter(
                        n ->
                                n.getId().getLocation().equals(Service.getId().getLocation())
                                        && n.getFieldVariable().equals(repositoryMethodCall.getCalledServiceId()))
                .findFirst();
    }

    /**
     * Finds the MethodCall (repository) associated with a Method (service)
     *
     * @param Service the Class (service) being searched
     * @param ServiceMethod the Method (service) being searched
     * @return the MethodCall (repository) if found
     */
    private Optional<MethodCall> findRepositoryMethodCall(
            JClass Service, Method ServiceMethod) {
        return CachingService.getCache().getMethodCallList().stream()
                .filter(
                        n ->
                                n.getId().getLocation().equals(Service.getId().getLocation())
                                        && n.getCalledMethodName().equals(ServiceMethod.getMethodName()))
                .findFirst();
    }

    /**
     * Finds the Method (service) associated with a MethodCall (controller)
     *
     * @param Service the Class (service) being searched
     * @param controllerServiceMethodCall the MethodCall (controller) being searched
     * @return the Method (service) if found
     */
    private Optional<Method> findServiceMethod(
            JClass Service, MethodCall controllerServiceMethodCall) {
        return CachingService.getCache().getMethodList().stream()
                .filter(
                        n ->
                                n.getId().getLocation().equals(Service.getId().getLocation())
                                        && n.getMethodName().equals(controllerServiceMethodCall.getCalledMethodName()))
                .findFirst();
    }

    /**
     * Finds all RestCall (?) associated with a Method (controller)
     *
     * @param MethodService the Method (controller) being searched
     * @return all instances of RestCall (?)
     */
    private List<RestCall> findRestCalls(Method MethodService) {
        return CachingService.getCache().getRestCallList().stream()
                .filter(
                        n ->
                                n.getMethodContext().getParentClassName().equals(MethodService.getClassName())
                                        && n.getMethodContext().getParentMethodName().equals(MethodService.getMethodName()))
                .collect(Collectors.toList());
    }

    /**
     * Finds the Class (service) associated with Field (controller)
     *
     * @param ControllerServiceField the Field (controller) being searched
     * @return the Class (service) if found
     */
    private Optional<JClass> findService(Field ControllerServiceField) {
        return CachingService.getCache().getClassList().stream()
                .filter(n -> n.getClassName().equals(ControllerServiceField.getFieldClass() + "Impl"))
                .findFirst();
    }

    /**
     * Finds the Field (controller) associated with MethodCall (controller)
     *
     * @param MethodCall the MethodCall (controller) being searched
     * @return the Field (controller) if found
     */
    private Optional<Field> findServiceField(MethodCall MethodCall) {
        return CachingService.getCache().getFieldList().stream()
                .filter(
                        n ->
                                n.getId().getLocation().equals(MethodCall.getId().getLocation())
                                        && n.getFieldVariable().equals(MethodCall.getCalledServiceId()))
                .findFirst();
    }

    /**
     * Finds all Flow (?)
     *
     * @return all instances of Flow
     */
    public List<Flow> findControllerMethods() {
        return CachingService.getCache().getMethodList().stream()
                .filter(
                        n ->
                                n.getId()
                                        .getLocation()
                                        .toLowerCase()
                                        .contains(ClassRole.CONTROLLER.toString().toLowerCase()))
                .map(Flow::new)
                .collect(Collectors.toList());
    }

    /**
     * Finds the Class (controller) associated with Flow (?)
     *
     * @param Flow the Flow (?) being searched
     * @return the Class (controller) if found
     */
    public JClass findController(Flow Flow) {
        return CachingService.getCache().getClassList().stream()
                .filter(
                        n ->
                                n.getId()
                                        .getLocation()
                                        .equals(Flow.getControllerMethod().getId().getLocation()))
                .findFirst()
                .get();
    }

    /**
     * Finds the MethodCall (controller) associated with MethodCall (service)
     *
     * @param ControllerMethod the Method (controller) being searched
     * @return the MethodCall (service) if found
     */
    private Optional<MethodCall> findServiceCall(Method ControllerMethod) {
        return CachingService.getCache().getMethodCallList().stream()
                .filter(
                        n ->
                                n.getMethodContext().getParentMethodName().equals(ControllerMethod.getMethodName())
                                        && n.getId().getLocation().equals(ControllerMethod.getId().getLocation()))
                .findFirst();
    }
}
