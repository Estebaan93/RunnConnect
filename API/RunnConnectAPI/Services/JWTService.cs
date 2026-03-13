//Services/JWTService
using System.IdentityModel.Tokens.Jwt; //Para validar, crear, y leer el JWT
using System.Security.Claims; //Define y gestiona las claims (inf dentro del token)
using Microsoft.IdentityModel.Tokens; // Para firmar y validar los tokens
using System.Text; //Para convertir string a byts (clave secreta)
using RunnConnectAPI.Models; // Importa el modelo Usuario definido en tu proyecto

namespace RunnConnectAPI.Services // Define el espacio de nombres donde vive este servicio
{
  public class JWTService //Clase que encapsula toda la logica
  {
    private readonly IConfiguration _config; // Configuracion inyectada (lee valores de appsettings.json)

    public JWTService(IConfiguration config) // Constructor que recibe la configuracion
    {
      _config = config; // Guarda la configuracion para usarla en la clase
    }

    //Metodo que genera un JWT para un usuario
    public string GenerarToken(Usuario usuario)
    {
      //Determinar el nombre segun el usuario
      string nombreCompleto = usuario.Nombre;

      // Si es runner y tiene perfil cargado, usar nombre + apellido
      if (usuario.TipoUsuario.ToLower() == "runner" && usuario.PerfilRunner != null)
      {
        nombreCompleto = $"{usuario.PerfilRunner.Nombre} {usuario.PerfilRunner.Apellido}";
      }

      //Lista de claims (dentro del token)
      var claims = new List<Claim>
      {
        new Claim(ClaimTypes.NameIdentifier, usuario.IdUsuario.ToString()),
        new Claim(ClaimTypes.Email, usuario.Email),
        new Claim(ClaimTypes.Name, nombreCompleto),
        new Claim(ClaimTypes.Role, usuario.TipoUsuario)
        //new Claim("TipoUsuario", usuario.TipoUsuario) //Revisar esta duplicado por ROLE
      };

      //Clave secreta para firma token
      var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(_config["Jwt:Key"])); //Clave secreta para firmar el token

      //Credenciales de firma
      var creds = new SigningCredentials(key, SecurityAlgorithms.HmacSha256); //Credencias de forma usando HMAC-SHA256

      //Crear token
      var token = new JwtSecurityToken(
        issuer: _config["Jwt:Issuer"],
        audience: _config["Jwt:Audience"],
        claims: claims,
        expires: DateTime.UtcNow.AddHours(1), //Valido por 1 hora
        signingCredentials: creds
      );

      return new JwtSecurityTokenHandler().WriteToken(token);

    }

  }

  public static class ClaimsPrincipalExtensions
  {
    public static int ObtenerUserId(this ClaimsPrincipal user)
    {
      var userIdClaim = user.FindFirst(ClaimTypes.NameIdentifier);
      if (userIdClaim == null)
        throw new UnauthorizedAccessException("ID de usuario no encontrado en el token.");

      return int.Parse(userIdClaim.Value);
    }

    public static string ObtenerRol(this ClaimsPrincipal user)
    {
      var rolClaim = user.FindFirst(ClaimTypes.Role);
      return rolClaim?.Value ?? "";
    }
  }



}