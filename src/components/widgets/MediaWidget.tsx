import React, { useState } from 'react';
import { Play, Pause, SkipBack, SkipForward, Disc3 } from 'lucide-react';
import { protocolService } from '../../services/protocolService';
import { MediaTrack } from '../../types';

interface MediaWidgetProps {
  onFeedback?: (msg: string, isError?: boolean) => void;
}

export const MediaWidget: React.FC<MediaWidgetProps> = ({ onFeedback }) => {
  const [track, setTrack] = useState<MediaTrack>({
    title: 'Midnight City',
    artist: 'M83',
    album: "Hurry Up, We're Dreaming",
    duration: 243,
    progress: 88,
    isPlaying: false,
  });

  const togglePlay = () => {
    const nextState = !track.isPlaying;
    setTrack((prev) => ({ ...prev, isPlaying: nextState }));
    const res = protocolService.sendCommand('MediaPlayPause', {});
    if (!res.success && onFeedback) {
      onFeedback(res.message, true);
    }
  };

  const handleNext = () => {
    const res = protocolService.sendCommand('MediaNext', {});
    if (!res.success && onFeedback) {
      onFeedback(res.message, true);
    } else {
      setTrack({
        title: 'Starboy',
        artist: 'The Weeknd, Daft Punk',
        album: 'Starboy',
        duration: 230,
        progress: 12,
        isPlaying: true,
      });
    }
  };

  const handlePrev = () => {
    const res = protocolService.sendCommand('MediaPrevious', {});
    if (!res.success && onFeedback) {
      onFeedback(res.message, true);
    } else {
      setTrack({
        title: 'Veridis Quo',
        artist: 'Daft Punk',
        album: 'Discovery',
        duration: 345,
        progress: 4,
        isPlaying: true,
      });
    }
  };

  const formatSec = (s: number) => {
    const mins = Math.floor(s / 60);
    const secs = Math.floor(s % 60)
      .toString()
      .padStart(2, '0');
    return `${mins}:${secs}`;
  };

  const progressPercent = (track.progress / track.duration) * 100;

  return (
    <div className="flex flex-col justify-between h-full w-full select-none py-0.5">
      {/* Top Track Info */}
      <div className="flex items-center gap-3">
        {/* Album Art with rotation when playing */}
        <div className="relative h-11 w-11 rounded-lg bg-zinc-900 border border-white/10 flex items-center justify-center overflow-hidden shrink-0 shadow-sm">
          <Disc3
            className={`text-purple-400 ${
              track.isPlaying ? 'animate-spin' : ''
            }`}
            style={{ animationDuration: '4s' }}
            size={24}
          />
        </div>

        <div className="flex-1 min-w-0">
          <div className="text-xs font-bold text-slate-100 truncate">
            {track.title}
          </div>
          <div className="text-[11px] text-slate-400 truncate">
            {track.artist}
          </div>
        </div>

        {/* Mini waveform animation */}
        {track.isPlaying && (
          <div className="flex items-end gap-0.5 h-4 shrink-0">
            <span className="w-0.5 h-3 bg-purple-500 rounded-full animate-pulse" />
            <span className="w-0.5 h-4 bg-purple-400 rounded-full animate-bounce" />
            <span className="w-0.5 h-2 bg-purple-600 rounded-full animate-pulse" />
          </div>
        )}
      </div>

      {/* Scrub Bar */}
      <div className="my-2">
        <div className="h-1.5 w-full bg-zinc-900 rounded-full overflow-hidden border border-white/5">
          <div
            style={{ width: `${progressPercent}%` }}
            className="h-full bg-purple-500 rounded-full transition-all duration-300"
          />
        </div>
        <div className="flex justify-between text-[9px] font-mono text-slate-500 mt-1">
          <span>{formatSec(track.progress)}</span>
          <span>{formatSec(track.duration)}</span>
        </div>
      </div>

      {/* Media Key Buttons */}
      <div className="flex items-center justify-center gap-4">
        <button
          onClick={handlePrev}
          type="button"
          className="p-1.5 rounded-full hover:bg-white/10 text-slate-300 hover:text-white transition-colors cursor-pointer"
          title="Предыдущий трек"
        >
          <SkipBack size={16} />
        </button>

        <button
          onClick={togglePlay}
          type="button"
          className="p-2.5 rounded-full bg-purple-600 hover:bg-purple-500 text-white shadow-md shadow-purple-600/30 transition-transform active:scale-95 cursor-pointer"
          title={track.isPlaying ? 'Пауза' : 'Воспроизведение'}
        >
          {track.isPlaying ? <Pause size={16} fill="currentColor" /> : <Play size={16} fill="currentColor" />}
        </button>

        <button
          onClick={handleNext}
          type="button"
          className="p-1.5 rounded-full hover:bg-white/10 text-slate-300 hover:text-white transition-colors cursor-pointer"
          title="Следующий трек"
        >
          <SkipForward size={16} />
        </button>
      </div>
    </div>
  );
};
