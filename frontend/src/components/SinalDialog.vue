<template>
  <CompactModal :is-open="sinal !== null" labelled-by="titulo-sinal" @close="$emit('close')">
        <div class="flex items-center justify-between gap-3 pb-3 border-b border-white/5 mb-3">
          <h2 id="titulo-sinal" class="font-playfair text-[18px] font-semibold">Sinal recebido</h2>
          <button type="button" aria-label="Fechar detalhes do sinal" @click="$emit('close')" class="p-2 text-[#c9a86a]"><X class="w-5 h-5" /></button>
        </div>
        <dl v-if="sinal" class="space-y-3 text-xs bg-[#09090b] border border-white/5 rounded-[2px] p-3">
          <div><dt class="text-[#a39e93]">Cliente</dt><dd>{{ sinal.clientName }}</dd></div>
          <div><dt class="text-[#a39e93]">Valor estimado</dt><dd>{{ moeda(sinal.total) }}</dd></div>
          <div><dt class="text-[#a39e93]">Sinal de 50%</dt><dd class="text-[#c8e6c9]">{{ moeda(sinal.amount) }}</dd></div>
          <div><dt class="text-[#a39e93]">Status</dt><dd>Recebido</dd></div>
          <div><dt class="text-[#a39e93]">Comprovante</dt><dd class="break-words">{{ sinal.receiptName || 'Nome do arquivo não disponível' }}</dd></div>
        </dl>
        <p v-if="loading" role="status" class="mt-5 text-sm text-[#a39e93]">Carregando comprovante...</p>
        <div v-if="error" role="alert" class="mt-5 text-sm text-[#ef8b83]">
          <p>{{ error }}</p>
          <button type="button" @click="$emit('retry')" class="mt-2 underline">Tentar novamente</button>
        </div>
        <div v-if="sinal?.receiptUrl" class="mt-6 flex flex-wrap gap-3 text-sm text-[#e6c383]">
          <a :href="sinal.receiptUrl" target="_blank" rel="noopener noreferrer" class="rounded border border-[#c9a86a]/40 p-3">Abrir comprovante</a>
          <a :href="sinal.receiptUrl" :download="sinal.receiptName || 'comprovante'" class="rounded border border-[#c9a86a]/40 p-3">Baixar comprovante</a>
        </div>
  </CompactModal>
</template>

<script setup lang="ts">
import { X } from '@lucide/vue';
import CompactModal from '@/components/CompactModal.vue';

export interface SinalDetails {
  clientName: string;
  total?: number | null;
  amount?: number | null;
  receiptName?: string;
  receiptUrl?: string;
}

defineProps<{ sinal: SinalDetails | null; loading?: boolean; error?: string }>();
defineEmits<{ close: []; retry: [] }>();

function moeda(valor?: number | null) {
  return valor == null ? 'Não informado' : valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
}
</script>

<style scoped>
button { min-height: 40px; }
</style>
