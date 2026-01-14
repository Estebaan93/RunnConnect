package com.example.runnconnect.data.response;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class MapaEventoResponse { //obj contenedor que me devuelve la api
  @SerializedName("idEvento")
  private int idEvento;

  // El backend devuelve un OBJETO "Ruta"
  @SerializedName(value = "ruta", alternate = {"Ruta"})
  private RutaObject ruta;

  public int getIdEvento() { return idEvento; }

  // Helper para obtener la lista limpia directamente
  public List<RutaPuntoResponse> getRuta() {
    if (ruta != null && ruta.puntos != null) {
      return ruta.puntos;
    }
    return new ArrayList<>();
  }

  // --- Clase interna para mapear la estructura intermedia del C# ---
  public static class RutaObject {
    @SerializedName(value = "puntos", alternate = {"Puntos"})
    private List<RutaPuntoResponse> puntos;
  }

}
