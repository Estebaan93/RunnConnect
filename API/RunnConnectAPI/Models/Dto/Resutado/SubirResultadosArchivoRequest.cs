//Models/Dto/Resultado/SubirResultadosArchivoRequest.cs
using System.ComponentModel.DataAnnotations;
using Microsoft.AspNetCore.Http;

namespace RunnConnectAPI.Models.Dto.Resultado
{
  public class SubirResultadosArchivoRequest
  {
    [Required]
    public int IdEvento {get;set;}

    [Required]
    public IFormFile Archivo {get;set;}= null; //Archivo fisico .csv lo provee la entidad que cronometra al evento 

  }
}