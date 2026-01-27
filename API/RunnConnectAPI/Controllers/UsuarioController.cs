//Controllers/UsuarioController
using Microsoft.AspNetCore.Authorization; //Para Authorize y AllowAnonymous
using Microsoft.AspNetCore.Mvc; //Para controllerBase, IActionResult, [HTTPGet]
using RunnConnectAPI.Models.Dto.Usuario;
using RunnConnectAPI.Models;
using RunnConnectAPI.Repositories;
using System.Security.Claims;
using RunnConnectAPI.Services;

namespace RunnConnectAPI.Controllers
{
  [ApiController]
  [Route("api/[controller]")]
  [Authorize] //Todos los endpoint requieren autenticacion JWT
  public class UsuarioController : ControllerBase
  {
    private readonly UsuarioRepositorio _usuarioRepositorio;
    private readonly JWTService _jwtService;
    private readonly PasswordService _passwordService;
    private readonly FileService _fileService;
    private readonly TokenRecuperacionRepositorio _tokenRecuperacionRepositorio;
    private readonly EmailService _emailService;

    public UsuarioController(UsuarioRepositorio usuarioRepositorio, JWTService jwtService, PasswordService passwordService, FileService fileService, TokenRecuperacionRepositorio tokenRecuperacionRepositorio, EmailService emailService)
    {
      _usuarioRepositorio = usuarioRepositorio;
      _jwtService = jwtService;
      _passwordService = passwordService;
      _fileService = fileService;
      _tokenRecuperacionRepositorio = tokenRecuperacionRepositorio;
      _emailService = emailService;
    }

    /*Autenticacion (Login comun para ambos RUNNER/ORGAN)
    POST: api/usuario/login - 
    Content-type: application/json*/
    [AllowAnonymous]
    [HttpPost("Login")]
    public async Task<IActionResult> Login([FromBody] LoginRequestDto loginRequestDto)
    {
      try
      {
        //Validamos el modelo DataAnnotations
        if (!ModelState.IsValid)
          return BadRequest(ModelState);

        //Buscar por email
        var usuario = await _usuarioRepositorio.GetByEmailAsync(loginRequestDto.Email);

        if (usuario == null || !_passwordService.VerifyPassword(loginRequestDto.Password, usuario.PasswordHash))
          return Unauthorized(new { message = "Credenciales invalidas" });

        //Generar token
        var token = _jwtService.GenerarToken(usuario);
        var avatarUrl = _fileService.ObtenerUrlCompleta(usuario.ImgAvatar, Request);

        //Retornar token
        return Ok(new
        {
          token,
          message = "Login exitoso",
          usuario = new
          {
            idUsuario = usuario.IdUsuario,
            nombre = usuario.Nombre,
            email = usuario.Email,
            tipoUsuario = usuario.TipoUsuario,
            imgAvatar = avatarUrl
          }
        });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error en el login", error = ex.Message });
      }
    }

    /*Registro de nuevo usuarios RUNNERs (avatar opcional) y retorna un JWT para login automatico
    POST: api/usuarios/RegisterRunner
    Content-type: multipart/form-data
    Si no se envia imgAvatar se asigna uno default_runner.png
    nombre, apellido, email, password*/
    [AllowAnonymous]
    [HttpPost("RegisterRunner")]
    [Consumes("multipart/form-data")]
    public async Task<IActionResult> Register([FromForm] RegisterRunnerDto dto)
    {
      try
      {
        //Validar que el email no exista
        if (await _usuarioRepositorio.EmailExistenteAsync(dto.Email.Trim().ToLower()))
          return BadRequest(new { message = "El email ya esta registrado" });

        //Crear usuario base
        var usuario = new Models.Usuario
        {
          Nombre = dto.Nombre.Trim(),
          Email = dto.Email.Trim().ToLower(),
          Telefono = null, // Se completa despues
          PasswordHash = _passwordService.HashPassword(dto.Password),
          TipoUsuario = "runner",
          Estado = true
        };

        //Guardar avatar (o asignar default)
        usuario.ImgAvatar = await _fileService.GuardarAvatarRegistroAsync(
          dto.ImgAvatar,
          0, // El ID se asigna despues de guardar
          "runner"
        );

        //Crear perfil runner con datos basicos
        var perfilRunner = new PerfilRunner
        {
          Nombre = dto.Nombre.Trim(),
          Apellido = dto.Apellido.Trim(),
          // Todos los demas campos quedan NULL hasta que el usuario los complete
          FechaNacimiento = null,
          Genero = null,
          Dni = null,
          Localidad = null,
          Agrupacion = null,
          NombreContactoEmergencia = null,
          TelefonoEmergencia = null
        };

        usuario.PerfilRunner = perfilRunner;

        //Guardar en BD
        await _usuarioRepositorio.CreateAsync(usuario);

        //Si se subio un avatar temporal, renombrarlo con el ID real
        if (dto.ImgAvatar != null && dto.ImgAvatar.Length > 0)
        {
          usuario.ImgAvatar = await _fileService.GuardarAvatarAsync(dto.ImgAvatar, usuario.IdUsuario);
          await _usuarioRepositorio.UpdateAsync(usuario);
        }

        //Generar token JWT
        var token = _jwtService.GenerarToken(usuario);

        //Obtener URL completa del avatar
        var avatarUrl = _fileService.ObtenerUrlCompleta(usuario.ImgAvatar, Request);

        //Verificar si el perfil esta completo
        bool perfilCompleto = !string.IsNullOrEmpty(usuario.Telefono) &&
                              perfilRunner.FechaNacimiento.HasValue &&
                              !string.IsNullOrEmpty(perfilRunner.Genero) &&
                              perfilRunner.Dni.HasValue &&
                              !string.IsNullOrEmpty(perfilRunner.Localidad) &&
                              !string.IsNullOrEmpty(perfilRunner.Agrupacion) &&
                              !string.IsNullOrEmpty(perfilRunner.NombreContactoEmergencia) &&
                              !string.IsNullOrEmpty(perfilRunner.TelefonoEmergencia);

        //Retornar respuesta
        return Ok(new
        {
          message = perfilCompleto
            ? "Runner registrado exitosamente"
            : "Runner registrado exitosamente. Completa tu perfil para inscribirte a eventos",
          token = token,
          usuario = new
          {
            idUsuario = usuario.IdUsuario,
            nombre = usuario.Nombre,
            email = usuario.Email,
            tipoUsuario = usuario.TipoUsuario,
            imgAvatar = avatarUrl,
            perfilCompleto = perfilCompleto
          }
        });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al registrar runner", error = ex.Message });
      }
    }


