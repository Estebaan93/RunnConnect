using Microsoft.EntityFrameworkCore;
using RunnConnectAPI.Data;


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

				// verificar cada 30 minutos para ser mas precisos con el corte de las 6hs
				//metodo async, pausa pero no bloquea el hilo principal
				await Task.Delay(TimeSpan.FromMinutes(30), stoppingToken);

				//pruebas
				//await Task.Delay(TimeSpan.FromSeconds(15), stoppingToken);
			}
		}

		private async Task ProcesarEventosVencidos()
		{
			using (var scope = _serviceProvider.CreateScope())
			{
				var context = scope.ServiceProvider.GetRequiredService<RunnersContext>();

				// REGLA DE NEGOCIO: 6 HORAS DESPUES DE LA LARGADA
				// Si son las 14, buscamos eventos que iniciaron antes de las 08.
				DateTime tiempoLimite = DateTime.Now.AddHours(-6);

				// Buscamos eventos 'publicado' o 'suspendido' cuya fecha de inicio ya paso el limite
				var eventosVencidos = await context.Eventos
						.Where(e => e.FechaHora < tiempoLimite
										 && (e.Estado == "publicado" || e.Estado == "suspendido"))
						.ToListAsync();

				if (eventosVencidos.Any())
				{
					foreach (var evento in eventosVencidos)
					{
						evento.Estado = "finalizado";
						_logger.LogInformation($"[AUTO-FIN] Evento ID {evento.IdEvento} finalizado por tiempo limite (6hs).");
					}

					await context.SaveChangesAsync();
				}
			}
		}
	}
}