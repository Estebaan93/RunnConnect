//Models/Dto/Evento/CambiarEstadoRequest

using System.ComponentModel.DataAnnotations;

namespace RunnConnectAPI.Models.Dto.Evento
{
  /* DTO para cambiar el estado de un evento
  Estados validos: "publicado", "cancelado", "finalizado"
  PUT: api/Evento/{id}/Estado */

  public class CambiarEstadoEventoRequest
  {
    [Required(ErrorMessage = "El nuevo estado es obligatorio")]
    [RegularExpression("^(publicado|cancelado|finalizado|suspendido|retrasado)$", 
      ErrorMessage = "El estado debe ser: publicado, cancelado, suspendido, finalizado o retrasado")]
    public string NuevoEstado { get; set; } = string.Empty;

  
    /*Motivo opcional del cambio de estado (util para cancelaciones)
    Ejemplo: "Mal clima", "Falta de inscriptos", etc.*/
   
    [StringLength(500, ErrorMessage = "El motivo no puede exceder 500 caracteres")]
    public string? Motivo { get; set; }
  }
}