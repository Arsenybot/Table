import React from 'react';
import { Terminal, AppWindow, PlayCircle } from 'lucide-react';
import { protocolService } from '../../services/protocolService';
import { WidgetModel } from '../../types';

interface CustomButtonWidgetProps {
  model: WidgetModel;
  onFeedback?: (msg: string, isError?: boolean) => void;
}

export const CustomButtonWidget: React.FC<CustomButtonWidgetProps> = ({
  model,
  onFeedback,
}) => {
  const label = model.config?.label || model.title;
  const appId = model.config?.appId || 'wt';

  const handleClick = () => {
    const res = protocolService.sendCommand('LaunchApplication', {
      appId,
    });
    if (onFeedback) {
      onFeedback(res.message, !res.success);
    }
  };

  return (
    <button
      onClick={handleClick}
      type="button"
      className="flex items-center justify-center gap-2 h-full w-full rounded-xl bg-zinc-900/90 hover:bg-zinc-800 border border-white/5 hover:border-purple-500/40 active:scale-95 transition-all text-slate-200 hover:text-white cursor-pointer px-3"
    >
      <Terminal size={16} className="text-purple-400 shrink-0" />
      <span className="text-xs font-semibold truncate tracking-tight">
        {label}
      </span>
    </button>
  );
};
