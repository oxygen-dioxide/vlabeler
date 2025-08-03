package com.sdercolin.vlabeler.ui.common

import androidx.compose.foundation.ContextMenuItem
import androidx.compose.runtime.Composable

interface ContextMenuAction<T : ContextMenuAction<T>> {

    @Composable
    fun toContextMenuItem(onClick: (T) -> Unit): ContextMenuItem
}

interface NoOpContextMenuAction : ContextMenuAction<NoOpContextMenuAction> {
    // for cases where no context menu is needed
}
