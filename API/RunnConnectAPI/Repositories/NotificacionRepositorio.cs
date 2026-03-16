// Repositories/NotificacionRepositorio.cs
using Microsoft.EntityFrameworkCore;
using RunnConnectAPI.Data;
using RunnConnectAPI.Models;
using RunnConnectAPI.Models.Dto.Notificacion;
using RunnConnectAPI.Services;

namespace RunnConnectAPI.Repositories
{
  // Repositorio para gestion de notificaciones de eventos (sistema PULL/buzon)
  public class NotificacionRepositorio
  {
    private readonly RunnersContext _context;
    private readonly FirebaseNotificacionService _firebaseService; //inyectamos firebase

    public NotificacionRepositorio(RunnersContext context, FirebaseNotificacionService firebase)
    {
      _context = context;
      _firebaseService = firebase;
    }

    // Obtiene una notificacion por ID
    public async Task<NotificacionResponse?> ObtenerPorIdAsync(int idNotificacion)
    {
      var notificacion = await _context.NotificacionesEvento
        .Include(n => n.Evento)
        .FirstOrDefaultAsync(n => n.IdNotificacion == idNotificacion);

      if (notificacion == null) return null;

      return MapearAResponse(notificacion);
    }

    // Obtiene todas las notificaciones de un evento
    // Ordenadas por fecha (mas recientes primero)
    public async Task<List<NotificacionResponse>> ObtenerPorEventoAsync(int idEvento)
    {
      var notificaciones = await _context.NotificacionesEvento
        .AsNoTracking()
        .Include(n => n.Evento)
        .Where(n => n.IdEvento == idEvento)
        .OrderByDescending(n => n.FechaEnvio)
        .ToListAsync();

      return notificaciones.Select(MapearAResponse).ToList();
    }

    // Obtiene las notificaciones para el runner autenticado
    // Busca notificaciones de eventos donde esta inscripto (confirmado)
    public async Task<MisNotificacionesResponse> ObtenerMisNotificacionesAsync(int idUsuario)
    {
      // 1. Obtener Inscripciones del usuario
      var inscripcionesPagadas = await _context.Inscripciones
        .Include(i => i.Categoria)
        .Where(i => i.IdUsuario == idUsuario && i.EstadoPago == "pagado")
        .Select(i => new { i.Categoria!.IdEvento, i.Categoria.IdCategoria })
        .ToListAsync();

      // IDs de mis eventos
      var idEventosPagados = inscripcionesPagadas.Select(x => x.IdEvento).Distinct().ToList();
      var idCategoriasPagadas = inscripcionesPagadas.Select(x => x.IdCategoria).Distinct().ToList();

      // FECHA LIMITE PARA GLOBALES (Ej: Solo mostrar anuncios globales de los ultimos 7 dias)
      // Esto evita que un usuario nuevo vea notificaciones de eventos de hace 2 años.
      var fechaLimiteGlobal = DateTime.Now.AddDays(-7);

      // 2. QUERY PRINCIPAL
      var notificaciones = await _context.NotificacionesEvento
        .AsNoTracking()
        .Include(n => n.Evento)
        .Include(n => n.Categoria)
        .Where(n =>
    // CASO A: Es un mensaje PRIVADO para mi (Prioridad Maxima - Pagos rechazados, etc)
            n.IdUsuarioDestino == idUsuario

            ||

            // CASO B: Es un anuncio GLOBAL reciente
            (n.EsAnuncioGlobal == true && n.FechaEnvio >= fechaLimiteGlobal)

            ||

            // CASO C: Es un aviso PIBLICO GENERAL de un evento donde estoy pagado
            (n.IdUsuarioDestino == null && n.IdCategoria == null && idEventosPagados.Contains(n.IdEvento))

            ||

            // CASO D: Es un aviso ESPECIFICO de una categoria donde estoy pagado
            (n.IdUsuarioDestino == null && n.IdCategoria != null && idCategoriasPagadas.Contains(n.IdCategoria.Value))
        )
        .OrderByDescending(n => n.FechaEnvio)
        .ToListAsync();

      // 3. FILTRADO 
      var notificacionesFiltradas = notificaciones.Where(n =>
      {
        // 1. Si es PRIVADA para mi
        if (n.IdUsuarioDestino == idUsuario) return true; // NUEVO

        // 2. Si es Global
        if (n.EsAnuncioGlobal) return true;

        // 3. Si es Publica del Evento
        // Si no tiene categoria especifica
        if (n.IdCategoria == null) return true;

        // Si TIENE categoria especifica -> Debo estar en ESA categoria exacta
        return inscripcionesPagadas.Any(i => i.IdEvento == n.IdEvento && i.IdCategoria == n.IdCategoria);
      }).ToList();

      // 4. Mapeo final 
      var items = notificacionesFiltradas.Select(n => new NotificacionRunnerItem
      {
        IdNotificacion = n.IdNotificacion,
        // Agregamos prefijo visual si es global
        Titulo = n.EsAnuncioGlobal ? $"{n.Titulo}" : (n.Categoria != null ? $"[{n.Categoria.Nombre}] {n.Titulo}" : n.Titulo),
        Mensaje = n.Mensaje,
        FechaEnvio = n.FechaEnvio,
        IdEvento = n.IdEvento,
        NombreEvento = n.Evento?.Nombre ?? "",
        FechaEvento = n.Evento?.FechaHora ?? DateTime.MinValue,
        EstadoEvento = n.EstadoEvento ?? n.Evento?.Estado ?? "",

        EsPrivada = n.IdUsuarioDestino !=null

      }).ToList();

      return new MisNotificacionesResponse
      {
        TotalNotificaciones = items.Count,
        Notificaciones = items
      };
    }

