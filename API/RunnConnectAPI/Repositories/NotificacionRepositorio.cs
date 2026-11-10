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

    // Obtiene las notificaciones para el usuario autenticado (Runner u Organizador)
    public async Task<MisNotificacionesResponse> ObtenerMisNotificacionesAsync(int idUsuario)
    {
      var usuario = await _context.Usuarios.FindAsync(idUsuario);
      bool esOrganizador = usuario?.TipoUsuario == "organizador";

      List<NotificacionEvento> notificacionesFiltradas;

      DateTime ultimaLectura = DateTime.MinValue;
      if (esOrganizador)
      {
        var perfilOrga = await _context.PerfilesOrganizadores.FirstOrDefaultAsync(p => p.IdUsuario == idUsuario);
        ultimaLectura = perfilOrga?.FechaUltimaLectura ?? DateTime.MinValue;

        // El organizador SOLO ve notificaciones privadas para él (nuevas inscripciones, comprobantes subidos, etc.)
        notificacionesFiltradas = await _context.NotificacionesEvento
          .AsNoTracking()
          .Include(n => n.Evento)
          .Include(n => n.Categoria)
          .Where(n => n.IdUsuarioDestino == idUsuario)
          .OrderByDescending(n => n.FechaEnvio)
          .ToListAsync();
      }
      else
      {
        var perfilRunner = await _context.PerfilesRunners.FirstOrDefaultAsync(p => p.IdUsuario == idUsuario);
        ultimaLectura = perfilRunner?.FechaUltimaLectura ?? DateTime.MinValue;

        var estadosValidos = new List<string> { "pagado", "procesando", "pendiente" };

        // 1. Obtener Inscripciones del usuario (pagado, procesando y pendiente)
        var inscripcionesActivas = await _context.Inscripciones
          .Include(i => i.Categoria)
          .Where(i => i.IdUsuario == idUsuario && estadosValidos.Contains(i.EstadoPago))
          .Select(i => new { i.Categoria!.IdEvento, i.Categoria.IdCategoria })
          .ToListAsync();

        // IDs de mis eventos
        var idEventosActivos = inscripcionesActivas.Select(x => x.IdEvento).Distinct().ToList();
        var idCategoriasActivas = inscripcionesActivas.Select(x => x.IdCategoria).Distinct().ToList();

        // FECHA LIMITE PARA GLOBALES (Ej: Solo mostrar anuncios globales de los ultimos 7 dias)
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

              // CASO B: Es un anuncio GLOBAL reciente (Inscripciones abiertas para runners)
              (n.EsAnuncioGlobal == true && n.FechaEnvio >= fechaLimiteGlobal)

              ||

              // CASO C: Es un aviso PUBLICO GENERAL de un evento donde estoy inscripto (pagado, procesando o pendiente)
              (n.IdUsuarioDestino == null && n.IdCategoria == null && idEventosActivos.Contains(n.IdEvento))

              ||

              // CASO D: Es un aviso ESPECIFICO de una categoria donde estoy inscripto (pagado, procesando o pendiente)
              (n.IdUsuarioDestino == null && n.IdCategoria != null && idCategoriasActivas.Contains(n.IdCategoria.Value))
          )
          .OrderByDescending(n => n.FechaEnvio)
          .ToListAsync();

        // 3. FILTRADO
        notificacionesFiltradas = notificaciones.Where(n =>
        {
          if (n.IdUsuarioDestino == idUsuario) return true;
          if (n.EsAnuncioGlobal) return true;
          if (n.IdCategoria == null) return true;
          return inscripcionesActivas.Any(i => i.IdEvento == n.IdEvento && i.IdCategoria == n.IdCategoria);
        }).ToList();
      }

      // 4. Mapeo final
      var items = notificacionesFiltradas.Select(n => new NotificacionRunnerItem
      {
        IdNotificacion = n.IdNotificacion,
        Titulo = n.EsAnuncioGlobal ? $"{n.Titulo}" : (n.Categoria != null ? $"[{n.Categoria.Nombre}] {n.Titulo}" : n.Titulo),
        Mensaje = n.Mensaje,
        FechaEnvio = n.FechaEnvio,
        IdEvento = n.IdEvento,
        NombreEvento = n.Evento?.Nombre ?? "",
        IdCategoria = n.IdCategoria,
        NombreCategoria = n.Categoria?.Nombre,
        FechaEvento = n.Evento?.FechaHora ?? DateTime.MinValue,
        EstadoEvento = n.EstadoEvento ?? n.Evento?.Estado ?? "",
        EsPrivada = n.IdUsuarioDestino != null,
        EsNueva = n.FechaEnvio >= ultimaLectura
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

    // METODO PARA CREAR NOTIFICACION DIRECTA DESDE EL SISTEMA
    public async Task<NotificacionEvento> CrearNotificacionSistemaAsync(
      int idEvento, int? idCategoria, int? idUsuarioDestino, string titulo, string mensaje, string? estadoEvento = null)
    {
      var notificacion = new NotificacionEvento
      {
        IdEvento = idEvento,
        IdCategoria = idCategoria,
        IdUsuarioDestino = idUsuarioDestino,
        Titulo = titulo.Trim(),
        Mensaje = mensaje?.Trim(),
        FechaEnvio = DateTime.Now,
        EstadoEvento = estadoEvento,
        EsAnuncioGlobal = false
      };

      _context.NotificacionesEvento.Add(notificacion);
      await _context.SaveChangesAsync();

      // Disparar push a Firebase (al usuario destino o a los runners activos del evento/categoría)
      await DispararPushNotificacionAsync(idEvento, idCategoria, idUsuarioDestino, titulo, mensaje);

      return notificacion;
    }

    // Obtiene notificaciones recientes para runner u organizador
    public async Task<int> ContarNotificacionesRecientesAsync(int idUsuario)
    {
      var usuario = await _context.Usuarios.FindAsync(idUsuario);
      bool esOrganizador = usuario?.TipoUsuario == "organizador";

      if (esOrganizador)
      {
        var perfilOrga = await _context.PerfilesOrganizadores.FirstOrDefaultAsync(p => p.IdUsuario == idUsuario);
        var ultimaLecturaOrga = perfilOrga?.FechaUltimaLectura ?? DateTime.MinValue;

        return await _context.NotificacionesEvento
          .Where(n => n.IdUsuarioDestino == idUsuario && n.FechaEnvio >= ultimaLecturaOrga)
          .CountAsync();
      }

      // Runner:
      var perfil = await _context.PerfilesRunners.FirstOrDefaultAsync(p => p.IdUsuario == idUsuario);
      var ultimaLectura = perfil?.FechaUltimaLectura ?? DateTime.MinValue;

      var estadosValidos = new List<string> { "pagado", "procesando", "pendiente" };

      var inscripcionesUsuario = await _context.Inscripciones
        .Include(i => i.Categoria)
        .Where(i => i.IdUsuario == idUsuario && estadosValidos.Contains(i.EstadoPago))
        .Select(i => new { i.Categoria!.IdEvento, i.Categoria.IdCategoria })
        .ToListAsync();

      var idsEventos = inscripcionesUsuario.Select(x => x.IdEvento).Distinct().ToList();
      var idsCategorias = inscripcionesUsuario.Select(x => x.IdCategoria).Distinct().ToList();
      var fechaLimiteGlobal = DateTime.Now.AddDays(-7);

      return await _context.NotificacionesEvento
        .Where(n => n.FechaEnvio >= ultimaLectura && (
            // Caso A: Mensaje privado para este runner
            n.IdUsuarioDestino == idUsuario
            ||
            // Caso B: Anuncio global reciente
            (n.EsAnuncioGlobal && n.FechaEnvio >= fechaLimiteGlobal)
            ||
            // Caso C: Aviso publico general del evento donde está inscripto (pagado, procesando o pendiente)
            (n.IdUsuarioDestino == null && n.IdCategoria == null && idsEventos.Contains(n.IdEvento))
            ||
            // Caso D: Aviso publico especifico de la categoría donde esta inscripto (pagado, procesando o pendiente)
            (n.IdUsuarioDestino == null && n.IdCategoria != null && idsCategorias.Contains(n.IdCategoria.Value))
        ))
        .CountAsync();
    }

    // METODO: MARCAR TODO COMO LEIDO
    public async Task MarcarTodasComoLeidasAsync(int idUsuario)
    {
      var usuario = await _context.Usuarios.FindAsync(idUsuario);
      if (usuario?.TipoUsuario == "organizador")
      {
        var perfilOrga = await _context.PerfilesOrganizadores.FirstOrDefaultAsync(p => p.IdUsuario == idUsuario);
        if (perfilOrga != null)
        {
          perfilOrga.FechaUltimaLectura = DateTime.Now;
          await _context.SaveChangesAsync();
        }
      }
      else
      {
        var perfil = await _context.PerfilesRunners.FirstOrDefaultAsync(p => p.IdUsuario == idUsuario);
        if (perfil != null)
        {
          perfil.FechaUltimaLectura = DateTime.Now;
          await _context.SaveChangesAsync();
        }
      }
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

      // Disparar push a Firebase únicamente a los runners afectados (nunca al organizador)
      await DispararPushNotificacionAsync(
          request.IdEvento,
          request.IdCategoria,
          idUsuarioDestino,
          request.Titulo,
          request.Mensaje,
          idOrganizador);

      return (notificacion, null);
    }

    //  HELPERS

    // Envía la notificación Push por Firebase al usuario destino o a cada runner activo de la categoría/evento
    private async Task DispararPushNotificacionAsync(
      int idEvento,
      int? idCategoria,
      int? idUsuarioDestino,
      string titulo,
      string? mensaje,
      int? idOrganizadorExcluir = null)
    {
      // Caso 1: Notificación privada a un usuario puntual (Runner u Organizador)
      if (idUsuarioDestino.HasValue)
      {
        await _firebaseService.EnviarNotificacionTopicAsync(
            $"runner_{idUsuarioDestino.Value}",
            titulo,
            mensaje ?? string.Empty,
            idEvento);
        return;
      }

      var estadosValidos = new List<string> { "pagado", "procesando", "pendiente" };

      // Caso 2: Notificación a los runners inscriptos en una categoría específica (Ej: 10K Retrasada)
      if (idCategoria.HasValue)
      {
        var idsRunnersCategoria = await _context.Inscripciones
          .AsNoTracking()
          .Where(i => i.IdCategoria == idCategoria.Value
                      && estadosValidos.Contains(i.EstadoPago)
                      && (!idOrganizadorExcluir.HasValue || i.IdUsuario != idOrganizadorExcluir.Value))
          .Select(i => i.IdUsuario)
          .Distinct()
          .ToListAsync();

        foreach (var idRunner in idsRunnersCategoria)
        {
          await _firebaseService.EnviarNotificacionTopicAsync(
              $"runner_{idRunner}",
              titulo,
              mensaje ?? string.Empty,
              idEvento);
        }
        return;
      }

      // Caso 3: Notificación general a todos los runners inscriptos en el evento (Ej: Evento Suspendido/Cancelado)
      var idsRunnersEvento = await _context.Inscripciones
        .AsNoTracking()
        .Include(i => i.Categoria)
        .Where(i => i.Categoria != null
                    && i.Categoria.IdEvento == idEvento
                    && estadosValidos.Contains(i.EstadoPago)
                    && (!idOrganizadorExcluir.HasValue || i.IdUsuario != idOrganizadorExcluir.Value))
        .Select(i => i.IdUsuario)
        .Distinct()
        .ToListAsync();

      foreach (var idRunner in idsRunnersEvento)
      {
        await _firebaseService.EnviarNotificacionTopicAsync(
            $"runner_{idRunner}",
            titulo,
            mensaje ?? string.Empty,
            idEvento);
      }
    }

  }
}
