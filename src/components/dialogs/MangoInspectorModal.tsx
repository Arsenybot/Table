import React from 'react';
import { X, CheckCircle, ShieldAlert, Cpu, ExternalLink, Smartphone } from 'lucide-react';

interface MangoInspectorModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const MangoInspectorModal: React.FC<MangoInspectorModalProps> = ({
  isOpen,
  onClose,
}) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
      <div className="w-full max-w-2xl max-h-[85vh] rounded-2xl bg-zinc-950 border border-white/10 shadow-2xl flex flex-col overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-white/10 bg-zinc-900/50">
          <div className="flex items-center gap-2">
            <Smartphone size={18} className="text-purple-400" />
            <div>
              <h3 className="text-sm font-bold text-white tracking-wide">
                АРХИТЕКТУРА ИНТЕГРАЦИИ MANGO TALKER
              </h3>
              <p className="text-[11px] text-slate-400">
                Результаты анализа APK: com.mangotele.mtalker (v1.0.10914)
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg hover:bg-white/10 text-slate-400 hover:text-white"
            type="button"
          >
            <X size={16} />
          </button>
        </div>

        {/* Content */}
        <div className="flex-1 overflow-y-auto p-6 space-y-5 text-xs text-slate-300">
          {/* Confirmed Metadata Card */}
          <div className="p-4 rounded-xl bg-zinc-900/80 border border-white/5 space-y-2">
            <div className="text-[11px] font-bold text-purple-400 uppercase tracking-wider">
              1. Подтверждённый манифест APK
            </div>
            <div className="grid grid-cols-2 gap-2 font-mono text-[11px]">
              <div>
                <span className="text-slate-500">Package:</span>{' '}
                <span className="text-white font-semibold">
                  com.mangotele.mtalker
                </span>
              </div>
              <div>
                <span className="text-slate-500">Version:</span>{' '}
                <span className="text-slate-200">1.0.10914 (Code: 10914)</span>
              </div>
              <div>
                <span className="text-slate-500">Target SDK:</span>{' '}
                <span className="text-slate-200">API 36 / 34</span>
              </div>
              <div>
                <span className="text-slate-500">Min SDK:</span>{' '}
                <span className="text-slate-200">API 24 (Android 7.0+)</span>
              </div>
            </div>
          </div>

          {/* Confirmed Accessible Intents */}
          <div className="space-y-2">
            <div className="text-[11px] font-bold text-emerald-400 uppercase tracking-wider flex items-center gap-1.5">
              <CheckCircle size={14} />
              2. Официально доступные возможности (Intent & Schemes)
            </div>
            <p className="text-slate-400 leading-relaxed text-[11px]">
              На основе декомпиляции <code className="text-white">AndroidManifest.xml</code> подтверждены публичные точки входа:
            </p>
            <ul className="space-y-1.5 pl-2">
              <li className="flex items-start gap-2">
                <span className="text-emerald-400 font-bold">•</span>
                <div>
                  <strong className="text-white font-mono">android.intent.action.DIAL</strong> с схемой <code className="text-purple-300">tel:</code> — инициирует звонок через интерфейс Mango Talker с предзаполненным номером.
                </div>
              </li>
              <li className="flex items-start gap-2">
                <span className="text-emerald-400 font-bold">•</span>
                <div>
                  <strong className="text-white font-mono">android.intent.action.MAIN / LAUNCHER</strong> — запуск приложения через <code className="text-purple-300">com.mango.talker.LaunchActivity</code>.
                </div>
              </li>
              <li className="flex items-start gap-2">
                <span className="text-emerald-400 font-bold">•</span>
                <div>
                  <strong className="text-white font-mono">Package Query (API 30+)</strong> — проверка наличия приложения через блок <code className="text-purple-300">&lt;queries&gt;</code> в AndroidManifest.
                </div>
              </li>
            </ul>
          </div>

          {/* Architectural Constraints */}
          <div className="space-y-2">
            <div className="text-[11px] font-bold text-amber-400 uppercase tracking-wider flex items-center gap-1.5">
              <ShieldAlert size={14} />
              3. Системные ограничения Android (Без фиктивных API)
            </div>
            <div className="p-3.5 rounded-xl bg-amber-500/10 border border-amber-500/20 text-slate-300 leading-relaxed text-[11px] space-y-2">
              <p>
                <strong>Ограничение безопасности Android OS:</strong> Сторонняя программа не может встроить Activity другого приложения внутрь своего окна или перехватывать приватные аудиопотоки VoIP без прав системного фреймворка.
              </p>
              <p>
                В APK Mango Talker внутренние сервисы (<code className="text-white font-mono">CallService</code>) защищены атрибутом <code className="text-amber-300 font-mono">exported=false</code> и не экспортируют открытый AIDL/IPC интерфейс для сторонних приложений.
              </p>
              <p>
                <strong>Реализованное архитектурное решение:</strong> Создан адаптер <code className="text-purple-300 font-mono">MangoTalkerAdapter</code>. Он предоставляет карточку со всеми подтверждёнными состояниями вызова, управляет системными интентами и документирует статус готовности приложения.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
