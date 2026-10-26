import { ConnectionConfig, ConnectionStatus, WindowsProtocolMessage } from '../types';

type MessageListener = (msg: WindowsProtocolMessage) => void;
type StatusListener = (status: ConnectionStatus, detail?: string) => void;

class WindowsProtocolService {
  private ws: WebSocket | null = null;
  private status: ConnectionStatus = 'disconnected';
  private config: ConnectionConfig = {
    host: '192.168.1.100',
    port: 8088,
    transport: 'wifi_websocket',
    pin: '123456',
    autoReconnect: false,
  };
  private packetLog: WindowsProtocolMessage[] = [];
  private messageListeners: Set<MessageListener> = new Set();
  private statusListeners: Set<StatusListener> = new Set();
  private pingInterval: number | null = null;

  constructor() {
    // Restore stored configuration from localStorage if available
    try {
      const stored = localStorage.getItem('wtpp_config');
      if (stored) {
        this.config = { ...this.config, ...JSON.parse(stored) };
      }
    } catch {
      // fallback
    }
  }

  getConfig(): ConnectionConfig {
    return { ...this.config };
  }

  updateConfig(newConfig: Partial<ConnectionConfig>) {
    this.config = { ...this.config, ...newConfig };
    localStorage.setItem('wtpp_config', JSON.stringify(this.config));
  }

  getStatus(): ConnectionStatus {
    return this.status;
  }

  getPacketLog(): WindowsProtocolMessage[] {
    return [...this.packetLog];
  }

  onMessage(listener: MessageListener) {
    this.messageListeners.add(listener);
    return () => {
      this.messageListeners.delete(listener);
    };
  }

  onStatusChange(listener: StatusListener) {
    this.statusListeners.add(listener);
    listener(this.status);
    return () => {
      this.statusListeners.delete(listener);
    };
  }

  private setStatus(newStatus: ConnectionStatus, detail?: string) {
    this.status = newStatus;
    this.statusListeners.forEach((l) => l(newStatus, detail));
  }

  private logPacket(packet: WindowsProtocolMessage) {
    this.packetLog.unshift(packet);
    if (this.packetLog.length > 100) {
      this.packetLog.pop();
    }
    this.messageListeners.forEach((l) => l(packet));
  }

  /**
   * Connect to local Windows Host via WebSocket or simulated test connection
   */
  async connect(mockMode = false): Promise<boolean> {
    if (this.status === 'connected' || this.status === 'connecting') {
      return true;
    }

    this.setStatus('connecting', `Connecting to ${this.config.host}:${this.config.port}...`);

    if (mockMode) {
      // Simulate real handshake delay
      await new Promise((r) => setTimeout(r, 600));

      const handshakeMsg: WindowsProtocolMessage = {
        version: '1.0.0',
        id: `hs-${Date.now()}`,
        type: 'handshake',
        timestamp: Date.now(),
        payload: {
          clientName: 'Android Touch Panel',
          pairingPin: this.config.pin,
        },
      };
      this.logPacket(handshakeMsg);

      const respMsg: WindowsProtocolMessage = {
        version: '1.0.0',
        id: handshakeMsg.id,
        type: 'response',
        timestamp: Date.now() + 15,
        payload: {
          status: 'authenticated',
          hostName: 'DESKTOP-WIN11-LOCAL',
          authToken: 'sim_auth_token_8899',
          capabilities: ['volume', 'brightness', 'media', 'shortcuts'],
        },
      };
      this.logPacket(respMsg);

      this.setStatus('connected', 'Connected to DESKTOP-WIN11-LOCAL (Simulation)');
      return true;
    }

    // Real WebSocket Attempt
    return new Promise((resolve) => {
      const url = `ws://${this.config.host}:${this.config.port}`;
      try {
        const socket = new WebSocket(url);
        this.ws = socket;

        const timeout = setTimeout(() => {
          if (this.status === 'connecting') {
            socket.close();
            this.setStatus('error', `Connection timed out to ${url}`);
            resolve(false);
          }
        }, 4000);

        socket.onopen = () => {
          clearTimeout(timeout);
          const handshakeMsg: WindowsProtocolMessage = {
            version: '1.0.0',
            id: `hs-${Date.now()}`,
            type: 'handshake',
            timestamp: Date.now(),
            payload: {
              clientName: 'Android Touch Panel',
              pairingPin: this.config.pin,
            },
          };
          this.logPacket(handshakeMsg);
          socket.send(JSON.stringify(handshakeMsg));
          this.setStatus('connected', `Connected to Windows Host at ${this.config.host}`);
          this.startHeartbeat();
          resolve(true);
        };

        socket.onmessage = (event) => {
          try {
            const data: WindowsProtocolMessage = JSON.parse(event.data);
            this.logPacket(data);
          } catch {
            // non-json
          }
        };

        socket.onerror = () => {
          clearTimeout(timeout);
          this.setStatus('error', `Could not reach Windows Host at ${url}`);
          resolve(false);
        };

        socket.onclose = () => {
          clearTimeout(timeout);
          this.stopHeartbeat();
          if (this.status === 'connected') {
            this.setStatus('disconnected', 'Disconnected from Windows Host');
          }
        };
      } catch (err) {
        this.setStatus('error', `WebSocket creation error: ${(err as Error).message}`);
        resolve(false);
      }
    });
  }

  disconnect() {
    this.stopHeartbeat();
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
    this.setStatus('disconnected');
  }

  private startHeartbeat() {
    this.stopHeartbeat();
    this.pingInterval = window.setInterval(() => {
      if (this.status === 'connected') {
        this.sendCommand('Ping', {}, false);
      }
    }, 15000);
  }

  private stopHeartbeat() {
    if (this.pingInterval) {
      clearInterval(this.pingInterval);
      this.pingInterval = null;
    }
  }

  /**
   * Send a command to Windows.
   * If disconnected, returns false and warns the user without pretending the command succeeded.
   */
  sendCommand(
    action: string,
    params: Record<string, unknown> = {},
    requireConnected = true
  ): { success: boolean; message: string; packet?: WindowsProtocolMessage } {
    const isConnected = this.status === 'connected';

    const packet: WindowsProtocolMessage = {
      version: '1.0.0',
      id: `cmd-${Date.now()}-${Math.floor(Math.random() * 1000)}`,
      type: 'command',
      timestamp: Date.now(),
      payload: {
        action,
        params,
      },
    };

    this.logPacket(packet);

    if (!isConnected && requireConnected) {
      const errResp: WindowsProtocolMessage = {
        version: '1.0.0',
        id: packet.id,
        type: 'response',
        timestamp: Date.now() + 2,
        payload: {
          status: 'error',
          errorCode: 'NO_CONNECTION',
          message: 'Windows client is not connected. Connect via Wi-Fi or Bluetooth.',
        },
      };
      this.logPacket(errResp);

      return {
        success: false,
        message: 'No connection to Windows client',
        packet,
      };
    }

    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(packet));
    } else if (isConnected) {
      // Mock mode reply
      setTimeout(() => {
        const mockResp: WindowsProtocolMessage = {
          version: '1.0.0',
          id: packet.id,
          type: 'response',
          timestamp: Date.now() + 5,
          payload: {
            status: 'success',
            action,
            result: params,
          },
        };
        this.logPacket(mockResp);
      }, 50);
    }

    return {
      success: true,
      message: `Dispatched ${action}`,
      packet,
    };
  }
}

export const protocolService = new WindowsProtocolService();
