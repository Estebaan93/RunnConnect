//Controllers/ResultadoController
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using RunnConnectAPI.Models.Dto.Resultado;
using RunnConnectAPI.Repositories;
using System.Security.Claims;
using System.IO;

namespace RunnConnectAPI.Controllers
{
  [ApiController]
  [Route("api/[controller]")]
  public class ResultadoController : ControllerBase
  {
    private readonly ResultadoRepositorio _resultadoRepo;

    public ResultadoController(ResultadoRepositorio resultadoRepo)
    {
      _resultadoRepo = resultadoRepo;
    }

    // ═══════════════════ LECTURA (PUBLICO & RUNNER) ═══════════════════
    [HttpGet("{id}")]
    public async Task<IActionResult> ObtenerPorId(int id)
    {
      try
      {
        var resultado = await _resultadoRepo.ObtenerPorIdAsync(id);

        if (resultado == null)
          return NotFound(new { message = "Resultado no encontrado" });

        return Ok(resultado);
      }
      catch (Exception ex)
      {
        return StatusCode(500, new
        {
          message = "Error al obtener",
          error = ex.Message
        });
      }
    }

    [HttpGet("Evento/{idEvento}")]
    public async Task<IActionResult> ObtenerResultadosEvento(int idEvento)
    {
      try
      {
        var resultados = await _resultadoRepo.ObtenerResultadosEventoAsync(idEvento);

        if (resultados == null)
          return NotFound(new { message = "Evento no encontrado" });

        return Ok(resultados);
      }
      catch (Exception ex)
      {
        return StatusCode(500, new
        {
          message = "Error al obtener resultados",
          error = ex.Message
        });
      }
    }

    [HttpGet("Categoria/{idCategoria}")]
    public async Task<IActionResult> ObtenerResultadosCategoria(int idCategoria)
    {
      try
      {
        var resultados = await _resultadoRepo.ObtenerResultadosPorCategoriaAsync(idCategoria);
        return Ok(resultados);
      }
      catch (Exception ex)
      {
        return StatusCode(500, new
        {
          message = "Error",
          error = ex.Message
        });
      }
    }

    [HttpGet("MisResultados")]
    [Authorize]
    public async Task<IActionResult> MisResultados()
    {
      try
      {
        var (userId, error) = ValidarRunner();
        if (error != null)
          return error;

        var resultados = await _resultadoRepo.ObtenerMisResultadosAsync(userId);
        return Ok(resultados);
      }
      catch (Exception ex)
      {
        return StatusCode(500, new
        {
          message = "Error",
          error = ex.Message
        });
      }
    }

    [HttpPut("{id}/DatosSmartwatch")]
    [Authorize]
    public async Task<IActionResult> AgregarDatosSmartwatch(int id, [FromBody] DatosSmartwatchRequest request)
    {
      try
      {
        var (userId, error) = ValidarRunner();
        if (error != null)
          return error;

        var (exito, errorMsg) = await _resultadoRepo.AgregarDatosSmartwatchAsync(id, request, userId);

        if (!exito)
          return BadRequest(new { message = errorMsg });

        return Ok(new { message = "Datos agregados correctamente" });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new
        {
          message = "Error",
          error = ex.Message
        });
      }
    }

