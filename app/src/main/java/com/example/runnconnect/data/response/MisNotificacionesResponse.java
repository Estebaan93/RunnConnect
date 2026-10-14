package com.example.runnconnect.data.response;

import java.util.List;

public class MisNotificacionesResponse {

  private int totalNotificaciones;
  private List<NotificacionItemResponse> notificaciones;

  public int getTotalNotificaciones() {
    return totalNotificaciones;
  }

  public void setTotalNotificaciones(int totalNotificaciones) {
    this.totalNotificaciones = totalNotificaciones;
  }

  public List<NotificacionItemResponse> getNotificaciones() {
    return notificaciones;
  }

  public void setNotificaciones(List<NotificacionItemResponse> notificaciones) {
    this.notificaciones = notificaciones;
  }

  public static class NotificacionItemResponse {
    private int idNotificacion;
    private String titulo;
    private String mensaje;
    private String fechaEnvio;
    private boolean esPrivada;
    private int idEvento;
    private String nombreEvento;
    private Integer idCategoria;
    private String nombreCategoria;
    private String fechaEvento;
    private String estadoEvento;
    private String tiempoTranscurrido;
    private boolean esReciente;
    private boolean esNueva;

    public int getIdNotificacion() {
      return idNotificacion;
    }

    public void setIdNotificacion(int idNotificacion) {
      this.idNotificacion = idNotificacion;
    }

    public String getTitulo() {
      return titulo;
    }

    public void setTitulo(String titulo) {
      this.titulo = titulo;
    }

    public String getMensaje() {
      return mensaje;
    }

    public void setMensaje(String mensaje) {
      this.mensaje = mensaje;
    }

    public String getFechaEnvio() {
      return fechaEnvio;
    }

    public void setFechaEnvio(String fechaEnvio) {
      this.fechaEnvio = fechaEnvio;
    }

    public boolean isEsPrivada() {
      return esPrivada;
    }

    public void setEsPrivada(boolean esPrivada) {
      this.esPrivada = esPrivada;
    }

    public int getIdEvento() {
      return idEvento;
    }

    public void setIdEvento(int idEvento) {
      this.idEvento = idEvento;
    }

    public String getNombreEvento() {
      return nombreEvento;
    }

    public void setNombreEvento(String nombreEvento) {
      this.nombreEvento = nombreEvento;
    }

    public Integer getIdCategoria() {
      return idCategoria;
    }

    public void setIdCategoria(Integer idCategoria) {
      this.idCategoria = idCategoria;
    }

    public String getNombreCategoria() {
      return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
      this.nombreCategoria = nombreCategoria;
    }

    public String getFechaEvento() {
      return fechaEvento;
    }

    public void setFechaEvento(String fechaEvento) {
      this.fechaEvento = fechaEvento;
    }

    public String getEstadoEvento() {
      return estadoEvento;
    }

    public void setEstadoEvento(String estadoEvento) {
      this.estadoEvento = estadoEvento;
    }

    public String getTiempoTranscurrido() {
      return tiempoTranscurrido;
    }

    public void setTiempoTranscurrido(String tiempoTranscurrido) {
      this.tiempoTranscurrido = tiempoTranscurrido;
    }

    public boolean isEsReciente() {
      return esReciente;
    }

    public void setEsReciente(boolean esReciente) {
      this.esReciente = esReciente;
    }

    public boolean isEsNueva() {
      return esNueva;
    }

    public void setEsNueva(boolean esNueva) {
      this.esNueva = esNueva;
    }
  }
}
