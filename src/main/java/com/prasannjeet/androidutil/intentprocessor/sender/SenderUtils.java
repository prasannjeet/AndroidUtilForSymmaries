package com.prasannjeet.androidutil.intentprocessor.sender;

import java.util.Map;
import java.util.Random;
import java.util.Set;
import soot.RefType;
import soot.Unit;
import soot.Value;
import soot.jimple.StringConstant;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JSpecialInvokeExpr;
import soot.jimple.internal.JVirtualInvokeExpr;
import soot.jimple.internal.JimpleLocal;

public class SenderUtils {

  /**
   * Will extract intents, if intents are initialized using methos of type:
   * 1. Intent intent = new Intent(Intent.ACTION_SENDTO);
   * 2. Intent intent = new Intent();
   * Note that it only registers the intent, but not the type.
   * @param unit
   * @param intentsByVar
   * @param sentIntentList
   */
  public static void extractIntentIfAvailable(Unit unit, Map<JimpleLocal, String> intentsByVar, Map<JimpleLocal, Boolean> sentIntentList) {
    // Collect all Intents in the program
    if (unit instanceof JAssignStmt) {
      JAssignStmt assignStmt = (JAssignStmt) unit;
      Value leftOp = assignStmt.getLeftOp();
      Value rightOp = assignStmt.getRightOp();
      if (leftOp instanceof JimpleLocal) {
        JimpleLocal leftOpLocal = (JimpleLocal) leftOp;
        if (leftOpLocal.getType() instanceof RefType) {
          RefType leftOpType = (RefType) leftOpLocal.getType();
          if (leftOpType.getClassName().equals("android.content.Intent")) {
            intentsByVar.put(leftOpLocal, null);
            sentIntentList.put(leftOpLocal, false);
          }
        }
      }

      //Optional Case Below
      if (rightOp instanceof JimpleLocal) {
        JimpleLocal rightOpLocal = (JimpleLocal) rightOp;
        if (rightOpLocal.getType() instanceof RefType) {
          RefType rightOpType = (RefType) rightOpLocal.getType();
          if (rightOpType.getClassName().equals("android.content.Intent")) {
            intentsByVar.put(rightOpLocal, null);
            sentIntentList.put(rightOpLocal, false);
          }
        }
      }
    }
  }

  /**
   * Will registers the type of Intent, if intents are initialized using methods of type:
   * intent.setAction(Intent.ACTION_SENDTO);
   * @param unit
   * @param intentsByVar
   */
  public static void extractIntentTypeUsingAction(Unit unit, Map<JimpleLocal, String> intentsByVar) {
    if (unit instanceof JInvokeStmt) {
      JInvokeStmt invokeStmt = (JInvokeStmt) unit;
      if (invokeStmt.getInvokeExpr() instanceof JVirtualInvokeExpr) {
        JVirtualInvokeExpr virtualInvokeExpr = (JVirtualInvokeExpr) invokeStmt.getInvokeExpr();
        if (virtualInvokeExpr.getMethod().getName().equals("setAction") && virtualInvokeExpr.getMethod().getDeclaringClass().getName().equals("android.content.Intent")) {
          if (virtualInvokeExpr.getArgCount() == 1) {
            String type = virtualInvokeExpr.getArg(0).toString();
            Value base = virtualInvokeExpr.getBase();
            if (base instanceof JimpleLocal) {
              JimpleLocal baseLocal = (JimpleLocal) base;
              intentsByVar.put(baseLocal, type);
            }
          }
        }
      }
    }
  }

  /**
   * Extracts the type of intent, if intents are initialized using methods of type:
   * Intent intent = new Intent(Intent.ACTION_SENDTO);
   * @param unit
   * @param intentsByVar
   */
  public static void extractIntentTypes(Unit unit, Map<JimpleLocal, String> intentsByVar) {
    if (unit instanceof JInvokeStmt) {
      JInvokeStmt invokeStmt = (JInvokeStmt) unit;
      if (invokeStmt.getInvokeExpr() instanceof JSpecialInvokeExpr) {
        JSpecialInvokeExpr specialInvokeExpr = (JSpecialInvokeExpr) invokeStmt.getInvokeExpr();
        if (specialInvokeExpr.getBase() instanceof JimpleLocal) {
          JimpleLocal base = (JimpleLocal) specialInvokeExpr.getBase();
          if (intentsByVar.containsKey(base) && specialInvokeExpr.getMethodRef().getName().equals("<init>")) {
            specialInvokeExpr.getArgs().forEach(arg -> {
              if (arg instanceof StringConstant) {
                StringConstant argString = (StringConstant) arg;
                intentsByVar.put(base, argString.toString());
              }
            });
          }
        }
      }
    }
  }

  /**
   * Creates the intermediate method that will send the intent to the receiver.
   * @param unit
   * @param allIntents
   * @param sentIntentList
   * @param allStartActivityMethods
   * @return
   */
  public static SenderMethod getMethForIntent(Unit unit, Map<JimpleLocal, String> allIntents, Map<JimpleLocal, Boolean> sentIntentList,
      Set<String> allStartActivityMethods) {
    if (unit instanceof JInvokeStmt) {
      JInvokeStmt invokeStmt = (JInvokeStmt) unit;
      String methodName = invokeStmt.getInvokeExpr().getMethodRef().getName();
      if (methodName.equals("startActivity")) {
        // This method is used to send the Intent to the Android system
        String declaringClass =
            invokeStmt.getInvokeExpr().getMethodRef().getDeclaringClass().getName();
        String returnType = invokeStmt.getInvokeExpr().getMethodRef().getReturnType().toString();
        Value value =
            invokeStmt.getInvokeExpr().getArgs().stream()
                .filter(sentIntentList::containsKey)
                .findFirst()
                .orElse(null);
        if (value instanceof JimpleLocal && value.getType().toString().equals("android.content.Intent")) {
          String methodNamePartial = declaringClass + "_" + methodName;
          String qualifiedMethodName = methodNamePartial + methodNamePartial.hashCode() + new Random().nextInt(1000);

          String fileName = qualifiedMethodName + ".meth";

          if (!allStartActivityMethods.contains(fileName)) {
            allStartActivityMethods.add(fileName);
          } else {
            return null;
          }

          JimpleLocal intentLocal = (JimpleLocal) value;
          sentIntentList.put(intentLocal, true);
          StringBuilder sb = new StringBuilder();
          sb.append(returnType)
              .append(" ")
              .append(declaringClass)
              .append(":")
              .append(methodName)
              .append("(android.content.Intent )")
              .append("\n");
          sb.append("{\n");
          sb.append("\t  ").append("com.intentpropagator.Capture $u1;\n");
          sb.append(allIntents.containsKey(value) && allIntents.get(value) != null ? ("\tintent#android.content.Intent("+ allIntents.get(value) +");\n") : "");
          sb.append("\t").append("$u1=new com.intentpropagator.Capture;\n");
          sb.append("\t").append("$u1.receiveIntent(intent);\n");
          sb.append("}");

          return new SenderMethod(fileName, qualifiedMethodName, sb.toString());
        }
      }
    }
    return null;
  }
}
