package com.touchpanel.android.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.touchpanel.android.core.theme.*
import com.touchpanel.android.integration.mango.MangoCallState
import com.touchpanel.android.integration.mango.MangoTalkerAdapter

@Composable
fun MangoTalkerWidget(
    state: MangoCallState,
    adapter: MangoTalkerAdapter,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        when (state) {
            is MangoCallState.IncomingCall -> {
                IncomingCallCard(
                    callerName = state.callerName,
                    phoneNumber = state.phoneNumber,
                    onAnswer = { adapter.answerCall() },
                    onDecline = { adapter.declineCall() }
                )
            }
            is MangoCallState.ActiveCall -> {
                ActiveCallCard(
                    callerName = state.callerName,
                    phoneNumber = state.phoneNumber,
                    durationSeconds = state.durationSeconds,
                    onEnd = { adapter.endCall() }
                )
            }
            is MangoCallState.AppAvailable -> {
                AppReadyCard(
                    onLaunch = { adapter.launchApp() },
                    onSimulateIncoming = {
                        adapter.simulateIncomingCall("Alexander Romanov", "+7 (495) 212-92-98")
                    }
                )
            }
            is MangoCallState.AppNotInstalled -> {
                StatusNoticeCard(
                    title = "Mango Talker Not Found",
                    subtitle = "Install com.mangotele.mtalker on this device to link phone calls."
                )
            }
            is MangoCallState.CallEnded -> {
                CallEndedCard(
                    onDismiss = { adapter.checkInstallation() }
                )
            }
            is MangoCallState.IntegrationError -> {
                StatusNoticeCard(
                    title = "Integration Notice",
                    subtitle = state.message
                )
            }
            is MangoCallState.NotConfigured -> {
                AppReadyCard(
                    onLaunch = { adapter.launchApp() },
                    onSimulateIncoming = {
                        adapter.simulateIncomingCall("Client Support", "+7 (800) 555-55-50")
                    }
                )
            }
        }
    }
}

@Composable
private fun IncomingCallCard(
    callerName: String,
    phoneNumber: String,
    onAnswer: () -> Unit,
    onDecline: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AccentPurple),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = callerName,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = phoneNumber,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Text(
            text = "Incoming Call via Mango Talker...",
            color = StatusWarning,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onAnswer,
                modifier = Modifier.weight(1f).height(38.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StatusConnected),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Default.Call, contentDescription = "Answer", tint = TextPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Answer", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = onDecline,
                modifier = Modifier.weight(1f).height(38.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StatusDisconnected),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Default.CallEnd, contentDescription = "Decline", tint = TextPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Decline", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ActiveCallCard(
    callerName: String,
    phoneNumber: String,
    durationSeconds: Long,
    onEnd: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(StatusConnected),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Call, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = callerName,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Active Call • ${durationSeconds / 60}:${String.format("%02d", durationSeconds % 60)}",
                    color = StatusConnected,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Button(
            onClick = onEnd,
            modifier = Modifier.fillMaxWidth().height(36.dp),
            colors = ButtonDefaults.buttonColors(containerColor = StatusDisconnected),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.CallEnd, contentDescription = "End Call", tint = TextPrimary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("End Call", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun AppReadyCard(
    onLaunch: () -> Unit,
    onSimulateIncoming: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceCardActive),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Phone, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text("Mango Talker Ready", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text("Ready for calls & dialer", color = TextSecondary, fontSize = 11.sp)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onLaunch,
                modifier = Modifier.weight(1f).height(32.dp),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("Open App", fontSize = 11.sp, color = TextPrimary)
            }

            Button(
                onClick = onSimulateIncoming,
                modifier = Modifier.weight(1f).height(32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceCardElevated),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("Test Call", fontSize = 11.sp, color = AccentPurple)
            }
        }
    }
}

@Composable
private fun CallEndedCard(
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Call Ended", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onDismiss,
            modifier = Modifier.height(30.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SurfaceCardElevated),
            shape = RoundedCornerShape(6.dp)
        ) {
            Text("Done", fontSize = 11.sp, color = TextPrimary)
        }
    }
}

@Composable
private fun StatusNoticeCard(
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, color = StatusWarning, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(subtitle, color = TextMuted, fontSize = 10.sp, lineHeight = 14.sp)
    }
}