    // ═══════════════════ GESTION (ORGANIZADOR) ═══════════════════
    /// CARGA MANUAL (1 a 1) -> Recibe JSON
    [HttpPost("Cargar")]
    [Authorize]
    public async Task<IActionResult> CargarResultado([FromBody] CargarResultadoRequest request)
    {
      try
      {
        var (userId, error) = ValidarOrganizador();
        if (error != null)
          return error;

        var (resultado, errorMsg) = await _resultadoRepo.CargarResultadoAsync(request, userId);

        if (resultado == null)
          return BadRequest(new { message = errorMsg });

        return CreatedAtAction(
            nameof(ObtenerPorId),
            new { id = resultado.IdResultado },
            new
            {
              message = "Resultado cargado correctamente",
              idResultado = resultado.IdResultado
            });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new
        {
          message = "Error al cargar",
          error = ex.Message
        });
      }
    }

    /// CARGA MASIVA (Batch) -> Recibe Archivo CSV
    /// Formato CSV esperado: DNI,TiempoOficial,PosGeneral,PosCategoria
    [HttpPost("CargarArchivo")]
    [Authorize]
    [Consumes("multipart/form-data")]
    public async Task<IActionResult> CargarArchivoResultados([FromForm] SubirResultadosArchivoRequest request)
    {
      try
      {
        var (userId, error) = ValidarOrganizador();
        if (error != null)
          return error;

        if (request.Archivo == null || request.Archivo.Length == 0)
          return BadRequest(new { message = "El archivo es obligatorio" });

        var extension = Path.GetExtension(request.Archivo.FileName).ToLower();
        if (extension != ".csv" && extension != ".txt")
          return BadRequest(new { message = "Solo se permiten archivos CSV o TXT" });

        var listaResultados = new List<ResultadoItem>();

        using (var reader = new StreamReader(request.Archivo.OpenReadStream()))
        {
          // Opcional: Descomentar si el CSV tiene encabezados y quieres saltar la primera linea
          // await reader.ReadLineAsync();

          while (!reader.EndOfStream)
          {
            var linea = await reader.ReadLineAsync();
            if (string.IsNullOrWhiteSpace(linea))
              continue;

            var valores = linea.Split(','); // Separador coma

            // Validación basica de columnas (mínimo DNI y Tiempo)
            if (valores.Length < 2)
              continue;

            try
            {
              var item = new ResultadoItem
              {
                // Columna 0: DNI
                Dni = int.Parse(valores[0].Trim()),
                // Columna 1: Tiempo
                TiempoOficial = valores[1].Trim(),
                // Columna 2: Pos General (Opcional)
                PosicionGeneral = (valores.Length > 2 && int.TryParse(valores[2], out int pg)) ? pg : null,
                // Columna 3: Pos Categoria (Opcional)
                PosicionCategoria = (valores.Length > 3 && int.TryParse(valores[3], out int pc)) ? pc : null
              };

              listaResultados.Add(item);
            }
            catch
            {
              continue; // Ignoramos líneas mal formadas
            }
          }
        }

        if (listaResultados.Count == 0)
          return BadRequest(new { message = "No se pudieron leer resultados validos del archivo." });

        // Transformamos los datos leidos al objeto que el repositorio entiende
        var requestRepo = new CargarResultadosRequest
        {
          IdEvento = request.IdEvento,
          Resultados = listaResultados
        };

        // Delegamos la lógica de negocio al repositorio
        var resultado = await _resultadoRepo.CargarResultadosBatchAsync(requestRepo, userId);

        return Ok(new
        {
          message = $"Archivo procesado. {resultado.Exitosos} cargados, {resultado.Fallidos} fallidos.",
          detalles = resultado
        });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new
        {
          message = "Error al procesar archivo",
          error = ex.Message
        });
      }
    }

    [HttpPut("{id}/TiempoOficial")]
    [Authorize]
    public async Task<IActionResult> ActualizarTiempoOficial(int id, [FromBody] ActualizarTiempoOficialRequest request)
    {
      try
      {
        var (userId, error) = ValidarOrganizador();
        if (error != null)
          return error;

        var (exito, errorMsg) = await _resultadoRepo.ActualizarTiempoOficialAsync(id, request.TiempoOficial, userId);

        if (!exito)
          return BadRequest(new { message = errorMsg });

        return Ok(new { message = "Tiempo oficial actualizado" });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new
        {
          message = "Error",
          error = ex.Message
        });
      }
    }

    [HttpPut("{id}/Posiciones")]
    [Authorize]
    public async Task<IActionResult> ActualizarPosiciones(int id, [FromBody] ActualizarPosicionesRequest request)
    {
      try
      {
        var (userId, error) = ValidarOrganizador();
        if (error != null)
          return error;

        var (exito, errorMsg) = await _resultadoRepo.ActualizarPosicionesAsync(id, request, userId);

        if (!exito)
          return BadRequest(new { message = errorMsg });

        return Ok(new { message = "Posiciones actualizadas" });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new
        {
          message = "Error",
          error = ex.Message
        });
      }
    }

    [HttpDelete("{id}")]
    [Authorize]
    public async Task<IActionResult> EliminarResultado(int id)
    {
      try
      {
        var (userId, error) = ValidarOrganizador();
        if (error != null)
          return error;

        var (exito, errorMsg) = await _resultadoRepo.EliminarResultadoAsync(id, userId);

        if (!exito)
          return BadRequest(new { message = errorMsg });

        return Ok(new { message = "Resultado eliminado" });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new
        {
          message = "Error",
          error = ex.Message
        });
      }
    }

    // ═══════════════════ HELPERS ═══════════════════
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
        return (0, BadRequest(new { message = "Solo los organizadores pueden realizar esta acción" }));

      return (userId, null);
    }
  }
}