    /*Registro de nuevos ORGANIZADORES (imgAvatar opcional) y retorna el token
    POST_ api/usuario/RegisterOrganizador 
    Content-type: multipart/form-data
    Si no se envia imgAvatar se asigna uno por defecto default_organization.png
    razonSocial, nombreComercial, email, password*/
    [AllowAnonymous]
    [HttpPost("RegisterOrganizador")]
    [Consumes("multipart/form-data")]
    public async Task<IActionResult> RegisterOrganizador([FromForm] RegisterOrganizadorDto dto)
    {
      try
      {
        //Validar que el email no exista
        if (await _usuarioRepositorio.EmailExistenteAsync(dto.Email.Trim().ToLower()))
          return BadRequest(new { message = "El email ya esta registrado" });

        //Crear usuario base
        var usuario = new Models.Usuario
        {
          Nombre = dto.NombreComercial.Trim(),
          Email = dto.Email.Trim().ToLower(),
          Telefono = null, // Se completa despues
          PasswordHash = _passwordService.HashPassword(dto.Password),
          TipoUsuario = "organizador",
          Estado = true
        };

        //Guardar avatar (o asignar default)
        usuario.ImgAvatar = await _fileService.GuardarAvatarRegistroAsync(
          dto.ImgAvatar,
          0, // El ID se asigna despues de guardar
          "organizador"
        );

        //Crear perfil organizador con datos basicos
        var perfilOrganizador = new PerfilOrganizador
        {
          RazonSocial = dto.RazonSocial.Trim(),
          NombreComercial = dto.NombreComercial.Trim(),
          // Estos campos quedan NULL hasta que el usuario los complete
          CuitTaxId = null,
          DireccionLegal = null
        };

        usuario.PerfilOrganizador = perfilOrganizador;

        //Guardar en BD
        await _usuarioRepositorio.CreateAsync(usuario);

        //Si se subio un avatar temporal, renombrarlo con el ID real
        if (dto.ImgAvatar != null && dto.ImgAvatar.Length > 0)
        {
          usuario.ImgAvatar = await _fileService.GuardarAvatarAsync(dto.ImgAvatar, usuario.IdUsuario);
          await _usuarioRepositorio.UpdateAsync(usuario);
        }

        //Generar token JWT
        var token = _jwtService.GenerarToken(usuario);

        //Obtener URL completa del avatar
        var avatarUrl = _fileService.ObtenerUrlCompleta(usuario.ImgAvatar, Request);

        //Verificar si el perfil esta completo
        bool perfilCompleto = !string.IsNullOrEmpty(usuario.Telefono) &&
                              !string.IsNullOrEmpty(perfilOrganizador.CuitTaxId) &&
                              !string.IsNullOrEmpty(perfilOrganizador.DireccionLegal);

        //Retornar respuesta
        return Ok(new
        {
          message = perfilCompleto
            ? "Organizador registrado exitosamente"
            : "Organizador registrado exitosamente. Completa tu perfil para crear eventos",
          token = token,
          usuario = new
          {
            idUsuario = usuario.IdUsuario,
            nombre = usuario.Nombre,
            email = usuario.Email,
            tipoUsuario = usuario.TipoUsuario,
            imgAvatar = avatarUrl,
            perfilCompleto = perfilCompleto
          }
        });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al registrar organizador", error = ex.Message });
      }

    }

