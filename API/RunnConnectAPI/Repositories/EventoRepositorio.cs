//Repositories/EventoRepositorio.cs
using Microsoft.EntityFrameworkCore;
using RunnConnectAPI.Data;
using RunnConnectAPI.Models;

namespace RunnConnectAPI.Repositories
{
  /// Repositorio para gestion de eventos
  public class EventoRepositorio
  {
    private readonly RunnersContext _context;

    public EventoRepositorio(RunnersContext context)
    {
      _context = context;
    }

    // Consultas publicas
    /// Obtiene eventos publicados y futuros (para listado publico/runners)
    /*public async Task<List<Evento>> ObtenerEventosPublicadosAsync()
    {
      return await _context.Eventos
          .Include(e=>e.Organizador)
          .Include(e=>e.Categorias)
            .ThenInclude(c=>c.Inscripciones)
          .Where(e => e.Estado == "publicado" && e.FechaHora >= DateTime.Now)
          .OrderBy(e => e.FechaHora)
          .ToListAsync();
    }*/
    //Para paginado
    public async Task<(List<Evento> eventos, int totalCount)> ObtenerEventosPublicadosAsync(int pagina, int tamanioPagina)
    {
      // 1. Query base (Solo publicados y futuros)
      var query = _context.Eventos
          .AsNoTracking()
          .Where(e => e.Estado == "publicado" && e.FechaHora >= DateTime.Now);

      // 2. Contar total real
      var totalCount = await query.CountAsync();

      // 3. Obtener página
      var eventos = await query
          .Include(e => e.Organizador)
          .Include(e => e.Categorias)
              .ThenInclude(c => c.Inscripciones)
          .OrderBy(e => e.FechaHora) // Orden: Los más próximos primero
          .Skip((pagina - 1) * tamanioPagina)
          .Take(tamanioPagina)
          .ToListAsync();

      return (eventos, totalCount);
    }

    /// Obtiene un evento por ID con informacion del organizador
    public async Task<Evento?> ObtenerPorIdAsync(int id)
    {
      return await _context.Eventos
          .Include(e => e.Organizador)
          .FirstOrDefaultAsync(e => e.IdEvento == id);
    }

    /// Obtiene un evento por ID con todas sus relaciones (detalle completo)
    public async Task<Evento?> ObtenerPorIdConDetalleAsync(int id)
    {
      return await _context.Eventos
          .Include(e => e.Organizador)
            .ThenInclude(o => o!.PerfilOrganizador)
          .Include(e => e.Categorias)
            .ThenInclude(c=>c.Inscripciones)
          .FirstOrDefaultAsync(e => e.IdEvento == id);
    }


    /// Obtiene eventos publicos de un organizador (ultimos 6 meses)
    /// Para mostrar en el perfil publico del organizador
    public async Task<List<Evento>> ObtenerPorOrganizadorPublicoAsync(int idOrganizador)
    {
      var fechaLimite = DateTime.Now.AddMonths(-6);
      var estadosVisibles = new[] { "publicado", "finalizado" };

      return await _context.Eventos
          .Where(e => e.IdOrganizador == idOrganizador
              && e.FechaHora >= fechaLimite
              && estadosVisibles.Contains(e.Estado))
          .OrderByDescending(e => e.FechaHora)
          .ToListAsync();
    }


    //Consultas para Organizadores
    /// Obtiene TODOS los eventos de un organizador (para gestion)
    /// Incluye todos los estados
    /// Incluye paginacion para que traiga dinamicamente los eventos
    /*public async Task<List<Evento>> ObtenerTodosPorOrganizadorAsync(int idOrganizador)
    {
      return await _context.Eventos
          .Include(e=>e.Organizador)
          .Include(e=>e.Categorias)
            .ThenInclude(c=>c.Inscripciones)
          .Where(e => e.IdOrganizador == idOrganizador)
          .OrderByDescending(e => e.FechaHora)
          .ToListAsync();
    }*/
    public async Task<(List<Evento> eventos, int totalCount)> ObtenerTodosPorOrganizadorAsync(int idOrganizador, int pagina, int tamanioPagina)
        {
            // 1. Query base (Filtrar por organizador)
            var query = _context.Eventos
                .AsNoTracking()
                .Where(e => e.IdOrganizador == idOrganizador);

            // 2. Contar total
            var totalCount = await query.CountAsync();

            // 3. Obtener página
            var eventos = await query
                .Include(e => e.Organizador)
                .Include(e => e.Categorias)
                    .ThenInclude(c => c.Inscripciones)
                .OrderByDescending(e => e.FechaHora) // Orden: Los más nuevos primero
                .Skip((pagina - 1) * tamanioPagina)
                .Take(tamanioPagina)
                .ToListAsync();

            return (eventos, totalCount);
        }


