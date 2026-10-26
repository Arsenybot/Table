import React, { useRef, useState } from 'react';
import { Sun, SunDim, SunMedium } from 'lucide-react';
import { protocolService } from '../../services/protocolService';

interface BrightnessWidgetProps {
  onFeedback?: (msg: string, isError?: boolean) => void;
}

export const BrightnessWidget: React.FC<BrightnessWidgetProps> = ({ onFeedback }) => {
  const [brightness, setBrightness] = useState<number>(() => {
    const saved = localStorage.getItem('wtpp_sim_brightness');
    return saved ? parseInt(saved, 10) : 75;
  });
  const trackRef = useRef<HTMLDivElement>(null);
  const isDragging = useRef<boolean>(false);

  const updateBrightness = (newVal: number) => {
    const clamped = Math.max(0, Math.min(100, Math.round(newVal)));
    setBrightness(clamped);
    localStorage.setItem('wtpp_sim_brightness', clamped.toString());

    const res = protocolService.sendCommand('SetBrightness', { level: clamped });
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
    const relativeY = rect.bottom - e.clientY;
    const pct = (relativeY / rect.height) * 100;
    updateBrightness(pct);
  };

  const handlePointerUp = (e: React.PointerEvent) => {
    isDragging.current = false;
    try {
      (e.target as HTMLElement).releasePointerCapture(e.pointerId);
    } catch {
      // ignore
    }
  };

  return (
    <div className="flex flex-col items-center justify-between h-full w-full py-1 select-none">
      <span className="font-mono text-xs font-bold text-slate-200">
        {brightness}%
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
          style={{ height: `${brightness}%` }}
          className="w-full rounded-full transition-all duration-75 flex items-start justify-center bg-gradient-to-t from-zinc-400 to-amber-200"
        >
          <div className="w-5 h-1 bg-black/50 rounded-full mt-1.5 shadow-sm" />
        </div>
      </div>

      {/* Icon */}
      <div className="p-2 text-slate-400 flex items-center justify-center">
        {brightness < 35 ? (
          <SunDim size={16} />
        ) : brightness < 70 ? (
          <SunMedium size={16} />
        ) : (
          <Sun size={16} />
        )}
      </div>
    </div>
  );
};
