export type WidgetType =
  | 'clock'
  | 'calendar'
  | 'volume'
  | 'brightness'
  | 'media'
  | 'quick_actions'
  | 'connection'
  | 'mango_talker'
  | 'battery'
  | 'custom_button';

export interface WidgetModel {
  id: string;
  type: WidgetType;
  title: string;
  colSpan: number; // 1-12
  rowSpan: number; // 1-6
  isRemovable: boolean;
  config?: Record<string, string>;
}

export interface DashboardLayout {
  version: number;
  columns: number;
  widgets: WidgetModel[];
}

export type ConnectionStatus = 'disconnected' | 'connecting' | 'connected' | 'error';
export type TransportType = 'wifi_websocket' | 'bluetooth_spp';

export interface ConnectionConfig {
  host: string;
  port: number;
  transport: TransportType;
  pin: string;
  autoReconnect: boolean;
}

export type MangoCallStatus =
  | 'not_configured'
  | 'app_not_installed'
  | 'app_available'
  | 'incoming_call'
  | 'active_call'
  | 'call_ended'
  | 'integration_error';

export interface MangoCallInfo {
  status: MangoCallStatus;
  callerName: string;
  phoneNumber: string;
  durationSeconds: number;
  timestamp?: number;
}

export interface WindowsProtocolMessage {
  version: string;
  id: string;
  type: 'command' | 'response' | 'event' | 'handshake';
  timestamp: number;
  payload: {
    action?: string;
    params?: Record<string, unknown>;
    status?: 'success' | 'error' | 'authenticated';
    errorCode?: string;
    message?: string;
    result?: Record<string, unknown>;
    [key: string]: unknown;
  };
}

export interface MediaTrack {
  title: string;
  artist: string;
  album: string;
  duration: number; // seconds
  progress: number; // seconds
  isPlaying: boolean;
}
