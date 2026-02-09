//Repositories/CategoriaRepositorio.cs
using Microsoft.EntityFrameworkCore;
using RunnConnectAPI.Data;
using RunnConnectAPI.Models;

namespace RunnConnectAPI.Repositories
{
  /*Getion de categorias de eventos*/
  public class CategoriaRepositorio
  {
    private readonly RunnersContext _context;

    public CategoriaRepositorio(RunnersContext context)
    {
      _context = context;
    }

    // Obtiene todas las categorias de un evento
    public async Task<List<CategoriaEvento>> ObtenerPorEventoAsync(int idEvento)
    {
      return await _context.CategoriasEvento
          .Where(c => c.IdEvento == idEvento)
          .OrderBy(c => c.Nombre)
          .ToListAsync();
    }

    // Obtiene una categoria por su ID
    public async Task<CategoriaEvento?> ObtenerPorIdAsync(int idCategoria)
    {
      return await _context.CategoriasEvento
          .Include(c => c.Evento)
          .FirstOrDefaultAsync(c => c.IdCategoria == idCategoria);
    }

    //obtener categoria incluyendo el evento (valido si es el dueño)
    public async Task<CategoriaEvento?> ObtenerPorIdConEventoAsync(int idCategoria)
    {
      return await _context.CategoriasEvento
          .Include(c => c.Evento)
          .FirstOrDefaultAsync(c => c.IdCategoria == idCategoria);
    }



    // Obtiene una categoria verificando que pertenezca al evento especificado
    public async Task<CategoriaEvento?> ObtenerPorIdYEventoAsync(int idCategoria, int idEvento)
    {
      return await _context.CategoriasEvento
          .FirstOrDefaultAsync(c => c.IdCategoria == idCategoria && c.IdEvento == idEvento);
    }


    // Verifica si existe una categoria con el mismo nombre en el evento
    public async Task<bool> ExisteNombreEnEventoAsync(int idEvento, string nombre, int? excluirIdCategoria = null)
    {
      var query = _context.CategoriasEvento
          .Where(c => c.IdEvento == idEvento && c.Nombre.ToLower() == nombre.ToLower().Trim());

      // Si estamos actualizando, excluir la categoria actual
      if (excluirIdCategoria.HasValue)
        query = query.Where(c => c.IdCategoria != excluirIdCategoria.Value);

      return await query.AnyAsync();
    }


    // Cuenta las categorias de un evento
    public async Task<int> ContarPorEventoAsync(int idEvento)
    {
      return await _context.CategoriasEvento
          .CountAsync(c => c.IdEvento == idEvento);
    }



    // Operaciones CRUD
    // Crea una nueva categoria
    public async Task<CategoriaEvento> CrearAsync(CategoriaEvento categoria)
    {
      // Normalizar datos
      categoria.Nombre = categoria.Nombre.Trim();
      categoria.Genero = categoria.Genero.ToUpper().Trim();

      _context.CategoriasEvento.Add(categoria);
      await _context.SaveChangesAsync();

      return categoria;
    }


    // Crea multiples categorias para un evento (usado al crear evento con categorias)
    public async Task<List<CategoriaEvento>> CrearVariasAsync(List<CategoriaEvento> categorias)
    {
      foreach (var categoria in categorias)
      {
        categoria.Nombre = categoria.Nombre.Trim();
        categoria.Genero = categoria.Genero.ToUpper().Trim();
      }

      _context.CategoriasEvento.AddRange(categorias);
      await _context.SaveChangesAsync();

      return categorias;
    }


    // Actualiza una categoria existente
    public async Task ActualizarAsync(CategoriaEvento categoria)
    {
      // Normalizar datos
      categoria.Nombre = categoria.Nombre.Trim();
      categoria.Genero = categoria.Genero.ToUpper().Trim();

      _context.CategoriasEvento.Update(categoria);
      await _context.SaveChangesAsync();
    }


    // Elimina una categoria (eliminación física)
    public async Task EliminarAsync(CategoriaEvento categoria)
    {
      _context.CategoriasEvento.Remove(categoria);
      await _context.SaveChangesAsync();
    }



    // Validaciones
    // Verifica si una categoria tiene inscripciones
    public async Task<bool> TieneInscripcionesAsync(int idCategoria)
    {
      return await _context.Inscripciones
          .AnyAsync(i => i.IdCategoria == idCategoria);
    }


    // Cuenta inscriptos confirmados en una categoria
    public async Task<int> ContarInscriptosAsync(int idCategoria)
    {
      return await _context.Inscripciones
          .CountAsync(i => i.IdCategoria == idCategoria && i.EstadoPago == "pagado");
    }

    // Verifica si hay cupo disponible en la categoria
    public async Task<bool> TieneCupoDisponibleAsync(int idCategoria)
    {
      var categoria = await _context.CategoriasEvento.FindAsync(idCategoria);

      if (categoria == null)
        return false;

      // Si no tiene limite de cupo, siempre hay disponible
      if (!categoria.CupoCategoria.HasValue)
        return true;

      var inscriptos = await ContarInscriptosAsync(idCategoria);
      return inscriptos < categoria.CupoCategoria.Value;
    }


    // Valida que la edad minima sea menor o igual a la maxima
    public bool ValidarRangoEdades(int edadMinima, int edadMaxima)
    {
      return edadMinima <= edadMaxima;
    }

    // Valida que el cupo de la categoria no exceda el cupo total del evento
    public async Task<bool> ValidarCupoContraEventoAsync(int idEvento, int? cupoCategoria)
    {
      if (!cupoCategoria.HasValue)
        return true; // Sin límite de cupo

      var evento = await _context.Eventos.FindAsync(idEvento);

      if (evento == null)
        return false;

      // Si el evento no tiene límite, cualquier cupo de categoria es valido
      if (!evento.CupoTotal.HasValue)
        return true;

      return cupoCategoria.Value <= evento.CupoTotal.Value;
    }



    // Estadisticas
    // Obtiene el resumen de inscriptos por categoria de un evento
    public async Task<Dictionary<int, int>> ObtenerInscriptosPorCategoriaAsync(int idEvento)
    {
      var categorias = await _context.CategoriasEvento
          .Where(c => c.IdEvento == idEvento)
          .Select(c => c.IdCategoria)
          .ToListAsync();

      var resultado = new Dictionary<int, int>();

      foreach (var idCategoria in categorias)
      {
        var inscriptos = await ContarInscriptosAsync(idCategoria);
        resultado[idCategoria] = inscriptos;
      }

      return resultado;
    }


  }
}