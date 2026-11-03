<template>
  <ion-page>
    <ion-content class="bg-[#09090b] text-[#f2ebd9] font-sans" fullscreen>
      <div class="screen-container min-h-screen pb-10 pt-3">
        <!-- Compact Session Header (Back button, More menu, Studio logo) -->
        <header class="flex items-center justify-between py-2 mb-3">
          <button
            @click="handleBack"
            class="w-9 h-9 bg-[#141416] hover:bg-[#1e1d21] border border-[rgba(255,255,255,0.08)] rounded-[2px] flex items-center justify-center text-[#a39e93] hover:text-[#f2ebd9] transition-colors cursor-pointer"
            title="Voltar para Agendamento"
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
              <span class="font-jetbrains text-[10px] font-bold tracking-[1.4px] uppercase">
                ETAPA 2 DE 2 • ALOCAÇÃO DE MATERIAIS
              </span>
            </div>
            <span class="font-jetbrains text-[11px] font-bold text-[#c9a86a] tracking-wider uppercase">
              100% CONCLUÍDO
            </span>
          </div>

          <!-- 100% Filled Progress Bar -->
          <div class="w-full h-1 bg-[#c9a86a] rounded-full shadow-[0_0_8px_rgba(201,168,106,0.4)]"></div>
        </div>

        <!-- SECTION 1: CLIENTE & LOCAL DE EXECUÇÃO CARD -->
        <div class="bg-[#141416] border border-[rgba(255,255,255,0.07)] rounded-[4px] p-3.5 mb-4 shadow-lg">
          <!-- Card Header: Client Name & Station Badge -->
          <div class="flex items-start justify-between mb-2">
            <div>
              <div class="font-jetbrains text-[8.5px] font-bold tracking-[1.2px] text-[#8e8a82] uppercase">
                CLIENTE &amp; LOCAL DE EXECUÇÃO
              </div>
              <h2 class="font-playfair text-[20px] font-semibold text-[#f2ebd9] leading-tight mt-0.5">
                {{ client.name }}
              </h2>
            </div>
            <!-- Station Badge -->
            <div class="bg-[#2d2417] text-[#d6b77e] border border-[rgba(201,168,106,0.3)] font-jetbrains text-[9px] font-bold px-2.5 py-1 rounded-[2px] tracking-wider uppercase shrink-0">
              {{ client.station || 'BANCADA 01' }}
            </div>
          </div>

          <!-- Bottom Row: Date/Time & Project -->
          <div class="grid grid-cols-2 gap-2 pt-2.5 mt-2 border-t border-[rgba(255,255,255,0.05)]">
            <!-- Date & Time -->
            <div>
              <div class="flex items-center gap-1 text-[#8e8a82] font-jetbrains text-[8px] font-bold tracking-[1px] uppercase">
                <Calendar class="w-3.5 h-3.5 text-[#c9a86a]" />
                <span>DATA &amp; HORA</span>
              </div>
              <div class="font-inter text-[12px] text-[#f2ebd9] mt-0.5 font-medium truncate">
                {{ client.scheduledSlot ? (client.scheduledSlot.dayLabel + ' • ' + client.scheduledSlot.time.split(' — ')[0]) : 'Qua, 29/Mai • 14:30' }}
              </div>
            </div>

            <!-- Designated Project -->
            <div>
              <div class="flex items-center gap-1 text-[#8e8a82] font-jetbrains text-[8px] font-bold tracking-[1px] uppercase">
                <PenTool class="w-3.5 h-3.5 text-[#c9a86a]" />
                <span>OBRA DESIGNADA</span>
              </div>
              <div class="font-inter text-[12px] text-[#f2ebd9] mt-0.5 font-medium truncate">
                {{ client.projectTitle }}
              </div>
            </div>
          </div>
        </div>

        <!-- SECTION 2: KIT CIRÚRGICO DE BANCADA -->
        <div class="mb-4">
          <!-- Section Title Row -->
          <div class="flex items-center justify-between mb-2.5 px-0.5">
            <div class="flex items-center gap-2">
              <svg class="w-4.5 h-4.5 text-[#c9a86a] shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M19 14V6a2 2 0 0 0-2-2H7a2 2 0 0 0-2 2v8a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2Z" />
                <path d="M10 2v4" />
                <path d="M14 2v4" />
                <path d="M12 8v4" />
                <path d="M10 10h4" />
              </svg>
              <h3 class="font-playfair text-[18px] sm:text-[19px] font-semibold text-[#f2ebd9]">
                Kit Cirúrgico de Bancada
              </h3>
            </div>
            <span class="font-jetbrains text-[10px] font-bold tracking-[1.2px] text-[#c9a86a] uppercase">
              {{ kitItems.length }} ITENS
            </span>
          </div>

          <!-- List of Surgical Kit Items -->
          <div class="space-y-2">
            <div
              v-for="item in kitItems"
              :key="item.id"
              class="bg-[#141416] border border-[rgba(255,255,255,0.07)] rounded-[4px] p-3 shadow-sm"
            >
              <!-- Top Row: Name, Subtitle and Quantity Stepper -->
              <div class="flex items-start justify-between gap-2">
                <div class="min-w-0 flex-1">
                  <div class="font-inter text-[13px] font-semibold text-[#f2ebd9] leading-snug">
                    {{ item.title }}
                  </div>
                  <div class="font-inter text-[10.5px] text-[#8e8a82] mt-0.5">
                    {{ item.subtitle }}
                  </div>
                </div>

                <!-- Stepper Counter com Proteção Reativa contra Valores Negativos e Excesso -->
                <div class="bg-[#09090b] border border-[rgba(255,255,255,0.08)] rounded-[2px] px-2.5 py-1 flex items-center gap-3 shrink-0">
                  <button
                    @click="decrementQty(item)"
                    :disabled="item.quantity <= 1"
                    :class="[
                      'font-bold text-sm transition-all',
                      item.quantity <= 1
                        ? 'text-[#4a4742] opacity-35 cursor-not-allowed'
                        : 'text-[#8e8a82] hover:text-[#f2ebd9] cursor-pointer active:scale-90'
                    ]"
                    title="Diminuir"
                  >
                    –
                  </button>
                  <span class="font-jetbrains text-[13px] font-bold text-[#f2ebd9] min-w-[16px] text-center">
                    {{ item.quantity }}
                  </span>
                  <button
                    @click="incrementQty(item)"
                    :disabled="item.maxStock !== undefined && item.quantity >= item.maxStock"
                    :class="[
                      'font-bold text-sm transition-all',
                      item.maxStock !== undefined && item.quantity >= item.maxStock
                        ? 'text-[#4a4742] opacity-35 cursor-not-allowed'
                        : 'text-[#8e8a82] hover:text-[#f2ebd9] cursor-pointer active:scale-90'
                    ]"
                    title="Aumentar"
                  >
                    +
                  </button>
                </div>
              </div>

              <!-- Bottom Row: Warehouse availability -->
              <div class="flex items-center justify-between pt-2 mt-2 border-t border-[rgba(255,255,255,0.05)]">
                <span class="font-jetbrains text-[8.5px] font-bold tracking-[1px] text-[#8e8a82] uppercase">
                  ALMOXARIFADO GERAL
                </span>
                <span class="font-jetbrains text-[11px] text-[#f2ebd9] font-medium">
                  {{ item.stock }}
                </span>
              </div>
            </div>
          </div>

          <!-- Add Extra Item CTA Button -->
          <button
            @click="openAddModal"
            class="w-full bg-[#141416] hover:bg-[#1c1a17] border border-[rgba(255,255,255,0.08)] hover:border-[rgba(201,168,106,0.3)] py-3 px-4 rounded-[2px] flex items-center justify-center gap-2 my-3 transition-all cursor-pointer active:scale-[0.99]"
          >
            <PlusCircle class="w-4 h-4 text-[#c9a86a]" />
            <span class="font-jetbrains text-[9.5px] font-bold text-[#e6c383] tracking-[1.4px] uppercase">
              + ADICIONAR INSUMO EXTRA AO KIT
            </span>
          </button>
        </div>

        <!-- ACTION CTAS -->
        <div class="space-y-2.5 mb-2">
          <!-- Primary CTA Button: Confirmar Horário & Reservar Kit -->
          <button
            @click="confirmarReserva"
            :disabled="isReserving"
            class="w-full bg-[#c9a86a] hover:bg-[#d6b77e] disabled:opacity-50 text-[#09090b] font-inter text-[11.5px] font-bold tracking-[1.4px] uppercase py-3.5 px-4 rounded-[2px] shadow-xl flex items-center justify-center gap-2 cursor-pointer transition-all active:scale-[0.99]"
          >
            <span v-if="isReserving">RESERVANDO KIT & SINCRONIZANDO ESTOQUE...</span>
            <span v-else>CONFIRMAR HORÁRIO &amp; RESERVAR KIT</span>
            <ArrowRight v-if="!isReserving" class="w-4 h-4 text-[#09090b] stroke-[2.2]" />
          </button>

          <!-- Secondary CTA Button: Enviar Guia via WhatsApp -->
          <button
            @click="enviarGuiaWhatsApp"
            class="w-full bg-[#141416] hover:bg-[#1a1917] border border-[rgba(201,168,106,0.3)] hover:border-[#c9a86a] text-[#f2ebd9] font-inter text-[10.5px] font-bold tracking-[1.2px] uppercase py-3 px-4 rounded-[2px] flex items-center justify-center gap-2 cursor-pointer transition-all active:scale-[0.99]"
          >
            <MessageSquare class="w-4 h-4 text-[#c9a86a]" />
            <span>ENVIAR GUIA DE CUIDADOS VIA WHATSAPP</span>
          </button>

          <!-- Tertiary Back Link -->
          <button
            @click="handleBack"
            class="w-full flex items-center justify-center gap-1.5 text-[#8e8a82] hover:text-[#f2ebd9] font-jetbrains text-[9.5px] font-bold tracking-[1.2px] uppercase py-2 cursor-pointer transition-colors"
          >
            <ArrowLeft class="w-3 h-3" />
            <span>{{ route.query.from === 'agenda' ? 'VOLTAR PARA AGENDA' : 'VOLTAR PARA GRADE DE HORÁRIOS' }}</span>
          </button>
        </div>
      </div>

      <!-- SUCCESS MODAL: KIT & HORÁRIO CONFIRMADOS -->
      <div
        v-if="showSuccessModal"
        class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md transition-opacity"
        @click.self="showSuccessModal = false"
      >
        <div class="bg-[#141416] border border-[rgba(201,168,106,0.4)] rounded-[4px] max-w-sm w-full p-4 shadow-2xl relative">
          <div class="flex items-center justify-between pb-3 border-b border-[rgba(255,255,255,0.06)] mb-3">
            <div class="flex items-center gap-2">
              <CheckCircle2 class="w-5 h-5 text-[#86efac]" />
              <h3 class="font-playfair text-[18px] font-semibold text-[#f2ebd9]">
                Kit &amp; Agenda Confirmados
              </h3>
            </div>
            <button
              @click="showSuccessModal = false"
              class="text-[#a39e93] hover:text-[#f2ebd9] p-1 cursor-pointer"
            >
              <X class="w-4 h-4" />
            </button>
          </div>

          <div class="bg-[#09090b] p-3 rounded-[2px] border border-[rgba(255,255,255,0.05)] text-[12px] space-y-2 mb-4">
            <div class="text-[#c9a86a] font-jetbrains text-[10.5px] uppercase font-bold tracking-wider">
              RESERVA {{ client.station || 'BANCADA 01' }} · {{ client.assignedArtist }}
            </div>
            <p class="text-[#f2ebd9] font-inter text-[12px] leading-relaxed">
              Agendamento de <strong>{{ client.name }}</strong> concluído para <strong>{{ client.scheduledSlot ? (client.scheduledSlot.dayLabel + ' às ' + client.scheduledSlot.time.split(' — ')[0]) : 'Qua, 29/Mai às 14:30' }}</strong>. Todos os {{ kitItems.length }} insumos foram alocados.
            </p>
            <div class="text-[#a39e93] font-jetbrains text-[10px] space-y-1 pt-1.5 border-t border-[rgba(255,255,255,0.05)]">
              <div>LOTE NOTARIAL: {{ client.lotId }}</div>
              <div>PROTOCOLO DE BAIXA: {{ reservationProtocol }}</div>
              <div class="flex items-center gap-1.5 pt-0.5">
                <span class="w-1.5 h-1.5 rounded-full" :class="reservationOrigin === 'backend' ? 'bg-[#86efac]' : 'bg-[#e6c383]'"></span>
                <span :class="reservationOrigin === 'backend' ? 'text-[#86efac] font-medium' : 'text-[#e6c383] font-medium'">
                  {{ reservationOrigin === 'backend' ? 'SINCRONIZADO VIA BACKEND' : 'STATUS: BANCADA PREPARADA (BAIXA MOCK REATIVA)' }}
                </span>
              </div>
            </div>
          </div>

          <div class="flex gap-2">
            <button
              @click="irParaAgenda"
              class="flex-1 py-2.5 bg-[#1a1917] border border-[rgba(255,255,255,0.1)] text-[#a39e93] font-inter text-[10.5px] font-bold uppercase rounded-[2px] cursor-pointer hover:text-[#f2ebd9]"
            >
              VER AGENDA
            </button>
            <button
              @click="irParaTermo"
              class="flex-1 py-2.5 bg-[#c9a86a] text-[#09090b] font-inter text-[10.5px] font-bold tracking-[1px] uppercase rounded-[2px] cursor-pointer hover:bg-[#d6b77e]"
            >
              AVANÇAR PARA TERMO
            </button>
          </div>
        </div>
      </div>

      <!-- MODAL: ADICIONAR INSUMO EXTRA -->
      <div
        v-if="showAddModal"
        class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md transition-opacity"
        @click.self="showAddModal = false"
      >
        <div class="bg-[#141416] border border-[rgba(201,168,106,0.4)] rounded-[4px] max-w-sm w-full p-4 shadow-2xl relative">
          <div class="flex items-center justify-between pb-3 border-b border-[rgba(255,255,255,0.06)] mb-3">
            <div class="flex items-center gap-2">
              <PlusCircle class="w-5 h-5 text-[#c9a86a]" />
              <h3 class="font-playfair text-[18px] font-semibold text-[#f2ebd9]">
                Adicionar Insumo Extra
              </h3>
            </div>
            <button
              @click="showAddModal = false"
              class="text-[#a39e93] hover:text-[#f2ebd9] p-1 cursor-pointer"
            >
              <X class="w-4 h-4" />
            </button>
          </div>

          <div class="space-y-2 mb-4">
            <button
              v-for="extra in extraItemsPool"
              :key="extra.title"
              @click="addExtraItem(extra)"
              class="w-full text-left bg-[#09090b] hover:bg-[#1a1917] border border-[rgba(255,255,255,0.06)] rounded-[2px] p-2.5 flex items-center justify-between cursor-pointer transition-colors"
            >
              <div>
                <div class="font-inter text-[12px] font-semibold text-[#f2ebd9]">{{ extra.title }}</div>
                <div class="font-inter text-[10px] text-[#8e8a82]">{{ extra.subtitle }}</div>
              </div>
              <span class="font-jetbrains text-[10px] text-[#c9a86a] font-bold">+ ADICIONAR</span>
            </button>
          </div>

          <button
            @click="showAddModal = false"
            class="w-full py-2 bg-[#1a1917] text-[#a39e93] font-inter text-[10.5px] font-bold uppercase rounded-[2px] cursor-pointer"
          >
            FECHAR
          </button>
        </div>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { IonPage, IonContent } from '@ionic/vue';
