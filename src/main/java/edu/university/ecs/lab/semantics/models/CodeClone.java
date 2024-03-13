package edu.university.ecs.lab.semantics.models;

import lombok.Data;

import java.util.Objects;

@Data
public class CodeClone {

  private Flow flowA;
  private Flow flowB;

  private double similarityController = 0;
  private double similarityService = 0;
  private double similarityRepository = 0;
  private double similarityRestCalls = 0;
  private double globalSimilarity = 0;
  private boolean typeA;
  private boolean typeB;
  private boolean typeC;

  private double ctrMethodNameSimilarity = 0;
  private double ctrArgumentsLiteralSimilarity = 0;
  //  private double ctrArgumentsSemanticSimilarity = 0;
  private double ctrReturnTypeLiteralSimilarity = 0;
  //  private double ctrReturnTypeSemanticSimilarity = 0;
  private double ctrHttpMethodSimilarity = 0;

  private double srvMethodNameSimilarity = 0;
  private double srvArgumentsLiteralSimilarity = 0;
  //    private double srvArgumentsSemanticSimilarity = 0;
  private double srvReturnTypeLiteralSimilarity = 0;
  //    private double srvReturnTypeSemanticSimilarity = 0;

  private double repOperationTypeSimilarity = 0;
  private double repArgumentsLiteralSimilarity = 0;
  //    private double repArgumentsSemanticSimilarity = 0;
  private double repReturnTypeLiteralSimilarity = 0;
  //    private double repReturnTypeSemanticSimilarity = 0;

  private double calHttpMethodSimilarity = 0;
  private double calURLSimilarity = 0;
  private double calArgumentsLiteralSimilarity = 0;
  //    private double calArgumentsSemanticSimilarity = 0;
  private double calReturnTypeLiteralSimilarity = 0;

  //    private double calReturnTypeSemanticSimilarity = 0;

  @Override
  public boolean equals(Object o) {

    // If the object is compared with itself then return true
    if (o == this) {
      return true;
    }

    /* Check if o is an instance of Complex or not
    "null instanceof [type]" also returns false */
    if (!(o instanceof CodeClone)) {
      return false;
    }

    // typecast o to Complex so that we can compare data members
    CodeClone c = (CodeClone) o;

    // Compare the data members and return accordingly
    return Objects.equals(c, this);
  }

  @Override
  public String toString() {
    return flowA.toString() + " | " + flowB.toString() + " : " + globalSimilarity;
  }
}
