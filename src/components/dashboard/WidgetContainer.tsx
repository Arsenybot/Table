import React from 'react';
import { WidgetModel } from '../../types';

interface WidgetContainerProps {
  widget: WidgetModel;
  isEditMode: boolean;
  isSelected: boolean;
  isDragging?: boolean;
  onClick?: () => void;
  children: React.ReactNode;
}

export const WidgetContainer: React.FC<WidgetContainerProps> = ({
  widget,
  isEditMode,
  isSelected,
  isDragging = false,
  onClick,
  children,
}) => {
  return (
    <div
      onClick={isEditMode ? onClick : undefined}
      className={`relative flex flex-col h-full w-full rounded-2xl p-2.5 sm:p-3 transition-all duration-150 select-none overflow-hidden ${
        isDragging
          ? 'bg-zinc-900/90 shadow-2xl scale-[1.02] border-purple-500/80 z-30'
          : isSelected
          ? 'bg-zinc-900/90 border-2 border-purple-500 ring-2 ring-purple-500/40 shadow-xl shadow-purple-950/40'
          : isEditMode
          ? 'bg-zinc-950/80 border border-white/10 hover:border-purple-500/50 cursor-pointer'
          : 'bg-zinc-950/80 border border-white/10 hover:border-white/15 shadow-sm shadow-black/80'
      }`}
    >
      {/* Title Bar */}
      <div className="flex items-center justify-between pb-1 mb-1 shrink-0">
        <span
          className={`text-[9.5px] font-bold tracking-wider uppercase truncate ${
            isSelected ? 'text-purple-400' : 'text-slate-400'
          }`}
        >
          {widget.title}
        </span>

        {isSelected && (
          <span className="h-1.5 w-1.5 rounded-full bg-purple-500 animate-pulse shrink-0 ml-1" />
        )}
      </div>

      {/* Widget Content */}
      <div className="flex-1 w-full min-h-0 overflow-hidden relative">
        {children}
      </div>
    </div>
  );
};
