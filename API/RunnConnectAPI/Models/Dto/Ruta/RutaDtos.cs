// Models/Dto/Ruta/RutaDtos.cs
using System.ComponentModel.DataAnnotations;

namespace RunnConnectAPI.Models.Dto.Ruta
{
  /// DTO para crear/actualizar la ruta completa de un evento
  /// PUT: api/Evento/{idEvento}/Ruta
  public class GuardarRutaRequest
  {
    /// Lista de puntos GPS que forman el trazado
    /// Deben estar ordenados (el orden se asigna automáticamente)
    [Required(ErrorMessage = "Debe incluir al menos un punto en la ruta")]
    [MinLength(2, ErrorMessage = "La ruta debe tener al menos 2 puntos")]
    public List<PuntoRutaRequest> Puntos { get; set; } = new();
  }

  /// Punto individual de la ruta
  public class PuntoRutaRequest
  {
    [Required]
    [Range(-90, 90, ErrorMessage = "La latitud debe estar entre -90 y 90")]
    public decimal Latitud { get; set; }

    [Required]
    [Range(-180, 180, ErrorMessage = "La longitud debe estar entre -180 y 180")]
    public decimal Longitud { get; set; }
  }

  /// Respuesta con la ruta completa de un evento
  /// GET: api/Evento/{idEvento}/Ruta
  public class RutaResponse
  {
    public int IdEvento { get; set; }
    public string NombreEvento { get; set; } = string.Empty;
    public int TotalPuntos { get; set; }
    
    /// Lista de puntos ordenados que forman el trazado
    public List<PuntoRutaResponse> Puntos { get; set; } = new();
  }

  /// Punto individual en la respuesta
  public class PuntoRutaResponse
  {
    public int IdRuta { get; set; }
    public int Orden { get; set; }
    public decimal Latitud { get; set; }
    public decimal Longitud { get; set; }
  }
}