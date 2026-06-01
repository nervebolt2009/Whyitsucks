package com.apexmusic

import android.app.Application
import com.apexmusic.data.db.AppDatabase
import com.apexmusic.data.repository.MusicRepository

class ApexMusicApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val repository: MusicRepository by lazy { MusicRepository(this, database) }

    override fun onCreate() {
        super.onCreate()
    }
}
