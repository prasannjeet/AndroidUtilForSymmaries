package com.prasannjeet.androidutil.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
class IntentType {

  String variableName;
  Boolean isIntentSent;
  String intentType;
  String sendingClassName;
}
