package com.example.runnconnect.data.request;

import com.google.gson.annotations.SerializedName;

public class BusquedaInscripcionResponse {
  private int idInscripcion;
  private String fechaInscripcion;
  private String estadoPago; // "pagado", "pendiente", "cancelado"

  private int idEvento;
  private String nombreEvento;
  private String nombreCategoria;

  @SerializedName("runner")
  private RunnerSimpleInfo runner;

  // Getters
  public int getIdInscripcion() { return idInscripcion; }
  public String getEstadoPago() { return estadoPago; }
  public String getNombreEvento() { return nombreEvento; }
  public String getNombreCategoria() { return nombreCategoria; }
  public RunnerSimpleInfo getRunner() { return runner; }

  public static class RunnerSimpleInfo {
    private String nombre;
    private String apellido;
    private String dni;
    private String email;

    public String getNombreCompleto() { return nombre + " " + (apellido != null ? apellido : ""); }
    public String getDni() { return dni; }
    public String getEmail() { return email; }
  }


}