    /*OBTENER MI PERFIL COMPLETO (PRIVADO)
    Retorna todos los datos del usuario autenticado, incluyendo datos sensibles
    Incluye flag perfilCompleto para saber si puede inscribirse a eventos o crear eventos*/
    /*GET- Obtener perfil del usuario autenticado
    GET: api/Usuario/Perfil*/
    [HttpGet("perfil")]
    public async Task<IActionResult> ObtenerPerfil()
    {
      try
      {
        // Obtenemos el Id del usuario desde el token JWT
        var userClaim = User.FindFirst(ClaimTypes.NameIdentifier);
        if (userClaim == null)
          return Unauthorized(new { message = "No autorizado" });

        var userId = int.Parse(userClaim.Value);

        // Buscamos el usuario en la BD (Solo los activos)
        var usuario = await _usuarioRepositorio.GetByIdAsync(userId);

        if (usuario == null)
          return NotFound(new { message = "Usuario no encontrado" });

        // Obtenemos el imgAvatar
        var avatarUrl = _fileService.ObtenerUrlCompleta(usuario.ImgAvatar, Request);

        // RUNNER
        if (usuario.TipoUsuario == "runner" && usuario.PerfilRunner != null)
        {
          // Verificar si el perfil esta completo (para poder inscribirse a eventos)
          bool perfilCompleto = !string.IsNullOrEmpty(usuario.Telefono) &&
                                usuario.PerfilRunner.FechaNacimiento.HasValue &&
                                !string.IsNullOrEmpty(usuario.PerfilRunner.Genero) &&
                                usuario.PerfilRunner.Dni.HasValue &&
                                !string.IsNullOrEmpty(usuario.PerfilRunner.Localidad) &&
                                !string.IsNullOrEmpty(usuario.PerfilRunner.Agrupacion) &&
                                !string.IsNullOrEmpty(usuario.PerfilRunner.NombreContactoEmergencia) &&
                                !string.IsNullOrEmpty(usuario.PerfilRunner.TelefonoEmergencia);

          return Ok(new
          {
            idUsuario = usuario.IdUsuario,
            nombre = usuario.Nombre,
            apellido = usuario.PerfilRunner.Apellido,
            email = usuario.Email,
            telefono = usuario.Telefono,
            tipoUsuario = usuario.TipoUsuario,
            fechaNacimiento = usuario.PerfilRunner.FechaNacimiento,
            genero = usuario.PerfilRunner.Genero,
            dni = usuario.PerfilRunner.Dni,
            localidad = usuario.PerfilRunner.Localidad,
            agrupacion = usuario.PerfilRunner.Agrupacion,
            nombreContactoEmergencia = usuario.PerfilRunner.NombreContactoEmergencia,
            telefonoEmergencia = usuario.PerfilRunner.TelefonoEmergencia,
            imgAvatar = avatarUrl,
            esAvatarPorDefecto = _fileService.EsAvatarPorDefecto(usuario.ImgAvatar),
            estado = usuario.Estado,
            perfilCompleto = perfilCompleto  // ← FLAG AGREGADO
          });
        }
        // ORGANIZADOR
        else if (usuario.TipoUsuario == "organizador" && usuario.PerfilOrganizador != null)
        {
          // Verificar si el perfil esta completo (para poder crear eventos)
          bool perfilCompleto = !string.IsNullOrEmpty(usuario.Telefono) &&
                                !string.IsNullOrEmpty(usuario.PerfilOrganizador.CuitTaxId) &&
                                !string.IsNullOrEmpty(usuario.PerfilOrganizador.DireccionLegal);

          return Ok(new
          {
            idUsuario = usuario.IdUsuario,
            nombre = usuario.Nombre,
            razonSocial = usuario.PerfilOrganizador.RazonSocial,
            nombreComercial = usuario.PerfilOrganizador.NombreComercial,
            cuit = usuario.PerfilOrganizador.CuitTaxId,
            direccionLegal = usuario.PerfilOrganizador.DireccionLegal,
            email = usuario.Email,
            telefono = usuario.Telefono,
            tipoUsuario = usuario.TipoUsuario,
            imgAvatar = avatarUrl,
            esAvatarPorDefecto = _fileService.EsAvatarPorDefecto(usuario.ImgAvatar),
            estado = usuario.Estado,
            perfilCompleto = perfilCompleto  // ← FLAG AGREGADO
          });
        }

        return BadRequest(new { message = "Perfil incompleto" });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al obtener perfil", error = ex.Message });
      }
    }


