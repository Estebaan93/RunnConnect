//Models/Dto/Evento/ActualizarEventoRequest.cs
using System.ComponentModel.DataAnnotations;

namespace RunnConnectAPI.Models.Dto.Evento
{
  /*Dto para actualizar un evento
  PUT: api/Evento/{id}*/
  public class ActualizarEventoRequest
  {
    [Required(ErrorMessage = "El nombre del evento es obligatorio")]
    [StringLength(255, MinimumLength = 5, ErrorMessage = "El nombre debe tener entre 5 y 255 caracteres")]
    public string Nombre { get; set; } = string.Empty;

    [StringLength(2000, ErrorMessage = "La descripción no puede exceder 2000 caracteres")]
    public string? Descripcion { get; set; }

    [Required(ErrorMessage = "La fecha y hora del evento son obligatorias")]
    public DateTime FechaHora { get; set; }

    [Required(ErrorMessage = "El lugar del evento es obligatorio")]
    [StringLength(255, MinimumLength = 3, ErrorMessage = "El lugar debe tener entre 3 y 255 caracteres")]
    public string Lugar { get; set; } = string.Empty;

    [Range(1, 100000, ErrorMessage = "El cupo debe estar entre 1 y 100.000 participantes")]
    public int? CupoTotal { get; set; }

    [Url(ErrorMessage = "La URL del pronóstico debe ser válida")]
    [StringLength(255)]
    public string? UrlPronosticoClima { get; set; }

    [StringLength(500, ErrorMessage = "Los datos de pago no pueden exceder 500 caracteres")]
    public string? DatosPago { get; set; }

    
  }
}