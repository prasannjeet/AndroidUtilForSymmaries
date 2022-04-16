package com.prasannjeet.androidutil.intentprocessor;

import static java.util.Objects.requireNonNullElse;
import static org.apache.commons.collections4.CollectionUtils.union;

import com.prasannjeet.androidutil.dto.AllIntentTypes;
import com.prasannjeet.androidutil.intentprocessor.receiver.ReceiverService;
import com.prasannjeet.androidutil.intentprocessor.receiver.ReceiverServiceImpl;
import com.prasannjeet.androidutil.intentprocessor.receiver.method.ReceiverMethod;
import com.prasannjeet.androidutil.intentprocessor.sender.SenderService;
import com.prasannjeet.androidutil.intentprocessor.sender.SenderServiceImpl;
import com.prasannjeet.androidutil.intentprocessor.sender.method.SenderMethod;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import soot.Body;
import soot.jimple.internal.JimpleLocal;

@Data
public class IntentServiceImpl implements IntentService {

  Map<String, List<String>> intentFiltersByActivity;
  SenderService senderService;
  ReceiverService receiverService;

  public IntentServiceImpl(Map<String, List<String>> intentFiltersByActivity) {
    this.intentFiltersByActivity = intentFiltersByActivity;
    this.senderService = new SenderServiceImpl();
    this.receiverService = new ReceiverServiceImpl(intentFiltersByActivity);
  }

  @Override
  public void processBody(Body body) {
    this.receiverService.processBody(body);
    this.senderService.processBody(body);
  }

  @Override
  public List<SenderMethod> getSenderMethods() {
    return this.senderService.getSenderMethods();
  }

  @Override
  public ReceiverMethod getReceiverMethod() {
    return this.receiverService.getReceiverMethod();
  }

  @Override
  public AllIntentTypes getAllIntentTypes() {
    Map<JimpleLocal, String> intentTypes = this.senderService.getIntentTypes();
    Map<JimpleLocal, Boolean> sentIntents = this.senderService.getSentIntents();
    Map<JimpleLocal, String> senderClasses = this.senderService.getIntentClassNames();

    AllIntentTypes allIntentTypes = new AllIntentTypes();

    Collection<JimpleLocal> jimpleLocals = union(intentTypes.keySet(), sentIntents.keySet());

    jimpleLocals.forEach(
        j -> {
          String varName = j.getName();
          Boolean isSent = requireNonNullElse(sentIntents.get(j), false);
          String intentType = requireNonNullElse(intentTypes.get(j), "NOT_FOUND");
          intentType = intentType.replace("\"", "");
          String sendingClassName = requireNonNullElse(senderClasses.get(j), "NOT_FOUND");
          allIntentTypes.addIntent(varName, isSent, intentType, sendingClassName);
        });

    return allIntentTypes;
  }

  @Override
  public void syncMethodsToIgnore(Set<String> methodsToIgnore) {
    Set<String> latestSet = this.senderService.getMethodsToIgnore();
    methodsToIgnore.addAll(latestSet);
  }
}
