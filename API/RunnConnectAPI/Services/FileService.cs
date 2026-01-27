//Services/FileService
using System.IO;

namespace RunnConnectAPI.Services
{
  /// Servicio para manejo de archivos (avatares (por defecto se le asigna uno), comprobantes, etc.)

  public class FileService
  {
    private readonly IWebHostEnvironment _environment;
    private readonly IConfiguration _configuration;

    public FileService(IWebHostEnvironment environment, IConfiguration configuration)
    {
      _environment = environment;
      _configuration = configuration;
    }

    /*Obtenemos la ruta del avatar por defecto, segun tipo de usuario*/
    public string ObtenerAvatarPorDefecto(string tipoUsuario)
    {
      return tipoUsuario.ToLower() switch
      {
        "organizador" => "/uploads/avatars/defaults/default_organization.png",
        "runner" => "/uploads/avatars/defaults/default_runner.png",
        _ => "/uploads/avatars/defaults/default_runner.png" // Por defecto, runner
      };
    }


    /// Guarda un avatar y retorna la URL relativa
    public async Task<string> GuardarAvatarAsync(IFormFile archivo, int idUsuario)
    {
      if (!ValidarImagen(archivo))
        throw new Exception("Archivo de imagen invalido");

      // Validar extension
      var extensionesPermitidas = new[] { ".jpg", ".jpeg", ".png", ".gif", ".webp" };
      var extension = Path.GetExtension(archivo.FileName).ToLowerInvariant();

      if (!extensionesPermitidas.Contains(extension))
        throw new Exception("Formato de imagen no permitido. Use: jpg, jpeg, png, gif o webp");

      // Validar tamaño (maximo 20MB)
      if (archivo.Length > 20 * 1024 * 1024)
        throw new Exception("La imagen no puede superar 20MB");

      // Crear carpeta si no existe
      var carpetaAvatares = Path.Combine(_environment.WebRootPath, "uploads", "avatars");
      if (!Directory.Exists(carpetaAvatares))
        Directory.CreateDirectory(carpetaAvatares);

      // Generar nombre unico: userid_timestamp.extension
      var nombreArchivo = $"{idUsuario}_{DateTime.Now:yyyyMMddHHmmss}{extension}";
      var rutaCompleta = Path.Combine(carpetaAvatares, nombreArchivo);

      // Guardar archivo
      using (var stream = new FileStream(rutaCompleta, FileMode.Create))
      {
        await archivo.CopyToAsync(stream);
      }

      // Retornar URL relativa
      return $"/uploads/avatars/{nombreArchivo}";
    }

    /*Guardar avatar durante el registro (opcional)
    Si no se sube un avatar el sistema le asigna uno por defecto*/
    public async Task<string> GuardarAvatarRegistroAsync(IFormFile? archivo, int idUsuario, string tipoUsuario)
    {
      //Si no hay archivo retorna avatar por defecto
      if (archivo == null || archivo.Length == 0)
      {
        return ObtenerAvatarPorDefecto(tipoUsuario);
      }

      //Si hay archivo, guardar
      return await GuardarAvatarAsync(archivo, idUsuario);
    }

    /// Elimina un avatar del servidor (solo si no e avatar por defecto)
    public bool EliminarAvatar(string urlRelativa)
    {
      try
      {
        if (string.IsNullOrEmpty(urlRelativa))
          return false;

        //No elimino URLs externas
        if (urlRelativa.StartsWith("http://") || urlRelativa.StartsWith("https://"))
          return false;

        //No eliminar avatares por defecto
        if (EsAvatarPorDefecto(urlRelativa))
          return false;

        // Convertir URL relativa a ruta fisica
        var rutaFisica = Path.Combine(_environment.WebRootPath, urlRelativa.TrimStart('/'));

        if (File.Exists(rutaFisica))
        {
          File.Delete(rutaFisica);
          return true;
        }

        return false;
      }
      catch
      {
        return false;
      }
    }