    /*actualizar perfil de un usuario RUNNER - Actualiza usuarios + perfiles_runners
    Requerido todos los campos antes de inscribirse a evento
    PUT: api/Usuario/ActualizarPerfilRunner
    Content-Type: application/json*/
    [HttpPut("ActualizarPerfilRunner")]
    public async Task<IActionResult> ActualizarPerfilRunner([FromBody] ActualizarPerfilRunnerDto dto)
    {
      try
      {
        //Obtener ID del usuario autenticado desde el token JWT
        var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
        if (string.IsNullOrEmpty(userIdClaim) || !int.TryParse(userIdClaim, out int userId))
          return Unauthorized(new { message = "Token invalido" });

        //Buscar usuario con su perfil runner
        var usuario = await _usuarioRepositorio.GetByIdAsync(userId);
        if (usuario == null)
          return NotFound(new { message = "Usuario no encontrado" });

        //Verificar que sea runner
        if (usuario.TipoUsuario.ToLower() != "runner")
          return BadRequest(new { message = "Este endpoint es solo para runners" });

        //Verificar que tenga perfil runner
        if (usuario.PerfilRunner == null)
          return NotFound(new { message = "Perfil runner no encontrado" });

        //Validar edad minima (14 años)
        var edad = DateTime.Now.Year - dto.FechaNacimiento.Year;
        if (DateTime.Now < dto.FechaNacimiento.AddYears(edad))
          edad--;

        if (edad < 14)
          return BadRequest(new { message = "Debes tener al menos 14 años para registrarte" });

        //Verificar que el DNI no este en uso por otro usuario
        if (dto.Dni != usuario.PerfilRunner.Dni)
        {
          if (await _usuarioRepositorio.DniExisteAsync(dto.Dni))
            return BadRequest(new { message = "El DNI ya esta registrado por otro usuario" });
        }

        //Actualizar datos en tabla usuarios
        usuario.Nombre = dto.Nombre.Trim();
        usuario.Telefono = dto.Telefono.Trim();

        //Actualizar datos en perfiles_runners
        usuario.PerfilRunner.Nombre = dto.Nombre.Trim();
        usuario.PerfilRunner.Apellido = dto.Apellido.Trim();
        usuario.PerfilRunner.FechaNacimiento = dto.FechaNacimiento;
        usuario.PerfilRunner.Genero = dto.Genero.ToUpper();
        usuario.PerfilRunner.Dni = dto.Dni;
        usuario.PerfilRunner.Localidad = dto.Localidad.Trim();
        usuario.PerfilRunner.Agrupacion = dto.Agrupacion.Trim();
        usuario.PerfilRunner.NombreContactoEmergencia = dto.NombreContactoEmergencia.Trim();
        usuario.PerfilRunner.TelefonoEmergencia = dto.TelefonoEmergencia.Trim();

        //Guardar cambios
        await _usuarioRepositorio.UpdateAsync(usuario);

        //Retornar respuesta
        return Ok(new
        {
          message = "Perfil actualizado exitosamente. Ya puedes inscribirte a eventos",
          usuario = new
          {
            idUsuario = usuario.IdUsuario,
            nombre = usuario.Nombre,
            email = usuario.Email,
            telefono = usuario.Telefono,
            tipoUsuario = usuario.TipoUsuario,
            perfilCompleto = true
          }
        });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al actualizar perfil", error = ex.Message });
      }
    }


    /*actualizar perfil de un usuario RUNNER - Actualiza usuarios + perfiles_organizadores
    PUT: api/Usuario/ActualizarPerfilOrganizador
    Requerido todos perfil completo para poder crear eventos
    Content-Type: application/json*/
    [HttpPut("ActualizarPerfilOrganizador")]
    public async Task<IActionResult> ActualizarPerfilOrganizador([FromBody] ActualizarPerfilOrganizadorDto dto)
    {
      try
      {
        //Obtener ID del usuario autenticado desde el token JWT
        var userIdClaim = User.FindFirst(System.Security.Claims.ClaimTypes.NameIdentifier)?.Value;
        if (string.IsNullOrEmpty(userIdClaim) || !int.TryParse(userIdClaim, out int userId))
          return Unauthorized(new { message = "Token invalido" });

        //Buscar usuario con su perfil organizador
        var usuario = await _usuarioRepositorio.GetByIdAsync(userId);
        if (usuario == null)
          return NotFound(new { message = "Usuario no encontrado" });

        //Verificar que sea organizador
        if (usuario.TipoUsuario.ToLower() != "organizador")
          return BadRequest(new { message = "Este endpoint es solo para organizadores" });

        //Verificar que tenga perfil organizador
        if (usuario.PerfilOrganizador == null)
          return NotFound(new { message = "Perfil organizador no encontrado" });

        //Verificar que el CUIT no este en uso por otro usuario
        if (dto.CuitTaxId != usuario.PerfilOrganizador.CuitTaxId)
        {
          if (await _usuarioRepositorio.CuitExisteAsync(dto.CuitTaxId))
            return BadRequest(new { message = "El CUIT ya esta registrado por otro usuario" });
        }

        //Actualizar datos en tabla usuarios
        usuario.Nombre = dto.Nombre.Trim();
        usuario.Telefono = dto.Telefono.Trim();

        //Actualizar datos en perfiles_organizadores
        usuario.PerfilOrganizador.RazonSocial = dto.RazonSocial.Trim();
        usuario.PerfilOrganizador.NombreComercial = dto.NombreComercial.Trim();
        usuario.PerfilOrganizador.CuitTaxId = dto.CuitTaxId.Trim();
        usuario.PerfilOrganizador.DireccionLegal = dto.DireccionLegal.Trim();

        //Guardar cambios
        await _usuarioRepositorio.UpdateAsync(usuario);

        //Retornar respuesta
        return Ok(new
        {
          message = "Perfil actualizado exitosamente. Ya puedes crear eventos",
          usuario = new
          {
            idUsuario = usuario.IdUsuario,
            nombre = usuario.Nombre,
            email = usuario.Email,
            telefono = usuario.Telefono,
            tipoUsuario = usuario.TipoUsuario,
            perfilCompleto = true
          }
        });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al actualizar perfil", error = ex.Message });
      }
    }

