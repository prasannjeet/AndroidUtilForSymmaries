package com.prasannjeet.androidutil.intentprocessor;

import com.prasannjeet.androidutil.dto.AllIntentTypes;
import com.prasannjeet.androidutil.intentprocessor.receiver.method.ReceiverMethod;
import com.prasannjeet.androidutil.intentprocessor.sender.method.SenderMethod;
import java.util.List;
import java.util.Set;
import soot.Body;

public interface IntentService {

  /**
   * Method that takes in a Soot Method Body and processes it by doing the following: 1. Extract
   * intents if available 2. Create intent sender dummy method if applicable 3. Create intent
   * receiver dummy method if applicable 4. Modify methods to conform to intent sender and receiver
   *
   * <p>Method checks if the body sends or receives intent. If yes, the method is saved in memory
   * for further processing.
   *
   * @param body
   */
  void processBody(Body body);

  /**
   * Method that returns the list of generated methods that sends intents
   *
   * @return
   */
  List<SenderMethod> getSenderMethods();

  /**
   * Method that returns the generated method that receives intents
   *
   * @return
   */
  ReceiverMethod getReceiverMethod();

  /**
   * Method that returns the list of generated Intents and their types in the app.
   *
   * @return
   */
  AllIntentTypes getAllIntentTypes();

  /**
   * Takes in the list, and makes sure it is same as the current list. Read more about this in
   * SenderService.getMethodsToIgnore
   * @param methodsToIgnore
   */
  void syncMethodsToIgnore(Set<String> methodsToIgnore);
}
