package com.example.runnconnect.data.fcm;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;

public class FcmTopicManager {

  private static final String TAG = "FcmTopicManager";
  private static final String TOPICO_GLOBAL = "topico_global";

  public static void suscribirTopicosUsuario(int idUsuario) {
    FirebaseMessaging.getInstance().subscribeToTopic(TOPICO_GLOBAL)
        .addOnCompleteListener(task -> {
          if (task.isSuccessful()) {
            Log.d(TAG, "Suscripción exitosa a " + TOPICO_GLOBAL);
          } else {
            Log.e(TAG, "Error al suscribir a " + TOPICO_GLOBAL, task.getException());
          }
        });

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
  }

  public static void suscribirEvento(int idEvento) {
    if (idEvento > 0) {
      String topicEvento = "evento_" + idEvento;
      FirebaseMessaging.getInstance().subscribeToTopic(topicEvento)
          .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
              Log.d(TAG, "Suscripción exitosa a " + topicEvento);
            }
          });
    }
  }

  public static void suscribirEventoCategoria(int idEvento, int idCategoria) {
    if (idEvento > 0 && idCategoria > 0) {
      String topicCat = "evento_" + idEvento + "_cat_" + idCategoria;
      FirebaseMessaging.getInstance().subscribeToTopic(topicCat)
          .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
              Log.d(TAG, "Suscripción exitosa a " + topicCat);
            }
          });
    }
  }
}
