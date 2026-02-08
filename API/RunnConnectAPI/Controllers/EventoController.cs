// Controllers/EventoController.cs
using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using RunnConnectAPI.Models;
using RunnConnectAPI.Models.Dto.Evento;
using RunnConnectAPI.Models.Dto.Categoria;
using RunnConnectAPI.Repositories;
using System.Security.Claims;

using RunnConnectAPI.Models.Dto.Notificacion;

namespace RunnConnectAPI.Controllers
{
  [ApiController]
  [Route("api/[controller]")]
  public class EventoController : ControllerBase
  {
    private readonly EventoRepositorio _eventoRepositorio;
    private readonly UsuarioRepositorio _usuarioRepositorio;
    private readonly CategoriaRepositorio _categoriaRepositorio;

    //notificaciones
    private readonly NotificacionRepositorio _notificacionRepositorio;

    //inyectamos en el constructor
    public EventoController(EventoRepositorio eventoRepositorio, UsuarioRepositorio usuarioRepositorio, CategoriaRepositorio categoriaRepositorio, NotificacionRepositorio notificacionRepositorio) 
    {
      _eventoRepositorio = eventoRepositorio; //asignamos
      _usuarioRepositorio = usuarioRepositorio;
      _categoriaRepositorio = categoriaRepositorio;
      _notificacionRepositorio= notificacionRepositorio; 
    }

    /*Endpoint publicos (sin autenticacion) para el usuario nuevo antes de loguearse, pueda ver los proximos eventos, y posterior
    decide si loguearse o no
    GET: api/Evento/Publicados - obtiene los eventos publicado y futuros*/
    [AllowAnonymous]
    [HttpGet("Publicados")]
    public async Task<IActionResult> ObtenerEventosPublicados([FromQuery] int pagina = 1, [FromQuery] int tamanioPagina = 10)
    {
      try
      {
        // Ahora este metodo requiere paginacion obligatoria
        var (eventos, totalCount) = await _eventoRepositorio.ObtenerEventosPublicadosAsync(pagina, tamanioPagina);

        var totalPaginas = (int)Math.Ceiling(totalCount / (double)tamanioPagina);

        // Mapeo
        var eventosDto = eventos.Select(e => new EventoResumenResponse
        {
          IdEvento = e.IdEvento,
          Nombre = e.Nombre,
          FechaHora = e.FechaHora,
          Lugar = e.Lugar,
          Estado = e.Estado,
          CupoTotal = e.CupoTotal,
          CantidadCategorias = e.Categorias?.Count ?? 0,
          InscriptosActuales = e.Categorias?
                .SelectMany(c => c.Inscripciones)
                .Count(i => i.EstadoPago == "pagado") ?? 0,
          NombreOrganizador = e.Organizador?.Nombre ?? "Sin informacion",
          Categorias = e.Categorias?.Select(c => new CategoriaEventoResponse
          {
              IdCategoria = c.IdCategoria,
              Nombre = c.Nombre, //Esto es lo que necesitamos para el Chip!
              CostoInscripcion = c.CostoInscripcion,
              Genero = c.Genero
          }).ToList()

        }).ToList();

        return Ok(new EventosPaginadosResponse
        {
          Eventos = eventosDto,
          PaginaActual = pagina,
          TotalPaginas = totalPaginas,
          TotalEventos = totalCount,
          TamanioPagina = tamanioPagina
        });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al obtener eventos", error = ex.Message });
      }
    }

    /*Buscan eventos con filtros y paginacion (publico)
    GET: api/Evento/Buscar*/
    [AllowAnonymous]
    [HttpGet("Buscar")]
    public async Task<IActionResult> BuscarEventos([FromQuery] FiltroEventosRequest filtro)
    {
      try
      {
        if (!ModelState.IsValid)
          return BadRequest(ModelState);

        var (eventos, totalCount) = await _eventoRepositorio.BuscarConFiltrosAsync(
            nombre: filtro.Nombre,
            lugar: filtro.Lugar,
            fechaDesde: filtro.FechaDesde,
            fechaHasta: filtro.FechaHasta,
            estado: filtro.Estado,
            idOrganizador: filtro.IdOrganizador,
            pagina: filtro.Pagina,
            tamanioPagina: filtro.TamanioPagina
        );

        var totalPaginas = (int)Math.Ceiling(totalCount / (double)filtro.TamanioPagina);

        var response = new EventosPaginadosResponse
        {
          Eventos = eventos.Select(e => new EventoResumenResponse
          {
            IdEvento = e.IdEvento,
            Nombre = e.Nombre,
            FechaHora = e.FechaHora,
            Lugar = e.Lugar,
            Estado = e.Estado,
            CupoTotal = e.CupoTotal,
            CantidadCategorias = e.Categorias?.Count ?? 0,
            InscriptosActuales = e.Categorias?
                .SelectMany(c => c.Inscripciones)
                .Count(i => i.EstadoPago == "pagado") ?? 0,
            NombreOrganizador = e.Organizador?.Nombre ?? "Sin información"
          }).ToList(),
          TotalEventos = totalCount,
          PaginaActual = filtro.Pagina,
          TotalPaginas = totalPaginas,
          TamanioPagina = filtro.TamanioPagina
        };

        return Ok(response);
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error en la búsqueda", error = ex.Message });
      }
    }

