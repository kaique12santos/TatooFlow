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
          <p class="font-montserrat text-[12px] text-[#a39e93] leading-relaxed">
            Curadoria material, rastreio de esterilização e liquidação de proventos.
          </p>
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
                    'py-2.5 px-3 rounded-[2px] font-inter text-[10px] font-bold tracking-[1.2px] uppercase flex items-center justify-center gap-2 transition-all cursor-pointer',
                    activeSection === 'insumos'
                      ? 'bg-[#141416] border border-[rgba(201,168,106,0.3)] text-[#e6c383] shadow-md'
                      : 'bg-transparent border border-[rgba(255,255,255,0.06)] text-[#a39e93] hover:text-[#f2ebd9]'
                  ]"
                >
                  <Package class="w-4 h-4" />
                  <span>ESTOQUE DE INSUMOS</span>
                </button>

                <button
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
                </button>
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
            <ion-row>
              <!-- Alert Card 1: Cartuchos 03RL Cheyenne -->
              <ion-col size="12" size-md="6" class="ion-no-padding mb-3 md:mb-0 md:pr-2">
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
                        Cartuchos 03RL Cheyenne
                      </h3>

                      <p class="font-montserrat text-[11px] text-[#a39e93]">
                        Estoque atual: <strong class="text-[#f2ebd9] font-medium">{{ itemCheyenne03?.currentStock ?? 4 }} unidades</strong> • Mínimo do Atelier: {{ itemCheyenne03?.minStock ?? 15 }}
                      </p>
                    </div>

                    <div class="w-10 h-10 bg-[#261315] border border-[rgba(239,68,68,0.3)] rounded-[2px] flex items-center justify-center text-[#f87171] shrink-0 shadow-md">
                      <Syringe class="w-4.5 h-4.5" />
                    </div>
                  </div>
                </div>
              </ion-col>

              <!-- Alert Card 2: Tinta Dynamic Black -->
              <ion-col size="12" size-md="6" class="ion-no-padding md:pl-2">
                <div class="bg-[#141416] border border-[rgba(201,168,106,0.3)] rounded-[2px] p-3.5 h-full shadow-xl relative">
                  <div class="flex items-start justify-between gap-3">
                    <div>
                      <div class="flex items-center gap-1.5 mb-1">
                        <span class="w-1.5 h-1.5 bg-[#e6c383] rounded-[1px]"></span>
                        <span class="font-inter text-[9px] font-bold text-[#e6c383] tracking-[1.44px] uppercase">
                          ATENÇÃO • REORDENAMENTO EM ESPERA
                        </span>
                      </div>

                      <h3 class="font-playfair text-[17px] font-semibold text-[#f2ebd9] leading-tight my-1">
                        Tinta Dynamic Black (240ml)
                      </h3>

                      <p class="font-montserrat text-[11px] text-[#a39e93]">
                        Estoque: <strong class="text-[#f2ebd9] font-medium">{{ itemDynamicBlack?.currentStock ?? 2 }} frascos</strong> • Consumo estimado: 10 dias
                      </p>
                    </div>

                    <div class="w-10 h-10 bg-[#1e1913] border border-[rgba(201,168,106,0.3)] rounded-[2px] flex items-center justify-center text-[#e6c383] shrink-0 shadow-md">
                      <Pipette class="w-4.5 h-4.5" />
                    </div>
                  </div>
                </div>
              </ion-col>
            </ion-row>
          </ion-grid>
        </div>

        <!-- Section 2: Insumos & Autoclave (Mobile 1 coluna, Tablet 2 colunas) -->
        <div class="mb-5">
          <h2 class="font-playfair text-[19px] font-semibold text-[#f2ebd9] mb-3 px-0.5">
            Insumos &amp; Autoclave
          </h2>

          <ion-grid class="ion-no-padding">
            <ion-row>
              <!-- Item 1: Cartuchos 07MG Magnum -->
              <ion-col size="12" size-md="6" class="ion-no-padding mb-2.5 md:pr-2">
                <div class="bg-[#141416] border border-[rgba(255,255,255,0.06)] rounded-[2px] p-3 shadow-lg flex items-center justify-between h-full">
                  <div class="flex items-center gap-3">
                    <div class="w-12 h-12 bg-[#09090b] rounded-[2px] border border-[rgba(255,255,255,0.08)] overflow-hidden shrink-0">
                      <img :src="itemImages.needles" alt="Cartuchos 07MG Magnum" class="w-full h-full object-cover" />
                    </div>
                    <div>
                      <span class="font-inter text-[9px] font-bold text-[#a39e93] tracking-[1.2px] uppercase block">
                        AGULHAS KWADRON
                      </span>
                      <h3 class="font-playfair text-[15px] font-semibold text-[#f2ebd9] leading-tight">
                        Cartuchos 07MG Magnum
                      </h3>
                      <span class="font-inter text-[9px] font-bold text-[#a39e93] tracking-[1.2px] uppercase mt-0.5 block">
                        UNIDADES: {{ itemMagnum07?.currentStock ?? 28 }} / CAIXA DE 20
                      </span>
                    </div>
                  </div>

                  <!-- Quantity Stepper -->
                  <div class="bg-[#09090b] border border-[rgba(255,255,255,0.08)] rounded-[2px] px-2.5 py-1 flex items-center gap-3 shrink-0">
                    <button
                      @click="decrementQty('cartuchos')"
                      class="text-[#a39e93] hover:text-[#f2ebd9] font-bold text-sm cursor-pointer transition-colors active:scale-90"
                    >
                      –
                    </button>
                    <span class="font-jetbrains text-[13px] font-bold text-[#f2ebd9] min-w-[14px] text-center">
                      {{ quantities.cartuchos }}
                    </span>
                    <button
                      @click="incrementQty('cartuchos')"
                      class="text-[#a39e93] hover:text-[#f2ebd9] font-bold text-sm cursor-pointer transition-colors active:scale-90"
                    >
                      +
                    </button>
                  </div>
                </div>
              </ion-col>

              <!-- Item 2: Luvas Nitrílicas Pretas (M) -->
              <ion-col size="12" size-md="6" class="ion-no-padding mb-2.5 md:pl-2">
                <div class="bg-[#141416] border border-[rgba(255,255,255,0.06)] rounded-[2px] p-3 shadow-lg flex items-center justify-between h-full">
                  <div class="flex items-center gap-3">
                    <div class="w-12 h-12 bg-[#09090b] rounded-[2px] border border-[rgba(255,255,255,0.08)] overflow-hidden shrink-0">
                      <img :src="itemImages.gloves" alt="Luvas Nitrílicas Pretas" class="w-full h-full object-cover" />
                    </div>
                    <div>
                      <span class="font-inter text-[9px] font-bold text-[#a39e93] tracking-[1.2px] uppercase block">
                        BIOSSEGURANÇA
                      </span>
                      <h3 class="font-playfair text-[15px] font-semibold text-[#f2ebd9] leading-tight">
                        Luvas Nitrílicas Pretas (M)
                      </h3>
                      <span class="font-inter text-[9px] font-bold text-[#a39e93] tracking-[1.2px] uppercase mt-0.5 block">
                        UNIDADES: {{ Math.max(1, Math.floor((itemLuvasM?.currentStock ?? 180) / 15)) }} CAIXAS ({{ itemLuvasM?.currentStock ?? 180 }} PARES)
                      </span>
                    </div>
                  </div>

                  <!-- Quantity Stepper -->
                  <div class="bg-[#09090b] border border-[rgba(255,255,255,0.08)] rounded-[2px] px-2.5 py-1 flex items-center gap-3 shrink-0">
                    <button
                      @click="decrementQty('luvas')"
                      class="text-[#a39e93] hover:text-[#f2ebd9] font-bold text-sm cursor-pointer transition-colors active:scale-90"
                    >
                      –
                    </button>
                    <span class="font-jetbrains text-[13px] font-bold text-[#f2ebd9] min-w-[14px] text-center">
                      {{ quantities.luvas }}
                    </span>
                    <button
                      @click="incrementQty('luvas')"
                      class="text-[#a39e93] hover:text-[#f2ebd9] font-bold text-sm cursor-pointer transition-colors active:scale-90"
                    >
                      +
                    </button>
                  </div>
                </div>
              </ion-col>

              <!-- Item 3: Stencil Stuff Transfer 250ml -->
              <ion-col size="12" size-md="6" class="ion-no-padding mb-2.5 md:pr-2">
                <div class="bg-[#141416] border border-[rgba(255,255,255,0.06)] rounded-[2px] p-3 shadow-lg flex items-center justify-between h-full">
                  <div class="flex items-center gap-3">
                    <div class="w-12 h-12 bg-[#09090b] rounded-[2px] border border-[rgba(255,255,255,0.08)] overflow-hidden shrink-0">
                      <img :src="itemImages.stencil" alt="Stencil Stuff Transfer" class="w-full h-full object-cover" />
                    </div>
                    <div>
                      <span class="font-inter text-[9px] font-bold text-[#a39e93] tracking-[1.2px] uppercase block">
                        FIXAÇÃO &amp; DECALQUE
                      </span>
                      <h3 class="font-playfair text-[15px] font-semibold text-[#f2ebd9] leading-tight">
                        Stencil Stuff Transfer 250ml
                      </h3>
                      <span class="font-inter text-[9px] font-bold text-[#a39e93] tracking-[1.2px] uppercase mt-0.5 block">
                        UNIDADES: {{ itemStencilStuff?.currentStock ?? 7 }} FRASCOS
                      </span>
                    </div>
                  </div>

                  <!-- Quantity Stepper -->
                  <div class="bg-[#09090b] border border-[rgba(255,255,255,0.08)] rounded-[2px] px-2.5 py-1 flex items-center gap-3 shrink-0">
                    <button
                      @click="decrementQty('stencil')"
                      class="text-[#a39e93] hover:text-[#f2ebd9] font-bold text-sm cursor-pointer transition-colors active:scale-90"
                    >
                      –
                    </button>
                    <span class="font-jetbrains text-[13px] font-bold text-[#f2ebd9] min-w-[14px] text-center">
                      {{ quantities.stencil }}
                    </span>
                    <button
                      @click="incrementQty('stencil')"
                      class="text-[#a39e93] hover:text-[#f2ebd9] font-bold text-sm cursor-pointer transition-colors active:scale-90"
                    >
                      +
                    </button>
                  </div>
                </div>
              </ion-col>

              <!-- Item 4: Tiras Indicadoras Biológicas -->
              <ion-col size="12" size-md="6" class="ion-no-padding mb-2.5 md:pl-2">
                <div class="bg-[#141416] border border-[rgba(255,255,255,0.06)] rounded-[2px] p-3 shadow-lg flex items-center justify-between h-full">
                  <div class="flex items-center gap-3">
                    <div class="w-12 h-12 bg-[#09090b] rounded-[2px] border border-[rgba(201,168,106,0.3)] flex items-center justify-center text-[#e6c383] shrink-0 shadow-md">
                      <BadgeCheck class="w-6 h-6 stroke-[1.8]" />
                    </div>
                    <div>
                      <span class="font-inter text-[9px] font-bold text-[#a39e93] tracking-[1.2px] uppercase block">
                        ESTERILIZAÇÃO CRISTÓFOLI
                      </span>
                      <h3 class="font-playfair text-[15px] font-semibold text-[#f2ebd9] leading-tight">
                        Tiras Indicadoras Biológicas
                      </h3>
                      <span class="font-inter text-[9px] font-bold text-[#a39e93] tracking-[1.2px] uppercase mt-0.5 block">
                        CICLO #891 • {{ itemTirasBio?.currentStock ?? 48 }} FITAS RESTANTES
                      </span>
                    </div>
                  </div>

                  <!-- Quantity Stepper -->
                  <div class="bg-[#09090b] border border-[rgba(255,255,255,0.08)] rounded-[2px] px-2.5 py-1 flex items-center gap-3 shrink-0">
                    <button
                      @click="decrementQty('tiras')"
                      class="text-[#a39e93] hover:text-[#f2ebd9] font-bold text-sm cursor-pointer transition-colors active:scale-90"
                    >
                      –
                    </button>
                    <span class="font-jetbrains text-[13px] font-bold text-[#f2ebd9] min-w-[20px] text-center">
                      {{ quantities.tiras }}
                    </span>
                    <button
                      @click="incrementQty('tiras')"
                      class="text-[#a39e93] hover:text-[#f2ebd9] font-bold text-sm cursor-pointer transition-colors active:scale-90"
                    >
                      +
                    </button>
                  </div>
                </div>
              </ion-col>
            </ion-row>
          </ion-grid>
        </div>

        <!-- Primary Action CTA Button: Cadastrar Insumos (Mobile 100%, Tablet contido e centralizado) -->
        <ion-grid class="ion-no-padding mt-2 mb-6">
          <ion-row class="ion-justify-content-center">
            <ion-col size="12" size-md="6" size-lg="5" class="ion-no-padding">
              <button
                @click="cadastrarInsumos"
                class="w-full bg-[#c9a86a] hover:bg-[#d6b77e] text-[#09090b] font-inter text-[11px] font-bold tracking-[1.44px] uppercase py-3.5 px-4 rounded-[2px] shadow-xl flex items-center justify-center gap-2 transition-all cursor-pointer active:scale-[0.99]"
              >
                <FileText class="w-4 h-4 stroke-[2.2]" />
                <span>CADASTRAR INSUMOS</span>
              </button>
            </ion-col>
          </ion-row>
        </ion-grid>
      </div>

      <!-- Bottom Navigation Bar -->

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
  Syringe,
  Pipette,
  BadgeCheck,
  FileText,
  ClipboardList,
  Layers,
  ScrollText,
} from '@lucide/vue';
import { useRouter } from 'vue-router';
import AppHeader from '../components/AppHeader.vue';
import { useInventoryStore } from '@/stores/inventory';

