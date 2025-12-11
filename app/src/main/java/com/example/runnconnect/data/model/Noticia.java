package com.example.runnconnect.data.model;

public class Noticia {
  private String titulo;
  private String descripcion;
  private String link;
  private String imagenUrl; //aca guardamos la URL de la imagen extraida con Regex
  private String fecha;

  public Noticia(String titulo, String descripcion, String link, String imagenUrl, String fecha) {
    this.titulo = titulo;
    this.descripcion = descripcion;
    this.link = link;
    this.imagenUrl = imagenUrl;
    this.fecha = fecha;
  }

  public String getTitulo() {
    return titulo;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public String getLink() {
    return link;
  }

  public String getImagenUrl() {
    return imagenUrl;
  }

  public String getFecha() {
    return fecha;
  }


}
