package com.prasannjeet.androidutil.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class manifest {
  @JsonProperty("uses-sdk")
  public usessdk usessdk;
  @JsonProperty("uses-permission")
  @JacksonXmlElementWrapper(useWrapping = false)
  public List<usespermission> usespermission;
  @JacksonXmlElementWrapper(useWrapping = false)
  public List<permission> permission;
  @JsonProperty("application")
  public Application application;

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class usessdk {
    public int minSdkVersion;
    public int targetSdkVersion;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class usespermission {
    public String name;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class permission {
    public String name;
    public String protectionLevel;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class _package {
    public String name;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Action {
    private String name;

    public String getName() {
      return name;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class data {
    public String scheme;
    public String mimeType;
    public String host;
    public String path;
    public String pathPrefix;
    public String pathPattern;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class intent {
    public Action action;
    public data data;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class metadata {
    public String name;
    public String value;
    public String resource;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class category {
    public String name;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class IntentFilter {
    @JsonProperty("action")
    @JacksonXmlElementWrapper(useWrapping = false)
    public List<Action> actions;
    @JacksonXmlElementWrapper(useWrapping = false)
    public List<category> category;
    @JacksonXmlElementWrapper(useWrapping = false)
    public List<data> data;

    public List<Action> getActions() {
      if (actions == null) {
        return List.of();
      }

      List<Action> list = new ArrayList<>();
      for (Action action : actions) {
        if (action != null) {
          list.add(action);
        }
      }
      return list;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Activity {
    public String name;
    @JsonProperty("intent-filter")
    @JacksonXmlElementWrapper(useWrapping = false)
    public List<IntentFilter> intentFilters;

    public List<IntentFilter> getIntentFilters() {
      if (intentFilters == null) {
        return List.of();
      }

      List<IntentFilter> list = new ArrayList<>();
      for (IntentFilter i : intentFilters) {
        if (i != null) {
          list.add(i);
        }
      }
      return list;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Application {
    @JsonProperty("meta-data")
    @JacksonXmlElementWrapper(useWrapping = false)
    public List<metadata> metadata;
    @JsonProperty("activity")
    @JacksonXmlElementWrapper(useWrapping = false)
    public List<Activity> activities;

    public List<Activity> getActivities() {
      if (activities == null) {
        return List.of();
      }

      List<Activity> list = new ArrayList<>();
      for (Activity a : activities) {
        if (a != null) {
          list.add(a);
        }
      }
      return list;
    }
  }

}


