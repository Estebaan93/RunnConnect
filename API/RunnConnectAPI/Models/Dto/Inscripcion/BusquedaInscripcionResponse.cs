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

    // Datos del Runner
    public RunnerSimpleDto Runner { get; set; } = new RunnerSimpleDto();
  }

  public class RunnerSimpleDto
  {
    public string Nombre { get; set; } = string.Empty;
    public string Apellido { get; set; } = string.Empty;
    public string Dni { get; set; } = string.Empty;
    public string Email { get; set; } = string.Empty;

    // Propiedad calculada útil para mostrar en listas
    public string NombreCompleto => $"{Nombre} {Apellido}".Trim();



  }
}