package com.prasannjeet.androidutil.intentprocessor.sender;

import static com.prasannjeet.androidutil.intentprocessor.sender.method.SenderUtils.extractIntentIfAvailable;
import static com.prasannjeet.androidutil.intentprocessor.sender.method.SenderUtils.extractIntentTypeUsingAction;
import static com.prasannjeet.androidutil.intentprocessor.sender.method.SenderUtils.extractIntentTypes;
import static com.prasannjeet.androidutil.intentprocessor.sender.method.SenderUtils.getMethForIntent;

import com.prasannjeet.androidutil.intentprocessor.sender.method.SenderMethod;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import soot.Body;
import soot.Unit;
import soot.jimple.internal.JimpleLocal;

@Data
public class SenderServiceImpl implements SenderService {
  Map<JimpleLocal, String> intentTypes;
  Map<JimpleLocal, Boolean> sentIntents;
  Map<JimpleLocal, String> intentClassNames;
  Set<String> allStartActivityMethods;
  List<SenderMethod> senderMethods;
  Set<String> methodsToIgnore;
  int counter;

  public SenderServiceImpl() {
    this.intentTypes = new HashMap<>();
    this.sentIntents = new HashMap<>();
    this.intentClassNames = new HashMap<>();
    this.allStartActivityMethods = new HashSet<>();
    this.senderMethods = new ArrayList<>();
    this.methodsToIgnore = new HashSet<>();
    this.counter = 0;
  }

  @Override
  public void processBody(Body body) {
    for (Unit unit : body.getUnits()) {
      extractIntentIfAvailable(unit, this.intentTypes, this.sentIntents);
      extractIntentTypes(unit, this.intentTypes);
      extractIntentTypeUsingAction(unit, this.intentTypes);

      SenderMethod sm =
          getMethForIntent(
              unit,
              this.intentTypes,
              this.sentIntents,
              this.allStartActivityMethods,
              this.intentClassNames,
              this.methodsToIgnore,
              this.senderMethods.size() + 1);
      if (sm != null) {
        this.senderMethods.add(sm);
      }
    }
  }
}
