package com.example.runnconnect.data.model;

import org.json.JSONObject;

public class ClimaInfo {
  private final int min;
  private final int max;

  public ClimaInfo(int min, int max) {
    this.min = min;
    this.max = max;
  }

  public int getMin() {
    return min;
  }

  public int getMax() {
    return max;
  }

  public String getTextoTemperatura() {
    return min + "° / " + max + "°";
  }

  public static ClimaInfo fromJson(String rawJson) {
    if (rawJson == null || rawJson.trim().isEmpty()) {
      return new ClimaInfo(0, 0);
    }

    String trimmed = rawJson.trim();
    if (trimmed.startsWith("{")) {
      try {
        JSONObject obj = new JSONObject(trimmed);
        int min = obj.optInt("min", 0);
        int max = obj.optInt("max", 0);
        return new ClimaInfo(min, max);
      } catch (Exception e) {
        return new ClimaInfo(0, 0);
      }
    }

    return new ClimaInfo(0, 0);
  }
}
