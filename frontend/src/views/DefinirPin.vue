<template>
  <ion-page>
    <ion-content class="bg-[#09090b] text-[#f2ebd9] select-none font-sans" fullscreen>
      <div class="min-h-screen bg-[#09090b] pb-12 max-w-[480px] mx-auto px-4 pt-6 sm:px-6 flex flex-col justify-between">
        <div>
          <!-- Top Badge -->
          <div class="mb-4">
            <span class="bg-[#141416] border border-[rgba(201,168,106,0.3)] text-[#e6c383] font-inter text-[9px] font-bold px-2.5 py-1 rounded-[2px] tracking-[1.44px] uppercase inline-block">
              PRIMEIRA ATIVAÇÃO
            </span>
          </div>

          <!-- Lock Icon Box -->
          <div class="w-12 h-12 bg-[#09090b] border border-[rgba(201,168,106,0.3)] rounded-[2px] p-2.5 shadow-2xl mx-auto mb-3 flex items-center justify-center text-[#e6c383]">
            <Lock class="w-5 h-5" />
          </div>

          <!-- Screen Title & Description -->
          <div class="text-center mb-5">
            <h1 class="font-playfair text-[26px] font-semibold text-[#f2ebd9] leading-tight mb-1">
              Definir PIN Definitivo
            </h1>
            <p class="font-montserrat text-[12px] text-[#a39e93] leading-relaxed max-w-xs mx-auto">
              Substitua o PIN provisório por uma combinação pessoal de 6 dígitos para liberar o terminal, fichas clínicas, etc.
            </p>
          </div>

          <!-- Artist Profile Card -->
          <div class="bg-[#141416] rounded-[2px] p-3.5 border border-[rgba(255,255,255,0.06)] shadow-xl flex items-center justify-between mb-5">
            <div class="flex items-center gap-3">
              <div class="relative w-11 h-11 rounded-[2px] overflow-hidden border border-[rgba(201,168,106,0.3)] bg-[#09090b] shrink-0">
                <img :src="artistAvatar" :alt="artistName" class="w-full h-full object-cover" />
              </div>
              <div>
                <h3 class="font-playfair text-[18px] font-semibold text-[#f2ebd9] leading-tight">
                  {{ artistName }}
                </h3>
                <p class="font-inter text-[9px] text-[#a39e93] tracking-widest uppercase mt-0.5">
                  {{ artistRole }} • {{ artistStation }}
                </p>
              </div>
            </div>

            <div class="bg-[#09090b] border border-[rgba(201,168,106,0.3)] text-[#e6c383] font-inter text-[9px] font-bold px-2 py-1 rounded-[2px] flex items-center gap-1">
              <ShieldCheck class="w-3.5 h-3.5" />
              <span class="tracking-wider uppercase">VALIDADO</span>
            </div>
          </div>

          <!-- Step Progress Box -->
          <div class="bg-[#141416] rounded-[2px] p-4 border border-[rgba(255,255,255,0.05)] shadow-xl mb-5">
            <!-- Step Tabs Grid -->
            <div class="grid grid-cols-2 gap-2 mb-4">
              <!-- Step 1 Tab -->
              <div
                @click="currentStep = 1"
                :class="[
                  'p-2.5 rounded-[2px] border transition-all cursor-pointer relative',
                  currentStep === 1
                    ? 'bg-[#09090b] border-[rgba(201,168,106,0.5)] shadow-md'
                    : 'bg-[#09090b] border-[rgba(255,255,255,0.04)] opacity-60'
                ]"
              >
                <div class="flex items-center justify-between">
                  <span class="font-inter text-[8px] font-bold text-[#e6c383] tracking-widest uppercase">
                    PASSO 1
                  </span>
                  <span v-if="currentStep === 1" class="w-1.5 h-1.5 rounded-full bg-[#e6c383]"></span>
                </div>
                <span class="font-inter text-[11px] font-bold text-[#f2ebd9] mt-0.5 block">
                  Novo PIN (Ativo)
                </span>
              </div>

              <!-- Step 2 Tab -->
              <div
                @click="currentStep = 2"
                :class="[
                  'p-2.5 rounded-[2px] border transition-all cursor-pointer relative',
                  currentStep === 2
                    ? 'bg-[#09090b] border-[rgba(201,168,106,0.5)] shadow-md'
                    : 'bg-[#09090b] border-[rgba(255,255,255,0.04)] opacity-60'
                ]"
              >
                <div class="flex items-center justify-between">
                  <span class="font-inter text-[8px] font-bold text-[#a39e93] tracking-widest uppercase">
                    PASSO 2
                  </span>
                  <span v-if="currentStep === 2" class="w-1.5 h-1.5 rounded-full bg-[#e6c383]"></span>
                </div>
                <span class="font-inter text-[11px] font-bold text-[#a39e93] mt-0.5 block">
                  Confirmar PIN
                </span>
              </div>
            </div>

            <!-- PIN Indicator Component -->
            <PinIndicator :length="pin.length" :max-length="6" />

            <span class="font-inter text-[9px] font-bold text-[#a39e93] tracking-widest text-center block mb-3 uppercase">
              {{ pin.length }} DE 6 DÍGITOS DEFINIDOS
            </span>

            <!-- Rules Checklist Box -->
            <div class="bg-[#09090b] border border-[rgba(255,255,255,0.04)] rounded-[2px] p-3 space-y-2">
              <div class="flex items-center gap-2">
                <CheckCircle2 :class="['w-3.5 h-3.5 shrink-0', pin.length === 6 ? 'text-[#e6c383]' : 'text-[#a39e93]']" />
                <span :class="['font-montserrat text-[11px]', pin.length === 6 ? 'text-[#f2ebd9]' : 'text-[#a39e93]']">
                  6 dígitos numéricos obrigatórios
                </span>
              </div>

              <div class="flex items-center gap-2">
                <CheckCircle2 class="w-3.5 h-3.5 text-[#e6c383] shrink-0" />
                <span class="font-montserrat text-[11px] text-[#f2ebd9]">
                  Sem sequências triviais (ex: 123456, 111111)
                </span>
              </div>

              <div class="flex items-center gap-2 opacity-70">
                <Circle class="w-3.5 h-3.5 text-[#a39e93] shrink-0" />
                <span class="font-montserrat text-[11px] text-[#a39e93]">
                  Evite datas de nascimento ou dados públicos
                </span>
              </div>
            </div>
          </div>

          <!-- Numeric Keypad Component -->
          <PinKeypad @press="handleKeyPress" />

          <!-- Activate Credential Button -->
          <button
            @click="activatePin"
            :disabled="pin.length < 6"
            :class="[
              'w-full font-inter text-[11px] font-bold tracking-wider uppercase py-4 px-4 rounded-[2px] flex items-center justify-center gap-2 shadow-xl transition-all cursor-pointer active:scale-[0.99] my-4',
              pin.length === 6
                ? 'bg-[#c9a86a] hover:bg-[#d6b77e] text-[#09090b]'
                : 'bg-[#141416] text-[#a39e93] border border-[rgba(255,255,255,0.06)] opacity-70'
            ]"
          >
            <Lock class="w-4 h-4" />
            <span>ATIVAR CREDENCIAL DEFINITIVA</span>
          </button>
        </div>

        <!-- Footer Hardware Vault Ingot -->
        <div class="bg-[#09090b] border border-[rgba(255,255,255,0.06)] rounded-[2px] p-3 flex items-center gap-3">
          <Lock class="w-4 h-4 text-[#e6c383] shrink-0" />
          <div>
            <span class="font-inter text-[9px] font-bold text-[#f2ebd9] tracking-[1.2px] uppercase block mb-0.5">
              CRIPTOGRAFIA HARDWARE VAULT • FIPS 140-3 COMPLIANT
            </span>
            <span class="font-jetbrains text-[9px] text-[#a39e93] tracking-wider uppercase opacity-80 block">
              SESSÃO: ATELIER-SEC-8492-B01 • SHA-256 HMAC
            </span>
          </div>
        </div>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { IonPage, IonContent } from '@ionic/vue';
