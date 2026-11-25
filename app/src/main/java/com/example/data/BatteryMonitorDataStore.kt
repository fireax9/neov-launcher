package com.example.data

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.BatteryTelemetry
import com.example.model.ChargingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlin.math.roundToInt

private val Context.batteryMonitorDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "battery_monitor_prefs_v1"
)

@Immutable
data class BatteryMonitorState(
    val level: Int = 82,
    val isCharging: Boolean = false,
    val source: ChargingSource = ChargingSource.BATTERY,
    val temperatureCelsius: Float = 31.2f,
    val voltageVolts: Float = 4.12f,
    val lastUpdatedEpochMs: Long = 0L,
    val isServiceActive: Boolean = false
) {
    fun toBatteryTelemetry(): BatteryTelemetry = BatteryTelemetry(
        level = level,
        isCharging = isCharging,
        source = source,
        temperatureCelsius = temperatureCelsius,
        voltageVolts = voltageVolts
    )
}

@Composable
fun rememberBatteryMonitorState(
    stateFlow: StateFlow<BatteryMonitorState>
): State<BatteryMonitorState> {
    return stateFlow.collectAsStateWithLifecycle()
}

class BatteryMonitorDataStore(private val context: Context) {

    private object Keys {
        val LEVEL = intPreferencesKey("battery_level")
        val IS_CHARGING = booleanPreferencesKey("is_charging")
        val SOURCE = stringPreferencesKey("charging_source")
        val TEMPERATURE = floatPreferencesKey("temperature_celsius")
        val VOLTAGE = floatPreferencesKey("voltage_volts")
        val LAST_UPDATED_MS = longPreferencesKey("last_updated_ms")
        val SERVICE_ACTIVE = booleanPreferencesKey("service_active")
    }

    val monitorStateFlow: Flow<BatteryMonitorState> = context.batteryMonitorDataStore.data.map { prefs ->
        val source = prefs[Keys.SOURCE]?.let { raw ->
            runCatching { ChargingSource.valueOf(raw) }.getOrNull()
        } ?: ChargingSource.BATTERY
        BatteryMonitorState(
            level = (prefs[Keys.LEVEL] ?: 82).coerceIn(0, 100),
            isCharging = prefs[Keys.IS_CHARGING] ?: false,
            source = source,
            temperatureCelsius = prefs[Keys.TEMPERATURE] ?: 31.2f,
            voltageVolts = prefs[Keys.VOLTAGE] ?: 4.12f,
            lastUpdatedEpochMs = prefs[Keys.LAST_UPDATED_MS] ?: 0L,
            isServiceActive = prefs[Keys.SERVICE_ACTIVE] ?: false
        )
    }

    suspend fun saveSnapshot(state: BatteryMonitorState) {
        context.batteryMonitorDataStore.edit { prefs ->
            prefs[Keys.LEVEL] = state.level.coerceIn(0, 100)
            prefs[Keys.IS_CHARGING] = state.isCharging
            prefs[Keys.SOURCE] = state.source.name
            prefs[Keys.TEMPERATURE] = state.temperatureCelsius
            prefs[Keys.VOLTAGE] = state.voltageVolts
            prefs[Keys.LAST_UPDATED_MS] = state.lastUpdatedEpochMs
            prefs[Keys.SERVICE_ACTIVE] = state.isServiceActive
        }
    }

    suspend fun updateServiceActive(active: Boolean) {
        context.batteryMonitorDataStore.edit { prefs ->
            prefs[Keys.SERVICE_ACTIVE] = active
        }
    }

    fun readCurrentSnapshot(serviceActive: Boolean = true): BatteryMonitorState {
        val sticky = runCatching {
            context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        }.getOrNull()
        return parseIntent(sticky, serviceActive = serviceActive)
    }

    fun parseIntent(intent: Intent?, serviceActive: Boolean = true): BatteryMonitorState {
        if (intent == null) {
            return BatteryMonitorState(
                lastUpdatedEpochMs = System.currentTimeMillis(),
                isServiceActive = serviceActive
            )
        }
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100).coerceAtLeast(1)
        val pct = if (level >= 0) ((level * 100f) / scale).roundToInt().coerceIn(0, 100) else 80

        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL ||
            plugged > 0

        val source = when {
            !isCharging -> ChargingSource.BATTERY
            plugged == BatteryManager.BATTERY_PLUGGED_AC -> ChargingSource.AC
            plugged == BatteryManager.BATTERY_PLUGGED_USB -> ChargingSource.USB
            plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS -> ChargingSource.WIRELESS
            else -> ChargingSource.AC
        }

        val tempTenths = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 300)
        val voltageMv = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 4100)

        return BatteryMonitorState(
            level = pct,
            isCharging = isCharging,
            source = source,
            temperatureCelsius = tempTenths / 10f,
            voltageVolts = voltageMv / 1000f,
            lastUpdatedEpochMs = System.currentTimeMillis(),
            isServiceActive = serviceActive
        )
    }
}
