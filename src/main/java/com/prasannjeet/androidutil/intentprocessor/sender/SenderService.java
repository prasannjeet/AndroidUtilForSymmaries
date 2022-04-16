package com.prasannjeet.androidutil.intentprocessor.sender;

import com.prasannjeet.androidutil.intentprocessor.sender.method.SenderMethod;
import java.util.List;
import java.util.Map;
import java.util.Set;
import soot.Body;
import soot.jimple.internal.JimpleLocal;

public interface SenderService {

  /**
   * Process the method body. Checks if the method body sends Intent.
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
   * Get the list of Intent and Types that the app sends.
   *
   * @return List of Intent Types
   */
  Map<JimpleLocal, String> getIntentTypes();

  /**
   * Get the list of Intents that the app sends.
   *
   * @return List of Intents that were sent
   */
  Map<JimpleLocal, Boolean> getSentIntents();

  /**
   * Get the classes that are responsible for sending Intents.
   *
   * @return List of classes that send Intents.
   */
  Map<JimpleLocal, String> getIntentClassNames();

  /**
   * When processing intent sending, we change the actual method `startActivity` to `startActivityA` or similar.
   * Due to this, this method is processed again in Jsymcompiler, that results in two methods of the same name.
   * Thus, we need to make a list of all the methods that were modified, so that we do not process thsi method
   * in Jsymcompiler.
   * @return List of methods to ignore.
   */
  Set<String> getMethodsToIgnore();
}
