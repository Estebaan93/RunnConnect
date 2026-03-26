package com.example.runnconnect.data.request;

import com.google.gson.annotations.SerializedName;

public class RestablecerPasswordRequest {
  //@SerializedName("token")
  private String token;

  //@SerializedName("nuevaPassword")
  private  String passwordNueva;
  private String confirmarPassword;

  public RestablecerPasswordRequest(String token, String passwordNueva, String confirmarPassword) {
    this.token = token;
    this.passwordNueva = passwordNueva;
    this.confirmarPassword= confirmarPassword;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getPasswordNueva() {
    return passwordNueva;
  }

  public void setPasswordNueva(String passwordNueva) {
    this.passwordNueva = passwordNueva;
  }

  public String getConfirmarPassword() {
    return confirmarPassword;
  }

  public void setConfirmarPassword(String confirmarPassword) {
    this.confirmarPassword = confirmarPassword;
  }
}
