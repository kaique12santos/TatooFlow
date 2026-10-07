<template>
  <ion-page>
    <ion-content class="bg-[#09090b] text-[#f2ebd9] select-none font-sans" fullscreen>
      <div class="min-h-screen bg-[#09090b] pb-32 max-w-[480px] mx-auto px-4 pt-3 sm:px-6">
        <!-- Reusable Top App Header -->
        <AppHeader
          @click-notifications="handleNotifications"
          @click-profile="handleProfile"
        />

        <!-- Step Tracker & Segmented Progress Bar (Passo 2 de 3 · 66%) -->
        <div class="mb-4 px-0.5">
          <div class="flex items-center justify-between mb-1.5">
            <span class="font-inter text-[10px] font-bold tracking-[1.2px] uppercase text-[#a39e93]">
              PASSO 2 DE 3 · LEGENDA
            </span>
            <span class="font-jetbrains text-[11px] font-bold text-[#e6c383]">
              66%
            </span>
          </div>

          <!-- 3-Segment Progress Bar -->
          <div class="grid grid-cols-3 gap-2">
            <div class="h-1 bg-[#c9a86a] rounded-full shadow-[0_0_8px_rgba(201,168,106,0.4)]"></div>
            <div class="h-1 bg-[#c9a86a] rounded-full shadow-[0_0_8px_rgba(201,168,106,0.4)]"></div>
            <div class="h-1 bg-[#222226] rounded-full"></div>
          </div>
        </div>

        <!-- AI Analysis Header Card (Gemini Vision 1.5 Pro) -->
        <div class="bg-[#141416] border border-[rgba(201,168,106,0.18)] rounded-[4px] p-3.5 sm:p-4 shadow-xl mb-4">
          <!-- Top Status Row -->
          <div class="flex items-center justify-between mb-3">
            <div class="flex items-center gap-2">
              <BadgeCheck class="w-4.5 h-4.5 text-[#c9a86a]" />
              <span class="font-jetbrains text-[11px] font-bold tracking-[1.4px] text-[#e6c383] uppercase">
                GEMINI VISION 1.5 PRO
              </span>
            </div>

            <div class="bg-[#102619] border border-[#2d6342] px-2 py-0.5 rounded-[2px]">
              <span class="font-jetbrains text-[9px] font-bold text-[#86efac] tracking-wide uppercase">
                Análise Concluída
              </span>
            </div>
          </div>

          <!-- Artwork Recognition Row -->
          <div class="flex items-center gap-3">
            <div class="w-14 h-14 rounded-[2px] overflow-hidden border border-[rgba(255,255,255,0.08)] bg-[#09090b] shrink-0">
              <img
                :src="marketingStore.masterShotUrl || '/assets/sao_jeronimo_tattoo.jpg'"
                alt="São Jerônimo"
                class="w-full h-full object-cover"
              />
            </div>

            <div class="flex-1 min-w-0">
              <div class="font-jetbrains text-[10px] font-bold text-[#e6c383] tracking-wide uppercase truncate">
                ▨ {{ marketingStore.clientContext.artworkStyle }}
              </div>
              <p class="font-inter text-[11.5px] text-[#a39e93] leading-relaxed mt-0.5 truncate">
                {{ marketingStore.clientContext.artworkTitle }}
              </p>
            </div>
          </div>
        </div>

        <!-- Section: Calibração de Tom Literário -->
        <div class="mb-5">
          <span class="block font-inter text-[9px] font-bold tracking-[1.4px] text-[#a39e93] uppercase mb-2.5 px-0.5">
            CALIBRAÇÃO DE TOM LITERÁRIO
          </span>

          <!-- Tone Options Grid -->
          <div class="grid grid-cols-3 gap-2 mb-3.5">
            <!-- Option 1: Chiaroscuro -->
            <button
              @click="selectTone('chiaroscuro')"
              :class="[
                'p-2.5 rounded-[2px] text-left transition-all cursor-pointer border',
                marketingStore.selectedTone === 'chiaroscuro'
                  ? 'bg-[#19191d] border-[rgba(201,168,106,0.6)] shadow-md'
                  : 'bg-[#0e0e11] border-[rgba(255,255,255,0.06)] hover:border-[rgba(201,168,106,0.25)]'
              ]"
            >
              <div class="flex items-center justify-between mb-0.5">
                <span class="font-inter text-[10px] font-bold text-[#f2ebd9] uppercase">
                  CHIAROSCURO
                </span>
                <Check v-if="marketingStore.selectedTone === 'chiaroscuro'" class="w-3 h-3 text-[#c9a86a]" />
              </div>
              <span class="font-inter text-[8.5px] text-[#a39e93]">
                Artístico & Denso
              </span>
            </button>

            <!-- Option 2: Solene -->
            <button
              @click="selectTone('solene')"
              :class="[
                'p-2.5 rounded-[2px] text-left transition-all cursor-pointer border',
                marketingStore.selectedTone === 'solene'
                  ? 'bg-[#19191d] border-[rgba(201,168,106,0.6)] shadow-md'
                  : 'bg-[#0e0e11] border-[rgba(255,255,255,0.06)] hover:border-[rgba(201,168,106,0.25)]'
              ]"
            >
              <div class="flex items-center justify-between mb-0.5">
                <span class="font-inter text-[10px] font-bold text-[#a39e93] uppercase">
                  SOLENE
                </span>
                <Check v-if="marketingStore.selectedTone === 'solene'" class="w-3 h-3 text-[#c9a86a]" />
              </div>
              <span class="font-inter text-[8.5px] text-[#6d685e]">
                Notarial Histórico
              </span>
            </button>

            <!-- Option 3: Story -->
            <button
              @click="selectTone('story')"
              :class="[
                'p-2.5 rounded-[2px] text-left transition-all cursor-pointer border',
                marketingStore.selectedTone === 'story'
                  ? 'bg-[#19191d] border-[rgba(201,168,106,0.6)] shadow-md'
                  : 'bg-[#0e0e11] border-[rgba(255,255,255,0.06)] hover:border-[rgba(201,168,106,0.25)]'
              ]"
            >
              <div class="flex items-center justify-between mb-0.5">
                <span class="font-inter text-[10px] font-bold text-[#a39e93] uppercase">
                  STORY
                </span>
                <Check v-if="marketingStore.selectedTone === 'story'" class="w-3 h-3 text-[#c9a86a]" />
              </div>
              <span class="font-inter text-[8.5px] text-[#6d685e]">
                Memória & Afeto
              </span>
            </button>
          </div>

          <!-- Generate Caption Button -->
          <button
            @click="regenerateCaption"
            :disabled="marketingStore.isGeneratingAi"
            class="w-full h-11 bg-gradient-to-r from-[#c49f5e] via-[#d6b77e] to-[#ba9557] hover:brightness-105 active:scale-[0.99] text-[#09090b] font-inter text-[11px] font-bold tracking-[1.4px] uppercase rounded-[2px] flex items-center justify-center gap-2 shadow-[0_4px_20px_rgba(201,168,106,0.25)] transition-all cursor-pointer disabled:opacity-70"
          >
            <Sparkles :class="['w-4 h-4 text-[#09090b]', marketingStore.isGeneratingAi ? 'animate-spin' : '']" />
            <span>{{ marketingStore.isGeneratingAi ? 'REFINANDO VIA GEMINI...' : 'GERAR LEGENDA VIA GEMINI' }}</span>
          </button>
        </div>

        <!-- Section: Legenda Notarial -->
        <div class="bg-[#141416] border border-[rgba(201,168,106,0.18)] rounded-[4px] p-4 shadow-xl mb-5">
          <!-- Header Row -->
          <div class="flex items-start justify-between mb-2">
            <div>
              <span class="font-jetbrains text-[9px] font-bold tracking-[1.2px] text-[#c9a86a] uppercase block mb-0.5">
                {{ marketingStore.captionTitle }}
              </span>
              <h2 class="font-playfair text-[20px] font-semibold text-[#f2ebd9] leading-tight">
                Legenda Notarial
              </h2>
            </div>

            <!-- Action links: Copiar & Regenerar -->
            <div class="flex items-center gap-3 pt-1">
              <button
                @click="copyCaption"
                class="flex items-center gap-1 font-inter text-[10.5px] text-[#a39e93] hover:text-[#f2ebd9] transition-colors cursor-pointer"
              >
                <Copy class="w-3.5 h-3.5 text-[#a39e93]" />
                <span>{{ copied ? 'Copiado!' : 'Copiar' }}</span>
              </button>

              <button
                @click="regenerateCaption"
                class="flex items-center gap-1 font-inter text-[10.5px] text-[#a39e93] hover:text-[#f2ebd9] transition-colors cursor-pointer"
              >
                <RefreshCw :class="['w-3.5 h-3.5 text-[#a39e93]', marketingStore.isGeneratingAi ? 'animate-spin' : '']" />
                <span>Regenerar</span>
              </button>
            </div>
          </div>

          <!-- Main Caption Container Box (Reativo & Editável) -->
          <div class="border border-[rgba(201,168,106,0.22)] bg-[#0c0c0e] rounded-[2px] p-3 mb-3 relative font-inter text-[12px] text-[#f2ebd9] leading-relaxed">
            <textarea
              v-model="marketingStore.captionBody"
              rows="5"
              class="w-full bg-transparent text-[#f2ebd9] font-inter text-[12px] leading-relaxed border-none outline-none resize-none focus:ring-0"
              placeholder="Refine a curadoria literária da obra..."
            ></textarea>
          </div>

          <!-- Instagram Ready & Character Counter Row -->
          <div class="flex items-center justify-between text-[11px] pt-1">
            <div class="flex items-center gap-1.5 text-[#f2ebd9]">
              <Bookmark class="w-3.5 h-3.5 text-[#c9a86a]" />
              <span class="font-inter font-bold text-[9.5px] tracking-[0.8px] uppercase">
                PRONTO PARA INSTAGRAM FEED
              </span>
            </div>

            <div class="font-jetbrains text-[11px]">
              <span class="text-[#e6c383] font-bold">{{ marketingStore.fullFormattedCaption.length }}</span>
              <span class="text-[#a39e93]"> / 2.200 carac.</span>
            </div>
          </div>
        </div>

        <!-- Section: Hashtags Curadas -->
        <div class="mb-5 px-0.5">
          <div class="flex items-start justify-between mb-3">
            <div>
              <h2 class="font-playfair text-[20px] font-semibold text-[#f2ebd9] leading-tight">
                Hashtags Curadas
              </h2>
              <p class="font-inter text-[11px] text-[#a39e93] mt-0.5">
                {{ marketingStore.selectedHashtags.length }} Selecionadas • (Recomendação Atelier: 8 a 15)
              </p>
            </div>

            <span class="font-jetbrains text-[18px] text-[#c9a86a] font-bold">
              #
            </span>
          </div>

          <!-- Tag Cloud Deck -->
          <div class="flex flex-wrap gap-2">
            <button
              v-for="tag in marketingStore.hashtagsList"
              :key="tag"
              @click="marketingStore.toggleHashtag(tag)"
              :class="[
                'px-2.5 py-1.5 rounded-[2px] font-jetbrains text-[10px] flex items-center gap-1 transition-all cursor-pointer border',
                marketingStore.selectedHashtags.includes(tag)
                  ? 'bg-[#161619] border-[rgba(201,168,106,0.35)] text-[#f2ebd9] shadow-sm'
                  : 'bg-[#09090b] border-[rgba(255,255,255,0.06)] text-[#6d685e]'
              ]"
            >
              <span>{{ tag }}</span>
              <Check v-if="marketingStore.selectedHashtags.includes(tag)" class="w-3 h-3 text-[#c9a86a]" />
            </button>
          </div>

          <!-- Add Custom Hashtag -->
          <button
            @click="addCustomHashtag"
            class="flex items-center gap-1 font-inter font-bold text-[10px] tracking-[1px] text-[#c9a86a] hover:text-[#e6c383] uppercase mt-3 cursor-pointer transition-colors"
          >
            <Plus class="w-3.5 h-3.5 text-[#c9a86a]" />
            <span>ADICIONAR HASHTAG PERSONALIZADA</span>
          </button>
        </div>

      </div>

      <!-- Barra de Ação Ancorada no Rodapé Mobile -->
      <div class="fixed bottom-0 left-0 right-0 z-40 bg-[#09090b]/95 backdrop-blur-md border-t border-[rgba(201,168,106,0.2)] px-4 py-3 shadow-[0_-8px_25px_rgba(0,0,0,0.8)]">
        <div class="max-w-[480px] mx-auto flex items-center gap-3">
          <button
            @click="voltarParaUpload"
            class="h-11 px-4 bg-[#141416] hover:bg-[#1a1917] border border-[rgba(201,168,106,0.3)] text-[#a39e93] hover:text-[#f2ebd9] font-inter text-[11px] font-bold tracking-[1px] uppercase rounded-[2px] transition-all cursor-pointer flex items-center gap-1.5 shrink-0 active:scale-95"
          >
            <ArrowLeft class="w-3.5 h-3.5" />
            <span>VOLTAR</span>
          </button>

          <button
            @click="avancarParaPublicacao"
            class="flex-1 h-11 bg-gradient-to-r from-[#c49f5e] via-[#d6b77e] to-[#ba9557] hover:brightness-105 active:scale-[0.99] text-[#09090b] font-inter text-[11px] font-bold tracking-[1.4px] uppercase rounded-[2px] flex items-center justify-center gap-2 shadow-[0_4px_24px_rgba(201,168,106,0.28)] transition-all cursor-pointer"
          >
            <span>AVANÇAR PARA PUBLICAÇÃO</span>
            <ArrowRight class="w-4 h-4 text-[#09090b]" />
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
  BadgeCheck,
  Check,
  Sparkles,
  Copy,
  RefreshCw,
  Bookmark,
  Plus,
  ArrowRight,
  ArrowLeft
} from '@lucide/vue';
import AppHeader from '../components/AppHeader.vue';
import { useMarketingStore } from '@/stores/marketing';

const router = useRouter();
const marketingStore = useMarketingStore();

const copied = ref(false);

// Sincroniza rascunho de curadoria da obra do backend com fallback mock
onMounted(async () => {
  await marketingStore.fetchCuradoria();
});

function handleNotifications() {
  console.log('Notificações abertas');
}

function handleProfile() {
  router.push('/perfil');
}

async function selectTone(tone: 'chiaroscuro' | 'solene' | 'story') {
  await marketingStore.generateAiCaption(tone);
}

async function regenerateCaption() {
  await marketingStore.generateAiCaption(marketingStore.selectedTone);
}

function copyCaption() {
  if (navigator.clipboard) {
    navigator.clipboard.writeText(marketingStore.fullFormattedCaption);
  }
  copied.value = true;
  setTimeout(() => {
    copied.value = false;
  }, 2000);
}

function addCustomHashtag() {
  const custom = prompt('Digite a nova hashtag (ex: #TattooFlow):');
  if (custom) {
    marketingStore.addCustomHashtag(custom);
  }
}

function avancarParaPublicacao() {
  router.push({ name: 'PublicacaoDistribuicao' });
}

function voltarParaUpload() {
  router.push({ name: 'UploadArte' });
}
</script>
