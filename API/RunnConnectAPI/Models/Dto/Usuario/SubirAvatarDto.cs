//Models/Dto/Usuario/SubirAvatarDto.cs
using System.ComponentModel.DataAnnotations;

namespace RunnConnectAPI.Models.Dto.Usuario
{
  /// DTO para subir avatar del usuario
  public class SubirAvatarDto
  {
    [Required(ErrorMessage = "La imagen es obligatoria")]
    public IFormFile Imagen { get; set; } = null!;
  }

}