// Controllers/MapaController.cs
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using RunnConnectAPI.Models.Dto.Ruta;
using RunnConnectAPI.Models.Dto.PuntoInteres;
using RunnConnectAPI.Repositories;
// using System.Security.Claims;
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
    Endpoint publico - 
    Retorna toda la informacion necesaria para mostrar el mapa */
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


    // Guarda la ruta completa de un evento
    //Requiere: Token JWT de Organizador (dueño del evento)
    // Los puntos deben venir en orden
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



    // PUNTOS DE INTERES 
    /// Obtiene todos los puntos de interes de un evento
    /// Endpoint publico
    /// Retorna: hidratacion, primeros auxilios, etc
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

        return StatusCode(201, new
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



  }
}
