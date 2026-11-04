<template>
  <ion-modal
    :is-open="isOpen"
    :aria-labelledby="labelledById"
    :backdrop-dismiss="closeOnBackdrop"
    class="luxury-alert-dialog"
    @didDismiss="handleDismiss"
  >
    <div
      role="alertdialog"
      :aria-labelledby="labelledById"
      aria-modal="true"
      class="bg-[#141416] border rounded-[4px] p-5 text-[#f2ebd9] shadow-2xl relative max-h-[calc(100dvh-40px)] overflow-y-auto no-scrollbar"
      :class="borderVariantClass"
    >
      <!-- Top Decorative Gold Accent Line -->
      <div
        class="absolute top-0 left-0 right-0 h-[2px]"
        :class="topAccentClass"
      ></div>

      <!-- Header Section: Badge, Icon & Title -->
      <div class="flex flex-col items-center text-center mb-4">
        <!-- Optional Protocol / Context Badge -->
        <div v-if="badge || $slots.badge" class="mb-2.5">
          <slot name="badge">
            <span
              class="inline-flex items-center gap-1 font-jetbrains text-[9px] font-bold tracking-[1.8px] uppercase px-2.5 py-0.5 rounded-[2px] border"
              :class="badgeVariantClass"
            >
              <span>✦</span>
              <span>{{ badge }}</span>
            </span>
          </slot>
        </div>

        <!-- Variant Icon Container -->
        <div
          class="w-12 h-12 rounded-full border flex items-center justify-center mb-3 shadow-inner transition-transform"
          :class="iconContainerClass"
        >
          <slot name="icon">
            <component :is="defaultIcon" class="w-6 h-6" />
          </slot>
        </div>

        <!-- Title -->
        <h2
          :id="labelledById"
          class="font-playfair text-[20px] sm:text-[22px] font-semibold text-[#f2ebd9] leading-snug tracking-wide"
        >
          <slot name="title">{{ title }}</slot>
        </h2>

        <!-- Subtitle / Description -->
        <p
          v-if="description || $slots.description"
          class="font-inter text-[12.5px] text-[#a39e93] leading-relaxed mt-1.5 max-w-[320px]"
        >
          <slot name="description">{{ description }}</slot>
        </p>
      </div>

      <!-- Main Slot Content (For Structured Summaries, Data Cards, Insumos) -->
      <div v-if="$slots.content" class="mb-5">
        <slot name="content" />
      </div>

      <!-- Actions Section -->
      <div class="mt-4 pt-3 border-t border-[rgba(255,255,255,0.06)]">
        <slot name="actions">
          <div class="flex items-center gap-2.5">
            <button
              v-if="showCancel"
              type="button"
              @click="$emit('cancel')"
              class="flex-1 h-11 px-4 rounded-[2px] bg-[#1a1a1e] hover:bg-[#25252a] text-[#b8afa0] hover:text-[#f2ebd9] border border-[rgba(255,255,255,0.08)] font-inter text-[11px] font-bold tracking-[1.2px] uppercase transition-all active:scale-[0.98] cursor-pointer"
            >
              {{ cancelLabel || 'Cancelar' }}
            </button>

            <button
              type="button"
              @click="$emit('confirm')"
              class="flex-1 h-11 px-4 rounded-[2px] font-inter text-[11px] font-bold tracking-[1.4px] uppercase flex items-center justify-center gap-2 transition-all active:scale-[0.98] cursor-pointer shadow-lg"
              :class="primaryButtonClass"
            >
              <span>{{ confirmLabel || 'Confirmar' }}</span>
            </button>
          </div>
        </slot>
      </div>
    </div>
  </ion-modal>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { IonModal } from '@ionic/vue';
import {
  CheckCircle2,
  AlertTriangle,
  AlertOctagon,
  Info,
  Sparkles,
} from '@lucide/vue';
import type { AlertVariant } from './types';

const props = withDefaults(
  defineProps<{
    isOpen: boolean;
    variant?: AlertVariant;
    title?: string;
    description?: string;
    badge?: string;
    confirmLabel?: string;
    cancelLabel?: string;
    showCancel?: boolean;
    closeOnBackdrop?: boolean;
    labelledById?: string;
  }>(),
  {
    variant: 'gold',
    title: '',
    description: '',
    badge: '',
    confirmLabel: 'OK',
    cancelLabel: 'Cancelar',
    showCancel: false,
    closeOnBackdrop: true,
    labelledById: 'alert-dialog-title',
  }
);

const emit = defineEmits<{
  confirm: [];
  cancel: [];
  close: [];
}>();