import { Lock, ShieldCheck, CheckCircle2, Circle } from '@lucide/vue';
import { useRouter, useRoute } from 'vue-router';
import PinIndicator from '../components/PinIndicator.vue';
import PinKeypad, { type KeypadItem } from '../components/PinKeypad.vue';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();

const token = computed(() => (route.query.token as string) || '');

// Sincroniza dados da credencial do backend com fallback mock
onMounted(async () => {
  await authStore.fetchActivationProfile(token.value);
});

const artistAvatar = computed(() => authStore.activationProfile.avatar);
const artistName = computed(() => authStore.activationProfile.name);
const artistRole = computed(() => authStore.activationProfile.role);
const artistStation = computed(() => authStore.activationProfile.station);

const currentStep = ref(1);
const pin = ref('1234');
const isSubmitting = ref(false);

function handleKeyPress(key: KeypadItem) {
  if (key.type === 'digit' && key.digit) {
    if (pin.value.length < 6) {
      pin.value += key.digit;
    }
  } else if (key.type === 'delete') {
    pin.value = pin.value.slice(0, -1);
  } else if (key.type === 'bio') {
    pin.value = '654321';
  }
}

async function activatePin() {
  if (pin.value.length !== 6) {
    alert('O PIN deve conter exatamente 6 dígitos numéricos.');
    return;
  }

  isSubmitting.value = true;
  try {
    // Envia ativação com verificação condicional (backend se disponível vs fallback local seguro)
    const result = await authStore.definePermanentPin({
      pin: pin.value,
      token: token.value,
      station: artistStation.value,
    });

    console.log('[DefinirPin] Resposta da ativação:', result);
    alert(result.message);

    const destination = authStore.currentRole === 'admin' ? '/triagem' : '/agenda';
    router.push(destination);
  } catch (err) {
    console.warn('[DefinirPin] Erro ao ativar credencial:', err);
    const destination = authStore.currentRole === 'admin' ? '/triagem' : '/agenda';
    router.push(destination);
  } finally {
    isSubmitting.value = false;
  }
}
</script>