import { useRouter, useRoute } from 'vue-router';
import {
  ArrowLeft,
  MoreVertical,
  Calendar,
  PenTool,
  PlusCircle,
  ArrowRight,
  MessageSquare,
  CheckCircle2,
  X
} from '@lucide/vue';
import { useTriageStore } from '@/stores/triage';
import { useInventoryStore } from '@/stores/inventory';

const router = useRouter();
const route = useRoute();
const triageStore = useTriageStore();
const inventoryStore = useInventoryStore();

// Contexto da sessão recebido via query ou padrão
const sessionId = computed(() => {
  return (route.query.sessionId as string) || 'sess-02';
});

// Dynamic client from Route Query or Store
const clientId = computed(() => {
  return (route.query.clientId as string) || triageStore.selectedClientId || 'cli-beatriz';
});

const client = computed(() => {
  return triageStore.getClientById(clientId.value);
});

// Studio logo URL
const logoUrl = 'https://s3-alpha-sig.figma.com/img/3c73/8bb6/09078c4aab5df9f0c9aa2e5b4489ac50?Expires=1792368000&Key-Pair-Id=APKAQ4GOSFWCW27IBOMQ&Signature=SCdgUzfuAfo61MxW3nRPxZOcJqBEhQi-8pORR9hhvKMCiPgugvyqSkk5p5z~NsYx4HWlSe8d0~9xPpbGi4f0np1DwPhWBszD0Gtf2B-5-ZuCuO~lDpnfdBGO~MsVmMg7G7G~0X2qrANB-bLoTOBm7T3s7JPpz5pmaCt1bhEI3Ja4DW8T1u0TVhCJ-n~ZU3t6Hi37tnzBY1fUiCqqStql4MyyqsQSyU4~bwbhMxIZimqefof0zrJi1lvERse9cv-po2bpTcfnoftzt0aTLVsIoMurlP4ZphisFdZ2MFrErItliBLSAQkiGd7jHKFImnqew8JRbdJ2AI5CYU3ebuHZZQ__';

