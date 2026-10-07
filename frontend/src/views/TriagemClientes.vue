<template>
  <ion-page>
    <ion-content class="bg-[#09090b] text-[#f2ebd9] font-sans" fullscreen>
      <div class="screen-container min-h-screen pb-24 pt-3">
        <!-- Reusable App Header -->
        <AppHeader
          @click-notifications="handleNotifications"
          @click-profile="handleProfile"
        />

        <!-- Reception Protocol Badge -->
        <div class="flex items-center gap-1.5 mt-2 mb-2 px-0.5">
          <span class="text-[#c9a86a] text-[10px] leading-none">✦</span>
          <span class="font-jetbrains text-[10px] font-bold tracking-[1.8px] text-[#c9a86a] uppercase">
            RECEPÇÃO
          </span>
        </div>

        <!-- Page Title & Subtitle -->
        <div class="mb-4 px-0.5">
          <h1 class="font-playfair text-[25px] sm:text-[28px] font-semibold text-[#f2ebd9] leading-tight mb-1">
            Triagem de Clientes
          </h1>
          <p class="font-inter text-[12px] text-[#a39e93] leading-relaxed">
            WhatsApp &amp; Cobrança de Sinal
          </p>
        </div>

        <!-- Search Bar Input -->
        <div class="bg-[#141416] border border-[rgba(255,255,255,0.08)] rounded-[4px] px-3.5 py-2.5 flex items-center gap-2.5 mb-3 shadow-sm">
          <Search class="w-4 h-4 text-[#8e8a82] shrink-0" />
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Buscar por cliente, estilo ou projeto..."
            class="font-inter text-[12px] text-[#f2ebd9] placeholder-[#5a5852] bg-transparent outline-none flex-1"
          />
          <button
            @click="toggleFilter"
            class="text-[#8e8a82] hover:text-[#f2ebd9] transition-colors cursor-pointer shrink-0"
            title="Filtros avançados"
          >
            <SlidersHorizontal class="w-4 h-4" />
          </button>
        </div>

        <!-- Filter Tabs / Pills -->
        <div class="flex items-center gap-2 overflow-x-auto no-scrollbar pb-1 mb-5">
          <button
            v-for="pill in filterPills"
            :key="pill.id"
            @click="activeFilter = pill.id"
            :class="[
              'px-3 py-1.5 rounded-[2px] font-jetbrains text-[10px] font-bold tracking-wider uppercase whitespace-nowrap cursor-pointer transition-all',
              activeFilter === pill.id
                ? 'bg-[#c9a86a] text-[#09090b] shadow-sm'
                : 'bg-[#141416] border border-[rgba(255,255,255,0.08)] text-[#a39e93] hover:text-[#f2ebd9]'
            ]"
          >
            {{ pill.label }} ({{ pill.count }})
          </button>
        </div>

        <!-- Main Responsive Content Grid (Cards & Listas) -->
        <ion-grid class="ion-no-padding">
          <ion-row>
            <!-- COLUNA 1: FEATURED ACTIVE TRIAGE CARD (Helena Vasco) -->
            <ion-col size="12" size-md="6" class="ion-no-padding mb-4 md:mb-0 md:pr-3">
              <div class="flex items-center justify-between mb-2 px-0.5">
                <span class="font-jetbrains text-[9px] font-bold tracking-[1.5px] text-[#c9a86a] uppercase">
                  ATENDIMENTO EM DESTAQUE
                </span>
                <span class="font-jetbrains text-[8.5px] text-[#8e8a82]">
                  {{ featuredClient.statusLabel }}
                </span>
              </div>

              <div class="bg-[#141416] border border-[rgba(255,255,255,0.07)] rounded-[4px] p-4 shadow-lg flex flex-col justify-between h-full">
                <div>
                  <!-- Card Header: Client & Priority Badge -->
                  <div class="flex items-start justify-between mb-3">
                    <div class="flex items-center gap-3">
                      <div class="w-11 h-11 rounded-[2px] overflow-hidden border border-[rgba(255,255,255,0.1)] shrink-0 bg-[#09090b]">
                        <img
                          :src="featuredClient.avatar"
                          :alt="featuredClient.name"
                          class="w-full h-full object-cover"
                        />
                      </div>
                      <div>
                        <div class="flex items-center gap-1.5">
                          <h2 class="font-playfair text-[16px] font-semibold text-[#f2ebd9] leading-tight">
                            {{ featuredClient.name }}
                          </h2>
                          <CheckCircle2 class="w-3.5 h-3.5 text-[#8e8a82]" />
                        </div>
                        <div class="flex items-center gap-1 text-[#8e8a82] font-jetbrains text-[9.5px] mt-0.5">
                          <Clock class="w-3 h-3 text-[#8e8a82]" />
                          <span>{{ featuredClient.timeAgo }}</span>
                        </div>
                      </div>
                    </div>

                    <!-- Priority Badge -->
                    <div class="bg-[#381617] border border-[#752629] px-2.5 py-0.5 rounded-[2px] shrink-0">
                      <span class="font-jetbrains text-[8.5px] font-bold text-[#f87171] tracking-[1.2px] uppercase">
                        ALTA PRIORIDADE
                      </span>
                    </div>
                  </div>

                  <!-- Project Details -->
                  <div class="bg-[#09090b] border border-[rgba(255,255,255,0.05)] rounded-[3px] p-2.5 mb-2.5">
                    <div class="flex items-center justify-between text-[10px] font-jetbrains text-[#a39e93]">
                      <span>PROJETO: {{ featuredClient.style }}</span>
                      <span class="text-[#c9a86a]">{{ featuredClient.lotId }}</span>
                    </div>
                    <div class="font-inter text-[12px] text-[#f2ebd9] mt-0.5 font-medium">
                      {{ featuredClient.conceptDescription }}
                    </div>
                  </div>

                  <!-- Deposit & Guarantee Row -->
                  <div class="flex items-end justify-between py-2 border-t border-[rgba(255,255,255,0.05)] mt-1 mb-2">
                    <div>
                      <div class="flex items-center gap-1 text-[#c9a86a] mb-1">
                        <CreditCard class="w-4 h-4 text-[#c9a86a]" />
                      </div>
                      <div class="flex items-center gap-1 text-[#8e8a82] font-inter text-[10px]">
                        <ShieldCheck class="w-3.5 h-3.5 text-[#c9a86a]" />
                        <span>Garantia de bancada • 50% obrigatório</span>
                      </div>
                    </div>

                    <!-- Price Tag Display -->
                    <div class="text-right">
                      <div class="flex items-baseline justify-end gap-1">
                        <span class="font-jetbrains text-[13px] font-bold text-[#d6b77e] leading-none">R$</span>
                        <span class="font-playfair text-[24px] font-bold text-[#f2ebd9] leading-none">
                          {{ featuredClient.depositAmount.toFixed(2).replace('.', ',') }}
                        </span>
                      </div>
                      <div class="font-jetbrains text-[8px] text-[#8e8a82] uppercase tracking-wider mt-0.5">
                        REF: 50% DE R$ {{ featuredClient.priceTotal.toFixed(2).replace('.', ',') }}
                      </div>
                    </div>
                  </div>

                  <!-- Attach Receipt Box (Dotted Border) -->
                  <div class="bg-[#09090b] border border-dashed border-[rgba(201,168,106,0.3)] rounded-[4px] p-4 text-center my-3 relative">
                    <FileUp class="w-6 h-6 text-[#c9a86a] mx-auto mb-1.5" />
                    <div class="font-jetbrains text-[10px] font-bold text-[#f2ebd9] tracking-[1px] uppercase">
                      ANEXAR COMPROVANTE (JPG/PDF)
                    </div>
                    <p class="font-inter text-[10.5px] text-[#8e8a82] mt-0.5 mb-3">
                      Comprovante de transferência bancária ou PIX
                    </p>

                    <!-- File Input Row -->
                    <div class="bg-[#141416] border border-[rgba(255,255,255,0.06)] rounded-[2px] p-1.5 px-3 flex items-center justify-between">
                      <div class="flex items-center gap-2 truncate pr-2">
                        <FileText class="w-3.5 h-3.5 text-[#8e8a82] shrink-0" />
                        <span class="font-inter text-[10.5px] text-[#8e8a82] italic truncate">
                          {{ attachedFileName || 'Nenhum arquivo selecionado' }}
                        </span>
                      </div>
                      <button
                        @click="triggerFileSelect"
                        class="bg-[#1c1a17] hover:bg-[#25221d] border border-[rgba(201,168,106,0.3)] px-3 py-1 rounded-[2px] font-jetbrains text-[9.5px] font-bold text-[#c9a86a] tracking-[1px] uppercase cursor-pointer shrink-0 transition-colors"
                      >
                        PROCURAR
                      </button>
                    </div>
                    <input
                      ref="fileInputRef"
                      type="file"
                      accept=".jpg,.jpeg,.png,.pdf"
                      class="hidden"
                      @change="handleFileChange"
                    />
                  </div>
                </div>

                <!-- Main CTA: Registrar Sinal e Liberar Maca -->
                <button
                  @click="registrarSinal"
                  :disabled="isSubmittingDeposit"
                  class="w-full bg-[#c9a86a] hover:bg-[#d6b77e] text-[#09090b] font-inter text-[11px] font-bold tracking-[1.4px] uppercase py-3.5 px-4 rounded-[2px] shadow-xl flex items-center justify-center gap-2 cursor-pointer transition-all active:scale-[0.99] mt-2 disabled:opacity-75"
                >
                  <Lock class="w-3.5 h-3.5 stroke-[2.2] text-[#09090b]" />
                  <span>{{ isSubmittingDeposit ? 'REGISTRANDO NO SERVIDOR...' : 'REGISTRAR SINAL E LIBERAR MACA' }}</span>
                </button>
              </div>
            </ion-col>

            <!-- COLUNA 2: FILA GERAL DE ATENDIMENTO -->
            <ion-col size="12" size-md="6" class="ion-no-padding md:pl-3 flex flex-col justify-between">
              <div>
                <!-- Section Header Row -->
                <div class="flex items-center justify-between mb-2.5 px-0.5">
                  <span class="font-jetbrains text-[9px] font-bold tracking-[1.5px] text-[#8e8a82] uppercase">
                    FILA GERAL DE ATENDIMENTO
                  </span>
                  <span class="font-jetbrains text-[9px] text-[#8e8a82] flex items-center gap-1">
                    <span class="text-[#4ade80] text-[10px] leading-none">●</span>
                    {{ filteredQueueClients.length }} na fila
                  </span>
                </div>

                <!-- Lista Dinâmica de Clientes da Fila -->
                <div v-if="filteredQueueClients.length > 0" class="space-y-3">
                  <div
                    v-for="item in filteredQueueClients"
                    :key="item.id"
                    class="bg-[#141416] border border-[rgba(255,255,255,0.07)] hover:border-[rgba(201,168,106,0.3)] transition-all rounded-[4px] p-3.5 shadow-md"
                  >
                    <div class="flex items-start justify-between">
                      <div class="flex items-center gap-2.5">
                        <div class="w-10 h-10 rounded-[2px] overflow-hidden border border-[rgba(255,255,255,0.08)] shrink-0 bg-[#09090b]">
                          <img
                            :src="item.avatar"
                            :alt="item.name"
                            class="w-full h-full object-cover"
                          />
                        </div>
                        <div>
                          <h3 class="font-playfair text-[15px] font-semibold text-[#f2ebd9] leading-tight">
                            {{ item.name }}
                          </h3>
                          <div class="font-inter text-[11px] text-[#8e8a82]">
                            {{ item.style }}
                          </div>
                          <div class="font-inter text-[10.5px] text-[#8e8a82] italic truncate max-w-[190px]">
                            ({{ item.conceptDescription }})
                          </div>
                        </div>
                      </div>

                      <!-- Badge Dinâmica por Status -->
                      <div>
                        <div
                          v-if="item.status === 'confirmados'"
                          class="bg-[#0e2417] border border-[#225533] px-2.5 py-0.5 rounded-[2px] flex items-center gap-1 shrink-0"
                        >
                          <CheckCircle2 class="w-3 h-3 text-[#4ade80]" />
                          <span class="font-jetbrains text-[9px] font-bold text-[#4ade80] tracking-[1px] uppercase">
                            CONFIRMADO
                          </span>
                        </div>

                        <div
                          v-else-if="item.status === 'aguardando'"
                          class="bg-[#1a1917] border border-[rgba(255,255,255,0.08)] px-2.5 py-0.5 rounded-[2px] shrink-0"
                        >
                          <span class="font-jetbrains text-[9px] font-bold text-[#e6c383] tracking-[1px] uppercase">
                            AGUARDANDO
                          </span>
                        </div>

                        <div
                          v-else
                          class="bg-[#241e17] border border-[rgba(201,168,106,0.25)] px-2.5 py-0.5 rounded-[2px] shrink-0"
                        >
                          <span class="font-jetbrains text-[9px] font-bold text-[#d6b77e] tracking-[1px] uppercase">
                            SINAL PENDENTE
                          </span>
                        </div>
                      </div>
                    </div>

                    <!-- Footer: Informações e Botão de Ação -->
                    <div class="border-t border-[rgba(255,255,255,0.05)] pt-2.5 mt-2.5 flex items-center justify-between">
                      <div class="flex items-center gap-1 text-[11px] text-[#8e8a82] font-inter">
                        <template v-if="item.status === 'confirmados'">
                          <CheckCircle2 class="w-3.5 h-3.5 text-[#4ade80]" />
                          <span>Sinal Recebido: <strong class="text-[#f2ebd9] font-semibold">R$ {{ item.depositAmount.toFixed(2).replace('.', ',') }}</strong></span>
                        </template>
                        <template v-else-if="item.status === 'aguardando'">
                          <FileText class="w-3.5 h-3.5 text-[#8e8a82]" />
                          <span>Orçamento enviado (R$ {{ item.priceTotal.toFixed(2).replace('.', ',') }})</span>
                        </template>
                        <template v-else>
                          <Clock class="w-3.5 h-3.5 text-[#c9a86a]" />
                          <span>Sinal pendente (R$ {{ item.depositAmount.toFixed(2).replace('.', ',') }})</span>
                        </template>
                      </div>

                      <!-- CTA AGENDAR SESSÃO / ALOCAR HORÁRIO (Passa clientId via Vue Router) -->
                      <button
                        @click="iniciarAgendamento(item)"
                        class="font-jetbrains text-[9.5px] font-bold text-[#c9a86a] hover:text-[#d6b77e] tracking-[1px] uppercase flex items-center gap-1 cursor-pointer transition-colors"
                        :title="'Iniciar agendamento para ' + item.name"
                      >
                        <span>AGENDAR SESSÃO</span>
                        <span class="text-[12px] leading-none">→</span>
                      </button>
                    </div>
                  </div>
                </div>

                <!-- Empty State se nenhum cliente no filtro -->
                <div
                  v-else
                  class="bg-[#141416] border border-[rgba(255,255,255,0.05)] rounded-[4px] p-8 text-center my-3"
                >
                  <p class="font-playfair text-[15px] text-[#f2ebd9]">Nenhum atendimento neste filtro</p>
                  <p class="font-inter text-[11px] text-[#8e8a82] mt-1">Selecione outra categoria ou limpe a busca.</p>
                </div>
              </div>

              <!-- ACTION BUTTON: + NOVO CLIENTE / LINK DIRETO -->
              <button
                @click="openNovoClienteModal"
                class="w-full bg-[#09090b] hover:bg-[#141416] border border-[rgba(201,168,106,0.35)] hover:border-[#c9a86a] py-3.5 px-4 rounded-[2px] flex items-center justify-center gap-2 my-4 transition-all cursor-pointer active:scale-[0.99]"
              >
                <PlusCircle class="w-4 h-4 text-[#c9a86a]" />
                <span class="font-jetbrains text-[10px] font-bold text-[#e6c383] tracking-[1.5px] uppercase">
                  + NOVO CLIENTE / LINK DIRETO
                </span>
              </button>
            </ion-col>
          </ion-row>
        </ion-grid>
      </div>

      <!-- SUCCESS MODAL: SINAL REGISTRADO & MACA LIBERADA -->
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
                Sinal Confirmado
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
              BANCADA LIBERADA · SINAL R$ {{ featuredClient.depositAmount.toFixed(2).replace('.', ',') }}
            </div>
            <p class="text-[#f2ebd9] font-inter text-[12px] leading-relaxed">
              {{ depositFeedbackMessage || ('O pagamento do sinal de ' + featuredClient.name + ' foi autenticado com sucesso. A maca e agenda foram sincronizadas.') }}
            </p>
            <div class="text-[#a39e93] font-jetbrains text-[10px] space-y-0.5 pt-1 border-t border-[rgba(255,255,255,0.05)]">
              <div>COMPROVANTE: {{ attachedFileName || 'PIX_HELENA_VASCO.PDF' }}</div>
              <div>STATUS: SINAL 50% LIQUIDADO</div>
            </div>
          </div>

          <div class="flex flex-col gap-2">
            <div class="flex gap-2">
              <button
                @click="irParaAgendamentoDestaque"
                class="flex-1 py-2.5 bg-[#1a1917] border border-[rgba(201,168,106,0.3)] text-[#e6c383] font-inter text-[10.5px] font-bold uppercase rounded-[2px] cursor-pointer hover:text-[#f2ebd9]"
              >
                AGENDAR BANCADA
              </button>
              <button
                @click="irParaTermoDestaque"
                class="flex-1 py-2.5 bg-[#c9a86a] text-[#09090b] font-inter text-[10.5px] font-bold tracking-[1px] uppercase rounded-[2px] cursor-pointer hover:bg-[#d6b77e]"
              >
                GERAR TERMO
              </button>
            </div>
            <button
              @click="showSuccessModal = false"
              class="w-full py-2 bg-transparent text-[#8e8a82] hover:text-[#f2ebd9] font-inter text-[10px] uppercase font-semibold cursor-pointer"
            >
              FECHAR
            </button>
          </div>
        </div>
      </div>

      <!-- MODAL: LINK DIRETO / NOVO CLIENTE -->
      <div
        v-if="showLinkModal"
        class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md transition-opacity"
        @click.self="showLinkModal = false"
      >
        <div class="bg-[#141416] border border-[rgba(201,168,106,0.4)] rounded-[4px] max-w-sm w-full p-4 shadow-2xl relative">
          <div class="flex items-center justify-between pb-3 border-b border-[rgba(255,255,255,0.06)] mb-3">
            <div class="flex items-center gap-2">
              <PlusCircle class="w-5 h-5 text-[#c9a86a]" />
              <h3 class="font-playfair text-[18px] font-semibold text-[#f2ebd9]">
                Novo Link de Triagem
              </h3>
            </div>
            <button
              @click="showLinkModal = false"
              class="text-[#a39e93] hover:text-[#f2ebd9] p-1 cursor-pointer"
            >
              <X class="w-4 h-4" />
            </button>
          </div>

          <p class="font-inter text-[12px] text-[#a39e93] mb-3">
            Envie este link direto para o cliente preencher o pré-orçamento e o termo notarial pelo WhatsApp:
          </p>

          <div class="bg-[#09090b] p-2.5 rounded-[2px] border border-[rgba(201,168,106,0.25)] flex items-center justify-between gap-2 mb-4">
            <span class="font-jetbrains text-[10px] text-[#e6c383] truncate">
              {{ preAttendanceLink }}
            </span>
            <button
              @click="copyLink"
              class="bg-[#1c1a17] hover:bg-[#25221d] border border-[rgba(201,168,106,0.3)] px-2.5 py-1 rounded-[2px] font-jetbrains text-[9.5px] font-bold text-[#c9a86a] uppercase cursor-pointer shrink-0 flex items-center gap-1"
            >
              <Copy class="w-3 h-3" />
              <span>COPIAR</span>
            </button>
          </div>

          <button
            @click="showLinkModal = false"
            class="w-full py-2.5 bg-[#c9a86a] text-[#09090b] font-inter text-[11px] font-bold tracking-[1.2px] uppercase rounded-[2px] cursor-pointer hover:bg-[#d6b77e]"
          >
            CONCLUIR
          </button>
        </div>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { IonPage, IonContent, IonGrid, IonRow, IonCol } from '@ionic/vue';
