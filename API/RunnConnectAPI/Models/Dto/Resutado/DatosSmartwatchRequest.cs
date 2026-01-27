// Models/Dto/Resultado/DatosSmartwatchRequest.cs
using System.ComponentModel.DataAnnotations;

namespace RunnConnectAPI.Models.Dto.Resultado
{
  /// DTO para que el runner agregue sus datos de smartwatch/Google Fit
  /// PUT: api/Resultado/{id}/DatosSmartwatch
  public class DatosSmartwatchRequest
  {
    /// Tiempo registrado por el smartwatch. Formato: HH:MM:SS o HH:MM:SS.mmm
    [StringLength(20)]
    [RegularExpression(@"^\d{2}:\d{2}:\d{2}(\.\d{1,3})?$", 
      ErrorMessage = "Formato de tiempo inválido. Use HH:MM:SS o HH:MM:SS.mmm")]
    public string? TiempoSmartwatch { get; set; }

    /// Distancia recorrida en kilómetros. Ej: 10.02
    [Range(0.01, 999.99, ErrorMessage = "La distancia debe estar entre 0.01 y 999.99 km")]
    public decimal? DistanciaKm { get; set; }

    /// Ritmo promedio. Ej: "4:32 min/km"
    [StringLength(20)]
    public string? RitmoPromedio { get; set; }

    /// Velocidad promedio. Ej: "13.2 km/h"
    [StringLength(20)]
    public string? VelocidadPromedio { get; set; }

    /// Calorías quemadas durante la carrera
    [Range(0, 10000, ErrorMessage = "Las calorias deben estar entre 0 y 10000")]
    public int? CaloriasQuemadas { get; set; }

    /// Pulsaciones promedio durante la carrera
    [Range(30, 250, ErrorMessage = "Las pulsaciones promedio deben estar entre 30 y 250")]
    public int? PulsacionesPromedio { get; set; }

    /// Pulsaciones maximas durante la carrera
    [Range(30, 250, ErrorMessage = "Las pulsaciones maximas deben estar entre 30 y 250")]
    public int? PulsacionesMax { get; set; }
  }
}