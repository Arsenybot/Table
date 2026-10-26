import React, { useEffect, useState } from 'react';

export const ClockWidget: React.FC = () => {
  const [time, setTime] = useState(new Date());

  useEffect(() => {
    const timer = setInterval(() => setTime(new Date()), 1000);
    return () => clearInterval(timer);
  }, []);

  const hours = time.getHours().toString().padStart(2, '0');
  const minutes = time.getMinutes().toString().padStart(2, '0');
  const seconds = time.getSeconds().toString().padStart(2, '0');

  const dayOfWeek = time.toLocaleDateString('ru-RU', { weekday: 'long' });
  const dateFormatted = time.toLocaleDateString('ru-RU', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  });

  return (
    <div className="flex flex-col justify-between h-full w-full select-none">
      <div className="flex items-baseline">
        <span className="font-mono text-4xl lg:text-5xl font-bold tracking-tight text-white">
          {hours}:{minutes}
        </span>
        <span className="font-mono text-sm lg:text-base font-semibold text-purple-400 ml-1.5 opacity-90">
          :{seconds}
        </span>
      </div>

      <div className="mt-auto pt-2">
        <div className="text-xs font-bold uppercase tracking-wider text-purple-400">
          {dayOfWeek}
        </div>
        <div className="text-xs text-slate-400 font-medium capitalize">
          {dateFormatted}
        </div>
      </div>
    </div>
  );
};
