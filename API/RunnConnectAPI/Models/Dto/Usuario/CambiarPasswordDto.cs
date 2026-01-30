//Models/Dto/Usuario/CambiarPasswordDto.cs
using System.ComponentModel.DataAnnotations;

namespace RunnConnectAPI.Models.Dto.Usuario
{
  public class CambiarPasswordDto
  {
    [Required(ErrorMessage = "La contraseña actual es obligatoria")]
    public string PasswordActual { get; set; } = string.Empty;

    [Required(ErrorMessage = "La nueva contraseña es obligatoria")]
    [MinLength(6, ErrorMessage = "La nueva contraseña debe tener al menos 6 caracteres")]
    [StringLength(255, ErrorMessage = "La contraseña no puede exceder 255 caracteres")]
    public string NuevaPassword { get; set; } = string.Empty;

    [Required(ErrorMessage = "La confirmacion de contraseña es obligatoria")]
    [Compare("NuevaPassword", ErrorMessage = "Las contraseñas no coinciden")]
    public string ConfirmarPassword { get; set; } = string.Empty;
  }
}