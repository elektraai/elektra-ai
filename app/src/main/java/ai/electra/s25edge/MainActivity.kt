package ai.electra.s25edge

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.PowerManager
import android.os.StatFs
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.text.DecimalFormat

class MainActivity : Activity() {
    private val neon = Color.rgb(57, 255, 136)
    private val panel = Color.rgb(11, 21, 16)
    private val muted = Color.rgb(145, 169, 154)
    private lateinit var content: LinearLayout
    private val df = DecimalFormat("0.0")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(5, 9, 7)
        window.navigationBarColor = Color.rgb(5, 9, 7)
        buildScreen()
        renderDiagnostics()
    }

    private fun buildScreen() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(5, 9, 7))
            setPadding(18, 18, 18, 18)
        }
        val header = TextView(this).apply {
            text = "ELECTRA AI\nS25 EDGE EDITION  /  CORE 0.1"
            textSize = 21f
            setTextColor(neon)
            setPadding(4, 12, 4, 18)
            letterSpacing = 0.12f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        root.addView(header)
        val scroll = ScrollView(this)
        content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    private fun renderDiagnostics() {
        content.removeAllViews()
        addCard("STATUS SISTEM", "Pemeriksaan lokal aktif. ELECTRA membaca informasi yang disediakan Android; tidak mengubah pengaturan atau data.")
        addCard("IDENTITAS PERANGKAT", """
            Merek: ${Build.MANUFACTURER}
            Model: ${Build.MODEL}
            Nama perangkat: ${Build.DEVICE}
            Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})
            Build: ${Build.DISPLAY}
        """.trimIndent())
        addCard("BATERAI", batteryReport())
        addCard("MEMORI & PENYIMPANAN", memoryReport())
        addCard("KONEKTIVITAS", networkReport())
        addCard("SENSOR TERSEDIA", sensorReport())
        addCard("SUHU PERANGKAT", thermalReport())
        addCard("BATAS PEMERIKSAAN", "Versi ini membaca data sistem yang diizinkan Android. Ini belum mengukur kesehatan fisik baterai, kondisi panel, kamera, speaker, port USB, atau kerusakan motherboard. Tidak ada flashing, reset, pembukaan kunci, atau perbaikan otomatis.")
        val refresh = Button(this).apply {
            text = "PERIKSA ULANG"
            setTextColor(Color.rgb(3, 17, 9))
            setBackgroundColor(neon)
            textSize = 14f
            setOnClickListener { renderDiagnostics() }
        }
        content.addView(refresh, LinearLayout.LayoutParams(-1, dp(52)).apply {
            topMargin = dp(12)
            bottomMargin = dp(24)
        })
    }

    private fun batteryReport(): String {
        val intent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            ?: return "Data baterai tidak tersedia."
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val percent = if (level >= 0 && scale > 0) level * 100 / scale else -1
        val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) / 10.0
        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
        val status = when (intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Mengisi"
            BatteryManager.BATTERY_STATUS_FULL -> "Penuh"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Tidak mengisi"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Tidak mengisi daya"
            else -> "Tidak diketahui"
        }
        return "Level: ${if (percent >= 0) "$percent%" else "Tidak tersedia"}\nStatus: $status\nSuhu baterai: ${if (temp >= 0) "${df.format(temp)} °C" else "Tidak tersedia"}\nTegangan: ${if (voltage > 0) "$voltage mV" else "Tidak tersedia"}\nCatatan: suhu dan level bukan pengukuran kapasitas/kesehatan baterai."
    }

    private fun memoryReport(): String {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memory = ActivityManager.MemoryInfo()
        manager.getMemoryInfo(memory)
        val totalRam = memory.totalMem / (1024.0 * 1024.0 * 1024.0)
        val availRam = memory.availMem / (1024.0 * 1024.0 * 1024.0)
        val stat = StatFs(Environment.getDataDirectory().path)
        val totalStorage = stat.totalBytes / (1024.0 * 1024.0 * 1024.0)
        val freeStorage = stat.availableBytes / (1024.0 * 1024.0 * 1024.0)
        return "RAM terdeteksi: ${df.format(totalRam)} GB\nRAM tersedia saat ini: ${df.format(availRam)} GB\nPenyimpanan internal: ${df.format(totalStorage)} GB\nRuang kosong: ${df.format(freeStorage)} GB"
    }

    private fun networkReport(): String {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return "Tidak ada jaringan aktif."
        val caps = cm.getNetworkCapabilities(network) ?: return "Status jaringan tidak tersedia."
        val type = when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Data seluler"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> "Jaringan lain"
        }
        val internet = if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) "Internet terverifikasi" else "Internet belum terverifikasi"
        return "Jenis koneksi: $type\nStatus: $internet"
    }

    private fun sensorReport(): String {
        val manager = getSystemService(Context.SENSOR_SERVICE) as android.hardware.SensorManager
        val sensors = manager.getSensorList(android.hardware.Sensor.TYPE_ALL)
        if (sensors.isEmpty()) return "Android tidak melaporkan sensor yang tersedia."
        return sensors.joinToString(separator = "\n") { "• ${it.name} (${it.vendor})" }
    }

    private fun thermalReport(): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return "Status termal sistem tidak didukung versi Android ini."
        val power = getSystemService(Context.POWER_SERVICE) as PowerManager
        val status = power.currentThermalStatus
        val label = when (status) {
            PowerManager.THERMAL_STATUS_NONE -> "Normal"
            PowerManager.THERMAL_STATUS_LIGHT -> "Ringan"
            PowerManager.THERMAL_STATUS_MODERATE -> "Sedang"
            PowerManager.THERMAL_STATUS_SEVERE -> "Tinggi"
            PowerManager.THERMAL_STATUS_CRITICAL -> "Kritis"
            PowerManager.THERMAL_STATUS_EMERGENCY -> "Darurat"
            PowerManager.THERMAL_STATUS_SHUTDOWN -> "Shutdown termal"
            else -> "Tidak diketahui"
        }
        return "Status termal Android: $label\nCatatan: ini status pembatasan termal sistem, bukan suhu motherboard dalam °C."
    }

    private fun addCard(title: String, body: String) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(15), dp(14), dp(15), dp(14))
            setBackgroundColor(panel)
            elevation = dp(2).toFloat()
        }
        val heading = TextView(this).apply {
            text = title
            textSize = 13f
            setTextColor(neon)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            letterSpacing = 0.08f
        }
        val text = TextView(this).apply {
            this.text = body
            textSize = 13f
            setTextColor(muted)
            setLineSpacing(dp(3).toFloat(), 1f)
            setPadding(0, dp(9), 0, 0)
        }
        card.addView(heading)
        card.addView(text)
        content.addView(card, LinearLayout.LayoutParams(-1, -2).apply {
            bottomMargin = dp(11)
        })
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
