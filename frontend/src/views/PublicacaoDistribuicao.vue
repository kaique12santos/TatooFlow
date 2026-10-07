<template>
  <ion-page>
    <ion-content class="bg-[#09090b] text-[#f2ebd9] select-none font-sans" fullscreen>
      <div class="min-h-screen bg-[#09090b] pb-32 max-w-[480px] mx-auto px-4 pt-3 sm:px-6">
        <!-- Reusable App Header -->
        <AppHeader
          @click-notifications="handleNotifications"
          @click-profile="handleProfile"
        />

        <!-- Step Tracker & Full Progress Bar (Passo 3 de 3 · 100%) -->
        <div class="mb-4 px-0.5">
          <div class="flex items-center justify-between mb-1.5">
            <div class="flex items-center gap-1.5">
              <span class="w-1.5 h-1.5 bg-[#c9a86a] rounded-[1px]"></span>
              <span class="font-inter text-[10px] font-bold tracking-[1.2px] uppercase text-[#a39e93]">
                PASSO 3 DE 3 · PUBLICAÇÃO & DISTRIBUIÇÃO INSTAGRAM
              </span>
            </div>
            <span class="font-jetbrains text-[11px] font-bold text-[#e6c383]">
              100%
            </span>
          </div>

          <!-- 3-Segment Fully Completed Progress Bar -->
          <div class="grid grid-cols-3 gap-2">
            <div class="h-1 bg-[#c9a86a] rounded-full shadow-[0_0_8px_rgba(201,168,106,0.4)]"></div>
            <div class="h-1 bg-[#c9a86a] rounded-full shadow-[0_0_8px_rgba(201,168,106,0.4)]"></div>
            <div class="h-1 bg-[#c9a86a] rounded-full shadow-[0_0_8px_rgba(201,168,106,0.4)]"></div>
          </div>
        </div>

        <!-- Protocol & Context Card -->
        <div class="bg-[#141416] border border-[rgba(201,168,106,0.18)] rounded-[4px] p-3 shadow-md mb-3">
          <div class="flex items-center justify-between mb-1">
            <span class="font-inter text-[9px] font-bold tracking-[1.2px] text-[#7a766f] uppercase">
              PROTOCOLO DE REGISTRO
            </span>
            <span class="font-jetbrains text-[10px] font-bold text-[#c9a86a]">
              {{ marketingStore.protocolNumber }}
            </span>
          </div>

          <div class="font-inter font-medium text-[11.5px] text-[#f2ebd9] mb-1">
            {{ marketingStore.clientContext.clientName }} - {{ marketingStore.clientContext.station }} - M. {{ marketingStore.clientContext.artistName }}
          </div>

          <div class="flex items-center gap-1 text-[11px] text-[#a39e93] truncate">
            <span class="text-[#c9a86a]">🖼️</span>
            <span class="truncate">Obra: {{ marketingStore.clientContext.artworkTitle }}</span>
          </div>
        </div>

        <!-- Instagram Connected Profile Card -->
        <div class="bg-[#141416] border border-[rgba(201,168,106,0.2)] rounded-[4px] p-3.5 flex items-center justify-between mb-4 shadow-lg">
          <div class="flex items-center gap-3 min-w-0 pr-2">
            <!-- Avatar Monogram -->
            <div class="w-12 h-12 bg-[#0c0c0e] border border-[rgba(201,168,106,0.3)] rounded-[2px] flex items-center justify-center relative shrink-0 shadow-inner">
              <span class="font-playfair font-bold text-[16px] text-[#c9a86a]">
                {{ marketingStore.activeSocialAccount?.monogram || 'NC' }}
              </span>
              <!-- Tiny Instagram Badge -->
              <div class="absolute -bottom-1 -right-1 w-4 h-4 bg-[#141416] border border-[rgba(201,168,106,0.35)] rounded-full flex items-center justify-center text-[#c9a86a]">
                <Radio class="w-2.5 h-2.5" />
              </div>
            </div>

            <!-- Profile Details -->
            <div class="min-w-0 truncate">
              <div class="font-playfair text-[14.5px] font-semibold text-[#f2ebd9] truncate">
                {{ marketingStore.activeSocialAccount?.handle || '@Gabriel_dornelles' }}
              </div>
              <div class="flex items-center gap-1.5 mt-0.5">
                <span
                  class="w-1.5 h-1.5 rounded-full"
                  :class="(marketingStore.activeSocialAccount?.isValid ?? true) ? 'bg-[#86efac]' : 'bg-[#e06c75]'"
                ></span>
                <span class="font-jetbrains text-[8.5px] font-bold text-[#a39e93] tracking-[0.5px] uppercase truncate">
                  {{ marketingStore.activeSocialAccount?.statusToken || 'TOKEN META GRAPH V20.0 VÁLIDO' }}
                </span>
              </div>
            </div>
          </div>

          <!-- Switch Account Button -->
          <button
            @click="switchAccount"
            class="border border-[rgba(201,168,106,0.3)] bg-transparent hover:bg-[#1a1917] px-2.5 py-1.5 rounded-[2px] font-jetbrains text-[9px] font-bold text-[#d6b77e] uppercase tracking-wider cursor-pointer transition-colors shrink-0"
          >
            TROCAR CONTA
          </button>
        </div>

        <!-- Section: Composição Curada do Post -->
        <div class="mb-5">
          <div class="flex items-center justify-between mb-2.5 px-0.5">
            <div class="flex items-center gap-1.5">
              <span class="text-[#c9a86a] text-[13px]">⚜</span>
              <h2 class="font-inter text-[10px] font-bold tracking-[1.2px] text-[#f2ebd9] uppercase">
                COMPOSIÇÃO CURADA DO POST
              </h2>
            </div>

            <span class="bg-[#241f17] border border-[rgba(201,168,106,0.35)] text-[#e6c383] font-jetbrains text-[9px] font-bold px-2 py-0.5 rounded-[2px] uppercase">
              ETAPA 3/3 APROVADA
            </span>
          </div>

          <!-- Post Preview Deck -->
          <div class="bg-[#141416] border border-[rgba(201,168,106,0.18)] rounded-[4px] p-3.5 shadow-xl">
            <div class="flex items-start gap-3">
              <!-- Thumbnail Mock -->
              <div class="w-20 h-24 rounded-[2px] overflow-hidden border border-[rgba(255,255,255,0.08)] bg-[#09090b] relative shrink-0">
                <img
                  :src="marketingStore.masterShotUrl || '/assets/sao_jeronimo_tattoo.jpg'"
                  alt="Post Preview"
                  class="w-full h-full object-cover"
                />
                <span class="absolute bottom-1 right-1 bg-black/80 font-jetbrains text-[8px] text-[#b8afa0] px-1 rounded-[1px]">
                  1/4
                </span>
              </div>

              <!-- Caption Excerpt & Tags -->
              <div class="flex-1 min-w-0">
                <div class="flex items-center gap-1 text-[#c9a86a]">
                  <Sparkles class="w-3.5 h-3.5 text-[#c9a86a]" />
                  <span class="font-jetbrains text-[9px] font-bold uppercase tracking-wider">
                    LEGENDA IA {{ marketingStore.selectedTone.toUpperCase() }}
                  </span>
                </div>

                <p class="font-inter italic text-[11px] text-[#f2ebd9] leading-relaxed my-1.5 line-clamp-2">
                  "{{ marketingStore.captionBody }}"
                </p>

                <!-- Tags Row -->
                <div class="flex flex-wrap gap-1 mt-1">
                  <span
                    v-for="tag in marketingStore.selectedHashtags.slice(0, 3)"
                    :key="tag"
                    class="font-jetbrains text-[8.5px] text-[#a39e93] bg-[#0c0c0e] border border-[rgba(255,255,255,0.06)] px-1.5 py-0.5 rounded-[1px]"
                  >
                    {{ tag }}
                  </span>
                  <span
                    v-if="marketingStore.selectedHashtags.length > 3"
                    class="font-jetbrains text-[8.5px] text-[#c9a86a] bg-[#0c0c0e] border border-[rgba(201,168,106,0.2)] px-1.5 py-0.5 rounded-[1px]"
                  >
                    +{{ marketingStore.selectedHashtags.length - 3 }}
                  </span>
                </div>
              </div>
            </div>

            <!-- Technical Pipeline Strip -->
            <div class="border-t border-[rgba(255,255,255,0.06)] pt-2.5 mt-2.5 flex items-center justify-between text-[10px] font-jetbrains">
              <span class="text-[#7a766f]">
                Master DCI-P3 • Masterizado 4K UHD
              </span>
              <div class="flex items-center gap-1 text-[#86efac]">
                <CheckCircle2 class="w-3 h-3 text-[#86efac]" />
                <span class="font-medium">Compressor Ativo</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Section: Cronograma de Disparo (Janela de Engajamento de Arte) -->
        <div class="bg-[#141416] border border-[rgba(201,168,106,0.18)] rounded-[4px] p-4 shadow-xl mb-5">
          <!-- Header Row -->
          <div class="flex items-start justify-between mb-2">
            <div>
              <h2 class="font-playfair text-[20px] font-semibold text-[#f2ebd9] leading-tight">
                Cronograma de Disparo
              </h2>
              <span class="font-inter text-[9px] font-bold tracking-[1.4px] text-[#7a766f] uppercase block mt-0.5">
                JANELA DE ENGAJAMENTO DE ARTE
              </span>
            </div>

            <span class="text-[#c9a86a] text-[16px]">⚑</span>
          </div>

          <!-- AI Recommendation Box -->
          <div class="bg-[#1a1712] border border-[rgba(201,168,106,0.25)] rounded-[2px] p-3 my-3">
            <div class="flex items-center gap-1.5 text-[#e6c383]">
              <span class="text-[12px]">⚖</span>
              <span class="font-inter text-[9.5px] font-bold tracking-[1.2px] uppercase">
                CURADORIA ALGORÍTMICA DA GALERIA
              </span>
            </div>
            <p class="font-inter text-[11px] text-[#b8afa0] leading-relaxed mt-1">
              Horário de pico detectado: O público da New Concept atinge engajamento máximo às <strong class="text-[#f2ebd9]">{{ marketingStore.activeSocialAccount?.engagementPeakTime || '20:45' }}</strong> em {{ marketingStore.activeSocialAccount?.engagementPeakDay || 'sextas-feiras' }}.
            </p>
          </div>

          <!-- Date Presets Row -->
          <div class="grid grid-cols-3 gap-2 mb-3.5">
            <button
              @click="marketingStore.setPreset('hoje')"
              :class="[
                'py-2 rounded-[2px] font-jetbrains text-[10px] font-bold transition-all cursor-pointer border',
                marketingStore.selectedPreset === 'hoje'
                  ? 'bg-[#c9a86a] text-[#09090b] border-[#c9a86a]'
                  : 'bg-[#0c0c0e] border-[rgba(255,255,255,0.06)] text-[#7a766f] hover:text-[#f2ebd9]'
              ]"
            >
              HOJE
            </button>

            <button
              @click="marketingStore.setPreset('amanha')"
              :class="[
                'py-2 rounded-[2px] font-jetbrains text-[10px] font-bold transition-all cursor-pointer border',
                marketingStore.selectedPreset === 'amanha'
                  ? 'bg-[#c9a86a] text-[#09090b] border-[#c9a86a]'
                  : 'bg-[#0c0c0e] border-[rgba(255,255,255,0.06)] text-[#7a766f] hover:text-[#f2ebd9]'
              ]"
            >
              AMANHÃ
            </button>

            <button
              @click="marketingStore.setPreset('melhor')"
              :class="[
                'py-2 rounded-[2px] font-jetbrains text-[10px] font-bold transition-all cursor-pointer border',
                marketingStore.selectedPreset === 'melhor'
                  ? 'bg-[#c9a86a] text-[#09090b] border-[#c9a86a] shadow-sm'
                  : 'bg-[#0c0c0e] border-[rgba(255,255,255,0.06)] text-[#7a766f] hover:text-[#f2ebd9]'
              ]"
            >
              MELHOR IA
            </button>
          </div>

          <!-- Date Field -->
          <div class="mb-3">
            <div class="flex items-center justify-between mb-1 px-0.5">
              <span class="font-inter text-[9px] font-bold tracking-[1.2px] text-[#7a766f] uppercase">
                DATA DA EXPOSIÇÃO
              </span>
              <span class="font-jetbrains text-[10px] font-bold text-[#c9a86a]">
                {{ marketingStore.scheduleDate }}
              </span>
            </div>

            <div class="bg-[#0c0c0e] border border-[rgba(201,168,106,0.25)] rounded-[2px] p-3 flex items-center justify-between text-[12px] text-[#f2ebd9] font-inter">
              <span>{{ marketingStore.scheduleDateFormatted }}</span>
              <Calendar class="w-4 h-4 text-[#c9a86a]" />
            </div>
          </div>

          <!-- Time Field -->
          <div>
            <div class="flex items-center justify-between mb-1 px-0.5">
              <span class="font-inter text-[9px] font-bold tracking-[1.2px] text-[#7a766f] uppercase">
                HORÁRIO NOBRE RECOMENDADO
              </span>
              <span class="font-jetbrains text-[10px] font-bold text-[#c9a86a] uppercase">
                HORÁRIO NOBRE
              </span>
            </div>

            <div class="bg-[#0c0c0e] border border-[rgba(201,168,106,0.25)] rounded-[2px] p-3 flex items-center justify-between text-[12px] text-[#f2ebd9] font-inter">
              <span>{{ marketingStore.scheduleTime }} • Horário Nobre Atelier</span>
              <Clock class="w-4 h-4 text-[#c9a86a]" />
            </div>
          </div>
        </div>

        <!-- Section: Canais & Formatos (Multi-Destino) -->
        <div class="mb-5">
          <div class="flex items-center justify-between mb-3 px-0.5">
            <h2 class="font-playfair text-[20px] font-semibold text-[#f2ebd9] leading-tight">
              Canais & Formatos
            </h2>
            <span class="font-inter text-[9px] font-bold tracking-[1.2px] text-[#7a766f] uppercase">
              MULTI-DESTINO
            </span>
          </div>

          <!-- Channels List -->
          <div class="space-y-2.5">
            <!-- Channel 1: Feed Principal -->
            <div class="bg-[#141416] border border-[rgba(255,255,255,0.06)] rounded-[2px] p-3.5 flex items-center justify-between">
              <div class="flex-1 pr-3">
                <div class="flex items-center gap-1.5 font-inter text-[12px] font-bold text-[#f2ebd9]">
                  <LayoutGrid class="w-4 h-4 text-[#c9a86a]" />
                  <span>Publicar no Feed Principal</span>
                </div>
                <p class="font-inter text-[11px] text-[#a39e93] mt-0.5">
                  Exibir na grade permanente da galeria @Gabriel_dornelles.
                </p>
              </div>

              <!-- Toggle Switch -->
              <button
                @click="marketingStore.toggleChannel('feed')"
                :class="[
                  'w-11 h-6 rounded-full transition-colors cursor-pointer relative p-0.5 shrink-0',
                  marketingStore.channels.feed ? 'bg-[#c9a86a]' : 'bg-[#222226]'
                ]"
              >
                <div
                  :class="[
                    'w-5 h-5 rounded-full bg-[#09090b] transition-transform',
                    marketingStore.channels.feed ? 'translate-x-5' : 'translate-x-0'
                  ]"
                ></div>
              </button>
            </div>

            <!-- Channel 2: Reels / Processo -->
            <div class="bg-[#141416] border border-[rgba(255,255,255,0.06)] rounded-[2px] p-3.5 flex items-center justify-between">
              <div class="flex-1 pr-3">
                <div class="flex items-center gap-1.5 font-inter text-[12px] font-bold text-[#f2ebd9]">
                  <Film class="w-4 h-4 text-[#c9a86a]" />
                  <span>Publicar no Reels / Processo</span>
                </div>
                <p class="font-inter text-[11px] text-[#a39e93] mt-0.5">
                  Sincronizar clipe do timelapsed gravado na Bancada 01.
                </p>
              </div>

              <button
                @click="marketingStore.toggleChannel('reels')"
                :class="[
                  'w-11 h-6 rounded-full transition-colors cursor-pointer relative p-0.5 shrink-0',
                  marketingStore.channels.reels ? 'bg-[#c9a86a]' : 'bg-[#222226]'
                ]"
              >
                <div
                  :class="[
                    'w-5 h-5 rounded-full bg-[#09090b] transition-transform',
                    marketingStore.channels.reels ? 'translate-x-5' : 'translate-x-0'
                  ]"
                ></div>
              </button>
            </div>

            <!-- Channel 3: Compartilhar nos Stories -->
            <div class="bg-[#141416] border border-[rgba(255,255,255,0.06)] rounded-[2px] p-3.5 flex items-center justify-between">
              <div class="flex-1 pr-3">
                <div class="flex items-center gap-1.5 font-inter text-[12px] font-bold text-[#f2ebd9]">
                  <Share2 class="w-4 h-4 text-[#c9a86a]" />
                  <span>Compartilhar nos Stories</span>
                </div>
                <p class="font-inter text-[11px] text-[#a39e93] mt-0.5">
                  Sticker direcionando ao WhatsApp do Artista.
                </p>
              </div>

              <button
                @click="marketingStore.toggleChannel('stories')"
                :class="[
                  'w-11 h-6 rounded-full transition-colors cursor-pointer relative p-0.5 shrink-0',
                  marketingStore.channels.stories ? 'bg-[#c9a86a]' : 'bg-[#222226]'
                ]"
              >
                <div
                  :class="[
                    'w-5 h-5 rounded-full bg-[#09090b] transition-transform',
                    marketingStore.channels.stories ? 'translate-x-5' : 'translate-x-0'
                  ]"
                ></div>
              </button>
            </div>

            <!-- Channel 4: Fixar no Dossiê Digital do Cliente -->
            <div class="bg-[#141416] border border-[rgba(255,255,255,0.06)] rounded-[2px] p-3.5 flex items-center justify-between">
              <div class="flex-1 pr-3">
                <div class="flex items-center gap-1.5 font-inter text-[12px] font-bold text-[#f2ebd9]">
                  <FolderCheck class="w-4 h-4 text-[#c9a86a]" />
                  <span>Fixar no Dossiê Digital do Cliente</span>
                </div>
                <p class="font-inter text-[11px] text-[#a39e93] mt-0.5">
                  Registrar permalink no livro de registros de Camila Albuquerque.
                </p>
              </div>

              <button
                @click="marketingStore.toggleChannel('dossie')"
                :class="[
                  'w-11 h-6 rounded-full transition-colors cursor-pointer relative p-0.5 shrink-0',
                  marketingStore.channels.dossie ? 'bg-[#c9a86a]' : 'bg-[#222226]'
                ]"
              >
                <div
                  :class="[
                    'w-5 h-5 rounded-full bg-[#09090b] transition-transform',
                    marketingStore.channels.dossie ? 'translate-x-5' : 'translate-x-0'
                  ]"
                ></div>
              </button>
            </div>
          </div>
        </div>

        <!-- Pronto Para Envio à Rede Status Box -->
        <div class="bg-[#141416] border border-[rgba(255,255,255,0.06)] rounded-[2px] p-3 flex items-center gap-2.5 mb-5">
          <ShieldCheck class="w-4.5 h-4.5 text-[#c9a86a] shrink-0" />
          <div>
            <div class="font-inter text-[10px] font-bold tracking-[1.2px] text-[#f2ebd9] uppercase leading-tight">
              PRONTO PARA ENVIO À REDE
            </div>
            <div class="font-inter text-[10.5px] text-[#a39e93] leading-tight mt-0.5">
              4 destinos ativos · Sem duplicação de dados
            </div>
          </div>
        </div>

        <!-- Cryptographic Footer Note -->
        <div class="font-jetbrains text-[9px] text-[#55524d] text-center leading-relaxed mb-6">
          <div>Publicação programada via Instagram Graph API.</div>
          <div>Registro criptográfico imutável {{ marketingStore.protocolNumber }}.</div>
        </div>

        <!-- Confirmation Success Modal -->
        <div
          v-if="showSuccessModal"
          class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/85 backdrop-blur-md transition-opacity"
        >
          <div class="bg-[#141416] border border-[rgba(201,168,106,0.3)] rounded-[4px] max-w-sm w-full p-5 text-center shadow-2xl relative">
            <div class="w-14 h-14 bg-[#102619] border border-[#2d6342] rounded-full mx-auto flex items-center justify-center mb-3">
              <CheckCircle2 class="w-7 h-7 text-[#86efac]" />
            </div>

            <h3 class="font-playfair text-[20px] font-semibold text-[#f2ebd9] mb-1">
              Publicação Agendada!
            </h3>
            <p class="font-inter text-[12px] text-[#a39e93] mb-4">
              A obra {{ marketingStore.clientContext.artworkTitle }} foi programada com sucesso para {{ marketingStore.scheduleDate }} às {{ marketingStore.scheduleTime }} no perfil {{ marketingStore.activeSocialAccount?.handle || '@Gabriel_dornelles' }} e fixada no Dossiê de {{ marketingStore.clientContext.clientName }}.
            </p>

            <button
              @click="concluirFluxo"
              class="w-full py-2.5 bg-[#c9a86a] text-[#09090b] font-inter text-[11px] font-bold tracking-[1.2px] uppercase rounded-[2px] cursor-pointer hover:bg-[#d6b77e]"
            >
              RETORNAR AO DOSSIÊ
            </button>
          </div>
        </div>

      </div>

      <!-- Barra de Ação Ancorada no Rodapé Mobile -->
      <div class="fixed bottom-0 left-0 right-0 z-40 bg-[#09090b]/95 backdrop-blur-md border-t border-[rgba(201,168,106,0.2)] px-4 py-3 shadow-[0_-8px_25px_rgba(0,0,0,0.8)]">
        <div class="max-w-[480px] mx-auto flex items-center gap-3">
          <button
            @click="voltarParaLegenda"
            class="h-11 px-4 bg-[#141416] hover:bg-[#1a1917] border border-[rgba(201,168,106,0.3)] text-[#a39e93] hover:text-[#f2ebd9] font-inter text-[11px] font-bold tracking-[1px] uppercase rounded-[2px] transition-all cursor-pointer flex items-center gap-1.5 shrink-0 active:scale-95"
          >
            <ArrowLeft class="w-3.5 h-3.5" />
            <span>VOLTAR</span>
          </button>

          <button
            @click="agendarPublicacao"
            :disabled="marketingStore.isSubmittingSchedule"
            class="flex-1 h-11 bg-gradient-to-r from-[#c49f5e] via-[#d6b77e] to-[#ba9557] hover:brightness-105 active:scale-[0.99] text-[#09090b] font-inter text-[11px] font-bold tracking-[1.4px] uppercase rounded-[2px] flex items-center justify-center gap-2 shadow-[0_4px_24px_rgba(201,168,106,0.28)] transition-all cursor-pointer disabled:opacity-75"
          >
            <CheckCircle2 class="w-4 h-4 text-[#09090b]" />
            <span>{{ marketingStore.isSubmittingSchedule ? 'AUTENTICANDO...' : 'AGENDAR PUBLICAÇÃO' }}</span>
          </button>
        </div>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { IonPage, IonContent } from '@ionic/vue';
