package com.conundrum.thomas.v2

import android.os.Bundle
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.runtime.SideEffect
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.conundrum.thomas.v2.engine.ordinary.RequestedOrdinarySupport
import com.conundrum.thomas.v2.journal.JournalResponsePreference
import com.conundrum.thomas.v2.runtime.ProductionThomasMode
import com.conundrum.thomas.v2.runtime.ProductionSourceSummary
import com.conundrum.thomas.v2.ui.theme.ConundrumThomasV2Theme

class MainActivity : ComponentActivity() {
    override fun dispatchTouchEvent(event: android.view.MotionEvent): Boolean {
        if (event.actionMasked in listOf(0, 1, 3)) SubmissionDispatchProbe.record("ACTIVITY_TOUCH",
            "action=" + event.actionMasked + " x=" + event.x + " y=" + event.y)
        return super.dispatchTouchEvent(event).also {
            if (event.actionMasked in listOf(0, 1, 3)) SubmissionDispatchProbe.record("ACTIVITY_TOUCH_RESULT", "handled=" + it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ConundrumThomasV2Theme(dynamicColor = false) {
                ThomasApp()
            }
        }
    }
}

@Composable
private fun ThomasApp(viewModel: ThomasViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    var showData by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            Surface(shadowElevation = 2.dp) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Thomas", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                            Text(
                                "Governed local companion",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        TextButton(
                            onClick = { showData = true },
                            modifier = Modifier.testTag("data-custody"),
                        ) { Text("Your data") }
                    }
                    Spacer(Modifier.height(10.dp))
                    ModeSelector(state.mode, !state.processing, viewModel::selectMode)
                }
            }
        },
        bottomBar = {
            InputPanel(state, viewModel)
        },
        modifier = Modifier.fillMaxSize().imePadding(),
    ) { padding ->
        Conversation(
            state,
            Modifier.fillMaxSize().padding(padding),
        )
    }
    if (showData) {
        DataCustodyDialog(viewModel, onDismiss = { showData = false })
    }
}

@Composable
private fun ModeSelector(
    selected: ProductionThomasMode,
    enabled: Boolean,
    onSelect: (ProductionThomasMode) -> Unit,
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ProductionThomasMode.entries.forEach { mode ->
            FilterChip(
                selected = mode == selected,
                onClick = { onSelect(mode) },
                label = { Text(mode.displayName()) },
                enabled = enabled,
                modifier = Modifier.weight(1f).testTag("mode-${mode.name.lowercase()}"),
            )
        }
    }
}

