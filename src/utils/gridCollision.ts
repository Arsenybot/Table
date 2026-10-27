import { DashboardLayout, WidgetModel } from '../types';

export function isIntersecting(
  x1: number,
  y1: number,
  w1: number,
  h1: number,
  x2: number,
  y2: number,
  w2: number,
  h2: number
): boolean {
  return x1 < x2 + w2 && x1 + w1 > x2 && y1 < y2 + h2 && y1 + h1 > y2;
}

export function canPlace(
  colSpan: number,
  rowSpan: number,
  targetCol: number,
  targetRow: number,
  pageIndex: number,
  excludeWidgetId: string | null,
  layout: DashboardLayout
): boolean {
  const maxCols = layout.columns;
  const maxRows = layout.rows;

  if (targetCol < 0 || targetRow < 0) return false;
  if (targetCol + colSpan > maxCols) return false;
  if (targetRow + rowSpan > maxRows) return false;

  for (const other of layout.widgets) {
    if (other.id === excludeWidgetId) continue;
    if ((other.pageIndex ?? 0) !== pageIndex) continue;

    if (
      isIntersecting(
        targetCol,
        targetRow,
        colSpan,
        rowSpan,
        other.gridCol,
        other.gridRow,
        other.colSpan,
        other.rowSpan
      )
    ) {
      return false;
    }
  }

  return true;
}

export function findNearestFreeSlot(
  colSpan: number,
  rowSpan: number,
  preferredCol: number,
  preferredRow: number,
  pageIndex: number,
  excludeWidgetId: string | null,
  layout: DashboardLayout
): { col: number; row: number } | null {
  let bestSlot: { col: number; row: number } | null = null;
  let minDistance = Infinity;

  const maxCols = layout.columns;
  const maxRows = layout.rows;

  for (let r = 0; r <= maxRows - rowSpan; r++) {
    for (let c = 0; c <= maxCols - colSpan; c++) {
      if (canPlace(colSpan, rowSpan, c, r, pageIndex, excludeWidgetId, layout)) {
        const dist = Math.abs(c - preferredCol) + Math.abs(r - preferredRow);
        if (dist < minDistance) {
          minDistance = dist;
          bestSlot = { col: c, row: r };
        }
      }
    }
  }

  return bestSlot;
}

export function moveWidget(
  widgetId: string,
  newCol: number,
  newRow: number,
  newPage: number,
  layout: DashboardLayout
): DashboardLayout {
  const target = layout.widgets.find((w) => w.id === widgetId);
  if (!target) return layout;

  const clampedCol = Math.max(0, Math.min(layout.columns - target.colSpan, newCol));
  const clampedRow = Math.max(0, Math.min(layout.rows - target.rowSpan, newRow));
  const clampedPage = Math.max(0, Math.min(layout.pageCount - 1, newPage));

  const updatedTarget: WidgetModel = {
    ...target,
    gridCol: clampedCol,
    gridRow: clampedRow,
    pageIndex: clampedPage,
  };

  const remaining = layout.widgets.filter((w) => w.id !== widgetId);
  const colliding = remaining.filter(
    (other) =>
      (other.pageIndex ?? 0) === clampedPage &&
      isIntersecting(
        clampedCol,
        clampedRow,
        target.colSpan,
        target.rowSpan,
        other.gridCol,
        other.gridRow,
        other.colSpan,
        other.rowSpan
      )
  );

  if (colliding.length === 0) {
    return {
      ...layout,
      widgets: [...remaining, updatedTarget],
    };
  }

  // Relocate colliding widgets
  const relocated: WidgetModel[] = [updatedTarget];
  for (const other of remaining) {
    if (colliding.includes(other)) {
      const free =
        findNearestFreeSlot(
          other.colSpan,
          other.rowSpan,
          other.gridCol,
          other.gridRow,
          clampedPage,
          null,
          { ...layout, widgets: relocated }
        ) ||
        findNearestFreeSlot(
          other.colSpan,
          other.rowSpan,
          0,
          0,
          (clampedPage + 1) % layout.pageCount,
          null,
          { ...layout, widgets: relocated }
        );

      if (free) {
        relocated.push({
          ...other,
          gridCol: free.col,
          gridRow: free.row,
        });
      } else {
        relocated.push(other);
      }
    } else {
      relocated.push(other);
    }
  }

  return {
    ...layout,
    widgets: relocated,
  };
}

export function resizeWidget(
  widgetId: string,
  newColSpan: number,
  newRowSpan: number,
  layout: DashboardLayout
): DashboardLayout | null {
  const target = layout.widgets.find((w) => w.id === widgetId);
  if (!target) return null;

  if (target.gridCol + newColSpan > layout.columns) return null;
  if (target.gridRow + newRowSpan > layout.rows) return null;
  if (newColSpan < 1 || newRowSpan < 1) return null;

  if (
    !canPlace(
      newColSpan,
      newRowSpan,
      target.gridCol,
      target.gridRow,
      target.pageIndex ?? 0,
      widgetId,
      layout
    )
  ) {
    return null;
  }

  const updated = layout.widgets.map((w) =>
    w.id === widgetId ? { ...w, colSpan: newColSpan, rowSpan: newRowSpan } : w
  );

  return {
    ...layout,
    widgets: updated,
  };
}
