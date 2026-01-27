// Repositories/NotificacionRepositorio.cs
using Microsoft.EntityFrameworkCore;
using RunnConnectAPI.Data;
using RunnConnectAPI.Models;
using RunnConnectAPI.Models.Dto.Notificacion;

namespace RunnConnectAPI.Repositories
{
  /// Repositorio para gestion de notificaciones de eventos (sistema PULL/buzón)
  public class NotificacionRepositorio
  {
    private readonly RunnersContext _context;

    public NotificacionRepositorio(RunnersContext context)
    {
      _context = context;
    }

    // ═══════════════════ QUERIES ═══════════════════

    /// Obtiene una notificacion por ID
    public async Task<NotificacionResponse?> ObtenerPorIdAsync(int idNotificacion)
    {
      var notificacion = await _context.NotificacionesEvento
        .Include(n => n.Evento)
        .FirstOrDefaultAsync(n => n.IdNotificacion == idNotificacion);

      if (notificacion == null) return null;

      return MapearAResponse(notificacion);
    }

    /// Obtiene todas las notificaciones de un evento
    /// Ordenadas por fecha (mas recientes primero)
    public async Task<List<NotificacionResponse>> ObtenerPorEventoAsync(int idEvento)
    {
      var notificaciones = await _context.NotificacionesEvento
        .Include(n => n.Evento)
        .Where(n => n.IdEvento == idEvento)
        .OrderByDescending(n => n.FechaEnvio)
        .ToListAsync();

      return notificaciones.Select(MapearAResponse).ToList();
    }

    /// Obtiene las notificaciones para el runner autenticado
    /// Busca notificaciones de eventos donde esta inscripto (confirmado)
    public async Task<MisNotificacionesResponse> ObtenerMisNotificacionesAsync(int idUsuario)
    {
      // Obtener IDs de eventos donde el runner esta inscripto con pago confirmado
      var eventosInscripto = await _context.Inscripciones
        .Include(i => i.Categoria)
        .Where(i => i.IdUsuario == idUsuario && i.EstadoPago == "pagado")
        .Select(i => i.Categoria!.IdEvento)
        .Distinct()
        .ToListAsync();

      if (!eventosInscripto.Any())
      {
        return new MisNotificacionesResponse
        {
          TotalNotificaciones = 0,
          Notificaciones = new List<NotificacionRunnerItem>()
        };
      }

      // Obtener notificaciones de esos eventos
      var notificaciones = await _context.NotificacionesEvento
        .Include(n => n.Evento)
        .Where(n => eventosInscripto.Contains(n.IdEvento))
        .OrderByDescending(n => n.FechaEnvio)
        .ToListAsync();

      var items = notificaciones.Select(n => new NotificacionRunnerItem
      {
        IdNotificacion = n.IdNotificacion,
        Titulo = n.Titulo,
        Mensaje = n.Mensaje,
        FechaEnvio = n.FechaEnvio,
        IdEvento = n.IdEvento,
        NombreEvento = n.Evento?.Nombre ?? "",
        FechaEvento = n.Evento?.FechaHora ?? DateTime.MinValue,
        EstadoEvento = n.Evento?.Estado ?? ""
      }).ToList();

      return new MisNotificacionesResponse
      {
        TotalNotificaciones = items.Count,
        Notificaciones = items
      };
    }

    /// Obtiene notificaciones recientes (últimas 24 horas) para el runner
    /// Útil para mostrar badge/contador en la app
    public async Task<int> ContarNotificacionesRecientesAsync(int idUsuario)
    {
      //Fecha ultima lectura del runner
      var perfil = await _context.PerfilesRunners
            .FirstOrDefaultAsync(p => p.IdUsuario == idUsuario);

      //Si nunca entro asumimos que es una fecha muy vieja (todo nuevo)
      var ultimaLectura = perfil?.FechaUltimaLectura ?? DateTime.MinValue;

      // Obtener IDs de eventos donde el runner esta inscripto
      var eventosInscripto = await _context.Inscripciones
        .Include(i => i.Categoria)
        .Where(i => i.IdUsuario == idUsuario && i.EstadoPago == "pagado")
        .Select(i => i.Categoria!.IdEvento)
        .Distinct()
        .ToListAsync();

      if (!eventosInscripto.Any())
        return 0;

      //Contar notificaciones enviadas despues de su ultima lectura
      return await _context.NotificacionesEvento
        .CountAsync(n => eventosInscripto.Contains(n.IdEvento) && n.FechaEnvio >= ultimaLectura);
    }

