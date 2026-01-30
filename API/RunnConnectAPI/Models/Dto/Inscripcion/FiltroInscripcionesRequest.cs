// Models/Dto/Inscripcion/FiltroInscripcionesRequest.cs
using System.ComponentModel.DataAnnotations;

namespace RunnConnectAPI.Models.Dto.Inscripcion
{

  /// DTO para filtrar inscripciones de un evento (para organizadores)
  /// GET: api/Evento/{idEvento}/Inscripciones
  public class FiltroInscripcionesRequest
  {
    /// Filtrar por categoria especifica
    public int? IdCategoria { get; set; }


    /// Filtrar por estado de pago
    [RegularExpression("^(pendiente|procesando|pagado|rechazado|reembolsado|cancelado)$",
      ErrorMessage = "Estado invalido. Use: pendiente, procesando, pagado, rechazado, reembolsado, cancelado")]
    public string? EstadoPago { get; set; }


    /// Buscar por nombre o apellido del runner
    public string? BuscarRunner { get; set; }


    /// Pagina actual (para paginacion)
    [Range(1, int.MaxValue, ErrorMessage = "La pagina debe ser mayor a 0")]
    public int Pagina { get; set; } = 1;

    /// Cantidad de resultados por pagina
    [Range(1, 100, ErrorMessage = "El tamaño de pagina debe estar entre 1 y 100")]
    public int TamanioPagina { get; set; } = 20;

  }
}