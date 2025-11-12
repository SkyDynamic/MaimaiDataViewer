package io.github.skydynamic.maidataviewer.ui.page.treasurebox.collection

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import coil3.compose.rememberAsyncImagePainter
import io.github.skydynamic.maidataviewer.Application
import io.github.skydynamic.maidataviewer.R
import io.github.skydynamic.maidataviewer.core.data.MaimaiTitleData
import io.github.skydynamic.maidataviewer.core.noRippleClickable
import io.github.skydynamic.maidataviewer.core.strings
import io.github.skydynamic.maidataviewer.core.toFullWidth
import io.github.skydynamic.maidataviewer.core.utils.BitmapExtension.getBitmap
import io.github.skydynamic.maidataviewer.core.utils.SubMonitorPreviewGenerate
import io.github.skydynamic.maidataviewer.ui.AppNavController
import io.github.skydynamic.maidataviewer.ui.component.dialog.PreviewFullscreen
import kotlinx.coroutines.async
import java.io.ByteArrayOutputStream

internal object SubMonitorPreviewPageViewModel : ViewModel() {
    var playerName by mutableStateOf("")

    var previewBitmap by mutableStateOf<Bitmap?>(null)

    var playerNameFieldError by mutableStateOf(false)
}

@Composable
fun SubMonitorPreviewPage(
    onBackPressed: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var iconFile by remember { mutableStateOf(IconPageViewModel.pickItemFile) }
    var plateFile by remember { mutableStateOf(PlatePageViewModel.pickItemFile) }
    var frameFile by remember { mutableStateOf(FramePageViewModel.pickItemFile) }
    var title by remember { mutableStateOf(TitlePageViewModel.pickItem) }

    var showPreview by remember { mutableStateOf(false) }

    Surface {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackPressed) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = Icons.Default.Close.name,
                        modifier = Modifier.height(24.dp),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Text(
                    text = R.string.submonitor_page.strings,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            OutlinedTextField(
                value = SubMonitorPreviewPageViewModel.playerName,
                onValueChange = {
                    if (it.length > 8) {
                        SubMonitorPreviewPageViewModel.playerNameFieldError = true
                        return@OutlinedTextField
                    } else {
                        SubMonitorPreviewPageViewModel.playerNameFieldError = false
                        SubMonitorPreviewPageViewModel.playerName = it
                    }
                },
                label = { Text(text = R.string.player_name.strings) },
                modifier = Modifier
                    .fillMaxWidth(),
                singleLine = true,
                isError = SubMonitorPreviewPageViewModel.playerNameFieldError,
                supportingText = {
                    if (SubMonitorPreviewPageViewModel.playerNameFieldError) {
                        Text(text = R.string.player_name_field_error.strings)
                    }
                }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .fillMaxHeight()
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(Color.Gray, RoundedCornerShape(8.dp))
                            .align(Alignment.CenterHorizontally),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "NOT IMAGE PICKED",
                            modifier = Modifier
                                .fillMaxWidth(),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                        iconFile?.let {
                            val bitmap = it.getBitmap()!!.asImageBitmap()
                            Image(
                                bitmap = bitmap,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                            )
                        }
                    }

                    Button(
                        onClick = {
                            AppNavController.getInstance().navigate(
                                CollectionPages.ICON_PAGE.page + "?pickMode=true",
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Text(text = R.string.pick_icon.strings)
                    }
                }

                VerticalDivider(
                    modifier = Modifier.fillMaxHeight()
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(4.dp)
                ) {
                    val bgColor = if (plateFile != null) {
                        Color.Transparent
                    } else {
                        Color.Gray
                    }
                    Box(
                        modifier = Modifier
                            .height(60.dp)
                            .background(bgColor, RoundedCornerShape(8.dp))
                            .align(Alignment.CenterHorizontally),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "NOT IMAGE PICKED",
                            modifier = Modifier
                                .fillMaxWidth(),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                        plateFile?.let {
                            val bitmap = it.getBitmap()!!.asImageBitmap()
                            Image(
                                bitmap = bitmap,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                            )
                        }
                    }

                    Button(
                        onClick = {
                            AppNavController.getInstance().navigate(
                                CollectionPages.PLATE_PAGE.page + "?pickMode=true",
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Text(text = R.string.pick_plate.strings)
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .fillMaxHeight()
                        .padding(4.dp)
                ) {
                    val bgColor = if (frameFile != null) {
                        Color.Transparent
                    } else {
                        Color.Gray
                    }
                    Box(
                        modifier = Modifier
                            .height(60.dp)
                            .background(bgColor, RoundedCornerShape(8.dp))
                            .align(Alignment.CenterHorizontally),
                        contentAlignment = Alignment.Center
                    ) {
                        if (frameFile == null) {
                            Text(
                                text = "NOT IMAGE PICKED",
                                modifier = Modifier
                                    .fillMaxWidth(),
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center
                            )
                        }
                        frameFile?.let {
                            val bitmap = it.getBitmap()!!.asImageBitmap()
                            Image(
                                bitmap = bitmap,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                            )
                        }
                    }

                    Button(
                        onClick = {
                            AppNavController.getInstance().navigate(
                                CollectionPages.FRAME_PAGE.page + "?pickMode=true",
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Text(text = R.string.pick_frame.strings)
                    }
                }

                VerticalDivider(
                    modifier = Modifier.fillMaxHeight()
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(4.dp)
                ) {
                    val bgColor = if (title != null) {
                        Color.Transparent
                    } else {
                        Color.Gray
                    }

                    Box(
                        modifier = Modifier
                            .height(60.dp)
                            .background(bgColor, RoundedCornerShape(8.dp))
                            .align(Alignment.CenterHorizontally),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "NOT TITLE PICKED",
                            modifier = Modifier
                                .fillMaxWidth(),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                        title?.let { title ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painterResource(title.rareType.resId),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .size(36.dp)
                                )

                                Text(
                                    text = title.name ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = Color.Black,
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            AppNavController.getInstance().navigate(
                                CollectionPages.TITLE_PAGE.page + "?pickMode=true",
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Text(text = R.string.pick_title.strings)
                    }
                }
            }

            Button(
                onClick = {
                    scope.async {
                        SubMonitorPreviewPageViewModel.previewBitmap = SubMonitorPreviewGenerate(
                            SubMonitorPreviewPageViewModel.playerName.toFullWidth(),
                            iconFile,
                            plateFile,
                            frameFile,
                            title ?: MaimaiTitleData(
                                id = 0,
                                name = "NOT SELECTED",
                                normalText = "NOT SELECTED",
                                rareType = MaimaiTitleData.RareType.NORMAL
                            ),
                        ).generate()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(text = R.string.submit.strings)
            }

            if (SubMonitorPreviewPageViewModel.previewBitmap != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Image(
                        bitmap = SubMonitorPreviewPageViewModel.previewBitmap!!.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .noRippleClickable {
                                showPreview = true
                            }
                    )

                    Button(
                        onClick = {
                            val outputStream = ByteArrayOutputStream()
                            SubMonitorPreviewPageViewModel.previewBitmap!!.compress(
                                Bitmap.CompressFormat.PNG,
                                100,
                                outputStream
                            )
                            val bytes = outputStream.toByteArray()
                            Application.application.saveImageToGallery(
                                bytes,
                                "${System.currentTimeMillis()}_SubMonitorPreview.png"
                            )
                            outputStream.close()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(text = R.string.save.strings)
                    }
                }
            }
        }

        if (showPreview) {
            PreviewFullscreen(
                image = rememberAsyncImagePainter(
                    model = SubMonitorPreviewPageViewModel.previewBitmap
                )
            ) {
                showPreview = false
            }
        }
    }
}