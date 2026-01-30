// Controllers/InscripcionController.cs
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using RunnConnectAPI.Models;
using RunnConnectAPI.Models.Dto.Inscripcion;
using RunnConnectAPI.Repositories;
using RunnConnectAPI.Services;
using System.Security.Claims;

namespace RunnConnectAPI.Controllers
{
  [ApiController]
  [Route("api/[controller]")]
  [Authorize]
  public class InscripcionController : ControllerBase
  {
    private readonly InscripcionRepositorio _inscripcionRepositorio;
    private readonly CategoriaRepositorio _categoriaRepositorio;
    private readonly EventoRepositorio _eventoRepositorio;
    private readonly FileService _fileService;

    public InscripcionController(
        InscripcionRepositorio inscripcionRepositorio,
        CategoriaRepositorio categoriaRepositorio,
        EventoRepositorio eventoRepositorio,
        FileService fileService)
    {
      _inscripcionRepositorio = inscripcionRepositorio;
      _categoriaRepositorio = categoriaRepositorio;
      _eventoRepositorio = eventoRepositorio;
      _fileService = fileService;
    }

    // Endpoints para Runners

    /// Inscribirse a una categoria de evento
    /// POST: api/Inscripcion
    [HttpPost]
    public async Task<IActionResult> Inscribirse([FromBody] CrearInscripcionRequest request)
    {
      try
      {
        if (!ModelState.IsValid)
          return BadRequest(ModelState);

        var validacion = ValidarRunner();
        if (validacion.error != null)
          return validacion.error;

        // Validar que acepto el deslinde
        if (!request.AceptoDeslinde)
          return BadRequest(new { message = "Debe aceptar el deslinde de responsabilidad para inscribirse" });

        // Verificar que la categoria existe
        var categoria = await _categoriaRepositorio.ObtenerPorIdAsync(request.IdCategoria);
        if (categoria == null)
          return NotFound(new { message = "Categoría no encontrada" });

        // Verificar que el evento existe y esta publicado
        var evento = categoria.Evento;
        if (evento == null)
          return NotFound(new { message = "Evento no encontrado" });

        if (evento.Estado != "publicado")
          return BadRequest(new { message = "El evento no está disponible para inscripciones" });

        if (evento.FechaHora <= DateTime.Now)
          return BadRequest(new { message = "No se puede inscribir a un evento que ya paso" });

        // Verificar perfil completo del runner
        var (perfilCompleto, camposFaltantes) = await _inscripcionRepositorio.ValidarPerfilCompletoRunner(validacion.userId);
        if (!perfilCompleto)
          return BadRequest(new { message = "Debe completar su perfil antes de inscribirse", camposFaltantes });

        // Verificar requisitos de la categoría (edad y genero)
        var (cumpleRequisitos, motivo) = await _inscripcionRepositorio.ValidarRequisitosCategoria(validacion.userId, request.IdCategoria);
        if (!cumpleRequisitos)
          return BadRequest(new { message = motivo });

        // Verificar que no este ya inscripto en el evento
        if (await _inscripcionRepositorio.ExisteInscripcionEnEventoAsync(validacion.userId, evento.IdEvento))
          return BadRequest(new { message = "Ya tiene una inscripcion activa en este evento" });

        // Verificar cupo disponible en la categoria
        if (!await _categoriaRepositorio.TieneCupoDisponibleAsync(request.IdCategoria))
          return BadRequest(new { message = "No hay cupos disponibles en esta categoría" });

        // Verificar cupo disponible en el evento
        if (!await _eventoRepositorio.TieneCupoDisponibleAsync(evento.IdEvento))
          return BadRequest(new { message = "No hay cupos disponibles en el evento" });

        // Crear la inscripcion
        var inscripcion = new Inscripcion
        {
          IdUsuario = validacion.userId,
          IdCategoria = request.IdCategoria,
          TalleRemera = request.TalleRemera,
          AceptoDeslinde = true
        };

        var inscripcionCreada = await _inscripcionRepositorio.CrearAsync(inscripcion);

        return CreatedAtAction(
            nameof(ObtenerInscripcion),
            new { id = inscripcionCreada.IdInscripcion },
            new
            {
              message = "Inscripcion realizada exitosamente",
              inscripcion = new InscripcionResponse
              {
                IdInscripcion = inscripcionCreada.IdInscripcion,
                IdUsuario = inscripcionCreada.IdUsuario,
                IdCategoria = inscripcionCreada.IdCategoria,
                FechaInscripcion = inscripcionCreada.FechaInscripcion,
                EstadoPago = inscripcionCreada.EstadoPago,
                TalleRemera = inscripcionCreada.TalleRemera,
                AceptoDeslinde = inscripcionCreada.AceptoDeslinde
              },
              datosPago = evento.DatosPago,
              costoInscripcion = categoria.CostoInscripcion
            }
        );
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al crear la inscripcion", error = ex.Message });
      }
    }


