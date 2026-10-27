import React, { useEffect, useRef, useState } from 'react';
import { TopBar } from './components/TopBar';
import { WidgetContainer } from './components/dashboard/WidgetContainer';
import { MobileOverlayLayer } from './components/dashboard/MobileOverlayLayer';
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
import {
  findNearestFreeSlot,
  moveWidget,
  resizeWidget,
} from './utils/gridCollision';

export default function App() {
  const [layout, setLayout] = useState<DashboardLayout>(() => {
    try {
      const stored = localStorage.getItem('touch_panel_layout_v2');
      if (stored) {
        const parsed = JSON.parse(stored);
        if (parsed.version === 2 && parsed.columns === 8) {
          return parsed;
        }
      }
    } catch {
      // fallback
    }
    return INITIAL_DASHBOARD_LAYOUT;
  });

  const [activePage, setActivePage] = useState<number>(0);
  const [selectedWidgetId, setSelectedWidgetId] = useState<string | null>(null);
  const [isEditMode, setIsEditMode] = useState<boolean>(false);
  const [isPhoneFrame, setIsPhoneFrame] = useState<boolean>(true);
  const [isFullscreen, setIsFullscreen] = useState<boolean>(false);
  const [connectionStatus, setConnectionStatus] = useState<ConnectionStatus>(
    protocolService.getStatus()
  );

  const [statusMessage, setStatusMessage] = useState<{
    text: string;
    isError: boolean;
  } | null>(null);

  // Dragging state
  const [draggingWidgetId, setDraggingWidgetId] = useState<string | null>(null);
  const [dragOffset, setDragOffset] = useState<{ x: number; y: number }>({ x: 0, y: 0 });
  const [ghostCoords, setGhostCoords] = useState<{ col: number; row: number } | null>(null);
  const dragStartPos = useRef<{ x: number; y: number }>({ x: 0, y: 0 });

  // Grid container reference for dimensions
  const gridContainerRef = useRef<HTMLDivElement>(null);
  const [containerSize, setContainerSize] = useState<{ width: number; height: number }>({
    width: 900,
    height: 420,
  });

  // Modals
  const [isCatalogOpen, setIsCatalogOpen] = useState(false);
  const [isConnectionOpen, setIsConnectionOpen] = useState(false);
  const [isProtocolOpen, setIsProtocolOpen] = useState(false);
  const [isMangoOpen, setIsMangoOpen] = useState(false);
  const [isExportOpen, setIsExportOpen] = useState(false);
  const [configWidget, setConfigWidget] = useState<WidgetModel | null>(null);

  // Keep track of grid container width/height
  useEffect(() => {
    const updateSize = () => {
      if (gridContainerRef.current) {
        const rect = gridContainerRef.current.getBoundingClientRect();
        setContainerSize({ width: rect.width, height: rect.height });
      }
    };
    updateSize();
    window.addEventListener('resize', updateSize);
    return () => window.removeEventListener('resize', updateSize);
  }, [isPhoneFrame]);

  // Sync connection status
  useEffect(() => {
    return protocolService.onStatusChange((status) => {
      setConnectionStatus(status);
    });
  }, []);

  // Save layout changes to localStorage
  const saveLayout = (newLayout: DashboardLayout) => {
    setLayout(newLayout);
    localStorage.setItem('touch_panel_layout_v2', JSON.stringify(newLayout));
  };

  // Screen Wake Lock API
  useEffect(() => {
    let wakeLock: unknown = null;
    const requestWakeLock = async () => {
      if ('wakeLock' in navigator) {
        try {
          wakeLock = await (
            navigator as unknown as { wakeLock: { request: (type: string) => Promise<unknown> } }
          ).wakeLock.request('screen');
        } catch {
          // wake lock denied
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
      document.documentElement
        .requestFullscreen()
        .then(() => setIsFullscreen(true))
        .catch(() => {});
    } else {
      document
        .exitFullscreen()
        .then(() => setIsFullscreen(false))
        .catch(() => {});
    }
  };

  // Widget management
  const handleAddWidget = (type: WidgetType) => {
    const desc = WIDGET_CATALOG[type];
    const freeSlot =
      findNearestFreeSlot(
        desc.defaultColSpan,
        desc.defaultRowSpan,
        0,
        0,
        activePage,
        null,
        layout
      ) ||
      findNearestFreeSlot(
        desc.defaultColSpan,
        desc.defaultRowSpan,
        0,
        0,
        (activePage + 1) % layout.pageCount,
        null,
        layout
      );

    const targetCol = freeSlot ? freeSlot.col : 0;
    const targetRow = freeSlot ? freeSlot.row : 0;

    const newWidget: WidgetModel = {
      id: `w_${type}_${Date.now()}`,
      type,
      title: desc.displayName.toUpperCase(),
      gridCol: targetCol,
      gridRow: targetRow,
      colSpan: desc.defaultColSpan,
      rowSpan: desc.defaultRowSpan,
      pageIndex: activePage,
      isRemovable: true,
      config: type === 'custom_button' ? { label: 'Новая кнопка', appId: 'wt' } : {},
    };

    saveLayout({
      ...layout,
      widgets: [...layout.widgets, newWidget],
    });
    setSelectedWidgetId(newWidget.id);
    showFeedback(`Добавлен виджет: ${desc.displayName}`, false);
  };

  const handleDeleteWidget = (widgetId: string) => {
    const updated = layout.widgets.filter((w) => w.id !== widgetId);
    saveLayout({ ...layout, widgets: updated });
    if (selectedWidgetId === widgetId) setSelectedWidgetId(null);
    showFeedback('Виджет удалён с панели', false);
  };

  const handleResizeWidth = (delta: number) => {
    if (!selectedWidgetId) return;
    const target = layout.widgets.find((w) => w.id === selectedWidgetId);
    if (!target) return;

    const updated = resizeWidget(
      selectedWidgetId,
      target.colSpan + delta,
      target.rowSpan,
      layout
    );
    if (updated) {
      saveLayout(updated);
    } else {
      showFeedback('Недостаточно места по горизонтали', true);
    }
  };

  const handleResizeHeight = (delta: number) => {
    if (!selectedWidgetId) return;
    const target = layout.widgets.find((w) => w.id === selectedWidgetId);
    if (!target) return;

    const updated = resizeWidget(
      selectedWidgetId,
      target.colSpan,
      target.rowSpan + delta,
      layout
    );
    if (updated) {
      saveLayout(updated);
    } else {
      showFeedback('Недостаточно места по вертикали', true);
    }
  };

  const handleMoveToPage = (targetPage: number) => {
    if (!selectedWidgetId) return;
    const target = layout.widgets.find((w) => w.id === selectedWidgetId);
    if (!target) return;

    const freeSlot = findNearestFreeSlot(
      target.colSpan,
      target.rowSpan,
      target.gridCol,
      target.gridRow,
      targetPage,
      selectedWidgetId,
      layout
    );

    if (freeSlot) {
      const updated = moveWidget(
        selectedWidgetId,
        freeSlot.col,
        freeSlot.row,
        targetPage,
        layout
      );
      saveLayout(updated);
      setActivePage(targetPage);
      showFeedback(`Виджет перенесён на страницу ${targetPage + 1}`);
    } else {
      showFeedback(`Нет свободного места на странице ${targetPage + 1}`, true);
    }
  };

  const handleUpdateWidget = (updatedWidget: WidgetModel) => {
    const updated = layout.widgets.map((w) =>
      w.id === updatedWidget.id ? updatedWidget : w
    );
    saveLayout({ ...layout, widgets: updated });
    showFeedback('Виджет сохранён', false);
  };

  const handleResetLayout = () => {
    saveLayout(INITIAL_DASHBOARD_LAYOUT);
    setSelectedWidgetId(null);
    setActivePage(0);
    showFeedback('Раскладка сброшена по умолчанию', false);
  };

  // Dimensions math for 8x4 grid
  const columns = layout.columns || 8;
  const rows = layout.rows || 4;
  const gap = 8;
  const colWidth = (containerSize.width - (columns - 1) * gap) / columns;
  const rowHeight = (containerSize.height - (rows - 1) * gap) / rows;

  const getWidgetBox = (w: WidgetModel) => {
    return {
      x: w.gridCol * (colWidth + gap),
      y: w.gridRow * (rowHeight + gap),
      width: w.colSpan * colWidth + (w.colSpan - 1) * gap,
      height: w.rowSpan * rowHeight + (w.rowSpan - 1) * gap,
    };
  };

  // Selected widget object and bounding box
  const selectedWidget = layout.widgets.find(
    (w) => w.id === selectedWidgetId && (w.pageIndex ?? 0) === activePage
  ) || null;
  const selectedBox = selectedWidget ? getWidgetBox(selectedWidget) : null;

  // Dragging event handlers
  const handlePointerDown = (widget: WidgetModel, e: React.PointerEvent) => {
    if (!isEditMode) return;
    e.stopPropagation();
    setSelectedWidgetId(widget.id);

    // Prepare dragging
    setDraggingWidgetId(widget.id);
    dragStartPos.current = { x: e.clientX, y: e.clientY };
    setDragOffset({ x: 0, y: 0 });
    setGhostCoords({ col: widget.gridCol, row: widget.gridRow });

    const handlePointerMove = (moveEvt: PointerEvent) => {
      const dx = moveEvt.clientX - dragStartPos.current.x;
      const dy = moveEvt.clientY - dragStartPos.current.y;
      setDragOffset({ x: dx, y: dy });

      const currentX = widget.gridCol * (colWidth + gap) + dx;
      const currentY = widget.gridRow * (rowHeight + gap) + dy;

      const calcCol = Math.max(
        0,
        Math.min(columns - widget.colSpan, Math.round(currentX / (colWidth + gap)))
      );
      const calcRow = Math.max(
        0,
        Math.min(rows - widget.rowSpan, Math.round(currentY / (rowHeight + gap)))
      );

      setGhostCoords({ col: calcCol, row: calcRow });
    };

    const handlePointerUp = () => {
      window.removeEventListener('pointermove', handlePointerMove);
      window.removeEventListener('pointerup', handlePointerUp);

      setGhostCoords((ghost) => {
        if (ghost) {
          const updated = moveWidget(
            widget.id,
            ghost.col,
            ghost.row,
            activePage,
            layout
          );
          saveLayout(updated);
        }
        return null;
      });

      setDraggingWidgetId(null);
      setDragOffset({ x: 0, y: 0 });
    };

    window.addEventListener('pointermove', handlePointerMove);
    window.addEventListener('pointerup', handlePointerUp);
  };

  // Handle Resize handle pointer drag
  const handleResizeHandlePointerDown = (e: React.PointerEvent) => {
    if (!selectedWidget) return;
    e.stopPropagation();

    const startX = e.clientX;
    const startY = e.clientY;
    const initialColSpan = selectedWidget.colSpan;
    const initialRowSpan = selectedWidget.rowSpan;

    const handleMove = (ev: PointerEvent) => {
      const dx = ev.clientX - startX;
      const dy = ev.clientY - startY;

      const deltaCols = Math.round(dx / (colWidth + gap));
      const deltaRows = Math.round(dy / (rowHeight + gap));

      const newColSpan = initialColSpan + deltaCols;
      const newRowSpan = initialRowSpan + deltaRows;

      if (newColSpan !== selectedWidget.colSpan || newRowSpan !== selectedWidget.rowSpan) {
        const updated = resizeWidget(
          selectedWidget.id,
          newColSpan,
          newRowSpan,
          layout
        );
        if (updated) {
          saveLayout(updated);
        }
      }
    };

    const handleUp = () => {
      window.removeEventListener('pointermove', handleMove);
      window.removeEventListener('pointerup', handleUp);
    };

    window.addEventListener('pointermove', handleMove);
    window.addEventListener('pointerup', handleUp);
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

  const pageWidgets = layout.widgets.filter(
    (w) => (w.pageIndex ?? 0) === activePage
  );

  const draggingWidget = layout.widgets.find((w) => w.id === draggingWidgetId);
  const ghostBox =
    ghostCoords && draggingWidget
      ? {
          x: ghostCoords.col * (colWidth + gap),
          y: ghostCoords.row * (rowHeight + gap),
          width: draggingWidget.colSpan * colWidth + (draggingWidget.colSpan - 1) * gap,
          height: draggingWidget.rowSpan * rowHeight + (draggingWidget.rowSpan - 1) * gap,
        }
      : null;

  return (
    <div className="flex flex-col h-screen w-screen bg-[#050507] text-slate-100 overflow-hidden font-sans select-none">
      {/* Top Navigation Bar */}
      <TopBar
        isEditMode={isEditMode}
        onToggleEdit={() => {
          setIsEditMode(!isEditMode);
          setSelectedWidgetId(null);
        }}
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
      <main
        onClick={() => {
          if (isEditMode) setSelectedWidgetId(null);
        }}
        className="flex-1 min-h-0 flex items-center justify-center p-2 sm:p-4 bg-black overflow-hidden relative"
      >
        {/* Phone Bezel Simulator Frame or Fullscreen Canvas */}
        <div
          className={`transition-all duration-300 w-full h-full flex flex-col items-center justify-center ${
            isPhoneFrame
              ? 'max-w-[1260px] max-h-[620px] aspect-[19.5/9] rounded-[44px] p-3 sm:p-4 bg-zinc-950 border-[10px] border-zinc-800 shadow-[0_0_80px_rgba(0,0,0,0.9),0_20px_40px_rgba(0,0,0,0.8)] relative'
              : 'max-w-full max-h-full'
          }`}
        >
          {/* Subtle phone punchhole camera at top center (when horizontal) */}
          {isPhoneFrame && (
            <div className="absolute left-6 top-1/2 -translate-y-1/2 h-3.5 w-3.5 rounded-full bg-black border border-white/10 flex items-center justify-center z-40">
              <div className="h-1.5 w-1.5 rounded-full bg-zinc-800/80" />
            </div>
          )}

          {/* OLED Dashboard Grid Area */}
          <div
            className={`w-full h-full rounded-2xl bg-[#08080A] flex flex-col p-3 ${
              isPhoneFrame ? 'rounded-[32px] pl-10' : ''
            }`}
          >
            {/* Grid Container (Layer 1 + 2 + 3 + 4 + 5) */}
            <div
              ref={gridContainerRef}
              className="flex-1 w-full min-h-0 relative overflow-hidden"
            >
              {/* Layer 1: Normal Grid Cards */}
              {pageWidgets.map((widget) => {
                const box = getWidgetBox(widget);
                const isSelected = isEditMode && selectedWidgetId === widget.id;
                const isBeingDragged = draggingWidgetId === widget.id;

                const style: React.CSSProperties = isBeingDragged
                  ? {
                      position: 'absolute',
                      transform: `translate3d(${box.x + dragOffset.x}px, ${
                        box.y + dragOffset.y
                      }px, 0)`,
                      width: `${box.width}px`,
                      height: `${box.height}px`,
                      zIndex: 35,
                      opacity: 0.9,
                    }
                  : {
                      position: 'absolute',
                      transform: `translate3d(${box.x}px, ${box.y}px, 0)`,
                      width: `${box.width}px`,
                      height: `${box.height}px`,
                      zIndex: isSelected ? 20 : 10,
                    };

                return (
                  <div
                    key={widget.id}
                    style={style}
                    onPointerDown={(e) => handlePointerDown(widget, e)}
                  >
                    <WidgetContainer
                      widget={widget}
                      isEditMode={isEditMode}
                      isSelected={isSelected}
                      isDragging={isBeingDragged}
                      onClick={() => {
                        setSelectedWidgetId(
                          selectedWidgetId === widget.id ? null : widget.id
                        );
                      }}
                    >
                      {renderWidgetContent(widget)}
                    </WidgetContainer>
                  </div>
                );
              })}

              {/* Layer 4 & 5: External Overlay Floating Toolbar & Resize Handle */}
              <MobileOverlayLayer
                selectedWidget={selectedWidget}
                selectedBox={selectedBox}
                isDragging={!!draggingWidgetId}
                ghostBox={ghostBox}
                currentPage={activePage}
                pageCount={layout.pageCount || 2}
                containerWidth={containerSize.width}
                onResizeWidth={handleResizeWidth}
                onResizeHeight={handleResizeHeight}
                onMoveToPage={handleMoveToPage}
                onConfigure={
                  selectedWidget?.type === 'custom_button'
                    ? () => setConfigWidget(selectedWidget)
                    : undefined
                }
                onDelete={() => {
                  if (selectedWidgetId) handleDeleteWidget(selectedWidgetId);
                }}
                onDeselect={() => setSelectedWidgetId(null)}
                onResizeHandlePointerDown={handleResizeHandlePointerDown}
              />
            </div>

            {/* Bottom Page Navigation Dots (Layer 0 Bar) */}
            {(layout.pageCount || 2) > 1 && (
              <div className="flex items-center justify-center gap-3 pt-2 shrink-0">
                {Array.from({ length: layout.pageCount || 2 }).map((_, idx) => {
                  const isCurrent = idx === activePage;
                  return (
                    <button
                      key={idx}
                      type="button"
                      onClick={() => {
                        setActivePage(idx);
                        setSelectedWidgetId(null);
                      }}
                      className={`h-2 rounded-full transition-all duration-200 cursor-pointer ${
                        isCurrent
                          ? 'w-7 bg-purple-500 shadow-sm shadow-purple-500/50'
                          : 'w-2 bg-zinc-700 hover:bg-zinc-500'
                      }`}
                      title={`Страница ${idx + 1}`}
                    />
                  );
                })}
              </div>
            )}
          </div>
        </div>
      </main>

      {/* Modals & Dialogs (Layer 6) */}
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
