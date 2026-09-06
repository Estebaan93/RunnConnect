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
using System.Collections.Generic;
using RunnConnectAPI.Services;

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
      _notificacionRepositorio = notificacionRepositorio;
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
          TipoEvento= e.TipoEvento,
          DatosPago= e.DatosPago,
          UrlPronosticoClima = e.UrlPronosticoClima,
          CantidadCategorias = e.Categorias?.Count ?? 0,
          InscriptosActuales = e.Categorias?
                .SelectMany(c => c.Inscripciones)
                .Count(i => i.EstadoPago == "pagado") ?? 0,
          NombreOrganizador = e.Organizador?.Nombre ?? "Sin informacion",
          Categorias = e.Categorias?.Select(c => new CategoriaEventoResponse
          {
            IdCategoria = c.IdCategoria,
            IdEvento= c.IdEvento,
            Nombre = c.Nombre, //Esto es lo que necesitamos para el Chip!
            CostoInscripcion = c.CostoInscripcion,
            CupoCategoria = c.CupoCategoria,
            EdadMinima = c.EdadMinima,
            EdadMaxima = c.EdadMaxima,
            Genero = c.Genero,
            Estado = c.Estado,
            //calculamos los inscrptos
            InscriptosActuales = c.Inscripciones.Count(i=>i.EstadoPago=="pagado")
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
            TipoEvento = e.TipoEvento,
            DatosPago = e.DatosPago,
            UrlPronosticoClima = e.UrlPronosticoClima,
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
          TipoEvento= evento.TipoEvento,
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
            Estado= c.Estado,
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
    [Authorize(Roles= "organizador")]
    public async Task<IActionResult> ObtenerMisEventos([FromQuery] int pagina = 1, [FromQuery] int tamanioPagina = 10)
    {
      try
      {
        int userId = User.ObtenerUserId();


        var (eventos, totalCount) = await _eventoRepositorio.ObtenerTodosPorOrganizadorAsync(userId, pagina, tamanioPagina);

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
          TipoEvento = e.TipoEvento,
          DatosPago = e.DatosPago,
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


    /*POST: api/Evento - Crea un nuevo evento (Organizadores) y sus categorias*/
    [HttpPost]
    [Authorize(Roles="organizador")]
    public async Task<IActionResult> CrearEvento([FromBody] CrearEventoRequest request)
    {
      try
      {
        if (!ModelState.IsValid)
          return BadRequest(ModelState);

        int userId = User.ObtenerUserId();

        // Verificar perfil completo del organizador
        var usuario = await _usuarioRepositorio.GetByIdAsync(userId);
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
          TipoEvento = request.TipoEvento,
          IdOrganizador = userId
        };

        var eventoCreado = await _eventoRepositorio.CrearAsync(evento);

        var categoriasResponse = new List<CategoriaEventoResponse>();

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
              Genero = catDto.Genero,
              Estado="programada"
            };
            // Usamos el repositorio de categorias existente
            await _categoriaRepositorio.CrearAsync(nuevaCategoria);


        categoriasResponse.Add(new CategoriaEventoResponse
            {
                IdCategoria = nuevaCategoria.IdCategoria, // id
                IdEvento = nuevaCategoria.IdEvento,
                Nombre = nuevaCategoria.Nombre,
                CostoInscripcion = nuevaCategoria.CostoInscripcion,
                CupoCategoria = nuevaCategoria.CupoCategoria,
                EdadMinima = nuevaCategoria.EdadMinima,
                EdadMaxima = nuevaCategoria.EdadMaxima,
                Genero = nuevaCategoria.Genero,
                Estado = nuevaCategoria.Estado,
                InscriptosActuales = 0
                // nota: no asignamos GeneroDescripcion ni TieneCupo porque son de solo lectura (se calculan solas)
            });
          }
        }

        //  NUEVO: DISPARAR NOTIFICACION GLOBAL
       // Esto avisa a TODOS los usuarios de la app que hay un evento nuevo
       var notifRequest = new CrearNotificacionRequest
       {
           IdEvento = eventoCreado.IdEvento,
           IdCategoria = null,
           Titulo = "¡Nuevo Evento Disponible!",
           Mensaje = $"{eventoCreado.Nombre} en {eventoCreado.Lugar}. ¡Inscripciones abiertas!"
       };

       // Usamos el metodo especifico que creamos arriba (no requiere userId porque es automatica)
       await _notificacionRepositorio.CrearNotificacionGlobalAsync(notifRequest);


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
                Estado = eventoCreado.Estado,
                CupoTotal = eventoCreado.CupoTotal,

                TipoEvento = eventoCreado.TipoEvento,
                DatosPago = eventoCreado.DatosPago,
                Categorias = categoriasResponse,

                NombreOrganizador = usuario.Nombre,
                CantidadCategorias = categoriasResponse.Count
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
    [HttpPut("{id}")]
    [Authorize(Roles = "organizador")]
    public async Task<IActionResult> ActualizarEvento(int id, [FromBody] ActualizarEventoRequest request)
    {
      try
      {
        if (!ModelState.IsValid)
          return BadRequest(ModelState);

        int userId = User.ObtenerUserId();

        var evento = await _eventoRepositorio.ObtenerPorIdAsync(id);
        if (evento == null)
          return NotFound(new { message = "Evento no encontrado" });

        if (evento.IdOrganizador != userId)
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

    /*PUT: api/Evento/{id}/CambiarEstado - Cambiamos el estado de un evento (publicado, cancelado, finalizado o retrasado)
    Solo el orga que creo el evento puede cambiar su estado, tambien enviamos una notificacion*/
    [HttpPut("{id}/CambiarEstado")]
    [Authorize(Roles = "organizador")]
    public async Task<IActionResult> CambiarEstadoEvento(int id, [FromBody] CambiarEstadoEventoRequest request)
    {
      try
      {
        if (!ModelState.IsValid)
          return BadRequest(ModelState);

        int userId = User.ObtenerUserId();

        var evento = await _eventoRepositorio.ObtenerPorIdConDetalleAsync(id);
        if (evento == null)
          return NotFound(new { message = "Evento no encontrado" });

        if (evento.IdOrganizador != userId)
          return Forbid();

        string nuevoEstadoEvento = request.NuevoEstado.ToLower().Trim();

        //aplicar cambio al Evento Padre
        await _eventoRepositorio.CambiarEstadoAsync(id, nuevoEstadoEvento);

        if (evento.Categorias != null)
        {
          // CASO A:
          if (nuevoEstadoEvento == "suspendido" || nuevoEstadoEvento == "cancelado" || nuevoEstadoEvento == "retrasado")
          {
            //Evento -> Categoria
            string estadoParaCategoria = nuevoEstadoEvento; // Valor por defecto

            // Mapeamos lo que la base de datos acepta en Categorias
            if (nuevoEstadoEvento == "cancelado") estadoParaCategoria = "cancelada";
            if (nuevoEstadoEvento == "retrasado") estadoParaCategoria = "retrasada";
            if (nuevoEstadoEvento == "suspendido") estadoParaCategoria = "suspendido";


            foreach (var cat in evento.Categorias)
            {
              string estadoActual = cat.Estado?.ToLower().Trim() ?? "";

              // Solo cambiamos si no estaba ya en un estado final definitivo
              if (estadoActual != "finalizada" && estadoActual != "cancelada")
              {
                cat.Estado = estadoParaCategoria;
                await _categoriaRepositorio.ActualizarAsync(cat);
              }
            }
          }
          // CASO B: VOLVEMOS A LA NORMALIDAD (Restaurar)
          else if (nuevoEstadoEvento == "publicado")
          {
            foreach (var cat in evento.Categorias)
            {
              // Obtenemos el estado actual seguro (evitando nulos)
              string estadoCat = cat.Estado?.ToLower().Trim() ?? "";

              // 1. Si esta "suspendido" o "retrasada" -> volver a programada.
              // 2. Si esta "" (BLANCO/VACIO por error anterior) -> volver a programada (reparacion).
              if (estadoCat == "suspendido" ||
                  estadoCat == "retrasada" ||
                  estadoCat == "retrasado" || // Por si acaso
                  string.IsNullOrEmpty(estadoCat)) // ESTO ARREGLA LOS BLANCOS
              {
                cat.Estado = "programada"; // Vuelve a la normalidad
                await _categoriaRepositorio.ActualizarAsync(cat);
              }
            }
          }
        }

        //notificacion
        if (!string.IsNullOrEmpty(request.Motivo))
        {
          string titulo = $"EVENTO {request.NuevoEstado.ToUpper()}";

          if (nuevoEstadoEvento == "cancelado") titulo = "URGENTE: Evento Cancelado";
          if (nuevoEstadoEvento == "suspendido") titulo = "Evento Suspendido";
          if (nuevoEstadoEvento == "retrasado") titulo = "Evento Retrasado";
          if (nuevoEstadoEvento == "publicado") titulo = "Evento Confirmado / Reanudado";

          var notif = new CrearNotificacionRequest
          {
            IdEvento = id,
            IdCategoria = null,
            Titulo = titulo,
            Mensaje = request.Motivo
          };

          await _notificacionRepositorio.CrearAsync(notif, userId);
        }

        return Ok(new { message = $"Evento actualizado a {nuevoEstadoEvento}. Categorías sincronizadas." });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error interno", error = ex.Message });
      }
    }

  }
}
