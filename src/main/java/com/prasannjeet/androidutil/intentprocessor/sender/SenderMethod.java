package com.prasannjeet.androidutil.intentprocessor.sender;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SenderMethod {
  String fileName;
  String qualifiedMethodName;
  String methodBody;
}
