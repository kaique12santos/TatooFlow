<template>
  <BaseAlertDialog
    :is-open="isOpen"
    variant="gold"
    title="Revisão do Agendamento"
    description="Confira todos os dados operacionais antes de confirmar o agendamento no atelier."
    badge="ETAPA DE REVISÃO E GOVERNANÇA"
    confirm-label="Confirmar e Salvar"
    cancel-label="Revisar Dados"
    :show-cancel="true"
    :close-on-backdrop="false"
    @confirm="$emit('confirm')"
    @cancel="$emit('cancel')"
    @close="$emit('close')"
  >
    <template #icon>
      <CalendarCheck class="w-6 h-6 text-[#e6c383]" />
    </template>

    <template #content>
      <div v-if="data" class="space-y-3">
        <!-- Client & Artist Block -->
        <div class="bg-[#09090b] border border-[rgba(255,255,255,0.06)] rounded-[3px] p-3 space-y-2.5">
          <!-- Client Row -->
          <div class="flex items-start justify-between gap-2">
            <div>
              <span class="font-jetbrains text-[9px] font-bold text-[#a39e93] tracking-wider uppercase block">
                CLIENTE
              </span>
              <p class="font-playfair text-[15px] font-semibold text-[#f2ebd9] mt-0.5">
                {{ data.clientName }}
              </p>
              <p v-if="data.clientPhone" class="font-jetbrains text-[11px] text-[#c9a86a]">
                {{ data.clientPhone }}
              </p>
            </div>
            <span class="bg-[#141416] border border-[rgba(201,168,106,0.3)] text-[#e6c383] font-jetbrains text-[9px] font-bold px-2 py-0.5 rounded-[2px] uppercase">
              CONFIRMADO
            </span>
          </div>

          <!-- Artist Row -->
          <div class="pt-2 border-t border-[rgba(255,255,255,0.04)] flex items-center justify-between text-xs">
            <div>
              <span class="font-jetbrains text-[9px] font-bold text-[#a39e93] tracking-wider uppercase block">
                TATUADOR RESPONSÁVEL
              </span>
              <p class="font-inter text-[12px] font-medium text-[#f2ebd9] mt-0.5">
                {{ data.artistName }}
              </p>
            </div>
            <span class="text-[#b8afa0] font-jetbrains text-[10px]">
              {{ data.artistStation || 'BANCADA DESIGNADA' }}
            </span>
          </div>
        </div>

        <!-- Schedule & Timing Ingot -->
        <div class="grid grid-cols-2 gap-2 bg-[#09090b] border border-[rgba(255,255,255,0.06)] rounded-[3px] p-3 text-xs">
          <div>
            <span class="font-jetbrains text-[9px] font-bold text-[#a39e93] tracking-wider uppercase block">
              DATA & HORÁRIO
            </span>
            <div class="flex items-center gap-1.5 mt-1 text-[#f2ebd9]">
              <Calendar class="w-3.5 h-3.5 text-[#c9a86a]" />
              <span class="font-jetbrains font-medium text-[12px]">
                {{ formatarData(data.data) }} · {{ data.hora }}
              </span>
            </div>
          </div>

          <div class="border-l border-[rgba(255,255,255,0.04)] pl-2.5">
            <span class="font-jetbrains text-[9px] font-bold text-[#a39e93] tracking-wider uppercase block">
              DURAÇÃO PREVISTA
            </span>
            <div class="flex items-center gap-1.5 mt-1 text-[#f2ebd9]">
              <Clock class="w-3.5 h-3.5 text-[#c9a86a]" />
              <span class="font-jetbrains font-medium text-[12px]">
                {{ formatarDuracao(data.duracaoMinutos) }}
              </span>
            </div>
            <p v-if="data.terminoPrevisto" class="font-jetbrains text-[9.5px] text-[#a39e93] mt-0.5">
              Até {{ data.terminoPrevisto }}
            </p>
          </div>
        </div>

        <!-- Financial Breakdown (RN01: 50% Sinal) -->
        <div class="bg-[#09090b] border border-[rgba(201,168,106,0.2)] rounded-[3px] p-3 flex items-center justify-between">
          <div>
            <span class="font-jetbrains text-[9px] font-bold text-[#a39e93] tracking-wider uppercase block">
              VALOR TOTAL ESTIMADO
            </span>
            <span class="font-playfair text-[18px] font-semibold text-[#f2ebd9]">
              {{ formatarMoeda(data.valorEstimado) }}
            </span>
          </div>

          <div class="text-right">
            <span class="font-jetbrains text-[9px] font-bold text-[#e6c383] tracking-wider uppercase block">
              SINAL EXIGIDO (50%)
            </span>
            <span class="font-jetbrains text-[14px] font-bold text-[#81c784]">
              {{ formatarMoeda(data.sinalEstimado ?? data.valorEstimado * 0.5) }}
            </span>
          </div>
        </div>

        <!-- Description Note if present -->
        <div v-if="data.descricaoSessao" class="bg-[#09090b] border border-[rgba(255,255,255,0.04)] rounded-[3px] p-2.5">
          <span class="font-jetbrains text-[8.5px] font-bold text-[#a39e93] tracking-wider uppercase block mb-0.5">
            DESCRIÇÃO DA SESSÃO
          </span>
          <p class="font-inter text-[11px] text-[#b8afa0] italic line-clamp-2">
            "{{ data.descricaoSessao }}"
          </p>
        </div>
      </div>
    </template>
  </BaseAlertDialog>
</template>

<script setup lang="ts">
import { CalendarCheck, Calendar, Clock } from '@lucide/vue';
import BaseAlertDialog from './BaseAlertDialog.vue';
import type { AppointmentConfirmationData } from './types';

defineProps<{
  isOpen: boolean;
  data: AppointmentConfirmationData | null;
}>();

defineEmits<{
  confirm: [];
  cancel: [];
  close: [];
}>();

function formatarData(dataStr?: string) {
  if (!dataStr) return '';
  const [ano, mes, dia] = dataStr.split('-');
  return `${dia}/${mes}/${ano}`;
}

function formatarDuracao(minutos: number) {
  if (!minutos) return '0 min';
  const horas = Math.floor(minutos / 60);
  const minRestantes = minutos % 60;
  if (horas === 0) return `${minRestantes} min`;
  if (minRestantes === 0) return `${horas}h 00`;
  return `${horas}h ${String(minRestantes).padStart(2, '0')}`;
}

function formatarMoeda(valor?: number | null) {
  if (valor == null) return 'R$ 0,00';
  return valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
}
</script>

