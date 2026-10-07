<template>
  <ion-page>
    <ion-content class="bg-[#09090b] text-[#f2ebd9] select-none font-sans" fullscreen>
      <div class="min-h-screen bg-[#09090b] pb-24 max-w-[480px] mx-auto px-4 pt-3 sm:px-6">
        <!-- Reusable Header Component -->
        <AppHeader />

        <!-- Back to Fechamento Geral Link -->
        <button
          @click="goBack"
          class="flex items-center gap-1.5 font-inter text-[11px] font-bold text-[#e6c383] uppercase tracking-wider mb-3 hover:text-[#d6b77e] transition-colors cursor-pointer"
        >
          <ArrowLeft class="w-4 h-4" />
          <span>VOLTAR AO FECHAMENTO GERAL</span>
        </button>

        <!-- Meta Row & Screen Title -->
        <div class="mb-5">
          <div class="flex items-center justify-between mb-2">
            <div class="flex items-center gap-1.5">
              <span class="w-2 h-2 rounded-[1px] bg-[#c9a86a]"></span>
              <span class="font-inter text-[9px] font-bold text-[#a39e93] tracking-[1.44px] uppercase">
                VISUALIZAÇÃO DE PERFORMANCE
              </span>
            </div>

            <span class="bg-[#141416] border border-[rgba(201,168,106,0.2)] text-[#e6c383] font-jetbrains text-[11px] font-bold px-2.5 py-1 rounded-[2px]">
              {{ financialStore.currentMonth }}
            </span>
          </div>

          <h1 class="font-playfair text-[26px] font-semibold text-[#f2ebd9] leading-tight mb-1">
            Performance dos Artistas
          </h1>
          <p class="font-montserrat text-[12px] text-[#a39e93] leading-relaxed">
            Apuração mensal de produção individual e retenção da taxa contratual do atelier ({{ financialStore.summary.artistShareRate }}% Artista / {{ financialStore.summary.studioTaxRate }}% Estúdio).
          </p>
        </div>

        <!-- Global Performance Summary Card -->
        <div class="bg-[#141416] rounded-[2px] p-4 mb-5 border border-[rgba(255,255,255,0.05)] shadow-xl grid grid-cols-2 gap-3">
          <div>
            <span class="font-inter text-[9px] font-bold text-[#a39e93] tracking-[1.44px] uppercase block mb-1">
              PRODUÇÃO GLOBAL
            </span>
            <span class="font-playfair text-[20px] font-semibold text-[#f2ebd9] block leading-tight">
              {{ financialStore.summary.grossRevenueFormatted }}
            </span>
            <span class="font-montserrat text-[12px] text-[#a39e93] mt-1 block">
              {{ financialStore.summary.activeArtistsCount }} Artistas Ativos
            </span>
          </div>

          <div class="border-l border-[rgba(255,255,255,0.04)] pl-3">
            <span class="font-inter text-[9px] font-bold text-[#a39e93] tracking-[1.44px] uppercase block mb-1">
              TAXA ESTÚDIO ({{ financialStore.summary.studioTaxRate }}%)
            </span>
            <span class="font-playfair text-[20px] font-semibold text-[#d6b77e] block leading-tight">
              {{ financialStore.summary.studioTaxFormatted }}
            </span>
            <span class="font-montserrat text-[12px] text-[#a39e93] mt-1 block truncate">
              Repasse: {{ financialStore.summary.artistShareFormatted }}
            </span>
          </div>
        </div>

        <!-- List of Artist Performance Cards -->
        <div class="space-y-4">
          <ArtistPerformanceCard
            v-for="artist in financialStore.summary.artistsBreakdown"
            :key="artist.name"
            :artist="artist"
          />
        </div>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { IonPage, IonContent } from '@ionic/vue';
import { ArrowLeft } from '@lucide/vue';
import { useRouter } from 'vue-router';
import AppHeader from '../components/AppHeader.vue';
import ArtistPerformanceCard from '../components/ArtistPerformanceCard.vue';
import { useFinancialStore } from '@/stores/financial';

const router = useRouter();
const financialStore = useFinancialStore();

// Sincroniza fechamento e performance individual dos artistas
onMounted(async () => {
  await financialStore.fetchMonthlyClosing();
});

function goBack() {
  router.push('/fechamento');
}
</script>
