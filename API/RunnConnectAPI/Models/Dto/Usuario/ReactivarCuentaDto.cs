//Models/Dto/Usuario/ReactivarCuentaDto.cs
using System.ComponentModel.DataAnnotations;

namespace RunnConnectAPI.Models.Dto.Usuario
{
  public class ReactivarCuentaDto
  {
    [Required(ErrorMessage ="El token es obligatorio")]
    public string Token {get;set;}= string.Empty;
  }
}