// Models/Dto/Notificacion/NotificacionResponse.cs

namespace RunnConnectAPI.Models.Dto.Notificacion
{
  /// DTO de respuesta con información de una notificacion
  public class NotificacionResponse
  {
    public int IdNotificacion { get; set; }
    public int IdEvento { get; set; }
    public string Titulo { get; set; } = string.Empty;
    public string? Mensaje { get; set; }
    public DateTime FechaEnvio { get; set; }

    /// Informacion basica del evento asociado
    public EventoNotificacionInfo? Evento { get; set; }

    /// Tiempo transcurrido desde el envio (para mostrar "hace 2 horas", etc.)
    public string TiempoTranscurrido => CalcularTiempoTranscurrido();

    private string CalcularTiempoTranscurrido()
    {
      var diferencia = DateTime.Now - FechaEnvio;

      if (diferencia.TotalMinutes < 1)
        return "Hace un momento";
      if (diferencia.TotalMinutes < 60)
        return $"Hace {(int)diferencia.TotalMinutes} minuto{((int)diferencia.TotalMinutes != 1 ? "s" : "")}";
      if (diferencia.TotalHours < 24)
        return $"Hace {(int)diferencia.TotalHours} hora{((int)diferencia.TotalHours != 1 ? "s" : "")}";
      if (diferencia.TotalDays < 7)
        return $"Hace {(int)diferencia.TotalDays} día{((int)diferencia.TotalDays != 1 ? "s" : "")}";
      if (diferencia.TotalDays < 30)
        return $"Hace {(int)(diferencia.TotalDays / 7)} semana{((int)(diferencia.TotalDays / 7) != 1 ? "s" : "")}";

      return FechaEnvio.ToString("dd/MM/yyyy HH:mm");
    }
  }


  /// Informacion basica del evento para mostrar junto a la notificacion
  public class EventoNotificacionInfo
  {
    public int IdEvento { get; set; }
    public string Nombre { get; set; } = string.Empty;
    public DateTime FechaHora { get; set; }
    public string Lugar { get; set; } = string.Empty;
    public string Estado { get; set; } = string.Empty;
  }
}