    // NUEVO METODO PARA CREAR GLOBAL
    // Usado por EventoController al crear un evento nuevo
    public async Task CrearNotificacionGlobalAsync(CrearNotificacionRequest request)
    {
      var notificacion = new NotificacionEvento
      {
        IdEvento = request.IdEvento,
        IdCategoria = null, // Global no tiene categoria especifica
        Titulo = request.Titulo.Trim(),
        Mensaje = request.Mensaje?.Trim(),
        FechaEnvio = DateTime.Now,
        EstadoEvento = "publicado",
        EsAnuncioGlobal = true // 
      };

      _context.NotificacionesEvento.Add(notificacion);
      await _context.SaveChangesAsync();

      //Disparar a firebase
      await _firebaseService.EnviarNotificacionTopicAsync("topico_global", request.Titulo, request.Mensaje, request.IdEvento);
    }

    // Obtiene notificaciones recientes (ultimas 24 horas) para el runner
    // util para mostrar contador en la app
    public async Task<int> ContarNotificacionesRecientesAsync(int idUsuario)
    {
      // Fecha ultima lectura del runner
      var perfil = await _context.PerfilesRunners
            .FirstOrDefaultAsync(p => p.IdUsuario == idUsuario);

      var ultimaLectura = perfil?.FechaUltimaLectura ?? DateTime.MinValue;

      // 1. Obtener Inscripciones (Evento y Categoria)
      var inscripcionesUsuario = await _context.Inscripciones
        .Include(i => i.Categoria)
        .Where(i => i.IdUsuario == idUsuario && i.EstadoPago == "pagado")
        .Select(i => new { i.Categoria!.IdEvento, i.Categoria.IdCategoria })
        .ToListAsync();

      if (!inscripcionesUsuario.Any()) return 0;

      var idsEventos = inscripcionesUsuario.Select(x => x.IdEvento).Distinct().ToList();

      // 2. Traer candidatos de la base de datos (filtro por fecha y evento)
      var notificacionesCandidatas = await _context.NotificacionesEvento
        .Where(n => idsEventos.Contains(n.IdEvento) && n.FechaEnvio >= ultimaLectura)
        .Select(n => new { n.IdEvento, n.IdCategoria }) // Solo necesitamos IDs para contar
        .ToListAsync();

      // 3. Contar aplicando la logica de categoria
      var cantidad = notificacionesCandidatas.Count(n =>
      {
        if (n.IdCategoria == null) return true; // Global
        return inscripcionesUsuario.Any(i => i.IdEvento == n.IdEvento && i.IdCategoria == n.IdCategoria);
      });

      return cantidad;
    }

