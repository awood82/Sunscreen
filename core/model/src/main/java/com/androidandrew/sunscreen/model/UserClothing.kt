package com.androidandrew.sunscreen.model

val defaultTop = ClothingTop.T_SHIRT
val defaultBottom = ClothingBottom.SHORTS
val defaultUserClothing = UserClothing(top = defaultTop, bottom = defaultBottom)

data class UserClothing(
    val top: ClothingTop,
    val bottom: ClothingBottom
)

fun UserClothing.toDatabaseValue(): Int {
    return top.dbValue * 10 + bottom.dbValue
}

fun Int.toUserClothing(): UserClothing {
    return UserClothing(
        top = ClothingTop.from(this / 10),
        bottom = ClothingBottom.from(this % 10)
    )
}

interface ClothingRegion

enum class ClothingTop(val dbValue: Int) : ClothingRegion {
    NOTHING(0),
    T_SHIRT(1),
    LONG_SLEEVE_SHIRT(2);

    companion object {
        infix fun from(value: Int): ClothingTop = ClothingTop.values().firstOrNull { it.dbValue == value } ?: defaultTop
    }
}

enum class ClothingBottom(val dbValue: Int) : ClothingRegion {
    NOTHING(0),
    SHORTS(1),
    PANTS(2);

    companion object {
        infix fun from(value: Int): ClothingBottom = ClothingBottom.values().firstOrNull { it.dbValue == value } ?: defaultBottom
    }
}