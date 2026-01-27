//Models/Dto/Usuario/SolicitarReactivacionDto.cs
using System.ComponentModel.DataAnnotations;

namespace RunnConnectAPI.Models.Dto.Usuario
{
  public class SolicitarReactivacionDto
  {
    [Required(ErrorMessage ="Em mail es obligatorio")]
    [EmailAddress(ErrorMessage ="El formato del email es invalido")]
    public string Email {get;set;} =string.Empty;
    
    [Required(ErrorMessage ="La contraseña es obligatoria")]
    public string Password {get;set;} =string.Empty;


  }

}