    /// Verifica si un evento pertenece a un organizador especifico
    public async Task<bool> PerteneceAOrganizadorAsync(int idEvento, int idOrganizador)
    {
      return await _context.Eventos
          .AnyAsync(e => e.IdEvento == idEvento && e.IdOrganizador == idOrganizador);
    }



    // Operaciones CRUD

    // Crea un nuevo evento con estado "publicado"
    public async Task<Evento> CrearAsync(Evento evento)
    {
      // Normalizar datos
      evento.Nombre = evento.Nombre.Trim();
      evento.Lugar = evento.Lugar.Trim();
      evento.Descripcion = evento.Descripcion?.Trim();
      evento.DatosPago = evento.DatosPago?.Trim();
      evento.Estado = "publicado";

      _context.Eventos.Add(evento);
      await _context.SaveChangesAsync();

      return evento;
    }

    // Actualiza un evento existente
    public async Task ActualizarAsync(Evento evento)
    {
      // Normalizar datos antes de actualizar
      evento.Nombre = evento.Nombre.Trim();
      evento.Lugar = evento.Lugar.Trim();
      evento.Descripcion = evento.Descripcion?.Trim();
      evento.DatosPago = evento.DatosPago?.Trim();

      _context.Eventos.Update(evento);
      await _context.SaveChangesAsync();
    }

    /// Cambia el estado de un evento con validaciones de negocio
    
    public async Task CambiarEstadoAsync(int idEvento, string nuevoEstado)
    {
      var evento = await _context.Eventos.FindAsync(idEvento);

      if (evento == null)
        throw new KeyNotFoundException("Evento no encontrado");

      nuevoEstado = nuevoEstado.ToLower().Trim();

      // Validar estado valido
      var estadosValidos = new[] { "publicado", "cancelado", "finalizado", "suspendido", "retrasado" };
      
      if (!estadosValidos.Contains(nuevoEstado))
        throw new ArgumentException($"Estado inválido. Estados válidos: {string.Join(", ", estadosValidos)}");

      // Validaciones de logica de negocio
      ValidarTransicionEstado(evento, nuevoEstado);

      evento.Estado = nuevoEstado;
      await _context.SaveChangesAsync();
    }


    // Metodos de Conveniencia para estados

    public async Task PublicarAsync(int idEvento)
        => await CambiarEstadoAsync(idEvento, "publicado");

    public async Task CancelarAsync(int idEvento)
        => await CambiarEstadoAsync(idEvento, "cancelado");

    public async Task FinalizarAsync(int idEvento)
        => await CambiarEstadoAsync(idEvento, "finalizado");



    // Validaciones privadas

    /// Valida las reglas de negocio para transiciones de estado
    private void ValidarTransicionEstado(Evento evento, string nuevoEstado)
    {
      var estadoActual = evento.Estado;

      // No permitir cambios si ya esta cancelado
      if (estadoActual == "cancelado")
        throw new InvalidOperationException("No se puede cambiar el estado de un evento cancelado");

      // No permitir volver a publicado desde finalizado
      if (estadoActual == "finalizado")
        throw new InvalidOperationException("No se puede modificar un evento ya finalizado (concluido)");

      // No se puede finalizar un evento que aun no ocurrio
      if (nuevoEstado == "finalizado" && evento.FechaHora > DateTime.Now)
        throw new InvalidOperationException("No se puede finalizar un evento que aún no ha ocurrido");

      // No se puede publicar un evento con fecha pasada
      if (nuevoEstado == "publicado" && evento.FechaHora < DateTime.Now)
        throw new InvalidOperationException("No se puede publicar un evento con fecha pasada");
    }


