package com.example.runnconnect.data.response;

import java.util.List;

public class MisResultadosResponse {

  private int totalCarreras;
  private List<MiResultadoItem> resultados;
  private EstadisticasRunner estadisticas;

  public int getTotalCarreras() {
    return totalCarreras;
  }

  public void setTotalCarreras(int totalCarreras) {
    this.totalCarreras = totalCarreras;
  }

  public List<MiResultadoItem> getResultados() {
    return resultados;
  }

  public void setResultados(List<MiResultadoItem> resultados) {
    this.resultados = resultados;
  }

  public EstadisticasRunner getEstadisticas() {
    return estadisticas;
  }

  public void setEstadisticas(EstadisticasRunner estadisticas) {
    this.estadisticas = estadisticas;
  }

  public static class MiResultadoItem {
    private int idResultado;
    private int idInscripcion;
    private int idEvento;
    private String nombreEvento;
    private String fechaEvento;
    private String lugarEvento;
    private String nombreCategoria;
    private String tiempoOficial;
    private Integer posicionCategoria;
    private Integer totalParticipantesCategoria;

    public int getIdResultado() {
      return idResultado;
    }

    public void setIdResultado(int idResultado) {
      this.idResultado = idResultado;
    }

    public int getIdInscripcion() {
      return idInscripcion;
    }

    public void setIdInscripcion(int idInscripcion) {
      this.idInscripcion = idInscripcion;
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

    public String getFechaEvento() {
      return fechaEvento;
    }

    public void setFechaEvento(String fechaEvento) {
      this.fechaEvento = fechaEvento;
    }

    public String getLugarEvento() {
      return lugarEvento;
    }

    public void setLugarEvento(String lugarEvento) {
      this.lugarEvento = lugarEvento;
    }

    public String getNombreCategoria() {
      return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
      this.nombreCategoria = nombreCategoria;
    }

    public String getTiempoOficial() {
      return tiempoOficial;
    }

    public void setTiempoOficial(String tiempoOficial) {
      this.tiempoOficial = tiempoOficial;
    }

    public Integer getPosicionCategoria() {
      return posicionCategoria;
    }

    public void setPosicionCategoria(Integer posicionCategoria) {
      this.posicionCategoria = posicionCategoria;
    }

    public Integer getTotalParticipantesCategoria() {
      return totalParticipantesCategoria;
    }

    public void setTotalParticipantesCategoria(Integer totalParticipantesCategoria) {
      this.totalParticipantesCategoria = totalParticipantesCategoria;
    }
  }

  public static class EstadisticasRunner {
    private int carrerasCompletadas;
    private Double distanciaTotalKm;
    private Integer caloriasTotales;
    private String mejorTiempo;
    private Integer posicionCategoria;

    public int getCarrerasCompletadas() {
      return carrerasCompletadas;
    }

    public void setCarrerasCompletadas(int carrerasCompletadas) {
      this.carrerasCompletadas = carrerasCompletadas;
    }

    public Double getDistanciaTotalKm() {
      return distanciaTotalKm;
    }

    public void setDistanciaTotalKm(Double distanciaTotalKm) {
      this.distanciaTotalKm = distanciaTotalKm;
    }

    public Integer getCaloriasTotales() {
      return caloriasTotales;
    }

    public void setCaloriasTotales(Integer caloriasTotales) {
      this.caloriasTotales = caloriasTotales;
    }

    public String getMejorTiempo() {
      return mejorTiempo;
    }

    public void setMejorTiempo(String mejorTiempo) {
      this.mejorTiempo = mejorTiempo;
    }

    public Integer getPosicionCategoria() {
      return posicionCategoria;
    }

    public void setPosicionCategoria(Integer posicionCategoria) {
      this.posicionCategoria = posicionCategoria;
    }
  }
}
