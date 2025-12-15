//data/request/ActualizarPerfilRunnerRequest
package com.example.runnconnect.data.request;

public class ActualizarPerfilRunnerRequest { //lo que se recibe
  public String Nombre;
  public String Apellido;
  public String Telefono;
  public String FechaNacimiento;
  public String Genero;
  public int Dni;
  public String Localidad;
  public String Agrupacion;
  public String NombreContactoEmergencia;
  public String TelefonoEmergencia;

  public ActualizarPerfilRunnerRequest(String nombre, String apellido, String telefono, String fechaNacimiento, String genero, int dni, String localidad, String agrupacion, String nombreContactoEmergencia, String telefonoEmergencia) {
    Nombre = nombre;
    Apellido = apellido;
    Telefono = telefono;
    FechaNacimiento = fechaNacimiento;
    Genero = genero;
    Dni = dni;
    Localidad = localidad;
    Agrupacion = agrupacion;
    NombreContactoEmergencia = nombreContactoEmergencia;
    TelefonoEmergencia = telefonoEmergencia;
  }





}
