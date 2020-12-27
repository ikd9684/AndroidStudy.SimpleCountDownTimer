package jp.co.excite_software.s_ikeda.simple_countdown_timer

import java.util.*

class CountDownTimer(
        private val timeValueListener: TimeValueListener,
        private val alarmListener: AlarmListener
) {

    interface TimeValueListener {
        fun updateTimeValue(minutes: Int, seconds: Int, milliSeconds: Int)
    }

    interface AlarmListener {
        fun alarm()
    }

    enum class TimerStatus {
        Start,
        Stop,
        Alarm,
    }

    companion object {
        const val SECOND: Long = 1000
    }

    var status: TimerStatus = TimerStatus.Stop

    private var timer: Timer? = null
    private var aSetSeconds: Int = 0
        set(value) {
            field = value
            alarmTime = 0L
        }
    private var alarmTime: Long = 0
    private var remaining: Long = 0

    val minutes: Int
        get() = if (alarmTime == 0L) {
            aSetSeconds / 60
        } else {
            (remaining / SECOND / 60).toInt()
        }
    val seconds: Int
        get() = if (alarmTime == 0L) {
            aSetSeconds % 60
        } else {
            (remaining / SECOND % 60).toInt()
        }

    fun start() {
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
        alarmTime = 0L
        status = TimerStatus.Alarm
        alarmListener.alarm()
    }

    fun reset() {
        initAlarmTime()
        status = TimerStatus.Stop
    }

    fun stop() {
        timer?.cancel()
        status = TimerStatus.Stop
    }

    fun initAlarmTime(aSetSeconds: Int? = null) {
        aSetSeconds?.let {
            this.aSetSeconds = it
        }
        setTimeValue(this.aSetSeconds * SECOND)
    }

    private fun setTimeValue(time: Long) {
        val minutes: Int = (time / SECOND / 60).toInt()
        val seconds: Int = (time / SECOND % 60).toInt()
        val milliSec: Int = (time - (minutes * SECOND * 60) - (seconds * SECOND)).toInt()

        timeValueListener.updateTimeValue(minutes, seconds, milliSec)
    }
}
