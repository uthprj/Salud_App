package com.example.salud_app.components.step_counter

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Manager để đếm bước chân sử dụng cảm biến TYPE_STEP_COUNTER
 * Dữ liệu được lưu trong SharedPreferences (local)
 */
class StepCounterManager(private val context: Context) : SensorEventListener {

    companion object {
        private const val TAG = "StepCounterManager"
        private const val PREFS_NAME = "step_counter_prefs"
        private const val KEY_TOTAL_STEPS = "total_steps"
        private const val KEY_INITIAL_STEPS = "initial_steps"
        private const val KEY_LAST_DATE = "last_date"
        private const val KEY_DAILY_STEPS = "daily_steps"

        @Volatile
        private var INSTANCE: StepCounterManager? = null

        fun getInstance(context: Context): StepCounterManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: StepCounterManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    private val sensorManager: SensorManager = 
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    
    private val stepCounterSensor: Sensor? = 
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    
    private val stepDetectorSensor: Sensor? = 
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    private val prefs: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    private val _dailySteps = MutableStateFlow(0)
    val dailySteps: StateFlow<Int> = _dailySteps.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _hasStepSensor = MutableStateFlow(false)
    val hasStepSensor: StateFlow<Boolean> = _hasStepSensor.asStateFlow()

    private var initialSteps: Int = -1
    private var isListening = false

    init {
        _hasStepSensor.value = stepCounterSensor != null || stepDetectorSensor != null
        loadDailySteps()
        Log.d(TAG, "StepCounterManager initialized. Has step sensor: ${_hasStepSensor.value}")
    }

    /**
     * Kiểm tra xem thiết bị có hỗ trợ đếm bước không
     */
    fun isStepCounterAvailable(): Boolean {
        return stepCounterSensor != null || stepDetectorSensor != null
    }

    /**
     * Bắt đầu đếm bước chân
     */
    fun startCounting() {
        if (isListening) {
            Log.d(TAG, "Already listening to step sensor")
            return
        }

        // Kiểm tra và reset nếu ngày mới
        checkAndResetForNewDay()

        val sensor = stepCounterSensor ?: stepDetectorSensor
        if (sensor != null) {
            val registered = sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_UI
            )
            isListening = registered
            _isRunning.value = registered
            Log.d(TAG, "Started step counting. Registered: $registered")
        } else {
            Log.w(TAG, "No step sensor available on this device")
            _isRunning.value = false
        }
    }

    /**
     * Dừng đếm bước chân
     */
    fun stopCounting() {
        if (isListening) {
            sensorManager.unregisterListener(this)
            isListening = false
            _isRunning.value = false
            Log.d(TAG, "Stopped step counting")
        }
    }

    /**
     * Load số bước hôm nay từ SharedPreferences
     */
    private fun loadDailySteps() {
        val today = LocalDate.now().format(dateFormatter)
        val savedDate = prefs.getString(KEY_LAST_DATE, "") ?: ""

        if (savedDate == today) {
            _dailySteps.value = prefs.getInt(KEY_DAILY_STEPS, 0)
            initialSteps = prefs.getInt(KEY_INITIAL_STEPS, -1)
        } else {
            // Ngày mới, reset về 0
            _dailySteps.value = 0
            initialSteps = -1
            saveSteps()
        }
        Log.d(TAG, "Loaded daily steps: ${_dailySteps.value}, initialSteps: $initialSteps")
    }

    /**
     * Kiểm tra và reset nếu là ngày mới
     */
    private fun checkAndResetForNewDay() {
        val today = LocalDate.now().format(dateFormatter)
        val savedDate = prefs.getString(KEY_LAST_DATE, "") ?: ""

        if (savedDate != today) {
            Log.d(TAG, "New day detected. Resetting steps.")
            _dailySteps.value = 0
            initialSteps = -1
            saveSteps()
        }
    }

    /**
     * Lưu số bước vào SharedPreferences
     */
    private fun saveSteps() {
        val today = LocalDate.now().format(dateFormatter)
        prefs.edit().apply {
            putInt(KEY_DAILY_STEPS, _dailySteps.value)
            putInt(KEY_INITIAL_STEPS, initialSteps)
            putString(KEY_LAST_DATE, today)
            apply()
        }
        Log.d(TAG, "Saved steps: ${_dailySteps.value}")
    }

    /**
     * Lấy số bước hiện tại
     */
    fun getCurrentSteps(): Int {
        checkAndResetForNewDay()
        return _dailySteps.value
    }

    /**
     * Cập nhật số bước thủ công (cho trường hợp không có sensor)
     */
    fun setManualSteps(steps: Int) {
        _dailySteps.value = steps
        saveSteps()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_STEP_COUNTER -> {
                    // TYPE_STEP_COUNTER trả về tổng số bước từ lần reboot
                    val totalSteps = it.values[0].toInt()
                    
                    if (initialSteps < 0) {
                        // Lần đầu tiên, lưu số bước ban đầu
                        initialSteps = totalSteps - _dailySteps.value
                        Log.d(TAG, "Initial steps set to: $initialSteps")
                    }
                    
                    val newDailySteps = totalSteps - initialSteps
                    if (newDailySteps >= 0 && newDailySteps != _dailySteps.value) {
                        _dailySteps.value = newDailySteps
                        saveSteps()
                        Log.d(TAG, "Step counter: total=$totalSteps, daily=${_dailySteps.value}")
                    }
                }
                
                Sensor.TYPE_STEP_DETECTOR -> {
                    // TYPE_STEP_DETECTOR phát hiện từng bước
                    _dailySteps.value += 1
                    saveSteps()
                    Log.d(TAG, "Step detected. Total daily: ${_dailySteps.value}")
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG, "Sensor accuracy changed: $accuracy")
    }

    /**
     * Cleanup khi không dùng nữa
     */
    fun destroy() {
        stopCounting()
        INSTANCE = null
    }
}
