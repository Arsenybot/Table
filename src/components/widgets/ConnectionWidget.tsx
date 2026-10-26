import React, { useEffect, useState } from 'react';
import { Wifi, Bluetooth, Radio } from 'lucide-react';
import { protocolService } from '../../services/protocolService';
import { ConnectionStatus } from '../../types';

interface ConnectionWidgetProps {
  onOpenSettings: () => void;
}

export const ConnectionWidget: React.FC<ConnectionWidgetProps> = ({ onOpenSettings }) => {
  const [status, setStatus] = useState<ConnectionStatus>(protocolService.getStatus());
  const config = protocolService.getConfig();

  useEffect(() => {
    return protocolService.onStatusChange((s) => setStatus(s));
  }, []);

  const isConnected = status === 'connected';
  const isConnecting = status === 'connecting';

  return (
    <div
      onClick={onOpenSettings}
      className="flex items-center justify-between h-full w-full select-none cursor-pointer group"
      title="Нажмите для настройки подключения"
    >
      <div className="flex items-center gap-2">
        <div className="relative flex items-center justify-center">
          {isConnected && (
            <span className="absolute inline-flex h-3 w-3 rounded-full bg-emerald-400 opacity-75 animate-ping" />
          )}
          <div
            className={`h-2.5 w-2.5 rounded-full ${
              isConnected
                ? 'bg-emerald-500'
                : isConnecting
                ? 'bg-amber-500 animate-pulse'
                : 'bg-rose-500'
            }`}
          />
        </div>

        <div>
          <div className="text-[11px] font-bold text-slate-200 group-hover:text-purple-300 transition-colors">
            {isConnected ? 'Windows Host' : isConnecting ? 'Подключение...' : 'Офлайн'}
          </div>
          <div className="text-[9px] text-slate-400 font-mono">
            {isConnected ? `${config.host} • 12ms` : 'Нет соединения'}
          </div>
        </div>
      </div>

      <div className="flex items-center gap-1.5 text-slate-500">
        <Wifi
          size={14}
          className={isConnected ? 'text-emerald-400' : 'text-slate-600'}
        />
        <Bluetooth size={14} className="text-slate-600" />
      </div>
    </div>
  );
};
