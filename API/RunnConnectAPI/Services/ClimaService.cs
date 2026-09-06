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

    /// Obtiene la URL del icono del clima para un evento segun sus coordenadas y fecha/hora.
    /// Si el evento es a mas de 5 dias o ya ocurrio, retorna null.
    public async Task<string?> ObtenerIconoClimaAsync(decimal latitud, decimal longitud, DateTime fechaHoraEvento)
    {
      try
      {
        var ahora = DateTime.Now;

        // Si el evento ya ocurrio o falta mas de 5 dias, no tiene clima disponible
        if (fechaHoraEvento < ahora || fechaHoraEvento > ahora.AddDays(5))
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

        if (!document.RootElement.TryGetProperty("list", out var listElement) || listElement.GetArrayLength() == 0)
        {
          return null;
        }

        var targetUnix = ((DateTimeOffset)fechaHoraEvento.ToUniversalTime()).ToUnixTimeSeconds();
        long menorDiferencia = long.MaxValue;
        string? iconoMasCercano = null;

        foreach (var item in listElement.EnumerateArray())
        {
          if (item.TryGetProperty("dt", out var dtElement))
          {
            var dt = dtElement.GetInt64();
            var diferencia = Math.Abs(dt - targetUnix);

            if (diferencia < menorDiferencia)
            {
              menorDiferencia = diferencia;

              if (item.TryGetProperty("weather", out var weatherElement) && weatherElement.GetArrayLength() > 0)
              {
                var primerWeather = weatherElement[0];
                if (primerWeather.TryGetProperty("icon", out var iconElement))
                {
                  iconoMasCercano = iconElement.GetString();
                }
              }
            }
          }
        }

        if (string.IsNullOrWhiteSpace(iconoMasCercano))
        {
          return null;
        }

        // URL oficial de OpenWeather para el icono en tamaño (@2x)
        return $"https://openweathermap.org/img/wn/{iconoMasCercano}@2x.png";
      }
      catch (Exception ex)
      {
        _logger.LogError(ex, "[ClimaService] Excepción al obtener el clima para coordenadas ({Lat}, {Lon})", latitud, longitud);
        return null;
      }
    }
  }
}
