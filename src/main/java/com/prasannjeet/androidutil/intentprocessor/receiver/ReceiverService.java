package com.prasannjeet.androidutil.intentprocessor.receiver;

import com.prasannjeet.androidutil.intentprocessor.receiver.method.ReceiverMethod;
import soot.Body;

public interface ReceiverService {

  /**
   * Checks if the method body receives Intent.
   *
   * @param body
   * @return True if it receives, false if it doesn't.
   */
  boolean doesBodyReceiveIntent(Body body);

  /**
   * Checks if the method body receives Intent, and saves it if yes.
   *
   * @param body
   */
  void processBody(Body body);

  /**
   * Method that returns the generated method that receives intents
   *
   * @return
   */
  ReceiverMethod getReceiverMethod();
}