@Composable
private fun Conversation(state: ThomasUiState, modifier: Modifier = Modifier) {
    val visible = state.transcript.filter { it.mode == state.mode }
    val listState = rememberLazyListState()
    LaunchedEffect(visible.size) {
        if (visible.isNotEmpty()) listState.animateScrollToItem(visible.lastIndex)
    }
    if (visible.isEmpty()) {
        Box(modifier.padding(24.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(state.mode.displayName(), style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Text(
                    modeIntroduction(state.mode),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.padding(horizontal = 14.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item { Spacer(Modifier.height(12.dp)) }
            items(visible, key = { it.id }) { item -> TranscriptBubble(item) }
            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun TranscriptBubble(item: TranscriptItem) {
    val isUser = item.role == TranscriptRole.USER
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.86f).testTag(
                if (isUser) "transcript-user" else "transcript-thomas",
            ),
            shape = RoundedCornerShape(18.dp),
        ) {
            Column(
                Modifier.background(
                    if (isUser) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant,
                ).padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Text(
                    if (isUser) "You" else "Thomas",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(4.dp))
                Text(item.text, style = MaterialTheme.typography.bodyLarge)
                if (!item.committed) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Not saved",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
private fun InputPanel(state: ThomasUiState, viewModel: ThomasViewModel) {
    SideEffect { SubmissionDispatchProbe.record("BUTTON_STATE", "enabled=" +
        (state.runtimeAvailable && !state.processing && state.draft.isNotBlank()) +
        " processing=" + state.processing + " draftLength=" + state.draft.length) }

    Surface(shadowElevation = 4.dp) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            when (state.mode) {
                ProductionThomasMode.JOURNAL -> JournalControls(state, viewModel)
                ProductionThomasMode.THERAPY -> TherapyControls(state, viewModel)
                ProductionThomasMode.BIOGRAPHER -> Unit
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = state.privateTurn,
                    onCheckedChange = viewModel::setPrivate,
                    modifier = Modifier.testTag("private-turn"),
                )
                Text("Private: use now, exclude from future memory", style = MaterialTheme.typography.labelMedium)
            }
            OutlinedTextField(
                value = state.draft,
                onValueChange = viewModel::updateDraft,
                label = { Text("Write in ${state.mode.displayName()}") },
                minLines = 2,
                maxLines = 5,
                enabled = state.runtimeAvailable && !state.processing,
                modifier = Modifier.fillMaxWidth().testTag("turn-draft"),
            )
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    state.status,
                    modifier = Modifier.weight(1f).testTag("runtime-status"),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (state.runtimeAvailable) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.error,
                )
                OutlinedButton(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.testTag("speech-unavailable"),
                ) { Text("Speech unavailable") }
                Button(
                    onClick = {
                        SubmissionDispatchProbe.record("UI_ON_CLICK")
                        viewModel.submit()
                    },
                    enabled = state.runtimeAvailable && !state.processing && state.draft.isNotBlank(),
                    modifier = Modifier.testTag("commit-turn").onGloballyPositioned {
                        SubmissionDispatchProbe.record("BUTTON_BOUNDS", it.boundsInWindow().toString())
                    },
                ) {
                    Text(if (state.processing) "Working…" else "Commit")
                }
            }
        }
    }
}

@Composable
private fun JournalControls(state: ThomasUiState, viewModel: ThomasViewModel) {
    Text("After committing", style = MaterialTheme.typography.labelMedium)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf(
            JournalResponsePreference.NO_RESPONSE to "Silence",
            JournalResponsePreference.REFLECT to "Reflect",
            JournalResponsePreference.ASK_ONE_QUESTION to "One question",
        ).forEach { (preference, label) ->
            FilterChip(
                selected = state.journalPreference == preference,
                onClick = { viewModel.setJournalPreference(preference) },
                label = { Text(label) },
                modifier = Modifier.testTag("journal-${preference.name.lowercase()}"),
            )
        }
    }
}

@Composable
private fun TherapyControls(state: ThomasUiState, viewModel: ThomasViewModel) {
    Text("What support do you want?", style = MaterialTheme.typography.labelMedium)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf(
            RequestedOrdinarySupport.LISTEN to "Listen",
            RequestedOrdinarySupport.UNDERSTAND to "Understand",
            RequestedOrdinarySupport.PRACTICAL_HELP to "Practical",
        ).forEach { (support, label) ->
            FilterChip(
                selected = state.therapySupport == support,
                onClick = { viewModel.setTherapySupport(support) },
                label = { Text(label) },
            )
        }
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = state.explicitRecall,
            onCheckedChange = viewModel::setExplicitRecall,
            modifier = Modifier.testTag("therapy-explicit-recall"),
        )
        Text("I’m explicitly asking Thomas to look back", style = MaterialTheme.typography.labelMedium)
    }
    Text(
        "You can begin with: My specific concern is: ... Safety and scope are clarified from your current replies.",
        style = MaterialTheme.typography.labelSmall,
    )
}

