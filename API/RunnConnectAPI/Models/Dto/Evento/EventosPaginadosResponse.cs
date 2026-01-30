//Models/Dto/Evento/EventosPaginadosResponse.cs

namespace RunnConnectAPI.Models.Dto.Evento
{
  /*DTO de respuesta para la lista de eventos*/
  public class EventosPaginadosResponse
  {
    public List<EventoResumenResponse> Eventos { get; set; } = new();
    public int PaginaActual { get; set; }
    public int TotalPaginas { get; set; }
    public int TotalEventos { get; set; }
    public int TamanioPagina { get; set; }
    public bool TienePaginaAnterior => PaginaActual > 1;
    public bool TienePaginaSiguiente => PaginaActual < TotalPaginas;
  }
}