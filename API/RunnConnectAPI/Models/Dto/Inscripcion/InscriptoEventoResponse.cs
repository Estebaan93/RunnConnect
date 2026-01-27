// Models/Dto/Inscripcion/InscriptoEventoResponse.cs

namespace RunnConnectAPI.Models.Dto.Inscripcion
{

  /// DTO para que el organizador vea los inscriptos a su evento

  public class InscriptoEventoResponse
  {
    public int IdInscripcion { get; set; }
    public DateTime FechaInscripcion { get; set; }
    public string EstadoPago { get; set; } = string.Empty;
    public string? TalleRemera { get; set; }
    public string? ComprobantePagoURL { get; set; }

 
    /// Información del runner inscripto
 
    public RunnerInscriptoInfo? Runner { get; set; }


    /// Nombre de la categoría a la que se inscribió

    public string NombreCategoria { get; set; } = string.Empty;
    public int IdCategoria { get; set; }
  }


  /// Información del runner para el organizador

  public class RunnerInscriptoInfo
  {
    public int IdUsuario { get; set; }
    public string Nombre { get; set; } = string.Empty;
    public string? Apellido { get; set; }
    public string Email { get; set; } = string.Empty;
    public string? Telefono { get; set; }
    public int? Dni { get; set; }
    public string? Genero { get; set; }
    public DateTime? FechaNacimiento { get; set; }
    public string? Localidad { get; set; }
    public string? NombreContactoEmergencia { get; set; }
    public string? TelefonoEmergencia { get; set; }


    /// Edad calculada del runner

    public int? Edad => FechaNacimiento.HasValue
        ? (int)((DateTime.Now - FechaNacimiento.Value).TotalDays / 365.25)
        : null;
  }
}