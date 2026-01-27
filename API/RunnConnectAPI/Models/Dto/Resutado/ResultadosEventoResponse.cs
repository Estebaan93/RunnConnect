// Models/Dto/Resultado/ResultadosEventoResponse.cs

namespace RunnConnectAPI.Models.Dto.Resultado
{
  /// Respuesta con todos los resultados de un evento
  /// GET: api/Resultado/Evento/{idEvento}
  public class ResultadosEventoResponse
  {
    public EventoResultadoInfo Evento { get; set; } = new();
    public int TotalParticipantes { get; set; }
    public int TotalConResultado { get; set; }
    public int TotalSinResultado { get; set; }
    public List<ResultadoEventoItem> Resultados { get; set; } = new();
  }

  public class EventoResultadoInfo
  {
    public int IdEvento { get; set; }
    public string Nombre { get; set; } = string.Empty;
    public DateTime FechaHora { get; set; }
    public string Lugar { get; set; } = string.Empty;
    public string Estado { get; set; } = string.Empty;
  }

  /// Item de resultado en el listado del evento
  /// Ordenado por posición general
  public class ResultadoEventoItem
  {
    public int IdResultado { get; set; }
    public int IdInscripcion { get; set; }

    // Runner
    public string NombreRunner { get; set; } = string.Empty;
    public int? DniRunner { get; set; }
    public string? Genero { get; set; }
    public string? Agrupacion { get; set; }

    // Categoría
    public string NombreCategoria { get; set; } = string.Empty;

    // Resultados oficiales
    public string? TiempoOficial { get; set; }
    public int? PosicionGeneral { get; set; }
    public int? PosicionCategoria { get; set; }

    // Indicador de datos smartwatch
    public bool TieneDatosSmartwatch { get; set; }
  }
}