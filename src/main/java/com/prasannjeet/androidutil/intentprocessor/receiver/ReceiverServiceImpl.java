package com.prasannjeet.androidutil.intentprocessor.receiver;

import static com.prasannjeet.androidutil.intentprocessor.receiver.method.ReceiverUtils.doesMethodAlreadyHasIntentParam;
import static com.prasannjeet.androidutil.intentprocessor.receiver.method.ReceiverUtils.doesMethodReceiveIntent;
import static com.prasannjeet.androidutil.intentprocessor.receiver.method.ReceiverUtils.injectIntentInsideReceivingMethod;
import static com.prasannjeet.androidutil.intentprocessor.receiver.method.ReceiverUtils.isMethodFromEligibleActivityReceivingIntent;

import com.prasannjeet.androidutil.intentprocessor.receiver.method.IntentCallingMethod;
import com.prasannjeet.androidutil.intentprocessor.receiver.method.ReceiverMethod;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;
import soot.Body;

@Data
public class ReceiverServiceImpl implements ReceiverService {

  Map<String, List<String>> intentFiltersByActivity;
  ReceiverMethod receiverMethod;
  List<Body> receiverBodies;

  public ReceiverServiceImpl(Map<String, List<String>> intentFiltersByActivity) {
    this.receiverMethod = new ReceiverMethod();
    this.receiverMethod.init(false); //TODO Let initializer decide if it is static or not
    this.receiverBodies = new ArrayList<>();
    this.intentFiltersByActivity = intentFiltersByActivity;
  }

  @Override
  public void processBody(Body body) {
    if (this.doesBodyReceiveIntent(body)) {
      this.receiverBodies.add(body);

      IntentCallingMethod icm =
          injectIntentInsideReceivingMethod(body, this.intentFiltersByActivity);
      this.receiverMethod.addIntentCallingMethod(icm);
    }
  }

  @Override
  public boolean doesBodyReceiveIntent(Body body) {
    return (doesMethodReceiveIntent(body)
        && !doesMethodAlreadyHasIntentParam(body)
        && isMethodFromEligibleActivityReceivingIntent(body, this.intentFiltersByActivity));
  }
}
