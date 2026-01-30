using Microsoft.EntityFrameworkCore;
using RunnConnectAPI.Models;

namespace RunnConnectAPI.Data
{
  public class RunnersContext : DbContext
  {
    public RunnersContext(DbContextOptions<RunnersContext> options) : base(options) { }

    //Tabla Usuarios
    public DbSet<Usuario> Usuarios { get; set; }
    public DbSet<PerfilRunner> PerfilesRunners { get; set; }
    public DbSet<PerfilOrganizador> PerfilesOrganizadores { get; set; }

    //Tabla eventos
    public DbSet<Evento> Eventos { get; set; }
    public DbSet<CategoriaEvento> CategoriasEvento { get; set; }
    public DbSet<Inscripcion> Inscripciones { get; set; }
    public DbSet<Resultado> Resultados { get; set; }
    public DbSet<Ruta> Rutas { get; set; }
    public DbSet<PuntoInteres> PuntosInteres { get; set; }
    public DbSet<NotificacionEvento> NotificacionesEvento { get; set; }

    //Recuperar cuenta
    public DbSet<TokenRecuperacion> TokenRecuperacion {get;set;}

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
      base.OnModelCreating(modelBuilder);

      // --- Mapeo de nombres de tablas (Solución Case-Sensitive para Linux) ---
    
    // Usuarios y Perfiles
    modelBuilder.Entity<Usuario>().ToTable("usuarios");
    modelBuilder.Entity<PerfilRunner>().ToTable("perfiles_runners");
    modelBuilder.Entity<PerfilOrganizador>().ToTable("perfiles_organizadores");

    // Eventos y Logística
    modelBuilder.Entity<Evento>().ToTable("eventos");
    modelBuilder.Entity<CategoriaEvento>().ToTable("categorias_evento");
    modelBuilder.Entity<Inscripcion>().ToTable("inscripciones");
    modelBuilder.Entity<Resultado>().ToTable("resultados");
    modelBuilder.Entity<Ruta>().ToTable("rutas");
    modelBuilder.Entity<PuntoInteres>().ToTable("puntosinteres");
    modelBuilder.Entity<NotificacionEvento>().ToTable("notificaciones_evento");

    // Seguridad
    modelBuilder.Entity<TokenRecuperacion>().ToTable("tokens_recuperacion");

      // Configurar relacion 1:1 Usuario - PerfilRunner
      modelBuilder.Entity<Usuario>()
          .HasOne(u => u.PerfilRunner)
          .WithOne(p => p.Usuario)
          .HasForeignKey<PerfilRunner>(p => p.IdUsuario)
          .OnDelete(DeleteBehavior.Cascade);

      // Configurar relacion 1:1 Usuario - PerfilOrganizador
      modelBuilder.Entity<Usuario>()
          .HasOne(u => u.PerfilOrganizador)
          .WithOne(p => p.Usuario)
          .HasForeignKey<PerfilOrganizador>(p => p.IdUsuario)
          .OnDelete(DeleteBehavior.Cascade);

      // Indices unicos
      modelBuilder.Entity<PerfilRunner>()
          .HasIndex(p => p.Dni)
          .IsUnique();

      modelBuilder.Entity<PerfilOrganizador>()
          .HasIndex(p => p.RazonSocial)
          .IsUnique();

      modelBuilder.Entity<PerfilOrganizador>()
          .HasIndex(p => p.CuitTaxId)
          .IsUnique();
    }

  }
}