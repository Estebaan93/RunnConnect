//Models/CategoriaEvento
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Text.Json.Serialization;

namespace RunnConnectAPI.Models
{
  [Table("categorias_evento")]
  public class CategoriaEvento
  {
    [Key]
    public int IdCategoria{get;set;}

    [Required]
    public int IdEvento{get;set;}

    [Required(ErrorMessage ="El nombre de la categoria es requerido")]
    [StringLength(100, MinimumLength =2, ErrorMessage ="El nombre debe tener mas de 2 caracteres")]
    public string Nombre {get;set;}

    [Required]
    [Column(TypeName ="decimal(10,2)")]
    [Range(0, 999999.99, ErrorMessage ="El costo debe ser mayor o igual a 0")]
    public decimal CostoInscripcion {get;set;}= 0.00M;

    [Range(1,10000, ErrorMessage ="El cupo debe estar en 1 y 10000")]
    public int? CupoCategoria {get;set;}

    [Range(14, 90, ErrorMessage ="La edad minima es 14")]
    public int EdadMinima {get;set;}

    [Range(14, 90, ErrorMessage ="La edad Maxima es 95")]
    public int EdadMaxima {get;set;}

    [Required]
    [Column(TypeName="varchar(1)")] //F, M , X
    public string Genero {get;set;}= "X";


    /*Navegacion*/
    //Evento al que pertenece la categoria
    [ForeignKey("IdEvento")]
    [JsonIgnore]
    public Evento? Evento{get;set;}

    //Inscripciones a esta categoria
    [JsonIgnore]
    public ICollection<Inscripcion>? Inscripciones{get;set;}

  }
}