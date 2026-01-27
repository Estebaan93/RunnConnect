// Models/Dto/Inscripcion/SubirComprobanteRequest.cs
using System.ComponentModel.DataAnnotations;
using Microsoft.AspNetCore.Http;

namespace RunnConnectAPI.Models.Dto.Inscripcion
{

  /// DTO para subir comprobante de pago
  /// PUT: api/Inscripcion/{id}/Comprobante

  public class SubirComprobanteRequest
  {
    [Required(ErrorMessage = "El comprobante de pago es obligatorio")]
    public IFormFile Comprobante { get; set; } = null!;
  }
}