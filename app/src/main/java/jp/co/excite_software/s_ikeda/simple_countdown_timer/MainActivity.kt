package jp.co.excite_software.s_ikeda.simple_countdown_timer

import android.app.Dialog
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.NumberPicker
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import jp.co.excite_software.s_ikeda.simple_countdown_timer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val alarmListener = object : CountDownTimer.AlarmListener {
        override fun alarm() {
            updateUI {
                binding.buttonStartStop.setImageResource(R.drawable.ic_baseline_replay_24)
            }
            playSound(soundAlarm)
        }
    }

    private val timeValueListener = object : CountDownTimer.TimeValueListener {
        override fun updateTimeValue(minutes: Int, seconds: Int, milliSeconds: Int) {
            updateUI {
                binding.textViewTime.text = getString(R.string.time_value_format, minutes, seconds)
                binding.textViewMilliSec.text = getString(R.string.msec_value_format, milliSeconds / 100)
            }
        }
    }

    private var countDownTimer = CountDownTimer(timeValueListener, alarmListener)

    private lateinit var binding: ActivityMainBinding

    private lateinit var soundPool: SoundPool
    private var soundSingle = 0
    private var soundAlarm = 0
    private var soundClick = 0
    private var streamId: Int? = null

    private val uiHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this

        binding.textViewTime.setOnClickListener {
            val dialogFragment = ASetTimerDialog(
                    countDownTimer.minutes,
                    countDownTimer.seconds,
                    { minutes, seconds ->
                        playSound(soundSingle)
                        countDownTimer.initAlarmTime(minutes * 60 + seconds)
                    }, { _, _, _ ->
                playSound(soundClick)
            }, { _, _, _ ->
                playSound(soundClick)
            })
            dialogFragment.show(supportFragmentManager, "fragment_dialog")
        }

        binding.buttonStartStop.setImageResource(R.drawable.ic_baseline_play_arrow_24)
        binding.buttonStartStop.setOnClickListener {
            // play(ロードしたID, 左音量, 右音量, 優先度, ループ, 再生速度)
            playSound(soundSingle)

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

        countDownTimer.initAlarmTime(3 * 60)
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

        /* 効果音のダウンロード元
         * https://taira-komori.jpn.org/quick/quick.cgi?mode=find&word=%83A%83%89%81%5B%83%80
         */
        soundSingle = soundPool.load(this, R.raw.kitchen_timer_single, 1)
        soundAlarm = soundPool.load(this, R.raw.kitchen_timer_alerm, 1)
        soundClick = soundPool.load(this, R.raw.click, 1)

        // load が終わったか確認する場合
        soundPool.setOnLoadCompleteListener { _, _, _ ->
            completion()
        }
    }

    private fun playSound(
            soundId: Int,
            volume: Float = 1.0f,
            loop: Int = 0,
            rate: Float = 1.0f
    ): Int {
        return soundPool.play(soundId, volume, volume, 0, loop, rate)
    }

    class ASetTimerDialog(
            private val minutes: Int,
            private val seconds: Int,
            private val applyEvent: (minutes: Int, seconds: Int) -> Unit,
            private val onChangeMinutes: ((picker: NumberPicker, oldVal: Int, newVal: Int) -> Unit)? = null,
            private val onChangeSeconds: ((picker: NumberPicker, oldVal: Int, newVal: Int) -> Unit)? = null
    ) :
            CustomDialogFragment(R.layout.layout_a_set_time_dialog) {

        private lateinit var buttonCancel: ImageButton
        private lateinit var buttonApply: ImageButton
        private lateinit var pickerMinutes: NumberPicker
        private lateinit var pickerSeconds: NumberPicker

        override fun onViewCreated(dialog: Dialog, view: View) {
            buttonCancel = view.findViewById(R.id.buttonCancel)
            buttonApply = view.findViewById(R.id.buttonApply)
            pickerMinutes = view.findViewById(R.id.pickerMinutes)
            pickerSeconds = view.findViewById(R.id.pickerSeconds)

            buttonCancel.setOnClickListener {
                dialog.dismiss()
            }

            buttonApply.setOnClickListener {
                applyEvent(pickerMinutes.value, pickerSeconds.value)
                dialog.dismiss()
            }

            pickerMinutes.apply {
                minValue = 0
                maxValue = 99
                value = minutes
                displayedValues = Array(maxValue + 1) { String.format("%02d", it) }
                setOnValueChangedListener { picker, oldVal, newVal ->
                    onChangeMinutes?.invoke(picker, oldVal, newVal)
                }
            }

            pickerSeconds.apply {
                minValue = 0
                maxValue = 59
                value = seconds
                displayedValues = Array(maxValue + 1) { String.format("%02d", it) }
                setOnValueChangedListener { picker, oldVal, newVal ->
                    onChangeSeconds?.invoke(picker, oldVal, newVal)
                }
            }
        }
    }
}