    /*Obtenemos el detalle de un evento especifico
    GET: api/Evento/{id}*/
    [AllowAnonymous]
    [HttpGet("{id}")]
    public async Task<IActionResult> ObtenerEventosPorId(int id)
    {
      try
      {
        var evento = await _eventoRepositorio.ObtenerPorIdConDetalleAsync(id);

        if (evento == null)
          return NotFound(new { message = "Evento no encontrado" });

        //obtener inscriptos por categoria para mostar en el detalle
        var inscriptosTotal = await _eventoRepositorio.ContarInscriptosAsync(id);
        var inscriptosPorCategoria = await _categoriaRepositorio.ObtenerInscriptosPorCategoriaAsync(id);

        var response = new EventoDetalleResponse
        {
          IdEvento = evento.IdEvento,
          Nombre = evento.Nombre,
          Descripcion = evento.Descripcion,
          FechaHora = evento.FechaHora,
          Lugar = evento.Lugar,
          CupoTotal = evento.CupoTotal,
          InscriptosActuales = inscriptosTotal,
          CuposDisponibles = evento.CupoTotal.HasValue
              ? evento.CupoTotal.Value - inscriptosTotal
              : int.MaxValue,
          Estado = evento.Estado,
          UrlPronosticoClima = evento.UrlPronosticoClima,
          DatosPago = evento.DatosPago,
          Organizador = evento.Organizador != null ? new OrganizadorEventoResponse
          {
            IdUsuario = evento.Organizador.IdUsuario,
            Nombre = evento.Organizador.Nombre,
            NombreComercial = evento.Organizador.PerfilOrganizador?.NombreComercial,
            Telefono = evento.Organizador.Telefono,
            Email = evento.Organizador.Email
          } : null,
          Categorias = evento.Categorias?.Select(c => new CategoriaEventoResponse
          {
            IdCategoria = c.IdCategoria,
            IdEvento = c.IdEvento,
            Nombre = c.Nombre,
            CostoInscripcion = c.CostoInscripcion,
            CupoCategoria = c.CupoCategoria,
            EdadMinima = c.EdadMinima,
            EdadMaxima = c.EdadMaxima,
            Genero = c.Genero,
            InscriptosActuales = inscriptosPorCategoria.ContainsKey(c.IdCategoria)
            ? inscriptosPorCategoria[c.IdCategoria]
            : 0
          }).ToList()
        };

        return Ok(response);
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al obtener el evento", error = ex.Message });
      }
    }



    /*Endpoints para organizadores (requiere autenticacion)*/
    /*GET: api/Evento/MisEventos - Obtenemos los eventos de un organizador autenticado
     paginacion cada 10 elementos*/
    [Authorize]
    [HttpGet("MisEventos")]
    public async Task<IActionResult> ObtenerMisEventos([FromQuery] int pagina = 1, [FromQuery] int tamanioPagina = 10)
    {
      try
      {
        var validacion = ValidarOrganizador();
        if (validacion.error != null) return validacion.error;

        var (eventos, totalCount) = await _eventoRepositorio.ObtenerTodosPorOrganizadorAsync(validacion.userId, pagina, tamanioPagina);

        var totalPaginas = (int)Math.Ceiling(totalCount / (double)tamanioPagina);

        // Mapeo
        var eventosDto = eventos.Select(e => new EventoResumenResponse
        {
          IdEvento = e.IdEvento,
          Nombre = e.Nombre,
          FechaHora = e.FechaHora,
          Lugar = e.Lugar,
          Estado = e.Estado,
          CupoTotal = e.CupoTotal,
          CantidadCategorias = e.Categorias?.Count ?? 0,
          InscriptosActuales = e.Categorias?
                .SelectMany(c => c.Inscripciones)
                .Count(i => i.EstadoPago == "pagado") ?? 0,
          NombreOrganizador = e.Organizador?.Nombre ?? ""
        }).ToList();

        return Ok(new EventosPaginadosResponse
        {
          Eventos = eventosDto,
          PaginaActual = pagina,
          TotalPaginas = totalPaginas,
          TotalEventos = totalCount,
          TamanioPagina = tamanioPagina
        });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al obtener eventos", error = ex.Message });
      }
    }


