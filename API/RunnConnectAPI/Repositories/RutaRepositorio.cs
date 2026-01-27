// Repositories/RutaRepositorio.cs
using Microsoft.EntityFrameworkCore;
using RunnConnectAPI.Data;
using RunnConnectAPI.Models;
using RunnConnectAPI.Models.Dto.Ruta;
using RunnConnectAPI.Models.Dto.PuntoInteres;

namespace RunnConnectAPI.Repositories
{
  /// Repositorio para gestion de Rutas y Puntos de Interés de eventos
  public class RutaRepositorio
  {
    private readonly RunnersContext _context;

    public RutaRepositorio(RunnersContext context)
    {
      _context = context;
    }

    // ═══════════════════ RUTAS ═══════════════════

    /// Obtiene la ruta completa de un evento
    public async Task<RutaResponse?> ObtenerRutaEventoAsync(int idEvento)
    {
      var evento = await _context.Eventos
        .FirstOrDefaultAsync(e => e.IdEvento == idEvento);

      if (evento == null) return null;

      var puntos = await _context.Rutas
        .Where(r => r.IdEvento == idEvento)
        .OrderBy(r => r.Orden)
        .Select(r => new PuntoRutaResponse
        {
          IdRuta = r.IdRuta,
          Orden = r.Orden,
          Latitud = r.Latitud,
          Longitud = r.Longitud
        })
        .ToListAsync();

      return new RutaResponse
      {
        IdEvento = idEvento,
        NombreEvento = evento.Nombre,
        TotalPuntos = puntos.Count,
        Puntos = puntos
      };
    }

    /// Guarda la ruta completa de un evento (reemplaza la existente)
    public async Task<(bool exito, string? error)> GuardarRutaAsync(
      int idEvento, GuardarRutaRequest request, int idOrganizador)
    {
      // Verificar que el evento existe
      var evento = await _context.Eventos
        .FirstOrDefaultAsync(e => e.IdEvento == idEvento);

      if (evento == null)
        return (false, "El evento no existe");

      // Verificar que el organizador es dueño del evento
      if (evento.IdOrganizador != idOrganizador)
        return (false, "No tienes permiso para modificar la ruta de este evento");

      // Verificar que el evento no esté cancelado
      if (evento.Estado == "cancelado")
        return (false, "No se puede modificar la ruta de un evento cancelado");

      // Eliminar ruta existente
      var rutaExistente = await _context.Rutas
        .Where(r => r.IdEvento == idEvento)
        .ToListAsync();

      if (rutaExistente.Any())
      {
        _context.Rutas.RemoveRange(rutaExistente);
      }

      // Crear nuevos puntos con orden automático
      var orden = 1;
      foreach (var punto in request.Puntos)
      {
        var nuevoPunto = new Ruta
        {
          IdEvento = idEvento,
          Orden = orden++,
          Latitud = punto.Latitud,
          Longitud = punto.Longitud
        };
        _context.Rutas.Add(nuevoPunto);
      }

      await _context.SaveChangesAsync();
      return (true, null);
    }

    /// Elimina toda la ruta de un evento
    public async Task<(bool exito, string? error)> EliminarRutaAsync(int idEvento, int idOrganizador)
    {
      var evento = await _context.Eventos
        .FirstOrDefaultAsync(e => e.IdEvento == idEvento);

      if (evento == null)
        return (false, "El evento no existe");

      if (evento.IdOrganizador != idOrganizador)
        return (false, "No tienes permiso para modificar este evento");

      var rutaExistente = await _context.Rutas
        .Where(r => r.IdEvento == idEvento)
        .ToListAsync();

      if (!rutaExistente.Any())
        return (false, "El evento no tiene ruta definida");

      _context.Rutas.RemoveRange(rutaExistente);
      await _context.SaveChangesAsync();

      return (true, null);
    }

    /// Cuenta los puntos de la ruta de un evento
    public async Task<int> ContarPuntosRutaAsync(int idEvento)
    {
      return await _context.Rutas.CountAsync(r => r.IdEvento == idEvento);
    }


