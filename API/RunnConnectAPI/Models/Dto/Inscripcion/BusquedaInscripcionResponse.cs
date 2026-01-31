using System;

namespace RunnConnectAPI.Models.Dto.Inscripcion
{
  public class BusquedaInscripcionResponse
  {
    public int IdInscripcion { get; set; }
    public DateTime FechaInscripcion { get; set; }
    public string EstadoPago { get; set; }

    //datos del evento
    public int IdEvento { get; set; }
    public string NombreEvento { get; set; } = string.Empty;
    public string NombreCategoria { get; set; } = string.Empty;

    //talle remera
    public string? TalleRemera { get; set; }

    // Datos del Runner
    public RunnerSimpleDto Runner { get; set; } = new RunnerSimpleDto();
  }

  public class RunnerSimpleDto
  {
    public string Nombre { get; set; } = string.Empty;
    public string Apellido { get; set; } = string.Empty;
    public string Dni { get; set; } = string.Empty;
    public string Email { get; set; } = string.Empty;

    public string? Telefono { get; set; }
    public string? Genero { get; set; }
    public string? Localidad { get; set; }
    public string? NombreContactoEmergencia { get; set; }
    public string? TelefonoEmergencia { get; set; }

    // Propiedad calculada util para mostrar en listas
    public string NombreCompleto => $"{Nombre} {Apellido}".Trim();



  }
}