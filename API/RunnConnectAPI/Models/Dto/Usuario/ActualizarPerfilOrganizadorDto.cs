// Models/Dto/Usuario/ActualizarPerfilOrganizadorDto.cs
using System.ComponentModel.DataAnnotations;

namespace RunnConnectAPI.Models.Dto.Usuario
{

  /*DTO para actualizar perfil de Organizador - actualiza campos en usuarios + perfiles_organizadoes
  El avatar se actualiza con el endpoint separado PUT /api/Usuarios/Avatar
  Todos los campos son requeridos para poder crear eventos*/
  
  public class ActualizarPerfilOrganizadorDto
  {
    /*Campos tabla usuarios*/
    [Required(ErrorMessage = "El nombre comercial es obligatorio")]
    [StringLength(100, MinimumLength =3, ErrorMessage = "El nombre debe tener mas de 3 caracteres")]
    public string Nombre { get; set; } = string.Empty;

    [Required(ErrorMessage = "El telefono es obligatorio")]
    [StringLength(20, MinimumLength = 7, ErrorMessage = "El telefono debe tener entre 7 y 20 digitos")]
    public string Telefono { get; set; }= string.Empty;


    /*Campos tabla perfiles_organizadors*/
    [Required(ErrorMessage = "La razón social es obligatoria")]
    [StringLength(100, MinimumLength = 2, ErrorMessage = "La razón social debe tener entre 2 y 100 caracteres")]
    public string RazonSocial { get; set; } = string.Empty;

    [Required(ErrorMessage ="El nombre comercial es obligatorio")]
    [StringLength(100, ErrorMessage = "El nombre comercial no puede exceder 100 caracteres")]
    public string NombreComercial { get; set; }=string.Empty;

    [Required(ErrorMessage = "El CUIT es obligatorio")]
    [StringLength(30, MinimumLength = 11, ErrorMessage = "El CUIT debe tener entre 11 y 30 caracteres")]
    [RegularExpression(@"^\d{2}-\d{8}-\d{1}$|^\d{11}$", ErrorMessage = "El CUIT debe tener formato XX-XXXXXXXX-X o 11 digitos")]
    public string CuitTaxId { get; set; } = string.Empty;    

    [Required(ErrorMessage ="La direccion es obligatoria")]
    [StringLength(255, ErrorMessage = "La dirección legal no puede exceder 255 caracteres")]
    public string DireccionLegal { get; set; }=string.Empty;

  }
}