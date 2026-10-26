import React from 'react';
import {
  X,
  Clock,
  Calendar,
  PhoneCall,
  Music,
  Volume2,
  Sun,
  LayoutGrid,
  Wifi,
  BatteryCharging,
  Terminal,
} from 'lucide-react';
import { WIDGET_CATALOG, WidgetDescriptor } from '../../data/defaultLayout';
import { WidgetType } from '../../types';

interface WidgetCatalogModalProps {
  isOpen: boolean;
  onClose: () => void;
  onAddWidget: (type: WidgetType) => void;
}

const iconMap: Record<string, React.FC<{ size?: number; className?: string }>> = {
  Clock,
  Calendar,
  PhoneCall,
  Music,
  Volume2,
  Sun,
  LayoutGrid,
  Wifi,
  BatteryCharging,
  Terminal,
};

export const WidgetCatalogModal: React.FC<WidgetCatalogModalProps> = ({
  isOpen,
  onClose,
  onAddWidget,
}) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-in fade-in duration-150">
      <div className="relative w-full max-w-2xl max-h-[85vh] flex flex-col rounded-2xl bg-zinc-950 border border-white/10 shadow-2xl overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-white/10 bg-zinc-900/50">
          <div>
            <h2 className="text-sm font-bold text-white tracking-wide">
              КАТАЛОГ ВИДЖЕТОВ
            </h2>
            <p className="text-xs text-slate-400 mt-0.5">
              Выберите виджет для добавления на сенсорную панель Touch Panel
            </p>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg hover:bg-white/10 text-slate-400 hover:text-white transition-colors cursor-pointer"
            type="button"
          >
            <X size={18} />
          </button>
        </div>

        {/* Catalog Grid */}
        <div className="flex-1 overflow-y-auto p-6 grid grid-cols-1 sm:grid-cols-2 gap-3">
          {Object.values(WIDGET_CATALOG).map((item: WidgetDescriptor) => {
            const Icon = iconMap[item.iconName] || Terminal;
            return (
              <div
                key={item.type}
                onClick={() => {
                  onAddWidget(item.type);
                  onClose();
                }}
                className="group flex flex-col justify-between p-4 rounded-xl bg-zinc-900/60 hover:bg-zinc-800/80 border border-white/5 hover:border-purple-500/50 transition-all cursor-pointer shadow-sm hover:shadow-purple-950/30"
              >
                <div className="flex items-start gap-3">
                  <div className="p-2.5 rounded-lg bg-purple-500/10 group-hover:bg-purple-500/20 text-purple-400 transition-colors">
                    <Icon size={20} />
                  </div>
                  <div className="flex-1 min-w-0">
                    <h3 className="text-xs font-bold text-white group-hover:text-purple-300 transition-colors">
                      {item.displayName}
                    </h3>
                    <p className="text-[11px] text-slate-400 mt-1 line-clamp-2 leading-relaxed">
                      {item.description}
                    </p>
                  </div>
                </div>

                <div className="mt-3 pt-2.5 border-t border-white/5 flex items-center justify-between text-[10px]">
                  <span className="text-slate-500">
                    Размер: {item.defaultColSpan}x{item.defaultRowSpan}
                  </span>
                  <span className="text-purple-400 font-semibold group-hover:underline">
                    + Добавить
                  </span>
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
};
