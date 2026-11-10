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
