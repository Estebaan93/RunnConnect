//Models/Usuario
using System;
using System.ComponentModel.DataAnnotations; //Para las anotaciones y reglas de negocios
using System.ComponentModel.DataAnnotations.Schema;
using System.Text.Json.Serialization; //Para el mapeo de la tabla

namespace RunnConnectAPI.Models
{
  [Table("Usuarios")]
  public class Usuario
  {
    /*Datos obligatorios al registrar usuario (runner/organizador)*/
    [Key]
    public int IdUsuario { get; set; }

    [Required(ErrorMessage = "El nombre es requerido")]
    [StringLength(100, MinimumLength = 2, ErrorMessage = "El nombre debe tener mas de 2 caracteres")]
    public string Nombre { get; set; } = string.Empty;

    [Required(ErrorMessage = "El email es requerido")]
    [EmailAddress(ErrorMessage = "El formato del email no es valido")]
    public string Email { get; set; }

    [Required]
    [JsonIgnore] //No exponer el hash de password en las respuestas
    public string PasswordHash { get; set; }

    [Required(ErrorMessage = "El tipo de usuario es requerido")]
    [Column(TypeName = "varchar(20)")]
    public string TipoUsuario { get; set; } = string.Empty; //"runner" o "organizador"

    //Estado para borrado logico - default true
    [Column("estado", TypeName = "tinyint(1)")]
    public bool Estado { get; set; } = true;

    /*Datos que se pueden completar post registro*/
    //Se completa post registro
    [StringLength(20, MinimumLength = 7, ErrorMessage = "El telefono debe tener entre 7 y 20 caracteres")]
    [Column("telefono")]
    public string? Telefono { get; set; }

    //Opcional si no el sistema le asigna un avatar default
    [StringLength(500, ErrorMessage = "La URL del avatar no puede exceder 500 caracteres")]
    [Url(ErrorMessage = "La URL del avatar no es valida")]
    public string? ImgAvatar { get; set; }



    //Navgacion EF puede inferirla
    public PerfilRunner? PerfilRunner {get;set;}
    public PerfilOrganizador? PerfilOrganizador {get;set;}

  }
}