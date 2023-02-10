package edu.university.ecs.lab.semantics.checkers.controller;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;

import edu.university.ecs.lab.semantics.entity.inconsistencies.MsDefectInstance;

import java.util.concurrent.atomic.AtomicBoolean;

public class RequestHeaderChecker {

    public static void check(MethodDeclaration n){
        NodeList<Parameter> parameters = n.getParameters();
        AtomicBoolean abContainsReqHeader = new AtomicBoolean(false);
        parameters.forEach(e -> {
            NodeList<AnnotationExpr> annotationExprs = e.getAnnotations();
            if (annotationExprs != null) {
                annotationExprs.forEach(an -> {
                    // System.out.println(an);
                    if (an.toString().equals("@RequestHeader")) {
                        abContainsReqHeader.set(true);
                    }
                });
            }
        });
        boolean reqHeader = abContainsReqHeader.get();
        if (!reqHeader) {
            String str = n.toString();
            //line
            MsDefectInstance msDefectInstance = new MsDefectInstance();
            msDefectInstance.setCode(n.toString());

//            msDefectInstance.setMsPackage();
//            msDefectInstance.setMsClass();
//            msDefectInstance.setMsMethod();
//            msDefectInstance.setVariableType();
//            msDefectInstance.setVariableName();
//            msDefectInstance.setLineNumber();
        }
    }

}
