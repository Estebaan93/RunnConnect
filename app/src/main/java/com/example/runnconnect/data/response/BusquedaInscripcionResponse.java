package com.example.runnconnect.data.response;

import com.google.gson.annotations.SerializedName;

public class BusquedaInscripcionResponse {
  private int idInscripcion;
  private String fechaInscripcion;
  private String estadoPago; // "pagado", "pendiente", "cancelado"
  private String estadoEvento;
  private String talleRemera;
  private int idEvento;
  private String nombreEvento;
  private String nombreCategoria;

  @SerializedName("runner")
  private RunnerSimpleInfo runner;

  // Getters
  public int getIdInscripcion() { return idInscripcion; }
  public String getEstadoPago() { return estadoPago; }
  public String getNombreEvento() { return nombreEvento; }
  public String getEstadoEvento() {return estadoEvento; }
  public String getTalleRemera() { return talleRemera; }
  public String getNombreCategoria() { return nombreCategoria; }

  public RunnerSimpleInfo getRunner() { return runner; }
  public static class RunnerSimpleInfo {
    private String nombre;
    private String apellido;
    private String dni;
    private String email;
    private String telefono;
    private String genero;
    private String localidad;
    private String nombreContactoEmergencia;
    private String telefonoEmergencia;

    public String getNombreCompleto() { return nombre + " " + (apellido != null ? apellido : ""); }
    public String getDni() { return dni; }
    public String getEmail() { return email; }
    public String getTelefono() { return telefono; }
    public String getGenero() { return genero; }
    public String getLocalidad() { return localidad; }
    public String getNombreContactoEmergencia() { return nombreContactoEmergencia; }
    public String getTelefonoEmergencia() { return telefonoEmergencia; }
  }


}