import { useRouter } from 'vue-router';
import {
  Sparkles,
  Calendar,
  Clock,
  LayoutGrid,
  Film,
  Share2,
  FolderCheck,
  ShieldCheck,
  CheckCircle2,
  Radio,
  ArrowLeft
} from '@lucide/vue';
import AppHeader from '../components/AppHeader.vue';
import { useMarketingStore } from '@/stores/marketing';

const router = useRouter();
const marketingStore = useMarketingStore();

const showSuccessModal = ref(false);

// Sincroniza configurações ativas de publicação (backend ou fallback mock)
onMounted(async () => {
  await marketingStore.fetchPublishSettings();
});

function handleNotifications() {
  console.log('Notificações abertas');
}

function handleProfile() {
  console.log('Perfil aberto');
}

async function switchAccount() {
  const result = await marketingStore.switchSocialAccount();
  alert(`Conta conectada alternada com sucesso para: ${result.account.handle}`);
}

async function agendarPublicacao() {
  // Dispara o agendamento com validação de backend ou fallback mock
  const result = await marketingStore.submitSchedule();
  if (result.success) {
    showSuccessModal.value = true;
  }
}

function voltarParaLegenda() {
  router.push({ name: 'LegendaCuradoria' });
}

function concluirFluxo() {
  showSuccessModal.value = false;
  marketingStore.resetToDefaults();
  router.push({ name: 'Clientes' });
}
</script>
