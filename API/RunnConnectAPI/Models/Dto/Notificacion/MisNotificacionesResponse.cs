// Models/Dto/Notificacion/MisNotificacionesResponse.cs

namespace RunnConnectAPI.Models.Dto.Notificacion
{
   /// DTO de respuesta con las notificaciones del runner
  /// GET: api/Notificacion/MisNotificaciones
  public class MisNotificacionesResponse
  {
    /// Total de notificaciones encontradas

    public int TotalNotificaciones { get; set; }

    /// Lista de notificaciones ordenadas por fecha (mas recientes primero)
    public List<NotificacionRunnerItem> Notificaciones { get; set; } = new();
  }

  /// Item de notificación para el runner
  /// Incluye info del evento para contexto
  public class NotificacionRunnerItem
  {
    public int IdNotificacion { get; set; }
    public string Titulo { get; set; } = string.Empty;
    public string? Mensaje { get; set; }
    public DateTime FechaEnvio { get; set; }

    // Información del evento
    public int IdEvento { get; set; }
    public string NombreEvento { get; set; } = string.Empty;
    public DateTime FechaEvento { get; set; }
    public string EstadoEvento { get; set; } = string.Empty;

    /// Tiempo transcurrido legible
    public string TiempoTranscurrido => CalcularTiempoTranscurrido();

    /// Indica si es una notificación reciente (últimas 24 horas)
    public bool EsReciente => (DateTime.Now - FechaEnvio).TotalHours < 24;

    private string CalcularTiempoTranscurrido()
    {
      var diferencia = DateTime.Now - FechaEnvio;

      if (diferencia.TotalMinutes < 1)
        return "Hace un momento";
      if (diferencia.TotalMinutes < 60)
        return $"Hace {(int)diferencia.TotalMinutes} min";
      if (diferencia.TotalHours < 24)
        return $"Hace {(int)diferencia.TotalHours} h";
      if (diferencia.TotalDays < 7)
        return $"Hace {(int)diferencia.TotalDays} d";

      return FechaEnvio.ToString("dd/MM");
    }
  }
}