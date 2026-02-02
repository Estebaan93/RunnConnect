// Controllers/MapaController.cs
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using RunnConnectAPI.Models.Dto.Ruta;
using RunnConnectAPI.Models.Dto.PuntoInteres;
using RunnConnectAPI.Repositories;
using System.Security.Claims;

namespace RunnConnectAPI.Controllers
{
  /// Controller para gestion de Rutas y Puntos de Interes de eventos
  /// Permite al organizador dibujar la ruta y marcar puntos importantes
  [ApiController]
  [Route("api/Evento/{idEvento}")]
  public class MapaController : ControllerBase
  {
    private readonly RutaRepositorio _rutaRepo;

    public MapaController(RutaRepositorio rutaRepo)
    {
      _rutaRepo = rutaRepo;
    }

    // MAPA COMPLETO 
    /* Obtiene el mapa completo del evento (ruta + puntos de interes)
    Endpoint publico - ideal para la app Android
    Retorna toda la información necesaria para mostrar el mapa */
    
    [HttpGet("Mapa")]
    public async Task<IActionResult> ObtenerMapaCompleto(int idEvento)
    {
      try
      {
        var mapa = await _rutaRepo.ObtenerMapaCompletoAsync(idEvento);

        if (mapa == null)
          return NotFound(new { message = "Evento no encontrado" });

        return Ok(mapa);
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al obtener el mapa", error = ex.Message });
      }
    }


    //RUTAS 
    /* Obtiene la ruta (trazado GPS) de un evento
    Endpoint publico
    Retorna los puntos ordenados que forman el recorrido */
    [HttpGet("Ruta")]
    public async Task<IActionResult> ObtenerRuta(int idEvento)
    {
      try
      {
        var ruta = await _rutaRepo.ObtenerRutaEventoAsync(idEvento);

        if (ruta == null)
          return NotFound(new { message = "Evento no encontrado" });

        return Ok(ruta);
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al obtener la ruta", error = ex.Message });
      }
    }

    /// Guarda la ruta completa de un evento (reemplaza la existente)
    /// Requiere: Token JWT de Organizador (dueño del evento)
    /// Si ya existe una ruta, la reemplaza completamente
    /// Los puntos deben venir en orden (el sistema asigna el número de orden automaticamente)
    [HttpPut("Ruta")]
    [Authorize]
    public async Task<IActionResult> GuardarRuta(int idEvento, [FromBody] GuardarRutaRequest request)
    {
      try
      {
        if (!ModelState.IsValid)
          return BadRequest(ModelState);

        var (userId, error) = ValidarOrganizador();
        if (error != null) return error;

        var (exito, errorMsg) = await _rutaRepo.GuardarRutaAsync(idEvento, request, userId);

        if (!exito)
          return BadRequest(new { message = errorMsg });

        return Ok(new
        {
          message = "Ruta guardada correctamente",
          totalPuntos = request.Puntos.Count
        });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al guardar la ruta", error = ex.Message });
      }
    }

    /// Elimina toda la ruta de un evento
    [HttpDelete("Ruta")]
    [Authorize]
    public async Task<IActionResult> EliminarRuta(int idEvento)
    {
      try
      {
        var (userId, error) = ValidarOrganizador();
        if (error != null) return error;

        var (exito, errorMsg) = await _rutaRepo.EliminarRutaAsync(idEvento, userId);

        if (!exito)
          return BadRequest(new { message = errorMsg });

        return Ok(new { message = "Ruta eliminada correctamente" });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al eliminar la ruta", error = ex.Message });
      }
    }


    // PUNTOS DE INTERES 
    /// Obtiene todos los puntos de interes de un evento
    /// Endpoint publico
    /// Retorna: hidratacion, primeros auxilios, etc.
    [HttpGet("PuntosInteres")]
    public async Task<IActionResult> ObtenerPuntosInteres(int idEvento)
    {
      try
      {
        var puntos = await _rutaRepo.ObtenerPuntosInteresEventoAsync(idEvento);

        if (puntos == null)
          return NotFound(new { message = "Evento no encontrado" });

        return Ok(puntos);
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al obtener puntos de interés", error = ex.Message });
      }
    }

    /// Obtiene un punto de interes especifico
    [HttpGet("PuntosInteres/{idPunto}")]
    public async Task<IActionResult> ObtenerPuntoInteres(int idEvento, int idPunto)
    {
      try
      {
        var punto = await _rutaRepo.ObtenerPuntoInteresPorIdAsync(idPunto);

        if (punto == null)
          return NotFound(new { message = "Punto de interés no encontrado" });

        // Verificar que pertenece al evento
        if (punto.IdEvento != idEvento)
          return NotFound(new { message = "El punto de interés no pertenece a este evento" });

        return Ok(punto);
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al obtener el punto de interés", error = ex.Message });
      }
    }

