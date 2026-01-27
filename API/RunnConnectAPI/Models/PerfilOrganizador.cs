//Models/PerfilOrganizador.cs
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace RunnConnectAPI.Models
{
  /*Perfil especifico a organizadores - Datos legales y comerciales
  Relacion 1:1 con Usuario*/
[Table("perfiles_organizadores")]
  public class PerfilOrganizador
  {
    /*Datos obligatorios al momento del registro del perfil*/
    [Key]
    [Column("idPerfilOrganizador")]
    public int IdPerfilOrganizador { get; set; }

    [Required]
    [Column("idUsuario")]
    public int IdUsuario { get; set; }

    [Required(ErrorMessage = "La razon social es obligatoria")]
    [StringLength(100, MinimumLength = 2)]
    [Column("razonSocial")]
    public string RazonSocial { get; set; } = string.Empty;

    [Required(ErrorMessage ="El nombre comercial es obligatorio")]
    [StringLength(100)]
    [Column("nombreComercial")]
    public string NombreComercial { get; set; }= string.Empty;

    /*Datos que se pueden completarr post registro, pero que antes de crear el evento 
    debe estar completado en su totalidad para que los runners puedan ver datos 
    del organizador*/
    // Se completa post registro, pero antes de crear un evento (requisito)

    [StringLength(30)]
    [Column("cuit_taxid")]
    public string? CuitTaxId { get; set; }

    [StringLength(255)]
    [Column("direccionLegal")]
    public string? DireccionLegal { get; set; }

    // Navegacion
    [ForeignKey("IdUsuario")]
    public Usuario Usuario { get; set; } = null!;
  }  



}