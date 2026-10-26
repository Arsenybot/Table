import React, { useEffect, useState } from 'react';
import { TopBar } from './components/TopBar';
import { WidgetContainer } from './components/dashboard/WidgetContainer';
import { ClockWidget } from './components/widgets/ClockWidget';
import { CalendarWidget } from './components/widgets/CalendarWidget';
import { VolumeWidget } from './components/widgets/VolumeWidget';
import { BrightnessWidget } from './components/widgets/BrightnessWidget';
import { MediaWidget } from './components/widgets/MediaWidget';
import { QuickActionsWidget } from './components/widgets/QuickActionsWidget';
import { MangoTalkerWidget } from './components/widgets/MangoTalkerWidget';
import { ConnectionWidget } from './components/widgets/ConnectionWidget';
import { BatteryWidget } from './components/widgets/BatteryWidget';
import { CustomButtonWidget } from './components/widgets/CustomButtonWidget';

import { WidgetCatalogModal } from './components/editor/WidgetCatalogModal';
import { WidgetConfigModal } from './components/editor/WidgetConfigModal';
import { ConnectionModal } from './components/dialogs/ConnectionModal';
import { ProtocolInspectorModal } from './components/dialogs/ProtocolInspectorModal';
import { MangoInspectorModal } from './components/dialogs/MangoInspectorModal';
import { ProjectExportModal } from './components/dialogs/ProjectExportModal';

import { INITIAL_DASHBOARD_LAYOUT, WIDGET_CATALOG } from './data/defaultLayout';
import { protocolService } from './services/protocolService';
import { ConnectionStatus, DashboardLayout, WidgetModel, WidgetType } from './types';

