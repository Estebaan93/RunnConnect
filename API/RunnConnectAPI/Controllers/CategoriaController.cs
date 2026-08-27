// Controllers/CategoriaController.cs
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using RunnConnectAPI.Models;
using RunnConnectAPI.Models.Dto.Categoria;
using RunnConnectAPI.Models.Dto.Notificacion;
using RunnConnectAPI.Repositories;
using RunnConnectAPI.Services;
using System.Security.Claims;

namespace RunnConnectAPI.Controllers
{
  /* Controller para gestion de categorias de eventos, las categorias definen las divisiones de un evento (10K, 5K, etc.)
  con sus respectivos costos, cupos, rangos de edad y genero*/

  [ApiController]
  [Route("api/Evento/{idEvento}/Categorias")]
  public class CategoriaController : ControllerBase
  {
    private readonly CategoriaRepositorio _categoriaRepo;
    private readonly EventoRepositorio _eventoRepo;
    private readonly NotificacionRepositorio _notificacionRepo;

    public CategoriaController(CategoriaRepositorio categoriaRepo, EventoRepositorio eventoRepo, NotificacionRepositorio notificacionRepo)
    {
      _categoriaRepo = categoriaRepo;
      _eventoRepo = eventoRepo;
      _notificacionRepo = notificacionRepo;
    }

    //ENDPOINTS PUBLICOS
    /* Obtiene todas las categorias de un evento, endpoint publico - cualquiera puede ver las categorias
     incluye informacion de cupos disponibles*/
    [AllowAnonymous]
    [HttpGet]
    public async Task<IActionResult> ObtenerCategorias(int idEvento)
    {
      try
      {
        // Verificar que el evento existe
        var evento = await _eventoRepo.ObtenerPorIdAsync(idEvento);
        if (evento == null)
          return NotFound(new { message = "Evento no encontrado" });

        var categorias = await _categoriaRepo.ObtenerPorEventoAsync(idEvento);

        // Agregar info de inscriptos a cada categoria
        var categoriasResponse = new List<CategoriaEventoResponse>();
        foreach (var cat in categorias)
        {
          var inscriptos = await _categoriaRepo.ContarInscriptosAsync(cat.IdCategoria);

          categoriasResponse.Add(new CategoriaEventoResponse
          {
            IdCategoria = cat.IdCategoria,
            IdEvento = cat.IdEvento,
            Nombre = cat.Nombre,
            CostoInscripcion = cat.CostoInscripcion,
            CupoCategoria = cat.CupoCategoria,
            EdadMinima = cat.EdadMinima,
            EdadMaxima = cat.EdadMaxima,
            Genero = cat.Genero,
            InscriptosActuales = inscriptos
            // CuposDisponibles y TieneCupo se calculan automaticamente en el DTO
          });
        }

        return Ok(new
        {
          idEvento,
          nombreEvento = evento.Nombre,
          totalCategorias = categoriasResponse.Count,
          categorias = categoriasResponse
        });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al obtener categorías", error = ex.Message });
      }
    }

    // Obtiene una categoria especifica por ID
    [AllowAnonymous]
    [HttpGet("{idCategoria}")]
    public async Task<IActionResult> ObtenerCategoria(int idEvento, int idCategoria)
    {
      try
      {
        var categoria = await _categoriaRepo.ObtenerPorIdAsync(idCategoria);

        if (categoria == null)
          return NotFound(new { message = "Categoría no encontrada" });

        // Verificar que pertenece al evento
        if (categoria.IdEvento != idEvento)
          return NotFound(new { message = "La categoría no pertenece a este evento" });

        var inscriptos = await _categoriaRepo.ContarInscriptosAsync(idCategoria);

        return Ok(new CategoriaEventoResponse
        {
          IdCategoria = categoria.IdCategoria,
          IdEvento = categoria.IdEvento,
          Nombre = categoria.Nombre,
          CostoInscripcion = categoria.CostoInscripcion,
          CupoCategoria = categoria.CupoCategoria,
          EdadMinima = categoria.EdadMinima,
          EdadMaxima = categoria.EdadMaxima,
          Genero = categoria.Genero,
          InscriptosActuales = inscriptos
          // CuposDisponibles y TieneCupo se calculan automaticamente en el DTO
        });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al obtener la categoría", error = ex.Message });
      }
    }

