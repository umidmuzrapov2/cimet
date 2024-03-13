package edu.university.ecs.lab.semantics.services;

import edu.university.ecs.lab.semantics.models.*;

import java.util.List;

public class CodeCloneService {

  private EntitySimilarityCheckStrategy entitySimilarityChecker;

  public CodeCloneService() {}

  public CodeCloneService(EntitySimilarityCheckStrategy entitySimilarityChecker) {
    this.entitySimilarityChecker = entitySimilarityChecker;
  }

  public void findCodeClones() {

    Cache cache = CachingService.getCache();

    // compare each flow with each flow


    for (int i = 0; i < cache.getFlowList().size(); i++) {
      for (int j = 0; j < i; j++) {

        Flow flowA = cache.getFlowList().get(i);
        Flow flowB = cache.getFlowList().get(j);

        // skip if flows are the same
        if (flowA.equals(flowB)) {
          continue;
        }

        // Initialize code clone
        CodeClone codeClone = new CodeClone();
        codeClone.setFlowA(flowA);
        codeClone.setFlowB(flowB);

        // Check Controller Method
        compareController(codeClone, flowA, flowB);

        // Check Service Method
        compareService(codeClone, flowA.getServiceMethod(), flowB.getServiceMethod());

        // Check Repository Method
        compareRepository(codeClone, flowA.getRepositoryMethod(), flowB.getRepositoryMethod());

        // Check Rest Calls
        compareRestCalls(codeClone, flowA.getRestCalls(), flowB.getRestCalls());

        // Calculate full similarity score
        codeClone.setGlobalSimilarity(calculateGlobalSimilarity(codeClone));

        // Add code clone to cache
        CachingService.getCache().getCodeCloneList().add(codeClone);


      }
    }
  }

  private double calculateGlobalSimilarity(CodeClone msCodeClone) {
    return (msCodeClone.getSimilarityController() * 0.8)
        + (msCodeClone.getSimilarityService() * 0.05)
        + (msCodeClone.getSimilarityRepository() * 0.05)
        + (msCodeClone.getSimilarityRestCalls() * 0.1);
  }

