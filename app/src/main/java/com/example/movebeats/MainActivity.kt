package com.example.movebeats

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var audioManager: AudioManager
    private var mediaController: MediaController? = null
    private lateinit var mediaSessionManager: MediaSessionManager

    // Sonidos
    private var mediaPlayerLeft: MediaPlayer? = null
    private var mediaPlayerRight: MediaPlayer? = null
    private var mediaPlayerUp: MediaPlayer? = null
    private var mediaPlayerDown: MediaPlayer? = null

    // Modos
    private var isSoundActive = false
    private var isMediaControlActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar Firebase Auth para verificar si el usuario está autenticado
        firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            // Si el usuario no ha iniciado sesión, redirigir a LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        // Continuar con la funcionalidad normal si el usuario está autenticado
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Mostrar el nombre del usuario
        val userNameTextView = findViewById<TextView>(R.id.user_name_text_view)
        userNameTextView.text = "Hola, ${currentUser.displayName}"

        // Mostrar la foto de perfil del usuario
        val userProfileImageView = findViewById<ImageView>(R.id.user_profile_image_view)
        val profileImageUrl: Uri? = currentUser.photoUrl
        if (profileImageUrl != null) {
            Glide.with(this)
                .load(profileImageUrl)
                .into(userProfileImageView)
        }

        // Configuración para ajuste de barras de sistema en la vista
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar el sensor de acelerómetro
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // Inicializar AudioManager para controlar el volumen
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // Inicializar los sonidos
        mediaPlayerLeft = MediaPlayer.create(this, R.raw.sound_left)
        mediaPlayerRight = MediaPlayer.create(this, R.raw.sound_right)
        mediaPlayerUp = MediaPlayer.create(this, R.raw.sound_up)
        mediaPlayerDown = MediaPlayer.create(this, R.raw.sound_down)

        // Botones para cambiar entre modos
        val buttonNone = findViewById<Button>(R.id.button_none)
        val buttonSounds = findViewById<Button>(R.id.button_sounds)
        val buttonMediaControl = findViewById<Button>(R.id.button_media_control)

        // Modo sin funcionalidad
        buttonNone.setOnClickListener {
            isSoundActive = false
            isMediaControlActive = false
            Log.d("MainActivity", "Modo sin funcionalidad activado")
        }

        // Modo sonidos activado
        buttonSounds.setOnClickListener {
            isSoundActive = true
            isMediaControlActive = false
            Log.d("MainActivity", "Modo sonidos activado")
        }

        // Modo control del reproductor activado
        buttonMediaControl.setOnClickListener {
            isSoundActive = false
            isMediaControlActive = true
            Log.d("MainActivity", "Modo control de reproductor activado")
            requestNotificationPermission()
        }

        // Registrar el listener para el acelerómetro
        accelerometer?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    // Método para solicitar permisos de notificación y redirigir al usuario a la configuración
    private fun requestNotificationPermission() {
        if (!isNotificationServiceEnabled()) {
            Toast.makeText(this, "Permitir acceso a las notificaciones", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        } else {
            initializeMediaController()
        }
    }

    // Verificar si el servicio de notificaciones está habilitado
    private fun isNotificationServiceEnabled(): Boolean {
        val enabledListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        val packageName = packageName
        return enabledListeners != null && enabledListeners.contains(packageName)
    }

    // Inicializar el controlador de medios
    private fun initializeMediaController() {
        mediaSessionManager = getSystemService(MEDIA_SESSION_SERVICE) as MediaSessionManager
        val activeSessions = mediaSessionManager.getActiveSessions(ComponentName(this, NotificationListener::class.java))
        if (activeSessions.isNotEmpty()) {
            mediaController = activeSessions[0] // Tomamos la primera sesión activa
        } else {
            Log.d("MainActivity", "No se encontraron sesiones multimedia activas")
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val x = it.values[0]
            val y = it.values[1]

            Log.d("SensorValues", "X: $x, Y: $y")

            // Umbrales de detección de movimiento
            val thresholdUp = 9.0f
            val thresholdDown = -2.0f
            val thresholdLeft = 3.5f
            val thresholdRight = -3.5f

            if (isSoundActive) {
                when {
                    x > thresholdLeft -> mediaPlayerLeft?.start()
                    x < thresholdRight -> mediaPlayerRight?.start()
                    y > thresholdUp -> mediaPlayerUp?.start()
                    y < thresholdDown -> mediaPlayerDown?.start()
                    else -> {}
                }
            } else if (isMediaControlActive) {
                when {
                    x > thresholdLeft -> mediaController?.transportControls?.skipToPrevious()
                    x < thresholdRight -> mediaController?.transportControls?.skipToNext()
                    y > thresholdUp -> audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND)
                    y < thresholdDown -> audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND)
                    else -> {}
                }
            } else {
                // Modo sin funcionalidad activado
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No es necesario implementar esta función
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }
}
