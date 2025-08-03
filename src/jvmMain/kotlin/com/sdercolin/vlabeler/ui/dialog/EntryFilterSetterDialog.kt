package com.sdercolin.vlabeler.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sdercolin.vlabeler.env.Log
import com.sdercolin.vlabeler.model.Entry
import com.sdercolin.vlabeler.model.EntrySelector
import com.sdercolin.vlabeler.model.LabelerConf
import com.sdercolin.vlabeler.model.filter.EntryFilter
import com.sdercolin.vlabeler.ui.common.ConfirmButton
import com.sdercolin.vlabeler.ui.common.DoneTriStateIcon
import com.sdercolin.vlabeler.ui.common.HeaderFooterColumn
import com.sdercolin.vlabeler.ui.common.StarTriStateIcon
import com.sdercolin.vlabeler.ui.common.ToggleButtonGroup
import com.sdercolin.vlabeler.ui.common.WithTooltip
import com.sdercolin.vlabeler.ui.dialog.plugin.ParamEntrySelector
import com.sdercolin.vlabeler.ui.string.*
import com.sdercolin.vlabeler.ui.theme.White
import com.sdercolin.vlabeler.util.JavaScript

data class EntryFilterSetterDialogArgs(
    val labelerConf: LabelerConf,
    val entries: List<Entry>,
    val value: EntryFilter,
) : EmbeddedDialogArgs

data class EntryFilterSetterDialogResult(
    val value: EntryFilter,
) : EmbeddedDialogResult<EntryFilterSetterDialogArgs>

private enum class Mode {
    Basic,
    Advanced,
}

private class EntryFilterSetterDialogState(
    val args: EntryFilterSetterDialogArgs,
    finish: (EntryFilterSetterDialogResult?) -> Unit,
) {
    val js by lazy { JavaScript() }
    var mode by mutableStateOf(if (args.value.advanced == null) Mode.Basic else Mode.Advanced)
    var basicValue by mutableStateOf(args.value.parse())
    var advancedValue by mutableStateOf(args.value.advanced)
    var isAdvancedValueParseError: Boolean by mutableStateOf(false)
    var resetKey: Int by mutableStateOf(0)
        private set
    val dismiss = { finish(null) }
    val submit = {
        val result = when (mode) {
            Mode.Basic -> basicValue.toEntryFilter()
            Mode.Advanced -> EntryFilter(advanced = advancedValue)
        }
        finish(EntryFilterSetterDialogResult(result))
    }

    val canSubmit: Boolean
        get() = when (mode) {
            Mode.Basic -> true
            Mode.Advanced -> advancedValue?.isValid(args.labelerConf) != false
        }

    val canClear: Boolean
        get() = basicValue != EntryFilter.Args() || advancedValue?.isEmpty() == false

    val canReset: Boolean
        get() = basicValue != args.value.parse() || advancedValue != args.value.advanced

    fun clear() {
        basicValue = EntryFilter.Args()
        advancedValue = null
        resetKey++
    }

    fun reset() {
        basicValue = args.value.parse()
        advancedValue = args.value.advanced
        resetKey++
    }
}

@Composable
fun EntryFilterSetterDialog(
    args: EntryFilterSetterDialogArgs,
    finish: (EntryFilterSetterDialogResult?) -> Unit,
) {
    val state = remember { EntryFilterSetterDialogState(args, finish) }
    HeaderFooterColumn(
        modifier = Modifier.fillMaxWidth(0.6f).padding(top = 20.dp),
        header = {
            Text(
                text = string(Strings.EntryFilterSetterDialogTitle),
                style = MaterialTheme.typography.h6,
            )
            Spacer(modifier = Modifier.height(25.dp))
        },
        footer = {
            Spacer(modifier = Modifier.height(25.dp))
            ButtonBar(
                canClear = state.canClear,
                canReset = state.canReset,
                canSubmit = state.canSubmit,
                clear = state::clear,
                reset = state::reset,
                submit = state.submit,
                dismiss = state.dismiss,
            )
        },
    ) {
        Content(state)
    }
}

private val HEADER_WIDTH = 200.dp

@Composable
private fun Content(state: EntryFilterSetterDialogState) {
    Column {
        Selector(
            mode = state.mode,
            setMode = { state.mode = it },
        )
        Spacer(modifier = Modifier.height(30.dp))
        when (state.mode) {
            Mode.Basic -> BasicContent(
                value = state.basicValue,
                setValue = {
                    state.basicValue = it
                },
            )
            Mode.Advanced -> AdvancedContent(state)
        }
    }
}

@Composable
private fun Selector(mode: Mode, setMode: (Mode) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(15.dp),
    ) {
        ModeButton(
            text = string(Strings.EntryFilterSetterDialogModeBasic),
            isSelected = mode == Mode.Basic,
            onClick = { setMode(Mode.Basic) },
        )
        ModeButton(
            text = string(Strings.EntryFilterSetterDialogModeAdvanced),
            isSelected = mode == Mode.Advanced,
            onClick = { setMode(Mode.Advanced) },
        )
    }
}

