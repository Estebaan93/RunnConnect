package com.example.runnconnect.data.request;

import com.google.gson.annotations.SerializedName;

public class ReactivarCuentaRequest {
  @SerializedName("token")
  private String token;

  public ReactivarCuentaRequest(String token) {
    this.token = token;
  }

  // Getter y Setter
  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }


}