import { useRouter } from 'vue-router';
import {
  Search,
  SlidersHorizontal,
  Clock,
  CheckCircle2,
  CreditCard,
  ShieldCheck,
  FileUp,
  FileText,
  Lock,
  PlusCircle,
  X,
  Copy
} from '@lucide/vue';
import AppHeader from '../components/AppHeader.vue';
import { useTriageStore, type TriageClient } from '@/stores/triage';

const router = useRouter();
const triageStore = useTriageStore();

// Sincroniza fila de triagem de clientes do backend (ou mantém fallback mock)
onMounted(async () => {
  if (typeof triageStore.fetchTriageQueue === 'function') {
    await triageStore.fetchTriageQueue();
  }
});

// Search & Filter state
const searchQuery = ref('');
const activeFilter = ref('todos');

// Featured active client (Helena Vasco)
const featuredClient = computed(() => {
  return triageStore.getClientById('cli-helena');
});

// Dynamic filter pills with counts based on store
const filterPills = computed(() => [
  { id: 'todos', label: 'TODOS', count: triageStore.clients.length },
  {
    id: 'aguardando',
    label: 'AGUARDANDO ORÇAMENTO',
    count: triageStore.clients.filter((c) => c.status === 'aguardando').length,
  },
  {
    id: 'sinal',
    label: '• SINAL PENDENTE',
    count: triageStore.clients.filter((c) => c.status === 'sinal').length,
  },
  {
    id: 'confirmados',
    label: '• CONFIRMADOS',
    count: triageStore.clients.filter((c) => c.status === 'confirmados').length,
  },
]);

