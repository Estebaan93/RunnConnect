//ui/runner/PerfilRunnerViewModel
package com.example.runnconnect.ui.runner.perfil;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.runnconnect.data.repositorio.UsuarioRepositorio;
import com.example.runnconnect.data.request.ActualizarPerfilRunnerRequest;
import com.example.runnconnect.data.response.PerfilUsuarioResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilRunnerViewModel extends AndroidViewModel {
  // TODO: Implement the ViewModel
  private final UsuarioRepositorio repo;

  // --- ESTADOS DE UI (LiveData) ---
  private final MutableLiveData<PerfilUsuarioResponse> perfilData = new MutableLiveData<>();
  private final MutableLiveData<Boolean> isEditable = new MutableLiveData<>(false);
  private final MutableLiveData<String> btnText = new MutableLiveData<>("Editar");
  private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
  private final MutableLiveData<String> mensajeToast = new MutableLiveData<>();

  //url del avatar
  private final MutableLiveData<String> avatarUrl=new MutableLiveData<>();
  public PerfilRunnerViewModel(@NonNull Application application) {
    super(application);
    repo = new UsuarioRepositorio(application);
  }

  // --- GETTERS (Para que el Fragment observe) ---
  public LiveData<PerfilUsuarioResponse> getPerfilData() { return perfilData; }
  public LiveData<Boolean> getIsEditable() { return isEditable; }
  public LiveData<String> getBtnText() { return btnText; }
  public LiveData<Boolean> getIsLoading() { return isLoading; }
  public LiveData<String> getMensajeToast() { return mensajeToast; }
  public LiveData<String> getAvatarUrl() { return avatarUrl; }

  // 1. CARGAR PERFIL (Inicio)
  public void cargarPerfil() {
    isLoading.setValue(true);
    repo.obtenerPerfil(new Callback<PerfilUsuarioResponse>() {
      @Override
      public void onResponse(Call<PerfilUsuarioResponse> call, Response<PerfilUsuarioResponse> response) {
        isLoading.setValue(false);
        if (response.isSuccessful() && response.body() != null) {
          PerfilUsuarioResponse p= response.body();
          perfilData.setValue(p);

          //procesamos el img avatar para el localhost del emulador
          procesarAvatar(p.getImgAvatar());
        } else {
          mensajeToast.setValue("Error al cargar perfil: " + response.code());
          Log.d("ERROR_PERFIL", "Error al cargar perfil: "+response.code());
        }
      }

      @Override
      public void onFailure(Call<PerfilUsuarioResponse> call, Throwable t) {
        isLoading.setValue(false);
        mensajeToast.setValue("Error de conexión");
      }
    });
  }

  // meto auxiliar para limpiar la URL del avatar
  private void procesarAvatar(String url) {
    if (url == null || url.isEmpty()) {
      avatarUrl.setValue(null);
      return;
    }
    // Parche para emulador Android: localhost -> 10.0.2.2
    if (url.contains("localhost")) {
      url = url.replace("localhost", "10.0.2.2");
    }
    avatarUrl.setValue(url);
    Log.d("URLAVATAR", "procesarAvatar: "+url);
  }

  // 2. LOGICA DEL BOTON (Alternar entre Editar y Guardar)
  public void onBotonClick(RunnerInput input) {
    if (Boolean.TRUE.equals(isEditable.getValue())) {
      // Si ya estaba editando, intentamos guardar
      intentarGuardarCambios(input);
    } else {
      // Si estaba en modo lectura, habilitamos edición
      habilitarEdicion();
    }
  }

  private void habilitarEdicion() {
    isEditable.setValue(true);
    btnText.setValue("Guardar");
  }

  // 3. GUARDAR CAMBIOS (Conversion y llamada a API)
  private void intentarGuardarCambios(RunnerInput input) {
    // Validar DNI (conversion segura de String a int)
    int dniInt = 0;
    try {
      if (input.dni != null && !input.dni.isEmpty()) {
        dniInt = Integer.parseInt(input.dni);
      }
    } catch (NumberFormatException e) {
      mensajeToast.setValue("El DNI debe ser un número válido");
      return; // Detenemos el proceso si el DNI está mal
    }

    // Creamos el objeto Request que pide la API
    ActualizarPerfilRunnerRequest request = new ActualizarPerfilRunnerRequest(
            input.nombre,
            input.apellido,
            input.telefono,
            input.fechaNac,
            input.genero,
            dniInt,
            input.localidad,
            input.agrupacion,
            input.nombreContacto,
            input.telContacto
    );

    isLoading.setValue(true);
    repo.actualizarRunner(request, new Callback<PerfilUsuarioResponse>() {
      @Override
      public void onResponse(Call<PerfilUsuarioResponse> call, Response<PerfilUsuarioResponse> response) {
        isLoading.setValue(false);
        if (response.isSuccessful() && response.body() != null) {
          // Éxito: actualizamos los datos en pantalla y bloqueamos edición
          PerfilUsuarioResponse p= response.body();
          perfilData.postValue(p);
          procesarAvatar(p.getImgAvatar());

          isEditable.setValue(false);
          btnText.setValue("Editar");
          mensajeToast.setValue("Perfil actualizado correctamente");
        } else {
          mensajeToast.setValue("Error al actualizar: " + response.code());
        }
      }

      @Override
      public void onFailure(Call<PerfilUsuarioResponse> call, Throwable t) {
        isLoading.setValue(false);
        mensajeToast.setValue("Fallo de red: " + t.getMessage());
      }
    });
  }

  // --- CLASE AUXILIAR (DTO interno) ---
  // Sirve solo para pasar los datos crudos del Fragment al ViewModel
  public static class RunnerInput {
    public String nombre, apellido, telefono, dni, fechaNac, genero, localidad, agrupacion, nombreContacto, telContacto;

    public RunnerInput(String nombre, String apellido, String telefono, String dni, String fechaNac, String genero, String localidad, String agrupacion, String nombreContacto, String telContacto) {
      this.nombre = nombre;
      this.apellido = apellido;
      this.telefono = telefono;
      this.dni = dni;
      this.fechaNac = fechaNac;
      this.genero = genero;
      this.localidad = localidad;
      this.agrupacion = agrupacion;
      this.nombreContacto = nombreContacto;
      this.telContacto = telContacto;
    }
  }
}