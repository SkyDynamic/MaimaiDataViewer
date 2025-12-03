package io.github.skydynamic.maidataviewer.ui.page.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SwitchSettingItem(
    title: String,
    description: String,
    checked: MutableState<Boolean>,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(16.dp)
    ) {
        BaseTextItem(
            modifier = Modifier
                .fillMaxWidth(0.7f),
            title = title,
            description = description
        )

        Spacer(modifier = Modifier.weight(1.0f))

        Switch(
            checked = checked.value,
            onCheckedChange = {
                checked.value = !checked.value
                onCheckedChange(checked.value)
            },
            thumbContent = if (checked.value) {
                {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier
                            .size(SwitchDefaults.IconSize),
                    )
                }
            } else {
                null
            }
        )
    }
}