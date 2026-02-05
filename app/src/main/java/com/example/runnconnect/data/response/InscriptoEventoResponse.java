package com.example.runnconnect.data.response;

public class InscriptoEventoResponse {
  private int idInscripcion;
  private String fechaInscripcion;
  private String estadoPago; // "pendiente", "procesando", "pagado", etc.
  private String estadoEvento;
  private String talleRemera;
  private String comprobantePagoURL; // Puede venir null
  private String nombreCategoria;
  private int idCategoria;
  private RunnerInscriptoInfo runner;

  // Getters
  public int getIdInscripcion() { return idInscripcion; }
  public String getEstadoPago() { return estadoPago; }
  public String getComprobantePagoURL() { return comprobantePagoURL; }
  public String getNombreCategoria() { return nombreCategoria; }
  public String getEstadoEvento() { return estadoEvento; }
  public int getIdCategoria() { return idCategoria; }

  public RunnerInscriptoInfo getRunner() { return runner; }

  public String getTalleRemera() {
    return talleRemera;
  }

  public static class RunnerInscriptoInfo {
    private int idUsuario;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private int dni;
    private String localidad;
    private String genero;
    private String fechaNacimiento;
    private String nombreContactoEmergencia;
    private String telefonoEmergencia;

    // Helper para nombre completo
    public String getNombre () { return nombre; }
    public String getApellido () { return apellido; }
    public String getNombreCompleto() { return nombre + " " + (apellido != null ? apellido : ""); }
    public String getDni() { return String.valueOf(dni); }
    public String getEmail() { return email; }
    public String getLocalidad() { return localidad; }
    public String getGenero() { return genero; }

    public String getTelefono() {
      return telefono;
    }

    public String getFechaNacimiento() { return fechaNacimiento; }
    public String getNombreContactoEmergencia() { return nombreContactoEmergencia; }
    public String getTelefonoEmergencia() { return telefonoEmergencia; }
  }

}
