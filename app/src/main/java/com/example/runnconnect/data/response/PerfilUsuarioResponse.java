package com.example.runnconnect.data.response;

public class PerfilUsuarioResponse { //lo que se recibe
  //comunes
  private int idUsuario;
  private String nombre;
  private String email;
  private String telefono;
  private String tipoUsuario; // "runner" o "organizador"
  private String imgAvatar;

  //runner
  private String apellido;
  private String fechaNacimiento;
  private String genero;
  private Integer dni;
  private String localidad;
  private String agrupacion;
  private String nombreContactoEmergencia;
  private String telefonoEmergencia;

  //organizador
  private String razonSocial;
  private String nombreComercial;
  private String cuit;
  private String direccionLegal;

  //get set obligatoiros
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


  //runner get set
  public String getApellido() {
    return apellido;
  }

  public void setApellido(String apellido) {
    this.apellido = apellido;
  }

  public String getFechaNacimiento() {
    return fechaNacimiento;
  }

  public void setFechaNacimiento(String fechaNacimiento) {
    this.fechaNacimiento = fechaNacimiento;
  }

  public String getGenero() {
    return genero;
  }

  public void setGenero(String genero) {
    this.genero = genero;
  }

  public Integer getDni() {
    return dni;
  }

  public void setDni(Integer dni) {
    this.dni = dni;
  }

  public String getLocalidad() {
    return localidad;
  }

  public void setLocalidad(String localidad) {
    this.localidad = localidad;
  }

  public String getAgrupacion() {
    return agrupacion;
  }

  public void setAgrupacion(String agrupacion) {
    this.agrupacion = agrupacion;
  }

  public String getNombreContactoEmergencia() {
    return nombreContactoEmergencia;
  }

  public void setNombreContactoEmergencia(String nombreContactoEmergencia) {
    this.nombreContactoEmergencia = nombreContactoEmergencia;
  }

  public String getTelefonoEmergencia() {
    return telefonoEmergencia;
  }

  public void setTelefonoEmergencia(String telefonoEmergencia) {
    this.telefonoEmergencia = telefonoEmergencia;
  }


  //organizador get set
  public String getRazonSocial() {
    return razonSocial;
  }

  public void setRazonSocial(String razonSocial) {
    this.razonSocial = razonSocial;
  }

  public String getNombreComercial() {
    return nombreComercial;
  }

  public void setNombreComercial(String nombreComercial) {
    this.nombreComercial = nombreComercial;
  }

  public String getCuit() {
    return cuit;
  }

  public void setCuit(String cuit) {
    this.cuit = cuit;
  }

  public String getDireccionLegal() {
    return direccionLegal;
  }

  public void setDireccionLegal(String direccionLegal) {
    this.direccionLegal = direccionLegal;
  }



}
