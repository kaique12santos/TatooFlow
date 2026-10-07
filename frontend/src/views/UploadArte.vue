<template>
  <ion-page>
    <ion-content class="bg-[#09090b] text-[#f2ebd9] select-none font-sans" fullscreen>
      <div class="min-h-screen bg-[#09090b] pb-40 max-w-[480px] mx-auto px-4 pt-3 sm:px-6">
        <!-- Reusable App Header -->
        <AppHeader
          @click-notifications="handleNotifications"
          @click-profile="handleProfile"
        />

        <!-- Step Tracker & Segmented Progress Bar -->
        <div class="mb-4 px-0.5">
          <div class="flex items-center justify-between mb-1.5">
            <span class="font-inter text-[10px] font-bold tracking-[1.2px] uppercase text-[#a39e93]">
              PASSO 1 DE 3 · UPLOAD DA ARTE
            </span>
            <span class="font-jetbrains text-[11px] font-bold text-[#e6c383]">
              33%
            </span>
          </div>

          <!-- 3-Segment Progress Bar -->
          <div class="grid grid-cols-3 gap-2">
            <div class="h-1 bg-[#c9a86a] rounded-full shadow-[0_0_8px_rgba(201,168,106,0.4)]"></div>
            <div class="h-1 bg-[#222226] rounded-full"></div>
            <div class="h-1 bg-[#222226] rounded-full"></div>
          </div>
        </div>

        <!-- Client & Station Context Header -->
        <div class="mb-5">
          <div class="flex items-start justify-between">
            <div>
              <span class="bg-[#1d1a15] border border-[rgba(201,168,106,0.3)] text-[#e6c383] font-jetbrains text-[9.5px] font-bold px-2.5 py-0.5 rounded-[2px] tracking-wider uppercase inline-block mb-2">
                BANCADA 01 • GABRIEL DORNELLES
              </span>
              <h1 class="font-playfair text-[25px] sm:text-[27px] font-semibold text-[#f2ebd9] leading-tight mb-1">
                Camila Albuquerque
              </h1>
              <p class="font-inter text-[12px] text-[#a39e93]">
                Sessão 03/03 • Estudo de São Jerônimo...
              </p>
            </div>

            <!-- Close Modal Button -->
            <button
              @click="handleClose"
              class="w-8 h-8 bg-[#141416] hover:bg-[#1f1e22] border border-[rgba(255,255,255,0.08)] rounded-[2px] flex items-center justify-center text-[#a39e93] hover:text-[#f2ebd9] transition-colors cursor-pointer shrink-0 mt-0.5"
            >
              <X class="w-4 h-4" />
            </button>
          </div>
        </div>

        <!-- SECTION 1: FOTOGRAFIA PRINCIPAL (MASTER SHOT) -->
        <div class="mb-5">
          <!-- Section Title Row -->
          <div class="flex items-center justify-between mb-3 px-0.5">
            <div class="flex items-center gap-2">
              <Camera class="w-4 h-4 text-[#c9a86a]" />
              <h2 class="font-inter text-[10.5px] font-bold tracking-[0.9px] text-[#f2ebd9] uppercase">
                FOTOGRAFIA PRINCIPAL (MASTER SHOT)
              </h2>
            </div>

            <span class="bg-[#1c1a16] border border-[rgba(201,168,106,0.3)] text-[#e6c383] font-jetbrains text-[9px] font-bold px-2 py-0.5 rounded-[2px] uppercase tracking-wide">
              OBRIGATÓRIO
            </span>
          </div>

          <!-- Main Upload Dropzone Card -->
          <div class="bg-[#141416] border border-[rgba(201,168,106,0.2)] rounded-[4px] p-4 sm:p-5 shadow-2xl relative">
            <!-- Dropzone Box -->
            <div
              @click="triggerFileInput"
              class="border border-[rgba(255,255,255,0.06)] hover:border-[rgba(201,168,106,0.35)] bg-[#0d0d0f] rounded-[2px] p-5 text-center mb-3 relative overflow-hidden group cursor-pointer transition-all"
            >
              <!-- Hidden Native Input -->
              <input
                ref="fileInputRef"
                type="file"
                accept="image/*"
                class="hidden"
                @change="handleFileSelected"
              />

              <!-- Preview State if image chosen -->
              <div v-if="marketingStore.masterShotUrl" class="relative rounded-[2px] overflow-hidden mb-2">
                <img
                  :src="marketingStore.masterShotUrl"
                  alt="Master Shot Preview"
                  class="w-full h-44 object-cover rounded-[2px]"
                />
                <div class="absolute inset-0 bg-black/40 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
                  <span class="bg-[#09090b]/90 text-[#e6c383] font-jetbrains text-[10px] px-2.5 py-1 rounded-[2px]">
                    Toque para alterar imagem
                  </span>
                </div>
              </div>

              <!-- Default Placeholder State (Faithful to Screenshot) -->
              <div v-else>
                <!-- Camera Center Icon with Badge Frame -->
                <div class="w-14 h-14 bg-[#141416] border border-[rgba(201,168,106,0.25)] rounded-[4px] mx-auto flex items-center justify-center mb-3 group-hover:border-[#c9a86a] transition-colors shadow-inner">
                  <Camera class="w-6 h-6 text-[#c9a86a]" />
                </div>

                <h3 class="font-playfair text-[20px] font-semibold text-[#f2ebd9] mb-1.5">
                  Curadoria da Imagem Central
                </h3>
                <p class="font-inter text-[11.5px] text-[#a39e93] leading-relaxed max-w-[280px] mx-auto mb-3.5">
                  Arraste a foto principal em alta definição ou toque para buscar no arquivo local
                </p>

                <!-- Format & Spec Specs Pill -->
                <div class="bg-[#09090b] border border-[rgba(201,168,106,0.2)] px-3 py-1.5 rounded-[2px] inline-flex items-center gap-1.5">
                  <Sliders class="w-3 h-3 text-[#c9a86a]" />
                  <span class="font-jetbrains text-[10px] text-[#d6b77e] font-medium">
                    RAW, JPG ou PNG até 50MB • Min. 4K
                  </span>
                </div>
              </div>
            </div>

            <!-- Upload Action Buttons (2 Columns) -->
            <div class="grid grid-cols-2 gap-2.5">
              <button
                @click="openCameraBancada"
                class="bg-[#1a1917] hover:bg-[#24211d] border border-[rgba(201,168,106,0.2)] active:scale-[0.99] text-[#f2ebd9] font-inter text-[10.5px] font-bold tracking-[0.8px] uppercase rounded-[2px] h-11 flex items-center justify-center gap-2 cursor-pointer transition-all"
              >
                <Camera class="w-4 h-4 text-[#c9a86a]" />
                <span>CÂMERA DA BANCADA</span>
              </button>

              <button
                @click="triggerFileInput"
                class="bg-[#c9a86a] hover:bg-[#d6b77e] active:scale-[0.99] text-[#09090b] font-inter text-[10.5px] font-bold tracking-[0.8px] uppercase rounded-[2px] h-11 flex items-center justify-center gap-2 shadow-md cursor-pointer transition-all"
              >
                <FolderOpen class="w-4 h-4 text-[#09090b]" />
                <span>ESCOLHER ARQUIVO</span>
              </button>
            </div>
          </div>
        </div>

        <!-- SECTION 2: ÂNGULOS ADICIONAIS & DETALHES -->
        <div class="mb-5">
          <!-- Section Title Row -->
          <div class="flex items-center justify-between mb-3 px-0.5">
            <div class="flex items-center gap-2">
              <Images class="w-4 h-4 text-[#c9a86a]" />
              <h2 class="font-inter text-[10.5px] font-bold tracking-[0.9px] text-[#f2ebd9] uppercase">
                ÂNGULOS ADICIONAIS & DETALHES
              </h2>
            </div>

            <span class="font-jetbrains text-[10px] text-[#a39e93] uppercase">
              OPCIONAL (2/4)
            </span>
          </div>

          <!-- Hidden Native Input for Additional Angle -->
          <input
            ref="angleInputRef"
            type="file"
            accept="image/*,video/*"
            class="hidden"
            @change="handleAngleSelected"
          />

          <!-- Thumbnails Deck (Cards Dinâmicos + Slot Adicionar) -->
          <div class="grid grid-cols-3 gap-2.5">
            <div
              v-for="(angle, idx) in marketingStore.additionalAngles"
              :key="angle.id"
              @click="previewDetail(idx)"
              class="relative aspect-square rounded-[2px] overflow-hidden border border-[rgba(201,168,106,0.25)] bg-[#09090b] group cursor-pointer shadow-md"
            >
              <img
                :src="angle.image"
                :alt="angle.title"
                class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
              />

              <!-- Center Play Icon if video -->
              <div v-if="angle.type === 'video'" class="absolute inset-0 flex items-center justify-center">
                <div class="w-7 h-7 rounded-full bg-[#141416]/85 border border-[#c9a86a] flex items-center justify-center text-[#c9a86a] group-hover:scale-110 transition-transform shadow-md">
                  <Play class="w-3 h-3 fill-[#c9a86a] text-[#c9a86a] ml-0.5" />
                </div>
              </div>

              <!-- Top Right Checkmark Badge if verified -->
              <div v-else class="absolute top-1.5 right-1.5 w-4 h-4 bg-[#141416]/90 border border-[#c9a86a] rounded-[1px] flex items-center justify-center">
                <Check class="w-2.5 h-2.5 text-[#c9a86a]" />
              </div>

              <!-- Bottom Label -->
              <div class="absolute bottom-1 inset-x-1 bg-black/80 backdrop-blur-xs py-0.5 text-center rounded-[1px]">
                <span class="font-jetbrains text-[8px] font-bold text-[#f2ebd9] uppercase tracking-wider block truncate">
                  {{ angle.title }}
                </span>
              </div>
            </div>

            <!-- Slot para Adicionar Novo Ângulo -->
            <div
              @click="addNewAngle"
              class="relative aspect-square rounded-[2px] border border-dashed border-[rgba(255,255,255,0.14)] hover:border-[rgba(201,168,106,0.4)] bg-[#111113] flex flex-col items-center justify-center p-2 text-center group cursor-pointer transition-colors"
            >
              <ImagePlus class="w-5 h-5 text-[#c9a86a] mb-1 group-hover:scale-110 transition-transform" />
              <span class="font-jetbrains text-[8px] font-semibold text-[#a39e93] uppercase tracking-tight block">
                NOVO ÂNGULO
              </span>
              <span class="font-inter text-[9px] text-[#c9a86a] mt-0.5 font-medium">
                + Adicionar
              </span>
            </div>
          </div>
        </div>

        <!-- SECTION 3: PRESERVAÇÃO DE PERFIL DE COR -->
        <div class="bg-[#141416] border border-[rgba(255,255,255,0.06)] rounded-[2px] p-3 flex items-center justify-between mb-4">
          <div class="flex items-center gap-2.5 min-w-0 pr-2">
            <ShieldCheck class="w-4.5 h-4.5 text-[#c9a86a] shrink-0" />
            <div class="truncate">
              <div class="font-inter text-[11px] font-bold text-[#f2ebd9] leading-tight">
                Preservação de Perfil de Cor
              </div>
              <div class="font-inter text-[10px] text-[#a39e93] truncate leading-tight mt-0.5">
                Espaço de cor DCI-P3 para fidelidade aos pigmentos
              </div>
            </div>
          </div>

          <div class="bg-[#0e2116] border border-[rgba(74,222,128,0.25)] px-2.5 py-1 rounded-[2px] shrink-0">
            <span class="font-jetbrains text-[9.5px] font-bold text-[#86efac] tracking-[1.2px] uppercase">
              ATIVO
            </span>
          </div>
        </div>

        <!-- AI NOTICE -->
        <div class="mb-4">
          <div class="flex items-center justify-center gap-2 text-center text-[#a39e93] text-[11px] font-inter px-2">
            <Sparkles class="w-3.5 h-3.5 text-[#c9a86a] shrink-0" />
            <span>Passo 2 gerará automaticamente a curadoria literária e hashtags via Gemini 1.5 Pro</span>
          </div>
        </div>

        <!-- MODAL: PREVIEW DO PASSO 2 (CURADORIA IA GEMINI 1.5 PRO) -->
        <div
          v-if="showAiModal"
          class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/85 backdrop-blur-md transition-opacity"
          @click.self="showAiModal = false"
        >
          <div class="bg-[#141416] border border-[rgba(201,168,106,0.3)] rounded-[4px] max-w-md w-full p-4 shadow-2xl relative">
            <div class="flex items-center justify-between pb-3 border-b border-[rgba(255,255,255,0.06)] mb-3">
              <div class="flex items-center gap-2">
                <Sparkles class="w-4 h-4 text-[#e6c383]" />
                <h3 class="font-playfair text-[17px] font-semibold text-[#f2ebd9]">
                  Curadoria Gemini 1.5 Pro · Passo 2
                </h3>
              </div>
              <button
                @click="showAiModal = false"
                class="text-[#a39e93] hover:text-[#f2ebd9] p-1 cursor-pointer"
              >
                <X class="w-4 h-4" />
              </button>
            </div>

            <!-- Content Generated by Gemini -->
            <div class="space-y-3 mb-4">
              <div class="bg-[#09090b] p-3 rounded-[2px] border border-[rgba(201,168,106,0.2)]">
                <span class="block font-jetbrains text-[9px] text-[#c9a86a] uppercase font-bold mb-1">
                  TÍTULO CURADO PELA IA
                </span>
                <div class="font-playfair text-[15px] font-semibold text-[#f2ebd9]">
                  {{ marketingStore.captionTitle }}
                </div>
              </div>

              <div class="bg-[#09090b] p-3 rounded-[2px] border border-[rgba(255,255,255,0.06)]">
                <span class="block font-jetbrains text-[9px] text-[#a39e93] uppercase font-bold mb-1">
                  TEXTO POÉTICO DE APRESENTAÇÃO
                </span>
                <p class="font-inter text-[12px] text-[#b8afa0] leading-relaxed italic">
                  {{ marketingStore.captionBody }}
                </p>
              </div>

              <!-- Curated Hashtags -->
              <div>
                <span class="block font-jetbrains text-[9px] text-[#c9a86a] uppercase font-bold mb-1.5">
                  HASHTAGS DE ALTO ALCANCE
                </span>
                <div class="flex flex-wrap gap-1.5">
                  <span
                    v-for="tag in marketingStore.selectedHashtags"
                    :key="tag"
                    class="bg-[#19191d] border border-[rgba(201,168,106,0.25)] text-[#e6c383] font-jetbrains text-[9px] px-2 py-0.5 rounded-[2px]"
                  >
                    {{ tag }}
                  </span>
                </div>
              </div>
            </div>

            <div class="flex gap-2">
              <button
                @click="showAiModal = false"
                class="flex-1 py-2.5 bg-[#1a1917] border border-[rgba(255,255,255,0.1)] text-[#a39e93] font-inter text-[10.5px] font-bold uppercase rounded-[2px] cursor-pointer hover:text-[#f2ebd9]"
              >
                EDITAR
              </button>
              <button
                @click="concluirCuradoria"
                class="flex-1 py-2.5 bg-[#c9a86a] text-[#09090b] font-inter text-[10.5px] font-bold tracking-[1px] uppercase rounded-[2px] cursor-pointer hover:bg-[#d6b77e]"
              >
                APROVAR E PUBLICAR
              </button>
            </div>
          </div>
        </div>

      </div>

      <!-- Barra de Ação Ancorada no Rodapé Mobile (Acima da Tab Bar) -->
      <div class="fixed bottom-[62px] left-0 right-0 z-40 bg-[#09090b]/95 backdrop-blur-md border-t border-[rgba(201,168,106,0.2)] px-4 py-2.5 shadow-[0_-8px_25px_rgba(0,0,0,0.8)]">
        <div class="max-w-[480px] mx-auto flex items-center gap-3">
          <button
            @click="handleClose"
            class="h-11 px-3.5 bg-[#141416] hover:bg-[#1a1917] border border-[rgba(201,168,106,0.3)] text-[#a39e93] hover:text-[#f2ebd9] font-inter text-[10.5px] font-bold tracking-[1px] uppercase rounded-[2px] transition-all cursor-pointer flex items-center gap-1.5 shrink-0 active:scale-95"
          >
            <X class="w-3.5 h-3.5" />
            <span>CANCELAR</span>
          </button>

          <button
            @click="avancarParaIA"
            :disabled="isUploading || marketingStore.isGeneratingAi"
            class="flex-1 h-11 bg-gradient-to-r from-[#c49f5e] via-[#d6b77e] to-[#ba9557] hover:brightness-105 active:scale-[0.99] text-[#09090b] font-inter text-[11px] font-bold tracking-[1.4px] uppercase rounded-[2px] flex items-center justify-center gap-2 shadow-[0_4px_24px_rgba(201,168,106,0.28)] transition-all cursor-pointer disabled:opacity-75"
          >
            <span>{{ isUploading ? 'ENVIANDO ARTE...' : marketingStore.isGeneratingAi ? 'PROCESSANDO IA...' : 'AVANÇAR PARA IA' }}</span>
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
import { useRouter, useRoute } from 'vue-router';
import {
  Camera,
  Sliders,
  FolderOpen,
  Images,
  Play,
  ImagePlus,
  ShieldCheck,
  Sparkles,
  ArrowRight,
  X,
  Check
} from '@lucide/vue';
import AppHeader from '../components/AppHeader.vue';
import { useMarketingStore } from '@/stores/marketing';