    // ═══════════════════ PUNTOS DE INTERÉS ═══════════════════

    /// Obtiene todos los puntos de interés de un evento
    public async Task<PuntosInteresEventoResponse?> ObtenerPuntosInteresEventoAsync(int idEvento)
    {
      var evento = await _context.Eventos
        .FirstOrDefaultAsync(e => e.IdEvento == idEvento);

      if (evento == null) return null;

      var puntos = await _context.PuntosInteres
        .Where(p => p.IdEvento == idEvento)
        .Select(p => new PuntoInteresResponse
        {
          IdPuntoInteres = p.IdPuntoInteres,
          IdEvento = p.IdEvento,
          Tipo = p.Tipo,
          Nombre = p.Nombre,
          Latitud = p.Latitud,
          Longitud = p.Longitud
        })
        .ToListAsync();

      // Resumen por tipo
      var resumenPorTipo = puntos
        .GroupBy(p => p.Tipo)
        .ToDictionary(g => g.Key, g => g.Count());

      return new PuntosInteresEventoResponse
      {
        IdEvento = idEvento,
        NombreEvento = evento.Nombre,
        TotalPuntos = puntos.Count,
        PuntosInteres = puntos,
        ResumenPorTipo = resumenPorTipo
      };
    }

    /// Obtiene un punto de interés por ID
    public async Task<PuntoInteresResponse?> ObtenerPuntoInteresPorIdAsync(int idPuntoInteres)
    {
      var punto = await _context.PuntosInteres
        .FirstOrDefaultAsync(p => p.IdPuntoInteres == idPuntoInteres);

      if (punto == null) return null;

      return new PuntoInteresResponse
      {
        IdPuntoInteres = punto.IdPuntoInteres,
        IdEvento = punto.IdEvento,
        Tipo = punto.Tipo,
        Nombre = punto.Nombre,
        Latitud = punto.Latitud,
        Longitud = punto.Longitud
      };
    }

    /// Crea un nuevo punto de interés
    public async Task<(PuntoInteres? punto, string? error)> CrearPuntoInteresAsync(
      int idEvento, CrearPuntoInteresRequest request, int idOrganizador)
    {
      var evento = await _context.Eventos
        .FirstOrDefaultAsync(e => e.IdEvento == idEvento);

      if (evento == null)
        return (null, "El evento no existe");

      if (evento.IdOrganizador != idOrganizador)
        return (null, "No tienes permiso para modificar este evento");

      if (evento.Estado == "cancelado")
        return (null, "No se pueden agregar puntos de interés a un evento cancelado");

      var punto = new PuntoInteres
      {
        IdEvento = idEvento,
        Tipo = request.Tipo.ToLower().Trim(),
        Nombre = request.Nombre.Trim(),
        Latitud = request.Latitud,
        Longitud = request.Longitud
      };

      _context.PuntosInteres.Add(punto);
      await _context.SaveChangesAsync();

      return (punto, null);
    }

    /// Actualiza un punto de interés existente
    public async Task<(bool exito, string? error)> ActualizarPuntoInteresAsync(
      int idPuntoInteres, ActualizarPuntoInteresRequest request, int idOrganizador)
    {
      var punto = await _context.PuntosInteres
        .Include(p => p.Evento)
        .FirstOrDefaultAsync(p => p.IdPuntoInteres == idPuntoInteres);

      if (punto == null)
        return (false, "El punto de interés no existe");

      if (punto.Evento?.IdOrganizador != idOrganizador)
        return (false, "No tienes permiso para modificar este punto de interés");

      punto.Tipo = request.Tipo.ToLower().Trim();
      punto.Nombre = request.Nombre.Trim();
      punto.Latitud = request.Latitud;
      punto.Longitud = request.Longitud;

      await _context.SaveChangesAsync();
      return (true, null);
    }

