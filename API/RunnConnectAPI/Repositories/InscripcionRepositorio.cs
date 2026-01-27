//Repositories/InscripcionRepositorio.cs

using Microsoft.EntityFrameworkCore;
using RunnConnectAPI.Data;
using RunnConnectAPI.Models;

namespace RunnConnectAPI.Repositories
{

  // Repositorio para gestion de inscripciones

  public class InscripcionRepositorio
  {
    private readonly RunnersContext _context;

    public InscripcionRepositorio(RunnersContext context)
    {
      _context = context;
    }

    // Consultas para Runners
    // Obtiene todas las inscripciones de un runner
    public async Task<List<Inscripcion>> ObtenerPorRunnerAsync(int idUsuario)
    {
      return await _context.Inscripciones
          .Include(i => i.Categoria)
              .ThenInclude(c => c!.Evento)
          .Where(i => i.IdUsuario == idUsuario)
          .OrderByDescending(i => i.FechaInscripcion)
          .ToListAsync();
    }

    // Obtiene inscripciones activas (pendiente o confirmado) de un runner
    public async Task<List<Inscripcion>> ObtenerActivasPorRunnerAsync(int idUsuario)
    {
      var estadosActivos = new[] { "pendiente", "procesando", "pagado" };

      return await _context.Inscripciones
          .Include(i => i.Categoria)
              .ThenInclude(c => c!.Evento)
          .Where(i => i.IdUsuario == idUsuario && estadosActivos.Contains(i.EstadoPago))
          .OrderByDescending(i => i.FechaInscripcion)
          .ToListAsync();
    }

    // Obtiene una inscripción por ID con todas sus relaciones
    public async Task<Inscripcion?> ObtenerPorIdAsync(int idInscripcion)
    {
      return await _context.Inscripciones
          .Include(i => i.Usuario)
              .ThenInclude(u => u!.PerfilRunner)
          .Include(i => i.Categoria)
              .ThenInclude(c => c!.Evento)
          .FirstOrDefaultAsync(i => i.IdInscripcion == idInscripcion);
    }


    // Verifica si un runner ya esta inscripto en una categoria
    public async Task<bool> ExisteInscripcionAsync(int idUsuario, int idEvento)
    {
      var estadosActivos = new[] { "pendiente", "procesando", "pagado" };

      return await _context.Inscripciones
          .Include(i => i.Categoria)
          .AnyAsync(i => i.IdUsuario == idUsuario
              && i.Categoria != null
              && i.Categoria.IdEvento == idEvento
              && estadosActivos.Contains(i.EstadoPago));
    }


    /// Verifica si un runner ya esta inscripto en el mismo evento (cualquier categoria)
    public async Task<bool> ExisteInscripcionEnEventoAsync(int idUsuario, int idEvento)
    {
      var estadosActivos = new[] { "pendiente", "procesando", "pagado" };

      return await _context.Inscripciones
          .Include(i => i.Categoria)
          .AnyAsync(i => i.IdUsuario == idUsuario
              && i.Categoria != null
              && i.Categoria.IdEvento == idEvento
              && estadosActivos.Contains(i.EstadoPago));
    }

    // Consultas para Organizadores
    // Obtiene todas las inscripciones de un evento
    public async Task<List<Inscripcion>> ObtenerPorEventoAsync(int idEvento)
    {
      return await _context.Inscripciones
          .Include(i => i.Usuario)
              .ThenInclude(u => u!.PerfilRunner)
          .Include(i => i.Categoria)
          .Where(i => i.Categoria != null && i.Categoria.IdEvento == idEvento)
          .OrderByDescending(i => i.FechaInscripcion)
          .ToListAsync();
    }

