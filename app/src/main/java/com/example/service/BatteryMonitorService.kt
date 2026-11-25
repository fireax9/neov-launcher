package com.example.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import com.example.data.BatteryMonitorDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class BatteryMonitorService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var batteryStore: BatteryMonitorDataStore
    private var receiverRegistered = false

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action ?: return
            val snapshot = when (action) {
                Intent.ACTION_BATTERY_CHANGED -> {
                    batteryStore.parseIntent(intent, serviceActive = true)
                }
                Intent.ACTION_POWER_CONNECTED -> {
                    batteryStore.readCurrentSnapshot(serviceActive = true).copy(
                        isCharging = true,
                        lastUpdatedEpochMs = System.currentTimeMillis(),
                        isServiceActive = true
                    )
                }
                Intent.ACTION_POWER_DISCONNECTED -> {
                    batteryStore.readCurrentSnapshot(serviceActive = true).copy(
                        isCharging = false,
                        lastUpdatedEpochMs = System.currentTimeMillis(),
                        isServiceActive = true
                    )
                }
                else -> batteryStore.readCurrentSnapshot(serviceActive = true)
            }
            serviceScope.launch {
                batteryStore.saveSnapshot(snapshot)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        batteryStore = BatteryMonitorDataStore(applicationContext)
        registerBatteryReceiver()
        persistCurrentSnapshot()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        registerBatteryReceiver()
        persistCurrentSnapshot()
        return START_STICKY
    }

    override fun onDestroy() {
        if (receiverRegistered) {
            runCatching { unregisterReceiver(batteryReceiver) }
            receiverRegistered = false
        }
        serviceScope.launch {
            runCatching { batteryStore.updateServiceActive(false) }
        }
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun registerBatteryReceiver() {
        if (receiverRegistered) return
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(Intent.ACTION_BATTERY_LOW)
            addAction(Intent.ACTION_BATTERY_OKAY)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(batteryReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(batteryReceiver, filter)
        }
        receiverRegistered = true
    }

    private fun persistCurrentSnapshot() {
        val snapshot = batteryStore.readCurrentSnapshot(serviceActive = true)
        serviceScope.launch {
            batteryStore.saveSnapshot(snapshot)
        }
    }
}
