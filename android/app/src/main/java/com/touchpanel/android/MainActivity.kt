package com.touchpanel.android

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.touchpanel.android.core.theme.TouchPanelTheme
import com.touchpanel.android.data.connection.ConnectionManager
import com.touchpanel.android.data.layout.DataStoreLayoutRepository
import com.touchpanel.android.domain.commands.CommandDispatcher
import com.touchpanel.android.integration.mango.MangoTalkerAdapter
import com.touchpanel.android.ui.dashboard.DashboardScreen
import com.touchpanel.android.ui.dashboard.DashboardViewModel

class MainActivity : ComponentActivity() {

    private lateinit var layoutRepository: DataStoreLayoutRepository
    private lateinit var connectionManager: ConnectionManager
    private lateinit var commandDispatcher: CommandDispatcher
    private lateinit var mangoAdapter: MangoTalkerAdapter

    private val viewModel: DashboardViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DashboardViewModel(
                    layoutRepository = layoutRepository,
                    connectionManager = connectionManager,
                    commandDispatcher = commandDispatcher,
                    mangoAdapter = mangoAdapter
                ) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep screen on when phone is placed on desk
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Enable immersive edge-to-edge fullscreen landscape
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())

        // Initialize dependencies
        layoutRepository = DataStoreLayoutRepository(applicationContext)
        connectionManager = ConnectionManager()
        commandDispatcher = CommandDispatcher(connectionManager)
        mangoAdapter = MangoTalkerAdapter(applicationContext)

        setContent {
            TouchPanelTheme {
                DashboardScreen(viewModel = viewModel)
            }
        }
    }
}
