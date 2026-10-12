package com.example.runnconnect;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.example.runnconnect.databinding.ActionLayoutNotificacionBellBinding;
import com.example.runnconnect.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

  private AppBarConfiguration mAppBarConfiguration;
  private ActivityMainBinding binding;
  private MainViewModel viewModel;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    setSupportActionBar(binding.appBarMain.toolbar);

    DrawerLayout drawer = binding.drawerLayout;
    NavigationView navigationView = binding.navView;

    // 1. Instanciamos el ViewModel
    viewModel = new ViewModelProvider(this).get(MainViewModel.class);

    // 2. Obtenemos referencias del Header del Navigation Drawer
    View headerView = navigationView.getHeaderView(0);
    TextView tvNombre = headerView.findViewById(R.id.tvNavNombre);
    TextView tvEmail = headerView.findViewById(R.id.tvNavEmail);
    ImageView ivAvatar = headerView.findViewById(R.id.ivNavAvatar);

    // 3. Suscripción a los observadores (MVVM Puro)
    viewModel.getNombreUsuario().observe(this, tvNombre::setText);
    viewModel.getEmailUsuario().observe(this, tvEmail::setText);

    viewModel.getAvatarUrl().observe(this, url -> {
        Glide.with(this)
          .load(url)
          .placeholder(R.mipmap.ic_launcher_round)
          .error(R.mipmap.ic_launcher_round)
          .circleCrop()
          .into(ivAvatar);
    });

    viewModel.getMenuResource().observe(this, menuResId -> {
      //if (menuResId != null) {
        navigationView.getMenu().clear();
        navigationView.inflateMenu(menuResId);
      //}
    });

    // 4. Configuración del NavController
    mAppBarConfiguration = new AppBarConfiguration.Builder(
      R.id.nav_inicio,
      R.id.nav_buscar,
      R.id.nav_inscripciones,
      R.id.nav_resultados,
      R.id.nav_mis_eventos,
      R.id.nav_crear_evento,
      R.id.nav_perfil,
      R.id.nav_perfil_organizador,
      R.id.nav_buscar_inscripciones,
      R.id.nav_cerrar_sesion)
      .setOpenableLayout(drawer)
      .build();

    NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
    NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
    NavigationUI.setupWithNavController(navigationView, navController);

    navController.addOnDestinationChangedListener((controller, destination, arguments) ->
        viewModel.onDestinoNavegado(destination.getId())
    );
  }

  @Override
  protected void onResume() {
    super.onResume();
    viewModel.cargarContadorNotificaciones();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);
    MenuItem notifItem = menu.findItem(R.id.nav_notificaciones);
    if (notifItem != null && notifItem.getActionView() != null) {
      ActionLayoutNotificacionBellBinding badgeBinding = ActionLayoutNotificacionBellBinding.bind(notifItem.getActionView());
      badgeBinding.getRoot().setOnClickListener(v -> {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        navController.navigate(R.id.nav_notificaciones);
      });
      viewModel.getContadorTexto().observe(this, badgeBinding.tvBadgeContador::setText);
      viewModel.getContadorVisibility().observe(this, badgeBinding.tvBadgeContador::setVisibility);
    }
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
    return NavigationUI.onNavDestinationSelected(item, navController)
        || super.onOptionsItemSelected(item);
  }

  @Override
  public boolean onSupportNavigateUp() {
    NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
    return NavigationUI.navigateUp(navController, mAppBarConfiguration)
      || super.onSupportNavigateUp();
  }
}