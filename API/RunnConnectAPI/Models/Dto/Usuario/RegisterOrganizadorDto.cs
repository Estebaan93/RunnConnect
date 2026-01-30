// Models/Dto/Usuario/RegisterOrganizadorDto.cs

using System.ComponentModel.DataAnnotations;

namespace RunnConnectAPI.Models.Dto.Usuario
{
  /*Para registro de organizadores - se crean registros en usuarios + perfiles_organizadores
  Este registro sera un registro rapido solo campos esenciales. Post registro el organizador
  debe completar su perfil para poder crear un evento en "completar perfil"*/
public class RegisterOrganizadorDto
  {

    [Required(ErrorMessage = "La razon social es obligatorio")]
    [StringLength(100, MinimumLength = 2, ErrorMessage = "La razon social debe tener entre 2 y 100 caracteres")]
    public string RazonSocial { get; set; } = string.Empty;

    [Required(ErrorMessage = "El nombre de la organizacion es obligatorio")]
    [StringLength(100, MinimumLength = 2, ErrorMessage = "El nombre debe tener entre 2 y 100 caracteres")]
    public string NombreComercial { get; set; } = string.Empty;

    [Required(ErrorMessage = "El email es obligatorio")]
    [EmailAddress(ErrorMessage = "El formato del email no es valido")]
    [StringLength(50, ErrorMessage = "El email no puede exceder 50 caracteres")]
    public string Email { get; set; } = string.Empty;

    [Required(ErrorMessage = "La contraseña es obligatoria")]
    [MinLength(6, ErrorMessage = "La contraseña debe tener al menos 6 caracteres")]
    [StringLength(255, ErrorMessage = "La contraseña no puede exceder 255 caracteres")]
    public string Password { get; set; } = string.Empty;

    [Required(ErrorMessage = "La confirmacion de contraseña es obligatoria")]
    [Compare("Password", ErrorMessage = "Las contraseñas no coinciden")]
    public string ConfirmPassword { get; set; } = string.Empty;


    /*Avatar opcional - Se envia como archivo en multipart/Form-data
    Si no se envia se asigna uno por defecto*/
    public IFormFile? ImgAvatar {get; set;}

  }

}