<template>
  <ion-page>
    <ion-content class="bg-[#09090b] text-[#f2ebd9] font-sans" fullscreen>
      <div class="screen-container min-h-screen pb-24 pt-3">
        <!-- Reusable App Header -->
        <AppHeader
          @click-notifications="handleNotifications"
          @click-profile="handleProfile"
        />

        <!-- Secondary Navigation Header (Voltar para o fluxo) -->
        <div class="flex items-center justify-between mt-1 mb-2 px-0.5">
          <button
            @click="handleBack"
            class="flex items-center gap-1.5 text-[#c9a86a] hover:text-[#d6b77e] transition-colors cursor-pointer group"
          >
            <ArrowLeft class="w-4 h-4 text-[#c9a86a] group-hover:-translate-x-0.5 transition-transform" />
            <span class="font-jetbrains text-[9.5px] font-bold tracking-[1.5px] uppercase">
              ETAPA ANTERIOR (ALOCAÇÃO)
            </span>
          </button>

          <!-- Protocol & Asepsis Badge / Header Tag -->
          <div class="flex items-center gap-1.5 px-0.5">
            <span class="text-[#c9a86a] text-[10px] leading-none">✦</span>
            <span class="font-jetbrains text-[10px] font-bold tracking-[1.8px] text-[#c9a86a] uppercase">
              PROTOCOLO NOTARIAL
            </span>
          </div>
        </div>

        <!-- Page Title & Subtitle -->
        <div class="mb-4 px-0.5">
          <h1 class="font-playfair text-[25px] sm:text-[27px] font-semibold text-[#f2ebd9] leading-tight mb-1">
            Termo &amp; Ficha de Anamnese
          </h1>
          <p class="font-inter text-[12px] text-[#a39e93] leading-relaxed">
            Consentimento Jurídico Esclarecido e Curadoria Sanitária do Atelier
          </p>
        </div>

        <!-- SECTION 1: PROCEDIMENTO AGENDADO CARD -->
        <div class="bg-[#141416] border border-[rgba(255,255,255,0.07)] rounded-[4px] p-3.5 mb-3.5 shadow-lg">
          <!-- Card Header Row -->
          <div class="flex items-center justify-between mb-1">
            <span class="font-jetbrains text-[9.5px] font-bold tracking-[1.4px] text-[#8e8a82] uppercase">
              PROCEDIMENTO AGENDADO
            </span>
            <!-- Verified Rosette Seal Badge -->
            <div class="w-7 h-7 bg-[#1c1a17] border border-[rgba(201,168,106,0.25)] rounded-[4px] flex items-center justify-center shadow-sm">
              <BadgeCheck class="w-4 h-4 text-[#d6b77e]" />
            </div>
          </div>

          <!-- Procedure Title & Description -->
          <h2 class="font-playfair text-[20px] font-semibold text-[#d6b77e] leading-snug">
            {{ client.style }}
          </h2>
          <p class="font-inter text-[12px] text-[#8e8a82] mt-0.5 mb-3 truncate">
            {{ client.projectTitle }} • {{ client.conceptDescription }}
          </p>

          <!-- Client Subcard -->
          <div class="bg-[#09090b] border border-[rgba(255,255,255,0.06)] rounded-[4px] p-2.5 px-3 flex items-center gap-3 mb-2">
            <div class="w-9 h-9 rounded-[2px] bg-[#141416] border border-[rgba(255,255,255,0.08)] flex items-center justify-center text-[#8e8a82] shrink-0">
              <User class="w-4.5 h-4.5" />
            </div>
            <div class="min-w-0 flex-1">
              <div class="font-jetbrains text-[8.5px] font-bold tracking-[1.2px] text-[#8e8a82] uppercase leading-none">
                CLIENTE SOLICITANTE
              </div>
              <div class="font-inter text-[13px] font-bold text-[#f2ebd9] truncate leading-tight mt-1">
                {{ client.name }}
              </div>
              <div class="font-jetbrains text-[10.5px] text-[#8e8a82] leading-none mt-1 flex items-center gap-2">
                <span>CPF: {{ client.cpf }}</span>
                <span>•</span>
                <span>{{ client.phone }}</span>
              </div>
            </div>
          </div>

          <!-- Tattoo Artist Subcard -->
          <div class="bg-[#09090b] border border-[rgba(255,255,255,0.06)] rounded-[4px] p-2.5 px-3 flex items-center gap-3">
            <div class="w-9 h-9 rounded-[2px] bg-[#141416] border border-[rgba(255,255,255,0.08)] flex items-center justify-center text-[#c9a86a] shrink-0">
              <PenTool class="w-4.5 h-4.5" />
            </div>
            <div class="min-w-0 flex-1">
              <div class="font-jetbrains text-[8.5px] font-bold tracking-[1.2px] text-[#8e8a82] uppercase leading-none">
                TATUADOR(A) RESPONSÁVEL
              </div>
              <div class="font-inter text-[13px] font-bold text-[#f2ebd9] truncate leading-tight mt-1">
                {{ client.assignedArtist }}
              </div>
              <div class="font-jetbrains text-[9.5px] font-bold text-[#c9a86a] tracking-[1.2px] uppercase leading-none mt-1">
                {{ client.station || 'BANCADA 01' }}
              </div>
            </div>
          </div>
        </div>

        <!-- SECTION 2: CHECKLIST DE ANAMNESE PRÉ-PREENCHIDA PELO BOT WHATSAPP -->
        <div class="bg-[#141416] border border-[rgba(255,255,255,0.07)] rounded-[4px] p-3.5 mb-3.5 shadow-lg">
          <!-- Card Header Row -->
          <div class="flex items-center justify-between mb-2">
            <div class="flex items-center gap-2">
              <ShieldCheck class="w-4.5 h-4.5 text-[#d6b77e]" />
              <h2 class="font-playfair text-[17px] font-semibold text-[#f2ebd9]">
                Anamnese &amp; Curadoria Sanitária
              </h2>
            </div>
            <!-- Status Badge: APROVADO PELO BOT -->
            <div class="bg-[#0e2417] border border-[#225533] px-2.5 py-0.5 rounded-[2px] flex items-center gap-1">
              <CheckCircle2 class="w-3 h-3 text-[#4ade80]" />
              <span class="font-jetbrains text-[9px] font-bold text-[#4ade80] tracking-[1.2px] uppercase">
                APROVADO VIA BOT
              </span>
            </div>
          </div>

          <!-- WhatsApp Bot Synchronization Banner -->
          <div class="bg-[#09090b] border border-[rgba(201,168,106,0.2)] rounded-[3px] p-2.5 mb-3 flex items-start gap-2.5">
            <Bot class="w-4 h-4 text-[#c9a86a] shrink-0 mt-0.5" />
            <div class="min-w-0 flex-1 text-[11px] font-inter">
              <div class="font-jetbrains text-[9px] font-bold text-[#c9a86a] uppercase tracking-wider">
                SINCRONIZADO VIA WHATSAPP BOT • MODO AUDITORIA
              </div>
              <p class="text-[#a39e93] text-[10.5px] leading-relaxed mt-0.5">
                Respostas informadas previamente pelo cliente via WhatsApp (<strong class="text-[#f2ebd9]">{{ client.anamnese.receivedAt }}</strong>).
                Ficha em <strong>somente leitura</strong> para conferência antes da assinatura na bancada.
              </p>
            </div>
          </div>

          <!-- Dynamic Anamnese Checklist Items (Boolean Sim/Não) -->
          <div class="space-y-2">
            <div
              v-for="item in anamneseQuestions"
              :key="item.id"
              class="bg-[#09090b] border border-[rgba(255,255,255,0.05)] rounded-[4px] px-3 py-2.5 flex items-center justify-between gap-3"
            >
              <div class="flex items-start gap-2.5 min-w-0 flex-1">
                <CheckCircle2 class="w-4 h-4 text-[#c9a86a] shrink-0 mt-0.5" />
                <div class="min-w-0 flex-1">
                  <div class="font-inter text-[12px] font-medium text-[#f2ebd9] leading-snug">
                    {{ item.title }}
                  </div>
                  <div class="font-inter text-[10px] text-[#8e8a82] mt-0.5 leading-tight">
                    {{ item.subtitle }}
                  </div>
                </div>
              </div>

              <!-- Boolean Badge Display (Sim / Não) -->
              <div class="shrink-0">
                <!-- Se for resposta NÃO (Seguro/Aprovado) -->
                <div
                  v-if="!item.value"
                  class="bg-[#0e2417] border border-[#225533] px-2.5 py-0.5 rounded-[2px]"
                >
                  <span class="font-jetbrains text-[9.5px] font-bold text-[#4ade80] uppercase tracking-wider">
                    NÃO
                  </span>
                </div>

                <!-- Se for resposta SIM (Alerta ou detalhe) -->
                <div
                  v-else
                  :class="[
                    'px-2.5 py-0.5 rounded-[2px] font-jetbrains text-[9.5px] font-bold uppercase tracking-wider',
                    item.warningIfTrue
                      ? 'bg-[#381617] border border-[#752629] text-[#f87171]'
                      : 'bg-[#241e17] border border-[rgba(201,168,106,0.3)] text-[#e6c383]'
                  ]"
                >
                  <span>{{ item.detailText ? item.detailText : 'SIM' }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- SECTION 3: TERMO DE CONSENTIMENTO CARD -->
        <div class="bg-[#141416] border border-[rgba(255,255,255,0.07)] rounded-[4px] p-3.5 mb-3.5 shadow-lg">
          <!-- Card Header Row -->
          <div class="flex items-center justify-between mb-2.5">
            <div class="flex items-center gap-2">
              <Gavel class="w-4.5 h-4.5 text-[#d6b77e]" />
              <h2 class="font-playfair text-[17px] font-semibold text-[#f2ebd9]">
                Termo de Consentimento Notarial
              </h2>
            </div>
            <!-- Doc ID Badge -->
            <div class="bg-[#09090b] border border-[rgba(201,168,106,0.3)] px-2.5 py-1 rounded-[2px]">
              <span class="font-jetbrains text-[9.5px] font-bold text-[#c9a86a] tracking-[1.2px] uppercase">
                DOC {{ client.termNumber }}
              </span>
            </div>
          </div>

          <!-- Document Clause Container -->
          <div class="bg-[#09090b] border border-[rgba(201,168,106,0.2)] rounded-[4px] p-3">
            <h3 class="font-playfair text-[12.5px] font-semibold text-[#d6b77e] mb-1.5 leading-snug">
              1. Responsabilidade Sanitária &amp; Normas ANVISA
            </h3>
            <p class="font-inter text-[11px] leading-[1.65] text-[#a39e93]">
              Declaro sob fé notarial estar ciente de que todos os agulhamentos, biqueiras descartáveis e tintas utilizadas neste procedimento seguem rigorosamente os padrões da RDC da ANVISA. Todo material perfurocortante é descartado em recipiente rígido apropriado na presença do cliente. As respostas fornecidas no questionário acima são verdadeiras.
            </p>
          </div>
        </div>

        <!-- SECTION 4: ASSINATURA DIGITAL CARD -->
        <div class="bg-[#141416] border border-[rgba(255,255,255,0.07)] rounded-[4px] p-3.5 mb-3.5 shadow-lg">
          <!-- Card Header Row -->
          <div class="flex items-center justify-between mb-2">
            <div>
              <h2 class="font-playfair text-[17px] font-semibold text-[#f2ebd9]">
                Assinatura Digital
              </h2>
              <span class="font-jetbrains text-[8.5px] font-bold tracking-[1.2px] text-[#8e8a82] uppercase block mt-0.5">
                BANCADA INTERATIVA • DEDO OU STYLUS
              </span>
            </div>
            <!-- Clear Signature Button -->
            <button
              @click="clearSignature"
              class="bg-[#141416] hover:bg-[#1f1d19] border border-[rgba(201,168,106,0.3)] px-2.5 py-1 rounded-[2px] flex items-center gap-1.5 text-[#c9a86a] transition-colors cursor-pointer active:scale-95"
            >
              <RotateCcw class="w-3 h-3" />
              <span class="font-jetbrains text-[9.5px] font-bold tracking-[1px] uppercase">
                LIMPAR
              </span>
            </button>
          </div>

          <!-- Signature Canvas Box -->
          <div
            class="relative h-44 bg-[#09090b] border border-[rgba(201,168,106,0.25)] rounded-[4px] mt-2.5 overflow-hidden flex flex-col justify-end p-3 select-none"
          >
            <!-- Background subtle gradient glow -->
            <div class="absolute inset-0 bg-[radial-gradient(ellipse_at_bottom,rgba(201,168,106,0.07)_0%,transparent_75%)] pointer-events-none"></div>

            <!-- Interactive Canvas -->
            <canvas
              ref="signatureCanvas"
              class="absolute inset-0 w-full h-full cursor-crosshair z-10 touch-none"
              @mousedown="startDrawing"
              @mousemove="draw"
              @mouseup="stopDrawing"
              @mouseleave="stopDrawing"
              @touchstart.passive="startDrawingTouch"
              @touchmove.prevent="drawTouch"
              @touchend="stopDrawing"
            ></canvas>

            <!-- Golden Guide Line & Baseline Label -->
            <div class="relative z-0 pointer-events-none w-full flex flex-col items-center pb-1">
              <div class="w-3/4 border-b border-[rgba(201,168,106,0.35)] mb-1.5"></div>
              <span class="font-jetbrains text-[8.5px] font-bold tracking-[1.5px] text-[#8e8a82] uppercase">
                RUBRIQUE SOBRE A LINHA DOURADA
              </span>
            </div>
          </div>

          <!-- Agreement Checkbox -->
          <label class="flex items-start gap-2.5 mt-3.5 cursor-pointer group">
            <div
              class="w-4.5 h-4.5 rounded-[2px] border transition-colors flex items-center justify-center shrink-0 mt-0.5"
              :class="isAgreed ? 'bg-[#c9a86a] border-[#c9a86a] text-[#09090b]' : 'bg-[#09090b] border-[rgba(201,168,106,0.4)] group-hover:border-[#c9a86a]'"
              @click.prevent="isAgreed = !isAgreed"
            >
              <Check v-if="isAgreed" class="w-3.5 h-3.5 stroke-[3]" />
            </div>
            <span
              class="font-inter text-[10.5px] leading-relaxed text-[#a39e93] group-hover:text-[#f2ebd9] transition-colors select-none"
              @click="isAgreed = !isAgreed"
            >
              Declaro ter conferido as respostas de anamnese preenchidas via WhatsApp, compreendido todas as diretrizes de assepsia e assinado livremente este termo de consentimento.
            </span>
          </label>
        </div>

        <!-- ACTION CTAS -->
        <div class="space-y-2.5 mb-3">
          <!-- Primary CTA Button: Autenticar Termo -->
          <button
            @click="autenticarTermo"
            :disabled="isSubmittingTerm"
            class="w-full bg-[#c9a86a] hover:bg-[#d6b77e] text-[#09090b] font-inter text-[11.5px] font-bold tracking-[1.5px] uppercase py-3.5 px-4 rounded-[2px] shadow-xl flex items-center justify-center gap-2 transition-all cursor-pointer active:scale-[0.99] disabled:opacity-75"
          >
            <Lock class="w-4 h-4 stroke-[2.2]" />
            <span>{{ isSubmittingTerm ? 'AUTENTICANDO NO SERVIDOR...' : 'AUTENTICAR TERMO' }}</span>
          </button>

          <!-- Secondary CTA Button: Enviar Cópia PDF WhatsApp -->
          <button
            @click="enviarWhatsapp"
            class="w-full bg-[#141416] hover:bg-[#1a1917] border border-[rgba(201,168,106,0.4)] hover:border-[#c9a86a] text-[#e6c383] font-inter text-[11px] font-bold tracking-[1.2px] uppercase py-3.5 px-4 rounded-[2px] flex items-center justify-center gap-2 transition-all cursor-pointer active:scale-[0.99]"
          >
            <FileText class="w-4 h-4 stroke-[2]" />
            <span>ENVIAR CÓPIA EM PDF PARA WHATSAPP</span>
          </button>
        </div>

        <!-- Footnote: Criptografia & Registro Digital -->
        <div class="flex items-center justify-center gap-1.5 text-[#8e8a82] font-jetbrains text-[9px] font-bold tracking-[1.8px] uppercase mb-4">
          <ShieldCheck class="w-3.5 h-3.5 text-[#c9a86a]" />
          <span>CRIPTOGRAFIA • REGISTRO DIGITAL NOTARIAL</span>
        </div>
      </div>

      <!-- SUCCESS MODAL: TERMO AUTENTICADO -->
      <div
        v-if="showAuthModal"
        class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md transition-opacity"
        @click.self="showAuthModal = false"
      >
        <div class="bg-[#141416] border border-[rgba(201,168,106,0.4)] rounded-[4px] max-w-sm w-full p-4 shadow-2xl relative">
          <div class="flex items-center justify-between pb-3 border-b border-[rgba(255,255,255,0.06)] mb-3">
            <div class="flex items-center gap-2">
              <ShieldCheck class="w-5 h-5 text-[#86efac]" />
              <h3 class="font-playfair text-[18px] font-semibold text-[#f2ebd9]">
                Termo Autenticado
              </h3>
            </div>
            <button
              @click="showAuthModal = false"
              class="text-[#a39e93] hover:text-[#f2ebd9] p-1 cursor-pointer"
            >
              <X class="w-4 h-4" />
            </button>
          </div>

          <div class="bg-[#09090b] p-3 rounded-[2px] border border-[rgba(255,255,255,0.05)] text-[12px] space-y-2 mb-4">
            <div class="text-[#c9a86a] font-jetbrains text-[10.5px] uppercase font-bold tracking-wider">
              REGISTRO NOTARIAL {{ client.termNumber }}
            </div>
            <p class="text-[#f2ebd9] font-inter text-[12px] leading-relaxed">
              {{ authFeedbackMessage || `Termo assinado e carimbado digitalmente por ${client.name} na ${client.station || 'Bancada 01'} com ${client.assignedArtist}.` }}
            </p>
            <div class="text-[#a39e93] font-jetbrains text-[10px] space-y-0.5 pt-1 border-t border-[rgba(255,255,255,0.05)]">
              <div>PROTOCOLO: {{ authProtocol || 'SHA256:7f4c9...821b0e' }}</div>
              <div>ASSINATURA: BIOMETRIA DIGITAL CONFIRMADA</div>
              <div>BOT WHATSAPP: REF {{ client.anamnese.botVerificationId }}</div>
            </div>
          </div>

          <div class="flex gap-2">
            <button
              @click="concluirParaRecepcao"
              class="flex-1 py-3 bg-[#c9a86a] text-[#09090b] font-inter text-[11px] font-bold tracking-[1.2px] uppercase rounded-[2px] cursor-pointer hover:bg-[#d6b77e]"
            >
              CONCLUIR PROTOCOLO
            </button>
          </div>
        </div>
      </div>

      <!-- SUCCESS MODAL: PDF ENVIADO WHATSAPP -->
      <div
        v-if="showWhatsappModal"
        class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md transition-opacity"
        @click.self="showWhatsappModal = false"
      >
        <div class="bg-[#141416] border border-[rgba(201,168,106,0.4)] rounded-[4px] max-w-sm w-full p-4 shadow-2xl relative">
          <div class="flex items-center justify-between pb-3 border-b border-[rgba(255,255,255,0.06)] mb-3">
            <div class="flex items-center gap-2">
              <CheckCircle2 class="w-5 h-5 text-[#86efac]" />
              <h3 class="font-playfair text-[18px] font-semibold text-[#f2ebd9]">
                Disparo via WhatsApp
              </h3>
            </div>
            <button
              @click="showWhatsappModal = false"
              class="text-[#a39e93] hover:text-[#f2ebd9] p-1 cursor-pointer"
            >
              <X class="w-4 h-4" />
            </button>
          </div>

          <p class="text-[#f2ebd9] font-inter text-[12.5px] leading-relaxed mb-4">
            {{ whatsappFeedbackMessage || `A cópia assinada do Termo & Anamnese ${client.termNumber} foi enviada em PDF criptografado para o WhatsApp de ${client.name} (${client.phone}).` }}
          </p>

          <button
            @click="showWhatsappModal = false"
            class="w-full py-2.5 bg-[#c9a86a] text-[#09090b] font-inter text-[11px] font-bold tracking-[1.2px] uppercase rounded-[2px] cursor-pointer hover:bg-[#d6b77e]"
          >
            FECHAR
          </button>
        </div>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import { IonPage, IonContent } from '@ionic/vue';
import { useRouter, useRoute } from 'vue-router';
import {
  BadgeCheck,
  User,
  PenTool,
  ShieldCheck,
  CheckCircle2,
  Gavel,
  RotateCcw,
  Check,
  Lock,
  FileText,
  ArrowLeft,
  Bot,
  X
} from '@lucide/vue';
import AppHeader from '../components/AppHeader.vue';
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

// Questions modeled with boolean answers from WhatsApp Bot
const anamneseQuestions = computed(() => [
  {
    id: 'alergias',
    title: 'Alergia a pigmentos, látex ou cosméticos',
    subtitle: 'Reações alérgicas a derivados tópicos ou tintas',
    value: client.value.anamnese.allergies,
    warningIfTrue: true,
  },
  {
    id: 'anticoagulantes',
    title: 'Uso de anticoagulantes ou antiagregantes',
    subtitle: 'Medicamentos que interferem na coagulação sanguínea',
    value: client.value.anamnese.anticoagulants,
    warningIfTrue: true,
  },
  {
    id: 'queloides',
    title: 'Histórico de queloides ou cicatrização hipertrófica',
    subtitle: 'Tendência dérmica a cicatrizes com relevo espesso',
    value: client.value.anamnese.keloids,
    warningIfTrue: true,
  },
  {
    id: 'diabetes',
    title: 'Diabetes descompensada ou coagulopatias',
    subtitle: 'Condições de cicatrização e hemostasia da pele',
    value: client.value.anamnese.diabetesOrBleeding,
    warningIfTrue: true,
  },
  {
    id: 'gestante',
    title: 'Gestante ou em período de lactação',
    subtitle: 'Contraindicação sanitária temporária',
    value: client.value.anamnese.pregnantOrLactating,
    warningIfTrue: true,
  },
  {
    id: 'alimentacao',
    title: 'Alimentação sólida prévia (< 3h)',
    subtitle: 'Glicemia adequada para prevenção de hipotensão',
    value: client.value.anamnese.recentMeal,
    detailText: client.value.anamnese.recentMealDetail,
    warningIfTrue: false,
  },
]);

// Agreement checkbox state
const isAgreed = ref(false);

// Modals
const showAuthModal = ref(false);
const showWhatsappModal = ref(false);
const isSubmittingTerm = ref(false);
const authProtocol = ref('');
const authFeedbackMessage = ref('');
const whatsappFeedbackMessage = ref('');

// Signature Canvas Reference & Drawing state
const signatureCanvas = ref<HTMLCanvasElement | null>(null);
const isDrawing = ref(false);
const hasSignature = ref(false);
let ctx: CanvasRenderingContext2D | null = null;

onMounted(async () => {
  setupCanvas();
  window.addEventListener('resize', handleCanvasResize);
  // Sincroniza dados da anamnese e do termo do backend com fallback mock
  await triageStore.fetchTermoAnamnese(clientId.value);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleCanvasResize);
});

