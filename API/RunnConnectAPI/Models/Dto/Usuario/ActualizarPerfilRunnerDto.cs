// Models/Dto/Usuario/ActualizarPerfilRunnerDto.cs
using System.ComponentModel.DataAnnotations;

namespace RunnConnectAPI.Models.Dto.Usuario
{
  /*DTO para actualizar perfil de Runner - actualiza campos en usuarios + perfiles_runners
   El avatar se actualiza con el endpoint separao PUT /api/Usuarios/Avatar
   Todos los campos son requeridos para poder inscribirse a eventos*/
 
  public class ActualizarPerfilRunnerDto
  {
    /*Campos tabla usuarios*/
    [Required(ErrorMessage = "El nombre es obligatorio")]
    [StringLength(100, MinimumLength =3,ErrorMessage = "El nombre debe tener mas de 3 caracteres")]
    public string Nombre { get; set; } = string.Empty;

    [Required(ErrorMessage = "El telefono es obligatorio")]
    [StringLength(20, MinimumLength = 7, ErrorMessage = "El telefono debe tener entre 7 y 20 digitos")]
    public string Telefono { get; set; }= string.Empty;


    /*Campos tabla perfeiles_runners*/
    [Required(ErrorMessage ="El apellido es obligatorio")]
    [StringLength(100, MinimumLength =3,ErrorMessage = "El apellido debe tener mas de 3 caracteres")]
    public string Apellido { get; set; }=string.Empty;
    
    [Required(ErrorMessage ="La fecha de nacimiento es obligatoria")]
    public DateTime FechaNacimiento { get; set; }= DateTime.Now;

    [Required(ErrorMessage ="El genero debe ser obligatorio")]
    [StringLength(1, ErrorMessage = "El genero no puede exceder 1 caracter")]
    [RegularExpression(@"^[FMX]$", ErrorMessage = "El género debe ser F, M o X")]
    public string Genero { get; set; }= "X";

    [Required(ErrorMessage = "El DNI es obligatorio")]
    [Range(1000000, 99999999, ErrorMessage = "El DNI debe tener entre 7 y 8 digitos")]
    public int Dni { get; set; }
    
    [Required(ErrorMessage ="La localidad es obligatoria")]
    [StringLength(100, ErrorMessage = "La localidad no puede exceder 100 caracteres")]
    public string Localidad { get; set; }= string.Empty;

    [Required(ErrorMessage ="La agrupacion es obligatoria, si no tiene colocar 'libre' ")]
    [StringLength(100, ErrorMessage = "La agrupacion no puede exceder 100 caracteres")]
    public string Agrupacion { get; set; }=string.Empty;

    [Required(ErrorMessage ="Nombre/Relacion del contacto de emergencia es requerido")]
    [StringLength(50, MinimumLength =3, ErrorMessage ="El nomnre y/o relacion con el contacto de emergencia debe tener mas de 3 caracteres")]
    public string NombreContactoEmergencia {get;set;}= string.Empty;

    [Required(ErrorMessage ="Debe asignar un nuemro de emergencia")]
    [StringLength(50 ,ErrorMessage = "El telefono de emergencia no puede exceder 50 caracteres")]
    [RegularExpression(@"^\d{6,15}$", ErrorMessage = "El telefono de emergencia debe contener solo numeros entre 6 y 15 digitos")]
    public string TelefonoEmergencia { get; set; }=string.Empty;

  }
}