export default function App() {
  const [layout, setLayout] = useState<DashboardLayout>(() => {
    try {
      const stored = localStorage.getItem('touch_panel_layout');
      if (stored) {
        return JSON.parse(stored);
      }
    } catch {
      // fallback
    }
    return INITIAL_DASHBOARD_LAYOUT;
  });

  const [isEditMode, setIsEditMode] = useState<boolean>(false);
  const [isPhoneFrame, setIsPhoneFrame] = useState<boolean>(false);
  const [isFullscreen, setIsFullscreen] = useState<boolean>(false);
  const [connectionStatus, setConnectionStatus] = useState<ConnectionStatus>(
    protocolService.getStatus()
  );

  const [statusMessage, setStatusMessage] = useState<{
    text: string;
    isError: boolean;
  } | null>(null);

  // Modals
  const [isCatalogOpen, setIsCatalogOpen] = useState(false);
  const [isConnectionOpen, setIsConnectionOpen] = useState(false);
  const [isProtocolOpen, setIsProtocolOpen] = useState(false);
  const [isMangoOpen, setIsMangoOpen] = useState(false);
  const [isExportOpen, setIsExportOpen] = useState(false);
  const [configWidget, setConfigWidget] = useState<WidgetModel | null>(null);

  // Sync connection status
  useEffect(() => {
    return protocolService.onStatusChange((status) => {
      setConnectionStatus(status);
    });
  }, []);

  // Save layout changes to localStorage
  const saveLayout = (newLayout: DashboardLayout) => {
    setLayout(newLayout);
    localStorage.setItem('touch_panel_layout', JSON.stringify(newLayout));
  };

  // Screen Wake Lock API: keep phone screen on
  useEffect(() => {
    let wakeLock: unknown = null;
    const requestWakeLock = async () => {
      if ('wakeLock' in navigator) {
        try {
          wakeLock = await (navigator as unknown as { wakeLock: { request: (type: string) => Promise<unknown> } }).wakeLock.request('screen');
        } catch {
          // wake lock denied or unsupported
        }
      }
    };
    requestWakeLock();
    return () => {
      if (wakeLock && typeof (wakeLock as { release?: () => void }).release === 'function') {
        (wakeLock as { release: () => void }).release();
      }
    };
  }, []);

  const showFeedback = (text: string, isError = false) => {
    setStatusMessage({ text, isError });
    setTimeout(() => {
      setStatusMessage((cur) => (cur?.text === text ? null : cur));
    }, 3500);
  };

  // Fullscreen toggle
  const toggleFullscreen = () => {
    if (!document.fullscreenElement) {
      document.documentElement.requestFullscreen().then(() => setIsFullscreen(true)).catch(() => {});
    } else {
      document.exitFullscreen().then(() => setIsFullscreen(false)).catch(() => {});
    }
  };

  // Widget management
  const handleAddWidget = (type: WidgetType) => {
    const desc = WIDGET_CATALOG[type];
    const newWidget: WidgetModel = {
      id: `w_${type}_${Date.now()}`,
      type,
      title: desc.displayName.toUpperCase(),
      colSpan: desc.defaultColSpan,
      rowSpan: desc.defaultRowSpan,
      isRemovable: true,
      config: type === 'custom_button' ? { label: 'Новая кнопка', appId: 'wt' } : {},
    };

    saveLayout({
      ...layout,
      widgets: [...layout.widgets, newWidget],
    });
    showFeedback(`Добавлен виджет: ${desc.displayName}`, false);
  };

  const handleDeleteWidget = (widgetId: string) => {
    const updated = layout.widgets.filter((w) => w.id !== widgetId);
    saveLayout({ ...layout, widgets: updated });
    showFeedback('Виджет удалён', false);
  };

  const handleMoveWidget = (index: number, direction: 'left' | 'right') => {
    const targetIndex = direction === 'left' ? index - 1 : index + 1;
    if (targetIndex < 0 || targetIndex >= layout.widgets.length) return;

    const newWidgets = [...layout.widgets];
    const [moved] = newWidgets.splice(index, 1);
    newWidgets.splice(targetIndex, 0, moved);

    saveLayout({ ...layout, widgets: newWidgets });
  };

  const handleResizeWidget = (widgetId: string, delta: number) => {
    const updated = layout.widgets.map((w) => {
      if (w.id === widgetId) {
        const desc = WIDGET_CATALOG[w.type];
        const minSpan = desc ? desc.minColSpan : 1;
        const maxSpan = desc ? desc.maxColSpan : 6;
        const newColSpan = Math.max(minSpan, Math.min(maxSpan, w.colSpan + delta));
        return { ...w, colSpan: newColSpan };
      }
      return w;
    });
    saveLayout({ ...layout, widgets: updated });
  };

  const handleUpdateWidget = (updatedWidget: WidgetModel) => {
    const updated = layout.widgets.map((w) =>
      w.id === updatedWidget.id ? updatedWidget : w
    );
    saveLayout({ ...layout, widgets: updated });
    showFeedback('Виджет обновлён', false);
  };

  const handleResetLayout = () => {
    saveLayout(INITIAL_DASHBOARD_LAYOUT);
    showFeedback('Раскладка сброшена по умолчанию', false);
  };

  // Render individual widget content by type
  const renderWidgetContent = (widget: WidgetModel) => {
    switch (widget.type) {
      case 'clock':
        return <ClockWidget />;
      case 'calendar':
        return <CalendarWidget />;
      case 'volume':
        return <VolumeWidget onFeedback={showFeedback} />;
      case 'brightness':
        return <BrightnessWidget onFeedback={showFeedback} />;
      case 'media':
        return <MediaWidget onFeedback={showFeedback} />;
      case 'quick_actions':
        return <QuickActionsWidget onFeedback={showFeedback} />;
      case 'mango_talker':
        return (
          <MangoTalkerWidget
            onOpenInspector={() => setIsMangoOpen(true)}
            onFeedback={showFeedback}
          />
        );
      case 'connection':
        return (
          <ConnectionWidget onOpenSettings={() => setIsConnectionOpen(true)} />
        );
      case 'battery':
        return <BatteryWidget />;
      case 'custom_button':
        return (
          <CustomButtonWidget model={widget} onFeedback={showFeedback} />
        );
      default:
        return <div className="text-xs text-slate-500">Виджет {widget.type}</div>;
    }
  };

  return (
    <div className="flex flex-col h-screen w-screen bg-[#050507] text-slate-100 overflow-hidden font-sans select-none">
      {/* Top Navigation Bar */}
      <TopBar
        isEditMode={isEditMode}
        onToggleEdit={() => setIsEditMode(!isEditMode)}
        onOpenCatalog={() => setIsCatalogOpen(true)}
        onResetLayout={handleResetLayout}
        onOpenConnection={() => setIsConnectionOpen(true)}
        onOpenProtocol={() => setIsProtocolOpen(true)}
        onOpenMango={() => setIsMangoOpen(true)}
        onOpenExport={() => setIsExportOpen(true)}
        isPhoneFrame={isPhoneFrame}
        onTogglePhoneFrame={() => setIsPhoneFrame(!isPhoneFrame)}
        isFullscreen={isFullscreen}
        onToggleFullscreen={toggleFullscreen}
        connectionStatus={connectionStatus}
        statusMessage={statusMessage}
      />

      {/* Main Touch Panel Canvas */}
      <main className="flex-1 min-h-0 flex items-center justify-center p-2 sm:p-4 bg-black overflow-hidden relative">
        {/* Phone Bezel Simulator Frame or Fullscreen Canvas */}
        <div
          className={`transition-all duration-300 w-full h-full flex flex-col items-center justify-center ${
            isPhoneFrame
              ? 'max-w-[1280px] max-h-[640px] aspect-[19.5/9] rounded-[44px] p-3.5 bg-zinc-950 border-[10px] border-zinc-800 shadow-[0_0_80px_rgba(0,0,0,0.9),0_20px_40px_rgba(0,0,0,0.8)] relative'
              : 'max-w-full max-h-full'
          }`}
        >
          {/* Subtle phone punchhole camera at top center (when in horizontal orientation) */}
          {isPhoneFrame && (
            <div className="absolute left-6 top-1/2 -translate-y-1/2 h-3.5 w-3.5 rounded-full bg-black border border-white/10 flex items-center justify-center z-40">
              <div className="h-1.5 w-1.5 rounded-full bg-zinc-800/80" />
            </div>
          )}

          {/* OLED Dashboard Grid Area */}
          <div
            className={`w-full h-full rounded-2xl bg-[#08080A] overflow-y-auto overflow-x-hidden p-3.5 ${
              isPhoneFrame ? 'rounded-[32px] pl-10' : ''
            }`}
          >
            <div className="grid grid-cols-12 auto-rows-[82px] gap-2.5 w-full min-h-full">
              {layout.widgets.map((widget, index) => (
                <WidgetContainer
                  key={widget.id}
                  widget={widget}
                  isEditMode={isEditMode}
                  onDelete={() => handleDeleteWidget(widget.id)}
                  onConfigure={() => setConfigWidget(widget)}
                  onMove={(dir) => handleMoveWidget(index, dir)}
                  onResize={(delta) => handleResizeWidget(widget.id, delta)}
                >
                  {renderWidgetContent(widget)}
                </WidgetContainer>
              ))}
            </div>
          </div>
        </div>
      </main>

      {/* Modals & Dialogs */}
      <WidgetCatalogModal
        isOpen={isCatalogOpen}
        onClose={() => setIsCatalogOpen(false)}
        onAddWidget={handleAddWidget}
      />

      <WidgetConfigModal
        widget={configWidget}
        onClose={() => setConfigWidget(null)}
        onSave={handleUpdateWidget}
      />

      <ConnectionModal
        isOpen={isConnectionOpen}
        onClose={() => setIsConnectionOpen(false)}
      />

      <ProtocolInspectorModal
        isOpen={isProtocolOpen}
        onClose={() => setIsProtocolOpen(false)}
      />

      <MangoInspectorModal
        isOpen={isMangoOpen}
        onClose={() => setIsMangoOpen(false)}
      />

      <ProjectExportModal
        isOpen={isExportOpen}
        onClose={() => setIsExportOpen(false)}
      />
    </div>
  );
}
