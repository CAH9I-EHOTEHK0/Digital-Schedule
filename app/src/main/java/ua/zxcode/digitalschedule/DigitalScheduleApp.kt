package ua.zxcode.digitalschedule

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen

class DigitalScheduleApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
    }
}

