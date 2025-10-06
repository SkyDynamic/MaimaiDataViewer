package io.github.skydynamic.maidataviewer.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import io.github.skydynamic.maidataviewer.ui.component.BasicDataTableRow
import io.github.skydynamic.maidataviewer.ui.component.DataTableColumn

object AchievementDataTablePageViewModel : ViewModel() {
    var columns by mutableStateOf(emptyList<DataTableColumn>())
    var rows by mutableStateOf(emptyList<BasicDataTableRow>())
}