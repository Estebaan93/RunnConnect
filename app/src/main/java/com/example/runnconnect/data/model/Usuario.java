//data/model/Usuario
package com.example.runnconnect.data.model;



public class Usuario { //represena el usuario logueado sea runner o orga
  private int idUsuario;
  private String nombre;
  private String email;
  private String telefono;
  private String tipoUsuario; //runner o organizador
  private String imgAvatar;

  //constructor vacio
  public Usuario() {}

  //get set
  public int getIdUsuario() {
    return idUsuario;
  }

  public void setIdUsuario(int idUsuario) {
    this.idUsuario = idUsuario;
  }

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

  public String getImgAvatar() {
    return imgAvatar;
  }

  public void setImgAvatar(String imgAvatar) {
    this.imgAvatar = imgAvatar;
  }


}