  private void compareController(CodeClone msCodeClone, Flow a, Flow b) {
    double same = 0.0;

    if (a == null || b == null) {
      msCodeClone.setSimilarityController(0.0);
      return;
    }

    Method aCtrl = a.getControllerMethod();
    Method bCtrl = b.getControllerMethod();

    if (aCtrl == null && bCtrl == null) {
      msCodeClone.setCtrMethodNameSimilarity(-1.0);
      msCodeClone.setCtrReturnTypeLiteralSimilarity(-1.0);
      msCodeClone.setCtrHttpMethodSimilarity(-1.0);
      msCodeClone.setCtrArgumentsLiteralSimilarity(-1.0);
    }

    if (aCtrl == null || bCtrl == null) {
      msCodeClone.setSimilarityController(0.0);
      return;
    }

    //        private double ctrArgumentsSemanticSimilarity;
    //        private double ctrReturnTypeSemanticSimilarity;
    //        private double ctrHttpMethodSimilarity;

    //        System.err.println("CTR - " + aCtrl.getMethodName() + " --- " +
    // bCtrl.getMethodName());

    if (aCtrl.getMethodName().equalsIgnoreCase(bCtrl.getMethodName())) {
      same += 1.0;
      msCodeClone.setCtrMethodNameSimilarity(1.0); //
    }

    if (aCtrl.getReturnType().equalsIgnoreCase(bCtrl.getReturnType())) {
      same += 1.0;
      //            double similarityValue =
      // this.entitySimilarityChecker.calculateSimilarity(aCtrl.getReturnType(),
      // bCtrl.getReturnType());
      //            msCodeClone.setCtrReturnTypeLiteralSimilarity(similarityValue); //
    }

    // Rico commented out
    //
    // msCodeClone.setCtrReturnTypeLiteralSimilarity(this.entitySimilarityChecker.calculateSimilarity(msCodeClone.getFlowA().getPackageName(), msCodeClone.getFlowB().getPackageName(), aCtrl.getReturnType(), bCtrl.getReturnType())); //

    double annotationsSimilarity = 0;
    int maxNumberAnnotations =
        Math.max(aCtrl.getAnnotations().size(), bCtrl.getAnnotations().size());

    for (Annotation aA : aCtrl.getAnnotations()) {
      for (Annotation bA : bCtrl.getAnnotations()) {
        if (aA.getAnnotationName().equalsIgnoreCase(bA.getAnnotationName())) {
          if (aCtrl.getReturnType().equals(bCtrl.getReturnType())) {
            same += 1.0;
          }
          annotationsSimilarity += 1;
        }
      }
    }

    msCodeClone.setCtrHttpMethodSimilarity(annotationsSimilarity / maxNumberAnnotations); //

    // Rico commented out (no Entity)
    //        double argsSimilarity =
    // this.entitySimilarityChecker.calculateArgumentsSimilarity(aCtrl.getParameterList(),
    // bCtrl.getParameterList());
    //        msCodeClone.setCtrArgumentsLiteralSimilarity(Double.isNaN(argsSimilarity) ? 0.0 :
    // argsSimilarity);

    //
    // msCodeClone.setCtrArgumentsLiteralSimilarity(compareArguments(aCtrl.getMsArgumentList(),
    // bCtrl.getMsArgumentList())); //

    //        double argumentSimilarity = 0;
    //        List<Parameter> aArguments = aCtrl.getParameterList();
    //        List<Parameter> bArguments = bCtrl.getParameterList();
    //        double maxNumberArguments = Math.max(aArguments.size(), bArguments.size());
    //         List<Integer> usedArguments = new ArrayList<>();
    //         for (int i = 0; i < aArguments.size(); i++) {
    //             for (int j = 0; j < bArguments.size(); j++) {
    //
    //                 if (aArguments.get(i).getReturnType() != null &&
    // bArguments.get(j).getReturnType() != null && !usedArguments.contains(j)
    //                         &&
    // aArguments.get(i).getReturnType().equalsIgnoreCase(bArguments.get(j).getReturnType())) {
    //                             usedArguments.add(j);
    //                             same += 1.0;
    //                             argumentSimilarity += 1;
    //                     break;
    //                 }
    //             }
    //         }

    //
    // msCodeClone.setCtrArgumentsLiteralSimilarity((double)(argumentSimilarity/maxNumberArguments)); //

    //        for (MsArgument msA: aCtrl.getMsArgumentList()
    //             ) {
    //            for (MsArgument msB: bCtrl.getMsArgumentList()
    //                 ) {
    //                if (msA.getReturnType().equals(msB.getReturnType())){
    //                    same += 1.0;
    //                }
    //            }
    //        }

    msCodeClone.setSimilarityController(same / 4);
  }