    /*Gestion de contraseña - cambiar la contraseña de usuario
    PUT: api/Usuario/CambiarPassword
    Content-Type: application/json*/
    [Authorize]
    [HttpPut("CambiarPassword")]
    public async Task<IActionResult> CambiarPassword([FromBody] CambiarPasswordDto dto)
    {
      try
      {
        //Validar modelo
        if (!ModelState.IsValid)
          return BadRequest(ModelState);

        //Obtener ID del usuario desde el token
        var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier);
        if (userIdClaim == null)
          return Unauthorized(new { message = "No autorizado" });

        var userId = int.Parse(userIdClaim.Value);

        //Buscar usuario en la BD
        var usuario = await _usuarioRepositorio.GetByIdAsync(userId);

        if (usuario == null)
          return NotFound(new { message = "Usuario no encontrado" });

        //Verificar contraseña actual
        if (!_passwordService.VerifyPassword(dto.PasswordActual, usuario.PasswordHash))
          return BadRequest(new { message = "La contraseña actual es incorrecta" });

        //Actualizar contraseña
        usuario.PasswordHash = _passwordService.HashPassword(dto.NuevaPassword);
        await _usuarioRepositorio.UpdateAsync(usuario);

        return Ok(new { message = "Contraseña cambiada exitosamente" });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al cambiar contraseña", error = ex.Message });
      }
    }

    //verificar email antes de registrar
    //GET - api/usuarios/verificarEmail?email=test@test.com
    [AllowAnonymous]
    [HttpGet("verificarEmail")]
    public async Task<IActionResult> VerificarEmail([FromQuery] string email)
    {
      try
      {
        //Validar que el email no este vacio
        if (string.IsNullOrEmpty(email))
          return BadRequest(new { message = "Email requerido" });

        //Verificar si existe un usuario activo con ese email
        var existe = await _usuarioRepositorio.EmailExistenteAsync(email);

        //Renornar disponibilidad
        return Ok(new
        {
          disponible = !existe,
          message = existe ? "Email ya registrado" : "Email disponible"
        });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al verificar email", error = ex.Message });
      }
    }

    //verificar dni si esta disponible antes de registrase solo RUNNERS
    //GET - api/usuarios/verificarDni?dni=12345678
    [AllowAnonymous]
    [HttpGet("verificarDni")]
    public async Task<IActionResult> VerificarDni([FromQuery] int dni)
    {
      try
      {
        //Validar que el Dni sea valido mayor a 0
        if (dni <= 0)
          return BadRequest(new { message = "DNI invalido" });

        //Verificar si existe ese Dni en la BD
        var existe = await _usuarioRepositorio.DniExisteAsync(dni);

        //Retornamos si esta disponible
        return Ok(new
        {
          disponible = !existe,
          message = existe ? "Dni ya registrado" : "Dni disponible"
        });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al verificar DNI", error = ex.Message });
      }
    }

    /*Verificar CUIT del organizador
    GET: api/Usuario/VerificarCuit?cuit=20-1234578-9*/
    [AllowAnonymous]
    [HttpGet("VerificarCuit")]
    public async Task<IActionResult> VerificarCuit([FromQuery] string cuit)
    {
      try
      {
        if (string.IsNullOrEmpty(cuit))
          return BadRequest(new { message = "CUIT requerido" });

        var existe = await _usuarioRepositorio.CuitExisteAsync(cuit.Trim());

        return Ok(new
        {
          disponible = !existe,
          message = existe ? "CUIT ya registrado" : "CUIT disponible"
        });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al verificar CUIT", error = ex.Message });
      }
    }


    /*Ver perfil publico (por ej si un runner consulta la posicion, puede ver quienes son los otros runner)
    tambien ver el perfil de un organizador - conlleva a mostrar una vista personalizada por los datos sensibles*/
    //GET - api/usuarios/{id}
    [AllowAnonymous]
    [HttpGet("Runner/{id}")]
    public async Task<IActionResult> ObtenerPerfilRunner(int id)
    {
      try
      {
        // Buscar usuario por id (solo activos)
        var usuario = await _usuarioRepositorio.GetByIdAsync(id);

        // Si no existe o esta eliminado
        if (usuario == null)
          return NotFound(new { message = "Usuario no encontrado" });

        // Verificar que sea runner
        if (usuario.TipoUsuario.ToLower() != "runner")
          return BadRequest(new { message = "El usuario no es un runner" });

        // Verificar que tenga perfil runner cargado
        if (usuario.PerfilRunner == null)
          return NotFound(new { message = "Perfil runner no encontrado" });

        // Obtener URL completa del avatar
        var avatarUrl = _fileService.ObtenerUrlCompleta(usuario.ImgAvatar, Request);

        // Calcular edad del runner
        int? edad = null;
        if (usuario.PerfilRunner.FechaNacimiento.HasValue)
        {
          edad = DateTime.Now.Year - usuario.PerfilRunner.FechaNacimiento.Value.Year;
          if (DateTime.Now < usuario.PerfilRunner.FechaNacimiento.Value.AddYears(edad.Value))
            edad--;
        }

        // Retornar informacion publica del runner
        return Ok(new
        {
          idUsuario = usuario.IdUsuario,
          nombre = usuario.PerfilRunner.Nombre,
          apellido = usuario.PerfilRunner.Apellido,
          tipoUsuario = usuario.TipoUsuario,
          localidad = usuario.PerfilRunner.Localidad,
          agrupacion = usuario.PerfilRunner.Agrupacion,
          edad = edad,
          genero = usuario.PerfilRunner.Genero,
          imgAvatar = avatarUrl
          // Nota: No incluimos DNI, telefonos, email por privacidad
        });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al obtener perfil runner", error = ex.Message });
      }
    }