    /// Obtiene las inscripciones del runner autenticado
    /// GET: api/Inscripcion/MisInscripciones
    [HttpGet("MisInscripciones")]
    public async Task<IActionResult> ObtenerMisInscripciones([FromQuery] bool soloActivas = false)
    {
      try
      {
        var validacion = ValidarRunner();
        if (validacion.error != null)
          return validacion.error;

        var inscripciones = soloActivas
            ? await _inscripcionRepositorio.ObtenerActivasPorRunnerAsync(validacion.userId)
            : await _inscripcionRepositorio.ObtenerPorRunnerAsync(validacion.userId);

        var response = inscripciones.Select(i => new InscripcionDetalleResponse
        {
          IdInscripcion = i.IdInscripcion,
          FechaInscripcion = i.FechaInscripcion,
          EstadoPago = i.EstadoPago,
          TalleRemera = i.TalleRemera,
          AceptoDeslinde = i.AceptoDeslinde,
          ComprobantePagoURL = i.ComprobantePagoURL,
          Evento = i.Categoria?.Evento != null ? new EventoInscripcionInfo
          {
            IdEvento = i.Categoria.Evento.IdEvento,
            Nombre = i.Categoria.Evento.Nombre,
            FechaHora = i.Categoria.Evento.FechaHora,
            Lugar = i.Categoria.Evento.Lugar,
            Estado = i.Categoria.Evento.Estado,
            DatosPago = i.Categoria.Evento.DatosPago
          } : null,
          Categoria = i.Categoria != null ? new CategoriaInscripcionInfo
          {
            IdCategoria = i.Categoria.IdCategoria,
            Nombre = i.Categoria.Nombre,
            CostoInscripcion = i.Categoria.CostoInscripcion,
            Genero = i.Categoria.Genero,
            EdadMinima = i.Categoria.EdadMinima,
            EdadMaxima = i.Categoria.EdadMaxima
          } : null
        }).ToList();

        return Ok(new
        {
          total = response.Count,
          inscripciones = response
        });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al obtener inscripciones", error = ex.Message });
      }
    }

    /// Obtiene el detalle de una inscripcion
    /// GET: api/Inscripcion/{id}
    [HttpGet("{id}")]
    public async Task<IActionResult> ObtenerInscripcion(int id)
    {
      try
      {
        var userId = ObtenerUserIdDelToken();
        if (userId == null)
          return Unauthorized(new { message = "No autorizado" });

        var inscripcion = await _inscripcionRepositorio.ObtenerPorIdAsync(id);

        if (inscripcion == null)
          return NotFound(new { message = "Inscripcion no encontrada" });

        // Verificar que la inscripción pertenece al usuario o es organizador del evento
        var esRunner = inscripcion.IdUsuario == userId;
        var esOrganizador = inscripcion.Categoria?.Evento?.IdOrganizador == userId;

        if (!esRunner && !esOrganizador)
          return Forbid();

        var response = new InscripcionDetalleResponse
        {
          IdInscripcion = inscripcion.IdInscripcion,
          FechaInscripcion = inscripcion.FechaInscripcion,
          EstadoPago = inscripcion.EstadoPago,
          TalleRemera = inscripcion.TalleRemera,
          AceptoDeslinde = inscripcion.AceptoDeslinde,
          ComprobantePagoURL = inscripcion.ComprobantePagoURL,
          Evento = inscripcion.Categoria?.Evento != null ? new EventoInscripcionInfo
          {
            IdEvento = inscripcion.Categoria.Evento.IdEvento,
            Nombre = inscripcion.Categoria.Evento.Nombre,
            FechaHora = inscripcion.Categoria.Evento.FechaHora,
            Lugar = inscripcion.Categoria.Evento.Lugar,
            Estado = inscripcion.Categoria.Evento.Estado,
            DatosPago = inscripcion.Categoria.Evento.DatosPago
          } : null,
          Categoria = inscripcion.Categoria != null ? new CategoriaInscripcionInfo
          {
            IdCategoria = inscripcion.Categoria.IdCategoria,
            Nombre = inscripcion.Categoria.Nombre,
            CostoInscripcion = inscripcion.Categoria.CostoInscripcion,
            Genero = inscripcion.Categoria.Genero,
            EdadMinima = inscripcion.Categoria.EdadMinima,
            EdadMaxima = inscripcion.Categoria.EdadMaxima
          } : null
        };

        return Ok(response);
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al obtener la inscripcion", error = ex.Message });
      }
    }

