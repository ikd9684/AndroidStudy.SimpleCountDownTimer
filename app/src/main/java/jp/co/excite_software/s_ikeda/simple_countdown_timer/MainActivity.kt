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

class MainActivity : AppCompatActivity() {

    private val alarmListener = object : CountDownTimer.AlarmListener {
        override fun alarm() {
            updateUI {
                binding.buttonStartStop.setImageResource(R.drawable.ic_baseline_replay_24)
            }
            streamId = soundPool.play(soundAlarm, 1.0f, 1.0f, 0, 0, 1.0f)
        }
    }

    private val timeValueListener = object : CountDownTimer.TimeValueListener {
        override fun updateTimeValue(minutes: Int, seconds: Int, milliSeconds: Int) {
            updateUI {
                binding.textViewSeconds.text =
                    getString(R.string.time_value_format, minutes, seconds)
                binding.textViewMilliSec.text =
                    getString(R.string.msec_value_format, milliSeconds / 100)
            }
        }
    }

    private var countDownTimer = CountDownTimer(timeValueListener, alarmListener)

    private lateinit var binding: ActivityMainBinding

    private lateinit var soundPool: SoundPool
    private var soundSingle = 0
    private var soundAlarm = 0
    private var streamId: Int? = null

    private val uiHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this

        binding.buttonStartStop.setImageResource(R.drawable.ic_baseline_play_arrow_24)
        binding.buttonStartStop.setOnClickListener {
            // play(ロードしたID, 左音量, 右音量, 優先度, ループ, 再生速度)
            soundPool.play(soundSingle, 1.0f, 1.0f, 0, 0, 1.0f)

            when (countDownTimer.status) {
                CountDownTimer.TimerStatus.Stop -> {
                    start()
                }
                CountDownTimer.TimerStatus.Start -> {
                    stop()
                }
                CountDownTimer.TimerStatus.Alarm -> {
                    reset()
                }
            }
        }

        binding.buttonStartStop.visibility = View.INVISIBLE
        initSound {
            updateUI {
                binding.buttonStartStop.visibility = View.VISIBLE
            }
        }

        countDownTimer.initAlarmTime(6)
    }

    private fun updateUI(update: () -> Unit) {
        uiHandler.post(update)
    }

    private fun start() {
        updateUI {
            binding.buttonStartStop.setImageResource(R.drawable.ic_baseline_pause_24)
        }
        countDownTimer.start()
    }

    private fun reset() {
        updateUI {
            binding.buttonStartStop.setImageResource(R.drawable.ic_baseline_play_arrow_24)
        }
        streamId?.let { soundPool.stop(it) }
        streamId = null

        countDownTimer.reset()
    }

    private fun stop() {
        updateUI {
            binding.buttonStartStop.setImageResource(R.drawable.ic_baseline_play_arrow_24)
        }
        countDownTimer.stop()
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
