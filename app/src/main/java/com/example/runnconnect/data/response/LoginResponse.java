package com.example.runnconnect.data.response;

import com.example.runnconnect.data.model.Usuario;

public class LoginResponse { //lo que me responde el servidor
  private String token;
  private String expiration;
  private Usuario usuario;

  public LoginResponse() {}

  //get set


  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getExpiration() {
    return expiration;
  }

  public void setExpiration(String expiration) {
    this.expiration = expiration;
  }

  public Usuario getUsuario() {
    return usuario;
  }

  public void setUsuario(Usuario usuario) {
    this.usuario = usuario;
  }
}
