//Modeles/Dto/Inscripcion/CrearInscripcionRequest.cs

using System.ComponentModel.DataAnnotations;

namespace RunnConnectAPI.Models.Dto.Inscripcion
{
  /*DTO para crear una inscripcion a un evento*/

  public class CrearInscripcionRequest
  {
    [Required(ErrorMessage ="La categoria es obligatoria")]
    public int IdCategoria {get;set;}

    /*Talle remera si el evento entrega remeras - XS, S, M ,L ,XL, XXL*/
    [RegularExpression("^(XS|S|M|L|XL|XXL)$", ErrorMessage = "El talle debe ser: XS, S, M, L, XL o XXL")]
    public string? TalleRemera { get; set; }

    /*Aceptar deslinde de responsabilidades*/
    [Required(ErrorMessage = "Debe aceptar el deslinde de responsabilidad")]
    public bool AceptoDeslinde {get;set;}

  }

}