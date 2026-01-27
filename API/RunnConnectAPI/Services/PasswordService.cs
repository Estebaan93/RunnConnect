//Services/PasswordService

namespace RunnConnectAPI.Services
{
  /*Para hashear unas contraseña*/
  public class PasswordService
  {
    //Hash en de texto plano
    public string HashPassword(string password)
    {
      return BCrypt.Net.BCrypt.HashPassword(password, workFactor:12);
    }

    //Para verificar si la contraseñas coinciden, se usa en el login
    public bool VerifyPassword(string password, string HashPassword)
    {
      try{
      /*Extraemos el salt del pass con hash
      hasheamos el texto plano
      comparams con el hash de la BD*/
      return BCrypt.Net.BCrypt.Verify(password, HashPassword);
    }
      catch
      {
        /*Si el hash es corrupto o tiene un formato invalido
        capturamos y enviamos la exception*/
        return false;
      }

    }


  }


}