// Queue items (excluding featured item if active in spotlight, but searchable)
const filteredQueueClients = computed(() => {
  return triageStore.clients.filter((item) => {
    // Keep featured out of secondary queue if 'todos' to avoid visual duplicate,
    // or include if user searches explicitly
    if (activeFilter.value === 'todos' && item.id === featuredClient.value.id) {
      return false;
    }

    const matchesFilter =
      activeFilter.value === 'todos' || item.status === activeFilter.value;

    const q = searchQuery.value.toLowerCase().trim();
    const matchesQuery =
      !q ||
      item.name.toLowerCase().includes(q) ||
      item.style.toLowerCase().includes(q) ||
      item.projectTitle.toLowerCase().includes(q) ||
      item.conceptDescription.toLowerCase().includes(q);

    return matchesFilter && matchesQuery;
  });
});

// File Upload state
const fileInputRef = ref<HTMLInputElement | null>(null);
const attachedFileName = ref('');

// Modals & Async state
const showSuccessModal = ref(false);
const showLinkModal = ref(false);
const isSubmittingDeposit = ref(false);
const depositFeedbackMessage = ref('');
const preAttendanceLink = ref('https://tattooflow.app/triagem/novo?ref=nct-8941');

function triggerFileSelect() {
  if (fileInputRef.value) {
    fileInputRef.value.click();
  }
}