    /// Subir comprobante de pago
    /// PUT: api/Inscripcion/{id}/Comprobante
    [HttpPut("{id}/Comprobante")]
    [Consumes("multipart/form-data")]
    public async Task<IActionResult> SubirComprobante(int id, [FromForm] SubirComprobanteRequest request)
    {
      try
      {
        var validacion = ValidarRunner();
        if (validacion.error != null)
          return validacion.error;

        var inscripcion = await _inscripcionRepositorio.ObtenerPorIdAsync(id);

        if (inscripcion == null)
          return NotFound(new { message = "Inscripcion no encontrada" });

        if (inscripcion.IdUsuario != validacion.userId)
          return Forbid();

        if (inscripcion.EstadoPago != "pendiente")
          return BadRequest(new { message = "Solo se puede subir comprobante para inscripciones pendientes" });

        // Validar archivo
        if (request.Comprobante == null || request.Comprobante.Length == 0)
          return BadRequest(new { message = "Debe seleccionar un archivo" });

        var extensionesPermitidas = new[] { ".jpg", ".jpeg", ".png", ".pdf" };
        var extension = Path.GetExtension(request.Comprobante.FileName).ToLower();
        if (!extensionesPermitidas.Contains(extension))
          return BadRequest(new { message = "Solo se permiten archivos JPG, PNG o PDF" });

        if (request.Comprobante.Length > 10 * 1024 * 1024) // 10MB maximo
          return BadRequest(new { message = "El archivo no puede exceder 10MB" });

        // Guardar archivo
        var urlComprobante = await _fileService.GuardarComprobanteAsync(request.Comprobante, id);

        await _inscripcionRepositorio.ActualizarComprobanteYEstadoAsync(id, urlComprobante, "procesando");

        return Ok(new
        {
          message = "Comprobante subido exitosamente. El pago se esta procesando",
          comprobantePagoURL = urlComprobante,
          estadoPago = "procesando"
        });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al subir el comprobante", error = ex.Message });
      }
    }

    /// Cancelar una inscripcion
    /// PUT: api/Inscripcion/{id}/Cancelar
    [HttpPut("{id}/Cancelar")]
    public async Task<IActionResult> CancelarInscripcion(int id)
    {
      try
      {
        var validacion = ValidarRunner();
        if (validacion.error != null)
          return validacion.error;

        var inscripcion = await _inscripcionRepositorio.ObtenerPorIdAsync(id);

        if (inscripcion == null)
          return NotFound(new { message = "Inscripcion no encontrada" });

        if (inscripcion.IdUsuario != validacion.userId)
          return Forbid();

        if (inscripcion.EstadoPago == "procesando")
        {
          return BadRequest(new { message = "Ya has enviado el comprobante. Por favor espera a que el organizador confirme o rechace tu pago para realizar cambios." });
        }

        if (inscripcion.EstadoPago == "pagado")
        {
          return BadRequest(new { message = "Tu inscripción ya está pagada. Si no puedes asistir, contacta al organizador para solicitar un reembolso." });
        }

        //Solo pasa si esta pendiente
        await _inscripcionRepositorio.CambiarEstadoPagoAsync(id, "cancelado");

        return Ok(new
        {
          message = "Inscripción cancelada exitosamente",
          idInscripcion = id,
          estadoFinal = "cancelado"
        });
      }
      catch (InvalidOperationException ex)
      {
        return BadRequest(new { message = ex.Message });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al cancelar la inscripcion", error = ex.Message });
      }
    }


    // Endpoints para Organizadores