const router = useRouter();
const route = useRoute();
const marketingStore = useMarketingStore();

// State
const fileInputRef = ref<HTMLInputElement | null>(null);
const angleInputRef = ref<HTMLInputElement | null>(null);
const showAiModal = ref(false);
const isUploading = ref(false);

// Sincroniza sessão da bancada e fotografias do backend (ou fallback mock)
onMounted(async () => {
  const sessionId = (route.query.sessionId as string) || (route.query.id as string);
  if (typeof marketingStore.fetchArtworkSession === 'function') {
    await marketingStore.fetchArtworkSession(sessionId);
  }
});

function handleClose() {
  if (window.history.length > 1) {
    router.back();
  } else {
    router.push({ name: 'Clientes' });
  }
}

function handleNotifications() {
  console.log('Notificações abertas');
}

function handleProfile() {
  router.push('/perfil');
}

function triggerFileInput() {
  if (fileInputRef.value) {
    fileInputRef.value.click();
  }
}

async function handleFileSelected(event: Event) {
  const target = event.target as HTMLInputElement;
  if (target.files && target.files[0]) {
    const file = target.files[0];
    isUploading.value = true;
    try {
      if (typeof marketingStore.uploadMasterShotFile === 'function') {
        const result = await marketingStore.uploadMasterShotFile(file);
        console.log('[UploadArte] Foto carregada:', result.message);
      } else {
        marketingStore.setMasterShot(URL.createObjectURL(file), file);
      }
    } catch (err) {
      console.warn('[UploadArte] Falha no upload:', err);
    } finally {
      isUploading.value = false;
    }
  }
}

