package com.example.data

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import android.view.KeyEvent
import com.example.model.BatteryTelemetry
import com.example.model.DeviceTelemetry
import com.example.service.BatteryMonitorService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import kotlin.math.roundToInt

class SystemTelemetryRepository(private val context: Context) {

    val batteryStore = BatteryMonitorDataStore(context.applicationContext)

    fun startBatteryMonitorService() {
        runCatching {
            val intent = Intent(context.applicationContext, BatteryMonitorService::class.java)
            context.applicationContext.startService(intent)
        }
    }

    fun stopBatteryMonitorService() {
        runCatching {
            val intent = Intent(context.applicationContext, BatteryMonitorService::class.java)
            context.applicationContext.stopService(intent)
        }
    }

    fun observeBatteryMonitorState(): Flow<BatteryMonitorState> {
        val liveSyncFlow = callbackFlow {
            val scope = this
            val initialSnapshot = batteryStore.readCurrentSnapshot(serviceActive = true)
            scope.launch(Dispatchers.IO) {
                batteryStore.saveSnapshot(initialSnapshot)
            }
            trySend(initialSnapshot)

            val receiver = object : BroadcastReceiver() {
                override fun onReceive(ctx: Context?, intent: Intent?) {
                    val snapshot = when (intent?.action) {
                        Intent.ACTION_BATTERY_CHANGED -> batteryStore.parseIntent(intent, serviceActive = true)
                        Intent.ACTION_POWER_CONNECTED -> batteryStore.readCurrentSnapshot(serviceActive = true).copy(isCharging = true)
                        Intent.ACTION_POWER_DISCONNECTED -> batteryStore.readCurrentSnapshot(serviceActive = true).copy(isCharging = false)
                        else -> batteryStore.readCurrentSnapshot(serviceActive = true)
                    }
                    scope.launch(Dispatchers.IO) {
                        batteryStore.saveSnapshot(snapshot)
                    }
                }
            }

            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_BATTERY_CHANGED)
                addAction(Intent.ACTION_POWER_CONNECTED)
                addAction(Intent.ACTION_POWER_DISCONNECTED)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                @Suppress("DEPRECATION")
                context.registerReceiver(receiver, filter)
            }

            awaitClose {
                runCatching { context.unregisterReceiver(receiver) }
            }
        }.flowOn(Dispatchers.IO)

        return merge(batteryStore.monitorStateFlow, liveSyncFlow)
    }

    fun observeBatteryTelemetry(): Flow<BatteryTelemetry> =
        observeBatteryMonitorState().map { it.toBatteryTelemetry() }

    fun observeDeviceTelemetry(): Flow<DeviceTelemetry> = flow {
        while (true) {
            emit(readDeviceTelemetrySync())
            delay(3000L)
        }
    }.flowOn(Dispatchers.IO)

    fun readDeviceTelemetrySync(): DeviceTelemetry {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memInfo)

        val totalRamMb = (memInfo.totalMem / (1024L * 1024L)).coerceAtLeast(1L)
        val availRamMb = (memInfo.availMem / (1024L * 1024L)).coerceIn(0L, totalRamMb)
        val usedRamMb = (totalRamMb - availRamMb).coerceAtLeast(0L)

        val statFs = StatFs(Environment.getDataDirectory().path)
        val totalBytes = statFs.totalBytes.toFloat().coerceAtLeast(1f)
        val availBytes = statFs.availableBytes.toFloat().coerceAtLeast(0f)
        val bytesPerGb = 1024f * 1024f * 1024f

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        val maxVol = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)?.coerceAtLeast(1) ?: 15
        val curVol = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 7
        val volPercent = ((curVol * 100f) / maxVol).roundToInt().coerceIn(0, 100)

        return DeviceTelemetry(
            usedRamMb = usedRamMb,
            totalRamMb = totalRamMb,
            freeStorageGb = availBytes / bytesPerGb,
            totalStorageGb = totalBytes / bytesPerGb,
            mediaVolumePercent = volPercent
        )
    }

    fun observeClock(): Flow<LocalDateTime> = flow {
        while (true) {
            emit(LocalDateTime.now())
            delay(1000L)
        }
    }

    fun setMediaVolumePercent(percent: Int): Int {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return percent
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        val target = ((percent.coerceIn(0, 100) / 100f) * maxVol).roundToInt().coerceIn(0, maxVol)
        runCatching {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
        }
        return ((target * 100f) / maxVol).roundToInt().coerceIn(0, 100)
    }

    fun dispatchMediaKey(keyCode: Int) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
        val eventTime = SystemClock.uptimeMillis()
        runCatching {
            audioManager.dispatchMediaKeyEvent(KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, keyCode, 0))
            audioManager.dispatchMediaKeyEvent(KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, keyCode, 0))
        }
    }
}
