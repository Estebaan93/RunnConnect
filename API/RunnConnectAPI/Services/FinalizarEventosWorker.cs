using Microsoft.EntityFrameworkCore;
using RunnConnectAPI.Data;
using RunnConnectAPI.Repositories; //

namespace RunnConnectAPI.Services
{
  public class FinalizarEventosWorker : BackgroundService
  {
    private readonly IServiceProvider _serviceProvider;
    private readonly ILogger<FinalizarEventosWorker> _logger;

    public FinalizarEventosWorker(IServiceProvider serviceProvider, ILogger<FinalizarEventosWorker> logger)
    {
      _serviceProvider = serviceProvider;
      _logger = logger;
    }

    protected override async Task ExecuteAsync(CancellationToken stoppingToken)
    {
      _logger.LogInformation("Worker de Finalizacion Automatica (6hs) INICIADO.");

      while (!stoppingToken.IsCancellationRequested)
      {
        try
        {
          await ProcesarEventosVencidos();
        }
        catch (Exception ex)
        {
          _logger.LogError(ex, "Error en el proceso de finalizacion de eventos.");
        }

        // Verificar cada 30 minutos
        await Task.Delay(TimeSpan.FromMinutes(30), stoppingToken);
      }
    }

    private async Task ProcesarEventosVencidos()
    {
      // Creamos un scope porque el Repositorio es Scoped y el Worker es Singleton
      using (var scope = _serviceProvider.CreateScope())
      {
        // Solicitamos el Repositorio en lugar del Contexto directo
        var eventoRepo = scope.ServiceProvider.GetRequiredService<EventoRepositorio>();

        // Delegamos la logica al repositorio
        int cantidadFinalizados = await eventoRepo.FinalizarEventosVencidosAsync();

        if (cantidadFinalizados > 0)
        {
          _logger.LogInformation($"[AUTO-FIN] Se han finalizado {cantidadFinalizados} eventos y sus categorias vencidas.");
        }
      }
    }
  }
}