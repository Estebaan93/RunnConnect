//Repositories/UsuarioRepositorio
using Microsoft.EntityFrameworkCore; //Para las consultas y operaciones con la BD
using RunnConnectAPI.Data;          // DbContext del proyecto
using RunnConnectAPI.Models;        //Entidades

namespace RunnConnectAPI.Repositories
{
  //Encapsular la logica de acceso a datos
  public class UsuarioRepositorio
  {
    private readonly RunnersContext _context; //campo privado para el dbContext

    public UsuarioRepositorio(RunnersContext context) //Constructor e inyectamos el DbContext por dependencias
    {
      _context = context;
    }
    /*Consultas para usuarios*/
    //Obtenemos usuario por ID(clave primaria)
    public async Task<Usuario?> GetByIdAsync(int id)
    {
      return await _context.Usuarios //Busca por PK y que esten activos
          .Include(u => u.PerfilRunner)
          .Include(u => u.PerfilOrganizador)
          .FirstOrDefaultAsync(u => u.IdUsuario == id && u.Estado);
    }

    //Obtener por email y que esten activos
    public async Task<Usuario?> GetByEmailAsync(string email)
    {
      return await _context.Usuarios //Devuelve el primero o null 
          .Include(u => u.PerfilRunner)
          .Include(u => u.PerfilOrganizador)
          .FirstOrDefaultAsync(u => u.Email == email && u.Estado);
    }

    //Verificar si existe un email (solo activos runner/organizadores)
    public async Task<bool> EmailExistenteAsync(string email)
    {
      return await _context.Usuarios.AnyAsync(u => u.Email == email && u.Estado); //True si existe
    }

    //Verificar si existe un DNI (solo activos y runner)
    public async Task<bool> DniExisteAsync(int dni)
    {
      return await _context.PerfilesRunners.AnyAsync(u => u.Dni == dni); //True si existe
    }

    /*Verficar si existe un CUIT (solo organizadores)*/
    public async Task<bool> CuitExisteAsync(string cuit)
    {
      return await _context.PerfilesOrganizadores.AnyAsync(p => p.CuitTaxId == cuit);
    }

    /*Operacione CRUD*/
    //Crear nuevo usuario por defecto true
    public async Task<Usuario> CreateAsync(Usuario usuario)
    {
      _context.Usuarios.Add(usuario);       //Marca la entidad como nueva
      await _context.SaveChangesAsync();    //Persite en la BD
      return usuario;                       //Devuelve el obj creado
    }

    /*Crear perfil runner*/
    public async Task<PerfilRunner> CreatePerfilRunnerAsync(PerfilRunner perfil)
    {
      _context.PerfilesRunners.Add(perfil);
      await _context.SaveChangesAsync();
      return perfil;
    }

    /*Crear perfil organizador*/
    public async Task<PerfilOrganizador> CreatePerfilOrganizadorAsync(PerfilOrganizador perfil)
    {
      _context.PerfilesOrganizadores.Add(perfil);
      await _context.SaveChangesAsync();
      return perfil;
    }

    //Actualizar usuario
    public async Task UpdateAsync(Usuario usuario)
    {
      _context.Usuarios.Update(usuario);  //Marca la entidad como modificacda
      await _context.SaveChangesAsync();  //Persiste cambios 
    }

    /*Actualizar perfil runner*/
    public async Task UpdatePerfilRunnerAsync(PerfilRunner perfil)
    {
      _context.PerfilesRunners.Update(perfil);
      await _context.SaveChangesAsync();
    }

    /*Actualizar perfil organizador*/
    public async Task UpdatePerfilOrganizadorAsync(PerfilOrganizador perfil)
    {
      _context.PerfilesOrganizadores.Update(perfil);
      await _context.SaveChangesAsync();
    }

    //Eliminar persistente
    /*public async Task DeleteAsync(Usuario usuario)
    {
      _context.Usuarios.Remove(usuario);    // Marca la entidad como eliminada
      await _context.SaveChangesAsync();    // Persiste eliminacion
    }*/

    //Eliminado logico
    public async Task DeleteLogicoAsync(Usuario usuario)
    {
      usuario.Estado = false; //Pasa el estado a desactivado
      _context.Usuarios.Update(usuario);
      await _context.SaveChangesAsync();
    }

    //Verificar si existe un usuario (solo activo)
    public async Task<bool> ExisteAsync(int id)
    {
      return await _context.Usuarios.AnyAsync(u => u.IdUsuario == id && u.Estado); //True si existe
    }

    //Obtener usuario sin filtro de estado (reactivar y recuperar )
    public async Task<Usuario?> GetByEmailSinFiltroEstadoAsync(string email)
    {
      return await _context.Usuarios
          .Include(u => u.PerfilRunner)
          .Include(u => u.PerfilOrganizador)
          .FirstOrDefaultAsync(u => u.Email == email);

    }

    // Obtener usuario SIN filtro de estado por ID (para reactivacion)
    public async Task<Usuario?> GetByIdSinFiltroEstadoAsync(int id)
    {
      return await _context.Usuarios
          .Include(u => u.PerfilRunner)
          .Include(u => u.PerfilOrganizador)
          .FirstOrDefaultAsync(u => u.IdUsuario == id);
      // Sin && u.Estado para poder encontrar cuentas desactivadas
    }

  }
}