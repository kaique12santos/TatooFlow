<template>
  <ion-page>
    <ion-content class="bg-[#09090b] text-[#f2ebd9] select-none font-sans" fullscreen>
      <div class="screen-container min-h-screen pb-28 pt-3">
        <!-- Reusable App Header -->
        <AppHeader />

        <!-- Title & Subtitle -->
        <div class="mb-4">
          <h1 class="font-playfair text-[26px] sm:text-[28px] font-semibold text-[#f2ebd9] leading-tight mb-1">
            Gestão de Estoque &amp; Repasses
          </h1>
        </div>

        <!-- Section Switcher Tabs & Tablet Action Bar -->
        <ion-grid class="ion-no-padding mb-5">
          <ion-row class="ion-align-items-center ion-justify-content-between">
            <!-- Section Switcher Tabs (Mobile 100%, Tablet 7 cols) -->
            <ion-col size="12" size-md="7" class="ion-no-padding mb-3 md:mb-0">
              <div class="grid grid-cols-2 gap-2">
                <button
                  @click="activeSection = 'insumos'"
                  :class="[
                    'py-5 px-5 rounded-[2px] font-inter text-[10px] font-bold tracking-[1.2px] uppercase flex items-center justify-center gap-2 transition-all cursor-pointer',
                    activeSection === 'insumos'
                      ? 'bg-[#141416] border border-[rgba(201,168,106,0.3)] text-[#e6c383] shadow-md'
                      : 'bg-transparent border border-[rgba(255,255,255,0.06)] text-[#a39e93] hover:text-[#f2ebd9]'
                  ]"
                >
                  <Package class="w-4 h-4" />
                  <span>ESTOQUE DE INSUMOS</span>
                </button>

                <!-- <button
                  @click="activeSection = 'repasses'"
                  :class="[
                    'py-2.5 px-3 rounded-[2px] font-inter text-[10px] font-bold tracking-[1.2px] uppercase flex items-center justify-center gap-2 transition-all cursor-pointer',
                    activeSection === 'repasses'
                      ? 'bg-[#141416] border border-[rgba(201,168,106,0.3)] text-[#e6c383] shadow-md'
                      : 'bg-transparent border border-[rgba(255,255,255,0.06)] text-[#a39e93] hover:text-[#f2ebd9]'
                  ]"
                >
                  <Wallet class="w-4 h-4" />
                  <span>REPASSES &amp; TAXAS</span>
                </button> -->
              </div>
            </ion-col>

            <!-- Quick Action on Tablet (hidden on mobile, visible on tablet) -->
            <ion-col size="12" size-md="5" class="ion-no-padding hidden md:block md:pl-4">
              <button
                @click="cadastrarInsumos"
                class="w-full bg-[#c9a86a] hover:bg-[#d6b77e] text-[#09090b] font-inter text-[10.5px] font-bold tracking-[1.2px] uppercase py-2.5 px-4 rounded-[2px] shadow-lg flex items-center justify-center gap-2 transition-all cursor-pointer active:scale-[0.99]"
              >
                <FileText class="w-4 h-4 stroke-[2.2]" />
                <span>+ CADASTRAR INSUMOS</span>
              </button>
            </ion-col>
          </ion-row>
        </ion-grid>

        <!-- Banner de Notificação em Tempo Real se houver alocações recentes -->
        <div v-if="inventoryStore.allocationHistory.length > 0" class="mb-4 bg-[#141416] border border-[rgba(201,168,106,0.3)] rounded-[2px] p-3 flex items-center justify-between shadow-lg">
          <div class="flex items-center gap-2.5">
            <span class="w-2 h-2 rounded-full bg-[#86efac] shrink-0 animate-pulse"></span>
            <div>
              <div class="font-inter text-[11.5px] text-[#f2ebd9] font-medium leading-tight">
                Última baixa alocada em bancada: <strong>{{ inventoryStore.lastAllocationProtocol }}</strong>
              </div>
              <div class="font-jetbrains text-[9.5px] text-[#a39e93] mt-0.5">
                {{ inventoryStore.allocationHistory[0]?.items.length }} insumos deduzidos • Saldo sincronizado em tempo real
              </div>
            </div>
          </div>
          <span class="font-jetbrains text-[9px] text-[#e6c383] uppercase tracking-wider bg-[#221c13] px-2 py-1 rounded-[2px] border border-[rgba(201,168,106,0.2)]">
            SINCRONIZADO
          </span>
        </div>

        <!-- Section 1: Alertas Críticos (Mobile 1 coluna, Tablet 2 colunas) -->
        <div class="mb-5">
          <!-- Section Header Row -->
          <div class="flex items-center justify-between mb-3 px-0.5">
            <div class="flex items-center gap-1.5">
              <AlertTriangle class="w-4 h-4 text-[#ef4444]" />
              <h2 class="font-playfair text-[18px] font-semibold text-[#f2ebd9]">
                Alertas Críticos
              </h2>
            </div>

            <span class="bg-[#341416] border border-[rgba(239,68,68,0.3)] text-[#f87171] font-inter text-[9px] font-bold tracking-wider uppercase px-2 py-0.5 rounded-[2px]">
              {{ inventoryStore.criticalItemsCount }} PENDÊNCIAS
            </span>
          </div>

          <ion-grid class="ion-no-padding">
            <ion-row v-if="criticalItems.length > 0">
              <ion-col
                v-for="item in criticalItems"
                :key="item.id"
                size="12"
                size-md="6"
                class="ion-no-padding mb-3 md:pr-2"
              >
                <div class="bg-[#141416] border border-[rgba(239,68,68,0.4)] rounded-[2px] p-3.5 h-full shadow-xl relative">
                  <div class="flex items-start justify-between gap-3">
                    <div>
                      <div class="flex items-center gap-1.5 mb-1">
                        <span class="w-1.5 h-1.5 bg-[#ef4444] rounded-[1px]"></span>
                        <span class="font-inter text-[9px] font-bold text-[#ef4444] tracking-[1.44px] uppercase">
                          REPOSIÇÃO IMEDIATA • ESTOQUE CRÍTICO
                        </span>
                      </div>

                      <h3 class="font-playfair text-[17px] font-semibold text-[#f2ebd9] leading-tight my-1">
                        {{ item.name }}
                      </h3>

                      <p class="font-montserrat text-[11px] text-[#a39e93]">
                        Estoque atual: <strong class="text-[#f2ebd9] font-medium">{{ item.currentStock }} {{ item.unit }}</strong> • Mínimo: {{ item.minStock }}
                      </p>
                    </div>

                    <div class="w-10 h-10 bg-[#261315] border border-[rgba(239,68,68,0.3)] rounded-[2px] flex items-center justify-center text-[#f87171] shrink-0 shadow-md">
                      <AlertTriangle class="w-4.5 h-4.5" />
                    </div>
                  </div>
                </div>
              </ion-col>
            </ion-row>
            <div v-else class="text-center py-6 bg-[#141416] rounded-[2px] border border-[rgba(255,255,255,0.04)] mb-4">
              <p class="font-montserrat text-[12px] text-[#86efac]">Nenhum item em nível crítico no banco de dados.</p>
            </div>
          </ion-grid>
        </div>

        <!-- Section 2: Insumos do Atelier (Todos os itens cadastrados no banco) -->
        <div class="mb-5">
          <div class="flex items-center justify-between mb-3 px-0.5">
            <h2 class="font-playfair text-[19px] font-semibold text-[#f2ebd9]">
              Insumos em Estoque
            </h2>
            <span class="font-jetbrains text-[10px] text-[#c9a86a] font-bold uppercase">
              {{ inventoryStore.stockItems.length }} ITENS CADASTRADOS
            </span>
          </div>

          <ion-grid class="ion-no-padding">
            <ion-row v-if="inventoryStore.stockItems.length > 0">
              <ion-col
                v-for="item in inventoryStore.stockItems"
                :key="item.id"
                size="12"
                size-md="6"
                class="ion-no-padding mb-2.5 md:pr-2"
              >
                <div class="bg-[#141416] border border-[rgba(255,255,255,0.06)] rounded-[2px] p-3 shadow-lg flex items-center justify-between h-full">
                  <div class="flex items-center gap-3">
                    <div class="w-10 h-10 bg-[#09090b] rounded-[2px] border border-[rgba(201,168,106,0.25)] flex items-center justify-center text-[#c9a86a] shrink-0">
                      <Package class="w-5 h-5" />
                    </div>
                    <div>
                      <span class="font-inter text-[9px] font-bold text-[#a39e93] tracking-[1.2px] uppercase block">
                        {{ item.category.toUpperCase() }}
                      </span>
                      <h3 class="font-playfair text-[15px] font-semibold text-[#f2ebd9] leading-tight">
                        {{ item.name }}
                      </h3>
                      <span class="font-inter text-[9px] font-bold text-[#a39e93] tracking-[1.2px] uppercase mt-0.5 block">
                        SALDO: {{ item.currentStock }} {{ item.unit }} • MÍN: {{ item.minStock }}
                      </span>
                    </div>
                  </div>

                  <!-- Quantity Stepper conectado ao backend -->
                  <div class="bg-[#09090b] border border-[rgba(255,255,255,0.08)] rounded-[2px] px-2.5 py-1 flex items-center gap-3 shrink-0">
                    <button
                      @click="inventoryStore.adjustItemStock(item.id, -1)"
                      class="text-[#a39e93] hover:text-[#f2ebd9] font-bold text-sm cursor-pointer transition-colors active:scale-90"
                    >
                      –
                    </button>
                    <span class="font-jetbrains text-[13px] font-bold text-[#f2ebd9] min-w-[14px] text-center">
                      {{ item.currentStock }}
                    </span>
                    <button
                      @click="inventoryStore.adjustItemStock(item.id, 1)"
                      class="text-[#a39e93] hover:text-[#f2ebd9] font-bold text-sm cursor-pointer transition-colors active:scale-90"
                    >
                      +
                    </button>
                  </div>
                </div>
              </ion-col>
            </ion-row>
            <div v-else class="text-center py-8 bg-[#141416] rounded-[2px] border border-[rgba(255,255,255,0.04)]">
              <p class="font-montserrat text-[12px] text-[#a39e93]">Nenhum item cadastrado no estoque.</p>
            </div>
          </ion-grid>
        </div>

        <!-- Primary Action CTA Button: Cadastrar Insumos (Mobile 100%, Tablet contido e centralizado) -->
        <ion-grid class="ion-no-padding mt-2 mb-6">
          <ion-row class="ion-justify-content-center">
            <ion-col size="12" size-md="6" size-lg="5" class="ion-no-padding">
              <button
                @click="cadastrarInsumos"
                class="w-full bg-[#c9a86a] hover:bg-[#d6b77e] text-[#09090b] font-inter text-[12px] font-bold tracking-[1.44px] uppercase py-3.5 px-4 rounded-[2px] shadow-xl flex items-center justify-center gap-2 transition-all cursor-pointer active:scale-[0.99]"
              >
                <FileText class="w-4 h-4 stroke-[2.2]" />
                <span>CADASTRAR INSUMOS</span>
              </button>
            </ion-col>
          </ion-row>
        </ion-grid>
      </div>

    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { IonPage, IonContent, IonGrid, IonRow, IonCol } from '@ionic/vue';
import {
  Package,
  Wallet,
  AlertTriangle,
  FileText,
} from '@lucide/vue';
import { useRouter } from 'vue-router';
import AppHeader from '../components/AppHeader.vue';
import { useInventoryStore } from '@/stores/inventory';

const router = useRouter();
const inventoryStore = useInventoryStore();

onMounted(async () => {
  await inventoryStore.fetchStock();
});

// Computed properties vinculadas à inventoryStore
const criticalItems = computed(() => inventoryStore.stockItems.filter((i) => i.currentStock <= i.minStock));

// Section state
const activeSection = ref<'insumos' | 'repasses'>('insumos');

function cadastrarInsumos() {
  router.push('/cadastrar-insumo');
}
</script>