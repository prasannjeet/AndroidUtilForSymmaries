package com.prasannjeet.androidutil.dto;

import static com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT;
import static com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AllIntentTypes {
  List<IntentType> allIntents;

  public AllIntentTypes() {
    this.allIntents = new ArrayList<>();
  }

  public void addIntent(String a1, Boolean a2, String a3, String a4) {
    this.allIntents.add(new IntentType(a1, a2, a3, a4));
  }

  public String toJson() {
    try {
      return objectMapper()
          .writerWithDefaultPrettyPrinter()
          .writeValueAsString(this);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "";
  }

  private ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
    mapper.configure(ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
    mapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.configure(FAIL_ON_EMPTY_BEANS, false);
    return mapper;
  }
}

