//Models/Dto/Usuario/RecuperarPasswordDto.cs
using System.ComponentModel.DataAnnotations;

namespace RunnConnectAPI.Models.Dto.Usuario
{
  public class RecuperarPasswordDto
  {
    [Required(ErrorMessage = "El email es obligatorio")]
    [EmailAddress(ErrorMessage = "El formato del email no es valido")]
    public string Email { get; set; } = string.Empty;
  }
}