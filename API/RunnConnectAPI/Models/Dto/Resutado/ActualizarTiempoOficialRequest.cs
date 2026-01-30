//Models/Dto/Resultado/ActualizarTiempoOficialRequest.cs
using System.ComponentModel.DataAnnotations;

namespace RunnConnectAPI.Models.Dto.Resultado
{
  // DTO para que el organizador actualice el tiempo oficial de un resultado
  // PUT: api/Resultado/{id}/TiempoOficial
  public class ActualizarTiempoOficialRequest
  {
    // Nuevo tiempo oficial. Formato: HH:MM:SS o HH:MM:SS.mmm
    [Required(ErrorMessage = "El tiempo oficial es obligatorio")]
    [StringLength(20, ErrorMessage = "El tiempo no puede exceder 20 caracteres")]
    [RegularExpression(@"^\d{2}:\d{2}:\d{2}(\.\d{1,3})?$", 
      ErrorMessage = "Formato de tiempo inválido. Use HH:MM:SS o HH:MM:SS.mmm")]
    public string TiempoOficial { get; set; } = string.Empty;
  }
}