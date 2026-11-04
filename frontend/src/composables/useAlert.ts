import { reactive, readonly } from 'vue';
import type {
  ActiveAlertState,
  AlertOptions,
  AppointmentConfirmationData,
  LoginSuccessData,
  ToastNotificationData,
} from '@/components/alerts/types';

const defaultOptions: AlertOptions = {
  title: 'Notificação do Atelier',
  message: '',
  description: '',
  badge: '',
  variant: 'gold',
  confirmLabel: 'Entendido',
  cancelLabel: 'Cancelar',
  showCancel: false,
  closeOnBackdrop: true,
};

const state = reactive<ActiveAlertState>({
  type: 'standard',
  isOpen: false,
  options: { ...defaultOptions },
  appointmentData: undefined,
  loginData: undefined,
  resolve: undefined,
});

const toastState = reactive<{ activeToast: ToastNotificationData | null }>({
  activeToast: null,
});

export function useAlert() {
  function open(
    type: ActiveAlertState['type'],
    options: AlertOptions,
    extra?: {
      appointmentData?: AppointmentConfirmationData;
      loginData?: LoginSuccessData;
    }
  ): Promise<boolean> {
    return new Promise((resolve) => {
      state.type = type;
      state.options = { ...defaultOptions, ...options };
      state.appointmentData = extra?.appointmentData;
      state.loginData = extra?.loginData;
      state.isOpen = true;
      state.resolve = resolve;
    });
  }

  function handleConfirm() {
    state.isOpen = false;
    if (state.resolve) {
      state.resolve(true);
      state.resolve = undefined;
    }
  }

  function handleCancel() {
    state.isOpen = false;
    if (state.resolve) {
      state.resolve(false);
      state.resolve = undefined;
    }
  }

  function handleClose() {
    state.isOpen = false;
    if (state.resolve) {
      state.resolve(false);
      state.resolve = undefined;
    }
  }

  // Generic Alert (Replaces window.alert)
  function alert(opts: AlertOptions | string): Promise<boolean> {
    const options: AlertOptions =
      typeof opts === 'string'
        ? { title: 'Aviso do Atelier', description: opts, showCancel: false, confirmLabel: 'Entendido' }
        : { showCancel: false, confirmLabel: 'Entendido', ...opts, description: opts.description || opts.message };

    return open('standard', options);
  }

  // Success Notification
  function success(message: string, title = 'Operação Concluída'): Promise<boolean> {
    return open('standard', {
      title,
      description: message,
      variant: 'success',
      badge: 'SUCESSO',
      showCancel: false,
      confirmLabel: 'OK',
    });
  }

  // Error Notification
  function error(message: string, title = 'Atenção'): Promise<boolean> {
    return open('standard', {
      title,
      description: message,
      variant: 'danger',
      badge: 'ERRO OPERACIONAL',
      showCancel: false,
      confirmLabel: 'Fechar',
    });
  }

  // Warning Notification
  function warning(message: string, title = 'Aviso Importante'): Promise<boolean> {
    return open('standard', {
      title,
      description: message,
      variant: 'warning',
      badge: 'ATENÇÃO',
      showCancel: false,
      confirmLabel: 'Entendido',
    });
  }

  // Info Notification
  function info(message: string, title = 'Informação'): Promise<boolean> {
    return open('standard', {
      title,
      description: message,
      variant: 'info',
      badge: 'INFORMATIVO',
      showCancel: false,
      confirmLabel: 'OK',
    });
  }

  // Generic Confirm (Replaces window.confirm)
  function confirm(opts: AlertOptions | string): Promise<boolean> {
    const options: AlertOptions =
      typeof opts === 'string'
        ? {
            title: 'Confirmação',
            description: opts,
            showCancel: true,
            confirmLabel: 'Confirmar',
            cancelLabel: 'Cancelar',
            variant: 'gold',
            badge: 'CONFIRMAÇÃO',
          }
        : {
            showCancel: true,
            confirmLabel: 'Confirmar',
            cancelLabel: 'Cancelar',
            variant: 'gold',
            badge: 'CONFIRMAÇÃO',
            ...opts,
            description: opts.description || opts.message,
          };

    return open('confirm', options);
  }

  // Destructive Confirmation (Cancellations, Deletions)
  function confirmDestructive(opts: AlertOptions | string): Promise<boolean> {
    const options: AlertOptions =
      typeof opts === 'string'
        ? {
            title: 'Ação Irreversível',
            description: opts,
            showCancel: true,
            confirmLabel: 'Sim, Executar',
            cancelLabel: 'Cancelar',
            variant: 'danger',
            badge: 'EXCLUSÃO DEFINITIVA',
          }
        : {
            showCancel: true,
            confirmLabel: 'Sim, Executar',
            cancelLabel: 'Cancelar',
            variant: 'danger',
            badge: 'AÇÃO DESTRUTIVA',
            ...opts,
            description: opts.description || opts.message,
          };

    return open('confirm', options);
  }

  // Specialized Appointment Confirmation Dialog
  function confirmAppointment(data: AppointmentConfirmationData): Promise<boolean> {
    return open('appointment', { title: 'Revisão do Agendamento' }, { appointmentData: data });
  }

  // Specialized Login Success Dialog
  function showLoginSuccess(data: LoginSuccessData): Promise<void> {
    return new Promise((resolve) => {
      open('login', { title: 'Acesso Autorizado' }, { loginData: data }).then(() => {
        resolve();
      });
    });
  }

  // Toast Notifications (Push / Webhook / In-App Alerts)
  function showToast(toast: Omit<ToastNotificationData, 'id'>) {
    const id = `toast-${Date.now()}-${Math.random().toString(36).slice(2, 7)}`;
    toastState.activeToast = {
      id,
      duration: 6000,
      ...toast,
    };
    if (typeof navigator !== 'undefined' && 'vibrate' in navigator) {
      try {
        navigator.vibrate([40, 60, 40]);
      } catch {
        // Haptic not allowed or unsupported
      }
    }
  }

  function notifyWhatsAppTriage(data: {
    clientName: string;
    text: string;
    timeAgo?: string;
    onAction?: () => void;
  }) {
    showToast({
      title: data.clientName,
      message: data.text,
      badge: 'WHATSAPP WEBHOOK',
      type: 'whatsapp',
      timeAgo: data.timeAgo || 'Agora',
      actionLabel: 'VER TRIAGEM',
      onAction: data.onAction,
      duration: 7000,
    });
  }

  function dismissToast() {
    toastState.activeToast = null;
  }

  function handleToastAction() {
    const action = toastState.activeToast?.onAction;
    toastState.activeToast = null;
    if (action) {
      action();
    }
  }

  return {
    state: readonly(state),
    activeToast: readonly(toastState),
    handleConfirm,
    handleCancel,
    handleClose,
    alert,
    success,
    error,
    warning,
    info,
    confirm,
    confirmDestructive,
    confirmAppointment,
    showLoginSuccess,
    showToast,
    notifyWhatsAppTriage,
    dismissToast,
    handleToastAction,
  };
}

