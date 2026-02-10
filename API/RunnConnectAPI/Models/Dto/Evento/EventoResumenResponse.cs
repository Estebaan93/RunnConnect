//Models/Dto/Evento/EventoResumenResponse.cs

using RunnConnectAPI.Models.Dto.Categoria;

namespace RunnConnectAPI.Models.Dto.Evento
{
 
  /// DTO de respuesta con informacion basica del evento (para listados)

  public class EventoResumenResponse
  {
    public int IdEvento { get; set; }
    public string Nombre { get; set; } = string.Empty;
    public DateTime FechaHora { get; set; }
    public string Lugar { get; set; } = string.Empty;
    public string Estado { get; set; } = string.Empty;
    public int? CupoTotal { get; set; }
    public int InscriptosActuales { get; set; }
    public int CantidadCategorias { get; set; }
    public string TipoEvento { get; set; }
    public string? DatosPago { get; set; }

    
    /// Nombre del organizador (de la tabla usuarios)
    public string NombreOrganizador { get; set; } = string.Empty;

    public List<CategoriaEventoResponse>? Categorias {get; set; }
  }

}