    // METODO NUEVO: MARCAR TODO COMO LEIDO
    // Se llama cuando el usuario abre la pantalla de notificaciones
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

    // Cuenta notificaciones de un evento
    public async Task<int> ContarPorEventoAsync(int idEvento)
    {
      return await _context.NotificacionesEvento
        .CountAsync(n => n.IdEvento == idEvento);
    }


    // Crea una nueva notificacion (Organizador)
    public async Task<(NotificacionEvento? notificacion, string? error)> CrearAsync(CrearNotificacionRequest request, int idOrganizador, int? idUsuarioDestino = null)
    {
      // Verificar que el evento existe
      var evento = await _context.Eventos
        .Include(e => e.Categorias)
        .FirstOrDefaultAsync(e => e.IdEvento == request.IdEvento);

      if (evento == null)
        return (null, "El evento no existe");

      // Verificar que el organizador es dueño del evento
      if (evento.IdOrganizador != idOrganizador)
        return (null, "No tienes permiso para crear notificaciones en este evento");

      // Verificar que el evento no este cancelado 
      // if (evento.Estado == "cancelado")
      //   return (null, "No se pueden crear notificaciones en eventos cancelados");

      var notificacion = new NotificacionEvento
      {
        IdEvento = request.IdEvento,
        IdCategoria = request.IdCategoria,
        IdUsuarioDestino = idUsuarioDestino,
        Titulo = request.Titulo.Trim(),
        Mensaje = request.Mensaje?.Trim(),
        FechaEnvio = DateTime.Now,
        EstadoEvento = evento.Estado
      };

      _context.NotificacionesEvento.Add(notificacion);
      await _context.SaveChangesAsync();

      //Disparar a firebase
      string notif = DeterminarNotificacion(request.IdEvento, request.IdCategoria, idUsuarioDestino);
      await _firebaseService.EnviarNotificacionTopicAsync(notif, request.Titulo, request.Mensaje, request.IdEvento);

      return (notificacion, null);
    }

    // Actualiza una notificacion existente (Organizador)
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


    //  HELPERS 

    // Metodo para auxiliar mensajes
    private string DeterminarNotificacion (int idEvento, int? idCategoria, int? idUsuarioDestino)
    {
      // Caso 1: Notificacion privada a un solo Runner (Ej: tu pago fue aprobado!)
        if (idUsuarioDestino.HasValue)
            return $"runner_{idUsuarioDestino.Value}";

        // Caso 2: Notificacion a los inscriptos de una categoria especifica (Ej: 10K Retrasada)
        if (idCategoria.HasValue)
            return $"evento_{idEvento}_cat_{idCategoria.Value}";

        // Caso 3: Notificacion a todos los inscriptos de un evento general (Ej: Evento Cancelado)
        return $"evento_{idEvento}";
    }

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
        EsPrivada = notificacion.IdUsuarioDestino != null,

        Evento = notificacion.Evento != null
          ? new EventoNotificacionInfo
          {
            IdEvento = notificacion.Evento.IdEvento,
            Nombre = notificacion.Evento.Nombre,
            FechaHora = notificacion.Evento.FechaHora,
            Lugar = notificacion.Evento.Lugar,
            Estado = notificacion.EstadoEvento ?? notificacion.Evento?.Estado ?? ""
          }
          : null
      };
    }

  }
}