// Models/Dto/Notificacion/CrearNotificacionRequest.cs
using System.ComponentModel.DataAnnotations;

namespace RunnConnectAPI.Models.Dto.Notificacion
{
  /// DTO para que el organizador cree una notificación para su evento
  /// POST: api/Notificacion
  public class CrearNotificacionRequest
  {
    /// ID del evento al que pertenece la notificacion
    [Required(ErrorMessage = "El evento es obligatorio")]
    public int IdEvento { get; set; }

    /// Titulo de la notificación (obligatorio)
    /// Ej: "Evento Suspendido", "Cambio de Horario", "Recordatorio"
    [Required(ErrorMessage = "El titulo es obligatorio")]
    [StringLength(255, MinimumLength = 3, ErrorMessage = "El título debe tener mas 3 caracteres")]
    public string Titulo { get; set; } = string.Empty;

    /// Mensaje detallado de la notificacion (opcional)
    /// Ej: "Debido al mal clima, el evento se posterga para el proximo domingo..."
    [StringLength(2000, ErrorMessage = "El mensaje no puede exceder 2000 caracteres")]
    public string? Mensaje { get; set; }
  }
}