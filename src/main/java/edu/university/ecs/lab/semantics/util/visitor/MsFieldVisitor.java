package edu.university.ecs.lab.semantics.util.visitor;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;

import edu.university.ecs.lab.semantics.entity.graph.MsField;
import edu.university.ecs.lab.semantics.entity.graph.MsId;
import edu.university.ecs.lab.semantics.util.MsCache;

/**
 * Parse field information from a FieldDeclaration and generate MsField object that will be saved to
 * cache
 */
public class MsFieldVisitor {

  /**
   * Parses field information and creates a MsField object representing the field
   *
   * @param n the FieldDeclaration that will be parsed
   * @param path the path to the current file
   * @param msId the msId of the current file
   */
  public static void visitFieldDeclaration(FieldDeclaration n, String path, MsId msId) {
    MsField msField = new MsField();
    if (n.getVariables().size() > 0) {
      VariableDeclarator vd = n.getVariables().get(0);
      String variableName = vd.getNameAsString();
      if (variableName.toLowerCase().contains("service")
          || variableName.toLowerCase().contains("repository")) {
        msField.setFieldVariable(vd.getNameAsString());
        if (vd.getType() != null) {
          msField.setFieldClass(vd.getTypeAsString());
          msField.setParentMethod(MsParentVisitor.getMsParentMethod(n));
          msField.setLine(n.getBegin().get().line);
          msField.setMsId(msId);
          MsCache.addMsField(msField);
        }
      }
    }
  }
}
