//Models/Dto/Categoria/CategoriaEventoResponse.cs
namespace RunnConnectAPI.Models.Dto.Categoria
{

  /// DTO de respuesta con información de una categoría

  public class CategoriaEventoResponse
  {
    public int IdCategoria { get; set; }
    public int IdEvento { get; set; }
    public string Nombre { get; set; } = string.Empty;
    public decimal CostoInscripcion { get; set; }
    public int? CupoCategoria { get; set; }
    public int EdadMinima { get; set; }
    public int EdadMaxima { get; set; }
    public string Genero { get; set; } = "X";
    
    
    /// Descripción legible del género
  
    public string GeneroDescripcion => Genero switch
    {
      "F" => "Femenino",
      "M" => "Masculino",
      "X" => "Mixto",
      _ => "Sin especificar"
    };

   
    /// Cantidad de inscriptos en esta categoría
  
    public int InscriptosActuales { get; set; }


    /// Cupos disponibles (null si no hay límite)

    public int? CuposDisponibles => CupoCategoria.HasValue
      ? CupoCategoria.Value - InscriptosActuales
      : null;

  
    /// Indica si la categoría tiene cupos disponibles
 
    public bool TieneCupo => !CupoCategoria.HasValue || CuposDisponibles > 0;
  }
}