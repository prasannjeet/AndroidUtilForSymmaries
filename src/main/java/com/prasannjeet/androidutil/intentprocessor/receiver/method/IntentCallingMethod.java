package com.prasannjeet.androidutil.intentprocessor.receiver.method;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
// Assumed intent is the last parameter
public class IntentCallingMethod {

  List<String> acceptedIntentTypes; // Only last value. Such as "SEND".
  List<String> allParamTypes; // Don't include intent (last param).
  String fullClassPath;
  String methodName;
}