// Kit Items State com limite de estoque auditável
export interface KitItem {
  id: string;
  title: string;
  subtitle: string;
  quantity: number;
  stock: string;
  maxStock: number;
}

const kitItems = ref<KitItem[]>([
  {
    id: 'agulha-03rl',
    title: 'Cartucho Agulha 03RL (Chiaroscuro)',
    subtitle: 'Lote Esterilizado 2027',
    quantity: 2,
    stock: '38 un. disponíveis',
    maxStock: 38
  },
  {
    id: 'carbon-black',
    title: 'Pigmento Carbon Black Puro (#882)',
    subtitle: 'Frasco Notarial Lacrado 30ml',
    quantity: 1,
    stock: '14 un. disponíveis',
    maxStock: 14
  },
  {
    id: 'luvas-m',
    title: 'Luvas Nitrílicas Pretas (Tam. M)',
    subtitle: 'Grau Médico Isento de Pó',
    quantity: 2,
    stock: '180 pares disponíveis',
    maxStock: 180
  },
  {
    id: 'stencil',
    title: 'Papel Stencil Hectográfico Especial',
    subtitle: 'Gramatura Alta 4 Folhas Archival',
    quantity: 3,
    stock: '92 fl. disponíveis',
    maxStock: 92
  },
  {
    id: 'filme-dermico',
    title: 'Filme Curativo Dérmico Protetor',
    subtitle: 'Rolo 15cm x 10m (1 Fracionamento)',
    quantity: 1,
    stock: '12 rolos disponíveis',
    maxStock: 12
  },
  {
    id: 'vaselina',
    title: 'Vaselina Dermocosmética Notarial',
    subtitle: 'Monodose Estéril 30g',
    quantity: 1,
    stock: '50 un. disponíveis',
    maxStock: 50
  }
]);

