import React, { useRef, useState, useEffect } from 'react';
import { Volume2, VolumeX, Volume1 } from 'lucide-react';
import { protocolService } from '../../services/protocolService';

interface VolumeWidgetProps {
  onFeedback?: (msg: string, isError?: boolean) => void;
}

export const VolumeWidget: React.FC<VolumeWidgetProps> = ({ onFeedback }) => {
  const [volume, setVolume] = useState<number>(() => {
    const saved = localStorage.getItem('wtpp_sim_volume');
    return saved ? parseInt(saved, 10) : 60;
  });
  const [isMuted, setIsMuted] = useState<boolean>(false);
  const trackRef = useRef<HTMLDivElement>(null);
  const isDragging = useRef<boolean>(false);

  const updateVolume = (newVal: number) => {
    const clamped = Math.max(0, Math.min(100, Math.round(newVal)));
    setVolume(clamped);
    localStorage.setItem('wtpp_sim_volume', clamped.toString());

    const res = protocolService.sendCommand('SetVolume', { level: clamped });
    if (!res.success && onFeedback) {
      onFeedback(res.message, true);
    }
  };

  const toggleMute = () => {
    const newMute = !isMuted;
    setIsMuted(newMute);
    const res = protocolService.sendCommand('ToggleMute', {});
    if (!res.success && onFeedback) {
      onFeedback(res.message, true);
    }
  };

  const handlePointerDown = (e: React.PointerEvent) => {
    isDragging.current = true;
    (e.target as HTMLElement).setPointerCapture(e.pointerId);
    handlePointerMove(e);
  };

  const handlePointerMove = (e: React.PointerEvent) => {
    if (!isDragging.current || !trackRef.current) return;
    const rect = trackRef.current.getBoundingClientRect();
    // Bottom is 0, Top is 100
    const relativeY = rect.bottom - e.clientY;
    const pct = (relativeY / rect.height) * 100;
    updateVolume(pct);
  };

  const handlePointerUp = (e: React.PointerEvent) => {
    isDragging.current = false;
    try {
      (e.target as HTMLElement).releasePointerCapture(e.pointerId);
    } catch {
      // ignore
    }
  };

  const displayPct = isMuted ? 0 : volume;

  return (
    <div className="flex flex-col items-center justify-between h-full w-full py-1 select-none">
      {/* Top Value */}
      <span className="font-mono text-xs font-bold text-slate-200">
        {isMuted ? 'MUTE' : `${volume}%`}
      </span>

      {/* Vertical Slider Track */}
      <div
        ref={trackRef}
        onPointerDown={handlePointerDown}
        onPointerMove={handlePointerMove}
        onPointerUp={handlePointerUp}
        className="relative w-10 sm:w-11 flex-1 my-2 bg-zinc-900 rounded-full border border-white/10 overflow-hidden cursor-ns-resize shadow-inner flex flex-col justify-end p-1 touch-none"
      >
        {/* Fill */}
        <div
          style={{ height: `${displayPct}%` }}
          className={`w-full rounded-full transition-all duration-75 flex items-start justify-center ${
            isMuted
              ? 'bg-zinc-700'
              : volume > 80
              ? 'bg-gradient-to-t from-purple-600 to-amber-500'
              : 'bg-purple-600'
          }`}
        >
          {/* Subtle grab line indicator */}
          <div className="w-5 h-1 bg-white/70 rounded-full mt-1.5 shadow-sm" />
        </div>
      </div>

      {/* Mute button */}
      <button
        onClick={toggleMute}
        type="button"
        className={`p-2 rounded-lg transition-colors cursor-pointer ${
          isMuted
            ? 'bg-rose-500/20 text-rose-400 border border-rose-500/40'
            : 'hover:bg-white/10 text-slate-400 hover:text-white'
        }`}
        title={isMuted ? 'Включить звук' : 'Выключить звук'}
      >
        {isMuted ? (
          <VolumeX size={16} />
        ) : volume < 40 ? (
          <Volume1 size={16} />
        ) : (
          <Volume2 size={16} />
        )}
      </button>
    </div>
  );
};
