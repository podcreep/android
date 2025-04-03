package com.podcreep.mobile.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "settings",
    indices = [Index("name")])
data class Setting(
    @PrimaryKey var name: String,
    var intValue: Long?,
    var stringValue: String?)
