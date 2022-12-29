package com.androidandrew.sunscreen.data.repository

import com.androidandrew.sunscreen.database.entity.UserTrackingEntity
import com.androidandrew.sunscreen.model.UserTracking

fun UserTrackingEntity.toModel(): UserTracking {
    return UserTracking(
        sunburnProgress = this.sunburnProgress,
        vitaminDProgress = this.vitaminDProgress
    )
}

fun UserTracking.toEntity(date: String): UserTrackingEntity {
    return UserTrackingEntity(
        date = date,
        sunburnProgress = this.sunburnProgress,
        vitaminDProgress = this.vitaminDProgress
    )
}