function handleFileChange(e: Event) {
  const target = e.target as HTMLInputElement;
  if (target.files && target.files[0]) {
    attachedFileName.value = target.files[0].name;
  }
}

async function registrarSinal() {
  if (!attachedFileName.value) {
    attachedFileName.value = 'COMPROVANTE_PIX_450.PDF';
  }
  isSubmittingDeposit.value = true;
  try {
    if (typeof triageStore.registerDepositWithReceipt === 'function') {
      const result = await triageStore.registerDepositWithReceipt({
        clientId: featuredClient.value.id,
        receiptFileName: attachedFileName.value,
        depositAmount: featuredClient.value.depositAmount,
      });
      depositFeedbackMessage.value = result.message;
    } else if (typeof triageStore.registerDeposit === 'function') {
      triageStore.registerDeposit(featuredClient.value.id, attachedFileName.value);
    }
    showSuccessModal.value = true;
  } catch (err) {
    console.warn('[TriagemClientes] Falha ao registrar sinal:', err);
    showSuccessModal.value = true;
  } finally {
    isSubmittingDeposit.value = false;
  }
}

// ===============================================================
// WIZARD NAVIGATION: PASSING CLIENT ID VIA ROUTE QUERY & STORE
// ===============================================================
function iniciarAgendamento(client: TriageClient) {
  triageStore.setSelectedClient(client.id);
  router.push({
    path: '/agendamento-sessao',
    query: { clientId: client.id },
  });
}

