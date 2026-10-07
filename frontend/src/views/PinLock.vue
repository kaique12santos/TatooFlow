<template>
  <ion-page>
    <ion-content class="bg-[#09090b] text-[#f2ebd9] select-none font-sans" fullscreen>
      <div class="min-h-screen bg-[#09090b] pb-12 max-w-[480px] mx-auto px-4 pt-6 sm:px-6 flex flex-col justify-between">
        <div>
          <!-- Top Centered Logo Header -->
          <div class="text-center mb-5">
            <div class="w-14 h-14 bg-[#000000] border border-[#b89355] rounded-[2px] p-1.5 shadow-2xl mx-auto mb-2.5 flex items-center justify-center">
              <img :src="imgLogo" alt="TattooFlow Atelier" class="w-full h-full object-contain" />
            </div>
            <span class="font-cinzel text-[13px] font-semibold text-[#d6b77e] tracking-[2.5px] uppercase block">
              TATTOOFLOW ATELIER
            </span>
            <span class="font-inter text-[9px] text-[#b8afa0] tracking-[2px] uppercase block mt-0.5">
              AUTENTICAÇÃO
            </span>
          </div>

          <!-- PIN Screen Title & Description -->
          <div class="text-center mb-5">
            <h1 class="font-playfair text-[26px] font-semibold text-[#f2ebd9] leading-tight mb-1">
              Digite seu PIN de segurança
            </h1>
            <p class="font-montserrat text-[12px] text-[#a39e93] leading-relaxed max-w-xs mx-auto">
              Acesso rápido para registro de sessão, anamnese clínica e liberação de suprimentos.
            </p>
            <div v-if="errorMessage" class="mt-2 text-[#e06c75] font-jetbrains text-[11px] font-bold tracking-wide">
              {{ errorMessage }}
            </div>
            <div v-else-if="isVerifying" class="mt-2 text-[#c9a86a] font-jetbrains text-[11px] tracking-wide animate-pulse">
              AUTENTICANDO TERMINAL...
            </div>
          </div>

          <!-- PIN Indicator Component -->
          <PinIndicator :length="pin.length" :max-length="6" />

          <!-- Numeric Keypad Component -->
          <PinKeypad @press="handleKeyPress" />

          <button @click="verifyPin" :disabled="isVerifying || pin.length < 4"
            class="w-full py-3 my-4 border border-[#b89355] text-[#e6c383] disabled:opacity-40">
            Entrar
          </button>

          <!-- Forgot PIN Link -->
          <button
            @click="forgotPin"
            class="font-inter text-[10px] font-bold text-[#a39e93] tracking-widest uppercase text-center block w-full hover:text-[#f2ebd9] transition-colors cursor-pointer mb-6"
          >
            ESQUECI MEU PIN
          </button>
        </div>

        <!-- Footer Security Meta -->
        <div class="text-center border-t border-[rgba(255,255,255,0.03)] pt-4">
          <div class="flex items-center justify-center gap-1.5 text-[#a39e93] mb-1">
            <Lock class="w-3.5 h-3.5" />
            <span class="font-inter text-[9px] font-bold tracking-[1.44px] uppercase">
              CRIPTOGRAFIA PONTA A PONTA
            </span>
          </div>
          <span class="font-jetbrains text-[9px] text-[#a39e93] tracking-wider uppercase opacity-70 block">
            ATELIER AUDIT LOG ID: #TK-8842-SEC • TERMINAL ATIVO
          </span>
        </div>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { IonPage, IonContent } from '@ionic/vue';
import { Lock } from '@lucide/vue';
import { useRouter } from 'vue-router';
import PinIndicator from '../components/PinIndicator.vue';
import PinKeypad, { type KeypadItem } from '../components/PinKeypad.vue';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();

// Logo and Valéria Moretti fallback avatar
const imgLogo = 'https://s3-alpha-sig.figma.com/img/3c73/8bb6/09078c4aab5df9f0c9aa2e5b4489ac50?Expires=1792368000&Key-Pair-Id=APKAQ4GOSFWCW27IBOMQ&Signature=SCdgUzfuAfo61MxW3nRPxZOcJqBEhQi-8pORR9hhvKMCiPgugvyqSkk5p5z~NsYx4HWlSe8d0~9xPpbGi4f0np1DwPhWBszD0Gtf2B-5-ZuCuO~lDpnfdBGO~MsVmMg7G7G~0X2qrANB-bLoTOBm7T3s7JPpz5pmaCt1bhEI3Ja4DW8T1u0TVhCJ-n~ZU3t6Hi37tnzBY1fUiCqqStql4MyyqsQSyU4~bwbhMxIZimqefof0zrJi1lvERse9cv-po2bpTcfnoftzt0aTLVsIoMurlP4ZphisFdZ2MFrErItliBLSAQkiGd7jHKFImnqew8JRbdJ2AI5CYU3ebuHZZQ__';
const pin = ref('');
const isVerifying = ref(false);
const errorMessage = ref('');

function handleKeyPress(key: KeypadItem) {
  if (isVerifying.value) return;
  errorMessage.value = '';

  if (key.type === 'digit' && key.digit) {
    if (pin.value.length < 6) {
      pin.value += key.digit;
      if (pin.value.length === 6) {
        verifyPin();
      }
    }
  } else if (key.type === 'delete') {
    pin.value = pin.value.slice(0, -1);
  } else if (key.type === 'bio') {
    errorMessage.value = 'Biometria indispon?vel. Digite seu PIN.';
  }
}

// Callback com verificação assíncrona (backend vs fallback mock local)
async function verifyPin() {
  if (isVerifying.value || pin.value.length < 4) return;
  isVerifying.value = true;
  errorMessage.value = '';

  try {
    const result = await authStore.verifyPinLock(pin.value);
    if (result.success) {
      const target = authStore.currentRole === 'admin' ? '/triagem' : '/agenda';
      router.push(target);
    } else {
      errorMessage.value = result.message || 'PIN incorreto. Tente novamente.';
      pin.value = '';
    }
  } catch (err) {
    console.warn('[PinLock] Falha na validação do PIN:', err);
    errorMessage.value = 'N?o foi poss?vel autenticar. Tente novamente.';
    pin.value = '';
  } finally {
    isVerifying.value = false;
  }
}

function forgotPin() {
  errorMessage.value = 'Solicite ao administrador a redefini??o do seu PIN.';
}
</script>
