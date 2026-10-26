import React, { useEffect, useState } from 'react';
import { X, Trash2, Copy, Check, Terminal, ArrowDownRight, ArrowUpLeft } from 'lucide-react';
import { protocolService } from '../../services/protocolService';
import { WindowsProtocolMessage } from '../../types';

interface ProtocolInspectorModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const ProtocolInspectorModal: React.FC<ProtocolInspectorModalProps> = ({
  isOpen,
  onClose,
}) => {
  if (!isOpen) return null;

  const [packets, setPackets] = useState<WindowsProtocolMessage[]>(
    protocolService.getPacketLog()
  );
  const [selectedPacket, setSelectedPacket] = useState<WindowsProtocolMessage | null>(
    packets[0] || null
  );
  const [copied, setCopied] = useState(false);

  useEffect(() => {
    return protocolService.onMessage(() => {
      const updated = protocolService.getPacketLog();
      setPackets(updated);
      if (!selectedPacket && updated.length > 0) {
        setSelectedPacket(updated[0]);
      }
    });
  }, [selectedPacket]);

  const handleCopy = () => {
    if (selectedPacket) {
      navigator.clipboard.writeText(JSON.stringify(selectedPacket, null, 2));
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
      <div className="w-full max-w-4xl h-[85vh] rounded-2xl bg-zinc-950 border border-white/10 shadow-2xl flex flex-col overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-white/10 bg-zinc-900/50">
          <div className="flex items-center gap-2">
            <Terminal size={18} className="text-purple-400" />
            <div>
              <h3 className="text-sm font-bold text-white tracking-wide">
                ИНСПЕКТОР ПРОТОКОЛА WTPP (v1.0.0)
              </h3>
              <p className="text-[11px] text-slate-400 font-mono">
                Трассировка JSON-команд и ответов Windows-клиента
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={() => {
                // Clear
                setPackets([]);
                setSelectedPacket(null);
              }}
              type="button"
              className="p-1.5 rounded-lg hover:bg-white/10 text-slate-400 hover:text-white"
              title="Очистить лог"
            >
              <Trash2 size={16} />
            </button>
            <button
              onClick={onClose}
              type="button"
              className="p-1.5 rounded-lg hover:bg-white/10 text-slate-400 hover:text-white"
            >
              <X size={16} />
            </button>
          </div>
        </div>

        {/* Content split pane */}
        <div className="flex-1 flex min-h-0 divide-x divide-white/10">
          {/* Packet List */}
          <div className="w-1/2 overflow-y-auto divide-y divide-white/5 font-mono text-xs">
            {packets.length === 0 ? (
              <div className="p-8 text-center text-slate-500">
                Пакетов ещё нет. Нажмите кнопки на панели или переместите
                ползунки для отправки команд.
              </div>
            ) : (
              packets.map((pkt) => {
                const isSelected = selectedPacket?.id === pkt.id;
                const isCmd = pkt.type === 'command';
                const isResp = pkt.type === 'response';
                const isError = pkt.payload.status === 'error';

                return (
                  <div
                    key={pkt.id}
                    onClick={() => setSelectedPacket(pkt)}
                    className={`p-3 cursor-pointer transition-colors ${
                      isSelected
                        ? 'bg-purple-950/40 text-purple-200 border-l-2 border-purple-500'
                        : 'hover:bg-zinc-900 text-slate-300'
                    }`}
                  >
                    <div className="flex items-center justify-between text-[10px] text-slate-500 mb-1">
                      <span className="flex items-center gap-1">
                        {isCmd ? (
                          <ArrowDownRight size={12} className="text-purple-400" />
                        ) : (
                          <ArrowUpLeft size={12} className={isError ? 'text-rose-400' : 'text-emerald-400'} />
                        )}
                        <span className="uppercase font-bold tracking-wider">
                          {pkt.type}
                        </span>
                      </span>
                      <span>{new Date(pkt.timestamp).toLocaleTimeString()}</span>
                    </div>

                    <div className="flex items-center justify-between">
                      <span className="font-semibold text-slate-200">
                        {pkt.payload.action || pkt.payload.status || 'Packet'}
                      </span>
                      <span className="text-[10px] text-slate-500 truncate max-w-[120px]">
                        {pkt.id}
                      </span>
                    </div>
                  </div>
                );
              })
            )}
          </div>

          {/* Packet Detail JSON Viewer */}
          <div className="w-1/2 flex flex-col min-h-0 bg-zinc-950">
            <div className="flex items-center justify-between px-4 py-2 border-b border-white/5 bg-zinc-900/40 text-xs">
              <span className="text-slate-400 font-mono text-[11px]">
                {selectedPacket ? selectedPacket.id : 'No packet selected'}
              </span>
              {selectedPacket && (
                <button
                  onClick={handleCopy}
                  type="button"
                  className="flex items-center gap-1 px-2 py-1 rounded bg-zinc-800 hover:bg-zinc-700 text-slate-300 text-[10px] cursor-pointer"
                >
                  {copied ? <Check size={12} /> : <Copy size={12} />}
                  {copied ? 'Скопировано' : 'JSON'}
                </button>
              )}
            </div>

            <div className="flex-1 overflow-auto p-4 font-mono text-xs text-purple-300/90 leading-relaxed">
              {selectedPacket ? (
                <pre>{JSON.stringify(selectedPacket, null, 2)}</pre>
              ) : (
                <span className="text-slate-600">Выберите пакет из списка слева</span>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
