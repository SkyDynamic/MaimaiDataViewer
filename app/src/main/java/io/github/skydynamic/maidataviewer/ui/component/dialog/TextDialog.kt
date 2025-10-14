package io.github.skydynamic.maidataviewer.ui.component.dialog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun TextDialog(
    text: String,
    textAlign: TextAlign = TextAlign.Center,
    fontWeight: FontWeight = FontWeight.Normal,
    fontStyle: FontStyle = FontStyle.Normal,
    onDismiss: () -> Unit
) {
    CommonDialog(
        onDismiss = onDismiss
    ) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top=4.dp, bottom=16.dp, start=16.dp, end=16.dp),
            textAlign = textAlign,
            fontWeight = fontWeight,
            fontStyle = fontStyle
        )
    }
}