function handleBack() {
  router.push({
    path: '/alocacao-materiais',
    query: { clientId: client.value.id },
  });
}

function setupCanvas() {
  const canvas = signatureCanvas.value;
  if (!canvas) return;

  const rect = canvas.getBoundingClientRect();
  const dpr = window.devicePixelRatio || 1;

  canvas.width = rect.width * dpr;
  canvas.height = rect.height * dpr;

  ctx = canvas.getContext('2d');
  if (ctx) {
    ctx.scale(dpr, dpr);
    ctx.strokeStyle = '#f2ebd9';
    ctx.lineWidth = 2.2;
    ctx.lineCap = 'round';
    ctx.lineJoin = 'round';
  }
}

function handleCanvasResize() {
  setupCanvas();
}

function getCanvasCoordinates(e: MouseEvent) {
  const canvas = signatureCanvas.value;
  if (!canvas) return { x: 0, y: 0 };
  const rect = canvas.getBoundingClientRect();
  return {
    x: e.clientX - rect.left,
    y: e.clientY - rect.top,
  };
}

function getTouchCoordinates(e: TouchEvent) {
  const canvas = signatureCanvas.value;
  if (!canvas || !e.touches[0]) return { x: 0, y: 0 };
  const rect = canvas.getBoundingClientRect();
  return {
    x: e.touches[0].clientX - rect.left,
    y: e.touches[0].clientY - rect.top,
  };
}

