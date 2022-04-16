package com.prasannjeet.androidutil.intentprocessor.receiver.method;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import soot.Body;
import soot.Local;
import soot.RefType;
import soot.SootClass;
import soot.Type;
import soot.Unit;
import soot.jimple.IdentityStmt;
import soot.jimple.JimpleBody;
import soot.jimple.ParameterRef;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.JVirtualInvokeExpr;
import soot.jimple.internal.JimpleLocal;

public class ReceiverUtils {

  public static boolean doesMethodAlreadyHasIntentParam(Body body) {
    boolean flag = false;

    for (Local jl : body.getParameterLocals()) {
      if (jl.getType().toString().contains("android.content.Intent")) {
        flag = true;
      }
    }
    return flag;
  }

  public static boolean isMethodFromEligibleActivityReceivingIntent(
      Body body, Map<String, List<String>> intentsPerActivity) {
    Set<String> allSuperClasses =
        new HashSet<>(getAllSuperClasses(body.getMethod().getDeclaringClass()));
    allSuperClasses.add(body.getMethod().getDeclaringClass().getName());

    Set<String> allIntentClasses = intentsPerActivity.keySet();

    Collection<String> commonItems =
        CollectionUtils.intersection(allSuperClasses, allIntentClasses);
    return commonItems.size() > 0;
  }

  public static List<String> getAllSuperClasses(SootClass clazz) {
    List<String> superClasses = new ArrayList<>();
    SootClass superClass = clazz.getSuperclass();
    while (superClass != null) {
      superClasses.add(superClass.getName());
      try {
        superClass = superClass.getSuperclass();
      } catch (RuntimeException e) {
        superClass = null;
      }
    }
    return superClasses;
  }

  public static IntentCallingMethod createIntermediateMethod(
      Body body, Map<String, List<String>> intentsByActivity) {
    String methodName = body.getMethod().getName();
    List<String> allParamTypes =
        body.getMethod().getParameterTypes().stream()
            .map(Type::toString)
            .collect(Collectors.toList());
    String thisClassName = body.getMethod().getDeclaringClass().getName();

    Set<String> allSuperClasses =
        getAllSuperClasses(body.getMethod().getDeclaringClass()).stream()
            .distinct()
            .collect(Collectors.toSet());
    allSuperClasses.add(body.getMethod().getDeclaringClass().getName());

    String thisActivity = null;
    for (Entry<String, List<String>> intentByActivity : intentsByActivity.entrySet()) {
      for (String theClass : allSuperClasses) {
        if (theClass.contains(intentByActivity.getKey())) {
          thisActivity = intentByActivity.getKey();
          break;
        }
      }
      if (thisActivity != null) {
        break;
      }
    }

    List<String> intentsForThisActivity = intentsByActivity.get(thisActivity);
    intentsForThisActivity =
        intentsForThisActivity.stream()
            .map(i -> i.replace("android.intent.action.", ""))
            .collect(Collectors.toList());

    // Remove last parameter (intent) from the list
    allParamTypes.remove(allParamTypes.size() - 1);

    return new IntentCallingMethod(
        intentsForThisActivity, allParamTypes, thisClassName, methodName);
  }

  public static IntentCallingMethod injectIntentInsideReceivingMethod(
      Body body, Map<String, List<String>> intentsByActivity) {

    int newParamIndex = body.getMethod().getParameterCount();

    List<Type> types = body.getMethod().getParameterTypes();
    List<Type> moreParams = new ArrayList<>(types);
    moreParams.add(RefType.v("android.content.Intent"));
    body.getMethod().setParameterTypes(moreParams);

    Iterator<Unit> it = body.getUnits().stream().iterator();
    Unit theUnit = null;
    while (it.hasNext()) {
      Unit u = it.next();
      if (u instanceof IdentityStmt) {
        IdentityStmt is = ((IdentityStmt) u);
        if (is.getRightOp() instanceof ParameterRef) {
          theUnit = u;
        }
      }
    }

    JimpleLocal l = new JimpleLocal("#u991", RefType.v("android.content.Intent"));
    ParameterRef r = new ParameterRef(RefType.v("android.content.Intent"), newParamIndex);
    JIdentityStmt istmt = new JIdentityStmt(l, r);

    if (theUnit != null) {
      body.getUnits().insertAfter(istmt, theUnit);
    } else {
      // That means currently the method does not take any parameter.
      // So insert the assignment statement just after the first statement
      theUnit = body.getUnits().stream().iterator().next();
      body.getUnits().insertAfter(istmt, theUnit);
    }

    Unit uToRemove = null;
    JAssignStmt njas = null;
    for (Unit u : body.getUnits()) {
      if (u instanceof JAssignStmt) {
        JAssignStmt jas = (JAssignStmt) u;
        if (jas.getLeftOp().getType().toString().contains("android.content.Intent")) {
          uToRemove = u;
          njas = new JAssignStmt(((JAssignStmt) u).getLeftOp(), l);
          break;
        }
      }
    }
    if (njas != null) {
      body.getUnits().insertAfter(njas, uToRemove);
      body.getUnits().remove(uToRemove);
    }

    // Now creating intermediate method
    return createIntermediateMethod(body, intentsByActivity);
  }

  /**
   * An implicit intent is usually received like so: Intent incomingIntent = getIntent() This method
   * checks if the method body receives this implicit intent.
   *
   * @param body
   * @return
   */
  public static boolean doesMethodReceiveIntent(Body body) {
    for (Unit u : body.getUnits()) {
      if (u instanceof JAssignStmt) {
        JAssignStmt statement = (JAssignStmt) u;
        String returnType = statement.rightBox.getValue().getType().toString();
        if (statement.rightBox.getValue() instanceof JVirtualInvokeExpr) {
          JVirtualInvokeExpr jvie = (JVirtualInvokeExpr) statement.rightBox.getValue();
          String methodName = jvie.getMethodRef().getName();
          if (methodName.equals("getIntent") && returnType.equals("android.content.Intent")) {
            return true;
          }
        }
      }
    }
    return false;
  }
}