    /*Obtiene categorias disponibles para un runner segun su edad y genero, endpoint publico -
    el runner puede ver en qu categorias puede inscribirse - filtra por edad, genero y cupo disponible*/
    [AllowAnonymous]
    [HttpGet("Disponibles")]
    public async Task<IActionResult> ObtenerCategoriasDisponibles(int idEvento, [FromQuery] int edad, [FromQuery] string genero)
    {
      try
      {
        var evento = await _eventoRepo.ObtenerPorIdAsync(idEvento);
        if (evento == null)
          return NotFound(new { message = "Evento no encontrado" });

        // Validar genero
        genero = genero?.ToUpper() ?? "X";
        if (genero != "F" && genero != "M" && genero != "X")
          return BadRequest(new { message = "Género debe ser F, M o X" });

        var categorias = await _categoriaRepo.ObtenerPorEventoAsync(idEvento);

        var categoriasDisponibles = new List<CategoriaEventoResponse>();

        foreach (var cat in categorias)
        {
          // Verificar rango de edad
          if (edad < cat.EdadMinima || edad > cat.EdadMaxima)
            continue;

          // Verificar genero (X = mixto/todos)
          if (cat.Genero != "X" && cat.Genero != genero)
            continue;

          // Verificar cupo disponible
          var tieneCupo = await _categoriaRepo.TieneCupoDisponibleAsync(cat.IdCategoria);
          if (!tieneCupo)
            continue;

          var inscriptos = await _categoriaRepo.ContarInscriptosAsync(cat.IdCategoria);

          categoriasDisponibles.Add(new CategoriaEventoResponse
          {
            IdCategoria = cat.IdCategoria,
            IdEvento = cat.IdEvento,
            Nombre = cat.Nombre,
            CostoInscripcion = cat.CostoInscripcion,
            CupoCategoria = cat.CupoCategoria,
            EdadMinima = cat.EdadMinima,
            EdadMaxima = cat.EdadMaxima,
            Genero = cat.Genero,
            InscriptosActuales = inscriptos
            // CuposDisponibles y TieneCupo se calculan automaticamente en el DTO
          });
        }

        return Ok(new
        {
          idEvento,
          nombreEvento = evento.Nombre,
          edadConsultada = edad,
          generoConsultado = genero,
          totalDisponibles = categoriasDisponibles.Count,
          categorias = categoriasDisponibles
        });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al obtener categorías disponibles", error = ex.Message });
      }
    }