function handleDismiss() {
  emit('close');
}

// Default Lucide Icon depending on variant
const defaultIcon = computed(() => {
  switch (props.variant) {
    case 'success':
      return CheckCircle2;
    case 'danger':
      return AlertOctagon;
    case 'warning':
      return AlertTriangle;
    case 'info':
      return Info;
    case 'gold':
    default:
      return Sparkles;
  }
});

// Border variant around dialog
const borderVariantClass = computed(() => {
  switch (props.variant) {
    case 'success':
      return 'border-[rgba(129,199,132,0.35)]';
    case 'danger':
      return 'border-[rgba(239,68,68,0.4)]';
    case 'warning':
      return 'border-[rgba(245,158,11,0.35)]';
    case 'info':
      return 'border-[rgba(59,130,246,0.35)]';
    case 'gold':
    default:
      return 'border-[rgba(201,168,106,0.35)]';
  }
});

// Top thin accent bar
const topAccentClass = computed(() => {
  switch (props.variant) {
    case 'success':
      return 'bg-gradient-to-r from-transparent via-[#81c784] to-transparent';
    case 'danger':
      return 'bg-gradient-to-r from-transparent via-[#ef4444] to-transparent';
    case 'warning':
      return 'bg-gradient-to-r from-transparent via-[#f59e0b] to-transparent';
    case 'info':
      return 'bg-gradient-to-r from-transparent via-[#3b82f6] to-transparent';
    case 'gold':
    default:
      return 'bg-gradient-to-r from-transparent via-[#c9a86a] to-transparent';
  }
});

// Icon container styling
const iconContainerClass = computed(() => {
  switch (props.variant) {
    case 'success':
      return 'bg-[#0f2416] text-[#81c784] border-[#2e7d32]/40 shadow-[0_0_15px_rgba(129,199,132,0.2)]';
    case 'danger':
      return 'bg-[#291214] text-[#f87171] border-[#ef4444]/40 shadow-[0_0_15px_rgba(239,68,68,0.2)]';
    case 'warning':
      return 'bg-[#291c09] text-[#fbbf24] border-[#f59e0b]/40 shadow-[0_0_15px_rgba(245,158,11,0.2)]';
    case 'info':
      return 'bg-[#0f172a] text-[#93c5fd] border-[#3b82f6]/40 shadow-[0_0_15px_rgba(59,130,246,0.2)]';
    case 'gold':
    default:
      return 'bg-[#1f1911] text-[#e6c383] border-[#c9a86a]/40 shadow-[0_0_15px_rgba(201,168,106,0.2)]';
  }
});

// Badge variant styling
const badgeVariantClass = computed(() => {
  switch (props.variant) {
    case 'success':
      return 'bg-[#0e2116] border-[#81c784]/30 text-[#81c784]';
    case 'danger':
      return 'bg-[#261315] border-[#ef4444]/30 text-[#f87171]';
    case 'warning':
      return 'bg-[#261c0c] border-[#f59e0b]/30 text-[#fbbf24]';
    case 'info':
      return 'bg-[#0f172a] border-[#3b82f6]/30 text-[#93c5fd]';
    case 'gold':
    default:
      return 'bg-[#1c1813] border-[#c9a86a]/30 text-[#e6c383]';
  }
});

// Primary CTA button styling
const primaryButtonClass = computed(() => {
  switch (props.variant) {
    case 'danger':
      return 'bg-[#ef4444] hover:bg-[#dc2626] text-[#ffffff] shadow-[0_4px_16px_rgba(239,68,68,0.35)]';
    case 'success':
      return 'bg-[#2e7d32] hover:bg-[#388e3c] text-[#ffffff] shadow-[0_4px_16px_rgba(46,125,50,0.35)]';
    case 'gold':
    default:
      return 'bg-[#c9a86a] hover:bg-[#d6b77e] text-[#09090b] shadow-[0_4px_16px_rgba(201,168,106,0.3)]';
  }
});
</script>

<style scoped>
.luxury-alert-dialog {
  --width: calc(100% - 32px);
  --max-width: 420px;
  --height: auto;
  --border-radius: 4px;
  --background: transparent;
  --backdrop-opacity: 0.82;
  --box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.75);
}
.luxury-alert-dialog::part(backdrop) {
  backdrop-filter: blur(10px);
  background: rgba(0, 0, 0, 0.85);
}
.luxury-alert-dialog::part(content) {
  box-sizing: border-box;
  background: transparent;
  border-radius: 4px;
}
</style>

