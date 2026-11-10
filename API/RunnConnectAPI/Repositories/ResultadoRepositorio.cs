//Repositories/ResultadoRepositorio
using Microsoft.EntityFrameworkCore;
using RunnConnectAPI.Data;
using RunnConnectAPI.Models;
using RunnConnectAPI.Models.Dto.Resultado;

namespace RunnConnectAPI.Repositories
{
  public class ResultadoRepositorio
  {
    private readonly RunnersContext _context;

    public ResultadoRepositorio(RunnersContext context)
    {
      _context = context;
    }

    // LECTURA 


    public async Task<ResultadosEventoResponse?> ObtenerResultadosEventoAsync(int idEvento)
    {
      var evento = await _context.Eventos
          .FirstOrDefaultAsync(e => e.IdEvento == idEvento);

      if (evento == null)
        return null;

      // Solo inscripciones PAGADAS
      var inscripcionesEvento = await _context.Inscripciones
          .Include(i => i.Usuario)
              .ThenInclude(u => u!.PerfilRunner)
          .Include(i => i.Categoria)
          .Include(i => i.Resultado)
          .Where(i => i.Categoria!.IdEvento == idEvento && i.EstadoPago == "pagado")
          .ToListAsync();

      var resultados = inscripcionesEvento
          .Where(i => i.Resultado != null)
          .OrderBy(i => i.Resultado!.PosicionCategoria ?? int.MaxValue)
          .ThenBy(i => i.Resultado!.TiempoOficial)
          .Select(i => new ResultadoEventoItem
          {
            IdResultado = i.Resultado!.IdResultado,
            IdInscripcion = i.IdInscripcion,
            NombreRunner = $"{i.Usuario?.PerfilRunner?.Nombre} {i.Usuario?.PerfilRunner?.Apellido}".Trim(),
            DniRunner = i.Usuario?.PerfilRunner?.Dni,
            Genero = i.Usuario?.PerfilRunner?.Genero,
            Agrupacion = i.Usuario?.PerfilRunner?.Agrupacion,
            NombreCategoria = i.Categoria?.Nombre ?? "",
            TiempoOficial = i.Resultado.TiempoOficial,
            PosicionCategoria = i.Resultado.PosicionCategoria,
            TieneDatosSmartwatch = !string.IsNullOrEmpty(i.Resultado.TiempoSmartwatch) || i.Resultado.DistanciaKm.HasValue
          })
          .ToList();

      return new ResultadosEventoResponse
      {
        Evento = new EventoResultadoInfo
        {
          IdEvento = evento.IdEvento,
          Nombre = evento.Nombre,
          FechaHora = evento.FechaHora,
          Lugar = evento.Lugar,
          Estado = evento.Estado
        },
        TotalParticipantes = inscripcionesEvento.Count,
        TotalConResultado = resultados.Count,
        TotalSinResultado = inscripcionesEvento.Count - resultados.Count,
        Resultados = resultados
      };
    }

    public async Task<List<ResultadoEventoItem>> ObtenerResultadosPorCategoriaAsync(int idCategoria)
    {
      return await _context.Inscripciones
          .Include(i => i.Usuario)
              .ThenInclude(u => u!.PerfilRunner)
          .Include(i => i.Categoria)
          .Include(i => i.Resultado)
          .Where(i => i.IdCategoria == idCategoria && i.EstadoPago == "pagado" && i.Resultado != null)
          .OrderBy(i => i.Resultado!.PosicionCategoria ?? int.MaxValue)
          .ThenBy(i => i.Resultado!.TiempoOficial)
          .Select(i => new ResultadoEventoItem
          {
            IdResultado = i.Resultado!.IdResultado,
            IdInscripcion = i.IdInscripcion,
            NombreRunner = $"{i.Usuario!.PerfilRunner!.Nombre} {i.Usuario.PerfilRunner.Apellido}".Trim(),
            DniRunner = i.Usuario.PerfilRunner.Dni,
            Genero = i.Usuario.PerfilRunner.Genero,
            Agrupacion = i.Usuario.PerfilRunner.Agrupacion,
            NombreCategoria = i.Categoria!.Nombre,
            TiempoOficial = i.Resultado.TiempoOficial,
            PosicionCategoria = i.Resultado.PosicionCategoria,
            TieneDatosSmartwatch = !string.IsNullOrEmpty(i.Resultado.TiempoSmartwatch)
          })
          .ToListAsync();
    }

