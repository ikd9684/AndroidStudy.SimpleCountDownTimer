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
    private var aSetTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this

        binding.textViewTime.text = getString(R.string.zero_time_value)

        aSetTime = 6 * SECOND
        setTimeValue(aSetTime)

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
            uiUpdate {
                binding.buttonStartStop.visibility = View.VISIBLE
            }
        }
    }

    private fun uiUpdate(update: () -> Unit) {
        uiHandler.post(update)
    }

    private fun setTimeValue(time: Long) {
        val minutes: Long = time / SECOND / 60
        val seconds: Long = time / SECOND % 60
        uiUpdate {
            binding.textViewTime.text = getString(R.string.time_value_format, minutes, seconds)
        }
    }

    private fun ok() {
        uiUpdate {
            binding.buttonStartStop.text = getString(R.string.button_label_start)
        }
        streamId?.let { soundPool.stop(it) }
        streamId = null

        status = TimerStatus.Stop
    }

    private fun start() {
        uiUpdate {
            binding.buttonStartStop.text = getString(R.string.button_label_stop)
        }

        timer?.cancel()
        timer = Timer()
        val startTime = Date().time + aSetTime + SECOND
        timer?.schedule(object : TimerTask() {
            override fun run() {
                aSetTime = startTime - Date().time
                if (aSetTime <= SECOND) {
                    timer?.cancel()
                    setTimeValue(0)
                    alarm()
                } else {
                    setTimeValue(aSetTime)
                }
            }
        }, 0, 100)

        status = TimerStatus.Start
    }

    private fun alarm() {
        streamId = soundPool.play(soundAlarm, 1.0f, 1.0f, 0, 0, 1.0f)

        uiUpdate {
            binding.buttonStartStop.text = getString(R.string.button_label_ok)
        }

        status = TimerStatus.Alarm
    }

    private fun stop() {
        uiUpdate {
            binding.buttonStartStop.text = getString(R.string.button_label_start)
        }

        timer?.cancel()

        status = TimerStatus.Stop
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