    //ENDPOINTS ORGANIZADOR
    /* Crea una nueva categoria en un evento, requiere: Token JWT de Organizador (dueño del evento)
    No permite crear si el evento esta cancelado o finalizado */
    [Authorize(Roles = "organizador")]
    [HttpPost]
    public async Task<IActionResult> CrearCategoria(int idEvento, [FromBody] CrearCategoriaRequest request)
    {
      try
      {
        if (!ModelState.IsValid)
          return BadRequest(ModelState);

        int userId = User.ObtenerUserId();

        // Verificar evento
        var evento = await _eventoRepo.ObtenerPorIdAsync(idEvento);
        if (evento == null)
          return NotFound(new { message = "Evento no encontrado" });

        //verificar organizador
        if (evento.IdOrganizador != userId)
          return StatusCode(403, new { message = "No tienes permiso para modificar este evento" });

        if (evento.Estado == "cancelado" || evento.Estado == "finalizado")
          return BadRequest(new { message = $"No se pueden agregar categorías a un evento {evento.Estado}" });

        // Validaciones de negocio
        if (!_categoriaRepo.ValidarRangoEdades(request.EdadMinima, request.EdadMaxima))
          return BadRequest(new { message = "La edad mínima no puede ser mayor que la edad máxima" });

        if (await _categoriaRepo.ExisteNombreEnEventoAsync(idEvento, request.Nombre))
          return Conflict(new { message = "Ya existe una categoría con este nombre en el evento" });

        if (!await _categoriaRepo.ValidarCupoContraEventoAsync(idEvento, request.CupoCategoria))
          return BadRequest(new { message = "El cupo de la categoría no puede exceder el cupo total del evento" });

        // Crear categoria
        var categoria = new CategoriaEvento
        {
          IdEvento = idEvento,
          Nombre = request.Nombre.Trim(),
          CostoInscripcion = request.CostoInscripcion,
          CupoCategoria = request.CupoCategoria,
          EdadMinima = request.EdadMinima,
          EdadMaxima = request.EdadMaxima,
          Genero = request.Genero?.ToUpper() ?? "X"
        };

        var categoriaCreada = await _categoriaRepo.CrearAsync(categoria);

        return CreatedAtAction(nameof(ObtenerCategoria), new { idEvento, idCategoria = categoriaCreada.IdCategoria },
          new
          {
            message = "Categoría creada correctamente",
            categoria = new
            {
              idCategoria = categoriaCreada.IdCategoria,
              nombre = categoriaCreada.Nombre,
              costoInscripcion = categoriaCreada.CostoInscripcion,
              cupoCategoria = categoriaCreada.CupoCategoria,
              edadMinima = categoriaCreada.EdadMinima,
              edadMaxima = categoriaCreada.EdadMaxima,
              genero = categoriaCreada.Genero
            }
          });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al crear la categoría", error = ex.Message });
      }
    }

    /* Actualiza una categoria existente, requiere: Token JWT de Organizador (dueño del evento)
     No permite reducir cupo por debajo de inscriptos actuales */
    [Authorize(Roles = "organizador")]
    [HttpPut("{idCategoria}")]
    public async Task<IActionResult> ActualizarCategoria(int idEvento, int idCategoria, [FromBody] ActualizarCategoriaRequest request)
    {
      try
      {
        if (!ModelState.IsValid)
          return BadRequest(ModelState);

        int userId = User.ObtenerUserId();

        // Verificar evento
        var evento = await _eventoRepo.ObtenerPorIdAsync(idEvento);
        if (evento == null)
          return NotFound(new { message = "Evento no encontrado" });

        //verificar organaizador
        if (evento.IdOrganizador != userId)
          return StatusCode(403, new { message = "No tienes permiso para modificar este evento" });

        if (evento.Estado == "cancelado" || evento.Estado == "finalizado")
          return BadRequest(new { message = $"No se pueden modificar categorías de un evento {evento.Estado}" });

        // Verificar que la categoria existe y pertenece al evento
        var categoria = await _categoriaRepo.ObtenerPorIdYEventoAsync(idCategoria, idEvento);
        if (categoria == null)
          return NotFound(new { message = "Categoría no encontrada en este evento" });

        // Validaciones de negocio
        if (!_categoriaRepo.ValidarRangoEdades(request.EdadMinima, request.EdadMaxima))
          return BadRequest(new { message = "La edad mínima no puede ser mayor que la edad máxima" });

        // Verificar nombre unico (si cambio)
        if (request.Nombre.ToLower().Trim() != categoria.Nombre.ToLower() &&
            await _categoriaRepo.ExisteNombreEnEventoAsync(idEvento, request.Nombre, idCategoria))
          return Conflict(new { message = "Ya existe otra categoría con este nombre en el evento" });

        // Verificar cupo vs inscriptos
        if (request.CupoCategoria.HasValue)
        {
          var inscriptos = await _categoriaRepo.ContarInscriptosAsync(idCategoria);
          if (request.CupoCategoria.Value < inscriptos)
            return BadRequest(new { message = $"No se puede reducir el cupo a {request.CupoCategoria}. Ya hay {inscriptos} inscriptos" });
        }

        // Actualizar
        categoria.Nombre = request.Nombre.Trim();
        categoria.CostoInscripcion = request.CostoInscripcion;
        categoria.CupoCategoria = request.CupoCategoria;
        categoria.EdadMinima = request.EdadMinima;
        categoria.EdadMaxima = request.EdadMaxima;
        categoria.Genero = request.Genero?.ToUpper() ?? "X";

        await _categoriaRepo.ActualizarAsync(categoria);

        return Ok(new { message = "Categoría actualizada correctamente" });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al actualizar la categoría", error = ex.Message });
      }
    }

