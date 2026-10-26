import React from 'react';
import { Lock, Cpu, Scissors, Calculator, Terminal, Globe } from 'lucide-react';
import { protocolService } from '../../services/protocolService';

interface QuickActionsWidgetProps {
  onFeedback?: (msg: string, isError?: boolean) => void;
}

export const QuickActionsWidget: React.FC<QuickActionsWidgetProps> = ({ onFeedback }) => {
  const actions = [
    {
      id: 'lock',
      label: 'Lock PC',
      icon: Lock,
      type: 'shortcut',
      shortcutId: 'lock_screen',
      keyCombo: 'Win+L',
    },
    {
      id: 'taskmgr',
      label: 'TaskMgr',
      icon: Cpu,
      type: 'shortcut',
      shortcutId: 'task_manager',
      keyCombo: 'Ctrl+Shift+Esc',
    },
    {
      id: 'snip',
      label: 'Snip',
      icon: Scissors,
      type: 'shortcut',
      shortcutId: 'screen_snip',
      keyCombo: 'Win+Shift+S',
    },
    {
      id: 'calc',
      label: 'Calc',
      icon: Calculator,
      type: 'app',
      appId: 'calc',
    },
    {
      id: 'terminal',
      label: 'Terminal',
      icon: Terminal,
      type: 'app',
      appId: 'wt',
    },
    {
      id: 'browser',
      label: 'Browser',
      icon: Globe,
      type: 'app',
      appId: 'browser',
    },
  ];

  const handleAction = (item: (typeof actions)[0]) => {
    let res;
    if (item.type === 'shortcut') {
      res = protocolService.sendCommand('ExecuteShortcut', {
        shortcutId: item.shortcutId,
        keyCombo: item.keyCombo,
      });
    } else {
      res = protocolService.sendCommand('LaunchApplication', {
        appId: item.appId,
      });
    }

    if (onFeedback) {
      onFeedback(res.message, !res.success);
    }
  };

  return (
    <div className="grid grid-cols-3 gap-2 h-full w-full select-none">
      {actions.map((act) => {
        const IconComponent = act.icon;
        return (
          <button
            key={act.id}
            onClick={() => handleAction(act)}
            type="button"
            className="flex flex-col items-center justify-center p-2 rounded-xl bg-zinc-900/90 hover:bg-zinc-800 border border-white/5 hover:border-purple-500/30 active:scale-95 transition-all text-slate-300 hover:text-white cursor-pointer group shadow-sm"
          >
            <IconComponent
              size={18}
              className="text-slate-400 group-hover:text-purple-400 transition-colors mb-1.5"
            />
            <span className="text-[10px] font-semibold tracking-tight text-slate-300">
              {act.label}
            </span>
          </button>
        );
      })}
    </div>
  );
};