@Composable
fun ModeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    TextButton(
        modifier = Modifier.widthIn(min = 120.dp).heightIn(min = 60.dp),
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(
            backgroundColor = if (isSelected) {
                MaterialTheme.colors.primaryVariant
            } else {
                White.copy(alpha = 0.1f)
            },
            contentColor = MaterialTheme.colors.onSurface,
        ),
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 10.dp),
            text = text,
        )
    }
}

@Composable
private fun BasicContent(value: EntryFilter.Args, setValue: (EntryFilter.Args) -> Unit) {
    TextItemRow(
        headerText = string(Strings.EntryFilterSetterDialogHeaderAny),
        value = value.any ?: "",
        setValue = { setValue(value.copy(any = it.ifEmpty { null })) },
    )
    TextItemRow(
        headerText = string(Strings.EntryFilterSetterDialogHeaderName),
        value = value.name ?: "",
        setValue = { setValue(value.copy(name = it.ifEmpty { null })) },
    )
    TextItemRow(
        headerText = string(Strings.EntryFilterSetterDialogHeaderSample),
        value = value.sample ?: "",
        setValue = { setValue(value.copy(sample = it.ifEmpty { null })) },
    )
    TextItemRow(
        headerText = string(Strings.EntryFilterSetterDialogHeaderTag),
        value = value.tag ?: "",
        setValue = { setValue(value.copy(tag = it.ifEmpty { null })) },
    )
    ItemRow(
        headerText = string(Strings.EntryFilterSetterDialogHeaderDone),
        hasValue = value.done != null,
    ) {
        ToggleButtonGroup(
            selected = value.done,
            options = listOf(null, false, true),
            onSelectedChange = { setValue(value.copy(done = it)) },
            buttonContent = {
                WithTooltip(
                    string(
                        when (it) {
                            true -> Strings.FilterDone
                            false -> Strings.FilterUndone
                            null -> Strings.FilterDoneIgnored
                        },
                    ),
                ) {
                    DoneTriStateIcon(it, Modifier.padding(12.dp))
                }
            },
        )
    }
    ItemRow(
        headerText = string(Strings.EntryFilterSetterDialogHeaderStar),
        hasValue = value.star != null,
    ) {
        ToggleButtonGroup(
            selected = value.star,
            options = listOf(null, false, true),
            onSelectedChange = { setValue(value.copy(star = it)) },
            buttonContent = {
                WithTooltip(
                    string(
                        when (it) {
                            true -> Strings.FilterStarred
                            false -> Strings.FilterUnstarred
                            null -> Strings.FilterStarIgnored
                        },
                    ),
                ) {
                    StarTriStateIcon(it, Modifier.padding(12.dp))
                }
            },
        )
    }
}

@Composable
private fun TextItemRow(headerText: String, value: String, setValue: (String) -> Unit) {
    ItemRow(headerText, value.isNotEmpty()) {
        TextField(
            modifier = Modifier.weight(1f),
            value = value,
            onValueChange = setValue,
            singleLine = true,
        )
    }
}

@Composable
private fun ItemRow(headerText: String, hasValue: Boolean, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ItemRowHeaderText(text = headerText, isActive = hasValue)
        content()
    }
}

@Composable
private fun ItemRowHeaderText(text: String, isActive: Boolean) {
    val alpha = if (isActive) 1f else 0.5f
    Text(
        text = text,
        modifier = Modifier.width(HEADER_WIDTH),
        style = MaterialTheme.typography.body2,
        fontWeight = FontWeight.Bold,
        color = LocalContentColor.current.copy(alpha = alpha),
    )
}

@Composable
private fun AdvancedContent(state: EntryFilterSetterDialogState) {
    key(state.resetKey) {
        Box(modifier = Modifier.fillMaxWidth()) {
            ParamEntrySelector(
                labelerConf = state.args.labelerConf,
                value = state.advancedValue ?: EntrySelector(listOf()),
                onValueChange = { state.advancedValue = it.takeIf { !it.isEmpty() } },
                isError = state.advancedValue?.isValid(state.args.labelerConf) == false,
                onParseErrorChange = { state.isAdvancedValueParseError = it },
                entries = state.args.entries,
                js = state.js,
                enabled = true,
                onError = { Log.error("Error in advanced filter: $it") },
                height = 300.dp,
                expandedHeight = null,
            )
        }
    }
}

@Composable
private fun ButtonBar(
    canClear: Boolean,
    canReset: Boolean,
    canSubmit: Boolean,
    clear: () -> Unit,
    reset: () -> Unit,
    submit: () -> Unit,
    dismiss: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Button(onClick = reset, enabled = canReset) {
            Text(text = string(Strings.CommonReset))
        }
        Spacer(Modifier.width(25.dp))
        Button(onClick = clear, enabled = canClear) {
            Text(text = string(Strings.CommonClear))
        }
        Spacer(Modifier.width(25.dp))
        Spacer(Modifier.weight(1f))
        TextButton(onClick = dismiss) {
            Text(text = string(Strings.CommonCancel))
        }
        Spacer(Modifier.width(25.dp))
        ConfirmButton(onClick = submit, enabled = canSubmit)
    }
}
