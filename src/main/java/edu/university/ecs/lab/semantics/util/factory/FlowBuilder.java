package edu.university.ecs.lab.semantics.util.factory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import edu.university.ecs.lab.semantics.entity.*;
import edu.university.ecs.lab.semantics.entity.graph.*;
import edu.university.ecs.lab.semantics.util.MsCache;

/**
 * This class manages the building of the flows from controller down to repository 
 * using the entries in MsCache
 * 
 * For method comments, (service/controller/repository) indicites either the MsClass type
 * or where the method call/definition would be located
 */
public class FlowBuilder {

    /**
     * Main function called to build flows and store them in MsCache
     */
    public void buildFlows(){
        List<MsFlowEntity> msFlowEntities = findControllerMethods();
        for (MsFlowEntity msFlowEntity: msFlowEntities
             ) {
            // 1. get controller
            msFlowEntity.setMsController(findController(msFlowEntity));
            
            
//            System.err.println("CONTROLLER : " + msFlowEntity.getMsControllerMethod().getMethodName());
            // 2. get service method call in controller method
            Optional<MsMethodCall> serviceMethodCall = findServiceCall(msFlowEntity.getMsControllerMethod());
            if (serviceMethodCall.isPresent()) {
                msFlowEntity.setMsServiceMethodCall(serviceMethodCall.get());
                
                // 3. get service field variable in controller class by method call
                Optional<MsField> serviceField = findServiceField(msFlowEntity.getMsServiceMethodCall());
                if (serviceField.isPresent()) {
                    msFlowEntity.setMsControllerServiceField(serviceField.get());
                    // 4. get service class
                    Optional<MsClass> msServiceClass = findService(msFlowEntity.getMsControllerServiceField());
                    if (msServiceClass.isPresent()) {
                        msFlowEntity.setMsService(msServiceClass.get());
                        // 5. find service method name
                        Optional<MsMethod> msServiceMethod = findMsServiceMethod(msFlowEntity.getMsService(), msFlowEntity.getMsServiceMethodCall());
                        if (msServiceMethod.isPresent()) {
                            msFlowEntity.setMsServiceMethod(msServiceMethod.get());
//                            System.err.println("SERVICE : " + msServiceMethod.get().getMethodName());
                            // 5. get rest calls
                            List<MsRestCall> restCalls = findRestCalls(msFlowEntity.getMsServiceMethod());
//                            System.err.println("REST CALLS = " + restCalls.size());
                            msFlowEntity.setMsRestCalls(restCalls);
                            // 6. find method call in the service
                            Optional<MsMethodCall> repositoryMethodCall =
                                    findMsRepositoryMethodCall(msFlowEntity.getMsService(), msFlowEntity.getMsServiceMethod());
                            if (repositoryMethodCall.isPresent()) {
                                msFlowEntity.setMsRepositoryMethodCall(repositoryMethodCall.get());
                                // 7. find repository variable
                                Optional<MsField> repositoryField = findRepositoryField(msFlowEntity.getMsService(), msFlowEntity.getMsRepositoryMethodCall());
                                if (repositoryField.isPresent()) {
                                    msFlowEntity.setMsServiceRepositoryField(repositoryField.get());
                                    // 8. find repository class
                                    Optional<MsClass> repositoryClass = findRepositoryClass(msFlowEntity.getMsServiceRepositoryField());
                                    if (repositoryClass.isPresent()) {
                                        msFlowEntity.setMsRepository(repositoryClass.get());
                                        // 9. find repository method
                                        Optional<MsMethod> repositoryMethod = findRepositoryMethod(msFlowEntity.getMsRepository(), msFlowEntity.getMsRepositoryMethodCall());
                                        if (repositoryMethod.isPresent()) {
                                            msFlowEntity.setMsRepositoryMethod(repositoryMethod.get());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
//            if (msFlowEntity.getMsController() != null) {
//            	System.err.println(msFlowEntity.getMsController().getPackageName());
//            }
        }
        MsCache.msFlows = msFlowEntities;
    }

    /**
     * Finds the MsMethod (repository) of a given MsMethodCall (service)
     * 
     * @param msRepository the repository associated with the msRepositoryMethodCall (service?)
     * @param msRepositoryMethodCall the MsMethodCall (service?) being searched
     * @return the MsMethod (repository) if found
     */
    private Optional<MsMethod> findRepositoryMethod(MsClass msRepository, MsMethodCall msRepositoryMethodCall) {
        return MsCache.msMethodList.stream()
                .filter(n -> n.getMsId().getPath().equals(msRepository.getMsId().getPath()) && n.getMethodName().equals(msRepositoryMethodCall.getCalledMethodName()))
                .findFirst();
    }

    /**
     * Finds the MsClass (repository) of a given MsField (service) and returns it
     * 
     * @param msServiceRepositoryField the MsField (service) being searched
     * @return the MsClass (repository) if found
     */
    private Optional<MsClass> findRepositoryClass(MsField msServiceRepositoryField) {
        return MsCache.msClassList.stream()
                .filter(n -> n.getClassName().equals(msServiceRepositoryField.getFieldClass()))
                .findFirst();
    }

    /**
     * Finds the MsField (repository) associated with a MsMethodCall (repository)
     * 
     * @param msService the MsClass (service) being searched
     * @param repositoryMethodCall the MsMethodCall (repository) being searched
     * @return the MsField (repository) if found
     */
    private Optional<MsField> findRepositoryField(MsClass msService, MsMethodCall repositoryMethodCall) {
        return MsCache.msFieldList.stream()
                .filter(n -> n.getMsId().getPath().equals(msService.getMsId().getPath()) && n.getFieldVariable().equals(repositoryMethodCall.getCalledServiceId()))
                .findFirst();
    }

    /**
     * Finds the MsMethodCall (repository) associated with a MsMethod (service)
     * 
     * @param msService the MsClass (service) being searched
     * @param msServiceMethod the MsMethod (service) being searched
     * @return the MsMethodCall (repository) if found
     */
    private Optional<MsMethodCall> findMsRepositoryMethodCall(MsClass msService, MsMethod msServiceMethod) {
        return MsCache.msMethodCallList.stream()
                .filter(n -> n.getMsId().getPath().equals(msService.getMsId().getPath()) && n.getParentMethodName().equals(msServiceMethod.getMethodName()))
                .findFirst();
    }

    /**
     * Finds the MsMethod (service) associated with a MsMethodCall (controller)
     * 
     * @param msService the MsClass (service) being searched
     * @param controllerServiceMethodCall the MsMethodCall (controller) being searched
     * @return the MsMethod (service) if found
     */
    private Optional<MsMethod> findMsServiceMethod(MsClass msService, MsMethodCall controllerServiceMethodCall) {
        return MsCache.msMethodList.stream()
                .filter(n -> n.getMsId().getPath().equals(msService.getMsId().getPath()) && n.getMethodName().equals(controllerServiceMethodCall.getCalledMethodName()))
                .findFirst();
    }

    /**
     * Finds all MsRestCall (?) associated with a MsMethod (controller)
     * 
     * @param msMethodService the MsMethod (controller) being searched
     * @return all instances of MsRestCall (?)
     */
    private List<MsRestCall> findRestCalls(MsMethod msMethodService) {
        return MsCache.msRestCallList.stream()
                .filter(n -> n.getParentClassName().equals(msMethodService.getClassName()) && n.getParentMethodName().equals(msMethodService.getMethodName()))
                .collect(Collectors.toList());
    }

    /**
     * Finds the MsClass (service) associated with MsField (controller)
     * 
     * @param msControllerServiceField the MsField (controller) being searched
     * @return the MsClass (service) if found
     */
    private Optional<MsClass> findService(MsField msControllerServiceField) {
        return MsCache.msClassList.stream()
                .filter(n -> n.getClassName().equals(msControllerServiceField.getFieldClass() + "Impl"))
                .findFirst();
    }

    /**
     * Finds the MsField (controller) associated with MsMethodCall (controller)
     * 
     * @param msMethodCall the MsMethodCall (controller) being searched
     * @return the MsField (controller) if found
     */
    private Optional<MsField> findServiceField(MsMethodCall msMethodCall) {
        return MsCache.msFieldList.stream()
                .filter(n -> n.getMsId().getPath().equals(msMethodCall.getMsId().getPath()) && n.getFieldVariable().equals(msMethodCall.getCalledServiceId()))
                .findFirst();
    }

    /**
     * Finds all MsFlowEntity (?)
     * 
     * @return all instances of MsFlowEntity
     */
    public List<MsFlowEntity> findControllerMethods()  {
        return MsCache.msMethodList
                .stream()
                .filter(n -> n.getMsId().getPath().toLowerCase().contains(MsClassRoles.CONTROLLER.toString().toLowerCase()))
                .map(MsFlowEntity::new)
                .collect(Collectors.toList());
    }

    /**
     * Finds the MsClass (controller) associated with MsFlowEntity (?)
     * 
     * @param msFlowEntity the MsFlowEntity (?) being searched
     * @return the MsClass (controller) if found
     */
    public MsClass findController(MsFlowEntity msFlowEntity) {
        return MsCache.msClassList
                .stream()
                .filter(n -> n.getMsId().getPath().equals(msFlowEntity.getMsControllerMethod().getMsId().getPath()))
                .findFirst()
                .get();
    }

    /**
     * Finds the MsMethodCall (controller) associated with MsMethodCall (service)
     * 
     * @param msControllerMethod the MsMethod (controller) being searched
     * @return the MsMethodCall (service) if found
     */
    private Optional<MsMethodCall> findServiceCall(MsMethod msControllerMethod) {
        return MsCache.msMethodCallList
                .stream()
                .filter(n -> n.getParentMethodName().equals(msControllerMethod.getMethodName()) && n.getMsId().getPath().equals(msControllerMethod.getMsId().getPath()))
                .findFirst();
    }


}