    /// Obtiene inscripciones de un evento con filtros y paginacion
    public async Task<(List<Inscripcion> inscripciones, int totalCount)> ObtenerPorEventoConFiltrosAsync(
        int idEvento,
        int? idCategoria = null,
        string? estadoPago = null,
        string? buscarRunner = null,
        int pagina = 1,
        int tamanioPagina = 20)
    {
      var query = _context.Inscripciones
          .Include(i => i.Usuario)
              .ThenInclude(u => u!.PerfilRunner)
          .Include(i => i.Categoria)
          .Where(i => i.Categoria != null && i.Categoria.IdEvento == idEvento)
          .AsQueryable();

      // Filtrar por categoría
      if (idCategoria.HasValue)
        query = query.Where(i => i.IdCategoria == idCategoria.Value);

      // Filtrar por estado de pago
      if (!string.IsNullOrWhiteSpace(estadoPago))
        query = query.Where(i => i.EstadoPago == estadoPago.ToLower().Trim());

      // Buscar por nombre/apellido del runner
      if (!string.IsNullOrWhiteSpace(buscarRunner))
      {
        var busqueda = buscarRunner.ToLower().Trim();
        query = query.Where(i => i.Usuario != null &&
            (i.Usuario.Nombre.ToLower().Contains(busqueda) ||
             (i.Usuario.PerfilRunner != null &&
              (i.Usuario.PerfilRunner.Nombre.ToLower().Contains(busqueda) ||
               i.Usuario.PerfilRunner.Apellido.ToLower().Contains(busqueda)))));
      }

      // Contar total antes de paginar
      var totalCount = await query.CountAsync();

      // Aplicar paginacion
      var inscripciones = await query
          .OrderByDescending(i => i.FechaInscripcion)
          .Skip((pagina - 1) * tamanioPagina)
          .Take(tamanioPagina)
          .ToListAsync();

      return (inscripciones, totalCount);
    }


    // Obtiene inscripciones de una categoría especifica
    public async Task<List<Inscripcion>> ObtenerPorCategoriaAsync(int idCategoria)
    {
      return await _context.Inscripciones
          .Include(i => i.Usuario)
              .ThenInclude(u => u!.PerfilRunner)
          .Where(i => i.IdCategoria == idCategoria)
          .OrderByDescending(i => i.FechaInscripcion)
          .ToListAsync();
    }


    // Operaciones CRUD
    // Crea una nueva inscripcion
    public async Task<Inscripcion> CrearAsync(Inscripcion inscripcion)
    {
      inscripcion.FechaInscripcion = DateTime.Now;
      inscripcion.EstadoPago = "pendiente"; //Estado inicial
      inscripcion.TalleRemera = inscripcion.TalleRemera?.ToUpper().Trim();

      _context.Inscripciones.Add(inscripcion);
      await _context.SaveChangesAsync();

      return inscripcion;
    }

    /// Actualiza una inscripcion existente
    public async Task ActualizarAsync(Inscripcion inscripcion)
    {
      inscripcion.TalleRemera = inscripcion.TalleRemera?.ToUpper().Trim();

      _context.Inscripciones.Update(inscripcion);
      await _context.SaveChangesAsync();
    }

    /// Cambia el estado de pago de una inscripcion
    public async Task CambiarEstadoPagoAsync(int idInscripcion, string nuevoEstado)
    {
      var inscripcion = await _context.Inscripciones.FindAsync(idInscripcion);

      if (inscripcion == null)
        throw new KeyNotFoundException("Inscripción no encontrada");

      nuevoEstado = nuevoEstado.ToLower().Trim();

      var estadosValidos = new[] { "pendiente", "procesando", "pagado", "rechazado", "reembolsado", "cancelado" };
      if (!estadosValidos.Contains(nuevoEstado))
        throw new ArgumentException($"Estado inválido. Estados válidos: {string.Join(", ", estadosValidos)}");

      // Validaciones de transicion
      ValidarTransicionEstado(inscripcion.EstadoPago, nuevoEstado);

      inscripcion.EstadoPago = nuevoEstado;
      await _context.SaveChangesAsync();
    }

    /// Actualiza la URL del comprobante de pago
    public async Task ActualizarComprobanteYEstadoAsync(int idInscripcion, string urlComprobante, string nuevoEstado)
    {
      var inscripcion = await _context.Inscripciones.FindAsync(idInscripcion);

      if (inscripcion == null)
        throw new KeyNotFoundException("Inscripción no encontrada");

      nuevoEstado = nuevoEstado.ToLower().Trim();


      // Validar transición: Solo se puede pasar a 'procesando' desde 'pendiente'
      if (inscripcion.EstadoPago != "pendiente" && nuevoEstado == "procesando")
        throw new InvalidOperationException("Solo se puede pasar a procesamiento de pago desde el estado pendiente");


      inscripcion.EstadoPago = nuevoEstado;
      inscripcion.ComprobantePagoURL = urlComprobante;
      await _context.SaveChangesAsync();
    }