    /// Elimina un punto de interés
    public async Task<(bool exito, string? error)> EliminarPuntoInteresAsync(
      int idPuntoInteres, int idOrganizador)
    {
      var punto = await _context.PuntosInteres
        .Include(p => p.Evento)
        .FirstOrDefaultAsync(p => p.IdPuntoInteres == idPuntoInteres);

      if (punto == null)
        return (false, "El punto de interés no existe");

      if (punto.Evento?.IdOrganizador != idOrganizador)
        return (false, "No tienes permiso para eliminar este punto de interés");

      _context.PuntosInteres.Remove(punto);
      await _context.SaveChangesAsync();

      return (true, null);
    }

    /// Crea múltiples puntos de interés a la vez
    public async Task<(int creados, string? error)> CrearPuntosInteresMultiplesAsync(
      int idEvento, List<CrearPuntoInteresRequest> puntos, int idOrganizador)
    {
      var evento = await _context.Eventos
        .FirstOrDefaultAsync(e => e.IdEvento == idEvento);

      if (evento == null)
        return (0, "El evento no existe");

      if (evento.IdOrganizador != idOrganizador)
        return (0, "No tienes permiso para modificar este evento");

      var nuevoPuntos = puntos.Select(p => new PuntoInteres
      {
        IdEvento = idEvento,
        Tipo = p.Tipo.ToLower().Trim(),
        Nombre = p.Nombre.Trim(),
        Latitud = p.Latitud,
        Longitud = p.Longitud
      }).ToList();

      _context.PuntosInteres.AddRange(nuevoPuntos);
      await _context.SaveChangesAsync();

      return (nuevoPuntos.Count, null);
    }

    /// Elimina todos los puntos de interés de un evento
    public async Task<(bool exito, string? error)> EliminarTodosPuntosInteresAsync(
      int idEvento, int idOrganizador)
    {
      var evento = await _context.Eventos
        .FirstOrDefaultAsync(e => e.IdEvento == idEvento);

      if (evento == null)
        return (false, "El evento no existe");

      if (evento.IdOrganizador != idOrganizador)
        return (false, "No tienes permiso para modificar este evento");

      var puntosExistentes = await _context.PuntosInteres
        .Where(p => p.IdEvento == idEvento)
        .ToListAsync();

      if (!puntosExistentes.Any())
        return (false, "El evento no tiene puntos de interés");

      _context.PuntosInteres.RemoveRange(puntosExistentes);
      await _context.SaveChangesAsync();

      return (true, null);
    }

  

    // ═══════════════════ MAPA COMPLETO ═══════════════════

    /// Obtiene el mapa completo del evento (ruta + puntos de interés)
    /// Útil para la app Android
    public async Task<MapaEventoResponse?> ObtenerMapaCompletoAsync(int idEvento)
    {
      var evento = await _context.Eventos
        .FirstOrDefaultAsync(e => e.IdEvento == idEvento);

      if (evento == null) return null;

      var ruta = await ObtenerRutaEventoAsync(idEvento);
      var puntosInteres = await ObtenerPuntosInteresEventoAsync(idEvento);

      return new MapaEventoResponse
      {
        IdEvento = idEvento,
        NombreEvento = evento.Nombre,
        FechaEvento = evento.FechaHora,
        Lugar = evento.Lugar,
        Ruta = ruta,
        PuntosInteres = puntosInteres
      };
    }

  }

  /// Respuesta con el mapa completo del evento
  public class MapaEventoResponse
  {
    public int IdEvento { get; set; }
    public string NombreEvento { get; set; } = string.Empty;
    public DateTime FechaEvento { get; set; }
    public string Lugar { get; set; } = string.Empty;
    public RutaResponse? Ruta { get; set; }
    public PuntosInteresEventoResponse? PuntosInteres { get; set; }

    /// Indica si el evento tiene mapa configurado
    public bool TieneMapa => (Ruta?.TotalPuntos ?? 0) > 0 || (PuntosInteres?.TotalPuntos ?? 0) > 0;
  }
}