function openCameraBancada() {
  // Simula captura da câmera integrada na Bancada 01 com fallback mock
  marketingStore.setMasterShot('/assets/sao_jeronimo_tattoo.jpg', null);
}

function previewDetail(idx: number) {
  console.log('Visualizando detalhe', idx);
}

function addNewAngle() {
  if (angleInputRef.value) {
    angleInputRef.value.click();
  }
}

async function handleAngleSelected(event: Event) {
  const target = event.target as HTMLInputElement;
  if (target.files && target.files[0]) {
    const file = target.files[0];
    const isVideo = file.type.startsWith('video');
    if (typeof marketingStore.uploadAdditionalAngleFile === 'function') {
      await marketingStore.uploadAdditionalAngleFile(
        file,
        isVideo ? 'VÍDEO 4K (REELS)' : `ÂNGULO DETALHE 0${marketingStore.additionalAngles.length + 1}`,
        isVideo ? 'video' : 'image'
      );
    } else {
      marketingStore.addAdditionalAngle({
        title: isVideo ? 'VÍDEO 4K (REELS)' : `ÂNGULO DETALHE 0${marketingStore.additionalAngles.length + 1}`,
        image: URL.createObjectURL(file),
        type: isVideo ? 'video' : 'image',
        verified: true,
      });
    }
  }
}

function concluirCuradoria() {
  showAiModal.value = false;
  router.push({ name: 'LegendaCuradoria' });
}

async function avancarParaIA() {
  // Dispara a geração de curadoria IA conectada à store
  await marketingStore.generateAiCaption(marketingStore.selectedTone);
  router.push({ name: 'LegendaCuradoria' });
}
</script>
