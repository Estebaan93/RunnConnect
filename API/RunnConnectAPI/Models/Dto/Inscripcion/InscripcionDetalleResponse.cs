// Models/Dto/Inscripcion/InscripcionDetalleResponse.cs

namespace RunnConnectAPI.Models.Dto.Inscripcion
{

  // DTO de respuesta con informacion completa de inscripcion (incluye evento y categoria)

  public class InscripcionDetalleResponse
  {
    public int IdInscripcion { get; set; }
    public DateTime FechaInscripcion { get; set; }
    public string EstadoPago { get; set; } = string.Empty;
    public string? TalleRemera { get; set; }
    public bool AceptoDeslinde { get; set; }
    public string? ComprobantePagoURL { get; set; }

    
    /// Informacion del evento

    public EventoInscripcionInfo? Evento { get; set; }


    /// Informacion de la categoria

    public CategoriaInscripcionInfo? Categoria { get; set; }


    /// Descripcion legible del estado

    public string EstadoDescripcion => EstadoPago switch
    {
      "pendiente" => "Pendiente de pago",
      "procesando" => "Pago en revisión",
      "pagado" => "Pago confirmado",
      "rechazado" => "Pago rechazado",
      "cancelado" => "Inscripción cancelada",
      "reembolsado" => "Pago reembolsado",
      _ => "Sin especificar"
    };

    public bool EstaActiva => EstadoPago == "pendiente" || EstadoPago == "procesando" || EstadoPago=="pagado";
  }


  /// Informacion resumida del evento para mostrar en inscripcion

  public class EventoInscripcionInfo
  {
    public int IdEvento { get; set; }
    public string Nombre { get; set; } = string.Empty;
    public DateTime FechaHora { get; set; }
    public string Lugar { get; set; } = string.Empty;
    public string Estado { get; set; } = string.Empty;
    public string? DatosPago { get; set; }
  }


  /// Informacion resumida de la categoria para mostrar en inscripcion

  public class CategoriaInscripcionInfo
  {
    public int IdCategoria { get; set; }
    public string Nombre { get; set; } = string.Empty;
    public decimal CostoInscripcion { get; set; }
    public string Genero { get; set; } = string.Empty;
    public int EdadMinima { get; set; }
    public int EdadMaxima { get; set; }
  }
}