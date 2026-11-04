<template>
  <Transition
    enter-active-class="transform transition duration-300 ease-out"
    enter-from-class="-translate-y-8 opacity-0 scale-95"
    enter-to-class="translate-y-0 opacity-100 scale-100"
    leave-active-class="transform transition duration-200 ease-in"
    leave-from-class="translate-y-0 opacity-100 scale-100"
    leave-to-class="-translate-y-6 opacity-0 scale-95"
  >
    <div
      v-if="toast"
      class="fixed top-[calc(env(safe-area-inset-top)+12px)] left-3 right-3 max-w-[440px] mx-auto z-[9999] pointer-events-auto"
      role="alert"
      aria-live="assertive"
    >
      <div
        class="bg-[#141416]/95 backdrop-blur-xl border border-[rgba(201,168,106,0.35)] rounded-lg shadow-[0_12px_36px_rgba(0,0,0,0.85)] p-3.5 relative overflow-hidden"
      >
        <!-- Accent Glow Line -->
        <div
          :class="[
            'absolute top-0 left-0 right-0 h-[2px]',
            isWhatsApp ? 'bg-gradient-to-r from-[#25D366] via-[#128C7E] to-[#25D366]' : 'bg-gradient-to-r from-[#c9a86a] via-[#e6c383] to-[#b89355]'
          ]"
        ></div>

        <div class="flex items-start gap-3">
          <!-- Icon with Glowing Ping -->
          <div class="relative shrink-0 mt-0.5">
            <div
              :class="[
                'w-9 h-9 rounded-full flex items-center justify-center border shadow-inner',
                isWhatsApp
                  ? 'bg-[#0f2419] border-[#25D366]/40 text-[#25D366]'
                  : 'bg-[#1f1a14] border-[#c9a86a]/40 text-[#e6c383]'
              ]"
            >
              <MessageSquare v-if="isWhatsApp" class="w-4 h-4" />
              <Bell v-else class="w-4 h-4" />
            </div>
            <!-- Pulse indicator -->
            <span
              :class="[
                'absolute -top-0.5 -right-0.5 w-2.5 h-2.5 rounded-full ring-2 ring-[#141416]',
                isWhatsApp ? 'bg-[#25D366] animate-pulse' : 'bg-[#e6c383] animate-pulse'
              ]"
            ></span>
          </div>

          <!-- Notification Body -->
          <div class="flex-1 min-w-0">
            <!-- Header Badges -->
            <div class="flex items-center justify-between gap-2 mb-1">
              <div class="flex items-center gap-1.5">
                <span
                  :class="[
                    'font-jetbrains text-[9px] font-bold tracking-wider uppercase px-1.5 py-0.5 rounded-[2px]',
                    isWhatsApp
                      ? 'bg-[#0f2419] text-[#25D366] border border-[#25D366]/30'
                      : 'bg-[#1f1a14] text-[#e6c383] border border-[#c9a86a]/30'
                  ]"
                >
                  {{ toast.badge || (isWhatsApp ? 'WHATSAPP WEBHOOK' : 'NOTIFICAÇÃO') }}
                </span>
                <span v-if="toast.timeAgo" class="font-jetbrains text-[9px] text-[#7a766f]">
                  {{ toast.timeAgo }}
                </span>
              </div>

              <!-- Close button -->
              <button
                @click="emit('dismiss')"
                class="text-[#7a766f] hover:text-[#f2ebd9] transition-colors p-0.5 cursor-pointer"
                title="Fechar notificação"
              >
                <X class="w-3.5 h-3.5" />
              </button>
            </div>

            <!-- Title (Client Name) -->
            <h4 class="font-playfair text-[14px] font-semibold text-[#f2ebd9] leading-snug truncate">
              {{ toast.title }}
            </h4>

            <!-- Excerpt / Message -->
            <p class="font-inter text-[11.5px] text-[#a39e93] leading-relaxed line-clamp-2 mt-0.5">
              {{ toast.message }}
            </p>

            <!-- Actions Row -->
            <div v-if="toast.onAction || toast.actionLabel" class="mt-2.5 flex items-center justify-end gap-2">
              <button
                @click="handleAction"
                class="h-7 px-3 bg-[#c9a86a] hover:bg-[#d6b77e] active:scale-95 text-[#09090b] font-inter text-[10px] font-bold tracking-wider uppercase rounded-[2px] transition-all cursor-pointer flex items-center gap-1 shadow-sm"
              >
                <span>{{ toast.actionLabel || 'VER TRIAGEM' }}</span>
                <ArrowRight class="w-3 h-3 text-[#09090b]" />
              </button>
            </div>
          </div>
        </div>

        <!-- Progress bar counting down -->
        <div class="absolute bottom-0 left-0 right-0 h-[2px] bg-white/5">
          <div
            class="h-full transition-all linear"
            :class="isWhatsApp ? 'bg-[#25D366]' : 'bg-[#c9a86a]'"
            :style="{ width: `${progress}%`, transitionDuration: '100ms' }"
          ></div>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { computed, ref, watch, onUnmounted } from 'vue';
import { MessageSquare, Bell, X, ArrowRight } from '@lucide/vue';
import type { ToastNotificationData } from './types';

const props = defineProps<{
  toast: ToastNotificationData | null;
}>();

const emit = defineEmits<{
  (e: 'dismiss'): void;
  (e: 'action'): void;
}>();

const progress = ref(100);
let timer: ReturnType<typeof setInterval> | null = null;

const isWhatsApp = computed(() => props.toast?.type === 'whatsapp');

watch(
  () => props.toast,
  (newToast) => {
    if (timer) {
      clearInterval(timer);
      timer = null;
    }

    if (newToast) {
      progress.value = 100;
      const totalDuration = newToast.duration || 6000;
      const interval = 50;
      const step = (interval / totalDuration) * 100;

      timer = setInterval(() => {
        progress.value = Math.max(0, progress.value - step);
        if (progress.value <= 0) {
          if (timer) clearInterval(timer);
          timer = null;
          emit('dismiss');
        }
      }, interval);
    }
  },
  { immediate: true }
);

onUnmounted(() => {
  if (timer) clearInterval(timer);
});

function handleAction() {
  if (timer) clearInterval(timer);
  emit('action');
}
</script>

