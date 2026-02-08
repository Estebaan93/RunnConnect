using FirebaseAdmin;
using FirebaseAdmin.Messaging;
using Google.Apis.Auth.OAuth2;

namespace RunnConnectAPI.Services
{
  public class FirebaseNotificacionService
  {
    // Constructor estatico: Se ejecuta una sola vez cuando inicia la API
    // Su funcion es conectar con Google usando el archivo .json
    static FirebaseNotificacionService()
    {
      // Nombre EXACTO del archivo json
      string fileName = "runnConnect.json";
      string path = Path.Combine(Directory.GetCurrentDirectory(), fileName);

      // Verificamos si Firebase ya esta iniciado para no iniciarlo dos veces
      if (FirebaseApp.DefaultInstance == null)
      {
        try
        {
          FirebaseApp.Create(new AppOptions()
          {
            Credential = GoogleCredential.FromFile(path)
          });
          Console.WriteLine(" Conexión con Firebase establecida correctamente.");
        }
        catch (Exception ex)
        {
          Console.WriteLine($"Error fatal al iniciar Firebase: {ex.Message}");
          Console.WriteLine($"Asegúrate de que el archivo '{fileName}' esté en la carpeta raíz y copiado al output.");
        }
      }
    }

    /* "topic" Nombre del topico (ej: "evento_26" o "evento_26_cat_23")
       "titulo" Titulo visible en la barra de notificaciones
       "cuerpo" Mensaje visible
       "idEvento" ID del evento para que la App sepa a donde ir al tocar*/
    public async Task<bool> EnviarNotificacionTopicAsync(string topic, string titulo, string cuerpo, int idEvento)
    {
      try
      {
        var message = new Message()
        {
          Topic = topic,

          // Lo que ve el usuario visualmente en su celular
          Notification = new Notification
          {
            Title = titulo,
            Body = cuerpo
          },

          // Datos ocultos (Payload) para que la App Android sepa que hacer
          Data = new Dictionary<string, string>()
                    {
                        { "click_action", "FLUTTER_NOTIFICATION_CLICK" }, // Estandar, funciona en nativo tambien
                        { "idEvento", idEvento.ToString() },
                        { "tipo", "aviso_evento" }
                    }
        };

        // Enviamos el mensaje a la nube de Google
        string response = await FirebaseMessaging.DefaultInstance.SendAsync(message);

        Console.WriteLine($" Notificación enviada al tópico '{topic}': {response}");
        return true;
      }
      catch (Exception ex)
      {
        Console.WriteLine($" Error al enviar notificación Push: {ex.Message}");
        // Retornamos false pero NO lanzamos error para no romper el flujo del endpoint
        return false;
      }
    }
  }
}