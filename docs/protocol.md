# Windows Touch Panel Protocol (WTPP) Specification

Version: `1.0.0`  
Transport Options: WebSocket (LAN Wi-Fi) on port `8088`, Bluetooth RFCOMM (SPP UUID `00001101-0000-1000-8000-00805F9B34FB`)  
Encoding: UTF-8 JSON  

---

## 1. Overview & Security Architecture

The Windows Touch Panel Protocol allows an Android device running the **Touch Panel Android** app to act as a low-latency, bi-directional control surface for a host PC running Windows 10/11.

### Security Principles
1. **Explicit Device Pairing**: Unpaired devices cannot execute commands on the Windows host.
2. **Pre-shared Secret / Handshake Token**: Pairing exchanges an ephemeral 6-digit PIN displayed on the Windows host monitor, returning an authorized device token (`authToken`).
3. **Restricted Command Allowlist**: Arbitrary shell execution (`cmd.exe /c ...` with unvalidated user input) is strictly prohibited. Only predefined command types and whitelisted application IDs can be executed.
4. **Local Network / Point-to-Point Only**: The server listens only on the local network interface or Bluetooth channel; it is never exposed publicly to the internet.

---

## 2. Message Envelope Format

Every packet exchanged between the Android client and Windows server adheres to the base envelope format:

```json
{
  "version": "1.0.0",
  "id": "uuid-v4-or-sequence-id",
  "type": "command | response | event | handshake",
  "timestamp": 1729944000000,
  "payload": {}
}
```

---

## 3. Protocol Phases

### Phase 1: Handshake & Authentication

#### 3.1 Client Handshake Request
Sent immediately upon socket connection.
```json
{
  "version": "1.0.0",
  "id": "req-init-001",
  "type": "handshake",
  "timestamp": 1729944000100,
  "payload": {
    "clientName": "Pixel 8 Pro (Touch Panel)",
    "clientId": "android-device-36a505cc",
    "authToken": "optional-stored-token-or-empty",
    "pairingPin": "123456"
  }
}
```

#### 3.2 Host Handshake Response
```json
{
  "version": "1.0.0",
  "id": "req-init-001",
  "type": "response",
  "timestamp": 1729944000150,
  "payload": {
    "status": "authenticated",
    "authToken": "wtpp_auth_9f81a7b3c2d4e5f6",
    "hostName": "DESKTOP-RYZEN9",
    "windowsVersion": "Windows 11 Pro 23H2",
    "capabilities": [
      "volume",
      "brightness",
      "media_control",
      "app_launch",
      "key_shortcut",
      "telemetry"
    ],
    "currentVolume": 65,
    "currentBrightness": 80,
    "isMuted": false
  }
}
```

If authentication fails (e.g. invalid PIN or expired token):
```json
{
  "version": "1.0.0",
  "id": "req-init-001",
  "type": "response",
  "timestamp": 1729944000150,
  "payload": {
    "status": "error",
    "errorCode": "AUTH_FAILED",
    "message": "Invalid pairing PIN or device not authorized."
  }
}
```

---

## 4. Commands (Client -> Windows Host)

### 4.1 Volume Control
- **Action**: `SetVolume`
- **Params**:
  - `level`: Integer `0`–`100`
  - `relative`: Optional Boolean (default `false`)
- **Action**: `ToggleMute`
  - `params`: `{}`

```json
{
  "version": "1.0.0",
  "id": "cmd-vol-002",
  "type": "command",
  "timestamp": 1729944002000,
  "payload": {
    "action": "SetVolume",
    "params": {
      "level": 75
    }
  }
}
```

### 4.2 Brightness Control
- **Action**: `SetBrightness`
- **Params**:
  - `level`: Integer `0`–`100`

```json
{
  "version": "1.0.0",
  "id": "cmd-bri-003",
  "type": "command",
  "timestamp": 1729944003000,
  "payload": {
    "action": "SetBrightness",
    "params": {
      "level": 60
    }
  }
}
```

### 4.3 Media Controls
- **Actions**: `MediaPlayPause`, `MediaNext`, `MediaPrevious`, `MediaStop`
- Simulates Windows standard virtual key codes (`VK_MEDIA_PLAY_PAUSE = 0xB3`, `VK_MEDIA_NEXT_TRACK = 0xB0`, `VK_MEDIA_PREV_TRACK = 0xB1`).

```json
{
  "version": "1.0.0",
  "id": "cmd-med-004",
  "type": "command",
  "timestamp": 1729944004000,
  "payload": {
    "action": "MediaPlayPause",
    "params": {}
  }
}
```

