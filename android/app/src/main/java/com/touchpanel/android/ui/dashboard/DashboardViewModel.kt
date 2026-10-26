package com.touchpanel.android.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.touchpanel.android.data.connection.ConnectionManager
import com.touchpanel.android.data.connection.ConnectionState
import com.touchpanel.android.data.connection.TransportType
import com.touchpanel.android.data.layout.DefaultLayout
import com.touchpanel.android.domain.commands.CommandDispatcher
import com.touchpanel.android.domain.commands.CommandResult
import com.touchpanel.android.domain.commands.WindowsCommand
import com.touchpanel.android.domain.widgets.DashboardLayout
import com.touchpanel.android.domain.widgets.LayoutRepository
import com.touchpanel.android.domain.widgets.WidgetModel
import com.touchpanel.android.domain.widgets.WidgetType
import com.touchpanel.android.integration.mango.MangoCallState
import com.touchpanel.android.integration.mango.MangoTalkerAdapter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class MediaTrackState(
    val title: String = "Starlight",
    val artist: String = "Muse",
    val isPlaying: Boolean = false,
    val progressPercent: Float = 0.35f
)

data class DashboardUiState(
    val layout: DashboardLayout = DefaultLayout.create(),
    val isEditMode: Boolean = false,
    val volume: Int = 50,
    val isMuted: Boolean = false,
    val brightness: Int = 75,
    val mediaState: MediaTrackState = MediaTrackState(),
    val connectionState: ConnectionState = ConnectionState.Disconnected,
    val mangoState: MangoCallState = MangoCallState.NotConfigured,
    val batteryPercent: Int = 85,
    val isPowerConnected: Boolean = true,
    val statusMessage: String? = null,
    val isCatalogOpen: Boolean = false,
    val selectedWidgetForEdit: WidgetModel? = null
)

class DashboardViewModel(
    private val layoutRepository: LayoutRepository,
    val connectionManager: ConnectionManager,
    private val commandDispatcher: CommandDispatcher,
    val mangoAdapter: MangoTalkerAdapter
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        // Observe layout changes from DataStore
        viewModelScope.launch {
            layoutRepository.getLayout().collect { layout ->
                _uiState.update { it.copy(layout = layout) }
            }
        }

        // Observe connection state
        viewModelScope.launch {
            connectionManager.state.collect { connState ->
                _uiState.update { it.copy(connectionState = connState) }
            }
        }

        // Observe Mango Talker call state
        viewModelScope.launch {
            mangoAdapter.callState.collect { callState ->
                _uiState.update { it.copy(mangoState = callState) }
            }
        }
    }

    fun toggleEditMode() {
        _uiState.update { it.copy(isEditMode = !it.isEditMode) }
    }

    fun setVolume(level: Int) {
        val clamped = level.coerceIn(0, 100)
        _uiState.update { it.copy(volume = clamped) }
        viewModelScope.launch {
            val cmd = WindowsCommand.SetVolume(UUID.randomUUID().toString(), clamped)
            val result = commandDispatcher.dispatch(cmd)
            handleCommandResult(result)
        }
    }

    fun toggleMute() {
        val newMute = !_uiState.value.isMuted
        _uiState.update { it.copy(isMuted = newMute) }
        viewModelScope.launch {
            val cmd = WindowsCommand.ToggleMute(UUID.randomUUID().toString())
            val result = commandDispatcher.dispatch(cmd)
            handleCommandResult(result)
        }
    }

    fun setBrightness(level: Int) {
        val clamped = level.coerceIn(0, 100)
        _uiState.update { it.copy(brightness = clamped) }
        viewModelScope.launch {
            val cmd = WindowsCommand.SetBrightness(UUID.randomUUID().toString(), clamped)
            val result = commandDispatcher.dispatch(cmd)
            handleCommandResult(result)
        }
    }

    fun mediaPlayPause() {
        val current = _uiState.value.mediaState
        _uiState.update { it.copy(mediaState = current.copy(isPlaying = !current.isPlaying)) }
        viewModelScope.launch {
            val cmd = WindowsCommand.MediaPlayPause(UUID.randomUUID().toString())
            val result = commandDispatcher.dispatch(cmd)
            handleCommandResult(result)
        }
    }

    fun mediaNext() {
        viewModelScope.launch {
            val cmd = WindowsCommand.MediaNext(UUID.randomUUID().toString())
            val result = commandDispatcher.dispatch(cmd)
            handleCommandResult(result)
        }
    }

    fun mediaPrevious() {
        viewModelScope.launch {
            val cmd = WindowsCommand.MediaPrevious(UUID.randomUUID().toString())
            val result = commandDispatcher.dispatch(cmd)
            handleCommandResult(result)
        }
    }

    fun launchApp(appId: String) {
        viewModelScope.launch {
            val cmd = WindowsCommand.LaunchApplication(UUID.randomUUID().toString(), appId)
            val result = commandDispatcher.dispatch(cmd)
            handleCommandResult(result)
        }
    }

    fun executeShortcut(shortcutId: String, keyCombo: String) {
        viewModelScope.launch {
            val cmd = WindowsCommand.ExecuteShortcut(UUID.randomUUID().toString(), shortcutId, keyCombo)
            val result = commandDispatcher.dispatch(cmd)
            handleCommandResult(result)
        }
    }

    fun connectToWindows(host: String, transport: TransportType, pin: String? = null) {
        viewModelScope.launch {
            connectionManager.connect(host, transport, pin)
        }
    }

    fun disconnectWindows() {
        viewModelScope.launch {
            connectionManager.disconnect()
        }
    }

    fun addWidget(type: WidgetType) {
        viewModelScope.launch {
            val newWidget = WidgetModel(
                id = "w_${type.name.lowercase()}_${System.currentTimeMillis()}",
                type = type,
                title = type.name.replace("_", " "),
                gridCol = 0,
                gridRow = 0,
                colSpan = 3,
                rowSpan = 2
            )
            layoutRepository.addWidget(newWidget)
            _uiState.update { it.copy(isCatalogOpen = false) }
        }
    }

    fun removeWidget(widgetId: String) {
        viewModelScope.launch {
            layoutRepository.removeWidget(widgetId)
        }
    }

    fun resetLayout() {
        viewModelScope.launch {
            layoutRepository.resetToDefault()
        }
    }

    fun openCatalog() {
        _uiState.update { it.copy(isCatalogOpen = true) }
    }

    fun closeCatalog() {
        _uiState.update { it.copy(isCatalogOpen = false) }
    }

    private fun handleCommandResult(result: CommandResult) {
        when (result) {
            is CommandResult.Error -> {
                _uiState.update { it.copy(statusMessage = result.message) }
                viewModelScope.launch {
                    delay(3500)
                    _uiState.update { it.copy(statusMessage = null) }
                }
            }
            is CommandResult.Success -> {
                // Clear any lingering error message
                _uiState.update { it.copy(statusMessage = null) }
            }
        }
    }
}
