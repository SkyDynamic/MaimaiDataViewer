package io.github.skydynamic.maidataviewer.ui.page


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.github.skydynamic.maidataviewer.R
import io.github.skydynamic.maidataviewer.core.config.AppConfig
import io.github.skydynamic.maidataviewer.core.network.ResourceNode
import io.github.skydynamic.maidataviewer.core.strings
import io.github.skydynamic.maidataviewer.ui.component.dialog.ConfirmDialog
import io.github.skydynamic.maidataviewer.ui.page.setting.TextButtonSettingItem
import kotlinx.coroutines.launch

@Composable
fun SettingPage() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val preferencesDataStore = remember(context) { AppConfig(context) }

    val chooseNode = remember { mutableStateOf(ResourceNode.DIRECT) }
    val customNodeUrl = remember { mutableStateOf("") }
    val showChooseNodeDialog = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        preferencesDataStore.read {
            it[AppConfig.chooseNode] ?: ""
        }.collect {
            chooseNode.value = ResourceNode.getNode(it)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextButtonSettingItem(
            title = R.string.resource_node.strings,
            description = R.string.resource_node_desc.strings,
            selectedValueStr = chooseNode.value.nodeName,
            onClick = { showChooseNodeDialog.value = true }
        )
    }

    when {
        showChooseNodeDialog.value -> {
            ConfirmDialog(
                title = R.string.resource_node.strings,
                onConfirm = {
                    coroutineScope.launch {
                        preferencesDataStore.update {
                            it[AppConfig.chooseNode] = chooseNode.value.nodeName
                        }
                    }.invokeOnCompletion {
                        showChooseNodeDialog.value = false
                    }
                },
                onDismiss = {
                    showChooseNodeDialog.value = false
                },
            ) {
                Column(
                    modifier = Modifier
                        .selectableGroup()
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    ResourceNode.entries.forEach { node ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .selectable(
                                    selected = (node == chooseNode.value),
                                    onClick = { chooseNode.value = node },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (node == chooseNode.value),
                                onClick = null
                            )

                            Text(
                                text = node.nodeName,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }

                    if (chooseNode.value == ResourceNode.CUSTOM) {
                        OutlinedTextField(
                            value = customNodeUrl.value,
                            onValueChange = { customNodeUrl.value = it },
                            placeholder = { Text(R.string.custom_node_url.strings) },
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}