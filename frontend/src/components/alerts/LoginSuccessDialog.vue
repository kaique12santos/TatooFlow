<template>
  <BaseAlertDialog
    :is-open="isOpen"
    variant="success"
    title="Acesso Autorizado"
    confirm-label="Acessar Sistema"
    :show-cancel="false"
    :close-on-backdrop="false"
    @confirm="$emit('confirm')"
    @close="$emit('close')"
  >
    <template #icon>
      <ShieldCheck class="w-6 h-6 text-[#81c784]" />
    </template>

    <template #content>
      <div v-if="data" class="space-y-3">
        <!-- User Profile Card -->
        <div class="bg-[#09090b] border border-[rgba(129,199,132,0.25)] rounded-[4px] p-4 text-center relative overflow-hidden">
          <div class="absolute top-0 right-0 left-0 h-[1.5px] bg-gradient-to-r from-transparent via-[#81c784] to-transparent"></div>

          <!-- Avatar with initials -->
          <div class="w-14 h-14 mx-auto rounded-full bg-[#122316] border border-[#81c784]/40 flex items-center justify-center text-[#81c784] font-playfair text-[20px] font-bold shadow-inner mb-2.5">
            {{ userInitials }}
          </div>

          <h3 class="font-playfair text-[18px] font-semibold text-[#f2ebd9]">
            {{ data.userName }}
          </h3>

          <div class="inline-flex items-center gap-1.5 mt-1.5 px-2.5 py-0.5 rounded-full bg-[#141416] border border-[rgba(201,168,106,0.3)]">
            <span class="w-1.5 h-1.5 rounded-full bg-[#81c784] animate-pulse"></span>
            <span class="font-jetbrains text-[9.5px] font-bold text-[#e6c383] uppercase tracking-wider">
              {{ data.userRole === 'admin' ? 'ADMINISTRAÇÃO DO ATELIER' : 'TATUADOR RESIDENTE' }}
            </span>
          </div>

        </div>

        <p class="text-center font-inter text-[11px] text-[#8e8a82]">
          Redirecionando para o seu espaço de trabalho...
        </p>
      </div>
    </template>
  </BaseAlertDialog>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { ShieldCheck } from '@lucide/vue';
import BaseAlertDialog from './BaseAlertDialog.vue';
import type { LoginSuccessData } from './types';

const props = defineProps<{
  isOpen: boolean;
  data: LoginSuccessData | null;
}>();

defineEmits<{
  confirm: [];
  close: [];
}>();

const userInitials = computed(() => {
  if (!props.data?.userName) return 'TF';
  return props.data.userName
    .split(' ')
    .map((p) => p[0])
    .slice(0, 2)
    .join('')
    .toUpperCase();
});
</script>
