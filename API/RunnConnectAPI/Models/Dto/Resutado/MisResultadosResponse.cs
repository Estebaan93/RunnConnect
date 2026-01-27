// Models/Dto/Resultado/MisResultadosResponse.cs

namespace RunnConnectAPI.Models.Dto.Resultado
{
  /// Respuesta con los resultados del runner autenticado
  /// GET: api/Resultado/MisResultados
  public class MisResultadosResponse
  {
    public int TotalCarreras { get; set; }
    public List<MiResultadoItem> Resultados { get; set; } = new();
    
    // Estadisticas generales
    public EstadisticasRunner? Estadisticas { get; set; }
  }

  public class MiResultadoItem
  {
    public int IdResultado { get; set; }
    public int IdInscripcion { get; set; }

    // Evento
    public int IdEvento { get; set; }
    public string NombreEvento { get; set; } = string.Empty;
    public DateTime FechaEvento { get; set; }
    public string LugarEvento { get; set; } = string.Empty;

    // Categoría
    public string NombreCategoria { get; set; } = string.Empty;

    // Resultados oficiales
    public string? TiempoOficial { get; set; }
    public int? PosicionGeneral { get; set; }
    public int? PosicionCategoria { get; set; }
    public int? TotalParticipantesCategoria { get; set; }

    // Datos smartwatch
    public DatosSmartwatchInfo? DatosSmartwatch { get; set; }
  }

  /// Estadisticas acumuladas del runner
  public class EstadisticasRunner
  {
    public int CarrerasCompletadas { get; set; }
    public decimal? DistanciaTotalKm { get; set; }
    public int? CaloriasTotales { get; set; }
    public string? MejorTiempo { get; set; }
    public int? MejorPosicion { get; set; }
  }
}