  private void compareService(CodeClone msCodeClone, Method a, Method b) {

    //        private double srvArgumentsSemanticSimilarity;
    //        private double srvReturnTypeSemanticSimilarity;

    if (a == null && b == null) {
      msCodeClone.setSrvMethodNameSimilarity(-1.0);
      msCodeClone.setSrvReturnTypeLiteralSimilarity(-1.0);
      msCodeClone.setSrvArgumentsLiteralSimilarity(-1.0);
    }

    if (a == null || b == null) {
      msCodeClone.setSimilarityService(0.0);
      return;
    }

    double same = 0.0;
    if (a.getReturnType() != null
        && b.getReturnType() != null
        && a.getReturnType().equalsIgnoreCase(b.getReturnType())) {
      same += 0.5;

      // Rico commented out
      //            double similarityValue =
      // this.entitySimilarityChecker.calculateSimilarity(msCodeClone.getFlowA().getPackageName(),
      // msCodeClone.getFlowB().getPackageName(), a.getReturnType(), b.getReturnType());
      //            msCodeClone.setSrvReturnTypeLiteralSimilarity(similarityValue); //
    }

    if (a.getMethodName().equals(b.getMethodName())) {
      //            same += 1.0;
      msCodeClone.setSrvMethodNameSimilarity(1.0); //
    }

    List<Parameter> aArguments = a.getParameterList();
    List<Parameter> bArguments = b.getParameterList();
    if (!aArguments.isEmpty() && !bArguments.isEmpty()) {
      double sameArguments = 0.0;
      for (int i = 0; i < aArguments.size() - 1; i++) {
        for (int j = i + 1; j < bArguments.size(); j++) {

          if (aArguments.get(i).getType() != null
              && aArguments.get(i).getType() != null
              && aArguments.get(i).getType().equals(bArguments.get(j).getType())) {
            sameArguments += 1.0;
          }
        }
      }
      double denominator = Math.max(aArguments.size(), bArguments.size());
      same += (sameArguments / denominator);
    }

    // Rico Commented out
    //        double argumentSimilarity =
    // this.entitySimilarityChecker.calculateArgumentsSimilarity(aArguments, bArguments);
    //        msCodeClone.setSrvArgumentsLiteralSimilarity(argumentSimilarity);

    //        double argumentSimilarity = 0;
    ////        List<MsArgument> aArguments = aCtrl.getMsArgumentList();
    ////        List<MsArgument> bArguments = bCtrl.getMsArgumentList();
    //        int maxNumberArguments = Math.max(aArguments.size(), bArguments.size());
    //         List<Integer> usedArguments = new ArrayList<>();
    //         for (int i = 0; i < aArguments.size(); i++) {
    //             for (int j = 0; j < bArguments.size(); j++) {
    //
    //                 if (aArguments.get(i).getReturnType() != null
    //                         && bArguments.get(j).getReturnType() != null
    //                         && !usedArguments.contains(j)
    //                         &&
    // aArguments.get(i).getReturnType().toLowerCase().equals(bArguments.get(j).getReturnType().toLowerCase())) {
    //                             usedArguments.add(j);
    ////                             same += 1.0;
    //                             argumentSimilarity += 1;
    //                     break;
    //                 }
    //             }
    //         }
    //
    //
    //
    // msCodeClone.setSrvArgumentsLiteralSimilarity((double)(argumentSimilarity/maxNumberArguments)); //

    msCodeClone.setSimilarityService(same);
  }

