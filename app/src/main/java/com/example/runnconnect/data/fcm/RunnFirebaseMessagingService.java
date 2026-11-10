package com.example.runnconnect.data.fcm;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.runnconnect.MainActivity;
import com.example.runnconnect.R;
import com.example.runnconnect.data.preferencias.SessionManager;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class RunnFirebaseMessagingService extends FirebaseMessagingService {

  private static final String TAG = "RunnFirebaseMsgService";
  private static final String CANAL_ID = "canal_notificaciones_runnconnect";
  private static final String CANAL_NOMBRE = "Notificaciones RunnConnect";
  private static final String CANAL_DESCRIPCION = "Avisos y novedades de eventos en tiempo real";

  @Override
  public void onNewToken(@NonNull String token) {
    super.onNewToken(token);
    Log.d(TAG, "Nuevo FCM Token recibido: " + token);
  }

  @Override
  public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
    super.onMessageReceived(remoteMessage);
    String from = remoteMessage.getFrom();
    Log.d(TAG, "Mensaje push recibido desde: " + from);

    SessionManager sessionManager = new SessionManager(getApplicationContext());
    if (!sessionManager.tieneSesionActiva()) {
      Log.d(TAG, "Ignorando push: no hay sesión activa.");
      return;
    }

    // Si es organizador, solo debe recibir notificaciones de su tópico privado (runner_{idUsuario})
    if ("organizador".equalsIgnoreCase(sessionManager.getTipoUsuario()) && from != null) {
      if (from.contains("evento_") || from.contains("topico_global")) {
        String topicName = from.replace("/topics/", "");
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topicName);
        Log.d(TAG, "Ignorando y desuscribiendo tópico de runner para organizador: " + topicName);
        return;
      }
    }

    // Sincronizar en tiempo real el contador de la app
    SincronizarNotificacion.notificarNuevaLlegada();

    String titulo = null;
    String cuerpo = null;

    if (remoteMessage.getNotification() != null) {
      titulo = remoteMessage.getNotification().getTitle();
      cuerpo = remoteMessage.getNotification().getBody();
    }

    if (titulo == null && remoteMessage.getData().containsKey("titulo")) {
      titulo = remoteMessage.getData().get("titulo");
    }
    if (cuerpo == null && remoteMessage.getData().containsKey("mensaje")) {
      cuerpo = remoteMessage.getData().get("mensaje");
    }

    if (titulo == null || titulo.trim().isEmpty()) {
      titulo = getString(R.string.app_name);
    }
    if (cuerpo == null) {
      cuerpo = "";
    }

    int idEvento = 0;
    if (remoteMessage.getData().containsKey("idEvento")) {
      try {
        idEvento = Integer.parseInt(remoteMessage.getData().get("idEvento"));
      } catch (NumberFormatException ignored) {}
    }

    mostrarNotificacion(titulo, cuerpo, idEvento);
  }

  private void mostrarNotificacion(String titulo, String cuerpo, int idEvento) {
    crearCanalNotificaciones();

    Intent intent = new Intent(this, MainActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    intent.putExtra("abrirNotificaciones", true);
    if (idEvento > 0) {
      intent.putExtra("idEvento", idEvento);
    }

    int requestCode = (int) (System.currentTimeMillis() & 0xfffffff);
    PendingIntent pendingIntent = PendingIntent.getActivity(
        this,
        requestCode,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
    );

    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CANAL_ID)
        .setSmallIcon(R.drawable.ic_notification_bell)
        .setContentTitle(titulo)
        .setContentText(cuerpo)
        .setStyle(new NotificationCompat.BigTextStyle().bigText(cuerpo))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent);

    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
        Log.w(TAG, "Permiso POST_NOTIFICATIONS no concedido. No se puede mostrar la notificación.");
        return;
      }
    }

    notificationManager.notify(requestCode, builder.build());
  }

  private void crearCanalNotificaciones() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      NotificationChannel canal = new NotificationChannel(
          CANAL_ID,
          CANAL_NOMBRE,
          NotificationManager.IMPORTANCE_HIGH
      );
      canal.setDescription(CANAL_DESCRIPCION);
      canal.enableVibration(true);

      NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
      if (manager != null) {
        manager.createNotificationChannel(canal);
      }
    }
  }
}
