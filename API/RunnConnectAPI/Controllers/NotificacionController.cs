// Controllers/NotificacionController.cs
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using RunnConnectAPI.Models.Dto.Notificacion;
using RunnConnectAPI.Repositories;
using RunnConnectAPI.Services;
// using System.Security.Claims;

namespace RunnConnectAPI.Controllers
{
  /// Controller para gestion de notificaciones de eventos
  
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




    // ENDPOINTS RUNNER 
    /// Obtiene las notificaciones del runner autenticado
    /// Requiere: Token JWT de Runner
    /// Retorna notificaciones de eventos donde esta inscripto (pago confirmado)
    /// Ordenadas por fecha (más recientes primero)
    /// Este es el endpoint principal para el "buzon" de notificaciones en la app
    [Authorize(Roles="runner,organizador")]
    [HttpGet("MisNotificaciones")]
    public async Task<IActionResult> MisNotificaciones()
    {
      try
      {
        int userId = User.ObtenerUserId();

        var resultado = await _notificacionRepo.ObtenerMisNotificacionesAsync(userId);
        return Ok(resultado);
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al obtener tus notificaciones", error = ex.Message });
      }
    }

    /// Obtiene el contador de notificaciones no leídas (desde la última lectura del usuario)
    /// Requiere: Token JWT (Runner u Organizador)
    /// Util para mostrar badge/contador en la app
    [Authorize(Roles="runner,organizador")]
    [HttpGet("ContadorRecientes")]
    public async Task<IActionResult> ContadorRecientes()
    {
      try
      {
        int userId = User.ObtenerUserId();

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
    [Authorize(Roles="runner,organizador")]
    [HttpPost("MarcarComoLeidas")]
    public async Task<IActionResult> MarcarComoLeidas()
    {
      try
      {
        int userId = User.ObtenerUserId();

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

  }
}