const extraItemsPool = [
  { id: 'biqueira-grip', title: 'Biqueira Descartável Grip 25mm', subtitle: 'Esterilizada ETO', stock: '64 un. disponíveis', maxStock: 64 },
  { id: 'espuma-calmante', title: 'Espuma Calmante Epitelial 150ml', subtitle: 'Extrato de Camomila & Hamamélis', stock: '22 frascos disponíveis', maxStock: 22 },
  { id: 'alcool-spray', title: 'Álcool Isopropílico 70% Spray', subtitle: 'Assepsia de Bancada', stock: '40 un. disponíveis', maxStock: 40 },
];

const showSuccessModal = ref(false);
const showAddModal = ref(false);
const isReserving = ref(false);
const reservationProtocol = ref('');
const reservationOrigin = ref<'backend' | 'mock'>('mock');

function incrementQty(item: KitItem) {
  if (item.maxStock !== undefined && item.quantity >= item.maxStock) {
    return;
  }
  item.quantity++;
}

function decrementQty(item: KitItem) {
  if (item.quantity > 1) {
    item.quantity--;
  }
}

function openAddModal() {
  showAddModal.value = true;
}

function addExtraItem(extra: typeof extraItemsPool[0]) {
  kitItems.value.push({
    id: extra.id || `extra-${Date.now()}`,
    title: extra.title,
    subtitle: extra.subtitle,
    quantity: 1,
    stock: extra.stock,
    maxStock: extra.maxStock || 50
  });
  showAddModal.value = false;
}

