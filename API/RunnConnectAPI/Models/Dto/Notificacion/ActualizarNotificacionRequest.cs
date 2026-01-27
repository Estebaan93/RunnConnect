// Models/Dto/Notificacion/ActualizarNotificacionRequest.cs
using System.ComponentModel.DataAnnotations;

namespace RunnConnectAPI.Models.Dto.Notificacion
{
  /// DTO para que el organizador actualice una notificacion existente
  /// PUT: api/Notificacion/{id}
  public class ActualizarNotificacionRequest
  {
    /// Título de la notificacion
    [Required(ErrorMessage = "El título es obligatorio")]
    [StringLength(255, MinimumLength = 3, ErrorMessage = "El titulo debe tener mas 3 caracteres")]
    public string Titulo { get; set; } = string.Empty;

    /// Mensaje detallado de la notificacion
    [StringLength(2000, ErrorMessage = "El mensaje no puede exceder 2000 caracteres")]
    public string? Mensaje { get; set; }
  }
}