package com.prasannjeet.androidutil.intentprocessor.receiver;

import static java.lang.Math.abs;

import java.util.LinkedList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
//Assumed just one parameter (intent), hardcoded to be named as $u0
public class ReceiverMethod {

  static final String FULL_CLASS_NAME = "com.intentpropagator.Capture";
  static final String METHOD_NAME = "receiveIntent";

  int varCount = 0;
  int labelCount = 0;
  boolean isClosed = false;

  boolean isStatic;
  List<VariableDeclaration> variables;
  List<IntentCallingMethod> intentCallingMethods;
  String boolConditionVariable;
  String intentVariable;
  String intentActionVariable;

  MethodBlocks methodBlocks;

  private ReceiverMethod(boolean isStatic, List<IntentCallingMethod> intentCallingMethods) {
    this.variables = new LinkedList<>();
    this.isStatic = isStatic;
    this.intentVariable = createVariableName();
    this.methodBlocks = new MethodBlocks();
    this.methodBlocks.setInitialDeclaration((isStatic ? "static " : "") +
        "void "+ FULL_CLASS_NAME +":" + METHOD_NAME + "(android.content.Intent " + this.intentVariable + ")\n"
        + "{\n");
    this.intentActionVariable = addVariable("java.lang.String", false);
    this.methodBlocks.setIntentActionStatement(intentActionVariable + " = " + this.intentVariable + ".getAction();");
    this.boolConditionVariable = addVariable("boolean", false);
    for (IntentCallingMethod icm : intentCallingMethods) {
      processIntentCallingMethod(icm);
    }
    this.methodBlocks.processVariableDeclarationAndInit(this.variables);
    this.methodBlocks.addClosingBlock(labelCount);
  }

  public void init(boolean isStatic) {
    this.variables = new LinkedList<>();
    this.isStatic = isStatic;
    this.intentVariable = createVariableName();
    this.methodBlocks = new MethodBlocks();
    this.methodBlocks.setInitialDeclaration((isStatic ? "static " : "") +
        "void "+ FULL_CLASS_NAME +":" + METHOD_NAME + "(android.content.Intent " + this.intentVariable + ")\n"
        + "{\n");
    this.intentActionVariable = addVariable("java.lang.String", false);
    this.methodBlocks.setIntentActionStatement(intentActionVariable + " = " + this.intentVariable + ".getAction();\n");
    this.boolConditionVariable = addVariable("boolean", false);
  }

  public String getFileNameWithoutExtension() {
    String fullMethodName = FULL_CLASS_NAME + "_" + METHOD_NAME;
    return fullMethodName + abs(fullMethodName.hashCode());
  }

  public void addIntentCallingMethod(IntentCallingMethod icm) {
    processIntentCallingMethod(icm);
  }

  public String toString() {

    if (!isClosed()) {
      this.finish();
      this.setClosed(true);
    }
    return methodBlocks.toString();
  }

  private void finish() {
    this.methodBlocks.processVariableDeclarationAndInit(this.variables);
    this.methodBlocks.addClosingBlock(labelCount);
  }

  //Manually ensure the varName is not reused. No checks done
  //Returns the variable Name
  private String addVariable(String type, String varName, boolean noArgsInstantiation) {
    VariableDeclaration v = new VariableDeclaration(type, varName, noArgsInstantiation);
    this.variables.add(v);
    return varName;
  }

  //Returns the variable Name
  private String addVariable(String type, boolean noArgsInstantiation) {
    String varName = createVariableName();
    return addVariable(type, varName, noArgsInstantiation);
  }

  private void processIntentCallingMethod(IntentCallingMethod icm) {
    String methodClassName = addVariable(icm.getFullClassPath(), true);
    List<String> methodArgs = new LinkedList<>();
    for (String param : icm.getAllParamTypes()) {
      methodArgs.add(addVariable(param, false));
    }
    for (String acceptedIntent : icm.getAcceptedIntentTypes()) {
      this.methodBlocks.addIfBlock(createOneIfBlock(acceptedIntent, methodClassName, icm.getMethodName(), methodArgs));
    }
  }

  private String createOneIfBlock(String intentType, String methodClassName, String methodName, List<String> methodArgs) {
    String l1 = "label"+ this.labelCount + ":" + this.boolConditionVariable + " = " + this.intentActionVariable + ".equals((java.lang.Object)\"android.intent.action." + intentType + "\");";
    String l2 = "if (" + this.boolConditionVariable + "==0) goto label"+ ++this.labelCount +";";
    String l3 = methodClassName + "." + methodName + "(" + String.join(", ", methodArgs) + "," + this.intentVariable + ");";
    return l1 + "\n" + l2 + "\n" + l3 + "\n";
  }

  private String createVariableName() {
    return "$u" + varCount++;
  }


}