  private void compareRepository(CodeClone msCodeClone, Method a, Method b) {
    //    	System.err.println("REPO:  " + aMethod.getClassId() + "  " + aMethod.getMethodId() + " "
    // + aMethod.getMethodName());
    //        return compareService(aMethod, bMethod);

    //        private double repArgumentsSemanticSimilarity;

    if (a == null && b == null) {
      msCodeClone.setRepReturnTypeLiteralSimilarity(-1.0);
      msCodeClone.setRepOperationTypeSimilarity(-1.0);
      msCodeClone.setRepArgumentsLiteralSimilarity(-1.0);
    }

    if (a == null || b == null) {
      msCodeClone.setSimilarityRepository(0.0);
      return;
    }

    double same = 0.0;
    if (a.getReturnType() != null
        && b.getReturnType() != null
        && a.getReturnType().equalsIgnoreCase(b.getReturnType())) {
      same += 0.5;

      // Rico Commented OUt
      //            double similarityValue =
      // this.entitySimilarityChecker.calculateSimilarity(msCodeClone.getFlowA().getPackageName(),
      // msCodeClone.getFlowB().getPackageName(), a.getReturnType(), b.getReturnType());
      //            msCodeClone.setRepReturnTypeLiteralSimilarity(similarityValue); //
    }

    //        if (aMethod.getMethodName().equals(bMethod.getMethodName())){
    //            same += 1.0;
    //        }

    msCodeClone.setRepOperationTypeSimilarity(getRepositoryOperationSimilarity(a, b)); //

    List<Parameter> aArguments = a.getParameterList();
    List<Parameter> bArguments = b.getParameterList();
    if (!aArguments.isEmpty() && !bArguments.isEmpty()) {
      double sameArguments = 0.0;
      for (int i = 0; i < aArguments.size() - 1; i++) {
        for (int j = i + 1; j < bArguments.size(); j++) {

          if (aArguments.get(i).getType() != null
              && aArguments.get(i).getType() != null
              && aArguments.get(i).getType().equalsIgnoreCase(bArguments.get(j).getType())) {
            sameArguments += 1.0;
          }
        }
      }
      double denominator = Math.max(aArguments.size(), bArguments.size());
      same += (sameArguments / denominator);

      // Rico Commented out
      //            double argumentSimilarity =
      // this.entitySimilarityChecker.calculateArgumentsSimilarity(aArguments, bArguments);
      //            msCodeClone.setRepArgumentsLiteralSimilarity(argumentSimilarity);

      //            double argumentSimilarity = 0;
      ////          List<MsArgument> aArguments = aCtrl.getMsArgumentList();
      ////          List<MsArgument> bArguments = bCtrl.getMsArgumentList();
      //           int maxNumberArguments = Math.max(aArguments.size(), bArguments.size());
      //           List<Integer> usedArguments = new ArrayList<>();
      //           for (int i = 0; i < aArguments.size(); i++) {
      //               for (int j = 0; j < bArguments.size(); j++) {
      //
      //                   if (aArguments.get(i).getReturnType() != null
      //                           && bArguments.get(j).getReturnType() != null
      //                           && !usedArguments.contains(j)
      //                           &&
      // aArguments.get(i).getReturnType().toLowerCase().equals(bArguments.get(j).getReturnType().toLowerCase())) {
      //                               usedArguments.add(j);
      ////                               same += 1.0;
      //                               argumentSimilarity += 1;
      //                       break;
      //                   }
      //               }
      //           }
      //
      //
      //
      // msCodeClone.setSrvArgumentsLiteralSimilarity((double)(argumentSimilarity/maxNumberArguments)); //

    }
    msCodeClone.setSimilarityRepository(same);
  }

  private void compareRestCalls(CodeClone msCodeClone, List<RestCall> listA, List<RestCall> listB) {

    if ((listA == null || listA.isEmpty()) && (listB == null || listB.isEmpty())) {
      msCodeClone.setCalURLSimilarity(-1.0);
      msCodeClone.setCalReturnTypeLiteralSimilarity(-1.0);
      msCodeClone.setCalHttpMethodSimilarity(-1.0);
    }

    if ((listA == null || listA.isEmpty()) || (listB == null || listB.isEmpty())) {
      msCodeClone.setSimilarityRestCalls(0.0);
      return;
    }

    double denominator = Math.max(listA.size(), listB.size());
    double nominator = 0.0;

    double urlSimilarity = 0;
    double httpSimilarity = 0;
    double returnTypeSimilarity = 0;

    for (RestCall a : listA) {
      for (RestCall b : listB) {
        nominator += compareRestCall(msCodeClone, a, b);

        if (urlSimilarity < msCodeClone.getCalURLSimilarity()) {
          urlSimilarity = msCodeClone.getCalURLSimilarity();
        }

        if (httpSimilarity < msCodeClone.getCalHttpMethodSimilarity()) {
          httpSimilarity = msCodeClone.getCalHttpMethodSimilarity();
        }

        if (returnTypeSimilarity < msCodeClone.getCalReturnTypeLiteralSimilarity()) {
          returnTypeSimilarity = msCodeClone.getCalReturnTypeLiteralSimilarity();
        }

        msCodeClone.setCalURLSimilarity(0);
        msCodeClone.setCalReturnTypeLiteralSimilarity(0);
        msCodeClone.setCalHttpMethodSimilarity(0);
      }
    }
    //        System.err.println("denominator = " + denominator);

    msCodeClone.setCalURLSimilarity(urlSimilarity);
    msCodeClone.setCalReturnTypeLiteralSimilarity(returnTypeSimilarity);
    msCodeClone.setCalHttpMethodSimilarity(httpSimilarity);

    if (denominator == 0.0) {
      msCodeClone.setSimilarityRestCalls(0.0);
      return;
    }

    //        msCodeClone.setCalURLSimilarity(msCodeClone.getCalURLSimilarity() / denominator);
    //
    // msCodeClone.setCalReturnTypeLiteralSimilarity(msCodeClone.getCalReturnTypeLiteralSimilarity()
    // / denominator);
    //        msCodeClone.setCalHttpMethodSimilarity(msCodeClone.getCalHttpMethodSimilarity() /
    // denominator);

    //
    //        System.err.println("URL = " + msCodeClone.getCalURLSimilarity());
    //        System.err.println("URL = " + msCodeClone.getCalReturnTypeLiteralSimilarity());
    //        System.err.println("URL = " + msCodeClone.getCalHttpMethodSimilarity());
    //        System.err.println("Result = " + nominator / denominator);

    msCodeClone.setSimilarityRestCalls(nominator / denominator);
  }

