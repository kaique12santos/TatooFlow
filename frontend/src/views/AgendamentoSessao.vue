<template>
  <ion-page>
    <ion-content class="bg-[#09090b] text-[#f2ebd9] font-sans" fullscreen>
      <div class="screen-container min-h-screen pb-12 pt-3">
        <!-- Compact Session Header (Back button, More menu, Studio logo) -->
        <header class="flex items-center justify-between py-2 mb-3">
          <button
            @click="handleBack"
            class="w-9 h-9 bg-[#141416] hover:bg-[#1e1d21] border border-[rgba(255,255,255,0.08)] rounded-[2px] flex items-center justify-center text-[#a39e93] hover:text-[#f2ebd9] transition-colors cursor-pointer"
            title="Voltar para Triagem"
          >
            <ArrowLeft class="w-4.5 h-4.5" />
          </button>

          <div class="flex items-center gap-2">
            <button
              @click="handleMenu"
              class="w-8 h-8 rounded-full flex items-center justify-center text-[#a39e93] hover:text-[#f2ebd9] transition-colors cursor-pointer"
              title="Mais opções"
            >
              <MoreVertical class="w-4 h-4" />
            </button>
            <div class="w-9 h-9 bg-[#000000] border border-[#b89355] rounded-full p-1 flex items-center justify-center shadow-lg">
              <img :src="logoUrl" alt="New Concept Tattoo" class="w-full h-full object-contain rounded-full" />
            </div>
          </div>
        </header>

        <!-- Step Tracker / Progress Bar -->
        <div class="mb-4 px-0.5">
          <div class="flex items-center justify-between mb-1.5">
            <div class="flex items-center gap-1.5 text-[#c9a86a]">
              <Compass class="w-3.5 h-3.5 text-[#c9a86a]" />
              <span class="font-jetbrains text-[10px] font-bold tracking-[1.4px] uppercase">
                ETAPA 1 DE 2 • ESPAÇO &amp; AGENDA
              </span>
            </div>
            <span class="font-jetbrains text-[11px] font-bold text-[#a39e93] tracking-wider uppercase">
              50% CONCLUÍDO
            </span>
          </div>

          <!-- 2-Segment Progress Bar -->
          <div class="grid grid-cols-2 gap-2">
            <div class="h-1 bg-[#c9a86a] rounded-full shadow-[0_0_8px_rgba(201,168,106,0.4)]"></div>
            <div class="h-1 bg-[#222226] rounded-full"></div>
          </div>
        </div>

        <!-- SECTION 1: SINAL CONFIRMADO & ORDEM DE EXECUÇÃO CARD -->
        <div class="bg-[#141416] border border-[rgba(255,255,255,0.07)] rounded-[4px] p-3.5 mb-3.5 shadow-lg">
          <!-- Top Badge Row -->
          <div class="flex items-center justify-between mb-2.5">
            <div
              v-if="client.depositPaid"
              class="bg-[#132c1c] border border-[#235838] px-2.5 py-1 rounded-[2px] flex items-center gap-1.5"
            >
              <CheckCircle2 class="w-3.5 h-3.5 text-[#86efac]" />
              <span class="font-jetbrains text-[9px] font-bold text-[#86efac] tracking-[0.5px] uppercase">
                SINAL CONFIRMADO • R$ {{ client.depositAmount.toFixed(2).replace('.', ',') }} (PIX AUTENTICADO)
              </span>
            </div>
            <div
              v-else
              class="bg-[#2d2417] border border-[rgba(201,168,106,0.3)] px-2.5 py-1 rounded-[2px] flex items-center gap-1.5"
            >
              <Clock class="w-3.5 h-3.5 text-[#e6c383]" />
              <span class="font-jetbrains text-[9px] font-bold text-[#e6c383] tracking-[0.5px] uppercase">
                GARANTIA DE BANCADA • 50% SINAL PENDENTE (R$ {{ client.depositAmount.toFixed(2).replace('.', ',') }})
              </span>
            </div>

            <span class="font-jetbrains text-[11px] font-bold text-[#a39e93]">
              {{ client.lotId }}
            </span>
          </div>

          <!-- Order of Execution Header -->
          <div class="font-jetbrains text-[9px] font-bold tracking-[1.4px] text-[#8e8a82] uppercase mb-0.5">
            ORDEM DE EXECUÇÃO
          </div>
          <h2 class="font-playfair text-[20px] font-semibold text-[#f2ebd9] leading-tight mb-3">
            {{ client.projectTitle }}
          </h2>

          <!-- 2x2 Grid Info -->
          <div class="grid grid-cols-2 gap-x-3 gap-y-2.5 pt-2 border-t border-[rgba(255,255,255,0.05)]">
            <!-- Col 1, Row 1: Cliente -->
            <div>
              <div class="font-jetbrains text-[8.5px] font-bold tracking-[1px] text-[#8e8a82] uppercase">
                CLIENTE SOLICITANTE
              </div>
              <div class="font-inter text-[13px] font-semibold text-[#f2ebd9] mt-0.5 truncate">
                {{ client.name }}
              </div>
            </div>
            <!-- Col 2, Row 1: Artista -->
            <div>
              <div class="font-jetbrains text-[8.5px] font-bold tracking-[1px] text-[#8e8a82] uppercase">
                ARTISTA RESPONSÁVEL
              </div>
              <div class="font-inter text-[13px] font-bold text-[#d6b77e] mt-0.5 truncate">
                {{ client.assignedArtist }}
              </div>
            </div>
            <!-- Col 1, Row 2: Técnica -->
            <div>
              <div class="font-jetbrains text-[8.5px] font-bold tracking-[1px] text-[#8e8a82] uppercase">
                ABORDAGEM TÉCNICA
              </div>
              <div class="font-inter text-[12px] text-[#a39e93] mt-0.5 truncate">
                {{ client.style }}
              </div>
            </div>
            <!-- Col 2, Row 2: Tempo -->
            <div>
              <div class="font-jetbrains text-[8.5px] font-bold tracking-[1px] text-[#8e8a82] uppercase">
                TEMPO ESTIMADO
              </div>
              <div class="flex items-center gap-1 font-jetbrains text-[12px] font-bold text-[#f2ebd9] mt-0.5">
                <Clock class="w-3.5 h-3.5 text-[#c9a86a]" />
                <span>{{ client.estimatedDuration }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- SECTION 2: BANCADA DO ATELIER -->
        <div class="bg-[#141416] border border-[rgba(255,255,255,0.07)] rounded-[4px] p-3.5 mb-3.5 shadow-lg">
          <!-- Header: Bancada + Badge ESPAÇO BLOQUEADO -->
          <div class="flex items-start justify-between mb-2">
            <div class="flex items-center gap-2.5">
              <div class="w-8 h-8 rounded-[2px] bg-[#1a1917] border border-[rgba(201,168,106,0.2)] flex items-center justify-center text-[#c9a86a] shrink-0">
                <Armchair class="w-4.5 h-4.5 text-[#c9a86a]" />
              </div>
              <div>
                <h3 class="font-playfair text-[16px] font-semibold text-[#f2ebd9] leading-tight">
                  {{ client.station || 'Bancada 01' }}
                </h3>
                <div class="font-jetbrains text-[8.5px] font-bold tracking-[1.2px] text-[#8e8a82] uppercase mt-0.5">
                  ALA PRINCIPAL DO ATELIER
                </div>
              </div>
            </div>

            <!-- Badge: ESPAÇO BLOQUEADO -->
            <div class="bg-[#2d2417] border border-[rgba(201,168,106,0.3)] px-2.5 py-1 rounded-[2px] text-center">
              <span class="font-jetbrains text-[8.5px] font-bold text-[#d6b77e] tracking-[1px] uppercase block leading-tight">
                ESPAÇO
              </span>
              <span class="font-jetbrains text-[8.5px] font-bold text-[#d6b77e] tracking-[1px] uppercase block leading-tight">
                BLOQUEADO
              </span>
            </div>
          </div>

          <!-- Checklist Rows -->
          <div class="divide-y divide-[rgba(255,255,255,0.05)] mt-2">
            <div class="py-2 flex items-center gap-2.5 font-inter text-[12px] text-[#f2ebd9]">
              <CheckCircle2 class="w-4 h-4 text-[#c9a86a] shrink-0" />
              <span>Autoclave Concluída</span>
            </div>
            <div class="py-2 flex items-center gap-2.5 font-inter text-[12px] text-[#f2ebd9]">
              <CheckCircle2 class="w-4 h-4 text-[#c9a86a] shrink-0" />
              <span>Iluminação Focal</span>
            </div>
            <div class="py-2 flex items-center gap-2.5 font-inter text-[12px] text-[#f2ebd9]">
              <CheckCircle2 class="w-4 h-4 text-[#c9a86a] shrink-0" />
              <span>Terminal Tablet &amp; Fonte Calibrada</span>
            </div>
          </div>
        </div>

        <!-- SECTION 3: DISPONIBILIDADE DE BANCADA & JANELA DE TURNO -->
        <div class="mb-4">
          <!-- Title Row -->
          <div class="flex items-center justify-between mb-2 px-0.5">
            <div>
              <div class="font-jetbrains text-[9.5px] font-bold tracking-[1.4px] text-[#c9a86a] uppercase">
                DISPONIBILIDADE DE BANCADA
              </div>
              <h2 class="font-playfair text-[20px] font-semibold text-[#f2ebd9] leading-tight mt-0.5">
                Selecione a Janela de Turno
              </h2>
            </div>
            <CalendarDays class="w-5 h-5 text-[#8e8a82]" />
          </div>

          <!-- 3 Day Selector Cards Grid -->
          <div class="grid grid-cols-3 gap-2.5 my-3">
            <!-- Day 1: 28 MAI -->
            <button
              @click="selectedDay = '28'"
              :class="[
                'rounded-[4px] p-2.5 text-center cursor-pointer transition-all',
                selectedDay === '28'
                  ? 'bg-[#141416] border border-[#c9a86a] shadow-[0_0_12px_rgba(201,168,106,0.15)]'
                  : 'bg-[#141416] border border-[rgba(255,255,255,0.07)] hover:border-[rgba(201,168,106,0.3)]'
              ]"
            >
              <div class="font-jetbrains text-[9px] font-bold tracking-[1px] text-[#8e8a82] uppercase">
                TERÇA
              </div>
              <div class="font-jetbrains text-[15px] font-bold text-[#f2ebd9] my-0.5">
                28 MAI
              </div>
              <div class="bg-[#241e17] text-[#d6b77e] border border-[rgba(201,168,106,0.25)] text-[8px] font-jetbrains font-bold px-1.5 py-0.5 rounded-[2px] mx-auto w-fit uppercase">
                1 VAGA
              </div>
            </button>

            <!-- Day 2: 29 MAI (SELECTED) -->
            <button
              @click="selectedDay = '29'"
              :class="[
                'rounded-[4px] p-2.5 text-center cursor-pointer transition-all',
                selectedDay === '29'
                  ? 'bg-[#141416] border border-[#c9a86a] shadow-[0_0_12px_rgba(201,168,106,0.15)]'
                  : 'bg-[#141416] border border-[rgba(255,255,255,0.07)] hover:border-[rgba(201,168,106,0.3)]'
              ]"
            >
              <div class="font-jetbrains text-[9px] font-bold tracking-[1px] text-[#e6c383] uppercase">
                QUARTA
              </div>
              <div class="font-jetbrains text-[15px] font-bold text-[#f2ebd9] my-0.5">
                29 MAI
              </div>
              <div class="bg-[#382e1d] text-[#e6c383] border border-[#c9a86a] text-[8px] font-jetbrains font-bold px-1.5 py-0.5 rounded-[2px] mx-auto w-fit uppercase">
                SELECIONADO
              </div>
            </button>

            <!-- Day 3: 30 MAI -->
            <button
              @click="selectedDay = '30'"
              :class="[
                'rounded-[4px] p-2.5 text-center cursor-pointer transition-all',
                selectedDay === '30'
                  ? 'bg-[#141416] border border-[#c9a86a] shadow-[0_0_12px_rgba(201,168,106,0.15)]'
                  : 'bg-[#141416] border border-[rgba(255,255,255,0.07)] hover:border-[rgba(201,168,106,0.3)]'
              ]"
            >
              <div class="font-jetbrains text-[9px] font-bold tracking-[1px] text-[#8e8a82] uppercase">
                QUINTA
              </div>
              <div class="font-jetbrains text-[15px] font-bold text-[#f2ebd9] my-0.5">
                30 MAI
              </div>
              <div class="bg-[#0e2417] text-[#4ade80] border border-[#225533] text-[8px] font-jetbrains font-bold px-1.5 py-0.5 rounded-[2px] mx-auto w-fit uppercase">
                2 VAGAS
              </div>
            </button>
          </div>

          <!-- Shift Slots -->
          <div class="space-y-2">
            <!-- Slot 1: Turno Manhã -->
            <div
              @click="selectedShift = 'manha'"
              :class="[
                'rounded-[4px] p-3 flex items-center justify-between cursor-pointer transition-colors',
                selectedShift === 'manha'
                  ? 'bg-[#141416] border-l-2 border-l-[#c9a86a] border border-[rgba(201,168,106,0.35)] shadow-md'
                  : 'bg-[#141416] border border-[rgba(255,255,255,0.07)] hover:border-[rgba(201,168,106,0.2)]'
              ]"
            >
              <div class="flex items-center gap-2">
                <span class="font-jetbrains text-[14px] font-bold text-[#f2ebd9]">09:30 — 14:00</span>
                <span class="font-jetbrains text-[9.5px] font-bold text-[#8e8a82] uppercase">TURNO MANHÃ</span>
              </div>
              <div
                v-if="selectedShift === 'manha'"
                class="w-4 h-4 rounded-full border-2 border-[#e6c383] flex items-center justify-center p-0.5"
              >
                <div class="w-2 h-2 rounded-full bg-[#e6c383]"></div>
              </div>
              <button
                v-else
                class="bg-[#09090b] hover:bg-[#1f1d19] border border-[rgba(255,255,255,0.1)] text-[#f2ebd9] font-jetbrains text-[9.5px] font-bold px-3 py-1.5 rounded-[2px] uppercase cursor-pointer transition-colors"
              >
                ESCOLHER
              </button>
            </div>

            <!-- Slot 2: Turno Tarde (SELECTED / RECOMENDADO IA) -->
            <div
              @click="selectedShift = 'tarde'"
              class="bg-[#141416] border-l-2 border-l-[#c9a86a] border border-[rgba(201,168,106,0.35)] rounded-[4px] p-3.5 shadow-lg cursor-pointer transition-all"
            >
              <div class="flex items-center justify-between">
                <span class="font-jetbrains text-[15px] font-bold text-[#f2ebd9]">14:30 — 19:00</span>
                <!-- Radio Double Circle Selected -->
                <div class="w-4 h-4 rounded-full border-2 border-[#e6c383] flex items-center justify-center p-0.5">
                  <div class="w-2 h-2 rounded-full bg-[#e6c383]"></div>
                </div>
              </div>

              <div class="bg-[#2d2417] border border-[rgba(201,168,106,0.3)] text-[#e6c383] font-jetbrains text-[8.5px] font-bold px-2 py-0.5 rounded-[2px] tracking-wider uppercase inline-block my-1.5">
                RECOMENDADO IA • JANELA IDEAL
              </div>

              <p class="font-inter text-[11.5px] text-[#a39e93] leading-snug mb-2.5">
                Janela de rendimento de pigmento &amp; foco ergonômico contínuo
              </p>

              <div class="flex items-center gap-2 text-[#8e8a82] font-jetbrains text-[10px] pt-1.5 border-t border-[rgba(255,255,255,0.05)]">
                <div class="flex items-center gap-1">
                  <Hourglass class="w-3.5 h-3.5 text-[#c9a86a]" />
                  <span>{{ client.estimatedDuration }} de Agulhamento</span>
                </div>
                <span>•</span>
                <div class="flex items-center gap-1">
                  <Sparkles class="w-3.5 h-3.5 text-[#c9a86a]" />
                  <span>15 min Assepsia</span>
                </div>
              </div>
            </div>

            <!-- Slot 3: Turno Noite (OCUPADO) -->
            <div class="bg-[#0d0d0e] border border-[rgba(255,255,255,0.04)] rounded-[4px] p-3 opacity-60">
              <div class="flex items-center justify-between">
                <div class="flex items-center gap-2">
                  <span class="font-jetbrains text-[13px] text-[#8e8a82] line-through">19:30 — 23:30</span>
                  <span class="font-jetbrains text-[9px] font-bold text-[#8e8a82] uppercase">OCUPADO</span>
                </div>
                <Lock class="w-3.5 h-3.5 text-[#8e8a82]" />
              </div>
              <div class="font-inter text-[11px] text-[#5a5852] mt-0.5">
                Sessão externa agendada para Gabriel D.
              </div>
            </div>
          </div>

          <!-- Mandatory Notice -->
          <div class="flex items-start gap-2 text-[#8e8a82] font-inter text-[10.5px] leading-relaxed my-4 px-0.5">
            <Info class="w-4 h-4 text-[#8e8a82] shrink-0 mt-0.5" />
            <span>Intervalo técnico mandatório de 30 minutos bloqueado antes e após para protocolo de assepsia e desinfecção química das superfícies.</span>
          </div>

          <!-- Primary CTA Button: Avançar para Reserva de Insumos -->
          <button
            @click="avancarParaInsumos"
            class="w-full bg-[#c9a86a] hover:bg-[#d6b77e] text-[#09090b] font-inter text-[11.5px] font-bold tracking-[1.4px] uppercase py-3.5 px-4 rounded-[2px] shadow-xl flex items-center justify-center gap-2 cursor-pointer transition-all active:scale-[0.99] mb-4"
          >
            <span>AVANÇAR PARA RESERVA DE INSUMOS (2/2)</span>
            <ArrowRight class="w-4 h-4 text-[#09090b] stroke-[2.2]" />
          </button>
        </div>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { IonPage, IonContent } from '@ionic/vue';
