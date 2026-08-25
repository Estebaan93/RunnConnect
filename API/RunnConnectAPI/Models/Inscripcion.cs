//Models/Inscripcion
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Text.Json.Serialization;

namespace RunnConnectAPI.Models
{
  [Table("inscripciones")]
  public class Inscripcion
  {
    [Key]
    [Column("idInscripcion")]
    public int IdInscripcion {get;set;}

    [Required]
    [Column("idUsuario")]
    public int IdUsuario {get;set;}

    [Required]
    [Column("idCategoria")]
    public int IdCategoria {get;set;}

    [Column("fechaInscripcion")]
    public DateTime FechaInscripcion {get;set;}= DateTime.Now;

    [Required]
    [Column("estadoPago", TypeName="varchar(20)")]
    public string EstadoPago {get;set;}= "pendiente";
    
    [Column("talleRemera", TypeName="varchar(10)")]
    public string? TalleRemera {get;set;} //xs, s, m , l ,xl, xxl 
    /*Ver disponibilidad del evento y ver si el evento entrega las remeras*/

    [Required]
    [Column("aceptoDeslinde", TypeName="tinyint(1)")]
    public bool AceptoDeslinde {get; set;}= false;

    [StringLength(255)]
    [Column("comprobantePagoURL")]
    public string? ComprobantePagoURL {get;set;}

    [StringLength(250)]
    [Column("observacion")]
    public string Observacion { get; set; } = "Inscripcion creada. Pago pendiente";//observacion del estado pago


    /*Navegacion*/
    [ForeignKey("IdUsuario")]
    [JsonIgnore]
    public Usuario? Usuario { get; set; }

    [ForeignKey("IdCategoria")]
    [JsonIgnore]
    public CategoriaEvento? Categoria { get; set; }

    // Resultado de esta inscripcion (1:1)
    [JsonIgnore]
    public Resultado? Resultado { get; set; } 


  }
}