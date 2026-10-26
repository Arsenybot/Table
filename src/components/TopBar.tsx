import React from 'react';
import {
  Smartphone,
  Maximize2,
  Minimize2,
  Edit3,
  Plus,
  RotateCcw,
  Terminal,
  FolderCode,
  Phone,
  SunMoon,
} from 'lucide-react';
import { ConnectionStatus } from '../types';

interface TopBarProps {
  isEditMode: boolean;
  onToggleEdit: () => void;
  onOpenCatalog: () => void;
  onResetLayout: () => void;
  onOpenConnection: () => void;
  onOpenProtocol: () => void;
  onOpenMango: () => void;
  onOpenExport: () => void;
  isPhoneFrame: boolean;
  onTogglePhoneFrame: () => void;
  isFullscreen: boolean;
  onToggleFullscreen: () => void;
  connectionStatus: ConnectionStatus;
  statusMessage: { text: string; isError: boolean } | null;
}

export const TopBar: React.FC<TopBarProps> = ({
  isEditMode,
  onToggleEdit,
  onOpenCatalog,
  onResetLayout,
  onOpenConnection,
  onOpenProtocol,
  onOpenMango,
  onOpenExport,
  isPhoneFrame,
  onTogglePhoneFrame,
  isFullscreen,
  onToggleFullscreen,
  connectionStatus,
  statusMessage,
}) => {
  const isConnected = connectionStatus === 'connected';

  return (
    <header className="h-12 w-full bg-zinc-950 border-b border-white/10 px-4 flex items-center justify-between shrink-0 select-none z-30">
      {/* Brand & Connection State */}
      <div className="flex items-center gap-3">
        <div className="flex items-center gap-2">
          <div className="h-6 w-6 rounded-lg bg-purple-600 flex items-center justify-center text-white shadow-sm shadow-purple-600/40">
            <Smartphone size={14} />
          </div>
          <span className="font-extrabold text-xs tracking-wider text-white">
            TOUCH PANEL
          </span>
        </div>

        <button
          onClick={onOpenConnection}
          type="button"
          className="flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-zinc-900 hover:bg-zinc-850 border border-white/5 transition-all text-[11px] font-medium cursor-pointer"
        >
          <span
            className={`h-2 w-2 rounded-full ${
              isConnected
                ? 'bg-emerald-400 shadow-sm shadow-emerald-400/50'
                : connectionStatus === 'connecting'
                ? 'bg-amber-400 animate-pulse'
                : 'bg-rose-500'
            }`}
          />
          <span className={isConnected ? 'text-emerald-400' : 'text-slate-400'}>
            {isConnected ? 'Windows Host' : 'Нет соединения'}
          </span>
        </button>
      </div>

      {/* Center Toast Message */}
      <div className="hidden md:flex items-center justify-center flex-1 px-4">
        {statusMessage ? (
          <div
            className={`px-3 py-1 rounded-full text-xs font-semibold animate-in fade-in slide-in-from-top-1 duration-200 ${
              statusMessage.isError
                ? 'bg-rose-500/20 text-rose-300 border border-rose-500/30'
                : 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/30'
            }`}
          >
            {statusMessage.text}
          </div>
        ) : (
          <div className="text-[11px] text-slate-500 tracking-wide font-medium">
            Сенсорная панель управления Windows (Android Landscape OLED)
          </div>
        )}
      </div>

      {/* Action Buttons Toolbar */}
      <div className="flex items-center gap-1.5">
        {/* Editor Controls */}
        {isEditMode && (
          <>
            <button
              onClick={onOpenCatalog}
              type="button"
              className="flex items-center gap-1 px-2.5 py-1.5 rounded-lg bg-purple-600 hover:bg-purple-500 text-white text-xs font-semibold transition-colors cursor-pointer shadow-sm shadow-purple-600/30"
              title="Добавить виджет из каталога"
            >
              <Plus size={13} />
              <span className="hidden sm:inline">Виджет</span>
            </button>

            <button
              onClick={onResetLayout}
              type="button"
              className="p-1.5 rounded-lg bg-zinc-900 hover:bg-zinc-800 text-slate-400 hover:text-white transition-colors cursor-pointer"
              title="Сбросить раскладку по умолчанию"
            >
              <RotateCcw size={14} />
            </button>
          </>
        )}

        <button
          onClick={onToggleEdit}
          type="button"
          className={`flex items-center gap-1 px-2.5 py-1.5 rounded-lg text-xs font-semibold transition-all cursor-pointer ${
            isEditMode
              ? 'bg-purple-600/30 border border-purple-500 text-purple-300 shadow-inner'
              : 'bg-zinc-900 hover:bg-zinc-800 text-slate-300 hover:text-white border border-white/5'
          }`}
          title={isEditMode ? 'Завершить редактирование' : 'Редактировать раскладку виджетов'}
        >
          <Edit3 size={13} />
          <span>{isEditMode ? 'Готово' : 'Раскладка'}</span>
        </button>

        <div className="h-4 w-px bg-white/10 mx-1" />

        {/* Diagnostic & Tools */}
        <button
          onClick={onOpenProtocol}
          type="button"
          className="p-1.5 rounded-lg bg-zinc-900 hover:bg-zinc-800 text-slate-400 hover:text-purple-300 transition-colors cursor-pointer"
          title="Инспектор протокола WTPP"
        >
          <Terminal size={14} />
        </button>

        <button
          onClick={onOpenMango}
          type="button"
          className="p-1.5 rounded-lg bg-zinc-900 hover:bg-zinc-800 text-slate-400 hover:text-purple-300 transition-colors cursor-pointer"
          title="Архитектура Mango Talker"
        >
          <Phone size={14} />
        </button>

        <button
          onClick={onOpenExport}
          type="button"
          className="p-1.5 rounded-lg bg-zinc-900 hover:bg-zinc-800 text-purple-400 hover:text-purple-300 transition-colors cursor-pointer"
          title="Исходники Android & Скачать ZIP"
        >
          <FolderCode size={14} />
        </button>

        <div className="h-4 w-px bg-white/10 mx-1" />

        {/* Bezel / Phone View Toggle */}
        <button
          onClick={onTogglePhoneFrame}
          type="button"
          className={`p-1.5 rounded-lg transition-colors cursor-pointer ${
            isPhoneFrame
              ? 'bg-purple-600/30 text-purple-300'
              : 'bg-zinc-900 hover:bg-zinc-800 text-slate-400 hover:text-white'
          }`}
          title={isPhoneFrame ? 'Полноэкранный режим' : 'Режим рамки смартфона на столе'}
        >
          <Smartphone size={14} />
        </button>

        {/* Fullscreen browser toggle */}
        <button
          onClick={onToggleFullscreen}
          type="button"
          className="p-1.5 rounded-lg bg-zinc-900 hover:bg-zinc-800 text-slate-400 hover:text-white transition-colors cursor-pointer"
          title={isFullscreen ? 'Выйти из полноэкранного режима' : 'Полноэкранный режим'}
        >
          {isFullscreen ? <Minimize2 size={14} /> : <Maximize2 size={14} />}
        </button>
      </div>
    </header>
  );
};
