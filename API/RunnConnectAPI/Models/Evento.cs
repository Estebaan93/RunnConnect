//Models/Evento
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Text.Json.Serialization;

namespace RunnConnectAPI.Models
{
  [Table("eventos")]
  public class Evento
  {
    [Key]
    public int IdEvento {get;set;}

    [Required(ErrorMessage ="El nombre del evento es requerido")]
    [StringLength(255, MinimumLength =3, ErrorMessage ="El nombre debe tener mas 3 caracteres")]
    public string Nombre {get;set;}= string.Empty;

    [Column(TypeName ="text")]
    public string? Descripcion {get;set;}

    [Required(ErrorMessage ="La fecha y hora del evento son requeridss")]
    public DateTime FechaHora {get;set;}

    [Required(ErrorMessage ="El lugar es requerido")]
    [StringLength(255)]
    public string Lugar {get;set;}= string.Empty;

    [Column("cupoTotal")]
    public int? CupoTotal {get;set;}

    [Required]
    [Column("idOrganizador")]
    public int IdOrganizador {get;set;}

    [Column("urlPronosticoClima")]
    [StringLength(255)]
    public string? UrlPronosticoClima {get;set;}

    //[Required(ErrorMessage ="Los datos para el pago son obligatorios")]
    [Column(TypeName ="text")]
    public string? DatosPago {get;set;}

    [Required]
    [Column("estado")]
    [StringLength(20)]
    public string Estado{get;set;}= "publicado";


    //Navegacion
    /*Usuario organizador del evento (FK a usuarios)*/
    [ForeignKey("IdOrganizador")]
    [JsonIgnore]
    public Usuario? Organizador { get; set; }


    /// Categorias del evento (10K, 5K, etc.)
    
    [JsonIgnore]
    public ICollection<CategoriaEvento>? Categorias { get; set; }

    
    /// Puntos de interes del evento (hidratacion, meta, etc.)
   
    [JsonIgnore]
    public ICollection<PuntoInteres>? PuntosInteres { get; set; }
    
    /// Puntos de la ruta del evento
    
    [JsonIgnore]
    public ICollection<Ruta>? Rutas { get; set; }

    
    /// Notificaciones enviadas para este evento
    
    [JsonIgnore]
    public ICollection<NotificacionEvento>? Notificaciones { get; set; }


  }
}