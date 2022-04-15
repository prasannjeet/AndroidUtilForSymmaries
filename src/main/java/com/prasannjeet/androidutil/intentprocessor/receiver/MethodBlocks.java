package com.prasannjeet.androidutil.intentprocessor.receiver;

import java.util.LinkedList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MethodBlocks {

  String initialDeclaration; //added
  String variableDeclaration; //added
  String variableAssignment; //added
  String intentActionStatement; //added
  List<String> ifBlocks = new LinkedList<>(); //alladded
  String returnAndCloseBraces;

  public void addIfBlock(String ifBlock) {
    this.ifBlocks.add(ifBlock);
  }

  public void processVariableDeclarationAndInit(List<VariableDeclaration> variables) {
    StringBuilder allVariableDeclaration = new StringBuilder();
    StringBuilder allVariableAssignment = new StringBuilder();
    for (VariableDeclaration v : variables) {
      allVariableDeclaration.append("  ").append(v.type).append(" ").append(v.varName).append(";\n");
      if (v.noArgsInstantiation) {
        allVariableAssignment.append(instantiateVariableNoArgsConstructor(v));
      }
    }
    this.variableAssignment = allVariableAssignment.toString();
    this.variableDeclaration = allVariableDeclaration.toString();
  }

  public void addClosingBlock(int labelCount) {
    this.returnAndCloseBraces = "label" + labelCount + ":return;\n"
        + "}\n";
  }

  public String toString() {
    return this.initialDeclaration + "\n" + this.variableDeclaration + "\n" + this.variableAssignment + "\n"
        + this.intentActionStatement + "\n"
        + String.join("\n", this.ifBlocks) + "\n" + this.returnAndCloseBraces + "\n";
  }

  private String instantiateVariableNoArgsConstructor(VariableDeclaration v) {
    String l1 = v.varName + "=new " + v.type + ";";
    String l2 = v.varName + "#" + v.type + "();";
    return "\n" + l1 + "\n" + l2 + "\n";
  }
}
