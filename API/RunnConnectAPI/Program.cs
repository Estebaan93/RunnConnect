//Program.cs
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using System.Text;
using RunnConnectAPI.Data;
using RunnConnectAPI.Services;
using RunnConnectAPI.Repositories;

var builder = WebApplication.CreateBuilder(args);
//para que siempre escuche en todas las redes
//builder.WebHost.UseUrls("http://0.0.0.0:5213");


var config = builder.Configuration; //Obtenemos la confi para usarla

//Habilitar controllers
builder.Services.AddControllers();


// Habilitar Swagger
// Learn more about configuring Swagger/OpenAPI at https://aka.ms/aspnetcore/swashbuckle
builder.Services.AddEndpointsApiExplorer();
//builder.Services.AddSwaggerGen();

// Configuración de Swagger con soporte para JWT
builder.Services.AddSwaggerGen(options =>
{
  options.SwaggerDoc("v1", new Microsoft.OpenApi.Models.OpenApiInfo
  {
    Title = "RunnConnectAPI",
    Version = "v1"
  });

  // 1. Definir el esquema de seguridad (Bearer JWT)
  options.AddSecurityDefinition("Bearer", new Microsoft.OpenApi.Models.OpenApiSecurityScheme
  {
    In = Microsoft.OpenApi.Models.ParameterLocation.Header,
    Description = "Ingrese 'Bearer' [espacio] y su token.\n\nEjemplo: \"Bearer eyJhbGciOiJIUzI1NiIsInR5c...\"",
    Name = "Authorization",
    Type = Microsoft.OpenApi.Models.SecuritySchemeType.ApiKey,
    Scheme = "Bearer"
  });

  // 2. Requisito de seguridad global
  options.AddSecurityRequirement(new Microsoft.OpenApi.Models.OpenApiSecurityRequirement
    {
        {
            new Microsoft.OpenApi.Models.OpenApiSecurityScheme
            {
                Reference = new Microsoft.OpenApi.Models.OpenApiReference
                {
                    Type = Microsoft.OpenApi.Models.ReferenceType.SecurityScheme,
                    Id = "Bearer"
                }
            },
            new string[]{}
        }
    });
});

//CONEXION A MySQL
builder.Services.AddDbContext<RunnersContext>(options =>
    options.UseMySql(
        builder.Configuration.GetConnectionString("MySql"),
        ServerVersion.AutoDetect(builder.Configuration.GetConnectionString("MySql"))
    )
);


//Registrar JWTService para inyeccion de dependencias
builder.Services.AddScoped<JWTService>();
builder.Services.AddScoped<PasswordService>();
builder.Services.AddScoped<FileService>();
builder.Services.AddScoped<EmailService>();


//Repos
builder.Services.AddScoped<UsuarioRepositorio>();
builder.Services.AddScoped<EventoRepositorio>();
builder.Services.AddScoped<TokenRecuperacionRepositorio>();
builder.Services.AddScoped<CategoriaRepositorio>();
builder.Services.AddScoped<InscripcionRepositorio>();
builder.Services.AddScoped<ResultadoRepositorio>();
builder.Services.AddScoped<NotificacionRepositorio>();
builder.Services.AddScoped<RutaRepositorio>();



//CORS (Para que la app se pueda conectar)
builder.Services.AddCors(options =>
{
  options.AddPolicy("AllowAllPolicy",
    policy =>
    {
      policy.AllowAnyOrigin() //Cualquier origen para el desarrollo
            .AllowAnyMethod() //Permite cualquier metodo HTTP (GET, POST, PUT etc)
            .AllowAnyHeader(); //PErmite cualquier cabecera
    });
});


// Configurar Autenticacion JWT
builder.Services.AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
    .AddJwtBearer(options =>
    {
      options.TokenValidationParameters = new TokenValidationParameters
      {
        // Que validar:
        ValidateIssuer = true,
        ValidateAudience = true,
        ValidateLifetime = true,
        ValidateIssuerSigningKey = true,

        // Valores validos (leidos desde appsettings.json):
        ValidIssuer = config["Jwt:Issuer"],
        ValidAudience = config["Jwt:Audience"],
        IssuerSigningKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(config["Jwt:Key"]))
      };
    });

//Configurar autorizacion
builder.Services.AddAuthorization();


//Construccion de la app y config del Pipeline HTTP

var app = builder.Build();

//Habilitar archivos estaticos


// Configure the HTTP request pipeline (El orden importa).
if (app.Environment.IsDevelopment())
{
  app.UseSwagger();
  app.UseSwaggerUI();
}

//app.UseHttpsRedirection();
app.UseStaticFiles();

//Aplicar politicas de CORS
app.UseCors("AllowAllPolicy");

//Aplicar Autenticacion (Antes de Autorizacion)
app.UseAuthentication();

//Aplicar Autorizacion
app.UseAuthorization();

//Mapear los Controllers 
app.MapControllers();

app.Run();

//Escuchar en todas las interfaces
//app.Urls.Add("http://0.0.0.0:5213");

