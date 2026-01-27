// Models/Dto/Resultado/CargarResultadoRequest.cs
using System.ComponentModel.DataAnnotations;

namespace RunnConnectAPI.Models.Dto.Resultado
{
  /// DTO para que el organizador cargue un resultado individual
  /// POST: api/Resultado/Cargar
  public class CargarResultadoRequest
  {
  
    /// ID de la inscripcion a la que se le carga el resultado
    [Required(ErrorMessage = "La inscripción es obligatoria")]
    public int IdInscripcion { get; set; }

    /// /// Tiempo oficial de la carrera. Formato: HH:MM:SS o HH:MM:SS.mmm
    /// Ej: "00:45:30" o "00:45:30.123"
    [Required(ErrorMessage = "El tiempo oficial es obligatorio")]
    [StringLength(20, ErrorMessage = "El tiempo no puede exceder 20 caracteres")]
    [RegularExpression(@"^\d{2}:\d{2}:\d{2}(\.\d{1,3})?$", 
      ErrorMessage = "Formato de tiempo invalido. Use HH:MM:SS o HH:MM:SS.mmm")]
    public string TiempoOficial { get; set; } = string.Empty;


    /// Posicion general en toda la carrera (opcional, puede calcularse despus)
    [Range(1, 100000, ErrorMessage = "La posicion general debe ser mayor a 0")]
    public int? PosicionGeneral { get; set; }


    /// Posicion dentro de su categoría (opcional, puede calcularse después)
    [Range(1, 10000, ErrorMessage = "La posicion en categoría debe ser mayor a 0")]
    public int? PosicionCategoria { get; set; }
  }
}