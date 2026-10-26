import React, { useEffect, useState } from 'react';
import { Phone, PhoneCall, PhoneOff, ExternalLink, Info } from 'lucide-react';
import { mangoService } from '../../services/mangoService';
import { MangoCallInfo } from '../../types';

interface MangoTalkerWidgetProps {
  onOpenInspector?: () => void;
  onFeedback?: (msg: string, isError?: boolean) => void;
}

export const MangoTalkerWidget: React.FC<MangoTalkerWidgetProps> = ({
  onOpenInspector,
  onFeedback,
}) => {
  const [callInfo, setCallInfo] = useState<MangoCallInfo>(mangoService.getCallInfo());

  useEffect(() => {
    return mangoService.subscribe((info) => setCallInfo(info));
  }, []);

  const formatSec = (s: number) => {
    const mins = Math.floor(s / 60);
    const secs = Math.floor(s % 60)
      .toString()
      .padStart(2, '0');
    return `${mins}:${secs}`;
  };

  const handleLaunchMangoIntent = () => {
    // Generate Android tel: or intent: URI
    const uri = mangoService.getDialerIntentUri();
    if (onFeedback) {
      onFeedback(`Triggered Intent: ${uri}`, false);
    }
  };

  return (
    <div className="flex flex-col justify-between h-full w-full select-none">
      {/* State: Incoming Call */}
      {callInfo.status === 'incoming_call' && (
        <div className="flex flex-col justify-between h-full">
          <div className="flex items-center gap-3">
            <div className="relative flex items-center justify-center">
              <span className="absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75 animate-ping" />
              <div className="relative h-10 w-10 rounded-full bg-emerald-500 flex items-center justify-center text-white shadow-lg shadow-emerald-500/40">
                <PhoneCall size={20} className="animate-bounce" />
              </div>
            </div>

            <div className="flex-1 min-w-0">
              <div className="text-xs font-bold text-white truncate">
                {callInfo.callerName}
              </div>
              <div className="text-[11px] text-emerald-400 font-mono">
                {callInfo.phoneNumber}
              </div>
              <div className="text-[9px] text-slate-400">
                Входящий звонок Mango Talker...
              </div>
            </div>
          </div>

          <div className="flex items-center gap-2 mt-2">
            <button
              onClick={() => mangoService.answerCall()}
              type="button"
              className="flex-1 py-1.5 px-3 rounded-lg bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-bold flex items-center justify-center gap-1.5 transition-colors shadow-sm cursor-pointer"
            >
              <Phone size={13} />
              Ответить
            </button>
            <button
              onClick={() => mangoService.declineCall()}
              type="button"
              className="flex-1 py-1.5 px-3 rounded-lg bg-rose-600 hover:bg-rose-500 text-white text-xs font-bold flex items-center justify-center gap-1.5 transition-colors shadow-sm cursor-pointer"
            >
              <PhoneOff size={13} />
              Отклонить
            </button>
          </div>
        </div>
      )}

      {/* State: Active Call */}
      {callInfo.status === 'active_call' && (
        <div className="flex flex-col justify-between h-full">
          <div className="flex items-center gap-3">
            <div className="h-10 w-10 rounded-full bg-emerald-600/30 border border-emerald-500/50 flex items-center justify-center text-emerald-400">
              <Phone size={18} />
            </div>
            <div className="flex-1 min-w-0">
              <div className="text-xs font-bold text-white truncate">
                {callInfo.callerName}
              </div>
              <div className="text-[11px] text-slate-300 font-mono">
                {callInfo.phoneNumber}
              </div>
              <div className="text-[10px] text-emerald-400 font-mono font-semibold">
                Разговор • {formatSec(callInfo.durationSeconds)}
              </div>
            </div>
          </div>

          <button
            onClick={() => mangoService.endCall()}
            type="button"
            className="w-full mt-2 py-1.5 px-3 rounded-lg bg-rose-600/90 hover:bg-rose-500 text-white text-xs font-bold flex items-center justify-center gap-1.5 transition-colors cursor-pointer"
          >
            <PhoneOff size={13} />
            Завершить звонок
          </button>
        </div>
      )}

      {/* State: Call Ended */}
      {callInfo.status === 'call_ended' && (
        <div className="flex flex-col items-center justify-center h-full text-center py-2">
          <div className="text-xs font-semibold text-slate-300">
            Звонок завершён
          </div>
          <div className="text-[10px] text-slate-500 mt-1">
            Mango Talker возвращён в режим ожидания
          </div>
        </div>
      )}

      {/* State: App Ready / Not Configured / Standby */}
      {(callInfo.status === 'app_available' ||
        callInfo.status === 'not_configured') && (
        <div className="flex flex-col justify-between h-full">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <div className="h-7 w-7 rounded-lg bg-purple-500/15 border border-purple-500/30 flex items-center justify-center text-purple-400">
                <Phone size={14} />
              </div>
              <div>
                <div className="text-xs font-bold text-slate-200">
                  Mango Talker
                </div>
                <div className="text-[10px] text-slate-400">
                  com.mangotele.mtalker
                </div>
              </div>
            </div>

            {onOpenInspector && (
              <button
                onClick={onOpenInspector}
                type="button"
                className="p-1 rounded text-slate-400 hover:text-purple-300 hover:bg-white/5 transition-colors cursor-pointer"
                title="Анализ APK и документация"
              >
                <Info size={14} />
              </button>
            )}
          </div>

          <div className="text-[10px] text-slate-400 leading-relaxed my-1">
            Интеграция готова. Входящие звонки и быстрый вызов через системный
            Intent.
          </div>

          <div className="flex items-center gap-1.5">
            <button
              onClick={() => mangoService.triggerIncomingCall()}
              type="button"
              className="flex-1 py-1 px-2 rounded-lg bg-purple-600/30 hover:bg-purple-600/50 border border-purple-500/40 text-purple-200 text-[10px] font-semibold transition-colors cursor-pointer"
            >
              Тест звонка
            </button>
            <button
              onClick={handleLaunchMangoIntent}
              type="button"
              className="py-1 px-2 rounded-lg bg-zinc-800 hover:bg-zinc-700 text-slate-300 text-[10px] font-medium flex items-center gap-1 transition-colors cursor-pointer"
              title="Открыть Intent в Android"
            >
              <ExternalLink size={11} />
              Intent
            </button>
          </div>
        </div>
      )}
    </div>
  );
};
