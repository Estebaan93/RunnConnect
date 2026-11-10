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

  }
}