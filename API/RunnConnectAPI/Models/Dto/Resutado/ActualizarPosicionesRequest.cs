// Models/Dto/Resultado/ActualizarPosicionesRequest.cs
using System.ComponentModel.DataAnnotations;

namespace RunnConnectAPI.Models.Dto.Resultado
{
  /*DTO para que el organizador actualice las posiciones de un resultado
  PUT: api/Resultado/{id}/Posiciones*/

  public class ActualizarPosicionesRequest
  {
    /*Posicion general en toda la carrera*/
    [Range(1, 10000, ErrorMessage = "La posicion general debe ser mayor a 0")]
    public int? PosicionGeneral { get; set; }

    /*Posicion dentro de su categoria*/
    [Range(1,10000,ErrorMessage ="La posicion en categoria debe ser mayor a 0")]
    public int? PosicionCategoria {get;set;}

  }
}