    // Validaciones
    // Valida las reglas de transicion de estado
    private void ValidarTransicionEstado(string estadoActual, string nuevoEstado)
    {
      // 1. Estados Finales Inmutables (Excepto pagado -> reembolsado)
      if (estadoActual == "rechazado" || estadoActual == "reembolsado" || estadoActual == "cancelado")
        throw new InvalidOperationException($"No se puede modificar una inscripción en estado {estadoActual}.");

      if (estadoActual == "pagado")
      {
        if (nuevoEstado != "reembolsado")
          throw new InvalidOperationException("No se puede cancelar una inscripcion que ya esta PAGADA. Comunicate con el Organizador");
      }

      // 2. Lógica para CANCELADO (Runner)
      if (nuevoEstado == "cancelado")
      {
        if (estadoActual != "pendiente")
          throw new InvalidOperationException("Solo se pueden cancelar inscripciones pendientes. Si ya enviaste el comprobante, debes esperar la respuesta del organizador");
      }

      // 3. Lógica para RECHAZADO (Organizador - Pago fallido)
      if (nuevoEstado == "rechazado")
      {
        if (estadoActual != "pendiente" && estadoActual != "procesando")
          throw new InvalidOperationException("Solo se pueden rechazar inscripciones pendientes o en procesamiento.");
      }

      // 4. Lógica para PAGADO (Organizador - Éxito)
      if (nuevoEstado == "pagado")
      {
        if (estadoActual != "procesando")
          throw new InvalidOperationException("El pago debe estar en PROCESANDO para ser marcado como PAGADO.");
      }

      // 5. Lógica para REEMBOLSO
      if (nuevoEstado == "reembolsado" && estadoActual != "pagado")
        throw new InvalidOperationException("Solo se pueden reembolsar inscripciones que ya están pagadas.");
    }

    /// Verifica si el runner cumple con los requisitos de la categoria (edad y género)
    public async Task<(bool cumple, string? motivo)> ValidarRequisitosCategoria(int idUsuario, int idCategoria)
    {
      var usuario = await _context.Usuarios
          .Include(u => u.PerfilRunner)
          .FirstOrDefaultAsync(u => u.IdUsuario == idUsuario);

      var categoria = await _context.CategoriasEvento.FindAsync(idCategoria);

      if (usuario == null || categoria == null)
        return (false, "Usuario o categoría no encontrada");

      if (usuario.PerfilRunner == null)
        return (false, "Debe completar su perfil de runner");

      // Validar fecha de nacimiento
      if (!usuario.PerfilRunner.FechaNacimiento.HasValue)
        return (false, "Debe completar su fecha de nacimiento en el perfil");

      // Calcular edad
      var edad = (int)((DateTime.Now - usuario.PerfilRunner.FechaNacimiento.Value).TotalDays / 365.25);

      // Validar rango de edad
      if (edad < categoria.EdadMinima || edad > categoria.EdadMaxima)
        return (false, $"Su edad ({edad} años) no está dentro del rango permitido ({categoria.EdadMinima}-{categoria.EdadMaxima} años)");

      // Validar genero (si la categoria no es mixta)
      if (categoria.Genero != "X")
      {
        if (string.IsNullOrEmpty(usuario.PerfilRunner.Genero))
          return (false, "Debe completar su género en el perfil");

        if (usuario.PerfilRunner.Genero != categoria.Genero)
          return (false, $"Esta categoría es solo para género {(categoria.Genero == "F" ? "Femenino" : "Masculino")}");
      }

      return (true, null);
    }

