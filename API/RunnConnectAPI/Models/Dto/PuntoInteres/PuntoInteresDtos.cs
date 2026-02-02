// Models/Dto/PuntoInteres/PuntoInteresDtos.cs
using System.ComponentModel.DataAnnotations;

namespace RunnConnectAPI.Models.Dto.PuntoInteres
{
  // DTO para crear un punto de interes
  // POST: api/Evento/{idEvento}/PuntosInteres
  public class CrearPuntoInteresRequest
  {
    // Tipo de punto de interes
    // Valores válidos: hidratacion, primeros_auxilios, otro
    [Required(ErrorMessage = "El tipo es obligatorio")]
    [RegularExpression("^(hidratacion|primeros_auxilios|punto_energetico|otro)$",
      ErrorMessage = "El tipo debe ser: hidratacion, primeros_auxilios, punto_energetico  u otro")]
    public string Tipo { get; set; } = string.Empty;

    [Required(ErrorMessage = "La latitud es obligatoria")]
    [Range(-90, 90, ErrorMessage = "La latitud debe estar entre -90 y 90")]
    public decimal Latitud { get; set; }

    [Required(ErrorMessage = "La longitud es obligatoria")]
    [Range(-180, 180, ErrorMessage = "La longitud debe estar entre -180 y 180")]
    public decimal Longitud { get; set; }
  }

  // DTO para actualizar un punto de interes
  // PUT: api/Evento/{idEvento}/PuntosInteres/{id}
  public class ActualizarPuntoInteresRequest
  {
    [Required(ErrorMessage = "El tipo es obligatorio")]
    [RegularExpression("^(hidratacion|primeros_auxilios|otro)$",
      ErrorMessage = "El tipo debe ser: hidratacion, primeros_auxilios u otro")]
    public string Tipo { get; set; } = string.Empty;

    [Required(ErrorMessage = "La latitud es obligatoria")]
    [Range(-90, 90, ErrorMessage = "La latitud debe estar entre -90 y 90")]
    public decimal Latitud { get; set; }

    [Required(ErrorMessage = "La longitud es obligatoria")]
    [Range(-180, 180, ErrorMessage = "La longitud debe estar entre -180 y 180")]
    public decimal Longitud { get; set; }
  }

  // Respuesta con informacion del punto de interes
  public class PuntoInteresResponse
  {
    public int IdPuntoInteres { get; set; }
    public int IdEvento { get; set; }
    public string Tipo { get; set; } = string.Empty;
    public string Nombre { get; set; } = string.Empty;
    public decimal Latitud { get; set; }
    public decimal Longitud { get; set; }

    // Descripcion legible del tipo
    public string TipoDescripcion => Tipo switch
    {
      "hidratacion" => "Punto de Hidratación",
      "primeros_auxilios" => "Primeros Auxilios",
      "punto_energetico"=>"Punto energetico",
      "otro" => "Otro",
      _ => Tipo
    };

    // Icono sugerido para el mapa (para la app Android)
    public string Icono => Tipo switch
    {
      "hidratacion" => "water_drop",
      "primeros_auxilios" => "medical_services",
      "punto_energetico" => "bolt",
      "otro" => "location_on",
      _ => "location_on"
    };
  }

  // Respuesta con todos los puntos de interes de un evento
  // GET: api/Evento/{idEvento}/PuntosInteres
  public class PuntosInteresEventoResponse
  {
    public int IdEvento { get; set; }
    public string NombreEvento { get; set; } = string.Empty;
    public int TotalPuntos { get; set; }
    public List<PuntoInteresResponse> PuntosInteres { get; set; } = new();

    // Resumen por tipo de punto
    public Dictionary<string, int> ResumenPorTipo { get; set; } = new();
  }
}