    /// Obtiene la URL completa del avatar
    public string ObtenerUrlCompleta(string urlRelativa, HttpRequest request)
    {
      if (string.IsNullOrEmpty(urlRelativa))
        return string.Empty;

      // Si ya es una URL completa, retornarla tal cual
      if (urlRelativa.StartsWith("http://") || urlRelativa.StartsWith("https://"))
        return urlRelativa;

      // Construir URL completa
      var scheme = request.Scheme; // http o https
      var host = request.Host.Value; // localhost:5000
      return $"{scheme}://{host}{urlRelativa}";
    }

    /*Verificar si una URL es un avatar por defecto*/
    public bool EsAvatarPorDefecto(string? urlAvatar)
    {
      if (string.IsNullOrEmpty(urlAvatar))
        return false;

      return urlAvatar.Contains("/defaults/");
    }

    /// Valida que una imagen sea valida
    public bool ValidarImagen(IFormFile archivo)
    {
      try
      {
        var extensionesPermitidas = new[] { ".jpg", ".jpeg", ".png", ".gif", ".webp" };
        var extension = Path.GetExtension(archivo.FileName).ToLowerInvariant();

        if (!extensionesPermitidas.Contains(extension))
          return false;

        if (archivo.Length > 20 * 1024 * 1024) // 20MB
          return false;

        // Verificar que el archivo tenga contenido
        if (archivo.Length == 0)
          return false;

        // Verificar los primeros bytes (magic numbers) para asegurar que es una imagen
        using (var stream = archivo.OpenReadStream())
        {
          var buffer = new byte[8];
          stream.Read(buffer, 0, 8);

          // JPEG: FF D8 FF
          if (buffer[0] == 0xFF && buffer[1] == 0xD8 && buffer[2] == 0xFF)
            return true;

          // PNG: 89 50 4E 47
          if (buffer[0] == 0x89 && buffer[1] == 0x50 && buffer[2] == 0x4E && buffer[3] == 0x47)
            return true;

          // GIF: 47 49 46
          if (buffer[0] == 0x47 && buffer[1] == 0x49 && buffer[2] == 0x46)
            return true;

          // WebP: 52 49 46 46
          if (buffer[0] == 0x52 && buffer[1] == 0x49 && buffer[2] == 0x46 && buffer[3] == 0x46)
            return true;

          return false;
        }
      }
      catch
      {
        return false;
      }
    }

    /*wwwtoor/uploads/comprobantes/ -Guarda comprobantes de pasgos-*/
    /*Guardar el comprobante de pago y retornar la ruta relativa*/
    public async Task<string> GuardarComprobanteAsync(IFormFile archivo, int idInscripcion)
    {
      // Validar archivo
      if (archivo == null || archivo.Length == 0)
        throw new Exception("Archivo inválido");

      // Validar extensiin
      var extensionesPermitidas = new[] { ".jpg", ".jpeg", ".png", ".pdf" };
      var extension = Path.GetExtension(archivo.FileName).ToLowerInvariant();

      if (!extensionesPermitidas.Contains(extension))
        throw new Exception("Formato no permitido. Use: jpg, jpeg, png o pdf");

      // Validar tamaño (máximo 5MB)
      if (archivo.Length > 5 * 1024 * 1024)
        throw new Exception("El archivo no puede superar 5MB");

      // Crear carpeta si no existe
      var carpetaComprobantes = Path.Combine(_environment.WebRootPath, "uploads", "comprobantes");
      if (!Directory.Exists(carpetaComprobantes))
        Directory.CreateDirectory(carpetaComprobantes);

      // Generar nombre unico: comprobante_idInscripcion_timestamp.extension
      var nombreArchivo = $"comprobante_{idInscripcion}_{DateTime.Now:yyyyMMddHHmmss}{extension}";
      var rutaCompleta = Path.Combine(carpetaComprobantes, nombreArchivo);

      // Guardar archivo
      using (var stream = new FileStream(rutaCompleta, FileMode.Create))
      {
        await archivo.CopyToAsync(stream);
      }

      // Retornar URL relativa
      return $"/uploads/comprobantes/{nombreArchivo}";
    }





  }
}