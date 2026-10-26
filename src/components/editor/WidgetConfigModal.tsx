import React, { useState } from 'react';
import { X, Check } from 'lucide-react';
import { WidgetModel } from '../../types';

interface WidgetConfigModalProps {
  widget: WidgetModel | null;
  onClose: () => void;
  onSave: (updated: WidgetModel) => void;
}

export const WidgetConfigModal: React.FC<WidgetConfigModalProps> = ({
  widget,
  onClose,
  onSave,
}) => {
  if (!widget) return null;

  const [title, setTitle] = useState(widget.title);
  const [colSpan, setColSpan] = useState(widget.colSpan);
  const [rowSpan, setRowSpan] = useState(widget.rowSpan);
  const [appId, setAppId] = useState(widget.config?.appId || '');
  const [label, setLabel] = useState(widget.config?.label || '');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSave({
      ...widget,
      title: title.trim() || widget.title,
      colSpan: Math.max(1, Math.min(12, colSpan)),
      rowSpan: Math.max(1, Math.min(6, rowSpan)),
      config: {
        ...widget.config,
        ...(appId ? { appId } : {}),
        ...(label ? { label } : {}),
      },
    });
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
      <div className="w-full max-w-md rounded-2xl bg-zinc-950 border border-white/10 shadow-2xl p-6">
        <div className="flex items-center justify-between pb-4 border-b border-white/10">
          <h3 className="text-sm font-bold text-white tracking-wide">
            НАСТРОЙКА ВИДЖЕТА
          </h3>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg hover:bg-white/10 text-slate-400 hover:text-white"
            type="button"
          >
            <X size={16} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="mt-4 space-y-4 text-xs">
          <div>
            <label className="block text-slate-300 font-semibold mb-1">
              Название виджета
            </label>
            <input
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              className="w-full px-3 py-2 rounded-lg bg-zinc-900 border border-white/10 text-white focus:outline-hidden focus:border-purple-500 font-medium"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-slate-300 font-semibold mb-1">
                Ширина (колонок: 1–12)
              </label>
              <input
                type="number"
                min={1}
                max={12}
                value={colSpan}
                onChange={(e) => setColSpan(parseInt(e.target.value, 10) || 1)}
                className="w-full px-3 py-2 rounded-lg bg-zinc-900 border border-white/10 text-white focus:outline-hidden focus:border-purple-500 font-mono"
              />
            </div>
            <div>
              <label className="block text-slate-300 font-semibold mb-1">
                Высота (строк: 1–6)
              </label>
              <input
                type="number"
                min={1}
                max={6}
                value={rowSpan}
                onChange={(e) => setRowSpan(parseInt(e.target.value, 10) || 1)}
                className="w-full px-3 py-2 rounded-lg bg-zinc-900 border border-white/10 text-white focus:outline-hidden focus:border-purple-500 font-mono"
              />
            </div>
          </div>

          {widget.type === 'custom_button' && (
            <>
              <div>
                <label className="block text-slate-300 font-semibold mb-1">
                  Текст кнопки
                </label>
                <input
                  type="text"
                  value={label}
                  placeholder="Например: Visual Studio"
                  onChange={(e) => setLabel(e.target.value)}
                  className="w-full px-3 py-2 rounded-lg bg-zinc-900 border border-white/10 text-white focus:outline-hidden focus:border-purple-500 font-medium"
                />
              </div>

              <div>
                <label className="block text-slate-300 font-semibold mb-1">
                  Идентификатор приложения Windows (appId)
                </label>
                <input
                  type="text"
                  value={appId}
                  placeholder="wt, calc, code, spotify..."
                  onChange={(e) => setAppId(e.target.value)}
                  className="w-full px-3 py-2 rounded-lg bg-zinc-900 border border-white/10 text-white focus:outline-hidden focus:border-purple-500 font-mono"
                />
              </div>
            </>
          )}

          <div className="pt-2 flex justify-end gap-2">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 rounded-lg bg-zinc-800 hover:bg-zinc-700 text-slate-300 font-semibold cursor-pointer"
            >
              Отмена
            </button>
            <button
              type="submit"
              className="px-4 py-2 rounded-lg bg-purple-600 hover:bg-purple-500 text-white font-semibold flex items-center gap-1.5 cursor-pointer"
            >
              <Check size={14} />
              Сохранить
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
