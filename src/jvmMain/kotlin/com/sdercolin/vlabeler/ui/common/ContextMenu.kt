package com.sdercolin.vlabeler.ui.common

import androidx.compose.foundation.ContextMenuArea
import androidx.compose.runtime.Composable

@Composable
fun <T : ContextMenuAction<T>> WithContextMenu(
    items: @Composable () -> List<T>,
    consumer: ((T) -> Unit)?,
    content: @Composable () -> Unit,
) {
    val items = if (consumer != null) {
        items().map { it.toContextMenuItem(consumer) }
    } else {
        emptyList()
    }
    ContextMenuArea(
        items = { items },
        content = content,
    )
}