    /// Obtiene los inscriptos de un evento
    /// GET: api/Evento/{idEvento}/Inscripciones
    [HttpGet("/api/Evento/{idEvento}/Inscripciones")]
    public async Task<IActionResult> ObtenerInscriptosEvento(int idEvento, [FromQuery] FiltroInscripcionesRequest filtro)
    {
      try
      {
        var validacion = ValidarOrganizador();
        if (validacion.error != null)
          return validacion.error;

        // Verificar que el evento existe y pertenece al organizador
        var evento = await _eventoRepositorio.ObtenerPorIdAsync(idEvento);
        if (evento == null)
          return NotFound(new { message = "Evento no encontrado" });

        if (evento.IdOrganizador != validacion.userId)
          return Forbid();

        var (inscripciones, totalCount) = await _inscripcionRepositorio.ObtenerPorEventoConFiltrosAsync(
            idEvento,
            filtro.IdCategoria,
            filtro.EstadoPago,
            filtro.BuscarRunner,
            filtro.Pagina,
            filtro.TamanioPagina
        );

        var totalPaginas = (int)Math.Ceiling(totalCount / (double)filtro.TamanioPagina);

        var response = inscripciones.Select(i => new InscriptoEventoResponse
        {
          IdInscripcion = i.IdInscripcion,
          FechaInscripcion = i.FechaInscripcion,
          EstadoPago = i.EstadoPago,
          TalleRemera = i.TalleRemera,
          ComprobantePagoURL = !string.IsNullOrEmpty(i.ComprobantePagoURL)
            ? _fileService.ObtenerUrlCompleta(i.ComprobantePagoURL, Request) : null,
          IdCategoria = i.IdCategoria,
          NombreCategoria = i.Categoria?.Nombre ?? "",
          Runner = i.Usuario != null ? new RunnerInscriptoInfo
          {
            IdUsuario = i.Usuario.IdUsuario,
            Nombre = i.Usuario.Nombre,
            Apellido = i.Usuario.PerfilRunner?.Apellido,
            Email = i.Usuario.Email,
            Telefono = i.Usuario.Telefono,
            Dni = i.Usuario.PerfilRunner?.Dni,
            Genero = i.Usuario.PerfilRunner?.Genero,
            FechaNacimiento = i.Usuario.PerfilRunner?.FechaNacimiento,
            Localidad = i.Usuario.PerfilRunner?.Localidad,
            NombreContactoEmergencia = i.Usuario.PerfilRunner?.NombreContactoEmergencia,
            TelefonoEmergencia = i.Usuario.PerfilRunner?.TelefonoEmergencia
          } : null
        }).ToList();

        // Obtener estadisticas
        var estadisticas = await _inscripcionRepositorio.ContarPorEstadoEnEventoAsync(idEvento);

        return Ok(new
        {
          idEvento,
          nombreEvento = evento.Nombre,
          estadisticas,
          totalInscripciones = totalCount,
          paginaActual = filtro.Pagina,
          totalPaginas,
          tamanioPagina = filtro.TamanioPagina,
          inscripciones = response
        });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al obtener inscripciones", error = ex.Message });
      }
    }

    /// Confirmar o rechazar pago de una inscripcion
    /// PUT: api/Inscripcion/{id}/EstadoPago
    [HttpPut("{id}/EstadoPago")]
    public async Task<IActionResult> CambiarEstadoPago(int id, [FromBody] CambiarEstadoPagoRequest request)
    {
      try
      {
        if (!ModelState.IsValid) return BadRequest(ModelState);

        var validacion = ValidarOrganizador();
        if (validacion.error != null) return validacion.error;

        var inscripcion = await _inscripcionRepositorio.ObtenerPorIdAsync(id);
        if (inscripcion == null) return NotFound(new { message = "Inscripción no encontrada" });

        if (inscripcion.Categoria?.Evento?.IdOrganizador != validacion.userId) return Forbid();

        // Validar estado origen
        if (inscripcion.EstadoPago != "procesando")
          return BadRequest(new { message = "Solo se puede confirmar/rechazar pagos en estado PROCESANDO." });

        var estadoAnterior = inscripcion.EstadoPago;
        string estadoFinal = request.NuevoEstado.ToLower();

        // El repositorio validara si 'pagado' o 'rechazado' son transiciones validas
        await _inscripcionRepositorio.CambiarEstadoPagoAsync(id, estadoFinal);

        return Ok(new
        {
          message = $"Estado de pago actualizado a '{estadoFinal}'",
          idInscripcion = id,
          estadoAnterior,
          estadoNuevo = estadoFinal,
          motivo = request.Motivo
        });
      }
      catch (InvalidOperationException ex)
      {
        return BadRequest(new { message = ex.Message });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al cambiar el estado", error = ex.Message });
      }
    }

    /// Reembolsar una inscripcion (solo confirmadas)
    /// PUT: api/Inscripcion/{id}/Reembolsar
    [HttpPut("{id}/Reembolsar")]
    public async Task<IActionResult> ReembolsarInscripcion(int id)
    {
      try
      {
        var validacion = ValidarOrganizador();
        if (validacion.error != null) return validacion.error;

        var inscripcion = await _inscripcionRepositorio.ObtenerPorIdAsync(id);
        if (inscripcion == null) return NotFound(new { message = "Inscripcion no encontrada" });

        if (inscripcion.Categoria?.Evento?.IdOrganizador != validacion.userId) return Forbid();

        // La validacion estricta (solo si es 'pagado') esta en el repositorio
        await _inscripcionRepositorio.CambiarEstadoPagoAsync(id, "reembolsado");

        return Ok(new
        {
          message = "Inscripcion marcada como reembolsada",
          idInscripcion = id,
          estadoNuevo = "reembolsado"
        });
      }
      catch (InvalidOperationException ex)
      {
        return BadRequest(new { message = ex.Message });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al reembolsar", error = ex.Message });
      }
    }