    /*Obtiene el perfil publico detallado de un Organizador 
      GET: api/Usuario/Organizador/{id}
      Incluye informacion de contacto para consultas sobre eventos*/
    [AllowAnonymous]
    [HttpGet("Organizador/{id}")]
    public async Task<IActionResult> ObtenerPerfilOrganizador(int id)
    {
      try
      {
        // Buscar usuario por id (solo activos)
        var usuario = await _usuarioRepositorio.GetByIdAsync(id);

        // Si no existe o esta eliminado
        if (usuario == null)
          return NotFound(new { message = "Usuario no encontrado" });

        // Verificar que sea organizador
        if (usuario.TipoUsuario.ToLower() != "organizador")
          return BadRequest(new { message = "El usuario no es un organizador" });

        //Obtenemos el imgAvatar
        var avatarUrl = _fileService.ObtenerUrlCompleta(usuario.ImgAvatar, Request);

        // Retornar informacion publica del organizador
        return Ok(new
        {
          idUsuario = usuario.IdUsuario,
          nombre = usuario.Nombre,
          razonSocial = usuario.PerfilOrganizador.RazonSocial,
          nombreComercial = usuario.PerfilOrganizador.NombreComercial,
          direccionLegal = usuario.PerfilOrganizador.DireccionLegal,
          tipoUsuario = usuario.TipoUsuario,
          telefono = usuario.Telefono, // Telefono publico para contacto
          email = usuario.Email, // Email publico para consultas
          imgAvatar = avatarUrl    // Nota: No incluimos campos sensibles como passwordHash, estado, etc.
        });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al obtener perfil organizador", error = ex.Message });
      }
    }

