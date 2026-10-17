package com.pindou.shop.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import com.pindou.shop.MainActivity
import com.pindou.shop.data.Store

class ReminderService : Service() {
    companion object {
        private const val CHANNEL_ID = "pindou_reminder"
        private const val NOTIFY_ID = 100
        private var ringtone: Ringtone? = null
        private val handler = Handler(Looper.getMainLooper())

        fun start(context: Context) {
            val i = Intent(context, ReminderService::class.java)
            if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(i)
            else context.startService(i)
        }

        fun stopAlarm() {
            try { ringtone?.stop(); ringtone = null } catch (_: Exception) {}
        }
    }

    private lateinit var ticker: Runnable

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(NOTIFY_ID, buildNotification("计时提醒服务运行中", false))
        ticker = object : Runnable {
            override fun run() {
                checkReminders()
                handler.postDelayed(this, 1000)
            }
        }
        handler.postDelayed(ticker, 1000)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val c = NotificationChannel(CHANNEL_ID, "计时提醒", NotificationManager.IMPORTANCE_HIGH)
            c.setSound(null, null)
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(c)
        }
    }

    private fun buildNotification(text: String, alarm: Boolean): Notification {
        val pi = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val b = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(this, CHANNEL_ID) else Notification.Builder(this)
        return b.setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("拼豆计时计费")
            .setContentText(text)
            .setContentIntent(pi)
            .setAutoCancel(alarm)
            .setOngoing(!alarm)
            .build()
    }

    private fun checkReminders() {
        val now = System.currentTimeMillis()
        val due = mutableListOf<String>()
        Store.tables.forEach { t ->
            t.seats.forEach { s ->
                val sess = s.session ?: return@forEach
                val label = "${t.name} ${t.id}-${s.no}座(${sess.customer})"
                if (!sess.alertedPre && sess.end - now <= 10 * 60 * 1000 && sess.end - now > 0) {
                    sess.alertedPre = true; due.add("$label 还剩不到10分钟")
                }
                if (!sess.alertedEnd && now >= sess.end) {
                    sess.alertedEnd = true; due.add("$label 计时已结束")
                }
            }
        }
        if (due.isNotEmpty()) {
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .notify(NOTIFY_ID + 1, buildNotification(due.joinToString("；"), true))
            playAlarm()
        }
    }

    private fun playAlarm() {
        try {
            if (ringtone?.isPlaying == true) return
            stopAlarm()
            var uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            if (uri == null) uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            if (uri != null) {
                ringtone = RingtoneManager.getRingtone(this, uri)
                ringtone?.setLooping(true)
                ringtone?.play()
            }
            val v = getSystemService(VIBRATOR_SERVICE) as Vibrator
            if (v.hasVibrator()) {
                val pattern = longArrayOf(0, 900, 400, 900, 400, 900)
                if (Build.VERSION.SDK_INT >= 26) v.vibrate(VibrationEffect.createWaveform(pattern, 0))
                else v.vibrate(pattern, 0)
            }
            handler.removeCallbacksAndMessages(null)
            handler.postDelayed({ stopAlarm() }, 60000)
        } catch (_: Exception) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarm()
    }
}