const router = useRouter();
const inventoryStore = useInventoryStore();

onMounted(async () => {
  // Sincroniza com backend caso haja endpoint ativo, com fallback mock transparente
  await inventoryStore.fetchStock();
});

// Computed properties vinculadas à inventoryStore compartilhada
const itemCheyenne03 = computed(() => inventoryStore.getItemById('cartuchos-03rl'));
const itemDynamicBlack = computed(() => inventoryStore.getItemById('tinta-dynamic-black'));
const itemMagnum07 = computed(() => inventoryStore.getItemById('cartuchos-07mg'));
const itemLuvasM = computed(() => inventoryStore.getItemById('luvas-m'));
const itemStencilStuff = computed(() => inventoryStore.getItemById('stencil-stuff'));
const itemTirasBio = computed(() => inventoryStore.getItemById('tiras-biologicas'));

// Section state
const activeSection = ref<'insumos' | 'repasses'>('insumos');

// Inventory item image URLs
const itemImages = {
  needles:
    'https://images.unsplash.com/photo-1598371839696-5c5bb00bdc28?auto=format&fit=crop&w=200&q=80',
  gloves:
    'https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?auto=format&fit=crop&w=200&q=80',
  stencil:
    'https://images.unsplash.com/photo-1590283603385-17ffb3a7f29f?auto=format&fit=crop&w=200&q=80',
};

// Quantities state
const quantities = ref({
  cartuchos: 1,
  luvas: 1,
  stencil: 1,
  tiras: 48,
});

function incrementQty(key: keyof typeof quantities.value) {
  quantities.value[key]++;
}

function decrementQty(key: keyof typeof quantities.value) {
  if (quantities.value[key] > 0) {
    quantities.value[key]--;
  }
}

function cadastrarInsumos() {
  router.push('/cadastrar-insumo');
}

// Navigation Tabs
const navItems = [
  { id: 'triagem', label: 'TRIAGEM', icon: ClipboardList, path: '/triagem' },
  { id: 'midia', label: 'MÍDIA & IA', icon: Layers, path: '/performance-artistas' },
  { id: 'termo', label: 'TERMO', icon: ScrollText, path: '/termo' },
  { id: 'gestao', label: 'GESTÃO', icon: Wallet, path: '/fechamento' },
];

function navigate(item: typeof navItems[0]) {
  if (item.path) {
    router.push(item.path).catch(() => {});
  }
}
</script>
