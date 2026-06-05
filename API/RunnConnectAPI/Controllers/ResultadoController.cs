//Controllers/ResultadoController
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using RunnConnectAPI.Models.Dto.Resultado;
using RunnConnectAPI.Models.Dto.Notificacion; //para la inyeccion de la notif
using RunnConnectAPI.Repositories;
using System.Security.Claims;
using System.IO;
using RunnConnectAPI.Services;

namespace RunnConnectAPI.Controllers
{
  [ApiController]
  [Route("api/[controller]")]
  public class ResultadoController : ControllerBase
  {
    private readonly ResultadoRepositorio _resultadoRepo;
    private readonly NotificacionRepositorio _notificacionRepo;


    public ResultadoController(ResultadoRepositorio resultadoRepo, NotificacionRepositorio notificacionRepo)
    {
      _resultadoRepo = resultadoRepo;
      _notificacionRepo = notificacionRepo;
    }

    // LECTURA (PUBLICO & RUNNER) 
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

    //lectura gnal de resultados  
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

    //podis por cate
    [HttpGet("Evento/{idEvento}/Podios")]
    public async Task<IActionResult> ObtenerPodios(int idEvento)
    {
      try
      {
        var podios = await _resultadoRepo.ObtenerPodiosAsync(idEvento);
        
        if (podios == null) 
            return NotFound(new { message = "Evento no encontrado" });
            
        return Ok(podios);
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al obtener podios", error = ex.Message });
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

    [Authorize(Roles = "runner")]
    [HttpGet("MisResultados")]
    public async Task<IActionResult> MisResultados()
    {
      try
      {
        int userId = User.ObtenerUserId();

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

    [Authorize(Roles = "runner")]
    [HttpPut("{id}/DatosSmartwatch")]
    public async Task<IActionResult> AgregarDatosSmartwatch(int id, [FromBody] DatosSmartwatchRequest request)
    {
      try
      {
        int userId = User.ObtenerUserId();

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

    // GESTION (ORGANIZADOR)
    // CARGA MASIVA -> Recibe Archivo CSV
    // Formato CSV esperado: DNI,TiempoOficial,PosGeneral,PosCategoria
    [Authorize(Roles = "organizador")]
    [HttpPost("CargarArchivo")]
    [Consumes("multipart/form-data")]
    public async Task<IActionResult> CargarArchivoResultados([FromForm] SubirResultadosArchivoRequest request)
    {
      try
      {
        int userId = User.ObtenerUserId();

        if (request.Archivo == null || request.Archivo.Length == 0)
          return BadRequest(new { message = "El archivo es obligatorio" });

        var extension = Path.GetExtension(request.Archivo.FileName).ToLower();
        if (extension != ".csv" && extension != ".txt")
          return BadRequest(new { message = "Solo se permiten archivos CSV o TXT" });

        var listaResultados = new List<ResultadoItem>();

        using (var reader = new StreamReader(request.Archivo.OpenReadStream()))
        {
          // descomentar si el CSV tiene encabezados y quieres saltar la primera linea
          // await reader.ReadLineAsync();

          while (!reader.EndOfStream)
          {
            var linea = await reader.ReadLineAsync();
            if (string.IsNullOrWhiteSpace(linea))
              continue;

            var valores = linea.Split(','); // Separador coma

            // Validacion basica de columnas (minimo DNI y Tiempo)
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
                // Columna 2: Pos Categoria
                PosicionCategoria = (valores.Length > 2 && int.TryParse(valores[2], out int pc)) ? pc : null
              };

              listaResultados.Add(item);
            }
            catch
            {
              continue; // Ignoramos lineas mal formadas
            }
          }
        }

        if (listaResultados.Count == 0)
          return BadRequest(new { message = "No se pudieron leer resultados validos del archivo." });

        // Transformamos los datos leidos al objeto que el repositorio entiende
        var requestRepo = new CargarResultadosRequest
        {
          IdEvento = request.IdEvento,
          IdCategoria = request.IdCategoria,
          Resultados = listaResultados
        };

        // Delegamos la logica de negocio al repositorio
        var resultado = await _resultadoRepo.CargarResultadosAsync(requestRepo, userId);

        //inyeccion de notif
        if (resultado.Exitosos > 0 && resultado.CategoriasActualizadas.Any())
        {
            // Enviamos una notificacion por CADA categoria que haya recibido resultados en este CSV
            foreach (var idCatActualizada in resultado.CategoriasActualizadas)
            {
                var notif = new CrearNotificacionRequest
                {
                    IdEvento = request.IdEvento,
                    IdCategoria = idCatActualizada, //Notifica solo a esta categoria
                    Titulo = "¡Resultados Oficiales Disponibles!",
                    Mensaje = "Se han cargado los tiempos de tu categoría. ¡Revisa tu posición en los Podios!"
                };
                
                // Usamos el metodo CrearAsync normal (no el global) y le pasamos el userId
                await _notificacionRepo.CrearAsync(notif, userId);
            }
        }

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


    [Authorize(Roles = "organizador")]
    [HttpPut("{id}/TiempoOficial")]
    public async Task<IActionResult> ActualizarTiempoOficial(int id, [FromBody] ActualizarTiempoOficialRequest request)
    {
      try
      {
        int userId = User.ObtenerUserId();

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

    [Authorize(Roles = "organizador")]
    [HttpPut("{id}/Posiciones")]
    public async Task<IActionResult> ActualizarPosiciones(int id, [FromBody] ActualizarPosicionesRequest request)
    {
      try
      {
        int userId = User.ObtenerUserId();

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

    //eliminacion
    [Authorize(Roles = "organizador")]
    [HttpDelete("{id}")]
    public async Task<IActionResult> EliminarResultado(int id)
    {
      try
      {
        int userId = User.ObtenerUserId();

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

   

  }
}
