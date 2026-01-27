// Repositories/TokenRecuperacionRepositorio.cs
using Microsoft.EntityFrameworkCore;
using RunnConnectAPI.Data;
using RunnConnectAPI.Models;

namespace RunnConnectAPI.Repositories
{
  public class TokenRecuperacionRepositorio
  {
    private readonly RunnersContext _context;

    public TokenRecuperacionRepositorio(RunnersContext context)
    {
      _context = context;
    }

    public async Task<TokenRecuperacion> CrearAsync(TokenRecuperacion token)
    {
      _context.TokenRecuperacion.Add(token);
      await _context.SaveChangesAsync();
      return token;
    }

    public async Task<TokenRecuperacion?> GetByTokenAsync(string token)
    {
      return await _context.TokenRecuperacion
        .FirstOrDefaultAsync(t => t.Token == token);
    }

    public async Task UpdateAsync(TokenRecuperacion token)
    {
      _context.TokenRecuperacion.Update(token);
      await _context.SaveChangesAsync();
    }

    // Opcional: Limpiar tokens expirados (ejecutar periodicamente)
    public async Task EliminarTokensExpiradosAsync()
    {
      var tokensExpirados = await _context.TokenRecuperacion
        .Where(t => t.FechaExpiracion < DateTime.Now || t.Usado)
        .ToListAsync();

      _context.TokenRecuperacion.RemoveRange(tokensExpirados);
      await _context.SaveChangesAsync();
    }
  }
}