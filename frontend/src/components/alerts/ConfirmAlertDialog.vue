<template>
  <BaseAlertDialog
    :is-open="isOpen"
    :variant="isDestructive ? 'danger' : variant"
    :title="title"
    :description="description"
    :badge="badge || (isDestructive ? 'AÇÃO IRREVERSÍVEL' : 'CONFIRMAÇÃO')"
    :confirm-label="confirmLabel || (isDestructive ? 'Sim, Confirmar' : 'Confirmar')"
    :cancel-label="cancelLabel || 'Cancelar'"
    :show-cancel="true"
    :close-on-backdrop="!isDestructive"
    @confirm="$emit('confirm')"
    @cancel="$emit('cancel')"
    @close="$emit('close')"
  >
    <!-- Slot for optional custom content -->
    <template #content v-if="$slots.default">
      <slot />
    </template>
  </BaseAlertDialog>
</template>

<script setup lang="ts">
import BaseAlertDialog from './BaseAlertDialog.vue';
import type { AlertVariant } from './types';

withDefaults(
  defineProps<{
    isOpen: boolean;
    title: string;
    description?: string;
    badge?: string;
    variant?: AlertVariant;
    isDestructive?: boolean;
    confirmLabel?: string;
    cancelLabel?: string;
  }>(),
  {
    variant: 'gold',
    isDestructive: false,
    confirmLabel: '',
    cancelLabel: 'Cancelar',
  }
);

defineEmits<{
  confirm: [];
  cancel: [];
  close: [];
}>();
</script>

