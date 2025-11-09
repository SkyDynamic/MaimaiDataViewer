package io.github.skydynamic.maidataviewer.ui.page.treasurebox.collection

import androidx.compose.runtime.Composable
import io.github.skydynamic.maidataviewer.R
import io.github.skydynamic.maidataviewer.core.getString

enum class CollectionPages(
    val pageName: String,
    val desc: String,
    var page: String,
    val pageComposable: @Composable (
            backPressed: () -> Any
    ) -> Unit
) {
    TITLE_PAGE(
        R.string.title_page.getString(),
        R.string.title_page_desc.getString(),
        "titlePage",
        { backPressed ->
            TitlePage {
                backPressed()
            }
        }
    ),
    ICON_PAGE(
        R.string.icon_page.getString(),
        R.string.icon_page_desc.getString(),
        "iconPage",
        { backPressed ->
            IconPage {
                backPressed()
            }
        }
    ),
    PLATE_PAGE(
        R.string.plate_page.getString(),
        R.string.plate_page_desc.getString(),
        "platePage",
        { backPressed ->
            PlatePage {
                backPressed()
            }
        }
    )
}