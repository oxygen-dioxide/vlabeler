package com.sdercolin.vlabeler.ui.common

import androidx.compose.runtime.Composable
import com.sdercolin.vlabeler.util.Indexed

interface ContextMenuSubject<T : ContextMenuAction<T>> : Indexed {

    @Composable
    fun getContextMenuActions(): List<T>
}