    /// METODO NUEVO: MARCAR TODO COMO LEÍDO
    /// Se llama cuando el usuario abre la pantalla de notificaciones
    public async Task MarcarTodasComoLeidasAsync(int idUsuario)
    {
        var perfil = await _context.PerfilesRunners
            .FirstOrDefaultAsync(p => p.IdUsuario == idUsuario);

        if (perfil != null)
        {
            // Actualizamos la fecha a "Ahora"
            perfil.FechaUltimaLectura = DateTime.Now;
            await _context.SaveChangesAsync();
        }
    }

    /// Cuenta notificaciones de un evento
    public async Task<int> ContarPorEventoAsync(int idEvento)
    {
      return await _context.NotificacionesEvento
        .CountAsync(n => n.IdEvento == idEvento);
    }


    // ═══════════════════ COMMANDS ═══════════════════

    /// Crea una nueva notificacion (Organizador)
    public async Task<(NotificacionEvento? notificacion, string? error)> CrearAsync(
      CrearNotificacionRequest request, int idOrganizador)
    {
      // Verificar que el evento existe
      var evento = await _context.Eventos
        .FirstOrDefaultAsync(e => e.IdEvento == request.IdEvento);

      if (evento == null)
        return (null, "El evento no existe");

      // Verificar que el organizador es dueño del evento
      if (evento.IdOrganizador != idOrganizador)
        return (null, "No tienes permiso para crear notificaciones en este evento");

      // Verificar que el evento no este cancelado (opcional, podrías permitirlo)
      // if (evento.Estado == "cancelado")
      //   return (null, "No se pueden crear notificaciones en eventos cancelados");

      var notificacion = new NotificacionEvento
      {
        IdEvento = request.IdEvento,
        Titulo = request.Titulo.Trim(),
        Mensaje = request.Mensaje?.Trim(),
        FechaEnvio = DateTime.Now
      };

      _context.NotificacionesEvento.Add(notificacion);
      await _context.SaveChangesAsync();

      return (notificacion, null);
    }

    /// Actualiza una notificacion existente (Organizador)
    public async Task<(bool exito, string? error)> ActualizarAsync(
      int idNotificacion, ActualizarNotificacionRequest request, int idOrganizador)
    {
      var notificacion = await _context.NotificacionesEvento
        .Include(n => n.Evento)
        .FirstOrDefaultAsync(n => n.IdNotificacion == idNotificacion);

      if (notificacion == null)
        return (false, "La notificacion no existe");

      // Verificar que el organizador es dueño del evento
      if (notificacion.Evento?.IdOrganizador != idOrganizador)
        return (false, "No tienes permiso para modificar esta notificacion");

      notificacion.Titulo = request.Titulo.Trim();
      notificacion.Mensaje = request.Mensaje?.Trim();
      // No actualizamos FechaEnvio para mantener el registro original

      await _context.SaveChangesAsync();
      return (true, null);
    }

    /// Elimina una notificacion (Organizador)
    public async Task<(bool exito, string? error)> EliminarAsync(int idNotificacion, int idOrganizador)
    {
      var notificacion = await _context.NotificacionesEvento
        .Include(n => n.Evento)
        .FirstOrDefaultAsync(n => n.IdNotificacion == idNotificacion);

      if (notificacion == null)
        return (false, "La notificacion no existe");

      // Verificar que el organizador es dueño del evento
      if (notificacion.Evento?.IdOrganizador != idOrganizador)
        return (false, "No tienes permiso para eliminar esta notificacion");

      _context.NotificacionesEvento.Remove(notificacion);
      await _context.SaveChangesAsync();

      return (true, null);
    }


    // ═══════════════════ HELPERS ═══════════════════

    /// Verifica si existe una notificacion
    public async Task<bool> ExisteAsync(int idNotificacion)
    {
      return await _context.NotificacionesEvento
        .AnyAsync(n => n.IdNotificacion == idNotificacion);
    }

    /// Verifica si el organizador es dueño de la notificacion
    public async Task<bool> EsDuenioAsync(int idNotificacion, int idOrganizador)
    {
      return await _context.NotificacionesEvento
        .Include(n => n.Evento)
        .AnyAsync(n => n.IdNotificacion == idNotificacion && 
                       n.Evento!.IdOrganizador == idOrganizador);
    }

    private NotificacionResponse MapearAResponse(NotificacionEvento notificacion)
    {
      return new NotificacionResponse
      {
        IdNotificacion = notificacion.IdNotificacion,
        IdEvento = notificacion.IdEvento,
        Titulo = notificacion.Titulo,
        Mensaje = notificacion.Mensaje,
        FechaEnvio = notificacion.FechaEnvio,
        Evento = notificacion.Evento != null
          ? new EventoNotificacionInfo
          {
            IdEvento = notificacion.Evento.IdEvento,
            Nombre = notificacion.Evento.Nombre,
            FechaHora = notificacion.Evento.FechaHora,
            Lugar = notificacion.Evento.Lugar,
            Estado = notificacion.Evento.Estado
          }
          : null
      };
    }

  }
}