function handleBack() {
  if (route.query.from === 'agenda') {
    router.push('/agenda');
  } else {
    router.push({
      path: '/agendamento-sessao',
      query: { clientId: client.value.id },
    });
  }
}

function handleMenu() {
  alert(`Opções do Kit ${client.value.lotId}: Duplicar kit cirúrgico, redefinir para o padrão ou alterar almoxarifado.`);
}

async function confirmarReserva() {
  isReserving.value = true;

  try {
    // Aloca materiais com verificação condicional de backend / mock
    const result = await inventoryStore.allocateMaterials(
      sessionId.value,
      client.value.id,
      kitItems.value
    );

    reservationProtocol.value = result.protocol;
    reservationOrigin.value = result.fromBackend ? 'backend' : 'mock';
    showSuccessModal.value = true;
  } catch (err) {
    console.warn('[AlocacaoMateriais] Erro ao alocar materiais via store:', err);
    reservationProtocol.value = `BX-FLB-${Date.now().toString().slice(-6)}`;
    reservationOrigin.value = 'mock';
    showSuccessModal.value = true;
  } finally {
    isReserving.value = false;
  }
}

function enviarGuiaWhatsApp() {
  alert(`Guia Oficial de Pré e Pós-Cuidados do Atelier disparado para o WhatsApp de ${client.value.name} (${client.value.phone}).`);
}

function irParaAgenda() {
  showSuccessModal.value = false;
  router.push('/agenda');
}

function irParaTermo() {
  showSuccessModal.value = false;
  router.push({
    path: '/termo',
    query: { clientId: client.value.id },
  });
}
</script>