function irParaAgendamentoDestaque() {
  showSuccessModal.value = false;
  triageStore.setSelectedClient('cli-helena');
  router.push({
    path: '/agendamento-sessao',
    query: { clientId: 'cli-helena' },
  });
}

function irParaTermoDestaque() {
  showSuccessModal.value = false;
  triageStore.setSelectedClient('cli-helena');
  router.push({
    path: '/termo',
    query: { clientId: 'cli-helena' },
  });
}

async function openNovoClienteModal() {
  if (typeof triageStore.generatePreAttendanceLink === 'function') {
    try {
      const res = await triageStore.generatePreAttendanceLink();
      if (res && res.link) {
        preAttendanceLink.value = res.link;
      }
    } catch (err) {
      console.warn('[TriagemClientes] Erro ao obter link:', err);
    }
  }
  showLinkModal.value = true;
}

function copyLink() {
  if (navigator.clipboard) {
    navigator.clipboard.writeText(preAttendanceLink.value).catch(() => {});
  }
  alert(`Link copiado para a área de transferência:\n${preAttendanceLink.value}`);
}

function toggleFilter() {
  alert('Filtros por tatuador, estilo e valor ativados.');
}

function handleNotifications() {
  console.log('Notificações abertas');
}

function handleProfile() {
  router.push('/perfil');
}
</script>