    /*POST: api/Nuevo - Crea un nuevo evento (Organizadores) y sus categorias*/
    [Authorize]
    [HttpPost]
    public async Task<IActionResult> CrearEvento([FromBody] CrearEventoRequest request)
    {
      try
      {
        if (!ModelState.IsValid)
          return BadRequest(ModelState);

        var validacion = ValidarOrganizador();
        if (validacion.error != null)
          return validacion.error;

        // Verificar perfil completo del organizador
        var usuario = await _usuarioRepositorio.GetByIdAsync(validacion.userId);
        if (usuario == null)
          return NotFound(new { message = "Usuario no encontrado" });

        // Verificar que el organizador tenga perfil completo
        if (usuario.PerfilOrganizador == null ||
            string.IsNullOrEmpty(usuario.PerfilOrganizador.CuitTaxId) ||
            string.IsNullOrEmpty(usuario.PerfilOrganizador.DireccionLegal) ||
            string.IsNullOrEmpty(usuario.Telefono))
        {
          return BadRequest(new { message = "Debe completar su perfil de organizador antes de crear eventos" });
        }

        // Validar fecha futura
        if (request.FechaHora <= DateTime.Now)
          return BadRequest(new { message = "La fecha del evento debe ser futura" });

        // Crear la entidad desde el DTO
        var evento = new Evento
        {
          Nombre = request.Nombre,
          Descripcion = request.Descripcion,
          FechaHora = request.FechaHora,
          Lugar = request.Lugar,
          CupoTotal = request.CupoTotal,
          UrlPronosticoClima = request.UrlPronosticoClima,
          DatosPago = request.DatosPago,
          IdOrganizador = validacion.userId
        };

        var eventoCreado = await _eventoRepositorio.CrearAsync(evento);

        //guardar categorias junto al precio
        if (request.Categorias != null && request.Categorias.Any())
        {
          foreach (var catDto in request.Categorias)
          {
            var nuevaCategoria = new CategoriaEvento
            {
              IdEvento = eventoCreado.IdEvento, // Vinculamos con el ID generado
              Nombre = catDto.Nombre,
              CostoInscripcion = catDto.CostoInscripcion, // $$$ Precio real
              CupoCategoria = catDto.CupoCategoria,
              EdadMinima = catDto.EdadMinima,
              EdadMaxima = catDto.EdadMaxima,
              Genero = catDto.Genero
            };
            // Usamos el repositorio de categorias existente
            await _categoriaRepositorio.CrearAsync(nuevaCategoria);
          }
        }


        return CreatedAtAction(
            nameof(ObtenerEventosPorId),
            new { id = eventoCreado.IdEvento },
            new
            {
              message = "Evento creado exitosamente",
              evento = new EventoResumenResponse
              {
                IdEvento = eventoCreado.IdEvento,
                Nombre = eventoCreado.Nombre,
                FechaHora = eventoCreado.FechaHora,
                Lugar = eventoCreado.Lugar,
                Estado = eventoCreado.Estado
              }
            }
        );
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al crear el evento", error = ex.Message });
      }
    }

