<template>
  <ion-page>
    <ion-content class="bg-[#070707] text-[#e6c383]" fullscreen>
      <div class="p-4 max-w-3xl mx-auto">
    <!-- Header -->
    <header class="flex items-center justify-between mb-6">
      <div class="flex items-center gap-4">
        <div class="w-12 h-12 bg-[#0b0b0b] rounded-md border border-[rgba(201,168,106,0.18)] p-2 flex items-center justify-center shadow-sm">
          <img :src="imgNewConceptTattooLogo" alt="logo" class="w-full h-full object-contain" />
        </div>
        <div>
          <div class="text-lg font-playfair leading-tight text-[#f2ebd9]">NEW CONCEPT</div>
          <div class="text-xs text-[#a39e93] tracking-widest">ATELIER & DOSSIÊ</div>
        </div>
      </div>

      <div class="flex items-center gap-3">
        <button class="bg-[#0f0f0f] border border-[rgba(184,147,85,0.14)] rounded-full p-2 shadow-inner">
          <img :src="imgContainer19" alt="bell" class="w-4 h-4" />
        </button>
        <router-link to="/login" class="ml-2 bg-[#c9a86a] text-[#09090b] px-4 py-2 rounded-lg font-semibold">Entrar</router-link>
      </div>
    </header>

    <!-- Main card header -->
    <section class="bg-[#111113] p-4 rounded-2xl shadow-md mb-4 border border-[rgba(230,195,131,0.04)]">
      <div class="flex items-start justify-between">
        <div class="flex gap-3 items-center">
          <div class="w-14 h-14 bg-[#09090b] rounded-full overflow-hidden shadow-sm">
            <img :src="imgAb6AXuAYx5NEdJl5JWnGbQn0S5BRgcm5Ugvx2N229YMuJ4Gz9G40J08Nd4XIeRvjFVvOBuje6YcujI152S8Ti3NTcUMyIoku3BRaCQtJTp4ZpN8YGpAsuwDIxVmLhcNwOvwMcNrJffjvvB4Uf5TyjEx3YXnJTduGOle2HzEf2RtTxgr8N5176CtHdzWpiZb39GzbFnBAuMlXh1QbPcrMy6IDculRfgBroQfpGh7BRg5MaPchswL1Atcq" alt="artist" class="object-cover w-full h-full" />
          </div>
          <div>
            <div class="text-lg font-playfair text-[#f2ebd9]">Gabriel Dornelles</div>
            <div class="text-xs text-[#a39e93]">BANCADA 01 • QUINTA-FEIRA • 24.OUT.2026</div>
          </div>
        </div>
        <div class="flex gap-3 items-center">
          <div class="bg-[rgba(255,255,255,0.02)] text-center p-3 rounded-lg w-24">
            <div class="text-xs text-[#bdb59e]">SESSÕES HOJE</div>
            <div class="text-2xl text-[#e6c383] font-bold">03</div>
          </div>
          <div class="bg-[rgba(255,255,255,0.02)] text-center p-3 rounded-lg w-32">
            <div class="text-xs text-[#bdb59e]">HORAS BANCADA</div>
            <div class="text-2xl text-[#f2ebd9]">07h 30m</div>
          </div>
        </div>
      </div>
    </section>

    <!-- Search + Tabs (Ionic) -->
    <div class="mb-4">
      <ion-header class="bg-transparent p-0">
        <ion-searchbar
            show-cancel-button="never"
            placeholder="Buscar cliente, CPF, termo ou projeto..."
            class="bg-[#0f0f0f] rounded-lg shadow-inner"
          ></ion-searchbar>
      </ion-header>

      <ion-segment value="todos" class="mt-3 bg-transparent rounded-md overflow-hidden">
        <ion-segment-button value="todos">
          <ion-label>TODOS <span class="ml-2 text-xs">5</span></ion-label>
        </ion-segment-button>
        <ion-segment-button value="confirmados">
          <ion-label>CONFIRMADOS <span class="ml-2 text-xs">2</span></ion-label>
        </ion-segment-button>
        <ion-segment-button value="andamento">
          <ion-label>EM ANDAMENTO <span class="ml-2 text-xs">1</span></ion-label>
        </ion-segment-button>
      </ion-segment>
    </div>

    <!-- Appointments list -->
    <main class="space-y-4">
      <ion-card v-for="(card, idx) in cards" :key="idx" class="bg-[#0d0d0f] p-4 rounded-2xl shadow-lg border border-[rgba(230,195,131,0.035)]">
        <ion-card-content>
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-3">
            <div class="w-20 h-20 bg-[#09090b] rounded-full overflow-hidden shadow-sm">
              <img :src="card.image" alt="client" class="object-cover w-full h-full" />
            </div>
            <div>
              <div class="text-lg font-playfair text-[#f2ebd9]">{{ card.name }}</div>
              <div class="text-sm text-[#e5c281]">{{ card.project }}</div>
            </div>
          </div>
          <div class="flex flex-col items-end">
            <div class="bg-[#3d301b] px-2 py-1 rounded text-xs text-[#eedaa2]">{{ card.statusText }}</div>
            <div class="text-sm text-[#a39e93] mt-2">{{ card.time }}</div>
          </div>
        </div>

        <div class="mt-4 grid grid-cols-3 gap-3">
          <div class="bg-[#09090b] p-3 rounded">
            <div class="text-xs text-[#a39e93] uppercase">HONORÁRIO TOTAL</div>
            <div class="text-sm text-[#f2ebd9] font-mono">{{ card.total }}</div>
          </div>
          <div class="bg-[#09090b] p-3 rounded">
            <div class="text-xs text-[#a39e93] uppercase">SINAL PIX RETIDO</div>
            <div class="text-sm text-[#c8e6c9] font-mono">{{ card.signal }}</div>
          </div>
          <div class="bg-[#09090b] p-3 rounded">
            <div class="text-xs text-[#a39e93] uppercase">SALDO RESTANTE</div>
            <div class="text-sm text-[#e6c383] font-mono">{{ card.balance }}</div>
          </div>
        </div>

        <div class="mt-4 flex gap-3">
          <ion-button size="small" fill="outline" color="light">ANAMNESE</ion-button>
          <ion-button size="small" fill="outline" color="light">WHATSAPP</ion-button>
          <ion-button size="small" fill="solid" color="warning">PAUSAR</ion-button>
        </div>
        </ion-card-content>
      </ion-card>
    </main>

    <!-- Bottom nav removed for Agenda view (Ionic tabs provide navigation) -->
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { IonContent, IonPage, IonCard, IonCardContent, IonButton, IonHeader, IonSegment, IonSegmentButton, IonSearchbar,IonLabel } from '@ionic/vue';
import { ref } from 'vue';