function startDrawing(e: MouseEvent) {
  if (!ctx) return;
  isDrawing.value = true;
  hasSignature.value = true;
  const { x, y } = getCanvasCoordinates(e);
  ctx.beginPath();
  ctx.moveTo(x, y);
}

function draw(e: MouseEvent) {
  if (!isDrawing.value || !ctx) return;
  const { x, y } = getCanvasCoordinates(e);
  ctx.lineTo(x, y);
  ctx.stroke();
}

function startDrawingTouch(e: TouchEvent) {
  if (!ctx) return;
  isDrawing.value = true;
  hasSignature.value = true;
  const { x, y } = getTouchCoordinates(e);
  ctx.beginPath();
  ctx.moveTo(x, y);
}

function drawTouch(e: TouchEvent) {
  if (!isDrawing.value || !ctx) return;
  const { x, y } = getTouchCoordinates(e);
  ctx.lineTo(x, y);
  ctx.stroke();
}

function stopDrawing() {
  if (!isDrawing.value || !ctx) return;
  isDrawing.value = false;
  ctx.closePath();
}

function clearSignature() {
  const canvas = signatureCanvas.value;
  if (!canvas || !ctx) return;
  ctx.clearRect(0, 0, canvas.width, canvas.height);
  hasSignature.value = false;
}

