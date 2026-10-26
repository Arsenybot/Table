import React from 'react';
import { Trash2, Settings, ArrowLeft, ArrowRight, Plus, Minus } from 'lucide-react';
import { WidgetModel } from '../../types';

interface WidgetContainerProps {
  widget: WidgetModel;
  isEditMode: boolean;
  onDelete?: () => void;
  onConfigure?: () => void;
  onMove?: (direction: 'left' | 'right') => void;
  onResize?: (delta: number) => void;
  children: React.ReactNode;
}

export const WidgetContainer: React.FC<WidgetContainerProps> = ({
  widget,
  isEditMode,
  onDelete,
  onConfigure,
  onMove,
  onResize,
  children,
}) => {
  return (
    <div
      style={{
        gridColumn: `span ${widget.colSpan}`,
        gridRow: `span ${widget.rowSpan}`,
      }}
      className={`relative flex flex-col rounded-2xl bg-zinc-950/80 backdrop-blur-md p-3.5 transition-all duration-150 border ${
        isEditMode
          ? 'border-purple-500/80 ring-1 ring-purple-500/50 shadow-lg shadow-purple-950/30'
          : 'border-white/10 hover:border-white/15 shadow-sm shadow-black/80'
      }`}
    >
      {/* Title & Edit Controls Bar */}
      <div className="flex items-center justify-between pb-1.5 mb-1">
        <span className="text-[10px] font-bold tracking-wider text-slate-400 uppercase select-none">
          {widget.title}
        </span>

        {isEditMode && (
          <div className="flex items-center gap-1 bg-black/60 rounded-lg p-0.5 border border-white/10">
            {onMove && (
              <>
                <button
                  onClick={() => onMove('left')}
                  className="p-1 rounded hover:bg-white/10 text-slate-400 hover:text-white transition-colors cursor-pointer"
                  title="Сдвинуть влево"
                  type="button"
                >
                  <ArrowLeft size={11} />
                </button>
                <button
                  onClick={() => onMove('right')}
                  className="p-1 rounded hover:bg-white/10 text-slate-400 hover:text-white transition-colors cursor-pointer"
                  title="Сдвинуть вправо"
                  type="button"
                >
                  <ArrowRight size={11} />
                </button>
              </>
            )}

            {onResize && (
              <>
                <button
                  onClick={() => onResize(-1)}
                  className="p-1 rounded hover:bg-white/10 text-slate-400 hover:text-white transition-colors cursor-pointer"
                  title="Уменьшить ширину"
                  type="button"
                >
                  <Minus size={11} />
                </button>
                <button
                  onClick={() => onResize(1)}
                  className="p-1 rounded hover:bg-white/10 text-slate-400 hover:text-white transition-colors cursor-pointer"
                  title="Увеличить ширину"
                  type="button"
                >
                  <Plus size={11} />
                </button>
              </>
            )}

            {onConfigure && (
              <button
                onClick={onConfigure}
                className="p-1 rounded hover:bg-white/10 text-slate-400 hover:text-purple-400 transition-colors cursor-pointer"
                title="Настройки виджета"
                type="button"
              >
                <Settings size={11} />
              </button>
            )}

            {widget.isRemovable && onDelete && (
              <button
                onClick={onDelete}
                className="p-1 rounded hover:bg-rose-500/20 text-slate-400 hover:text-rose-400 transition-colors cursor-pointer"
                title="Удалить виджет"
                type="button"
              >
                <Trash2 size={11} />
              </button>
            )}
          </div>
        )}
      </div>

      {/* Widget Content */}
      <div className="flex-1 w-full min-h-0 overflow-hidden">{children}</div>
    </div>
  );
};
