//Models/Dto/Categoria/CrearCategoriaRequest.cs
using System.ComponentModel.DataAnnotations;

namespace RunnConnectAPI.Models.Dto.Categoria
{

  /// DTO para crear una categoría de evento
  /// POST: api/Evento/{idEvento}/Categorias

  public class CrearCategoriaRequest
  {
    [Required(ErrorMessage = "El nombre de la categoría es obligatorio")]
    [StringLength(100, MinimumLength = 2, ErrorMessage = "El nombre debe tener entre 2 y 100 caracteres")]
    public string Nombre { get; set; } = string.Empty;

    [Range(0, 1000000, ErrorMessage = "El costo debe ser un valor positivo")]
    public decimal CostoInscripcion { get; set; } = 0;

    [Range(1, 10000, ErrorMessage = "El cupo debe estar entre 1 y 10.000")]
    public int? CupoCategoria { get; set; }

    [Range(0, 120, ErrorMessage = "La edad mínima debe estar entre 0 y 120")]
    public int EdadMinima { get; set; } = 0;

    [Range(0, 120, ErrorMessage = "La edad máxima debe estar entre 0 y 120")]
    public int EdadMaxima { get; set; } = 99;


    /// Género permitido: F (Femenino), M (Masculino), X (Mixto/Todos)

    [Required(ErrorMessage = "El género es obligatorio")]
    [RegularExpression("^[FMX]$", ErrorMessage = "El género debe ser F, M o X")]
    public string Genero { get; set; } = "X";
  }
}
