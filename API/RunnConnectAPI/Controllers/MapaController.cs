// Controllers/MapaController.cs
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using RunnConnectAPI.Models.Dto.Ruta;
using RunnConnectAPI.Models.Dto.PuntoInteres;
using RunnConnectAPI.Repositories;
using System.Security.Claims;
using RunnConnectAPI.Services;

namespace RunnConnectAPI.Controllers
{
  /// Controller para gestion de Rutas y Puntos de Interes de eventos
  /// Permite al organizador dibujar la ruta y marcar puntos importantes
  [Authorize]
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
    [AllowAnonymous]
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
    [AllowAnonymous]
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

    /// Guarda la ruta completa de un evento (no reemplaza la existente)
    /// Requiere: Token JWT de Organizador (dueño del evento)
    /// Los puntos deben venir en orden (el sistema asigna el numero de orden automaticamente)
    [Authorize(Roles="organizador")]
    [HttpPut("Ruta")]
    public async Task<IActionResult> GuardarRuta(int idEvento, [FromBody] GuardarRutaRequest request)
    {
      try
      {
        if (!ModelState.IsValid)
          return BadRequest(ModelState);

        int userId = User.ObtenerUserId();

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
    [Authorize(Roles="organizador")]
    [HttpDelete("Ruta")]
    public async Task<IActionResult> EliminarRuta(int idEvento)
    {
      try
      {
        int userId = User.ObtenerUserId();

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
    [AllowAnonymous]
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
    [AllowAnonymous]
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
    [Authorize(Roles="organizador")]
    [HttpPost("PuntosInteres")]
    public async Task<IActionResult> CrearPuntoInteres(int idEvento, [FromBody] CrearPuntoInteresRequest request)
    {
      try
      {
        if (!ModelState.IsValid)
          return BadRequest(ModelState);

        int userId = User.ObtenerUserId();

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
    [Authorize(Roles="organizador")]
    [HttpPost("PuntosInteres/Batch")]
    public async Task<IActionResult> CrearPuntosInteresMultiples(
      int idEvento, [FromBody] List<CrearPuntoInteresRequest> puntos)
    {
      try
      {
        if (!ModelState.IsValid)
          return BadRequest(ModelState);

        if (puntos == null || !puntos.Any())
          return BadRequest(new { message = "Debe incluir al menos un punto de interés" });

        int userId = User.ObtenerUserId();

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
    [Authorize(Roles="organizador")]
    [HttpPut("PuntosInteres/{idPunto}")]
    public async Task<IActionResult> ActualizarPuntoInteres(
      int idEvento, int idPunto, [FromBody] ActualizarPuntoInteresRequest request)
    {
      try
      {
        if (!ModelState.IsValid)
          return BadRequest(ModelState);

        int userId = User.ObtenerUserId();

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
     [Authorize(Roles="organizador")]
    [HttpDelete("PuntosInteres/{idPunto}")]
    public async Task<IActionResult> EliminarPuntoInteres(int idEvento, int idPunto)
    {
      try
      {
        int userId = User.ObtenerUserId();

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
    [Authorize(Roles="organizador")]
    [HttpDelete("PuntosInteres")]
    public async Task<IActionResult> EliminarTodosPuntosInteres(int idEvento)
    {
      try
      {
        int userId = User.ObtenerUserId();

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


  }
}
