//Models/Dto/Evento/EventoDetalleResponse.cs

using RunnConnectAPI.Models.Dto.Categoria;

namespace RunnConnectAPI.Models.Dto.Evento
{

  /// DTO de respuesta con información completa del evento (para detalle)

  public class EventoDetalleResponse
  {
    public int IdEvento { get; set; }
    public string Nombre { get; set; } = string.Empty;
    public string? Descripcion { get; set; }
    public DateTime FechaHora { get; set; }
    public string Lugar { get; set; } = string.Empty;
    public int? CupoTotal { get; set; }
    public int InscriptosActuales { get; set; }
    public int CuposDisponibles { get; set; }
    public string Estado { get; set; } = string.Empty;
    public string? UrlPronosticoClima { get; set; }
    public string? DatosPago { get; set; }

   
    /// Información del organizador
  
    public OrganizadorEventoResponse? Organizador { get; set; }

   
    /// Categorías disponibles en el evento
   
    public List<CategoriaEventoResponse>? Categorias { get; set; }
  }
}