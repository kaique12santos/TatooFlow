export type AlertVariant = 'info' | 'success' | 'warning' | 'danger' | 'gold';

export interface AlertOptions {
  title: string;
  message?: string;
  description?: string;
  badge?: string;
  variant?: AlertVariant;
  confirmLabel?: string;
  cancelLabel?: string;
  showCancel?: boolean;
  closeOnBackdrop?: boolean;
}

export interface AppointmentConfirmationData {
  clientName: string;
  clientPhone?: string;
  artistName: string;
  artistStation?: string;
  data: string;
  hora: string;
  duracaoMinutos: number;
  terminoPrevisto?: string;
  valorEstimado: number;
  sinalEstimado?: number;
  descricaoSessao?: string;
}

export interface LoginSuccessData {
  userName: string;
  userRole: string;
  userStation: string;
  deviceId: string;
  message?: string;
}

export interface ActiveAlertState {
  type: 'standard' | 'confirm' | 'appointment' | 'login';
  isOpen: boolean;
  options: AlertOptions;
  appointmentData?: AppointmentConfirmationData;
  loginData?: LoginSuccessData;
  resolve?: (value: boolean) => void;
}

export interface ToastNotificationData {
  id: string;
  title: string;
  message: string;
  badge?: string;
  type?: 'whatsapp' | 'info' | 'success' | 'warning' | 'danger';
  senderName?: string;
  timeAgo?: string;
  duration?: number;
  actionLabel?: string;
  onAction?: () => void;
}