    // Consultas con Paginacion (para bssqueda avanzada)

    /// Busca eventos con filtros y paginacion
    public async Task<(List<Evento> eventos, int totalCount)> BuscarConFiltrosAsync(
        string? nombre = null,
        string? lugar = null,
        DateTime? fechaDesde = null,
        DateTime? fechaHasta = null,
        string? estado = null,
        int? idOrganizador = null,
        int pagina = 1,
        int tamanioPagina = 10)
    {
      var query = _context.Eventos.AsQueryable();

      // Aplicar filtros
      if (!string.IsNullOrWhiteSpace(nombre))
        query = query.Where(e => e.Nombre.Contains(nombre.Trim()));

      if (!string.IsNullOrWhiteSpace(lugar))
        query = query.Where(e => e.Lugar.Contains(lugar.Trim()));

      if (fechaDesde.HasValue)
        query = query.Where(e => e.FechaHora >= fechaDesde.Value);

      if (fechaHasta.HasValue)
        query = query.Where(e => e.FechaHora <= fechaHasta.Value);

      if (!string.IsNullOrWhiteSpace(estado))
        query = query.Where(e => e.Estado == estado.ToLower().Trim());

      if (idOrganizador.HasValue)
        query = query.Where(e => e.IdOrganizador == idOrganizador.Value);

      // Contar total antes de paginar
      var totalCount = await query.CountAsync();

      // Aplicar paginacion
      var eventos = await query
          .Include(e=>e.Organizador)
          .Include(e=>e.Categorias)
            .ThenInclude(c=>c.Inscripciones)
          .OrderByDescending(e => e.FechaHora)
          .Skip((pagina - 1) * tamanioPagina)
          .Take(tamanioPagina)
          .ToListAsync();

      return (eventos, totalCount);
    }

    // Estadisticas

    /// Cuenta inscriptos en un evento (todas las categorias)
    public async Task<int> ContarInscriptosAsync(int idEvento)
    {
      // Obtener IDs de categorías del evento
      var categoriasIds = await _context.CategoriasEvento
          .Where(c => c.IdEvento == idEvento)
          .Select(c => c.IdCategoria)
          .ToListAsync();

      // Contar inscripciones confirmadas en esas categorias
      return await _context.Inscripciones
          .CountAsync(i => categoriasIds.Contains(i.IdCategoria)
              && i.EstadoPago == "pagado");
    }

    /// Cuenta inscriptos en una categoria especifica
    public async Task<int> ContarInscriptosPorCategoriaAsync(int idCategoria)
    {
      return await _context.Inscripciones
          .CountAsync(i => i.IdCategoria == idCategoria && i.EstadoPago == "pagado");
    }

    /// Verifica si hay cupo disponible en el evento
    public async Task<bool> TieneCupoDisponibleAsync(int idEvento)
    {
      var evento = await _context.Eventos.FindAsync(idEvento);

      if (evento == null)
        return false;

      // Si no tiene limite de cupo, siempre hay disponible
      if (!evento.CupoTotal.HasValue)
        return true;

      var inscriptos = await ContarInscriptosAsync(idEvento);
      return inscriptos < evento.CupoTotal.Value;
    }


    /// Verifica si hay cupo disponible en una categoría

    public async Task<bool> TieneCupoDisponibleEnCategoriaAsync(int idCategoria)
    {
      var categoria = await _context.CategoriasEvento.FindAsync(idCategoria);

      if (categoria == null)
        return false;

      // Si no tiene límite de cupo, siempre hay disponible
      if (!categoria.CupoCategoria.HasValue)
        return true;

      var inscriptos = await ContarInscriptosPorCategoriaAsync(idCategoria);
      return inscriptos < categoria.CupoCategoria.Value;
    }
  }
}