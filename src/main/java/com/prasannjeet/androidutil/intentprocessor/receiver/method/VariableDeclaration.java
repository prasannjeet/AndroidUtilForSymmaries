package com.prasannjeet.androidutil.intentprocessor.receiver.method;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VariableDeclaration {
  String type;
  String varName;
  boolean noArgsInstantiation;
}
