package com.example.runnconnect.data.request;

public class RegistroRequest { //para crear cuente
  private String nombre;
  private String email;
  private String password;
  private String telefono;
  private String tipoUsuario; // "runner" o "organizador"

  //contructor


  public RegistroRequest(String nombre, String email, String password, String telefono, String tipoUsuario) {
    this.nombre = nombre;
    this.email = email;
    this.password = password;
    this.telefono = telefono;
    this.tipoUsuario = tipoUsuario;
  }


  //get set


  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getTelefono() {
    return telefono;
  }

  public void setTelefono(String telefono) {
    this.telefono = telefono;
  }

  public String getTipoUsuario() {
    return tipoUsuario;
  }

  public void setTipoUsuario(String tipoUsuario) {
    this.tipoUsuario = tipoUsuario;
  }
}
