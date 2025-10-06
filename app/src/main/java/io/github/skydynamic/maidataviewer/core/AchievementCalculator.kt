package io.github.skydynamic.maidataviewer.core

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import io.github.skydynamic.maidataviewer.core.data.MaimaiMusicData
import io.github.skydynamic.maidataviewer.ui.component.BasicDataTableRow
import io.github.skydynamic.maidataviewer.ui.component.DataTableRowStyle
import io.github.skydynamic.maidataviewer.ui.component.DataTableRowValue

class AchievementCalculator(
    val notes: MaimaiMusicData.MaimaiMusicDifficultyNotesData
) {
    data class AchievementData(
        val total: Int,
        val tap: NormalNoteScores,
        val hold: NormalNoteScores,
        val slide: NormalNoteScores,
        val touch: NormalNoteScores,
        val breakNote: BreakNoteScores
    )

    private data class BreakScores(
        val score: Int,
        val bonus: Int
    )

    data class NormalNoteScores(
        var total: Int,
        val perfect: Double,
        val great: Double,
        val good: Double,
        val miss: Double
    )

    data class BreakNoteScores(
        val total: Int,
        val perfect: Perfect,
        val great: Great,
        val good: Double,
        val miss: Double
    ) {
        data class Perfect(
            val critical: Double,
            val fast: Double,
            val late: Double
        )

        data class Great(
            val fast1: Double,
            val fast2: Double,
            val late: Double
        )
    }

    private val tapScore = listOf(0, 250, 400, 500)
    private val holdScore = listOf(0, 500, 800, 1000)
    private val slideScore = listOf(0, 750, 1200, 1500)

    private val breakScores = mapOf(
        "CP" to BreakScores(2500, 100),
        "P1" to BreakScores(2500, 75),
        "P2" to BreakScores(2500, 50),
        "G1" to BreakScores(2000, 40),
        "G2" to BreakScores(1500, 40),
        "G3" to BreakScores(1250, 40),
        "GOOD" to BreakScores(1000, 30),
        "MISS" to BreakScores(0, 0)
    )

    private val totalScore: Int
    private val totalBonus: Int

    init {
        totalScore = calcTotalScore()
        totalBonus = calcTotalBonus()
    }

    private fun calcTotalScore(): Int {
        return (
                notes.tap * tapScore[3] +
                        notes.hold * holdScore[3] +
                        notes.slide * slideScore[3] +
                        notes.touch * tapScore[3] +
                        notes.breakNote * (breakScores["CP"]?.score ?: 0)
                )
    }

    private fun calcTotalBonus(): Int {
        return notes.breakNote * (breakScores["CP"]?.bonus ?: 0)
    }

    private fun calcTapScore(): NormalNoteScores {
        val base = tapScore[3].toDouble() / totalScore.toDouble() * 100
        return NormalNoteScores(
            notes.tap,
            0.0,
            (tapScore[2] - tapScore[3]).toDouble() / totalScore.toDouble() * 100,
            (tapScore[1] - tapScore[3]).toDouble() / totalScore.toDouble() * 100,
            -base
        )
    }

    private fun calcHoldScore(): NormalNoteScores {
        val base = holdScore[3].toDouble() / totalScore.toDouble() * 100
        return NormalNoteScores(
            notes.hold,
            0.0,
            (holdScore[2] - holdScore[3]).toDouble() / totalScore.toDouble() * 100,
            (holdScore[1] - holdScore[3]).toDouble() / totalScore.toDouble() * 100,
            -base
        )
    }

    private fun calcSlideScore(): NormalNoteScores {
        val base = slideScore[3].toDouble() / totalScore.toDouble() * 100
        return NormalNoteScores(
            notes.slide,
            0.0,
            (slideScore[2] - slideScore[3]).toDouble() / totalScore.toDouble() * 100,
            (slideScore[1] - slideScore[3]).toDouble() / totalScore.toDouble() * 100,
            -base
        )
    }

    private fun calcTouchScore(): NormalNoteScores {
        if (notes.touch == 0) {
            return NormalNoteScores(
                0,
                0.0,
                0.0,
                0.0,
                0.0
            )
        }
        return calcTapScore().apply { total = notes.touch }
    }

    private fun calcBreakScore(): BreakNoteScores {
        val cpScore = breakScores["CP"]
        val p1Score = breakScores["P1"]
        val p2Score = breakScores["P2"]
        val g1Score = breakScores["G1"]
        val g2Score = breakScores["G2"]
        val g3Score = breakScores["G3"]
        val goodScore = breakScores["GOOD"]

        if (cpScore == null || p1Score == null || p2Score == null ||
            g1Score == null || g2Score == null || g3Score == null ||
            goodScore == null
        ) {
            return BreakNoteScores(
                0,
                BreakNoteScores.Perfect(0.0, 0.0, 0.0),
                BreakNoteScores.Great(0.0, 0.0, 0.0),
                0.0,
                0.0
            )
        }

        val base = cpScore.score.toDouble() / totalScore.toDouble() * 100
        val maxExtra = cpScore.bonus.toDouble() / totalBonus.toDouble()

        val fpExtra = p1Score.bonus.toDouble() / totalBonus.toDouble()
        val lpExtra = p2Score.bonus.toDouble() / totalBonus.toDouble()

        val g1 = g1Score.score.toDouble() / totalScore.toDouble() * 100
        val g2 = g2Score.score.toDouble() / totalScore.toDouble() * 100
        val g3 = g3Score.score.toDouble() / totalScore.toDouble() * 100

        val greatExtra = g1Score.bonus.toDouble() / totalBonus.toDouble()

        val good = goodScore.score.toDouble() / totalScore.toDouble() * 100
        val goodExtra = goodScore.bonus.toDouble() / totalBonus.toDouble()

        return BreakNoteScores(
            notes.breakNote,
            BreakNoteScores.Perfect(
                0.0,
                fpExtra - maxExtra,
                lpExtra - maxExtra
            ),
            BreakNoteScores.Great(
                (g1 - base) + (greatExtra - maxExtra),
                (g2 - base) + (greatExtra - maxExtra),
                (g3 - base) + (greatExtra - maxExtra)
            ),
            (good - base) + (goodExtra - maxExtra),
            -(base + maxExtra)
        )
    }

    fun getResult(): AchievementData {
        return AchievementData(
            notes.total,
            calcTapScore(),
            calcHoldScore(),
            calcSlideScore(),
            calcTouchScore(),
            calcBreakScore()
        )
    }
}

