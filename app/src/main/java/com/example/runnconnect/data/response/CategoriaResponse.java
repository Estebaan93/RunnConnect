package com.example.runnconnect.data.response;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class CategoriaResponse {
  private int idCategoria;
  private String nombre; //ej 5K

  // IMPORTANTE: Mapear el nombre del JSON "costoInscripcion"
  @SerializedName("costoInscripcion")
  private BigDecimal precio;
  private int inscriptosActuales;
  private int edadMinima;
  private int edadMaxima;
  private String genero; // "X", "F", "M"
  private String estado;

  // Getters
  public int getIdCategoria() { return idCategoria; }
  public String getNombre() { return nombre; }
  public BigDecimal getPrecio() { return precio; } // Getter cómodo
  public int getEdadMinima() { return edadMinima; }
  public int getEdadMaxima() { return edadMaxima; }
  public String getGenero() { return genero; }
  public int getInscriptosActuales() { return inscriptosActuales; }
  public String getEstado() { return estado; }

}
