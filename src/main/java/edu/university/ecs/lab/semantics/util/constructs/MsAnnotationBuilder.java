package edu.university.ecs.lab.semantics.util.constructs;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;

import edu.university.ecs.lab.semantics.entity.graph.MsAnnotation;

import java.util.ArrayList;
import java.util.List;

public class MsAnnotationBuilder {
  public static List<MsAnnotation> buildAnnotations(NodeList<AnnotationExpr> annotations) {
    List<MsAnnotation> msAnnotations = new ArrayList<>();
    for (AnnotationExpr an : annotations) {
      msAnnotations.add(buildAnnotation(an));
    }
    return msAnnotations;
  }

  private static MsAnnotation buildAnnotation(AnnotationExpr annotationExpr) {
    MsAnnotation msAnnotation = new MsAnnotation();
    msAnnotation.setAnnotationName(annotationExpr.getNameAsString());
    List<Node> childNodes = annotationExpr.getChildNodes();
    for (Node node : childNodes) {
      if (node instanceof MemberValuePair) {
        MemberValuePair memberValuePair = (MemberValuePair) node;
        msAnnotation.setKey(memberValuePair.getNameAsString());
        msAnnotation.setValue(memberValuePair.getValue().toString());
        msAnnotation.setHttpAnnotation(true);
      }
    }
    return msAnnotation;
  }
}
