//Models/PerfilRunner.cs
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace RunnConnectAPI.Models
{
  /*Perfil especifico para runners- Datos personales y deportivos
  Relacion 1:1 con Usuario*/

  [Table("perfiles_runners")]
  public class PerfilRunner
  {
    /*Datos obligatorios al momento del registro del perfil*/
    [Key]
    [Column("idPerfilRunner")]
    public int IdPerfilRunner {get;set;}

    [Required]
    [Column("idUsuario")]
    public int IdUsuario {get;set;}  

    [Required(ErrorMessage = "El nombre es obligatorio")]
    [StringLength(100, MinimumLength = 2)]
    [Column("nombre")]
    public string Nombre { get; set; } = string.Empty;

    [Required(ErrorMessage = "El apellido es obligatorio")]
    [StringLength(100, MinimumLength = 2)]
    [Column("apellido")]
    public string Apellido { get; set; } = string.Empty;

    /*Datos que se pueden completarr post registro, pero que antes de inscripcion a evento 
    debe estar completado en su totalidad para que el organizador pueda obtener datos 
    de sus runners inscriptos*/
    // Se completa post registro, pero antes de inscribirse a un evento (requisito)
    [Column("fechaNacimiento", TypeName = "dateTime")]
    public DateTime? FechaNacimiento { get; set; }

    // Se completa post registro, pero antes de inscribirse a un evento (requisito)
    [Column("genero", TypeName = "enum('F','M','X')")]
    public string? Genero { get; set; } // F, M, X

    // Se completa post registro, pero antes de inscribirse a un evento (requisito)
    [Column("dni")]
    public int? Dni { get; set; }
    
    // Se completa post registro, pero antes de inscribirse a un evento (requisito)
    [StringLength(100)]
    [Column("localidad")]
    public string? Localidad { get; set; }

   // Se completa post registro, pero antes de inscribirse a un evento (requisito)
    [StringLength(100)]
    [Column("agrupacion")]
    public string? Agrupacion { get; set; }

    //Se completa post registro, pero antes de inscribirse a un evento (requisito)
    [StringLength(100)]
    [Column("nombreContactoEmergencia")]
    public string? NombreContactoEmergencia{get;set;}

    // Se completa post registro, pero antes de inscribirse a un evento (requisito)
    [StringLength(50)]
    [Column("telefonoEmergencia")]
    public string? TelefonoEmergencia { get; set; }

    [Column("fechaUltimaLectura")]
    public DateTime? FechaUltimaLectura {get;set;}

    // Navegacion
    [ForeignKey("IdUsuario")]
    public Usuario Usuario { get; set; } = null!;


  }



}