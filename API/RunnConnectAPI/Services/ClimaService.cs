// Services/ClimaService.cs
using System.Globalization;
using System.Text.Json;

namespace RunnConnectAPI.Services
{
  public class ClimaService
  {
    private readonly HttpClient _httpClient;
    private readonly IConfiguration _configuration;
    private readonly ILogger<ClimaService> _logger;

    public ClimaService(HttpClient httpClient, IConfiguration configuration, ILogger<ClimaService> logger)
    {
      _httpClient = httpClient;
      _configuration = configuration;
      _logger = logger;
    }

    /// Obtiene un JSON con el icono del clima y las temperaturas min y max ({"icono":"...","min":10,"max":25})
    /// Si el evento es a mas de 5 dias, toma el clima del dia actual como referencia inicial.
    /// Si el evento ya ocurrio en el pasado, retorna null.
    public async Task<string?> ObtenerPronosticoClimaAsync(decimal latitud, decimal longitud, DateTime fechaHoraEvento)
    {
      try
      {
        var ahora = DateTime.Now;

        // Si el evento ya ocurrió en el pasado, no tiene clima disponible
        if (fechaHoraEvento < ahora)
        {
          return null;
        }

        var apiKey = _configuration["OpenWeather:ApiKey"];
        if (string.IsNullOrWhiteSpace(apiKey))
        {
          _logger.LogWarning("[ClimaService] No se encontró la API Key de OpenWeather en la configuración.");
          return null;
        }

        var latStr = latitud.ToString(CultureInfo.InvariantCulture);
        var lonStr = longitud.ToString(CultureInfo.InvariantCulture);
        var url = $"https://api.openweathermap.org/data/2.5/forecast?lat={latStr}&lon={lonStr}&units=metric&lang=es&appid={apiKey}";

        using var response = await _httpClient.GetAsync(url);
        if (!response.IsSuccessStatusCode)
        {
          _logger.LogWarning("[ClimaService] Error al consultar OpenWeather: {StatusCode} para coordenadas ({Lat}, {Lon})", response.StatusCode, latStr, lonStr);
          return null;
        }

        var jsonString = await response.Content.ReadAsStringAsync();
        using var document = JsonDocument.Parse(jsonString);
        return ProcesarForecastJson(document, fechaHoraEvento, ahora);
      }
      catch (Exception ex)
      {
        _logger.LogError(ex, "[ClimaService] Excepción al obtener el clima para coordenadas ({Lat}, {Lon})", latitud, longitud);
        return null;
      }
    }

    /// Obtiene un JSON con el icono del clima y las temperaturas min y max a partir del nombre de la ciudad o localidad.
    public async Task<string?> ObtenerPronosticoClimaPorCiudadAsync(string ciudad, DateTime fechaHoraEvento)
    {
      try
      {
        var ahora = DateTime.Now;
        if (fechaHoraEvento < ahora || string.IsNullOrWhiteSpace(ciudad))
        {
          return null;
        }

        var apiKey = _configuration["OpenWeather:ApiKey"];
        if (string.IsNullOrWhiteSpace(apiKey))
        {
          _logger.LogWarning("[ClimaService] No se encontró la API Key de OpenWeather en la configuración.");
          return null;
        }

        string queryCiudad = ciudad.Trim();
        var url = $"https://api.openweathermap.org/data/2.5/forecast?q={Uri.EscapeDataString(queryCiudad)}&units=metric&lang=es&appid={apiKey}";
        using var response = await _httpClient.GetAsync(url);

        HttpResponseMessage? fallbackResponse = null;
        HttpResponseMessage finalResponse = response;

        if (!response.IsSuccessStatusCode)
        {
          // Fallback 1: agregar ",AR"
          var urlAr = $"https://api.openweathermap.org/data/2.5/forecast?q={Uri.EscapeDataString(queryCiudad + ",AR")}&units=metric&lang=es&appid={apiKey}";
          fallbackResponse = await _httpClient.GetAsync(urlAr);
          if (fallbackResponse.IsSuccessStatusCode)
          {
            finalResponse = fallbackResponse;
          }
          else if (queryCiudad.Contains(","))
          {
            // Fallback 2: tomar la última parte tras la coma (ej: de "Parque de las Naciones, San Luis" tomar "San Luis")
            var partes = queryCiudad.Split(',');
            var ultimaParte = partes[^1].Trim();
            if (!string.IsNullOrWhiteSpace(ultimaParte))
            {
              var urlParte = $"https://api.openweathermap.org/data/2.5/forecast?q={Uri.EscapeDataString(ultimaParte + ",AR")}&units=metric&lang=es&appid={apiKey}";
              var fallbackParte = await _httpClient.GetAsync(urlParte);
              if (fallbackParte.IsSuccessStatusCode)
              {
                fallbackResponse?.Dispose();
                fallbackResponse = fallbackParte;
                finalResponse = fallbackResponse;
              }
              else
              {
                fallbackParte.Dispose();
              }
            }
          }
        }

        using (fallbackResponse)
        {
          if (!finalResponse.IsSuccessStatusCode)
          {
            _logger.LogWarning("[ClimaService] Error al consultar OpenWeather por ciudad: {StatusCode} para '{Ciudad}'", finalResponse.StatusCode, ciudad);
            return null;
          }

          var jsonString = await finalResponse.Content.ReadAsStringAsync();
          using var document = JsonDocument.Parse(jsonString);
          return ProcesarForecastJson(document, fechaHoraEvento, ahora);
        }
      }
      catch (Exception ex)
      {
        _logger.LogError(ex, "[ClimaService] Excepción al obtener el clima para ciudad '{Ciudad}'", ciudad);
        return null;
      }
    }