    /* Elimina una categoria, requiere: Token JWT de Organizador(dueño del evento)
     No permite eliminar si tiene inscripciones */
    [Authorize(Roles = "organizador")]
    [HttpDelete("{idCategoria}")]
    public async Task<IActionResult> EliminarCategoria(int idEvento, int idCategoria)
    {
      try
      {
        int userId = User.ObtenerUserId();

        // Verificar evento
        var evento = await _eventoRepo.ObtenerPorIdAsync(idEvento);
        if (evento == null)
          return NotFound(new { message = "Evento no encontrado" });

        if (evento.IdOrganizador != userId)
          return StatusCode(403, new { message = "No tienes permiso para modificar este evento" });

        // Verificar categoria
        var categoria = await _categoriaRepo.ObtenerPorIdYEventoAsync(idCategoria, idEvento);
        if (categoria == null)
          return NotFound(new { message = "Categoría no encontrada en este evento" });

        // Verificar que no tenga inscripciones
        if (await _categoriaRepo.TieneInscripcionesAsync(idCategoria))
          return BadRequest(new { message = "No se puede eliminar la categoría porque tiene inscripciones" });

        await _categoriaRepo.EliminarAsync(categoria);

        return Ok(new { message = "Categoría eliminada correctamente" });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al eliminar la categoría", error = ex.Message });
      }
    }

    // Obtiene estadisticas de inscriptos por categoria (para el organizador)
    [Authorize(Roles = "organizador")]
    [HttpGet("Estadisticas")]
    public async Task<IActionResult> ObtenerEstadisticas(int idEvento)
    {
      try
      {
        int userId = User.ObtenerUserId();

        var evento = await _eventoRepo.ObtenerPorIdAsync(idEvento);
        if (evento == null)
          return NotFound(new { message = "Evento no encontrado" });

        if (evento.IdOrganizador != userId)
          return StatusCode(403, new { message = "No tienes permiso para ver estadísticas de este evento" });

        var categorias = await _categoriaRepo.ObtenerPorEventoAsync(idEvento);
        var inscriptosPorCategoria = await _categoriaRepo.ObtenerInscriptosPorCategoriaAsync(idEvento);

        var estadisticas = categorias.Select(c => new
        {
          idCategoria = c.IdCategoria,
          nombre = c.Nombre,
          cupoTotal = c.CupoCategoria,
          inscriptos = inscriptosPorCategoria.ContainsKey(c.IdCategoria) ? inscriptosPorCategoria[c.IdCategoria] : 0,
          cupoDisponible = c.CupoCategoria.HasValue
            ? c.CupoCategoria.Value - (inscriptosPorCategoria.ContainsKey(c.IdCategoria) ? inscriptosPorCategoria[c.IdCategoria] : 0)
            : (int?)null,
          porcentajeOcupacion = c.CupoCategoria.HasValue && c.CupoCategoria.Value > 0
            ? Math.Round((decimal)(inscriptosPorCategoria.ContainsKey(c.IdCategoria) ? inscriptosPorCategoria[c.IdCategoria] : 0) / c.CupoCategoria.Value * 100, 1)
            : 0
        }).ToList();

        return Ok(new
        {
          idEvento,
          nombreEvento = evento.Nombre,
          totalCategorias = categorias.Count,
          totalInscriptos = inscriptosPorCategoria.Values.Sum(),
          categorias = estadisticas
        });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al obtener estadísticas", error = ex.Message });
      }
    }