const assetPathPrefix = 'https://www.figma.com/api/mcp/asset/bf6fd634-cae9-4f48-b091-5a184c343967';
const imgAb6AXuAYx5NEdJl5JWnGbQn0S5BRgcm5Ugvx2N229YMuJ4Gz9G40J08Nd4XIeRvjFVvOBuje6YcujI152S8Ti3NTcUMyIoku3BRaCQtJTp4ZpN8YGpAsuwDIxVmLhcNwOvwMcNrJffjvvB4Uf5TyjEx3YXnJTduGOle2HzEf2RtTxgr8N5176CtHdzWpiZb39GzbFnBAuMlXh1QbPcrMy6IDculRfgBroQfpGh7BRg5MaPchswL1Atcq = `${assetPathPrefix}/9a55e.png`;
const imgAb6AXuAmOtgRrbjzcEmJwGfbHki70QdYeVQcOwGcW8MikwiC0POoWnmgjfcDNlCvpYuQbhBqzlmmeWcWcxQmAa4XUsbhuMcrvSyuWgdvh05SUx1ZusxdI81UvMM476DbI8PKVaqEpG0BCfVHplhP6WNPvRJlfnaWFcZyViSlT5KFVyyxitJwyYlgEo5WgzNbs3WWtlO3FCnmiYGbNbOev4PN7L59OL64Jphy9X9IdAjxRhEoZe6Oxg = `${assetPathPrefix}/37d21.png`;
const imgNewConceptTattooLogo = `${assetPathPrefix}/5143c.png`;
const imgContainer = `${assetPathPrefix}/dec75.svg`;
const imgContainer1 = `${assetPathPrefix}/7eed8.svg`;
const imgContainer2 = `${assetPathPrefix}/da602.svg`;
const imgContainer19 = `${assetPathPrefix}/e40f3.svg`;
const imgContainer20 = `${assetPathPrefix}/1edd5.svg`;
const imgContainer21 = `${assetPathPrefix}/4d8c3.svg`;
const imgContainer22 = `${assetPathPrefix}/7d8f6.svg`;
const imgContainer23 = `${assetPathPrefix}/d9e7e.svg`;
const imgContainer24 = `${assetPathPrefix}/c235c.svg`;

const cards = ref([
  {
    name: 'Rodrigo Sampaio',
    project: 'Arcanjo Miguel · Chiaroscuro Fechamento Braço',
    image: imgAb6AXuAmOtgRrbjzcEmJwGfbHki70QdYeVQcOwGcW8MikwiC0POoWnmgjfcDNlCvpYuQbhBqzlmmeWcWcxQmAa4XUsbhuMcrvSyuWgdvh05SUx1ZusxdI81UvMM476DbI8PKVaqEpG0BCfVHplhP6WNPvRJlfnaWFcZyViSlT5KFVyyxitJwyYlgEo5WgzNbs3WWtlO3FCnmiYGbNbOev4PN7L59OL64Jphy9X9IdAjxRhEoZe6Oxg,
    statusText: 'EM AGULHAMENTO',
    time: '10:00 — 13:30',
    total: 'R$ 2.400,00',
    signal: 'R$ 800,00 (100%)',
    balance: 'R$ 1.600,00'
  },
  {
    name: 'Helena Brandão',
    project: 'Botânica & Serpente · Gravura Cobre',
    image: `${assetPathPrefix}/8251f.png`,
    statusText: 'SINAL PIX LIQUIDADO',
    time: '14:00 — 18:00',
    total: 'R$ 1.500,00',
    signal: 'R$ 500,00',
    balance: 'R$ 1.000,00'
  },
  {
    name: 'Matheus Falk',
    project: 'Vanitas Costela · Ensaio Anatômico',
    image: `${assetPathPrefix}/11217.png`,
    statusText: 'AVALIAÇÃO / DECALQUE',
    time: '18:30 — 19:15',
    total: 'R$ 1.800 — R$ 2.200',
    signal: 'Pendente Decisão',
    balance: '-'
  }
]);
</script>
