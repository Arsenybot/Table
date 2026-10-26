import React, { useEffect, useState } from 'react';
import { X, Wifi, Bluetooth, CheckCircle2, AlertCircle, RefreshCw } from 'lucide-react';
import { protocolService } from '../../services/protocolService';
import { ConnectionConfig, ConnectionStatus, TransportType } from '../../types';

interface ConnectionModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const ConnectionModal: React.FC<ConnectionModalProps> = ({
  isOpen,
  onClose,
}) => {
  if (!isOpen) return null;

  const [config, setConfig] = useState<ConnectionConfig>(protocolService.getConfig());
  const [status, setStatus] = useState<ConnectionStatus>(protocolService.getStatus());
  const [isConnecting, setIsConnecting] = useState(false);
  const [statusDetail, setStatusDetail] = useState<string>('');

  useEffect(() => {
    return protocolService.onStatusChange((s, detail) => {
      setStatus(s);
      if (detail) setStatusDetail(detail);
      if (s === 'connected' || s === 'error') {
        setIsConnecting(false);
      }
    });
  }, []);

  const handleConnect = async (mockMode = false) => {
    setIsConnecting(true);
    protocolService.updateConfig(config);
    await protocolService.connect(mockMode);
  };

  const handleDisconnect = () => {
    protocolService.disconnect();
    setStatus('disconnected');
    setStatusDetail('Disconnected');
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
      <div className="w-full max-w-lg rounded-2xl bg-zinc-950 border border-white/10 shadow-2xl p-6">
        <div className="flex items-center justify-between pb-4 border-b border-white/10">
          <div>
            <h3 className="text-sm font-bold text-white tracking-wide">
              ПОДКЛЮЧЕНИЕ К WINDOWS
            </h3>
            <p className="text-xs text-slate-400 mt-0.5">
              Протокол WTPP v1.0.0 (WebSocket / Bluetooth SPP)
            </p>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg hover:bg-white/10 text-slate-400 hover:text-white"
            type="button"
          >
            <X size={16} />
          </button>
        </div>

        {/* Status Alert Banner */}
        <div className="mt-4 p-3 rounded-xl bg-zinc-900 border border-white/5 flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <div
              className={`h-3 w-3 rounded-full ${
                status === 'connected'
                  ? 'bg-emerald-500 shadow-sm shadow-emerald-500/50'
                  : status === 'connecting'
                  ? 'bg-amber-500 animate-pulse'
                  : 'bg-rose-500'
              }`}
            />
            <div>
              <span className="text-xs font-bold text-slate-200">
                {status === 'connected'
                  ? 'Подключено к Windows'
                  : status === 'connecting'
                  ? 'Соединение...'
                  : 'Нет соединения с Windows'}
              </span>
              {statusDetail && (
                <p className="text-[10px] text-slate-400 font-mono mt-0.5">
                  {statusDetail}
                </p>
              )}
            </div>
          </div>

          {status === 'connected' ? (
            <button
              onClick={handleDisconnect}
              type="button"
              className="px-3 py-1.5 rounded-lg bg-rose-500/20 hover:bg-rose-500/30 text-rose-300 text-xs font-semibold cursor-pointer"
            >
              Отключить
            </button>
          ) : null}
        </div>

        {/* Transport Type Tabs */}
        <div className="mt-4">
          <label className="block text-xs font-semibold text-slate-300 mb-1.5">
            Интерфейс связи
          </label>
          <div className="grid grid-cols-2 gap-2">
            <button
              type="button"
              onClick={() => setConfig({ ...config, transport: 'wifi_websocket' })}
              className={`flex items-center justify-center gap-2 py-2.5 rounded-xl border text-xs font-semibold transition-all cursor-pointer ${
                config.transport === 'wifi_websocket'
                  ? 'bg-purple-600/20 border-purple-500 text-purple-300'
                  : 'bg-zinc-900/50 border-white/5 text-slate-400 hover:text-white'
              }`}
            >
              <Wifi size={14} />
              Wi-Fi (WebSocket)
            </button>

            <button
              type="button"
              onClick={() => setConfig({ ...config, transport: 'bluetooth_spp' })}
              className={`flex items-center justify-center gap-2 py-2.5 rounded-xl border text-xs font-semibold transition-all cursor-pointer ${
                config.transport === 'bluetooth_spp'
                  ? 'bg-purple-600/20 border-purple-500 text-purple-300'
                  : 'bg-zinc-900/50 border-white/5 text-slate-400 hover:text-white'
              }`}
            >
              <Bluetooth size={14} />
              Bluetooth SPP
            </button>
          </div>
        </div>

        {/* Parameters Form */}
        <div className="mt-4 space-y-3 text-xs">
          <div className="grid grid-cols-3 gap-2">
            <div className="col-span-2">
              <label className="block text-slate-300 font-semibold mb-1">
                IP адрес Windows ПК
              </label>
              <input
                type="text"
                value={config.host}
                onChange={(e) => setConfig({ ...config, host: e.target.value })}
                className="w-full px-3 py-2 rounded-lg bg-zinc-900 border border-white/10 text-white font-mono focus:outline-hidden focus:border-purple-500"
              />
            </div>
            <div>
              <label className="block text-slate-300 font-semibold mb-1">
                Порт
              </label>
              <input
                type="number"
                value={config.port}
                onChange={(e) =>
                  setConfig({ ...config, port: parseInt(e.target.value, 10) || 8088 })
                }
                className="w-full px-3 py-2 rounded-lg bg-zinc-900 border border-white/10 text-white font-mono focus:outline-hidden focus:border-purple-500"
              />
            </div>
          </div>

          <div>
            <label className="block text-slate-300 font-semibold mb-1">
              Пин-код сопряжения (6 цифр)
            </label>
            <input
              type="text"
              maxLength={6}
              value={config.pin}
              onChange={(e) => setConfig({ ...config, pin: e.target.value })}
              className="w-full px-3 py-2 rounded-lg bg-zinc-900 border border-white/10 text-white font-mono tracking-widest text-center text-sm focus:outline-hidden focus:border-purple-500"
            />
          </div>
        </div>

        {/* Action Buttons */}
        <div className="mt-6 pt-4 border-t border-white/10 flex flex-col gap-2">
          <div className="flex gap-2">
            <button
              onClick={() => handleConnect(false)}
              disabled={isConnecting}
              type="button"
              className="flex-1 py-2.5 rounded-xl bg-purple-600 hover:bg-purple-500 disabled:opacity-50 text-white font-bold text-xs flex items-center justify-center gap-1.5 transition-all shadow-md shadow-purple-600/30 cursor-pointer"
            >
              {isConnecting ? (
                <>
                  <RefreshCw size={14} className="animate-spin" />
                  Подключение к реальному ПК...
                </>
              ) : (
                'Подключиться по сети'
              )}
            </button>

            <button
              onClick={() => handleConnect(true)}
              type="button"
              className="py-2.5 px-3 rounded-xl bg-zinc-800 hover:bg-zinc-700 text-purple-300 font-semibold text-xs border border-purple-500/30 cursor-pointer"
              title="Включить симуляцию без запуска внешнего скрипта Windows"
            >
              Тестовый режим (Демо)
            </button>
          </div>

          <p className="text-[10px] text-slate-500 text-center leading-relaxed mt-1">
            * Для работы с реальным Windows ПК запустите фоновый сервер WTPP
            (см. <code className="text-purple-400">docs/protocol.md</code>)
          </p>
        </div>
      </div>
    </div>
  );
};
