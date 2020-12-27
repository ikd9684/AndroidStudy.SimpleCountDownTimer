package jp.co.excite_software.s_ikeda.simple_countdown_timer

import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import jp.co.excite_software.s_ikeda.simple_countdown_timer.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {
    enum class TimerStatus {
        Start,
        Stop,
        Alarm,
    }

    companion object {
        private const val SECOND: Long = 1000
    }

    private lateinit var binding: ActivityMainBinding

    private lateinit var soundPool: SoundPool
    private var soundSingle = 0
    private var soundAlarm = 0
    private var streamId: Int? = null

    private val uiHandler = Handler(Looper.getMainLooper())

    private var timer: Timer? = null
    private var status: TimerStatus = TimerStatus.Stop
    private var aSetSeconds: Int = 0
    private var alarmTime: Long = 0
    private var remaining: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this

        binding.buttonStartStop.text = getString(R.string.button_label_start)
        binding.buttonStartStop.setOnClickListener {
            // play(ロードしたID, 左音量, 右音量, 優先度, ループ, 再生速度)
            soundPool.play(soundSingle, 1.0f, 1.0f, 0, 0, 1.0f)

            when (status) {
                TimerStatus.Stop -> {
                    start()
                }
                TimerStatus.Start -> {
                    stop()
                }
                TimerStatus.Alarm -> {
                    ok()
                }
            }
        }

        binding.buttonStartStop.visibility = View.INVISIBLE
        initSound {
            updateUI {
                binding.buttonStartStop.visibility = View.VISIBLE
            }
        }

        initAlarmTime(6)
    }

    private fun updateUI(update: () -> Unit) {
        uiHandler.post(update)
    }

    private fun setTimeValue(time: Long) {
        val minutes: Long = time / SECOND / 60
        val seconds: Long = time / SECOND % 60
        val milliSec: Long = (time - (minutes * SECOND * 60) - (seconds * SECOND)) / 100
        updateUI {
            binding.textViewSeconds.text = getString(R.string.time_value_format, minutes, seconds)
            binding.textViewMilliSec.text = getString(R.string.msec_value_format, milliSec)
        }
    }

    private fun start() {
        updateUI {
            binding.buttonStartStop.text = getString(R.string.button_label_stop)
        }

        timer?.cancel()
        timer = Timer()
        alarmTime = if (alarmTime == 0L) {
            Date().time + (aSetSeconds * SECOND)
        } else {
            Date().time + remaining
        }
        timer?.schedule(object : TimerTask() {
            override fun run() {
                remaining = alarmTime - Date().time
                if (remaining <= SECOND) {
                    timer?.cancel()
                    setTimeValue(0)
                    alarm()
                } else {
                    setTimeValue(remaining)
                }
            }
        }, 0, 100)

        status = TimerStatus.Start
    }

    private fun alarm() {
        streamId = soundPool.play(soundAlarm, 1.0f, 1.0f, 0, 0, 1.0f)

        updateUI {
            binding.buttonStartStop.text = getString(R.string.button_label_ok)
        }

        alarmTime = 0L

        status = TimerStatus.Alarm
    }

    private fun ok() {
        updateUI {
            binding.buttonStartStop.text = getString(R.string.button_label_start)
        }
        streamId?.let { soundPool.stop(it) }
        streamId = null

        initAlarmTime()

        status = TimerStatus.Stop
    }

    private fun stop() {
        updateUI {
            binding.buttonStartStop.text = getString(R.string.button_label_start)
        }

        timer?.cancel()

        status = TimerStatus.Stop
    }

    private fun initAlarmTime(aSetSeconds: Int? = null) {
        aSetSeconds?.let {
            this.aSetSeconds = it
        }
        setTimeValue(this.aSetSeconds * SECOND)
    }

    private fun initSound(completion: () -> Unit) {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()

        soundPool = SoundPool.Builder()
            .setAudioAttributes(audioAttributes)
            .setMaxStreams(1)
            .build()

        soundSingle = soundPool.load(this, R.raw.kitchen_timer_single, 1)
        soundAlarm = soundPool.load(this, R.raw.kitchen_timer_alerm, 1)

        // load が終わったか確認する場合
        soundPool.setOnLoadCompleteListener { _, _, _ ->
            completion()
        }
    }
}
