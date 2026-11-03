<template>
  <ion-page>
    <ion-content class="bg-[#09090b] text-[#f2ebd9] select-none font-sans" fullscreen>
      <div class="min-h-screen bg-[#09090b] pb-24 max-w-[480px] mx-auto px-4 pt-3 sm:px-6">
        <!-- Reusable Header Component -->
        <AppHeader />

        <!-- Title & Subtitle Section -->
        <div class="mb-5">
          <h1 class="font-playfair text-[26px] font-semibold text-[#f2ebd9] leading-tight mb-1">
            Relatório Mensal & Auditoria
          </h1>
          <p class="font-montserrat text-[12px] text-[#a39e93] leading-relaxed">
            Conciliação de repasses comissionados, retenção de fees e livro-razão do atelier.
          </p>
        </div>

        <!-- Month Navigation Component -->
        <MonthSelectorCard
          :month-year="financialStore.currentMonth"
          subtitle="MÊS VIGENTE · FECHAMENTO CONSOLIDADO"
          @prev="financialStore.prevMonth"
          @next="financialStore.nextMonth"
        />

        <!-- Section Header -->
        <div class="flex items-center justify-between mb-3 px-0.5">
          <span class="font-inter text-[9px] font-bold text-[#a39e93] tracking-[1.44px] uppercase">
            RESUMO EXECUTIVO MENSAL
          </span>
          <div
            :class="[
              'flex items-center gap-1.5 px-2 py-0.5 rounded-[2px] border',
              financialStore.summary.status === 'conciliado'
                ? 'bg-[#2a3b2e] border-[rgba(200,230,201,0.2)] text-[#c8e6c9]'
                : 'bg-[#292215] border-[rgba(230,195,131,0.2)] text-[#e6c383]'
            ]"
          >
            <span
              :class="[
                'w-1.5 h-1.5 rounded-full',
                financialStore.summary.status === 'conciliado' ? 'bg-[#c8e6c9]' : 'bg-[#e6c383]'
              ]"
            ></span>
            <span class="font-inter text-[9px] font-bold tracking-wider uppercase">
              {{ financialStore.summary.status === 'conciliado' ? 'CONCILIADO' : 'EM ABERTO' }}
            </span>
          </div>
        </div>

        <!-- Faturamento Bruto Total Card -->
        <div class="bg-[#141416] rounded-[2px] p-4 mb-4 border border-[rgba(255,255,255,0.05)] shadow-xl relative overflow-hidden">
          <div class="flex items-center justify-between mb-2">
            <span class="font-inter text-[9px] font-bold text-[#a39e93] tracking-[1.44px] uppercase">
              FATURAMENTO BRUTO TOTAL
            </span>
            <div class="flex items-center gap-1 bg-[rgba(201,168,106,0.1)] border border-[rgba(201,168,106,0.3)] text-[#e6c383] px-2 py-0.5 rounded-[2px] font-jetbrains text-[11px] font-bold">
              <TrendingUp class="w-3 h-3" />
              <span>{{ financialStore.summary.growthPercentage }}</span>
            </div>
          </div>

          <div class="font-playfair text-[28px] font-semibold text-[#e6c383] leading-none mb-3">
            {{ financialStore.summary.grossRevenueFormatted }}
          </div>

          <div class="pt-2.5 border-t border-[rgba(255,255,255,0.04)] flex items-center gap-2">
            <Pencil class="w-3.5 h-3.5 text-[#a39e93]" />
            <span class="font-montserrat text-[12px] text-[#a39e93]">
              {{ financialStore.summary.completedWorksCount }} obras e sessões concluídas no atelier
            </span>
          </div>
        </div>

        <!-- 2 Grid Columns: Taxa Atelier vs Repasse Artistas -->
        <div class="grid grid-cols-2 gap-3 mb-4">
          <!-- Taxa Atelier Card -->
          <div class="bg-[#141416] rounded-[2px] p-3.5 border border-[rgba(255,255,255,0.05)] shadow-lg flex flex-col justify-between">
            <div>
              <div class="flex items-center justify-between mb-2">
                <span class="font-inter text-[9px] font-bold text-[#a39e93] tracking-[1.44px] uppercase">
                  TAXA ATELIER
                </span>
                <span class="bg-[rgba(255,255,255,0.04)] text-[#e6c383] font-jetbrains text-[9px] font-bold px-1.5 py-0.5 rounded-[2px]">
                  {{ financialStore.summary.studioTaxRate }}%
                </span>
              </div>

              <div class="font-playfair text-[18px] font-semibold text-[#f2ebd9] leading-tight mb-2">
                {{ financialStore.summary.studioTaxFormatted }}
              </div>
            </div>

            <div class="pt-2 border-t border-[rgba(255,255,255,0.04)]">
              <span class="font-inter text-[9px] font-bold text-[#a39e93] tracking-[1.44px] uppercase">
                RETENÇÃO DE ESPAÇO
              </span>
            </div>
          </div>

          <!-- Repasse Artistas Card -->
          <div class="bg-[#141416] rounded-[2px] p-3.5 border border-[rgba(255,255,255,0.05)] shadow-lg flex flex-col justify-between">
            <div>
              <div class="flex items-center justify-between mb-2">
                <span class="font-inter text-[9px] font-bold text-[#a39e93] tracking-[1.44px] uppercase truncate">
                  REPASSE ARTISTAS
                </span>
                <span class="bg-[rgba(255,255,255,0.04)] text-[#e6c383] font-jetbrains text-[9px] font-bold px-1.5 py-0.5 rounded-[2px]">
                  {{ financialStore.summary.artistShareRate }}%
                </span>
              </div>

              <div class="font-playfair text-[18px] font-semibold text-[#e6c383] leading-tight mb-2">
                {{ financialStore.summary.artistShareFormatted }}
              </div>
            </div>

            <div class="pt-2 border-t border-[rgba(255,255,255,0.04)]">
              <span class="font-inter text-[9px] font-bold text-[#a39e93] tracking-[1.44px] uppercase">
                {{ financialStore.summary.activeArtistsCount }} RESIDENTES/GUESTS
              </span>
            </div>
          </div>
        </div>

        <!-- Custódia Sinais PIX Card -->
        <div class="bg-[#1a1a1e] rounded-[2px] p-3.5 mb-4 border border-[rgba(201,168,106,0.3)] shadow-xl flex items-center justify-between">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 bg-[#141416] border border-[rgba(201,168,106,0.4)] rounded-[2px] flex items-center justify-center text-[#e6c383] shrink-0">
              <Lock class="w-4.5 h-4.5" />
            </div>

            <div>
              <span class="font-inter text-[9px] font-bold text-[#a39e93] tracking-[1.44px] uppercase block">
                CUSTÓDIA SINAIS PIX
              </span>
              <span class="font-playfair text-[18px] font-semibold text-[#f2ebd9] leading-tight">
                {{ financialStore.summary.pixEscrowFormatted }}
              </span>
            </div>
          </div>

          <div class="bg-[#141416] border border-[rgba(201,168,106,0.4)] text-[#d6b77e] px-2.5 py-1 rounded-[2px] font-inter text-[9px] font-bold tracking-wider uppercase">
            GARANTIDOS
          </div>
        </div>

        <!-- Governança Notarial Banner -->
        <div class="bg-[#141416] rounded-[2px] p-4 mb-5 border border-[rgba(255,255,255,0.05)] shadow-lg flex items-start gap-3">
          <ShieldCheck class="w-5 h-5 text-[#e6c383] shrink-0 mt-0.5" />
          <div>
            <h3 class="font-inter text-[11px] font-bold text-[#f2ebd9] tracking-wider uppercase mb-1">
              CERTIFICAÇÃO DIGITAL NOTARIAL
            </h3>
            <p class="font-montserrat text-[12px] text-[#a39e93] leading-relaxed">
              Os balancetes deste atelier são criptografados para resguardo tributário e transparência irrestrita com os artistas residentes.
            </p>
          </div>
        </div>

        <!-- Action Buttons -->
        <div class="space-y-3">
          <button
            @click="downloadPdf"
            class="w-full bg-[#c9a86a] hover:bg-[#d6b77e] text-[#09090b] font-inter text-[11px] font-bold tracking-wider uppercase py-3.5 px-4 rounded-[2px] shadow-lg flex items-center justify-center gap-2.5 transition-all cursor-pointer active:scale-[0.99]"
          >
            <FileText class="w-4 h-4" />
            <span>BAIXAR RELATÓRIO MENSAL (PDF)</span>
          </button>

          <button
            @click="openArtistReport"
            class="w-full bg-[#141416] hover:bg-[#1a1a1e] text-[#f2ebd9] border border-[#c9a86a] font-inter text-[11px] font-bold tracking-wider uppercase py-3.5 px-4 rounded-[2px] flex items-center justify-between transition-all cursor-pointer active:scale-[0.99]"
          >
            <div class="flex items-center gap-2">
              <Users class="w-4 h-4 text-[#e6c383]" />
              <span class="text-[#d6b77e]">RELATÓRIO POR ARTISTA</span>
            </div>
            <ArrowRight class="w-4 h-4 text-[#e6c383]" />
          </button>
        </div>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { IonPage, IonContent } from '@ionic/vue';
import { TrendingUp, Pencil, Lock, ShieldCheck, FileText, Users, ArrowRight } from '@lucide/vue';
import { useRouter } from 'vue-router';
import AppHeader from '../components/AppHeader.vue';
import MonthSelectorCard from '../components/MonthSelectorCard.vue';
import { useFinancialStore } from '@/stores/financial';

const router = useRouter();
const financialStore = useFinancialStore();

// Sincroniza balancete mensal do backend com fallback mock
onMounted(async () => {
  await financialStore.fetchMonthlyClosing();
});

async function downloadPdf() {
  const result = await financialStore.downloadMonthlyReportPdf();
  alert(result.message);
}

function openArtistReport() {
  router.push('/performance-artistas');
}
</script>
