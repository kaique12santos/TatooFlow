<template>
  <ion-page>
    <ion-content class="bg-[#09090b] text-[#f2ebd9] select-none font-sans" fullscreen>
      <div class="min-h-screen bg-[#09090b] pb-24 max-w-[480px] mx-auto px-4 pt-3 sm:px-6">
        <!-- Reusable Top Header -->
        <AppHeader />

        <!-- Secondary Navigation Header -->
        <div class="flex items-center justify-between mb-3 mt-1">
          <button
            @click="handleBack"
            class="flex items-center gap-2 text-[#d6b77e] hover:text-[#f2ebd9] transition-colors cursor-pointer group"
          >
            <ArrowLeft class="w-4 h-4 text-[#d6b77e] group-hover:-translate-x-0.5 transition-transform" />
            <span class="font-inter text-[11px] font-semibold tracking-[2px] uppercase text-[#d6b77e]">
              PAINEL DE EQUIPE
            </span>
          </button>
        </div>

        <!-- Title & Description Section -->
        <div class="mb-4">
          <h1 class="font-playfair text-[26px] font-semibold text-[#f2ebd9] leading-tight mb-1">
            Convite de Primeiro Acesso
          </h1>
          <p class="font-montserrat text-[12px] text-[#a39e93] leading-relaxed">
            Emita credenciais exclusivas para artistas convidados, residentes e equipe de apoio com assinatura curada e controle de validade.
          </p>
        </div>

        <!-- Atelier Hero Photo Banner -->
        <div class="relative w-full h-44 rounded-[2px] overflow-hidden border border-[rgba(255,255,255,0.06)] mb-5 shadow-xl bg-[#09090b]">
          <img :src="heroImage" alt="Atelier Interior" class="w-full h-full object-cover" />
          <div class="absolute inset-0 bg-gradient-to-t from-[#09090b] via-transparent to-transparent opacity-80"></div>
          
          <div class="absolute bottom-3 left-3 bg-[rgba(9,9,11,0.85)] backdrop-blur-md px-2.5 py-1 rounded-[2px] border border-[rgba(255,255,255,0.12)] flex items-center gap-1.5 shadow-lg">
            <Shield class="w-3.5 h-3.5 text-[#e6c383]" />
            <span class="font-inter text-[9px] font-bold text-[#f2ebd9] tracking-[1.44px] uppercase">
              ATELIER NEW CONCEPT
            </span>
          </div>
        </div>

        <!-- Section 1: Credenciais do Artista -->
        <div class="mb-5">
          <div class="flex items-center justify-between mb-2 px-0.5">
            <span class="font-inter text-[9px] font-bold text-[#e6c383] tracking-[1.44px] uppercase">
              1. CREDENCIAIS DO ARTISTA
            </span>
            <span class="font-inter text-[9px] font-bold text-[#a39e93] tracking-[1.44px] uppercase">
              ETAPA OBRIGATÓRIA
            </span>
          </div>

          <div class="bg-[#141416] rounded-[2px] p-4 border border-[rgba(255,255,255,0.05)] shadow-xl space-y-3.5">
            <!-- Field 1: Nome Completo -->
            <div>
              <label class="flex items-center gap-1.5 font-inter text-[9px] font-bold text-[#a39e93] tracking-[1.44px] uppercase mb-1.5">
                <UserCheck class="w-3.5 h-3.5 text-[#a39e93]" />
                <span>NOME COMPLETO</span>
              </label>
              <input
                v-model="artistName"
                type="text"
                placeholder="Ex: Gabriel D. Marcenna"
                class="w-full bg-[#09090b] border border-[rgba(255,255,255,0.06)] rounded-[2px] px-3 py-2.5 font-montserrat text-[13px] text-[#f2ebd9] placeholder-[#a39e93] focus:outline-none focus:border-[#c9a86a] transition-all"
              />
            </div>

            <!-- Field 2: E-mail Profissional -->
            <div>
              <label class="flex items-center gap-1.5 font-inter text-[9px] font-bold text-[#a39e93] tracking-[1.44px] uppercase mb-1.5">
                <Mail class="w-3.5 h-3.5 text-[#a39e93]" />
                <span>E-MAIL PROFISSIONAL</span>
              </label>
              <input
                v-model="artistEmail"
                type="email"
                placeholder="Ex: gabriel@newconcept.art"
                class="w-full bg-[#09090b] border border-[rgba(255,255,255,0.06)] rounded-[2px] px-3 py-2.5 font-montserrat text-[13px] text-[#f2ebd9] placeholder-[#a39e93] focus:outline-none focus:border-[#c9a86a] transition-all"
              />
            </div>

            <!-- Field 3: WhatsApp para envio direto -->
            <div>
              <label class="flex items-center gap-1.5 font-inter text-[9px] font-bold text-[#a39e93] tracking-[1.44px] uppercase mb-1.5">
                <MessageSquare class="w-3.5 h-3.5 text-[#a39e93]" />
                <span>WHATSAPP PARA ENVIO DIRETO</span>
              </label>
              <div class="relative flex items-center">
                <input
                  v-model="artistPhone"
                  type="text"
                  placeholder="+55 (11) 98765-4321"
                  class="w-full bg-[#09090b] border border-[rgba(255,255,255,0.06)] rounded-[2px] px-3 py-2.5 font-jetbrains text-[13px] text-[#f2ebd9] placeholder-[#a39e93] focus:outline-none focus:border-[#c9a86a] transition-all pr-16"
                />
                <span class="absolute right-2.5 bg-[#141416] border border-[rgba(255,255,255,0.06)] text-[#a39e93] font-inter text-[9px] font-bold px-2 py-0.5 rounded-[2px] uppercase">
                  DIRETO
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- Section 2: Segurança & Validade do Token -->
        <div class="mb-5">
          <div class="mb-2 px-0.5">
            <span class="font-inter text-[9px] font-bold text-[#e6c383] tracking-[1.44px] uppercase">
              2. SEGURANÇA & VALIDADE DO TOKEN
            </span>
          </div>

          <div class="bg-[#141416] rounded-[2px] p-4 border border-[rgba(255,255,255,0.05)] shadow-xl">
            <!-- Automatic PIN Toggle Row -->
            <div class="flex items-center justify-between mb-3.5">
              <div>
                <h3 class="font-inter text-[11px] font-bold text-[#f2ebd9] uppercase tracking-wider leading-tight">
                  GERAR PIN PROVISÓRIO AUTOMÁTICO
                </h3>
                <p class="font-montserrat text-[11px] text-[#a39e93] mt-0.5">
                  Código instantâneo de desbloqueio para 1º login
                </p>
              </div>

              <button
                @click="autoPin = !autoPin"
                :class="[
                  'w-11 h-6 rounded-full transition-colors relative cursor-pointer p-0.5 shrink-0',
                  autoPin ? 'bg-[#e6c383]' : 'bg-[#09090b] border border-[rgba(255,255,255,0.1)]'
                ]"
              >
                <span
                  :class="[
                    'w-5 h-5 rounded-full transition-transform duration-200 block shadow-md',
                    autoPin ? 'bg-[#09090b] translate-x-5' : 'bg-[#a39e93] translate-x-0'
                  ]"
                ></span>
              </button>
            </div>

            <!-- PIN Code Ingot -->
            <div class="bg-[#09090b] border border-[rgba(255,255,255,0.04)] rounded-[2px] p-3 mb-4 flex items-center justify-between">
              <div>
                <span class="font-inter text-[9px] font-bold text-[#a39e93] tracking-[1.44px] uppercase block mb-0.5">
                  PIN PROVISÓRIO CRIPTOGRAFADO
                </span>
                <div class="flex items-center gap-2">
                  <span class="font-jetbrains text-[16px] font-bold text-[#e6c383] tracking-widest">
                    {{ generatedPin }}
                  </span>
                  <button @click="regeneratePin" class="text-[#a39e93] hover:text-[#e6c383] transition-colors p-0.5 cursor-pointer">
                    <RefreshCw class="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>

              <div class="text-right">
                <span class="font-inter text-[8px] text-[#a39e93] tracking-[1.2px] uppercase block mb-0.5">
                  AUTENTICAÇÃO
                </span>
                <div class="flex items-center gap-1 text-[#e6c383]">
                  <Lock class="w-3 h-3" />
                  <span class="font-inter text-[9px] font-bold tracking-wider uppercase">2FA FORÇADO</span>
                </div>
              </div>
            </div>

            <!-- Link Validity Selector -->
            <div>
              <span class="font-inter text-[9px] font-bold text-[#a39e93] tracking-[1.44px] uppercase block mb-2">
                VALIDADE DO LINK DE ATIVAÇÃO
              </span>

              <div class="grid grid-cols-3 gap-2">
                <button
                  v-for="opt in validityOptions"
                  :key="opt.value"
                  @click="selectedValidity = opt.value"
                  :class="[
                    'py-2 px-2 rounded-[2px] font-inter text-[10px] font-bold tracking-wider uppercase transition-all cursor-pointer text-center',
                    selectedValidity === opt.value
                      ? 'bg-[#e6c383] text-[#09090b] shadow-md font-extrabold'
                      : 'bg-[#09090b] text-[#a39e93] border border-[rgba(255,255,255,0.04)] hover:border-[rgba(201,168,106,0.3)] hover:text-[#f2ebd9]'
                  ]"
                >
                  {{ opt.label }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Primary Action Buttons -->
        <div class="space-y-3 mb-6">
          <button
            @click="sendInvite"
            class="w-full bg-[#e6c383] hover:bg-[#d6b77e] text-[#09090b] font-inter text-[11px] font-bold tracking-wider uppercase py-3.5 px-4 rounded-[2px] shadow-xl flex items-center justify-center gap-2 transition-all cursor-pointer active:scale-[0.99]"
          >
            <Send class="w-4 h-4" />
            <span>DISPARAR CONVITE VIA WHATSAPP & E-MAIL</span>
          </button>

          <button
            @click="copyLink"
            class="w-full bg-[#09090b] hover:bg-[#141416] text-[#f2ebd9] border border-[rgba(255,255,255,0.1)] font-inter text-[11px] font-bold tracking-wider uppercase py-3.5 px-4 rounded-[2px] flex items-center justify-center gap-2 transition-all cursor-pointer active:scale-[0.99]"
          >
            <Copy class="w-4 h-4 text-[#a39e93]" />
            <span>COPIAR LINK</span>
          </button>
        </div>

        <!-- Convites Recentes Pendentes -->
        <div class="mb-4">
          <div class="flex items-center justify-between mb-3 px-0.5">
            <div class="flex items-center gap-1.5">
              <Clock class="w-3.5 h-3.5 text-[#a39e93]" />
              <span class="font-inter text-[9px] font-bold text-[#a39e93] tracking-[1.44px] uppercase">
                CONVITES RECENTES PENDENTES
              </span>
            </div>
            <span class="font-inter text-[9px] font-bold text-[#e6c383] tracking-[1.44px] uppercase">
              {{ recentInvites.length }} EM ABERTO
            </span>
          </div>

          <div class="space-y-2.5">
            <div
              v-for="recent in recentInvites"
              :key="recent.id || recent.name"
              class="bg-[#141416] border border-[rgba(255,255,255,0.05)] rounded-[2px] p-3 flex items-center justify-between shadow-lg"
            >
              <div class="flex items-center gap-3">
                <div class="w-9 h-9 bg-[#09090b] border border-[rgba(255,255,255,0.06)] rounded-[2px] flex items-center justify-center text-[#a39e93] shrink-0">
                  <User class="w-4 h-4" />
                </div>
                <div>
                  <h4 class="font-playfair text-[15px] font-semibold text-[#f2ebd9] leading-snug">
                    {{ recent.name }}
                  </h4>
                  <p class="font-montserrat text-[11px] text-[#a39e93]">
                    {{ recent.statusText }}
                  </p>
                </div>
              </div>

              <button
                @click="resendInvite(recent.id || recent.name)"
                class="bg-[#09090b] text-[#f2ebd9] border border-[rgba(255,255,255,0.1)] hover:border-[#e6c383] font-inter text-[9px] font-bold tracking-wider uppercase px-2.5 py-1.5 rounded-[2px] flex items-center gap-1 transition-colors cursor-pointer"
              >
                <RefreshCw class="w-3 h-3 text-[#e6c383]" />
                <span>REENVIAR</span>
              </button>
            </div>
          </div>
        </div>

        <!-- Custom Navigation Bar for Invites / Access Section -->
        <!-- <AccessControlNavigation active-tab="convites" /> -->
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { IonPage, IonContent } from '@ionic/vue';
import { Shield, UserCheck, Mail, MessageSquare, RefreshCw, Lock, Send, Copy, Clock, User, ArrowLeft } from '@lucide/vue';
import { useRouter } from 'vue-router';
import AppHeader from '../components/AppHeader.vue';
import { useInvitesStore, type InviteItem } from '@/stores/invites';

const router = useRouter();
const invitesStore = useInvitesStore();

function handleBack() {
  if (window.history.length > 1) {
    router.back();
  } else {
    router.push({ name: 'EquipeArtistas' });
  }
}

// Hero image from Figma CDN
const heroImage = 'https://s3-alpha-sig.figma.com/img/e50a/06e1/5f90aaa2ed7535632339e8df873aa5f4?Expires=1792368000&Key-Pair-Id=APKAQ4GOSFWCW27IBOMQ&Signature=XPkhlwiepG0U5OxiSEy64vwkVwDhYHK59hL6n6cAnw1YnN0pm25i~ITQ-8HC8YD-ke17dTGdB7dUICNheaTE5wKyyqIy-Pt4BmV~1gC5~5sWoBwvWAckylOfnMNvk4ZEUNJtwO3-C8LL2HhaNWIDpUYxnXsYbLRhuZm-BCQbx71HTGuJCEsuqPQocMEpR0Ix0EOvcsqX65rTLHzbXgkFPGIkJ193Kyxk6AzvH84PQIVBRiU1IUm8iCtmcVEeMkwUx3sLi9g28BvL2SGiukCSQQpd2oC5RmiHegx7X3h6hUrCLPK1iglTx~q8-mStOaK3Vi6ZY~Odo8YMeia6rpGhAg__';

// Form state
const artistName = ref('Gabriel D. Marcenna');
const artistEmail = ref('gabriel@newconcept.art');
const artistPhone = ref('+55 (11) 98765-4321');
const autoPin = ref(true);
const generatedPin = ref('749 • 201');
const selectedValidity = ref('48h');
const isSubmitting = ref(false);

const validityOptions = [
  { label: '24 HORAS', value: '24h' },
  { label: '48 HORAS', value: '48h' },
  { label: '7 DIAS', value: '7d' },
];

// Sincroniza convites do backend ou mantém fallback mock local
onMounted(async () => {
  await invitesStore.fetchInvites();
});

const recentInvites = computed<InviteItem[]>(() => invitesStore.invites);

function regeneratePin() {
  const p1 = Math.floor(100 + Math.random() * 900);
  const p2 = Math.floor(100 + Math.random() * 900);
  generatedPin.value = `${p1} • ${p2}`;
}

async function sendInvite() {
  if (!artistName.value.trim()) {
    alert('Por favor, informe o nome do artista convidado.');
    return;
  }

  isSubmitting.value = true;
  try {
    // Executa disparo com verificação condicional (backend se disponível vs fallback mock)
    const result = await invitesStore.createInvite({
      name: artistName.value,
      email: artistEmail.value,
      phone: artistPhone.value,
      autoPin: autoPin.value,
      pin: generatedPin.value,
      validity: selectedValidity.value,
    });

    alert(result.message);
  } catch (err) {
    console.warn('[Convites] Erro no envio do convite:', err);
  } finally {
    isSubmitting.value = false;
  }
}

function copyLink() {
  const token = `token-${Date.now().toString(36)}`;
  const link = `${window.location.origin}/#/definir-pin-ativacao?token=${token}&pin=${encodeURIComponent(generatedPin.value)}`;
  if (navigator.clipboard) {
    navigator.clipboard.writeText(link).catch(() => {});
  }
  alert(`Link de ativação copiado:\n${link}`);
}

async function resendInvite(inviteIdOrName: string) {
  const result = await invitesStore.resendInvite(inviteIdOrName);
  alert(result.message);
}
</script>