@Composable
private fun DataCustodyDialog(viewModel: ThomasViewModel, onDismiss: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val custody = remember { viewModel.createCustodyController(context.contentResolver) }
    var confirmReset by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<ProductionSourceSummary?>(null) }
    var pendingRevision by remember { mutableStateOf<ProductionSourceSummary?>(null) }
    var revisionText by remember { mutableStateOf("") }
    var awaitingRecoveryDestination by remember { mutableStateOf(false) }
    val recoveryKeyLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream"),
    ) { uri ->
        val ok = uri != null && custody.writePendingRecoveryKey(uri).isSuccess
        awaitingRecoveryDestination = false
        viewModel.notifyCustodyStatus(if (ok) "Protected backup and separate recovery key created" else "Recovery-key custody failed")
    }
    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream"),
    ) { uri ->
        val ok = uri != null && custody.writeProtectedBackup(uri).isSuccess
        if (ok) {
            awaitingRecoveryDestination = true
            recoveryKeyLauncher.launch("thomas-recovery-key.ctkey")
        } else {
            viewModel.notifyCustodyStatus("Protected backup failed")
        }
    }
    val machineExportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        val ok = uri != null && custody.writeMachineReadableExport(uri).isSuccess
        viewModel.notifyCustodyStatus(if (ok) "Machine-readable export created" else "Export failed")
    }
    val humanExportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/markdown"),
    ) { uri ->
        val ok = uri != null && custody.writeHumanReadableExport(uri).isSuccess
        viewModel.notifyCustodyStatus(if (ok) "Human-readable export created" else "Export failed")
    }
    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments(),
    ) { uris ->
        val ok = custody.replaceFromSelectedFiles(uris).isSuccess
        viewModel.notifyCustodyStatus(
            if (ok) "Protected backup restored after complete local replacement"
            else "Restore rejected without partial authority",
        )
    }
    DisposableEffect(custody) { onDispose(custody::close) }

    AlertDialog(
        onDismissRequest = { if (!awaitingRecoveryDestination) onDismiss() },
        title = { Text("Your Thomas data") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Exports are readable. Backups are encrypted and require the separate recovery-key file.")
                if (state.sourceSummaries.isNotEmpty()) {
                    Text("Stored sources", style = MaterialTheme.typography.titleSmall)
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 210.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        items(state.sourceSummaries, key = { it.stableSourceId.value }) { source ->
                            SourceCustodyRow(
                                source,
                                enabled = !state.processing,
                                onPrivacy = { viewModel.changeSourcePrivacy(source) },
                                onRevise = {
                                    pendingRevision = source
                                    revisionText = ""
                                },
                                onDelete = { pendingDelete = source },
                            )
                        }
                    }
                } else {
                    Text("No committed sources are stored.", style = MaterialTheme.typography.bodySmall)
                }
                HorizontalDivider()
                OutlinedButton(
                    onClick = { machineExportLauncher.launch("thomas-data.json") },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Export machine-readable JSON") }
                OutlinedButton(
                    onClick = { humanExportLauncher.launch("thomas-data.md") },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Export human-readable Markdown") }
                OutlinedButton(
                    onClick = { backupLauncher.launch("thomas-protected-backup.ctbak") },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Create protected backup") }
                OutlinedButton(
                    onClick = { restoreLauncher.launch(arrayOf("application/octet-stream", "*/*")) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Replace local data from backup") }
                Button(
                    onClick = { confirmReset = true },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Reset all local Thomas data") }
                Text(
                    "Reset cannot erase exports or backups you keep elsewhere.",
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } },
    )
    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Delete all local Thomas data?") },
            text = { Text("This removes the protected local corpus, revisions, projections, and local key. External exports and backups remain.") },
            confirmButton = {
                Button(onClick = {
                    confirmReset = false
                    viewModel.resetAllPersonalData()
                    onDismiss()
                }) { Text("Delete local data") }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Cancel") } },
        )
    }
    pendingDelete?.let { source ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete this source history?") },
            text = {
                Text("This deletes ${source.stableSourceId.value}, all revisions, and dependent derived state. It cannot erase external exports or backups.")
            },
            confirmButton = {
                Button(onClick = {
                    pendingDelete = null
                    viewModel.deleteSource(source)
                }) { Text("Delete source") }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } },
        )
    }
    pendingRevision?.let { source ->
        AlertDialog(
            onDismissRequest = {
                pendingRevision = null
                revisionText = ""
            },
            title = { Text("Correct or revise this source") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter the complete corrected wording. The prior revision remains in governed history but stops being current authority.")
                    OutlinedTextField(
                        value = revisionText,
                        onValueChange = { if (it.length <= 4_096) revisionText = it },
                        label = { Text("Corrected source wording") },
                        minLines = 3,
                        maxLines = 7,
                    )
                    Text(source.acquisitionMode.name.replace('_', ' '), style = MaterialTheme.typography.labelSmall)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.reviseSource(source, revisionText)
                        pendingRevision = null
                        revisionText = ""
                    },
                    enabled = revisionText.isNotBlank(),
                ) { Text("Record revision") }
            },
            dismissButton = {
                TextButton(onClick = {
                    pendingRevision = null
                    revisionText = ""
                }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun SourceCustodyRow(
    source: ProductionSourceSummary,
    enabled: Boolean,
    onPrivacy: () -> Unit,
    onRevise: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(10.dp)) {
            Text(source.acquisitionMode.name.replace('_', ' '), fontWeight = FontWeight.SemiBold)
            Text(
                "${source.reportTime} • ${source.revisionCount} revision(s) • ${source.lifecycleStatus ?: "unknown"}",
                style = MaterialTheme.typography.labelSmall,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(onClick = onPrivacy, enabled = enabled) {
                    Text(if (source.eligibleForOrdinaryUse) "Make private" else "Request reuse review")
                }
                TextButton(onClick = onRevise, enabled = enabled) { Text("Correct / revise") }
                TextButton(onClick = onDelete, enabled = enabled) { Text("Delete") }
            }
        }
    }
}

private fun modeIntroduction(mode: ProductionThomasMode): String = when (mode) {
    ProductionThomasMode.JOURNAL -> "Commit a private or reusable entry. Silence is the default."
    ProductionThomasMode.BIOGRAPHER -> "Fill in your history through one governed question at a time."
    ProductionThomasMode.THERAPY -> "Choose the support you want. Memory may inform the move; it never chooses it."
}
