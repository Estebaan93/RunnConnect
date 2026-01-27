// Models/Dto/Resultado/CargarResultadosRequest.cs
using System.ComponentModel.DataAnnotations;

namespace RunnConnectAPI.Models.Dto.Resultado
{
  /// DTO para carga masiva de resultados
  /// POST: api/Resultado/CargarBatch
  public class CargarResultadosRequest
  {
    /// ID del evento al que pertenecen los resultados
    [Required(ErrorMessage = "El evento es obligatorio")]
    public int IdEvento { get; set; }

    /// Lista de resultados a cargar
    [Required(ErrorMessage = "Debe incluir al menos un resultado")]
    [MinLength(1, ErrorMessage = "Debe incluir al menos un resultado")]
    public List<ResultadoItem> Resultados { get; set; } = new();
  }


  /// Item individual para carga batch - identifica por DNI
  public class ResultadoItem
  {

    /// DNI del runner (para identificarlo)
    [Required(ErrorMessage = "El DNI es obligatorio")]
    public int Dni { get; set; }

    /// Tiempo oficial. Formato: HH:MM:SS o HH:MM:SS.mmm
    [Required(ErrorMessage = "El tiempo oficial es obligatorio")]
    [StringLength(20)]
    [RegularExpression(@"^\d{2}:\d{2}:\d{2}(\.\d{1,3})?$", 
      ErrorMessage = "Formato de tiempo inválido")]
    public string TiempoOficial { get; set; } = string.Empty;

    /// Posicion general (opcional)
    [Range(1, 100000)]
    public int? PosicionGeneral { get; set; }

    /// Posición en categoria (opcional)
    [Range(1, 10000)]
    public int? PosicionCategoria { get; set; }
  }

  /// Respuesta de la carga 
  public class ResultadosResponse
  {
    public int TotalProcesados { get; set; }
    public int Exitosos { get; set; }
    public int Fallidos { get; set; }
    public List<ResultadoError> Errores { get; set; } = new();
  }

  public class ResultadoError
  {
    public int Dni { get; set; }
    public string Motivo { get; set; } = string.Empty;
  }
}