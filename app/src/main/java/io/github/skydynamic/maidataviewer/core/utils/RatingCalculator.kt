package io.github.skydynamic.maidataviewer.core.utils

fun getMultiplierFactor(achievement: Float): Double {
    return when (achievement) {
        in 10.0000..19.9999 -> 1.6 // D
        in 20.0000..29.9999 -> 3.2 // D
        in 30.0000..39.9999 -> 4.8 // D
        in 40.0000..49.9999 -> 6.4 // D
        in 50.0000..59.9999 -> 8.0 // C
        in 60.0000..69.9999 -> 9.6 // B
        in 70.0000..74.9999 -> 11.2 // BB
        in 75.0000..79.9999 -> 12.0 // BBB
        in 80.0000..89.9999 -> 12.8 // A
        in 90.0000..93.9999 -> 15.2 // AA
        in 94.0000..96.9998 -> 16.8 // AAA
        in 96.9999..96.9999 -> 17.6 // AAA
        in 97.0000..97.9999 -> 20.0 // S
        in 98.0000..98.9998 -> 20.3 // S+
        in 98.9999..98.9999 -> 20.6 // S+
        in 99.0000..99.4999 -> 20.8 // SS
        in 99.5000..99.9998 -> 21.1 // SS+
        in 99.9999..99.9999 -> 21.4 // SSS+
        in 100.0000..100.4998 -> 21.6 // SSS
        in 100.4999..100.4999 -> 22.2 // SSS
        in 100.5000..101.0000 -> 22.4 // SSS+
        else -> 0.000 // < 10.0000%
    }
}

fun getRank(achievement: Float): String {
    return when (achievement) {
        in 00.0000..49.9999 -> "D"
        in 50.0000..59.9999 -> "C"
        in 60.0000..69.9999 -> "B"
        in 70.0000..74.9999 -> "BB"
        in 75.0000..79.9999 -> "BBB"
        in 80.0000..89.9999 -> "A"
        in 90.0000..93.9999 -> "AA"
        in 94.0000..96.9999 -> "AAA"
        in 97.0000..97.9999 -> "S"
        in 98.0000..98.9999 -> "S+"
        in 99.0000..99.4999 -> "SS"
        in 99.5000..99.9999 -> "SS+"
        in 100.0000..100.4999 -> "SSS"
        in 100.5000..101.0000 -> "SSS+"
        else -> "D"
    }
}

fun calcRating(musicLevel: Float, achievement: Float): Int {
    var achievement = achievement
    val multiplierFactor = getMultiplierFactor(achievement)
    if (achievement >= 100.5) achievement = 100.5F
    return (musicLevel * multiplierFactor * achievement / 100).toInt()
}
