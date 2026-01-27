// Services/EmailService.cs
using System.Net;
using System.Net.Mail;

namespace RunnConnectAPI.Services
{
  public class EmailService
  {
    private readonly IConfiguration _configuration;

    public EmailService(IConfiguration configuration)
    {
      _configuration = configuration;
    }

    public async Task<bool> EnviarEmailRecuperacionAsync(string emailDestino, string nombreUsuario, string token)
    {
      try
      {
        // Configuración SMTP (usa appsettings.json)
        var smtpHost = _configuration["Email:SmtpHost"];
        var smtpPort = int.Parse(_configuration["Email:SmtpPort"]);
        var smtpUser = _configuration["Email:SmtpUser"];
        var smtpPass = _configuration["Email:SmtpPassword"];
        var fromEmail = _configuration["Email:FromEmail"];
        var fromName = _configuration["Email:FromName"];

        // Construir URL de recuperación
        var baseUrl = _configuration["App:BaseUrl"]; // http://localhost:5213 o tu dominio
        var resetUrl = $"{baseUrl}/reset-password?token={token}";

        // Crear mensaje
        var mailMessage = new MailMessage
        {
          From = new MailAddress(fromEmail, fromName),
          Subject = "Recuperacion de Contraseña - RunnConnect",
          Body = GenerarHtmlEmail(nombreUsuario, resetUrl),
          IsBodyHtml = true
        };
        mailMessage.To.Add(emailDestino);

        // Configurar cliente SMTP
        using var smtpClient = new SmtpClient(smtpHost, smtpPort)
        {
          Credentials = new NetworkCredential(smtpUser, smtpPass),
          EnableSsl = true
        };

        // Enviar email
        await smtpClient.SendMailAsync(mailMessage);
        return true;
      }
      catch (Exception ex)
      {
        // Log error (usa un logger en producción)
        Console.WriteLine($"Error al enviar email: {ex.Message}");
        return false;
      }
    }

    private string GenerarHtmlEmail(string nombreUsuario, string resetUrl)
    {
      return $@"
        <!DOCTYPE html>
        <html>
        <head>
          <style>
            body {{ font-family: Arial, sans-serif; line-height: 1.6; color: #333; }}
            .container {{ max-width: 600px; margin: 0 auto; padding: 20px; }}
            .header {{ background: #4CAF50; color: white; padding: 20px; text-align: center; }}
            .content {{ background: #f9f9f9; padding: 30px; }}
            .button {{ 
              display: inline-block; 
              background: #4CAF50; 
              color: white; 
              padding: 15px 30px; 
              text-decoration: none; 
              border-radius: 5px;
              margin: 20px 0;
            }}
            .footer {{ text-align: center; color: #666; padding: 20px; font-size: 12px; }}
          </style>
        </head>
        <body>
          <div class='container'>
            <div class='header'>
              <h1>🏃 RunnConnect</h1>
            </div>
            <div class='content'>
              <h2>Hola {nombreUsuario},</h2>
              <p>Recibimos una solicitud para restablecer tu contraseña.</p>
              <p>Haz clic en el siguiente botón para crear una nueva contraseña:</p>
              <div style='text-align: center;'>
                <a href='{resetUrl}' class='button'>Restablecer Contraseña</a>
              </div>
              <p><strong>Este enlace expirará en 1 hora.</strong></p>
              <p>Si no solicitaste restablecer tu contraseña, ignora este email.</p>
              <hr>
              <p style='font-size: 12px; color: #666;'>
                Si el botón no funciona, copia y pega este enlace en tu navegador:<br>
                <a href='{resetUrl}'>{resetUrl}</a>
              </p>
            </div>
            <div class='footer'>
              <p>© 2024 RunnConnect - San Luis, Argentina</p>
              <p>Este es un email automático, por favor no respondas.</p>
            </div>
          </div>
        </body>
        </html>
      ";
    }


    public async Task<bool> EnviarEmailReactivacionAsync(string emailDestino, string nombreUsuario, string token)
    {
      try
      {
        // Configuración SMTP (usa appsettings.json)
        var smtpHost = _configuration["Email:SmtpHost"];
        var smtpPort = int.Parse(_configuration["Email:SmtpPort"]);
        var smtpUser = _configuration["Email:SmtpUser"];
        var smtpPass = _configuration["Email:SmtpPassword"];
        var fromEmail = _configuration["Email:FromEmail"];
        var fromName = _configuration["Email:FromName"];

        // Construir URL de reactivación
        var baseUrl = _configuration["App:BaseUrl"];
        var reactivateUrl = $"{baseUrl}/reactivate-account?token={token}";

        // Crear mensaje
        var mailMessage = new MailMessage
        {
          From = new MailAddress(fromEmail, fromName),
          Subject = "Reactivación de Cuenta - RunnConnect",
          Body = GenerarHtmlEmailReactivacion(nombreUsuario, reactivateUrl),
          IsBodyHtml = true
        };
        mailMessage.To.Add(emailDestino);

        // Configurar cliente SMTP
        using var smtpClient = new SmtpClient(smtpHost, smtpPort)
        {
          Credentials = new NetworkCredential(smtpUser, smtpPass),
          EnableSsl = true
        };

        // Enviar email
        await smtpClient.SendMailAsync(mailMessage);
        return true;
      }
      catch (Exception ex)
      {
        Console.WriteLine($"Error al enviar email de reactivación: {ex.Message}");
        return false;
      }
    }

    private string GenerarHtmlEmailReactivacion(string nombreUsuario, string reactivateUrl)
    {
      return $@"
    <!DOCTYPE html>
    <html>
    <head>
      <style>
        body {{ font-family: Arial, sans-serif; line-height: 1.6; color: #333; }}
        .container {{ max-width: 600px; margin: 0 auto; padding: 20px; }}
        .header {{ background: #FF9800; color: white; padding: 20px; text-align: center; }}
        .content {{ background: #f9f9f9; padding: 30px; }}
        .button {{ 
          display: inline-block; 
          background: #FF9800; 
          color: white; 
          padding: 15px 30px; 
          text-decoration: none; 
          border-radius: 5px;
          margin: 20px 0;
        }}
        .footer {{ text-align: center; color: #666; padding: 20px; font-size: 12px; }}
      </style>
    </head>
    <body>
      <div class='container'>
        <div class='header'>
          <h1>🏃 RunnConnect</h1>
        </div>
        <div class='content'>
          <h2>Hola {nombreUsuario},</h2>
          <p>Recibimos una solicitud para reactivar tu cuenta.</p>
          <p>Haz clic en el siguiente botón para reactivar tu cuenta:</p>
          <div style='text-align: center;'>
            <a href='{reactivateUrl}' class='button'>Reactivar Cuenta</a>
          </div>
          <p><strong>Este enlace expirará en 1 hora.</strong></p>
          <p>Si no solicitaste reactivar tu cuenta, ignora este email.</p>
          <hr>
          <p style='font-size: 12px; color: #666;'>
            Si el botón no funciona, copia y pega este enlace en tu navegador:<br>
            <a href='{reactivateUrl}'>{reactivateUrl}</a>
          </p>
        </div>
        <div class='footer'>
          <p>© 2024 RunnConnect - San Luis, Argentina</p>
          <p>Este es un email automático, por favor no respondas.</p>
        </div>
      </div>
    </body>
    </html>
  ";
    }


  }
}