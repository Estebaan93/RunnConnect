package com.example.runnconnect.data.request;

public class CambiarPasswordRequest {
  public String PasswordActual;
  public String NuevaPassword;
  public String ConfirmarPassword;

  public CambiarPasswordRequest(String passwordActual, String nuevaPassword, String confirmarPassword) {
    this.PasswordActual = passwordActual;
    this.NuevaPassword = nuevaPassword;
    this.ConfirmarPassword = confirmarPassword;
  }

}
