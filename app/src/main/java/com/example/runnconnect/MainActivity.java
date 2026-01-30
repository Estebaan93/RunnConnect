//com.example.runnconnect/MainActivity
package com.example.runnconnect;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.runnconnect.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

  private AppBarConfiguration mAppBarConfiguration;
  private ActivityMainBinding binding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    setSupportActionBar(binding.appBarMain.toolbar);
    /*binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show();
      }
    });*/
    DrawerLayout drawer = binding.drawerLayout;
    NavigationView navigationView = binding.navView;
    // Passing each menu ID as a set of Ids because each
    // menu should be considered as top level destinations.

    /*segun rol de usuario, leemos desde sharedpreferencias*/
    SharedPreferences sp= getSharedPreferences("session_sp", 0); //El nombre debe coincidir con el sessionManager
    String tipoUsuario= sp.getString("tipoUsuario", "runner"); //por defecto es el reunner
    String nombreUsuario= sp.getString("nombre", "Usuario");
    String emailUsuario = sp.getString("email", "correo@ejemplo.com");
    String avatarUrl = sp.getString("imgAvatar", "");

    Log.d("LOGIN", "TIPO USUARIO: "+tipoUsuario + ", Nombre: " + nombreUsuario + ", Email: "+emailUsuario);

    // 2. Obtener la vista del Header para editar sus componentes
    // El Header es el índice 0 de los headers del NavigationView
    View headerView = navigationView.getHeaderView(0);

    // 3. Buscar los componentes dentro del Header
    TextView tvNombre = headerView.findViewById(R.id.tvNavNombre);
    TextView tvEmail = headerView.findViewById(R.id.tvNavEmail);
    ImageView ivAvatar = headerView.findViewById(R.id.ivNavAvatar);

    // 4. Asignar textos
    tvNombre.setText(nombreUsuario);
    tvEmail.setText(emailUsuario);

    // 5. Cargar imagen con Glide
    if (avatarUrl != null && !avatarUrl.isEmpty()) {
      // Corrección para emulador (si viene localhost)
      if (avatarUrl.contains("localhost")) {
        avatarUrl = avatarUrl.replace("localhost", "10.0.2.2");
      }

      Glide.with(this)
        .load(avatarUrl)
        .placeholder(R.mipmap.ic_launcher_round) // Imagen mientras carga
        .error(R.mipmap.ic_launcher_round)       // Imagen si falla
        .circleCrop()                            // Recorte circular
        .into(ivAvatar);
    }

    // ---------------------------------------------------

    //limpiamos el menu que viene por defecto en el xml
    navigationView.getMenu().clear();

    //segun rol inflamos
    if("organizador".equalsIgnoreCase(tipoUsuario)){
      navigationView.inflateMenu(R.menu.menu_organizador);
    }else{
      //activity_main_drawer pertence al runner
      navigationView.inflateMenu(R.menu.activity_main_drawer);
    }
    //navegacion
    mAppBarConfiguration = new AppBarConfiguration.Builder(
            R.id.nav_inicio, //runner/orga
            R.id.nav_buscar, //runner
            R.id.nav_inscripciones, //runner
            R.id.nav_mis_eventos, //organizador
            R.id.nav_crear_evento, //organizador
            R.id.nav_perfil, //runner
            R.id.nav_perfil_organizador, //organizador
            R.id.nav_buscar_inscripciones, //organizador
            R.id.nav_cerrar_sesion) //runner/orga
            .setOpenableLayout(drawer)
            .build();
    NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
    NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
    NavigationUI.setupWithNavController(navigationView, navController);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    //getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onSupportNavigateUp() {
    NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
    return NavigationUI.navigateUp(navController, mAppBarConfiguration)
            || super.onSupportNavigateUp();
  }
}