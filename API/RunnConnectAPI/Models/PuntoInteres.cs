//Models/PuntoInteres
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Text.Json.Serialization;


namespace RunnConnectAPI.Models
{
  /*Puntos de interes del evento (hidratacion, mea, salida etc)- 
    Se muestra en el mapa para los runners*/
  [Table("puntosinteres")]
  public class PuntoInteres
  {
    [Key]
    [Column("idPuntoInteres")]
    public int IdPuntoInteres {get;set;}

    [Required]
    [Column("idEvento")]
    public int IdEvento {get;set;}


    /*Tipo del punto de interes: hidratacion, meta, primeroAuxilios*/
    [Required]
    [Column("tipo",TypeName="varchar(50)")]
    public string Tipo {get;set;} = string.Empty; //Hidratacion, meta, largada

    /*Nombre descriptivo (ej hidratacion KM 5)*/
    [Required(ErrorMessage ="El nombre del punto de interes es obligatorio")]
    [StringLength(100)]
    [Column("nombre")]
    public string Nombre {get;set;} = string.Empty;

    [Required]
    [Column("latitud", TypeName = "decimal(10,7)")]
    [Range(-90, 90, ErrorMessage = "La latitud debe estar entre -90 y 90")]
    public decimal Latitud { get; set; }

    [Required]
    [Column("longitud", TypeName = "decimal(10,7)")]
    [Range(-180, 180, ErrorMessage = "La longitud debe estar entre -180 y 180")]
    public decimal Longitud { get; set; }


    /*Navegacion*/  
    [ForeignKey("IdEvento")]
    [JsonIgnore]
    public Evento? Evento {get;set;}

  }
}