### 4.4 Launch Application
- **Action**: `LaunchApplication`
- **Params**:
  - `appId`: Whitelisted identifier string (e.g. `calc`, `taskmgr`, `terminal`, `browser`, `spotify`, `discord`, `notepad`)
  - `customPath`: Optional string, allowed only if path is inside user-configured whitelist on host.

```json
{
  "version": "1.0.0",
  "id": "cmd-app-005",
  "type": "command",
  "timestamp": 1729944005000,
  "payload": {
    "action": "LaunchApplication",
    "params": {
      "appId": "calc"
    }
  }
}
```

### 4.5 Execute Windows Shortcut
- **Action**: `ExecuteShortcut`
- **Params**:
  - `shortcutId`: Identifiers such as `lock_screen` (`Win+L`), `screen_snip` (`Win+Shift+S`), `task_manager` (`Ctrl+Shift+Esc`), `show_desktop` (`Win+D`).
  - `keyCombo`: Human-readable key description for logging.

```json
{
  "version": "1.0.0",
  "id": "cmd-shc-006",
  "type": "command",
  "timestamp": 1729944006000,
  "payload": {
    "action": "ExecuteShortcut",
    "params": {
      "shortcutId": "screen_snip",
      "keyCombo": "Win+Shift+S"
    }
  }
}
```

---

## 5. Host Responses

For every command with an `id`, the host returns a correlated response:

```json
{
  "version": "1.0.0",
  "id": "cmd-vol-002",
  "type": "response",
  "timestamp": 1729944002015,
  "payload": {
    "status": "success",
    "action": "SetVolume",
    "result": {
      "currentVolume": 75,
      "isMuted": false
    }
  }
}
```

---

## 6. Host Asynchronous Events (Windows Host -> Android Client)

The host can push system state changes when user adjusts volume physically on the keyboard, changes tracks in Spotify, or locks the PC:

### 6.1 Volume Changed Event
```json
{
  "version": "1.0.0",
  "id": "evt-vol-101",
  "type": "event",
  "timestamp": 1729944010000,
  "payload": {
    "eventType": "VolumeChanged",
    "volume": 82,
    "isMuted": false
  }
}
```

### 6.2 Now Playing Media Event
```json
{
  "version": "1.0.0",
  "id": "evt-med-102",
  "type": "event",
  "timestamp": 1729944015000,
  "payload": {
    "eventType": "MediaTrackChanged",
    "title": "Midnight City",
    "artist": "M83",
    "album": "Hurry Up, We're Dreaming",
    "isPlaying": true,
    "positionMs": 45000,
    "durationMs": 243000
  }
}
```

---

## 7. Reference Windows Host Server (Python / C#)

A minimalist Windows host server using Python + `websockets` + `pycaw` (audio control) + `wmi` (brightness):

```python
# windows_server.py
import asyncio
import json
import websockets
# Requires: pip install websockets pycaw screen-brightness-control pyautogui

ALLOWED_PIN = "123456"

async def handler(websocket):
    authenticated = False
    print("New connection from Android Touch Panel")
    
    async for message in websocket:
        packet = json.loads(message)
        msg_id = packet.get("id")
        msg_type = packet.get("type")
        payload = packet.get("payload", {})
        
        if msg_type == "handshake":
            if payload.get("pairingPin") == ALLOWED_PIN:
                authenticated = True
                await websocket.send(json.dumps({
                    "version": "1.0.0",
                    "id": msg_id,
                    "type": "response",
                    "payload": {
                        "status": "authenticated",
                        "authToken": "secret_token_123",
                        "hostName": "My-Windows-PC",
                        "currentVolume": 50,
                        "currentBrightness": 70
                    }
                }))
            else:
                await websocket.send(json.dumps({
                    "version": "1.0.0",
                    "id": msg_id,
                    "type": "response",
                    "payload": {"status": "error", "message": "Invalid PIN"}
                }))
            continue
            
        if not authenticated:
            await websocket.send(json.dumps({
                "version": "1.0.0",
                "id": msg_id,
                "type": "response",
                "payload": {"status": "error", "message": "Not authenticated"}
            }))
            continue
            
        # Dispatch command
        action = payload.get("action")
        params = payload.get("params", {})
        print(f"Executing: {action} with {params}")
        
        # Acknowledge command
        await websocket.send(json.dumps({
            "version": "1.0.0",
            "id": msg_id,
            "type": "response",
            "payload": {"status": "success", "action": action, "result": params}
        }))

async def main():
    print("Windows Touch Panel Server running on ws://0.0.0.0:8088")
    async with websockets.serve(handler, "0.0.0.0", 8088):
        await asyncio.Future()

if __name__ == "__main__":
    asyncio.run(main())
```