    public async Task<MisResultadosResponse> ObtenerMisResultadosAsync(int idUsuario)
    {
      //Obtener mis resultados
      var inscripciones = await _context.Inscripciones
          .Include(i => i.Categoria)
              .ThenInclude(c => c!.Evento)
          .Include(i => i.Resultado)
          .Where(i => i.IdUsuario == idUsuario && i.Resultado != null)
          .OrderByDescending(i => i.Categoria!.Evento!.FechaHora)
          .ToListAsync();

      //Obtener cantidad por categoria
      var categoriaIds = inscripciones
        .Select(
          i => i.IdCategoria
        ).Distinct()
          .ToList();

      //Contar inscriptos PAGADOS hay en esa categoria
      var contadoresCategoria = await _context.Inscripciones
        .Where(i => categoriaIds.Contains(i.IdCategoria) && i.EstadoPago == "pagado")
        .GroupBy(i => i.IdCategoria)
        .Select(g => new { IdCategoria = g.Key, Total = g.Count() })
        .ToDictionaryAsync(k => k.IdCategoria, v => v.Total);

      //Mapear respuestas
      var resultados = inscripciones
          .Select(i => new MiResultadoItem
          {
            IdResultado = i.Resultado!.IdResultado,
            IdInscripcion = i.IdInscripcion,
            IdEvento = i.Categoria!.IdEvento,
            NombreEvento = i.Categoria.Evento!.Nombre,
            FechaEvento = i.Categoria.Evento.FechaHora,
            LugarEvento = i.Categoria.Evento.Lugar,
            NombreCategoria = i.Categoria.Nombre,
            TiempoOficial = i.Resultado.TiempoOficial,
            PosicionCategoria = i.Resultado.PosicionCategoria,
            //Calculamos la categoria
            TotalParticipantesCategoria = contadoresCategoria.ContainsKey(i.IdCategoria)
              ? contadoresCategoria[i.IdCategoria]
              : 0,

            DatosSmartwatch = new DatosSmartwatchInfo
            {
              TiempoSmartwatch = i.Resultado.TiempoSmartwatch,
              DistanciaKm = i.Resultado.DistanciaKm,
              RitmoPromedio = i.Resultado.RitmoPromedio,
              VelocidadPromedio = i.Resultado.VelocidadPromedio,
              CaloriasQuemadas = i.Resultado.CaloriasQuemadas,
              PulsacionesPromedio = i.Resultado.PulsacionesPromedio,
              PulsacionesMax = i.Resultado.PulsacionesMax
            }
          })
          .ToList();

      return new MisResultadosResponse
      {
        TotalCarreras = resultados.Count,
        Resultados = resultados,
        Estadisticas = new EstadisticasRunner
        {
          CarrerasCompletadas = resultados.Count,
          DistanciaTotalKm = resultados.Sum(r => r.DatosSmartwatch?.DistanciaKm ?? 0),
          CaloriasTotales = resultados.Sum(r => r.DatosSmartwatch?.CaloriasQuemadas ?? 0),
          PosicionCategoria = resultados.Any() ? resultados.Min(r => r.PosicionCategoria) : null
        }
      };
    }

    // CARGA (ORGANIZADOR) 
    /// Carga masiva de resultados (llamado por el controller que leyo el CSV)
    public async Task<ResultadosResponse> CargarResultadosAsync(CargarResultadosRequest request, int idOrganizador)
    {
      var response = new ResultadosResponse
      {
        TotalProcesados = request.Resultados.Count
      };

      var evento = await _context.Eventos
          .Include(e => e.Categorias)
          .FirstOrDefaultAsync(e => e.IdEvento == request.IdEvento);

      if (evento == null || evento.IdOrganizador != idOrganizador)
      {
        response.Errores.Add(new ResultadoError
        {
          Dni = 0,
          Motivo = "Evento no encontrado o sin permisos"
        });
        response.Fallidos = request.Resultados.Count;
        return response;
      }

      //inscripc valida dni y categoria
      var inscripcionesEvento = await _context.Inscripciones
          .Include(i => i.Usuario)
              .ThenInclude(u => u!.PerfilRunner)
          .Include(i => i.Categoria)
          .Include(i => i.Resultado)
          .Where(i => i.IdCategoria == request.IdCategoria && i.EstadoPago == "pagado")
          .ToListAsync();

      foreach (var item in request.Resultados)
      {
        var inscripcion = inscripcionesEvento
            .FirstOrDefault(i => i.Usuario?.PerfilRunner?.Dni == item.Dni);

        if (inscripcion == null)
        {
          response.Errores.Add(new ResultadoError
          {
            Dni = item.Dni,
            Motivo = "No se encontró inscripción pagada con este DNI"
          });
          response.Fallidos++;
          continue;
        }

        // Solo permitimos cargar si la categoria específica esta finalizada
        if (inscripcion.Categoria?.Estado != "finalizada")
        {
          response.Errores.Add(new ResultadoError
          {
            Dni = item.Dni,
            Motivo = $"La categoría '{inscripcion.Categoria?.Nombre}' aún no está finalizada."
          });
          response.Fallidos++;
          continue;
        }

        // logica oficial
        if (inscripcion.Resultado != null)
        {
          inscripcion.Resultado.TiempoOficial = item.TiempoOficial;
          inscripcion.Resultado.PosicionCategoria = item.PosicionCategoria;
        }
        else
        {
          var nuevoResultado = new Resultado
          {
            IdInscripcion = inscripcion.IdInscripcion,
            TiempoOficial = item.TiempoOficial,
            PosicionCategoria = item.PosicionCategoria
          };
          _context.Resultados.Add(nuevoResultado);
        }

        response.Exitosos++;


        //para inyeccion de notif a categorias afectadas
        if (!response.CategoriasActualizadas.Contains(inscripcion.IdCategoria))
        {
          response.CategoriasActualizadas.Add(inscripcion.IdCategoria);
        }
      }

      await _context.SaveChangesAsync();
      return response;
    }

    // ELIMINACION EN BLOQUE DE RESULTADOS POR CATEGORIA
    public async Task<(bool exito, string? error, int totalEliminados)> EliminarResultadosPorCategoriaAsync(
        int idCategoria,
        int idOrganizador)
    {
      var categoria = await _context.CategoriasEvento
          .Include(c => c.Evento)
          .FirstOrDefaultAsync(c => c.IdCategoria == idCategoria);

      if (categoria == null)
        return (false, "Categoría no encontrada", 0);

      if (categoria.Evento?.IdOrganizador != idOrganizador)
        return (false, "Sin permisos para gestionar resultados de este evento", 0);

      var resultados = await _context.Resultados
          .Where(r => r.Inscripcion != null && r.Inscripcion.IdCategoria == idCategoria)
          .ToListAsync();

      if (!resultados.Any())
        return (false, "La categoría no tiene resultados cargados para eliminar", 0);

      int total = resultados.Count;
      _context.Resultados.RemoveRange(resultados);
      await _context.SaveChangesAsync();

      return (true, null, total);
    }






  }
}