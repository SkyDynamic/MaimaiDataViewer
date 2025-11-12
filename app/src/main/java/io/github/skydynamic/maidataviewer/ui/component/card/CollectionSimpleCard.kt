package io.github.skydynamic.maidataviewer.ui.component.card

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import io.github.skydynamic.maidataviewer.core.data.MaimaiCommonCollectionData
import io.github.skydynamic.maidataviewer.core.manager.resource.MaimaiResourceManager
import io.github.skydynamic.maidataviewer.ui.component.dialog.PreviewFullscreen
import io.github.skydynamic.maidataviewer.ui.page.treasurebox.collection.PlatePageViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun CollectionSimpleCard(
    manager: MaimaiResourceManager,
    collectionData: MaimaiCommonCollectionData
) {
    val defaultImageByte = remember { manager.getResByteFromAssets(0) }
    var imageFile by remember { mutableStateOf<File?>(null) }

    var showPreview by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        PlatePageViewModel.viewModelScope.launch(Dispatchers.IO) {
            imageFile = try {
                manager.getResFile(collectionData.id)
            } catch (_: Exception) {
                null
            }
        }
    }

    val image = rememberAsyncImagePainter(
        ImageRequest.Builder(LocalContext.current)
            .data(imageFile ?: defaultImageByte)
            .crossfade(true)
            .build()
    )

    ShadowElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        clickable = { showPreview = true }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = image,
                contentDescription = null,
                modifier = Modifier
                    .height(64.dp)
            )

            Text(
                text = collectionData.name ?: "",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleSmall
            )

            Text(
                text = "(${collectionData.normalText})",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showPreview) {
        PreviewFullscreen(
            image = image,
            onDismiss = { showPreview = false }
        )
    }
}