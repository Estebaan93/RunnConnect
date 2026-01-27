//Models/Dto/Evento/FiltroEventosRequest.cs
using System.ComponentModel.DataAnnotations;

namespace RunnConnectAPI.Models.Dto.Evento
{
  /// DTO para filtrar eventos en la búsqueda
  /// GET: api/Evento/Buscar

  public class FiltroEventosRequest
  {
    public string? Nombre { get; set; }
    public string? Lugar { get; set; }
    public DateTime? FechaDesde { get; set; }
    public DateTime? FechaHasta { get; set; }
    public string? Estado { get; set; }
    public int? IdOrganizador { get; set; }

  
    /// Página actual (para paginación)
   
    [Range(1, int.MaxValue, ErrorMessage = "La página debe ser mayor a 0")]
    public int Pagina { get; set; } = 1;

  
    /// Cantidad de resultados por página
   
    [Range(1, 50, ErrorMessage = "El tamaño de página debe estar entre 1 y 50")]
    public int TamanioPagina { get; set; } = 10;
  }
}