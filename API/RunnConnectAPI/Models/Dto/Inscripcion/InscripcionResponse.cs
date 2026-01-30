// Models/Dto/Inscripcion/InscripcionResponse.cs

namespace RunnConnectAPI.Models.Dto.Inscripcion
{
  /*DTO de respuesta con la info de la inscripcion*/

  public class InscripcionResponse
  {
    public int IdInscripcion {get;set;}
    public int IdUsuario {get;set;}
    public int IdCategoria { get; set; }
    public DateTime FechaInscripcion { get; set; }
    public string EstadoPago { get; set; } = string.Empty;
    public string? TalleRemera { get; set; }
    public bool AceptoDeslinde { get; set; }
    public string? ComprobantePagoURL { get; set; }    

    // Descripcion legible del estado
    public string EstadoDescripcion => EstadoPago switch
    {
      "pendiente" => "Pendiente de pago",
      "procesando" => "Pago en revision",
      "pagado" => "Pago confirmado",
      "rechazado"=> "Pago rechazado",
      "cancelado" => "Inscripción cancelada",
      "reembolsado" => "Pago reembolsado",
      _ => "Sin especificar"
    };

    /*Indica si la inscripcion esta activa (confirmada - pendiente)*/
    public bool EstaActiva=> EstadoPago=="pendiente" || EstadoPago=="procesando" || EstadoPago=="pagado";


  }
}