    /*PUT: api/Evento/{id} - Actualiza un evento exitosamente (solo el organizador propio)*/
    [Authorize]
    [HttpPut("{id}")]
    public async Task<IActionResult> ActualizarEvento(int id, [FromBody] ActualizarEventoRequest request)
    {
      try
      {
        if (!ModelState.IsValid)
          return BadRequest(ModelState);

        var validacion = ValidarOrganizador();
        if (validacion.error != null)
          return validacion.error;

        var evento = await _eventoRepositorio.ObtenerPorIdAsync(id);
        if (evento == null)
          return NotFound(new { message = "Evento no encontrado" });

        if (evento.IdOrganizador != validacion.userId)
          return Forbid();
        
        /*bloqueo de estados*/
        // si esta cancelado, no se puede editar
        if (evento.Estado == "cancelado")
          return BadRequest(new { message = "No se puede modificar un evento cancelado" });

        //si esta finalizado, no se puede editar
        if (evento.Estado == "finalizado")
          return BadRequest(new { message = "No se puede modificar un evento finalizado" });

        // Actualizar campos desde el DTO
        evento.Nombre = request.Nombre;
        evento.Descripcion = request.Descripcion;
        evento.FechaHora = request.FechaHora;
        evento.Lugar = request.Lugar;
        evento.CupoTotal = request.CupoTotal;
        evento.UrlPronosticoClima = request.UrlPronosticoClima;
        evento.DatosPago = request.DatosPago;

        await _eventoRepositorio.ActualizarAsync(evento);

        return Ok(new
        {
          message = "Evento actualizado exitosamente",
          evento = new EventoResumenResponse
          {
            IdEvento = evento.IdEvento,
            Nombre = evento.Nombre,
            FechaHora = evento.FechaHora,
            Lugar = evento.Lugar,
            Estado = evento.Estado,
            CupoTotal = evento.CupoTotal,
            NombreOrganizador = evento.Organizador?.Nombre ?? ""
          }
        });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al actualizar el evento", error = ex.Message });
      }
    }

    /*PUT: api/Evento/{id}/CambiarEstado - Cambiamos el estado de un evento (publicado, cancelado, finalizado)
    Solo el orga que creo el evento puede cambiar su estado, tambien enviamos una notificacion*/
    [Authorize]
    [HttpPut("{id}/CambiarEstado")]
    public async Task<IActionResult> CambiarEstadoEvento(int id, [FromBody] CambiarEstadoEventoRequest request)
    {
      try
      {
        if (!ModelState.IsValid)
          return BadRequest(ModelState);

        var validacion = ValidarOrganizador();
        if (validacion.error != null)
          return validacion.error;

        var evento = await _eventoRepositorio.ObtenerPorIdAsync(id);
        if (evento == null)
          return NotFound(new { message = "Evento no encontrado" });

        if (evento.IdOrganizador != validacion.userId)
          return Forbid();

        var estadoAnterior = evento.Estado;

        //cambiamos el estado en tabla eventos
        await _eventoRepositorio.CambiarEstadoAsync(id, request.NuevoEstado);

        //creamos la notificacion
        // si el estado es cancelado, finalizado o cualquier cambio
        if (!string.IsNullOrEmpty(request.Motivo)) 
        {
            // Preparamos el título automatico
            string tituloNotif = $"Evento {request.NuevoEstado.ToUpper()}";

          if (request.NuevoEstado.ToLower() == "cancelado")
            tituloNotif = "URGENTE: Evento Cancelado";
          else if (request.NuevoEstado.ToLower() == "retrasado")
            tituloNotif = "ATENCIÓN: Evento Retrasado"; // <--- Nuevo caso
          else if (request.NuevoEstado.ToLower() == "suspendido")
            tituloNotif = "AVISO: Evento Suspendido";

          var nuevaNotificacion = new CrearNotificacionRequest
            {
                IdEvento = id,
                Titulo = tituloNotif,
                Mensaje = request.Motivo // aqui va el texto que se ecribio en el Dialog de Android
            };

            // Usamos el repositorio de notificaciones para guardar en 'notificaciones_evento'
            await _notificacionRepositorio.CrearAsync(nuevaNotificacion, validacion.userId);
        }

        return Ok(new
        {
          message = "Estado del evento cambiado exitosamente",
          idEvento = id,
          nombreEvento = evento.Nombre,
          estadoAnterior,
          estadoNuevo = request.NuevoEstado.ToLower(),
          motivo = request.Motivo,
          fechaCambio = DateTime.Now
        });
      }
      catch (KeyNotFoundException ex)
      {
        return NotFound(new { message = ex.Message });
      }
      catch (InvalidOperationException ex)
      {
        return BadRequest(new { message = ex.Message });
      }
      catch (ArgumentException ex)
      {
        return BadRequest(new { message = ex.Message });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al cambiar el estado", error = ex.Message });
      }
    }

    /*Validar que el usuario autenticado sea organizado, retorna el userId si es valido o un ACtionResult con error*/
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