    /// Verifica si el runner tiene perfil completo para inscribirse
    public async Task<(bool completo, List<string> camposFaltantes)> ValidarPerfilCompletoRunner(int idUsuario)
    {
      var usuario = await _context.Usuarios
          .Include(u => u.PerfilRunner)
          .FirstOrDefaultAsync(u => u.IdUsuario == idUsuario);

      var faltantes = new List<string>();

      if (usuario == null)
      {
        faltantes.Add("Usuario no encontrado");
        return (false, faltantes);
      }

      if (usuario.PerfilRunner == null)
      {
        faltantes.Add("Perfil de runner");
        return (false, faltantes);
      }

      var perfil = usuario.PerfilRunner;

      if (string.IsNullOrEmpty(perfil.Nombre))
        faltantes.Add("Nombre");
      if (string.IsNullOrEmpty(perfil.Apellido))
        faltantes.Add("Apellido");
      if (!perfil.FechaNacimiento.HasValue)
        faltantes.Add("Fecha de nacimiento");
      if (string.IsNullOrEmpty(perfil.Genero))
        faltantes.Add("Género");
      if (!perfil.Dni.HasValue)
        faltantes.Add("DNI");
      if (string.IsNullOrEmpty(perfil.Localidad))
        faltantes.Add("Localidad");
      if (string.IsNullOrEmpty(perfil.NombreContactoEmergencia))
        faltantes.Add("Contacto de emergencia");
      if (string.IsNullOrEmpty(perfil.TelefonoEmergencia))
        faltantes.Add("Teléfono de emergencia");
      if (string.IsNullOrEmpty(usuario.Telefono))
        faltantes.Add("Teléfono");

      return (faltantes.Count == 0, faltantes);
    }


    // Estadisticas

    /// Cuenta inscripciones por estado en un evento
    public async Task<Dictionary<string, int>> ContarPorEstadoEnEventoAsync(int idEvento)
    {
      var inscripciones = await _context.Inscripciones
          .Include(i => i.Categoria)
          .Where(i => i.Categoria != null && i.Categoria.IdEvento == idEvento)
          .GroupBy(i => i.EstadoPago)
          .Select(g => new { Estado = g.Key, Cantidad = g.Count() })
          .ToListAsync();

      return inscripciones.ToDictionary(x => x.Estado, x => x.Cantidad);
    }

    /// Cuenta inscripciones confirmadas en un evento
    public async Task<int> ContarPagadosEnEventoAsync(int idEvento)
    {
      return await _context.Inscripciones
          .Include(i => i.Categoria)
          .CountAsync(i => i.Categoria != null
              && i.Categoria.IdEvento == idEvento
              && i.EstadoPago == "pagado");
    }


    /* Permite al organizador forzar la baja de un runner, sin importar si ya pagó.
      Salta las validaciones de transición de pago normales.*/
    public async Task DarBajaPorOrganizadorAsync(int idInscripcion)
    {
      var inscripcion = await _context.Inscripciones.FindAsync(idInscripcion);

      if (inscripcion == null)
        throw new KeyNotFoundException("Inscripción no encontrada");

      // Si ya está cancelada, no hacemos nada o lanzamos error (opcional)
      if (inscripcion.EstadoPago == "cancelado")
        throw new ArgumentException("La inscripción ya se encuentra cancelada.");

      // FORZAMOS el estado a cancelado directamente
      // NO llamamos a ValidarTransicionEstado() porque es una acción administrativa
      inscripcion.EstadoPago = "cancelado";

      // Opcional: Podrías guardar la fecha de baja o quién lo hizo si tuvieras esos campos
      // inscripcion.FechaBaja = DateTime.Now; 

      await _context.SaveChangesAsync();
    }

    public async Task<List<Inscripcion>> BuscarGlobalPorOrganizadorAsync(int idOrganizador, string termino)
    {
      if (string.IsNullOrWhiteSpace(termino))
        return new List<Inscripcion>();

      termino = termino.ToLower().Trim();

      return await _context.Inscripciones
          // Incluir datos del Evento para saber de cuál se trata
          .Include(i => i.Categoria).ThenInclude(c => c.Evento)
          // Incluir datos del Usuario y Perfil para buscar por nombre/DNI
          .Include(i => i.Usuario).ThenInclude(u => u.PerfilRunner)

          // 1. SEGURIDAD: Solo buscar en eventos de ESTE organizador
          .Where(i => i.Categoria.Evento.IdOrganizador == idOrganizador)

          // 2. FILTRO: Nombre, Apellido o DNI
          .Where(i =>
              i.Usuario.Nombre.ToLower().Contains(termino) ||
              (i.Usuario.PerfilRunner != null && i.Usuario.PerfilRunner.Apellido.ToLower().Contains(termino)) ||
              (i.Usuario.PerfilRunner != null && i.Usuario.PerfilRunner.Dni.ToString().Contains(termino))
          )
          .OrderByDescending(i => i.FechaInscripcion) // Más recientes primero
          .Take(50) // Límite por rendimiento
          .ToListAsync();
    }


  }
}