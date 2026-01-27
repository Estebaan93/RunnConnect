//Models/Dto/Evento/OrganizadorEventoResponse.cs

namespace RunnConnectAPI.Models.Dto.Evento
{
  /*DTO con la informacion del organizador*/
  public class OrganizadorEventoResponse
  {
    public int IdUsuario { get; set; }
    public string Nombre { get; set; } = string.Empty;
    public string? NombreComercial { get; set; }
    public string? Telefono { get; set; }
    public string Email { get; set; } = string.Empty;
  }

}