    private string? ProcesarForecastJson(JsonDocument document, DateTime fechaHoraEvento, DateTime ahora)
    {
      if (!document.RootElement.TryGetProperty("list", out var listElement) || listElement.GetArrayLength() == 0)
      {
        return null;
      }

      // Determinar fecha objetivo:
      // Si el evento está dentro de los 5 días, usamos la fecha y hora del evento.
      // Si falta más de 5 días, tomamos el día de hoy como clima de referencia.
      bool dentroDeVentana = fechaHoraEvento <= ahora.AddDays(5);
      DateTime fechaReferencia = dentroDeVentana ? fechaHoraEvento : ahora;
      var targetDate = fechaReferencia.Date;

      double minTemp = double.MaxValue;
      double maxTemp = double.MinValue;
      bool encontroDatosDelDia = false;

      foreach (var item in listElement.EnumerateArray())
      {
        if (item.TryGetProperty("dt", out var dtElement))
        {
          var dt = dtElement.GetInt64();
          var itemDate = DateTimeOffset.FromUnixTimeSeconds(dt).LocalDateTime.Date;

          // Procesar temperaturas min / max del día objetivo
          if (itemDate == targetDate)
          {
            if (item.TryGetProperty("main", out var mainElement))
            {
              if (mainElement.TryGetProperty("temp_min", out var minEl))
              {
                minTemp = Math.Min(minTemp, minEl.GetDouble());
                encontroDatosDelDia = true;
              }
              if (mainElement.TryGetProperty("temp_max", out var maxEl))
              {
                maxTemp = Math.Max(maxTemp, maxEl.GetDouble());
                encontroDatosDelDia = true;
              }
            }
          }
        }
      }

      // Si no encontró bloques del día exacto, tomar los del bloque general
      if (!encontroDatosDelDia)
      {
        foreach (var item in listElement.EnumerateArray())
        {
          if (item.TryGetProperty("main", out var mainElement))
          {
            if (mainElement.TryGetProperty("temp_min", out var minEl)) minTemp = Math.Min(minTemp, minEl.GetDouble());
            if (mainElement.TryGetProperty("temp_max", out var maxEl)) maxTemp = Math.Max(maxTemp, maxEl.GetDouble());
            break;
          }
        }
      }

      if (minTemp == double.MaxValue || maxTemp == double.MinValue)
      {
        return null;
      }

      int tempMinInt = (int)Math.Round(minTemp);
      int tempMaxInt = (int)Math.Round(maxTemp);

      // Asegurar que min <= max
      if (tempMinInt > tempMaxInt)
      {
        (tempMinInt, tempMaxInt) = (tempMaxInt, tempMinInt);
      }

      var climaObj = new
      {
        min = tempMinInt,
        max = tempMaxInt
      };

      return JsonSerializer.Serialize(climaObj);
    }

    public Task<string?> ObtenerIconoClimaAsync(decimal latitud, decimal longitud, DateTime fechaHoraEvento)
    {
      return ObtenerPronosticoClimaAsync(latitud, longitud, fechaHoraEvento);
    }
  }
}
