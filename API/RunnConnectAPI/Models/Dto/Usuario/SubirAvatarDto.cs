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

  /// DTO para actualizar avatar con URL
  public class ActualizarAvatarUrlDto
  {
    [Required(ErrorMessage = "La URL del avatar es obligatoria")]
    [StringLength(500, ErrorMessage = "La URL no puede exceder 500 caracteres")]
    [Url(ErrorMessage = "La URL no es valida")]
    public string ImgAvatar { get; set; } = string.Empty;
  }
}