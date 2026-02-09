//Models/Dto/Categoria/CambiarEstadoCategoriaRequest.cs
using System.ComponentModel.DataAnnotations;

namespace RunnConnectAPI.Models.Dto.Categoria
{
  public class CambiarEstadoCategoriaRequest
  {
    [Required(ErrorMessage = "El nuevo estado es obligatorio")]
        // Validamos solo los estados lógicos para una sub-categoría
        [RegularExpression("^(programada|retrasada|cancelada|finalizada)$", 
            ErrorMessage = "El estado debe ser: programada, retrasada, cancelada o finalizada")]
        public string NuevoEstado { get; set; } = string.Empty;

        [StringLength(500, ErrorMessage = "El motivo no puede exceder 500 caracteres")]
        public string? Motivo { get; set; } // Para la notificacion push específica
  }
}