import { useRouter, useRoute } from 'vue-router';
import {
  ArrowLeft,
  MoreVertical,
  Compass,
  CheckCircle2,
  Clock,
  Armchair,
  CalendarDays,
  Hourglass,
  Sparkles,
  Lock,
  Info,
  ArrowRight
} from '@lucide/vue';
import { useTriageStore } from '@/stores/triage';

const router = useRouter();
const route = useRoute();
const triageStore = useTriageStore();

// Dynamic client from Route Query or Store
const clientId = computed(() => {
  return (route.query.clientId as string) || triageStore.selectedClientId || 'cli-beatriz';
});

const client = computed(() => {
  return triageStore.getClientById(clientId.value);
});

// State
const selectedDay = ref('29');
const selectedShift = ref('tarde');
const isSaving = ref(false);

// Sincroniza agendamento do cliente via backend (com fallback mock)
onMounted(async () => {
  await triageStore.fetchClientSchedule(clientId.value);
  if (client.value?.scheduledSlot) {
    selectedDay.value = client.value.scheduledSlot.day || '29';
    selectedShift.value = client.value.scheduledSlot.shift || 'tarde';
  }
});

// Studio logo
const logoUrl = 'https://s3-alpha-sig.figma.com/img/3c73/8bb6/09078c4aab5df9f0c9aa2e5b4489ac50?Expires=1792368000&Key-Pair-Id=APKAQ4GOSFWCW27IBOMQ&Signature=SCdgUzfuAfo61MxW3nRPxZOcJqBEhQi-8pORR9hhvKMCiPgugvyqSkk5p5z~NsYx4HWlSe8d0~9xPpbGi4f0np1DwPhWBszD0Gtf2B-5-ZuCuO~lDpnfdBGO~MsVmMg7G7G~0X2qrANB-bLoTOBm7T3s7JPpz5pmaCt1bhEI3Ja4DW8T1u0TVhCJ-n~ZU3t6Hi37tnzBY1fUiCqqStql4MyyqsQSyU4~bwbhMxIZimqefof0zrJi1lvERse9cv-po2bpTcfnoftzt0aTLVsIoMurlP4ZphisFdZ2MFrErItliBLSAQkiGd7jHKFImnqew8JRbdJ2AI5CYU3ebuHZZQ__';