    //dar de baja un runner (solo organizador)
    //PUT: api/Inscripcion/{id}/BajaRunner
    [HttpPut("{id}/BajaRunner")]
    public async Task<IActionResult> DarDeBajaInscripcion(int id, [FromBody] MotivoRequest request)
    {
      try
      {
        var validacion = ValidarOrganizador();
        if (validacion.error != null) return validacion.error;

        var inscripcion = await _inscripcionRepositorio.ObtenerPorIdAsync(id);
        if (inscripcion == null) return NotFound(new { message = "Inscripcion no encontrada" });

        // Verificar que la inscripción pertenece a un evento de este organizador
        if (inscripcion.Categoria?.Evento?.IdOrganizador != validacion.userId)
          return Forbid();


        // Forzar cambio de estado a cancelado
        // Nota: guardar el motivo en la BD si tienes un campo para eso
        await _inscripcionRepositorio.DarBajaPorOrganizadorAsync(id);

        //Enviar notificacion al runner avisando que fue dado de baja (TODO)
        return Ok(new
        {
          message = "Inscripción dada de baja exitosamente",
          idInscripcion = id,
          estadoNuevo = "cancelado",
          motivo = request.Motivo
        });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al dar de baja", error = ex.Message });
      }
    }
    // DTO simple para recibir el motivo
    public class MotivoRequest
    {
      public string Motivo { get; set; } = string.Empty;
    }



    // Metodos Privados

    private (int userId, IActionResult? error) ValidarRunner()
    {
      var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier);
      if (userIdClaim == null)
        return (0, Unauthorized(new { message = "No autorizado" }));

      var userId = int.Parse(userIdClaim.Value);

      var tipoUsuarioClaim = User.FindFirst("TipoUsuario");
      if (tipoUsuarioClaim == null || tipoUsuarioClaim.Value.ToLower() != "runner")
        return (0, BadRequest(new { message = "Solo los runners pueden realizar esta accion" }));

      return (userId, null);
    }

    private (int userId, IActionResult? error) ValidarOrganizador()
    {
      var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier);
      if (userIdClaim == null)
        return (0, Unauthorized(new { message = "No autorizado" }));

      var userId = int.Parse(userIdClaim.Value);

      var tipoUsuarioClaim = User.FindFirst("TipoUsuario");
      if (tipoUsuarioClaim == null || tipoUsuarioClaim.Value.ToLower() != "organizador")
        return (0, BadRequest(new { message = "Solo los organizadores pueden realizar esta accion" }));

      return (userId, null);
    }

    private int? ObtenerUserIdDelToken()
    {
      var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier);
      if (userIdClaim == null)
        return null;

      return int.Parse(userIdClaim.Value);
    }

    [HttpGet("BuscarInscriptos")]
    public async Task<IActionResult> BuscarInscriptos([FromQuery] string busqueda)
    {
        try
        {
            var (userId, error) = ValidarOrganizador();
            if (error != null) return error;

            if (string.IsNullOrWhiteSpace(busqueda))
                return BadRequest(new { message = "Debe ingresar un término de búsqueda." });

            // Llamada al repo
            var resultados = await _inscripcionRepositorio.BuscarGlobalPorOrganizadorAsync(userId, busqueda);

            // Mapeo a tu nuevo DTO: BusquedaInscripcionResponse
            var response = resultados.Select(i => new BusquedaInscripcionResponse
            {
                IdInscripcion = i.IdInscripcion,
                FechaInscripcion = i.FechaInscripcion,
                EstadoPago = i.EstadoPago,
                
                IdEvento = i.Categoria.IdEvento,
                NombreEvento = i.Categoria.Evento.Nombre,
                NombreCategoria = i.Categoria.Nombre,
                
                Runner = new RunnerSimpleDto
                {
                    Nombre = i.Usuario.Nombre,
                    // Manejo seguro de nulos con el operador '?' y '??'
                    Apellido = i.Usuario.PerfilRunner?.Apellido ?? "",
                    Dni = i.Usuario.PerfilRunner?.Dni.ToString() ?? "S/D",
                    Email = i.Usuario.Email
                }
            }).ToList();

            return Ok(response);
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "Error en búsqueda global", error = ex.Message });
        }
    }



  }
}