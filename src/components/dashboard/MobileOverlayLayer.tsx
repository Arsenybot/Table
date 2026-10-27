import React from 'react';
import {
  Trash2,
  Settings,
  Plus,
  Minus,
  Check,
  ArrowRight,
  ArrowLeft,
  Move,
} from 'lucide-react';
import { WidgetModel } from '../../types';

interface MobileOverlayLayerProps {
  selectedWidget: WidgetModel | null;
  selectedBox: { x: number; y: number; width: number; height: number } | null;
  isDragging: boolean;
  ghostBox: { x: number; y: number; width: number; height: number } | null;
  currentPage: number;
  pageCount: number;
  containerWidth: number;
  onResizeWidth: (delta: number) => void;
  onResizeHeight: (delta: number) => void;
  onMoveToPage: (targetPage: number) => void;
  onConfigure?: () => void;
  onDelete: () => void;
  onDeselect: () => void;
  onResizeHandlePointerDown?: (e: React.PointerEvent) => void;
}

export const MobileOverlayLayer: React.FC<MobileOverlayLayerProps> = ({
  selectedWidget,
  selectedBox,
  isDragging,
  ghostBox,
  currentPage,
  pageCount,
  containerWidth,
  onResizeWidth,
  onResizeHeight,
  onMoveToPage,
  onConfigure,
  onDelete,
  onDeselect,
  onResizeHandlePointerDown,
}) => {
  return (
    <div className="absolute inset-0 pointer-events-none z-40 overflow-visible">
      {/* Ghost Drop Target Slot (Layer 2) */}
      {isDragging && ghostBox && (
        <div
          style={{
            transform: `translate3d(${ghostBox.x}px, ${ghostBox.y}px, 0)`,
            width: ghostBox.width,
            height: ghostBox.height,
          }}
          className="absolute rounded-2xl border-2 border-dashed border-purple-500 bg-purple-500/10 transition-all duration-75 pointer-events-none"
        />
      )}

      {/* Selected Widget Decorators (Layer 4 & 5) */}
      {selectedWidget && selectedBox && !isDragging && (
        <>
          {/* 44x44px Touch Resize Handle at bottom right of selected card */}
          <div
            style={{
              transform: `translate3d(${selectedBox.x + selectedBox.width - 24}px, ${
                selectedBox.y + selectedBox.height - 24
              }px, 0)`,
            }}
            onPointerDown={onResizeHandlePointerDown}
            className="absolute w-11 h-11 flex items-center justify-center cursor-se-resize pointer-events-auto touch-none select-none z-50 group"
            title="Потяните для изменения размера (Resize Handle 44dp)"
          >
            <div className="w-6 h-6 rounded-full bg-purple-600 text-white flex items-center justify-center shadow-lg border-2 border-white ring-2 ring-purple-500/40 group-active:scale-125 transition-transform">
              <Move size={12} className="rotate-45" />
            </div>
          </div>

          {/* Floating Context Toolbar (Layer 5) - Anchored ABOVE or BELOW outside the card */}
          <div
            style={{
              transform: `translate3d(${Math.max(
                8,
                Math.min(
                  containerWidth - 320,
                  selectedBox.x + selectedBox.width / 2 - 150
                )
              )}px, ${
                selectedBox.y > 60
                  ? selectedBox.y - 52
                  : selectedBox.y + selectedBox.height + 10
              }px, 0)`,
            }}
            className="absolute flex items-center gap-1 bg-zinc-950/95 border border-purple-500/60 rounded-full px-2 py-1 shadow-2xl backdrop-blur-md pointer-events-auto select-none z-50"
          >
            {/* Width - / + */}
            <div className="flex items-center bg-black/60 rounded-full p-0.5 border border-white/10">
              <button
                type="button"
                onClick={() => onResizeWidth(-1)}
                className="w-8 h-8 rounded-full flex items-center justify-center text-slate-300 hover:text-white hover:bg-white/10 active:scale-95 transition"
                title="Уменьшить ширину (-1)"
              >
                <Minus size={13} />
              </button>
              <span className="text-[10px] text-slate-400 font-mono px-1">
                W: {selectedWidget.colSpan}
              </span>
              <button
                type="button"
                onClick={() => onResizeWidth(1)}
                className="w-8 h-8 rounded-full flex items-center justify-center text-slate-300 hover:text-white hover:bg-white/10 active:scale-95 transition"
                title="Увеличить ширину (+1)"
              >
                <Plus size={13} />
              </button>
            </div>

            {/* Height - / + */}
            <div className="flex items-center bg-black/60 rounded-full p-0.5 border border-white/10">
              <button
                type="button"
                onClick={() => onResizeHeight(-1)}
                className="w-8 h-8 rounded-full flex items-center justify-center text-slate-300 hover:text-white hover:bg-white/10 active:scale-95 transition"
                title="Уменьшить высоту (-1)"
              >
                <Minus size={13} />
              </button>
              <span className="text-[10px] text-slate-400 font-mono px-1">
                H: {selectedWidget.rowSpan}
              </span>
              <button
                type="button"
                onClick={() => onResizeHeight(1)}
                className="w-8 h-8 rounded-full flex items-center justify-center text-slate-300 hover:text-white hover:bg-white/10 active:scale-95 transition"
                title="Увеличить высоту (+1)"
              >
                <Plus size={13} />
              </button>
            </div>

            {/* Page move button if multiple pages */}
            {pageCount > 1 && (
              <button
                type="button"
                onClick={() => onMoveToPage(currentPage === 0 ? 1 : 0)}
                className="w-8 h-8 rounded-full flex items-center justify-center text-purple-400 hover:text-purple-300 hover:bg-purple-900/30 active:scale-95 transition border border-purple-500/30"
                title={`Перенести на страницу ${currentPage === 0 ? 2 : 1}`}
              >
                {currentPage === 0 ? <ArrowRight size={13} /> : <ArrowLeft size={13} />}
              </button>
            )}

            {/* Settings */}
            {onConfigure && (
              <button
                type="button"
                onClick={onConfigure}
                className="w-8 h-8 rounded-full flex items-center justify-center text-slate-300 hover:text-purple-400 hover:bg-white/10 active:scale-95 transition"
                title="Параметры виджета"
              >
                <Settings size={13} />
              </button>
            )}

            {/* Delete */}
            {selectedWidget.isRemovable && (
              <button
                type="button"
                onClick={onDelete}
                className="w-8 h-8 rounded-full flex items-center justify-center text-rose-400 hover:text-rose-300 hover:bg-rose-950/40 active:scale-95 transition"
                title="Удалить виджет"
              >
                <Trash2 size={13} />
              </button>
            )}

            {/* Done / Deselect */}
            <button
              type="button"
              onClick={onDeselect}
              className="w-8 h-8 rounded-full flex items-center justify-center bg-purple-600 hover:bg-purple-500 text-white active:scale-95 transition shadow-sm ml-1"
              title="Готово"
            >
              <Check size={14} />
            </button>
          </div>
        </>
      )}
    </div>
  );
};
