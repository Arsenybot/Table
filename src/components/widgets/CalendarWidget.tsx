import React, { useState } from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react';

export const CalendarWidget: React.FC = () => {
  const [currentDate, setCurrentDate] = useState(new Date());
  const today = new Date();

  const year = currentDate.getFullYear();
  const month = currentDate.getMonth();

  const monthName = currentDate.toLocaleDateString('ru-RU', {
    month: 'long',
    year: 'numeric',
  });

  const prevMonth = () => {
    setCurrentDate(new Date(year, month - 1, 1));
  };

  const nextMonth = () => {
    setCurrentDate(new Date(year, month + 1, 1));
  };

  // Days in month calculation
  const firstDayIndex = new Date(year, month, 1).getDay(); // Sunday = 0
  const mondayBasedFirstDay = (firstDayIndex + 6) % 7; // Monday = 0
  const daysInMonth = new Date(year, month + 1, 0).getDate();

  const days: (number | null)[] = [];
  for (let i = 0; i < mondayBasedFirstDay; i++) {
    days.push(null);
  }
  for (let i = 1; i <= daysInMonth; i++) {
    days.push(i);
  }

  const weekDayLabels = ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'];

  return (
    <div className="flex flex-col h-full w-full justify-between select-none">
      {/* Month Navigator Header */}
      <div className="flex items-center justify-between pb-1.5 border-b border-white/5">
        <span className="text-xs font-semibold text-slate-200 capitalize tracking-wide">
          {monthName}
        </span>
        <div className="flex items-center gap-1">
          <button
            onClick={prevMonth}
            className="p-1 rounded hover:bg-white/10 text-slate-400 hover:text-white transition-colors cursor-pointer"
            title="Предыдущий месяц"
            type="button"
          >
            <ChevronLeft size={13} />
          </button>
          <button
            onClick={nextMonth}
            className="p-1 rounded hover:bg-white/10 text-slate-400 hover:text-white transition-colors cursor-pointer"
            title="Следующий месяц"
            type="button"
          >
            <ChevronRight size={13} />
          </button>
        </div>
      </div>

      {/* Week Day Labels */}
      <div className="grid grid-cols-7 gap-1 text-center my-1">
        {weekDayLabels.map((d, i) => (
          <span
            key={d}
            className={`text-[10px] font-semibold ${
              i >= 5 ? 'text-purple-400/80' : 'text-slate-500'
            }`}
          >
            {d}
          </span>
        ))}
      </div>

      {/* Days Grid */}
      <div className="grid grid-cols-7 gap-1 text-center flex-1 items-center">
        {days.slice(0, 35).map((day, idx) => {
          if (!day) {
            return <div key={`empty-${idx}`} className="h-5 w-5" />;
          }

          const isToday =
            day === today.getDate() &&
            month === today.getMonth() &&
            year === today.getFullYear();

          return (
            <div
              key={`day-${day}`}
              className={`h-5 w-5 mx-auto flex items-center justify-center rounded-full text-[11px] font-medium transition-all ${
                isToday
                  ? 'bg-purple-600 text-white font-bold shadow-sm shadow-purple-500/50 ring-1 ring-purple-400'
                  : 'text-slate-300 hover:bg-white/5'
              }`}
            >
              {day}
            </div>
          );
        })}
      </div>
    </div>
  );
};
