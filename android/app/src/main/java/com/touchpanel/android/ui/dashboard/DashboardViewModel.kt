package com.touchpanel.android.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.touchpanel.android.core.hardware.*
import com.touchpanel.android.data.connection.ConnectionManager
import com.touchpanel.android.data.connection.ConnectionState
import com.touchpanel.android.data.connection.TransportType
import com.touchpanel.android.data.layout.DefaultLayout
import com.touchpanel.android.domain.commands.CommandDispatcher
import com.touchpanel.android.domain.commands.CommandResult
import com.touchpanel.android.domain.commands.WindowsCommand
import com.touchpanel.android.domain.widgets.*
import com.touchpanel.android.integration.mango.MangoIntegrationStatus
import com.touchpanel.android.integration.mango.MangoTalkerAdapter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

enum class VolumeControlMode {
    ANDROID_DEVICE,
    WINDOWS_HOST
}

data class DashboardUiState(
    val layout: DashboardLayout = DefaultLayout.create(),
    val isEditMode: Boolean = false,
    val volumeMode: VolumeControlMode = VolumeControlMode.ANDROID_DEVICE,
    val androidAudio: AndroidAudioState = AndroidAudioState(),
    val windowsVolume: Int = 50,
    val isWindowsMuted: Boolean = false,
    val androidBrightness: BrightnessInfo = BrightnessInfo(),
    val windowsBrightness: Int = 75,
    val batteryInfo: BatteryInfo = BatteryInfo(),
    val connectionState: ConnectionState = ConnectionState.Disconnected,
    val mangoStatus: MangoIntegrationStatus = MangoIntegrationStatus.NotInstalled,
    val statusMessage: String? = null,
    val isErrorMessage: Boolean = false,
    val isCatalogOpen: Boolean = false,
    val quickDialNumber: String = ""
)

