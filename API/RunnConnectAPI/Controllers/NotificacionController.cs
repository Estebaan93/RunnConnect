// Controllers/NotificacionController.cs
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using RunnConnectAPI.Models.Dto.Notificacion;
using RunnConnectAPI.Repositories;
using System.Security.Claims;

namespace RunnConnectAPI.Controllers
{
  /// Controller para gestion de notificaciones de eventos (sistema PULL/buzon)
  /// Los runners ven las notificaciones al abrir la app
  [Authorize]
  [ApiController]
  [Route("api/[controller]")]
  public class NotificacionController : ControllerBase
  {
    private readonly NotificacionRepositorio _notificacionRepo;

    public NotificacionController(NotificacionRepositorio notificacionRepo)
    {
      _notificacionRepo = notificacionRepo;
    }

    // ENDPOINTS PUBLICOS 
    /// Obtiene una notificacion por ID
    /// Endpoint publico - cualquiera puede ver una notificacion especifica
    [AllowAnonymous]
    [HttpGet("{id}")]
    public async Task<IActionResult> ObtenerPorId(int id)
    {
      try
      {
        var notificacion = await _notificacionRepo.ObtenerPorIdAsync(id);

        if (notificacion == null)
          return NotFound(new { message = "Notificación no encontrada" });

        return Ok(notificacion);
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al obtener la notificacion", error = ex.Message });
      }
    }

    /// Obtiene todas las notificaciones de un evento
    /// Endpoint publico - cualquiera puede ver las notificaciones de un evento
    /// Ordenadas por fecha (más recientes primero)
    [AllowAnonymous]
    [HttpGet("Evento/{idEvento}")]
    public async Task<IActionResult> ObtenerPorEvento(int idEvento)
    {
      try
      {
        var notificaciones = await _notificacionRepo.ObtenerPorEventoAsync(idEvento);

        return Ok(new
        {
          idEvento,
          total = notificaciones.Count,
          notificaciones
        });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al obtener notificaciones del evento", error = ex.Message });
      }
    }


    // ENDPOINTS RUNNER 
    /// Obtiene las notificaciones del runner autenticado
    /// Requiere: Token JWT de Runner
    /// Retorna notificaciones de eventos donde esta inscripto (pago confirmado)
    /// Ordenadas por fecha (más recientes primero)
    /// Este es el endpoint principal para el "buzon" de notificaciones en la app
    [Authorize(Roles="runner")]
    [HttpGet("MisNotificaciones")]
    public async Task<IActionResult> MisNotificaciones()
    {
      try
      {
        int userId = ObtenerUserIdDelToken();

        var resultado = await _notificacionRepo.ObtenerMisNotificacionesAsync(userId);
        return Ok(resultado);
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al obtener tus notificaciones", error = ex.Message });
      }
    }

    /// Obtiene el contador de notificaciones recientes (ultimas 24h)
    /// Requiere: Token JWT de Runner
    /// Util para mostrar badge/contador en la app
    [Authorize(Roles= "runner")]
    [HttpGet("ContadorRecientes")]
    public async Task<IActionResult> ContadorRecientes()
    {
      try
      {
        int userId = ObtenerUserIdDelToken();

        var cantidad = await _notificacionRepo.ContarNotificacionesRecientesAsync(userId);

        return Ok(new
        {
          cantidadRecientes = cantidad,
          tieneNuevas = cantidad > 0
        });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al obtener contador", error = ex.Message });
      }
    }

    /// Marca todas las notificaciones como leidas (limpia el contador)
    /// La App debe llamar a esto cuando el usuario abre la pantalla "Mis Notificaciones"
    [Authorize(Roles="runner")]
    [HttpPost("MarcarComoLeidas")]
    public async Task<IActionResult> MarcarComoLeidas()
    {
      try
      {
        int userId = ObtenerUserIdDelToken();

        await _notificacionRepo.MarcarTodasComoLeidasAsync(userId);

        return Ok(new
        {
          message = "Buzon marcado como leido"
        });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new
        {
          message = "Error al marcar como leidas",
          error = ex.Message
        });
      }
    }


    // ENDPOINTS ORGANIZADOR 
    /// Crea una nueva notificacion para un evento
    /// Requiere: Token JWT de Organizador (dueño del evento)
    /// La notificacion queda disponible inmediatamente para los runners inscriptos
    [Authorize(Roles="organizador")]
    [HttpPost]
    public async Task<IActionResult> CrearNotificacion([FromBody] CrearNotificacionRequest request)
    {
      try
      {
        if (!ModelState.IsValid)
          return BadRequest(ModelState);

        int userId = ObtenerUserIdDelToken();

        var (notificacion, errorMsg) = await _notificacionRepo.CrearAsync(request, userId);

        if (notificacion == null)
          return BadRequest(new { message = errorMsg });

        return CreatedAtAction(
          nameof(ObtenerPorId),
          new { id = notificacion.IdNotificacion },
          new
          {
            message = "Notificacion creada correctamente",
            notificacion = new
            {
              idNotificacion = notificacion.IdNotificacion,
              idEvento = notificacion.IdEvento,
              titulo = notificacion.Titulo,
              mensaje = notificacion.Mensaje,
              fechaEnvio = notificacion.FechaEnvio
            }
          });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al crear la notificacion", error = ex.Message });
      }
    }

    /// Actualiza una notificacion existente
    /// Requiere: Token JWT de Organizador (dueño del evento)
    /// No modifica la fecha de envio original
    [Authorize(Roles="organizador")]
    [HttpPut("{id}")]
    public async Task<IActionResult> ActualizarNotificacion(int id, [FromBody] ActualizarNotificacionRequest request)
    {
      try
      {
        if (!ModelState.IsValid)
          return BadRequest(ModelState);

        int userId = ObtenerUserIdDelToken();

        var (exito, errorMsg) = await _notificacionRepo.ActualizarAsync(id, request, userId);

        if (!exito)
          return BadRequest(new { message = errorMsg });

        return Ok(new { message = "Notificacion actualizada correctamente" });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al actualizar la notificacion", error = ex.Message });
      }
    }

    /// Elimina una notificacion
    /// Requiere: Token JWT de Organizador (dueño del evento)
    /// Eliminacion fisica de la BD
    [Authorize(Roles="organizador")]
    [HttpDelete("{id}")]
    public async Task<IActionResult> EliminarNotificacion(int id)
    {
      try
      {
        int userId = ObtenerUserIdDelToken();

        var (exito, errorMsg) = await _notificacionRepo.EliminarAsync(id, userId);

        if (!exito)
          return BadRequest(new { message = errorMsg });

        return Ok(new { message = "Notificacion eliminada correctamente" });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al eliminar la notificacion", error = ex.Message });
      }
    }


    // HELPERS PRIVADOS 
    private int ObtenerUserIdDelToken()
    {
      var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier);
      if (userIdClaim == null)
        throw new UnauthorizedAccessException("ID de usuario no encontrado");

      return int.Parse(userIdClaim.Value);
    }


  }
}
