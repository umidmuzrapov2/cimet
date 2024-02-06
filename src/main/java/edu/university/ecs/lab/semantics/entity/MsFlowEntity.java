package edu.university.ecs.lab.semantics.entity;

import lombok.Data;

import java.util.List;

import edu.university.ecs.lab.semantics.entity.graph.*;

/** Represents a 'flow' which is a path from controller down to service */
@Data
public class MsFlowEntity {
  private MsClass msController;
  private MsMethod msControllerMethod;
  private MsMethodCall msServiceMethodCall;
  private MsField msControllerServiceField;
  private MsClass msService;
  private MsMethod msServiceMethod;
  private MsMethodCall msRepositoryMethodCall;
  private MsField msServiceRepositoryField;
  private MsClass msRepository;
  private List<MsRestCall> msRestCalls;
  private MsMethod msRepositoryMethod;

  /**
   * @param msController
   * @param msControllerMethod
   */
  public MsFlowEntity(MsClass msController, MsMethod msControllerMethod) {
    this.msController = msController;
    this.msControllerMethod = msControllerMethod;
  }

  public MsFlowEntity(MsMethod n) {
    this.msControllerMethod = n;
  }

  public String getPackageName() {
    if (msController != null) {
      return msController.getPackageName().split("\\.")[0];
    } else if (msService != null) {
      return msService.getPackageName().split("\\.")[0];
    } else if (msRepository != null) {
      return msRepository.getPackageName().split("\\.")[0];
    }
    return "";
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(msController.getClassName());
    sb.append(" -> ");
    sb.append(msControllerMethod.getMethodName());
    return sb.toString();
  }
}
