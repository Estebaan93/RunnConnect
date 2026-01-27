// Models/Dto/Resultado/ResultadoResponse.cs

namespace RunnConnectAPI.Models.Dto.Resultado
{
  /// Respuesta con datos del resultado
  public class ResultadoResponse
  {
    public int IdResultado { get; set; }
    public int IdInscripcion { get; set; }

    // Datos del runner
    public RunnerResultadoInfo? Runner { get; set; }

    // Datos de la categoría
    public CategoriaResultadoInfo? Categoria { get; set; }

    // Datos oficiales (cargados por organizador)
    public string? TiempoOficial { get; set; }
    public int? PosicionGeneral { get; set; }
    public int? PosicionCategoria { get; set; }

    // Datos de smartwatch (cargados por runner)
    public DatosSmartwatchInfo? DatosSmartwatch { get; set; }
  }

  public class RunnerResultadoInfo
  {
    public int IdUsuario { get; set; }
    public string NombreCompleto { get; set; } = string.Empty;
    public int? Dni { get; set; }
    public string? Genero { get; set; }
    public string? Agrupacion { get; set; }
  }

  public class CategoriaResultadoInfo
  {
    public int IdCategoria { get; set; }
    public string Nombre { get; set; } = string.Empty;
    public int IdEvento { get; set; }
    public string NombreEvento { get; set; } = string.Empty;
  }

  public class DatosSmartwatchInfo
  {
    public string? TiempoSmartwatch { get; set; }
    public decimal? DistanciaKm { get; set; }
    public string? RitmoPromedio { get; set; }
    public string? VelocidadPromedio { get; set; }
    public int? CaloriasQuemadas { get; set; }
    public int? PulsacionesPromedio { get; set; }
    public int? PulsacionesMax { get; set; }
    
    /// Indica si hay datos de smartwatch cargados
    public bool TieneDatos => 
      !string.IsNullOrEmpty(TiempoSmartwatch) ||
      DistanciaKm.HasValue ||
      CaloriasQuemadas.HasValue ||
      PulsacionesPromedio.HasValue;
  }
}