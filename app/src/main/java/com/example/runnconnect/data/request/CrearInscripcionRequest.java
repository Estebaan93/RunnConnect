package com.example.runnconnect.data.request;

public class CrearInscripcionRequest {
  private int idCategoria;
  private String talleRemera;
  private boolean aceptoDeslinde;

  public CrearInscripcionRequest(int idCategoria, String talleRemera, boolean aceptoDeslinde) {
    this.idCategoria = idCategoria;
    this.talleRemera = talleRemera;
    this.aceptoDeslinde = aceptoDeslinde;
  }

  public int getIdCategoria() {
    return idCategoria;
  }

  public void setIdCategoria(int idCategoria) {
    this.idCategoria = idCategoria;
  }

  public String getTalleRemera() {
    return talleRemera;
  }

  public void setTalleRemera(String talleRemera) {
    this.talleRemera = talleRemera;
  }

  public boolean isAceptoDeslinde() {
    return aceptoDeslinde;
  }

  public void setAceptoDeslinde(boolean aceptoDeslinde) {
    this.aceptoDeslinde = aceptoDeslinde;
  }
}
