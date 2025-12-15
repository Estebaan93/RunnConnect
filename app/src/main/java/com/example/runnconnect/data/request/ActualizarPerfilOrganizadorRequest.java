//data/request/ActualizarPerfilOrganizadorRequest
package com.example.runnconnect.data.request;

public class ActualizarPerfilOrganizadorRequest { //lo que se envia
  public String Nombre; // nombre de contacto
  public String Telefono;
  public String RazonSocial;
  public String NombreComercial;
  public String CuitTaxId;
  public String DireccionLegal;

  public ActualizarPerfilOrganizadorRequest(String nombre, String telefono, String razonSocial, String nombreComercial, String cuitTaxId, String direccionLegal) {
    Nombre = nombre;
    Telefono = telefono;
    RazonSocial = razonSocial;
    NombreComercial = nombreComercial;
    CuitTaxId = cuitTaxId;
    DireccionLegal = direccionLegal;
  }

}
