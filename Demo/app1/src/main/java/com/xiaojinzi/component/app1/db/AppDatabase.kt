package com.xiaojinzi.component.app1.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [VideoProject::class],
    version = 1,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun videoProjectDao(): VideoProjectDao

}