  private double getRepositoryOperationSimilarity(Method aMethod, Method bMethod) {

    if (aMethod.getMethodName().equals(bMethod.getMethodName())
        || aMethod.getMethodName().toLowerCase().contains("update")
            && bMethod.getMethodName().toLowerCase().contains("save")
        || bMethod.getMethodName().toLowerCase().contains("update")
            && aMethod.getMethodName().toLowerCase().contains("save")) {
      return 1.0;
    }
    return 0.0;
  }

  private double compareRestCall(CodeClone msCodeClone, RestCall a, RestCall b) {

    //        private double calReturnTypeSemanticSimilarity = 0;

    //    	System.err.println(a.getApi() + " --- " + b.getApi());
    //    	System.err.println(a.getReturnType() + " --- " + b.getReturnType());
    //    	System.err.println(a.getHttpMethod() + " --- " + b.getHttpMethod());

    double similarity = 0.0;
    if (a.getApiEndpoint() != null
        && b.getApiEndpoint() != null
        && a.getApiEndpoint()
            .replaceAll("\\s", "")
            .toLowerCase()
            .equals(b.getApiEndpoint().replaceAll("\\s", "").toLowerCase())) {
      similarity += 1.0;
      //            msCodeClone.setCalURLSimilarity(msCodeClone.getCalURLSimilarity() + 1.0);
      msCodeClone.setCalURLSimilarity(1.0);
    }
    if (a.getReturnType() != null
        && b.getReturnType() != null
        && a.getReturnType().equalsIgnoreCase(b.getReturnType())) {
      similarity += 1.0;
      //            System.err.println(a.getReturnType() + " --- " + b.getReturnType());

      // Rico Commented out
      //            double similarityValue =
      // this.entitySimilarityChecker.calculateSimilarity(msCodeClone.getFlowA().getPackageName(),
      // msCodeClone.getFlowB().getPackageName(), a.getReturnType(), b.getReturnType());
      //            msCodeClone.setCalReturnTypeLiteralSimilarity(similarityValue);

      //
      // msCodeClone.setCalReturnTypeLiteralSimilarity(msCodeClone.getCalReturnTypeLiteralSimilarity() + similarityValue);
    }
    if (a.getHttpMethod() != null
        && b.getHttpMethod() != null
        && a.getHttpMethod().equals(b.getHttpMethod())) {
      similarity += 1.0;
      msCodeClone.setCalHttpMethodSimilarity(1.0);
      //            msCodeClone.setCalHttpMethodSimilarity(msCodeClone.getCalHttpMethodSimilarity()
      // + 1.0);
    }
    //        System.err.println(similarity + "");
    return similarity / 3.0;
  }
}
