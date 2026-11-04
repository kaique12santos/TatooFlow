<template>
  <div>
    <!-- 0. Mobile Push / Webhook Toast Notification -->
    <MobileToastNotification
      :toast="activeToast.activeToast"
      @dismiss="dismissToast"
      @action="handleToastAction"
    />

    <!-- 1. Appointment Confirmation Dialog -->
    <AppointmentConfirmDialog
      v-if="state.type === 'appointment'"
      :is-open="state.isOpen"
      :data="state.appointmentData || null"
      @confirm="handleConfirm"
      @cancel="handleCancel"
      @close="handleClose"
    />

    <!-- 2. Login Success Dialog -->
    <LoginSuccessDialog
      v-else-if="state.type === 'login'"
      :is-open="state.isOpen"
      :data="state.loginData || null"
      @confirm="handleConfirm"
      @close="handleClose"
    />

    <!-- 3. Confirm Dialog (Standard & Destructive) -->
    <ConfirmAlertDialog
      v-else-if="state.type === 'confirm'"
      :is-open="state.isOpen"
      :title="state.options.title"
      :description="state.options.description"
      :badge="state.options.badge"
      :variant="state.options.variant"
      :confirm-label="state.options.confirmLabel"
      :cancel-label="state.options.cancelLabel"
      :is-destructive="state.options.variant === 'danger'"
      @confirm="handleConfirm"
      @cancel="handleCancel"
      @close="handleClose"
    />

    <!-- 4. Standard Alert Dialog (Success, Error, Info, Warning) -->
    <BaseAlertDialog
      v-else
      :is-open="state.isOpen"
      :title="state.options.title"
      :description="state.options.description"
      :badge="state.options.badge"
      :variant="state.options.variant"
      :confirm-label="state.options.confirmLabel"
      :show-cancel="state.options.showCancel"
      :close-on-backdrop="state.options.closeOnBackdrop"
      @confirm="handleConfirm"
      @cancel="handleCancel"
      @close="handleClose"
    />
  </div>
</template>

<script setup lang="ts">
import { useAlert } from '@/composables/useAlert';
import BaseAlertDialog from './BaseAlertDialog.vue';
import ConfirmAlertDialog from './ConfirmAlertDialog.vue';
import AppointmentConfirmDialog from './AppointmentConfirmDialog.vue';
import LoginSuccessDialog from './LoginSuccessDialog.vue';
import MobileToastNotification from './MobileToastNotification.vue';

const {
  state,
  activeToast,
  handleConfirm,
  handleCancel,
  handleClose,
  dismissToast,
  handleToastAction,
} = useAlert();
</script>

