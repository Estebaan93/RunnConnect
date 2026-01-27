//Models/Resultado.cs
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace RunnConnectAPI.Models
{

  /// Resultados de una carrera - Se crea DESPUES del evento
  /// Relación 1:1 con Inscripcion (UNIQUE en idInscripcion)
  [Table("resultados")]
  public class Resultado
  {
    [Key]
    [Column("idResultado")]
    public int IdResultado { get; set; }

    [Required]
    [Column("idInscripcion")]
    public int IdInscripcion { get; set; }

   
    // CAMPOS CARGADOS POR EL ORGANIZADOR (post-evento)

    /// Tiempo oficial de la carrera. Ej: "00:45:30.123"
    /// Nullable porque se carga después del evento
    [Column("tiempoOficial")]
    [StringLength(20)]
    public string? TiempoOficial { get; set; }

    /// Posicion general en toda la carrera
    [Column("posicionGeneral")]
    public int? PosicionGeneral { get; set; }

    /// Posicion dentro de su categoria
    [Column("posicionCategoria")]
    public int? PosicionCategoria { get; set; }


    // CAMPOS CARGADOS POR EL RUNNER (datos de smartwatch/Google Fit)

    /// Tiempo registrado por smartwatch. Ej: "00:45:28"
    [Column("tiempoSmartwatch")]
    [StringLength(20)]
    public string? TiempoSmartwatch { get; set; }

    /// Distancia recorrida en kilometros. Ej: 10.02
    [Column("distanciaKm", TypeName = "decimal(6,2)")]
    public decimal? DistanciaKm { get; set; }


    /// Ritmo promedio. Ej: "4:32 min/km"
    [Column("ritmoPromedio")]
    [StringLength(20)]
    public string? RitmoPromedio { get; set; }


    /// Velocidad promedio. Ej: "13.2 km/h"
    [Column("velocidadPromedio")]
    [StringLength(20)]
    public string? VelocidadPromedio { get; set; }

    /// Calorías quemadas durante la carrera
    [Column("caloriasQuemadas")]
    public int? CaloriasQuemadas { get; set; }

    /// Pulsaciones promedio durante la carrera
    [Column("pulsacionesPromedio")]
    public int? PulsacionesPromedio { get; set; }

    /// Pulsaciones maximas durante la carrera
    [Column("pulsacionesMax")]
    public int? PulsacionesMax { get; set; }


    // NAVEGACION
    [ForeignKey("IdInscripcion")]
    public virtual Inscripcion? Inscripcion { get; set; }
  }




  
}