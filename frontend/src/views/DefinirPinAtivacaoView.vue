<template>
  <ion-page>
    <ion-content class="bg-[#09090b] text-[#f2ebd9] select-none font-sans" fullscreen>
      <div class="min-h-screen bg-[#09090b] pb-28 max-w-[480px] mx-auto px-4 pt-3 sm:px-6">
        <!-- Top App Header -->
        <header class="flex items-center justify-between py-3 mb-4 border-b border-[rgba(201,168,106,0.12)]">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 bg-[#000000] border border-[#b89355] rounded-[2px] p-1 flex items-center justify-center shadow-lg">
              <img :src="imgLogo" alt="New Concept Tattoo" class="w-full h-full object-contain" />
            </div>
            <div>
              <div class="font-cinzel text-[13px] font-semibold text-[#d6b77e] tracking-[2.08px] leading-tight uppercase">
                TATTOOFLOW
              </div>
              <div class="font-inter text-[9px] text-[#b8afa0] tracking-[2.52px] leading-none uppercase mt-0.5">
                ATELIER SANCTUM
              </div>
            </div>
          </div>

          <div class="flex items-center gap-2.5">
            <button
              @click="handleNotifications"
              class="relative w-9 h-9 bg-[#141416] border border-[rgba(201,168,106,0.2)] rounded-full flex items-center justify-center text-[#b8afa0] hover:text-[#d6b77e] transition-colors cursor-pointer"
            >
              <Bell class="w-4 h-4" />
            </button>

            <button
              @click="handleProfile"
              class="w-9 h-9 bg-[#1d1a15] border border-[#b89355] rounded-[4px] flex items-center justify-center text-[#c9a86a] shadow-inner cursor-pointer hover:border-[#c9a86a] transition-colors"
            >
              <User class="w-4.5 h-4.5" />
            </button>
          </div>
        </header>

        <!-- Top Status Pill / Badge -->
        <div class="flex justify-center mb-4">
          <div class="inline-flex items-center gap-2 bg-[#102217] border border-[rgba(74,222,128,0.25)] px-3 py-1 rounded-[2px]">
            <span class="w-1.5 h-1.5 bg-[#4ade80] rounded-[1px]"></span>
            <span class="font-inter text-[9px] font-bold text-[#86efac] tracking-[1.44px] uppercase">
              TERMINAL 04 • AUTENTICAÇÃO ATUALIZADA
            </span>
          </div>
        </div>

        <!-- Lock Icon with Clock Badge Box -->
        <div class="w-13 h-13 bg-[#09090b] border border-[rgba(201,168,106,0.3)] rounded-[2px] p-2.5 shadow-2xl mx-auto mb-3 flex items-center justify-center relative text-[#e6c383]">
          <!-- Composed Lock & Clock Icon -->
          <svg class="w-6 h-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
            <path d="M7 11V7a5 5 0 0 1 10 0v4" />
            <circle cx="15.5" cy="16.5" r="4.5" fill="#09090b" stroke="#e6c383" stroke-width="1.75" />
            <polyline points="15.5 14.5 15.5 16.5 17 17.5" stroke="#e6c383" stroke-width="1.5" />
          </svg>
        </div>

        <!-- Screen Title & Description -->
        <div class="text-center mb-5">
          <h1 class="font-playfair text-[26px] font-semibold text-[#f2ebd9] leading-tight mb-1.5">
            PIN Temporário Ativado
          </h1>
          <p class="font-montserrat text-[12px] text-[#a39e93] leading-relaxed max-w-[340px] mx-auto">
            Sua credencial de terminal de bancada foi redefinida com sucesso. Utilize o código abaixo para liberação imediata da estação.
          </p>
        </div>

        <!-- Card 1: Chave Provisória de Acesso Rápido -->
        <div class="bg-[#141416] rounded-[2px] p-4 border border-[rgba(201,168,106,0.22)] shadow-xl mb-4">
          <!-- Card Header Row -->
          <div class="flex items-center justify-between">
            <span class="font-inter text-[10px] font-bold text-[#e6c383] tracking-[1.44px] uppercase">
              CHAVE PROVISÓRIA DE ACESSO RÁPIDO
            </span>

            <div class="inline-flex items-center gap-1 bg-[#102217] border border-[rgba(74,222,128,0.3)] text-[#4ade80] px-2 py-0.5 rounded-[2px] font-inter text-[9px] font-bold tracking-wider uppercase">
              <Zap class="w-3 h-3 fill-current" />
              <span>ATIVO</span>
            </div>
          </div>

          <!-- Code Display Ingot -->
          <div class="bg-[#09090b] border border-[rgba(255,255,255,0.06)] rounded-[2px] py-4.5 px-4 text-center my-3.5 shadow-inner">
            <span class="font-inter text-[9px] font-bold text-[#a39e93] tracking-[1.44px] uppercase block mb-1">
              CÓDIGO DE DESBLOQUEIO
            </span>

            <div class="font-jetbrains text-[32px] sm:text-[34px] font-bold text-[#f2ebd9] tracking-[4px] my-1 select-all">
              {{ pinCode }}
            </div>

            <button
              @click="copyPin"
              class="inline-flex items-center gap-2 bg-[#141416] hover:bg-[#1a1a1f] border border-[rgba(201,168,106,0.25)] text-[#f2ebd9] hover:text-[#e6c383] font-inter text-[10px] font-bold tracking-[1.2px] uppercase px-4 py-2 rounded-[2px] transition-all cursor-pointer active:scale-95 shadow-sm mt-1.5"
            >
              <Check v-if="copied" class="w-3.5 h-3.5 text-[#4ade80]" />
              <Copy v-else class="w-3.5 h-3.5 text-[#e6c383]" />
              <span>{{ copied ? 'PIN COPIADO!' : 'COPIAR PIN' }}</span>
            </button>
          </div>

          <!-- Expiration Progress & Status -->
          <div>
            <div class="flex items-center justify-between text-[#a39e93] mb-1.5">
              <div class="flex items-center gap-1.5">
                <Clock class="w-3.5 h-3.5" />
                <span class="font-inter text-[10px] font-bold tracking-widest uppercase">EXPIRA EM</span>
              </div>
              <span class="font-jetbrains text-[15px] font-bold text-[#e6c383] tracking-wider">
                {{ formattedTime }}
              </span>
            </div>

            <!-- Progress Bar -->
            <div class="w-full h-1 bg-[#222126] rounded-full overflow-hidden my-2">
              <div
                class="h-full bg-[#c9a86a] transition-all duration-1000"
                :style="{ width: progressPercentage + '%' }"
              ></div>
            </div>

            <div class="flex items-center justify-between pt-1">
              <span class="font-montserrat text-[11px] text-[#a39e93]">
                Janela de troca obrigatória
              </span>
              <div class="flex items-center gap-1 font-inter text-[10px] text-[#c9a86a]">
                <RefreshCw class="w-3 h-3 text-[#c9a86a]" />
                <span>Sincronizado na Bancada 01</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Card 2: Bancada & Operadora Autorizada -->
        <div class="bg-[#141416] rounded-[2px] p-3.5 border border-[rgba(255,255,255,0.06)] shadow-xl mb-4">
          <!-- Hero Studio Photo -->
          <div class="relative w-full h-44 rounded-[2px] overflow-hidden border border-[rgba(255,255,255,0.06)] mb-3.5 bg-[#09090b]">
            <img :src="valeriaStudioImage" alt="Valéria Moretti na Bancada 01" class="w-full h-full object-cover" />

            <!-- Bottom Left Badge -->
            <div class="absolute bottom-2.5 left-2.5 bg-[rgba(9,9,11,0.88)] backdrop-blur-md px-2.5 py-1 rounded-[2px] border border-[rgba(255,255,255,0.12)] flex items-center gap-1.5 shadow-md">
              <span class="w-1.5 h-1.5 bg-[#4ade80] rounded-[1px]"></span>
              <span class="font-inter text-[9px] font-bold text-[#f2ebd9] tracking-[1.2px] uppercase">
                BANCADA 01 EM ESPERA
              </span>
            </div>

            <!-- Bottom Right Badge -->
            <div class="absolute bottom-2.5 right-2.5 bg-[rgba(9,9,11,0.88)] backdrop-blur-md px-2 py-1 rounded-[2px] border border-[rgba(255,255,255,0.12)] shadow-md">
              <span class="font-inter text-[9px] font-bold text-[#a39e93] tracking-[1.2px] uppercase">
                STATION-READY
              </span>
            </div>
          </div>

          <!-- Artist Row -->
          <div class="flex items-center justify-between">
            <div>
              <span class="font-inter text-[9px] font-bold text-[#c9a86a] tracking-[1.44px] uppercase block mb-0.5">
                OPERADORA AUTORIZADA
              </span>
              <h3 class="font-playfair text-[18px] font-semibold text-[#f2ebd9] leading-tight">
                {{ artistName }}
              </h3>
              <p class="font-montserrat text-[11px] text-[#a39e93] mt-0.5">
                {{ artistRole }} • {{ artistSpecialty }}
              </p>
            </div>

            <!-- Biometric Fingerprint Box Button -->
            <button
              @click="handleBiometric"
              class="w-10 h-10 bg-[#09090b] border border-[rgba(201,168,106,0.3)] rounded-[2px] flex items-center justify-center text-[#c9a86a] hover:border-[#c9a86a] transition-all cursor-pointer active:scale-95 shadow-md"
            >
              <Fingerprint class="w-5 h-5" />
            </button>
          </div>
        </div>

        <!-- Card 3: Disparo via WhatsApp Concluído -->
        <div class="bg-[#141416] rounded-[2px] p-3.5 border border-[rgba(255,255,255,0.06)] shadow-xl mb-4 flex items-start gap-3">
          <div class="w-9 h-9 bg-[#09090b] border border-[rgba(255,255,255,0.06)] rounded-[2px] flex items-center justify-center text-[#a39e93] shrink-0">
            <MessageSquare class="w-4 h-4" />
          </div>
          <div>
            <span class="font-inter text-[10px] font-bold text-[#f2ebd9] tracking-[1.2px] uppercase block mb-0.5">
              DISPARO VIA WHATSAPP CONCLUÍDO
            </span>
            <p class="font-montserrat text-[11px] text-[#a39e93] leading-relaxed">
              Comprovante com hash criptográfica enviado para <span class="text-[#f2ebd9] font-medium">+55 11 98765-****</span>.
            </p>
          </div>
        </div>

        <!-- Card 4: Diretriz de Segurança & Próximo Passo -->
        <div class="bg-[#141416] rounded-[2px] p-4 border border-[rgba(201,168,106,0.18)] shadow-xl mb-5">
          <div class="flex items-center gap-1.5 text-[#e6c383] mb-2">
            <Shield class="w-3.5 h-3.5 text-[#e6c383]" />
            <span class="font-inter text-[10px] font-bold tracking-[1.44px] uppercase">
              DIRETRIZ DE SEGURANÇA & PRÓXIMO PASSO
            </span>
          </div>

          <p class="font-montserrat text-[11px] text-[#a39e93] leading-relaxed mb-3.5">
            Ao inserir este PIN na <strong class="text-[#f2ebd9] font-medium">Bancada 01</strong>, o terminal solicitará instantaneamente o cadastro de um PIN definitivo de 6 dígitos intransferível para liberação das agulhas digitais e ficha clínica.
          </p>

          <div class="flex items-center justify-between pt-2.5 border-t border-[rgba(255,255,255,0.04)]">
            <span class="font-inter text-[9px] font-bold text-[#a39e93] tracking-widest uppercase">
              HASH DA SESSÃO
            </span>
            <span class="bg-[#09090b] border border-[rgba(255,255,255,0.06)] text-[#eedaa2] font-jetbrains text-[10px] font-semibold px-2 py-0.5 rounded-[2px] uppercase tracking-wider">
              #SEC-TOKEN-88319-NC
            </span>
          </div>
        </div>

        <!-- Action Buttons -->
        <div class="space-y-3 mb-6">
          <!-- Primary CTA Button -->
          <button
            @click="accessTerminal"
            class="w-full bg-[#c9a86a] hover:bg-[#d6b77e] text-[#09090b] font-inter text-[11px] font-bold tracking-[1.44px] uppercase py-3.5 px-4 rounded-[2px] shadow-xl flex items-center justify-center gap-2 transition-all cursor-pointer active:scale-[0.99]"
          >
            <LogIn class="w-4 h-4 stroke-[2.5]" />
            <span>ACESSAR TERMINAL COM NOVO PIN</span>
          </button>

          <!-- Secondary CTA Button -->
          <button
            @click="resendWhatsapp"
            class="w-full bg-[#141416] hover:bg-[#1a1a1e] border border-[rgba(255,255,255,0.1)] text-[#f2ebd9] hover:text-[#e6c383] font-inter text-[11px] font-bold tracking-[1.44px] uppercase py-3.5 px-4 rounded-[2px] flex items-center justify-center gap-2 transition-all cursor-pointer active:scale-[0.99] shadow-sm"
          >
            <Send class="w-4 h-4 text-[#e6c383]" />
            <span>REENVIAR POR WHATSAPP</span>
          </button>
        </div>

        <!-- Security Protocol Footer -->
        <div class="text-center mb-6">
          <div class="flex items-center justify-center gap-1.5 text-[#a39e93] mb-1">
            <ShieldCheck class="w-3.5 h-3.5 text-[#e6c383]" />
            <span class="font-inter text-[9px] font-bold tracking-[1.2px] uppercase">
              ENCRIPTADO E2EE • ATELIER AUDIT PROTOCOL 4.2
            </span>
          </div>
          <span class="font-jetbrains text-[9px] text-[#a39e93] opacity-60 tracking-wider uppercase block">
            Log ID: 77b9-8fa0-c04 • TattooFlow Atelier Sanctum
          </span>
        </div>
      </div>

     
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { IonPage, IonContent } from '@ionic/vue';
import {
  Bell,
  User,
  Zap,
  Copy,
  Check,
  Clock,
  RefreshCw,
  Fingerprint,
  MessageSquare,
  Shield,
  ShieldCheck,
  LogIn,
  Send,
  LayoutGrid,
  Palette,
  PenTool,
  Gem,
} from '@lucide/vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();

// Brand & Studio Assets
const imgLogo =
  'https://s3-alpha-sig.figma.com/img/3c73/8bb6/09078c4aab5df9f0c9aa2e5b4489ac50?Expires=1792368000&Key-Pair-Id=APKAQ4GOSFWCW27IBOMQ&Signature=SCdgUzfuAfo61MxW3nRPxZOcJqBEhQi-8pORR9hhvKMCiPgugvyqSkk5p5z~NsYx4HWlSe8d0~9xPpbGi4f0np1DwPhWBszD0Gtf2B-5-ZuCuO~lDpnfdBGO~MsVmMg7G7G~0X2qrANB-bLoTOBm7T3s7JPpz5pmaCt1bhEI3Ja4DW8T1u0TVhCJ-n~ZU3t6Hi37tnzBY1fUiCqqStql4MyyqsQSyU4~bwbhMxIZimqefof0zrJi1lvERse9cv-po2bpTcfnoftzt0aTLVsIoMurlP4ZphisFdZ2MFrErItliBLSAQkiGd7jHKFImnqew8JRbdJ2AI5CYU3ebuHZZQ__';

const valeriaStudioImage =
  'https://s3-alpha-sig.figma.com/img/d350/7974/b8053e45bab384d26da949240ac51daf?Expires=1792368000&Key-Pair-Id=APKAQ4GOSFWCW27IBOMQ&Signature=c4nOMCaL58JpcHdWsqACkO1KKSFnCcg-jmIYgxoqZUPbM~dLCCz6SQ-RypE7g~E0IuuYAMRJTg9U7M6dWUl~bVEYV5Lwzr-Ld-Y2w4NNvFa743FpUKD2l2IGOBicVk2YdtwfsQJOqrEnGLdDU38qf0u18D4jLAJTC8rFxyWOol19frRfhlUEEYzcTzFPo5~EPawcWkgMlCyLQTxdFEPVrWPcK2Uq4o7H72FlPfMR3XO6gPyuBJhaz5vZ3RBeYeDPnXopf-er17mZ2YhMmoSbbPQH6KE3NfCe~npSB2nay078pwwk-HfligiPfKNJ0Pj5t~-zy7JG5cnQdpTuhXgtDA__';

const token = computed(() => (route.query.token as string) || '');
const queryPin = computed(() => (route.query.pin as string) || '');

// Sincroniza dados da credencial temporária via backend com fallback mock local
onMounted(async () => {
  await authStore.fetchActivationProfile(token.value);
  if (authStore.activationProfile.remainingSeconds) {
    remainingSeconds.value = authStore.activationProfile.remainingSeconds;
  }
});

// Artist Details reativos vindos da store (backend ou mock)
const artistName = computed(() => authStore.activationProfile.name);
const artistRole = computed(() => authStore.activationProfile.role);
const artistSpecialty = computed(() => authStore.activationProfile.specialty);

// PIN temporário reativo
const pinCode = computed(() => queryPin.value || authStore.activationProfile.temporaryPin || '749 • 382');
const copied = ref(false);

function copyPin() {
  const rawPin = pinCode.value.replace(/\D/g, '');
  navigator.clipboard?.writeText(rawPin).catch(() => {});
  copied.value = true;
  setTimeout(() => {
    copied.value = false;
  }, 2500);
}

// Countdown Timer
const totalSeconds = 15 * 60; // 15 minutes window
const remainingSeconds = ref(14 * 60 + 53); // 14:53
let timerInterval: ReturnType<typeof setInterval> | null = null;

const formattedTime = computed(() => {
  const m = Math.floor(remainingSeconds.value / 60);
  const s = remainingSeconds.value % 60;
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
});

const progressPercentage = computed(() => {
  return Math.max(0, Math.min(100, (remainingSeconds.value / totalSeconds) * 100));
});

onMounted(() => {
  timerInterval = setInterval(() => {
    if (remainingSeconds.value > 0) {
      remainingSeconds.value--;
    }
  }, 1000);
});

onUnmounted(() => {
  if (timerInterval) clearInterval(timerInterval);
});

// Interactive Handlers
function handleNotifications() {
  console.log('Notifications opened');
}

function handleProfile() {
  console.log('Profile opened');
}

function handleBiometric() {
  alert(`Biometria validada com sucesso para ${artistName.value}. Terminal liberado para configuração.`);
}

function accessTerminal() {
  router.push({
    path: '/definir-pin',
    query: {
      token: token.value,
      pin: pinCode.value,
    },
  });
}

async function resendWhatsapp() {
  // Callback com verificação condicional (backend se configurado vs simulação local)
  const result = await authStore.resendTemporaryPin(token.value);
  alert(result.message);
}

// Bottom Navigation
const activeTab = ref('sanctum');

const bottomNavItems = [
  { id: 'gallery', label: 'GALLERY', icon: LayoutGrid, path: '/agenda' },
  { id: 'concepts', label: 'CONCEPTS', icon: Palette, path: '/convites' },
  { id: 'masters', label: 'MASTERS', icon: PenTool, path: '/performance-artistas' },
  { id: 'sanctum', label: 'SANCTUM', icon: Gem, path: '/definir-pin-ativacao' },
];

function navigateTab(item: typeof bottomNavItems[0]) {
  activeTab.value = item.id;
  if (item.path) {
    router.push(item.path).catch(() => {});
  }
}
</script>