class DashboardViewModel(
    private val layoutRepository: LayoutRepository,
    val connectionManager: ConnectionManager,
    private val commandDispatcher: CommandDispatcher,
    val mangoAdapter: MangoTalkerAdapter,
    val batteryMonitor: BatteryMonitor,
    val volumeManager: AndroidVolumeManager,
    val brightnessManager: AndroidBrightnessManager
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

        // Observe real hardware battery telemetry
        viewModelScope.launch {
            batteryMonitor.startListening()
            batteryMonitor.batteryState.collect { battery ->
                _uiState.update { it.copy(batteryInfo = battery) }
            }
        }

        // Observe real Android hardware volume
        viewModelScope.launch {
            volumeManager.startListening()
            volumeManager.audioState.collect { audio ->
                _uiState.update { it.copy(androidAudio = audio) }
            }
        }

        // Observe Android display brightness
        viewModelScope.launch {
            brightnessManager.brightnessState.collect { brightness ->
                _uiState.update { it.copy(androidBrightness = brightness) }
            }
        }

        // Observe Mango Talker status
        viewModelScope.launch {
            mangoAdapter.status.collect { mango ->
                _uiState.update { it.copy(mangoStatus = mango) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        batteryMonitor.stopListening()
        volumeManager.stopListening()
    }

    fun toggleEditMode() {
        _uiState.update { it.copy(isEditMode = !it.isEditMode) }
    }

    fun setVolumeMode(mode: VolumeControlMode) {
        _uiState.update { it.copy(volumeMode = mode) }
    }

    // Android Hardware Volume
    fun setAndroidVolume(percent: Int) {
        volumeManager.setVolumePercent(percent)
    }

    fun toggleAndroidMute() {
        volumeManager.toggleMute()
    }

    // Windows Host Volume via WTPP
    fun setWindowsVolume(level: Int) {
        val clamped = level.coerceIn(0, 100)
        _uiState.update { it.copy(windowsVolume = clamped) }
        viewModelScope.launch {
            val cmd = WindowsCommand.SetVolume(UUID.randomUUID().toString(), clamped)
            val result = commandDispatcher.dispatch(cmd)
            handleCommandResult(result)
        }
    }

    fun toggleWindowsMute() {
        val newMute = !_uiState.value.isWindowsMuted
        _uiState.update { it.copy(isWindowsMuted = newMute) }
        viewModelScope.launch {
            val cmd = WindowsCommand.ToggleMute(UUID.randomUUID().toString())
            val result = commandDispatcher.dispatch(cmd)
            handleCommandResult(result)
        }
    }

    // Brightness Control (affects Android screen immediately)
    fun setAndroidBrightness(percent: Int) {
        brightnessManager.setBrightnessPercent(percent)
    }

    // Quick System Actions
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

    // Media Controls via WTPP
    fun mediaPlayPause() {
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

    // Connection
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

    // Mango Talker Genuine Actions
    fun setQuickDialNumber(number: String) {
        _uiState.update { it.copy(quickDialNumber = number) }
    }

    fun dialMangoNumber(phoneNumber: String) {
        if (phoneNumber.isBlank()) {
            showMessage("Введите номер для набора", isError = true)
            return
        }
        val success = mangoAdapter.dialNumber(phoneNumber)
        if (!success) {
            showMessage("Не удалось запустить набор через Mango Talker", isError = true)
        }
    }

    fun launchMangoTalkerApp() {
        val success = mangoAdapter.launchApp()
        if (!success) {
            showMessage("Mango Talker не установлен на этом телефоне", isError = true)
        }
    }

    fun installMangoTalker() {
        mangoAdapter.openStorePage()
    }

    // Layout Management
    fun addWidget(type: WidgetType) {
        viewModelScope.launch {
            val desc = WidgetCatalog.getDescriptor(type)
            val newWidget = WidgetModel(
                id = "w_${type.name.lowercase()}_${System.currentTimeMillis()}",
                type = type,
                title = desc.defaultTitle,
                gridCol = 0,
                gridRow = 0,
                colSpan = desc.defaultColSpan,
                rowSpan = desc.defaultRowSpan,
                isRemovable = true
            )
            layoutRepository.addWidget(newWidget)
            _uiState.update { it.copy(isCatalogOpen = false) }
            showMessage("Виджет '${desc.displayName}' добавлен")
        }
    }

    fun removeWidget(widgetId: String) {
        viewModelScope.launch {
            layoutRepository.removeWidget(widgetId)
            showMessage("Виджет удален с панели")
        }
    }

    fun moveWidget(index: Int, direction: Int) {
        val currentWidgets = _uiState.value.layout.widgets.toMutableList()
        val targetIndex = index + direction
        if (targetIndex in 0 until currentWidgets.size) {
            val moved = currentWidgets.removeAt(index)
            currentWidgets.add(targetIndex, moved)
            viewModelScope.launch {
                layoutRepository.saveLayout(_uiState.value.layout.copy(widgets = currentWidgets))
            }
        }
    }

    fun resizeWidget(widgetId: String, deltaCol: Int) {
        val current = _uiState.value.layout.widgets
        val updated = current.map { widget ->
            if (widget.id == widgetId) {
                val desc = WidgetCatalog.getDescriptor(widget.type)
                val newSpan = (widget.colSpan + deltaCol).coerceIn(desc.minColSpan, desc.maxColSpan)
                widget.copy(colSpan = newSpan)
            } else widget
        }
        viewModelScope.launch {
            layoutRepository.saveLayout(_uiState.value.layout.copy(widgets = updated))
        }
    }

    fun resetLayout() {
        viewModelScope.launch {
            layoutRepository.resetToDefault()
            showMessage("Раскладка сброшена по умолчанию")
        }
    }

    fun openCatalog() {
        _uiState.update { it.copy(isCatalogOpen = true) }
    }

    fun closeCatalog() {
        _uiState.update { it.copy(isCatalogOpen = false) }
    }

    fun showMessage(message: String, isError: Boolean = false) {
        _uiState.update { it.copy(statusMessage = message, isErrorMessage = isError) }
        viewModelScope.launch {
            delay(3500)
            _uiState.update { if (it.statusMessage == message) it.copy(statusMessage = null) else it }
        }
    }

    private fun handleCommandResult(result: CommandResult) {
        when (result) {
            is CommandResult.Error -> {
                showMessage(result.message, isError = true)
            }
            is CommandResult.Success -> {
                _uiState.update { it.copy(statusMessage = null) }
            }
        }
    }
}