    /// Crea un nuevo punto de interes
    /// Requiere: Token JWT de Organizador (dueño del evento)
    /// Tipos validos: hidratacion, primeros auxilios, punto de energia, otro
    [HttpPost("PuntosInteres")]
    [Authorize]
    public async Task<IActionResult> CrearPuntoInteres(int idEvento, [FromBody] CrearPuntoInteresRequest request)
    {
      try
      {
        if (!ModelState.IsValid)
          return BadRequest(ModelState);

        var (userId, error) = ValidarOrganizador();
        if (error != null) return error;

        var (punto, errorMsg) = await _rutaRepo.CrearPuntoInteresAsync(idEvento, request, userId);

        if (punto == null)
          return BadRequest(new { message = errorMsg });

        return CreatedAtAction(
          nameof(ObtenerPuntoInteres),
          new { idEvento, idPunto = punto.IdPuntoInteres },
          new
          {
            message = "Punto de interés creado correctamente",
            puntoInteres = new
            {
              idPuntoInteres = punto.IdPuntoInteres,
              tipo = punto.Tipo,
              nombre = punto.Nombre,
              latitud = punto.Latitud,
              longitud = punto.Longitud
            }
          });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al crear el punto de interés", error = ex.Message });
      }
    }

    /// Crea multiples puntos de interes a la vez
    /// Requiere: Token JWT de Organizador (dueño del evento)
    /// Util para cargar todos los puntos de una vez desde la app
    [HttpPost("PuntosInteres/Batch")]
    [Authorize]
    public async Task<IActionResult> CrearPuntosInteresMultiples(
      int idEvento, [FromBody] List<CrearPuntoInteresRequest> puntos)
    {
      try
      {
        if (!ModelState.IsValid)
          return BadRequest(ModelState);

        if (puntos == null || !puntos.Any())
          return BadRequest(new { message = "Debe incluir al menos un punto de interés" });

        var (userId, error) = ValidarOrganizador();
        if (error != null) return error;

        var (creados, errorMsg) = await _rutaRepo.CrearPuntosInteresMultiplesAsync(idEvento, puntos, userId);

        if (creados == 0 && errorMsg != null)
          return BadRequest(new { message = errorMsg });

        return Ok(new
        {
          message = $"{creados} puntos de interés creados correctamente",
          totalCreados = creados
        });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al crear puntos de interés", error = ex.Message });
      }
    }

    /// Actualiza un punto de interes existente
    [HttpPut("PuntosInteres/{idPunto}")]
    [Authorize]
    public async Task<IActionResult> ActualizarPuntoInteres(
      int idEvento, int idPunto, [FromBody] ActualizarPuntoInteresRequest request)
    {
      try
      {
        if (!ModelState.IsValid)
          return BadRequest(ModelState);

        var (userId, error) = ValidarOrganizador();
        if (error != null) return error;

        var (exito, errorMsg) = await _rutaRepo.ActualizarPuntoInteresAsync(idPunto, request, userId);

        if (!exito)
          return BadRequest(new { message = errorMsg });

        return Ok(new { message = "Punto de interés actualizado correctamente" });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al actualizar el punto de interés", error = ex.Message });
      }
    }

    /// Elimina un punto de interes
    [HttpDelete("PuntosInteres/{idPunto}")]
    [Authorize]
    public async Task<IActionResult> EliminarPuntoInteres(int idEvento, int idPunto)
    {
      try
      {
        var (userId, error) = ValidarOrganizador();
        if (error != null) return error;

        var (exito, errorMsg) = await _rutaRepo.EliminarPuntoInteresAsync(idPunto, userId);

        if (!exito)
          return BadRequest(new { message = errorMsg });

        return Ok(new { message = "Punto de interés eliminado correctamente" });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al eliminar el punto de interés", error = ex.Message });
      }
    }

    /// Elimina todos los puntos de interes de un evento
    [HttpDelete("PuntosInteres")]
    [Authorize]
    public async Task<IActionResult> EliminarTodosPuntosInteres(int idEvento)
    {
      try
      {
        var (userId, error) = ValidarOrganizador();
        if (error != null) return error;

        var (exito, errorMsg) = await _rutaRepo.EliminarTodosPuntosInteresAsync(idEvento, userId);

        if (!exito)
          return BadRequest(new { message = errorMsg });

        return Ok(new { message = "Todos los puntos de interés eliminados correctamente" });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al eliminar puntos de interés", error = ex.Message });
      }
    }


    // HELPERS PRIVADOS 
    private (int userId, IActionResult? error) ValidarOrganizador()
    {
      var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier);
      if (userIdClaim == null)
        return (0, Unauthorized(new { message = "No autorizado" }));

      var userId = int.Parse(userIdClaim.Value);

      var tipoUsuarioClaim = User.FindFirst("TipoUsuario");
      if (tipoUsuarioClaim == null || tipoUsuarioClaim.Value.ToLower() != "organizador")
        return (0, BadRequest(new { message = "Solo los organizadores pueden realizar esta acción" }));

      return (userId, null);
    }

  }
}
