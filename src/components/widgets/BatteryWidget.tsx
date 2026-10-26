import React, { useEffect, useState } from 'react';
import { BatteryCharging, Zap } from 'lucide-react';

export const BatteryWidget: React.FC = () => {
  const [level, setLevel] = useState<number>(88);
  const [charging, setCharging] = useState<boolean>(true);

  useEffect(() => {
    // Check if real browser battery API exists
    interface NavigatorWithBattery extends Navigator {
      getBattery?: () => Promise<{
        level: number;
        charging: boolean;
        addEventListener: (event: string, handler: () => void) => void;
      }>;
    }

    const nav = navigator as NavigatorWithBattery;
    if (nav.getBattery) {
      nav.getBattery().then((battery) => {
        setLevel(Math.round(battery.level * 100));
        setCharging(battery.charging);

        battery.addEventListener('levelchange', () => {
          setLevel(Math.round(battery.level * 100));
        });
        battery.addEventListener('chargingchange', () => {
          setCharging(battery.charging);
        });
      });
    }
  }, []);

  return (
    <div className="flex items-center justify-between h-full w-full select-none">
      <div className="flex items-center gap-2">
        {/* Hardware Battery Graphic */}
        <div className="relative w-8 h-4 rounded-sm border border-slate-600 p-0.5 flex items-center">
          <div
            style={{ width: `${level}%` }}
            className={`h-full rounded-2xs ${
              level > 20 ? 'bg-emerald-500' : 'bg-rose-500'
            }`}
          />
          <div className="absolute -right-1 top-1 h-2 w-0.5 bg-slate-600 rounded-r-xs" />
        </div>

        <span className="font-mono text-xs font-bold text-slate-200">
          {level}%
        </span>
      </div>

      <div className="flex items-center gap-1 text-amber-400 text-[10px] font-semibold">
        <Zap size={12} className="animate-pulse" />
        <span>Docked (5V/2A)</span>
      </div>
    </div>
  );
};
