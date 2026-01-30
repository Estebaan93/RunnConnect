//Models/NotificacionesEvento
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Text.Json.Serialization;

namespace RunnConnectAPI.Models
{
  /*NOTIFICACIONES enviadas por organizadores a runners inscriptos-
    SISTEMA PULL: Runners consultan al abrir la app*/
  [Table("notificaciones_evento")]
  public class NotificacionEvento
  {
    [Key]
    [Column("idNotificacion")]
    public int IdNotificacion {get;set;}

    [Required]
    [Column("idEvento")]
    public int IdEvento {get;set;}

    [Required(ErrorMessage="El titulo es requerido")]
    [StringLength(255, MinimumLength = 3, ErrorMessage = "El titulo debe tener mas de 3 caracteres")]
    public string Titulo {get;set;}= string.Empty;

    
    [Column("mensaje", TypeName="text")]  
    public string? Mensaje {get;set;}

    [Column("fechaEnvio")]
    public DateTime FechaEnvio {get;set;}= DateTime.Now;

    //Navegacion
    [ForeignKey("IdEvento")]
    [JsonIgnore]
    public Evento? Evento {get;set;}

  }
}