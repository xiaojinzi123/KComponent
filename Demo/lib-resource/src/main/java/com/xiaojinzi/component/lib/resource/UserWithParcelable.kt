package com.xiaojinzi.component.lib.resource

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserWithParcelable(
    val name: String
) : Parcelable
