import { MangoCallInfo, MangoCallStatus } from '../types';

type MangoListener = (info: MangoCallInfo) => void;

class MangoTalkerService {
  private callInfo: MangoCallInfo = {
    status: 'app_available',
    callerName: 'Alexander Romanov',
    phoneNumber: '+7 (495) 212-92-98',
    durationSeconds: 0,
  };

  private listeners: Set<MangoListener> = new Set();
  private timerId: number | null = null;

  constructor() {
    // Restore or initialize
  }

  getCallInfo(): MangoCallInfo {
    return { ...this.callInfo };
  }

  subscribe(listener: MangoListener) {
    this.listeners.add(listener);
    listener(this.callInfo);
    return () => {
      this.listeners.delete(listener);
    };
  }

  private notify() {
    this.listeners.forEach((l) => l(this.callInfo));
  }

  setStatus(status: MangoCallStatus) {
    this.stopTimer();
    this.callInfo = { ...this.callInfo, status };
    this.notify();
  }

  triggerIncomingCall(callerName = 'Dmitry Kuznetsov', phoneNumber = '+7 (916) 450-88-21') {
    this.stopTimer();
    this.callInfo = {
      status: 'incoming_call',
      callerName,
      phoneNumber,
      durationSeconds: 0,
      timestamp: Date.now(),
    };
    this.notify();
  }

  answerCall() {
    this.stopTimer();
    this.callInfo = {
      ...this.callInfo,
      status: 'active_call',
      durationSeconds: 1,
    };
    this.notify();

    this.timerId = window.setInterval(() => {
      this.callInfo.durationSeconds += 1;
      this.notify();
    }, 1000);
  }

  declineCall() {
    this.stopTimer();
    this.callInfo = {
      ...this.callInfo,
      status: 'call_ended',
    };
    this.notify();

    setTimeout(() => {
      if (this.callInfo.status === 'call_ended') {
        this.callInfo.status = 'app_available';
        this.notify();
      }
    }, 3000);
  }

  endCall() {
    this.declineCall();
  }

  private stopTimer() {
    if (this.timerId) {
      clearInterval(this.timerId);
      this.timerId = null;
    }
  }

  /**
   * Generates the confirmed Android Intent URL to launch Mango Talker Dialer
   */
  getDialerIntentUri(phone = this.callInfo.phoneNumber): string {
    const cleanPhone = phone.replace(/[^0-9+]/g, '');
    // Standard verified URI: tel: or Android intent syntax targeting com.mangotele.mtalker
    return `intent://${cleanPhone}#Intent;scheme=tel;package=com.mangotele.mtalker;action=android.intent.action.DIAL;end`;
  }
}

export const mangoService = new MangoTalkerService();