    //Eliminacion logica cambia estado
    //DELETE - /api/usuarios/perfil
    /*Quien puede eliminar usuarios?*/
    [Authorize] //Requiere estar autenticado
    [HttpDelete("Perfil")]
    public async Task<IActionResult> EliminarCuenta()
    {
      try
      {
        //Obtenemos el id del usuario desde el token
        var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier);
        if (userIdClaim == null)
          return Unauthorized(new { message = "No autorizado" });

        var userId = int.Parse(userIdClaim.Value);

        //Buscamos el usuario en la BD
        var usuario = await _usuarioRepositorio.GetByIdAsync(userId);

        if (usuario == null)
          return NotFound(new { message = "Usuario no encontrado" });

        //Eliminamos avatar si existe y no es el por defecto
        if (!string.IsNullOrEmpty(usuario.ImgAvatar))
        {
          _fileService.EliminarAvatar(usuario.ImgAvatar);
        }
      ;

        //Eliminado logico su estado pasa a falso
        await _usuarioRepositorio.DeleteLogicoAsync(usuario);

        return Ok(new { message = "Cuenta eliminada exitosamente" });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al eliminar cuenta", error = ex.Message });
      }
    }

    /*Actualiza avatar usuario autenticado
    PUT: api/Usuario/Avatar
    Content-Type: multipart/form-data*/
    [Authorize]
    [HttpPut("Avatar")]
    public async Task<IActionResult> ActualizarAvatar([FromForm] SubirAvatarDto dto)
    {
      // Validar modelo usando el DTO
      if (!ModelState.IsValid || dto.Imagen == null || dto.Imagen.Length == 0)
        return BadRequest(new { message = "Debe enviar una imagen válida" });

      //Obtener Id del usuario desde el token
      var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier);
      if (userIdClaim == null)
        return Unauthorized(new { message = "No autorizado" });

      var userId = int.Parse(userIdClaim.Value);

      try
      {
        //Buscar usuario en la BD
        var usuario = await _usuarioRepositorio.GetByIdAsync(userId);
        if (usuario == null)
          return NotFound(new { message = "Usuario no encontrado" });

        // Eliminar avatar anterior si existe y NO es por defecto
        if (!string.IsNullOrEmpty(usuario.ImgAvatar))
        {
          _fileService.EliminarAvatar(usuario.ImgAvatar);
        }

        // Guardar nuevo avatar (usando dto.Imagen)
        var urlAvatar = await _fileService.GuardarAvatarAsync(dto.Imagen, userId); // <--- USANDO dto.Imagen
        usuario.ImgAvatar = urlAvatar;
        await _usuarioRepositorio.UpdateAsync(usuario);

        //Obtener URL completa
        var urlCompleta = _fileService.ObtenerUrlCompleta(urlAvatar, Request);

        return Ok(new
        {
          message = "Avatar actualizado exitosamente",
          imgAvatar = urlCompleta
        });
      }
      catch (Exception ex)
      {
        return BadRequest(new { message = ex.Message });
      }
    }

    /*Eliminar avatar y restaurar el por defecto
    DELETE: api/Usuario/Avatar*/
    [Authorize]
    [HttpDelete("Avatar")]
    public async Task<IActionResult> EliminarAvatar()
    {
      //Obtener el Id del usuario desde el token
      var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier);
      if (userIdClaim == null)
        return Unauthorized(new { message = "No autorizado" });

      var userId = int.Parse(userIdClaim.Value);

      try
      {
        //Buscar usuario en la BD
        var usuario = await _usuarioRepositorio.GetByIdAsync(userId);
        if (usuario == null)
          return NotFound(new { message = "Usuario no encontrado" });

        //Eliminar archivo si existe y no es el por defecto
        if (!string.IsNullOrEmpty(usuario.ImgAvatar))
        {
          _fileService.EliminarAvatar(usuario.ImgAvatar);
        }

        //Restaurar avatar por defecto
        usuario.ImgAvatar = _fileService.ObtenerAvatarPorDefecto(usuario.TipoUsuario);
        await _usuarioRepositorio.UpdateAsync(usuario);

        //Obtener URL completa
        var urlCompleta = _fileService.ObtenerUrlCompleta(usuario.ImgAvatar, Request);

        return Ok(new
        {
          message = "Avatar eliminado. Se restauro el avatar por defecto",
          imgAvatar = urlCompleta
        });
      }
      catch (Exception ex)
      {
        return BadRequest(new { message = ex.Message });
      }
    }

    /*Recuperar contraseña (envia email con token)
      POST: api/Usuario/RecuperarPassword
      Genera token unico, lo guarda en la BD y envia con link de recuperacion*/
    [AllowAnonymous]
    [HttpPost("RecuperarPassword")]
    public async Task<IActionResult> RecuperarPassword([FromBody] RecuperarPasswordDto dto)
    {
      try
      {
        // Buscar usuario por email
        var usuario = await _usuarioRepositorio.GetByEmailAsync(dto.Email.Trim().ToLower());
        if (usuario == null)
          return NotFound(new { message = "No existe un usuario con ese email" });

        //Generar token unico
        var token = Guid.NewGuid().ToString("N"); // Token sin guiones

        //Crear registro de token (valido por 1 hora)
        var tokenRecuperacion = new TokenRecuperacion
        {
          IdUsuario = usuario.IdUsuario,
          Token = token,
          TipoToken="recuperacion",
          FechaCreacion = DateTime.Now,
          FechaExpiracion = DateTime.Now.AddHours(1),
          Usado = false
        };

        //Guardar token en BD
        await _tokenRecuperacionRepositorio.CrearAsync(tokenRecuperacion);

        //Enviar email
        var emailEnviado = await _emailService.EnviarEmailRecuperacionAsync(
          usuario.Email,
          usuario.Nombre,
          token
        );

        if (!emailEnviado)
        {
          // Si falla el envio, marcar token como usado para invalidarlo
          await _tokenRecuperacionRepositorio.UpdateAsync(tokenRecuperacion);
          return StatusCode(500, new { message = "Error al enviar el email de recuperación. Intenta nuevamente." });
        }
        //Retornar exito (sin exponer info sensible)
        return Ok(new
        {
          message = "Se ha enviado un email con instrucciones para recuperar tu contraseña",
          info = "Revisa tu bandeja de entrada y spam. El enlace expira en 1 hora."
        });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al procesar solicitud", error = ex.Message });
      }
    }

    [AllowAnonymous]
    [HttpPost("RestablecerPassword")]
    public async Task<IActionResult> RestablecerPassword([FromBody] RestablecerPasswordDto dto)
    {
      try
      {
        // Buscar token
        var tokenRecuperacion = await _tokenRecuperacionRepositorio.GetByTokenAsync(dto.Token);

        if (tokenRecuperacion == null)
          return BadRequest(new { message = "Token invalido" });

        //Verificar que sea un token de recuperacion (no de activacion)
        if(tokenRecuperacion.TipoToken!="recuperacion")
          return BadRequest(new {message="Este token no es valido para recuperar contraseña"});

        // Verificar que no este usado
        if (tokenRecuperacion.Usado)
          return BadRequest(new { message = "Este token ya fue utilizado" });

        // Verificar que no este expirado
        if (DateTime.Now > tokenRecuperacion.FechaExpiracion)
          return BadRequest(new { message = "El token ha expirado. Solicita uno nuevo" });

        // Buscar usuario
        var usuario = await _usuarioRepositorio.GetByIdAsync(tokenRecuperacion.IdUsuario);
        if (usuario == null)
          return NotFound(new { message = "Usuario no encontrado" });

        // Actualizar password
        usuario.PasswordHash = _passwordService.HashPassword(dto.PasswordNueva);
        await _usuarioRepositorio.UpdateAsync(usuario);

        // Marcar token como usado
        tokenRecuperacion.Usado = true;
        await _tokenRecuperacionRepositorio.UpdateAsync(tokenRecuperacion);

        // Retornar xito
        return Ok(new { message = "Contraseña restablecida exitosamente. Ya puedes iniciar sesión" });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al restablecer contraseña", error = ex.Message });
      }
    }


    /*SOLICITAR REACTIVACIÓN DE CUENTA (Envía email con token)
  POST: api/Usuario/SolicitarReactivacion
  Verifica credenciales y envía email con link de reactivación*/
    // Controllers/UsuarioController.cs - AGREGAR ESTOS DOS MÉTODOS

    [AllowAnonymous]
    [HttpPost("SolicitarReactivacion")]
    public async Task<IActionResult> SolicitarReactivacion([FromBody] SolicitarReactivacionDto dto)
    {
      try
      {
        // 1. Buscar usuario SIN filtro de estado
        var usuario = await _usuarioRepositorio.GetByEmailSinFiltroEstadoAsync(dto.Email.Trim().ToLower());

        if (usuario == null)
          return NotFound(new { message = "No existe un usuario con ese email" });

        // 2. Verificar que la cuenta este desactivada
        if (usuario.Estado)
          return BadRequest(new { message = "Tu cuenta ya esta activa. Puedes iniciar sesión normalmente" });

        // 3. Verificar password
        if (!_passwordService.VerifyPassword(dto.Password, usuario.PasswordHash))
          return Unauthorized(new { message = "Credenciales inválidas" });

        // 4. Generar token unico para reactivación
        var token = Guid.NewGuid().ToString("N");

        // 5. Crear registro de token (valido por 1 hora)
        var tokenReactivacion = new TokenRecuperacion
        {
          IdUsuario = usuario.IdUsuario,
          Token = token,
          TipoToken = "reactivacion", // ← DIFERENCIADOR
          FechaCreacion = DateTime.Now,
          FechaExpiracion = DateTime.Now.AddHours(1),
          Usado = false
        };

        // 6. Guardar token en BD
        await _tokenRecuperacionRepositorio.CrearAsync(tokenReactivacion);

        // 7. Enviar email con link de reactivacion
        var emailEnviado = await _emailService.EnviarEmailReactivacionAsync(
          usuario.Email,
          usuario.Nombre,
          token
        );

        if (!emailEnviado)
        {
          // Si falla el envío, marcar token como usado para invalidarlo
          tokenReactivacion.Usado = true;
          await _tokenRecuperacionRepositorio.UpdateAsync(tokenReactivacion);
          return StatusCode(500, new { message = "Error al enviar el email. Intenta nuevamente" });
        }

        // 8. Retornar exito
        return Ok(new
        {
          message = "Se ha enviado un email con instrucciones para reactivar tu cuenta",
          info = "Revisa tu bandeja de entrada y spam. El enlace expira en 1 hora"
        });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al procesar solicitud", error = ex.Message });
      }
    }

    /*CONFIRMAR REACTIVACIÓN DE CUENTA (Usa token del email)
    POST: api/Usuario/ReactivarCuenta
    Valida token y reactiva la cuenta*/
    [AllowAnonymous]
    [HttpPost("ReactivarCuenta")]
    public async Task<IActionResult> ReactivarCuenta([FromBody] ReactivarCuentaDto dto)
    {
      try
      {
        // 1. Buscar token de reactivación
        var tokenReactivacion = await _tokenRecuperacionRepositorio.GetByTokenAsync(dto.Token);

        if (tokenReactivacion == null)
          return BadRequest(new { message = "Token inválido" });

        // 2. Verificar que sea un token de reactivación
        if (tokenReactivacion.TipoToken != "reactivacion")
          return BadRequest(new { message = "Este token no es válido para reactivación" });

        // 3. Verificar que no esté usado
        if (tokenReactivacion.Usado)
          return BadRequest(new { message = "Este token ya fue utilizado" });

        // 4. Verificar que no esté expirado
        if (DateTime.Now > tokenReactivacion.FechaExpiracion)
          return BadRequest(new { message = "El token ha expirado. Solicita uno nuevo" });

        // 5. Buscar usuario SIN filtro de estado
         var usuario = await _usuarioRepositorio.GetByIdSinFiltroEstadoAsync(tokenReactivacion.IdUsuario);

        if (usuario == null)
          return NotFound(new { message = "Usuario no encontrado" });

        // 6. Verificar que la cuenta esté desactivada
        if (usuario.Estado)
          return BadRequest(new { message = "La cuenta ya está activa" });

        // 7. REACTIVAR CUENTA
        usuario.Estado = true;
        await _usuarioRepositorio.UpdateAsync(usuario);

        // 8. Marcar token como usado
        tokenReactivacion.Usado = true;
        await _tokenRecuperacionRepositorio.UpdateAsync(tokenReactivacion);

        // 9. Generar token JWT para login automático
        var jwtToken = _jwtService.GenerarToken(usuario);
        var avatarUrl = _fileService.ObtenerUrlCompleta(usuario.ImgAvatar, Request);

        // 10. Retornar éxito con token para login automático
        return Ok(new
        {
          message = "¡Cuenta reactivada exitosamente! Bienvenido de nuevo",
          token = jwtToken,
          usuario = new
          {
            idUsuario = usuario.IdUsuario,
            nombre = usuario.Nombre,
            email = usuario.Email,
            tipoUsuario = usuario.TipoUsuario,
            imgAvatar = avatarUrl
          }
        });
      }
      catch (Exception ex)
      {
        return StatusCode(500, new { message = "Error al reactivar cuenta", error = ex.Message });
      }
    }




  }
}