function handleBack() {
  router.push('/triagem');
}

function handleMenu() {
  alert(`Opções da Sessão ${client.value.lotId}: Reatribuir bancada, exportar ficha ou cancelar agendamento.`);
}

async function avancarParaInsumos() {
  const dayLabel = selectedDay.value === '28' ? 'TERÇA • 28 MAI' : selectedDay.value === '29' ? 'QUARTA • 29 MAI' : 'QUINTA • 30 MAI';
  const shiftLabel = selectedShift.value === 'manha' ? 'TURNO MANHÃ' : selectedShift.value === 'tarde' ? 'TURNO TARDE' : 'TURNO NOITE';
  const time = selectedShift.value === 'manha' ? '09:30 — 14:00' : selectedShift.value === 'tarde' ? '14:30 — 19:00' : '19:30 — 23:30';

  isSaving.value = true;
  try {
    // Executa persistência com validação condicional backend / mock
    await triageStore.saveScheduleSlot(client.value.id, {
      day: selectedDay.value,
      dayLabel,
      shift: selectedShift.value,
      shiftLabel,
      time,
    });
  } catch (err) {
    console.warn('[AgendamentoSessao] Erro ao sincronizar agendamento:', err);
  } finally {
    isSaving.value = false;
  }

  router.push({
    path: '/alocacao-materiais',
    query: { clientId: client.value.id },
  });
}
</script>
