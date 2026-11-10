package com.example.runnconnect.data.fcm;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class FcmTopicManager {

  private static final String TAG = "FcmTopicManager";
  private static final String TOPICO_GLOBAL = "topico_global";
  private static final String PREF_FCM_TOPICS = "fcm_topics_sp";
  private static final String KEY_EVENT_TOPICS = "subscribed_event_topics";
  private static final Set<String> topicosEventosActivos = Collections.synchronizedSet(new HashSet<>());

  public static void suscribirTopicosUsuario(int idUsuario) {
    suscribirTopicosUsuario(idUsuario, "runner");
  }

  public static void suscribirTopicosUsuario(int idUsuario, String tipoUsuario) {
    boolean esOrganizador = "organizador".equalsIgnoreCase(tipoUsuario);

    if (esOrganizador) {
      // El organizador NO debe recibir anuncios globales de inscripciones abiertas ni avisos de categorías de runners
      FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPICO_GLOBAL)
          .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
              Log.d(TAG, "Organizador desuscripto de " + TOPICO_GLOBAL);
            }
          });
      desuscribirTodosEventos();
    } else {
      FirebaseMessaging.getInstance().subscribeToTopic(TOPICO_GLOBAL)
          .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
              Log.d(TAG, "Suscripción exitosa a " + TOPICO_GLOBAL);
            } else {
              Log.e(TAG, "Error al suscribir a " + TOPICO_GLOBAL, task.getException());
            }
          });
    }

    if (idUsuario > 0) {
      String topicUsuario = "runner_" + idUsuario;
      FirebaseMessaging.getInstance().subscribeToTopic(topicUsuario)
          .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
              Log.d(TAG, "Suscripción exitosa a " + topicUsuario);
            } else {
              Log.e(TAG, "Error al suscribir a " + topicUsuario, task.getException());
            }
          });
    }
  }

  public static void desuscribirTopicosUsuario(int idUsuario) {
    FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPICO_GLOBAL);

    if (idUsuario > 0) {
      String topicUsuario = "runner_" + idUsuario;
      FirebaseMessaging.getInstance().unsubscribeFromTopic(topicUsuario)
          .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
              Log.d(TAG, "Desuscripción exitosa de " + topicUsuario);
            } else {
              Log.e(TAG, "Error al desuscribir de " + topicUsuario, task.getException());
            }
          });
    }

    desuscribirTodosEventos();
  }

  public static void suscribirEvento(int idEvento) {
    if (idEvento > 0) {
      String topicEvento = "evento_" + idEvento;
      guardarTopicoEvento(topicEvento);
      FirebaseMessaging.getInstance().subscribeToTopic(topicEvento)
          .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
              Log.d(TAG, "Suscripción exitosa a " + topicEvento);
            }
          });
    }
  }

  public static void desuscribirEvento(int idEvento) {
    if (idEvento > 0) {
      String topicEvento = "evento_" + idEvento;
      eliminarTopicoEvento(topicEvento);
      FirebaseMessaging.getInstance().unsubscribeFromTopic(topicEvento)
          .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
              Log.d(TAG, "Desuscripción exitosa de " + topicEvento);
            }
          });
    }
  }

  public static void suscribirEventoCategoria(int idEvento, int idCategoria) {
    if (idEvento > 0 && idCategoria > 0) {
      String topicCat = "evento_" + idEvento + "_cat_" + idCategoria;
      guardarTopicoEvento(topicCat);
      FirebaseMessaging.getInstance().subscribeToTopic(topicCat)
          .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
              Log.d(TAG, "Suscripción exitosa a " + topicCat);
            }
          });
    }
  }

  public static void desuscribirEventoCategoria(int idEvento, int idCategoria) {
    if (idEvento > 0 && idCategoria > 0) {
      String topicCat = "evento_" + idEvento + "_cat_" + idCategoria;
      eliminarTopicoEvento(topicCat);
      FirebaseMessaging.getInstance().unsubscribeFromTopic(topicCat)
          .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
              Log.d(TAG, "Desuscripción exitosa de " + topicCat);
            }
          });
    }
  }

  public static void desuscribirTodosEventos() {
    Set<String> todos = new HashSet<>(topicosEventosActivos);
    topicosEventosActivos.clear();

    try {
      Context context = FirebaseApp.getInstance().getApplicationContext();
      SharedPreferences sp = context.getSharedPreferences(PREF_FCM_TOPICS, Context.MODE_PRIVATE);
      Set<String> guardados = sp.getStringSet(KEY_EVENT_TOPICS, null);
      if (guardados != null) {
        todos.addAll(guardados);
      }
      sp.edit().remove(KEY_EVENT_TOPICS).apply();
    } catch (Exception ignored) {}

    for (String topic : todos) {
      FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
          .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
              Log.d(TAG, "Desuscripto de tópico de evento: " + topic);
            }
          });
    }
  }

  private static void guardarTopicoEvento(String topic) {
    topicosEventosActivos.add(topic);
    try {
      Context context = FirebaseApp.getInstance().getApplicationContext();
      SharedPreferences sp = context.getSharedPreferences(PREF_FCM_TOPICS, Context.MODE_PRIVATE);
      Set<String> guardados = new HashSet<>(sp.getStringSet(KEY_EVENT_TOPICS, Collections.emptySet()));
      guardados.add(topic);
      sp.edit().putStringSet(KEY_EVENT_TOPICS, guardados).apply();
    } catch (Exception ignored) {}
  }

  private static void eliminarTopicoEvento(String topic) {
    topicosEventosActivos.remove(topic);
    try {
      Context context = FirebaseApp.getInstance().getApplicationContext();
      SharedPreferences sp = context.getSharedPreferences(PREF_FCM_TOPICS, Context.MODE_PRIVATE);
      Set<String> guardados = new HashSet<>(sp.getStringSet(KEY_EVENT_TOPICS, Collections.emptySet()));
      if (guardados.remove(topic)) {
        sp.edit().putStringSet(KEY_EVENT_TOPICS, guardados).apply();
      }
    } catch (Exception ignored) {}
  }
}