    //PUT cambiar estado de una categoria especifica (ej la 10k)
    //se retrasa 1 hs, pero no afecta a la de 20k, que corre en el mismo circuito
    //cada categoria tiene su estado
    [HttpPut("{idCategoria}/CambiarEstado")]
    [Authorize(Roles = "organizador")]
    public async Task<IActionResult> CambiarEstadoCategoria([FromRoute] int idEvento, [FromRoute] int idCategoria, [FromBody] CambiarEstadoCategoriaRequest request)
    {
      try
      {
        if (!ModelState.IsValid) return BadRequest(ModelState);

        int userId = User.ObtenerUserId();

        // obtener la categoria con el Evento (Usando el metodo nuevo del Repo)
        var categoria = await _categoriaRepo.ObtenerPorIdConEventoAsync(idCategoria);

        if (categoria == null)
          return NotFound(new { message = "Categoría no encontrada" });

        //validamos URL idEvento coincida con la categoria
        if(categoria.IdEvento !=idEvento)
          return BadRequest(new {message = "La categoria no pertenece al evento especifico"});

        // Validar que el evento padre pertenezca al organizador
        if (categoria.Evento == null || categoria.Evento.IdOrganizador != userId)
          return StatusCode(403, new { message = "No tienes permiso para modificar este evento" });

        //verificamos si el evento esta finalizado
        string estadoEvento = categoria.Evento.Estado?.ToLower() ?? "";
        if (estadoEvento == "finalizado" || estadoEvento == "cancelado")
            return BadRequest(new { message = $"Acción denegada: El evento ya se encuentra {estadoEvento}." });

        //Verificar que la CATEGORIA no este cerrada
        string estadoActualCategoria = categoria.Estado?.ToLower() ?? "";
        if (estadoActualCategoria == "finalizada" || estadoActualCategoria == "cancelada")
            return BadRequest(new { message = $"Acción denegada: No se puede modificar una categoría que ya está {estadoActualCategoria}." });

        string nuevoEstado = request.NuevoEstado.ToLower();

        // Validacion de fecha para finalizar la categoria
        if (nuevoEstado == "finalizada" && DateTime.Now.Date < categoria.Evento.FechaHora.Date)
        {
            return BadRequest(new { message = $"Acción denegada: Solo puedes finalizar la categoría el día de la carrera o en fechas posteriores ({categoria.Evento.FechaHora.ToString("dd/MM/yyyy")})." });
        }

        // Actualizar el estado de la Categoria (Usando Repo)
        categoria.Estado = nuevoEstado;
        await _categoriaRepo.ActualizarAsync(categoria);

        //LOGICA DE VERIFICACION
        // Si esta categoria finalizo, verificamos si debemos cerrar el evento completo
        if (nuevoEstado == "finalizada")
        {
          // Traemos todas las categorias del evento para ver sus estados
          var todasLasCategorias = await _categoriaRepo.ObtenerPorEventoAsync(categoria.IdEvento);

          // Verificamos si queda alguna que NO este finalizada ni cancelada
          bool quedanActivas = todasLasCategorias
              .Any(c => c.Estado != "finalizada" && c.Estado != "cancelada");

          if (!quedanActivas)
          {
            // Si no quedan activas, cerramos el evento padre usando el Repo
            await _eventoRepo.CambiarEstadoAsync(categoria.IdEvento, "finalizado");
          }
        }

        // Notificacion (solo a esta categoria)
        if (!string.IsNullOrEmpty(request.Motivo))
        {
          string titulo = $"AVISO: {categoria.Nombre} {request.NuevoEstado.ToUpper()}";

          if (nuevoEstado == "retrasada") titulo = $"Retraso en {categoria.Nombre}";
          if (nuevoEstado == "cancelada") titulo = $"{categoria.Nombre} CANCELADA";
          if (nuevoEstado == "finalizada") titulo = $"{categoria.Nombre} Finalizada";

          var notif = new CrearNotificacionRequest
          {
            IdEvento = categoria.IdEvento,
            IdCategoria = categoria.IdCategoria,
            Titulo = titulo,
            Mensaje = request.Motivo
          };

          await _notificacionRepo.CrearAsync(notif, userId);
        }

        return Ok(new
        {
          message = $"Categoría actualizada a {nuevoEstado}",
          idCategoria = idCategoria,
          estadoEventoPadre = categoria.Evento.Estado // Para que el front sepa si cambio el padre
        });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error interno", error = ex.Message });
      }
    }



  }
}
