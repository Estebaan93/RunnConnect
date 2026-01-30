//Models/Ruta.cs
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Text.Json.Serialization;

namespace RunnConnectAPI.Models
{
  /*Punto de trazado GPS de la ruta del evento-
    Multiples puntos ordenados forman el recorrido completo*/
  [Table("rutas")]
  public class Ruta
  {
    [Key]
    [Column("idRuta")]
    public int IdRuta { get; set; }

    [Required]
    [Column("idEvento")]
    public int IdEvento { get; set; }

    /*Puntos de trazado: 1, 2, 3..*/
    [Required]
    [Column("orden")]
    [Range(1, 10000, ErrorMessage = "El orden debe ser mayor a 0")] //Orden del los puntos de trazado 1,2,4 etc
    public int Orden { get; set; }

    [Required]
    [Column("latitud", TypeName = "decimal(10,7)")]
    [Range(-90, 90, ErrorMessage = "La latidu debe estar entre -90 y 90")]
    public decimal Latitud { get; set; }

    [Required]
    [Column("longitud", TypeName = "decimal(10,7)")]
    [Range(-180, 180, ErrorMessage = "La longitud debe estar entre -180 y 180")]
    public decimal Longitud { get; set; }


    /*Navegacion*/
    [ForeignKey("IdEvento")]
    [JsonIgnore]
    public Evento? Evento{get;set;}


  }
}