object ExtensionMethods {
    fun AchievementCalculator.NormalNoteScores.toList(): List<Double> {
        return listOf(
            perfect,
            great,
            good,
            miss
        )
    }

    fun AchievementCalculator.BreakNoteScores.buildDataTableRow(): List<DataTableRowValue> {
        return listOf(
            DataTableRowValue(
                value = @Composable {
                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = this@buildDataTableRow.perfect.critical.toAchievementString(),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = Color(0xFFFFCA28),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = this@buildDataTableRow.perfect.fast.toAchievementString(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF42A5F5),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = this@buildDataTableRow.perfect.late.toAchievementString(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFEC407A),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            autoSize = TextAutoSize.StepBased(minFontSize = 6.sp, maxFontSize = 14.sp)
                        )
                    }
                },
                style = DataTableRowStyle(
                    textAlign = TextAlign.Center
                )
            ),
            DataTableRowValue(
                value = """
                ${this@buildDataTableRow.great.fast1.toAchievementString()}
                ${this@buildDataTableRow.great.fast2.toAchievementString()}
                ${this@buildDataTableRow.great.late.toAchievementString()}
            """.trimIndent(),
                style = DataTableRowStyle(
                    textAlign = TextAlign.Center
                )
            ),
            DataTableRowValue(
                value = this@buildDataTableRow.good.toAchievementString(),
                style = DataTableRowStyle(
                    textAlign = TextAlign.Center
                )
            ),
            DataTableRowValue(
                value = this@buildDataTableRow.miss.toAchievementString(),
                style = DataTableRowStyle(
                    textAlign = TextAlign.Center
                )
            )
        )
    }

    fun AchievementCalculator.AchievementData.buildDataTableRow(): List<BasicDataTableRow> {
        val nullRowValue = DataTableRowValue(
            value = "-",
            style = DataTableRowStyle(
                textAlign = TextAlign.Center
            )
        )

        val totalRow = BasicDataTableRow(
            values = listOf(
                DataTableRowValue("Total"),
                DataTableRowValue(
                    value = this@buildDataTableRow.total.toString(),
                    style = DataTableRowStyle(
                        textAlign = TextAlign.Center
                    )
                ),
                nullRowValue,
                nullRowValue,
                nullRowValue,
                nullRowValue
            )
        )

        val tapValue = arrayListOf(
            DataTableRowValue("Tap"),
            DataTableRowValue(
                value = tap.total.toString(),
                style = DataTableRowStyle(
                    textAlign = TextAlign.Center
                )
            )
        )
        tapValue.addAll(tap.toList().map {
            DataTableRowValue(
                value = it.toAchievementString(),
                style = DataTableRowStyle(
                    textAlign = TextAlign.Center
                )
            )
        })

        val tapRow = BasicDataTableRow(
            values = tapValue
        )

        val holdValue = arrayListOf(
            DataTableRowValue("Hold"),
            DataTableRowValue(
                value = hold.total.toString(),
                style = DataTableRowStyle(
                    textAlign = TextAlign.Center
                )
            )
        )

        holdValue.addAll(hold.toList().map {
            DataTableRowValue(
                value = it.toAchievementString(),
                style = DataTableRowStyle(
                    textAlign = TextAlign.Center
                )
            )
        })

        val holdRow = BasicDataTableRow(
            values = holdValue
        )

        val slideValue = arrayListOf(
            DataTableRowValue("Slide"),
            DataTableRowValue(
                value = slide.total.toString(),
                style = DataTableRowStyle(
                    textAlign = TextAlign.Center
                )
            )
        )

        slideValue.addAll(slide.toList().map {
            DataTableRowValue(
                value = it.toAchievementString(),
                style = DataTableRowStyle(
                    textAlign = TextAlign.Center
                )
            )
        })

        val slideRow = BasicDataTableRow(
            values = slideValue
        )

        val touchValue = arrayListOf(
            DataTableRowValue("Touch"),
            DataTableRowValue(
                value = if (touch.total != 0) touch.total.toString() else "-",
                style = DataTableRowStyle(
                    textAlign = TextAlign.Center
                )
            )
        )

        if (touch.total == 0) {
            touchValue.addAll(
                listOf(
                    nullRowValue,
                    nullRowValue,
                    nullRowValue,
                    nullRowValue
                )
            )
        } else {
            touchValue.addAll(touch.toList().map {
                DataTableRowValue(
                    value = it.toAchievementString(),
                    style = DataTableRowStyle(
                        textAlign = TextAlign.Center
                    )
                )
            })
        }

        val touchRow = BasicDataTableRow(
            values = touchValue
        )

        val breakNoteValue = arrayListOf(
            DataTableRowValue("Break"),
            DataTableRowValue(
                value = breakNote.total.toString(),
                style = DataTableRowStyle(
                    textAlign = TextAlign.Center
                )
            )
        )

        breakNoteValue.addAll(breakNote.buildDataTableRow())

        val breakNoteRow = BasicDataTableRow(
            values = breakNoteValue,
            height = 80
        )

        return listOf(
            totalRow,
            tapRow,
            holdRow,
            slideRow,
            touchRow,
            breakNoteRow
        )
    }
}