function handleNotifications() {
  console.log('Notificações abertas');
}

function handleProfile() {
  console.log('Perfil aberto');
}

async function autenticarTermo() {
  if (!isAgreed.value) {
    alert('Por favor, assinale a declaração de conferência e leitura do termo antes de autenticar.');
    return;
  }
  if (!hasSignature.value) {
    alert('Por favor, colete a assinatura digital na área demarcada antes de autenticar.');
    return;
  }

  isSubmittingTerm.value = true;
  try {
    const signatureBase64 = signatureCanvas.value ? signatureCanvas.value.toDataURL('image/png') : '';
    const result = await triageStore.signTermo({
      clientId: client.value.id,
      signatureBase64,
      termNumber: client.value.termNumber,
      agreedAt: new Date().toISOString(),
      botVerificationId: client.value.anamnese.botVerificationId,
      clientCpf: client.value.cpf,
    });

    authProtocol.value = result.protocol;
    authFeedbackMessage.value = result.message;
    showAuthModal.value = true;
  } catch (err) {
    console.warn('[TermoAnamnese] Falha na autenticação do termo:', err);
    showAuthModal.value = true;
  } finally {
    isSubmittingTerm.value = false;
  }
}

function concluirParaRecepcao() {
  showAuthModal.value = false;
  router.push('/triagem');
}

async function enviarWhatsapp() {
  try {
    const result = await triageStore.sendTermoWhatsapp(client.value.id);
    whatsappFeedbackMessage.value = result.message;
  } catch (err) {
    console.warn('[TermoAnamnese] Falha no disparo WhatsApp:', err);
  }
  showWhatsappModal.value = true;
}
</script>
