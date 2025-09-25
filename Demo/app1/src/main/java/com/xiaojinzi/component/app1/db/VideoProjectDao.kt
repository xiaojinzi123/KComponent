package com.xiaojinzi.component.app1.db

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class VideoProject(
    @PrimaryKey(
        autoGenerate = true,
    ) val uid: Int,
    @ColumnInfo(name = "first_name")
    val firstName: String?,
)

@Dao
interface VideoProjectDao {
}