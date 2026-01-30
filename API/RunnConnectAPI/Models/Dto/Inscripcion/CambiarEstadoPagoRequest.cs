// Models/Dto/Inscripcion/CambiarEstadoPagoRequest.cs
using System.ComponentModel.DataAnnotations;

namespace RunnConnectAPI.Models.Dto.Inscripcion
{

  /// DTO para que el organizador cambie el estado de pago
  /// PUT: api/Inscripcion/{id}/EstadoPago

  public class CambiarEstadoPagoRequest
  {
    [Required(ErrorMessage = "El nuevo estado es obligatorio")]
    [RegularExpression("^(pagado|rechazado)$", 
      ErrorMessage = "El estado debe ser: pagado o rechazado")]
    public string NuevoEstado { get; set; } = string.Empty;


    /// Motivo opcional (util para rechazos)
    /// Ejemplo: "Comprobante ilegible", "Monto incorrecto"

    [StringLength(500, ErrorMessage = "El motivo no puede exceder 500 caracteres")]
    public string? Motivo { get; set; }
  }
}