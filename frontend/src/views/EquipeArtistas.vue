<template>
  <ion-page>
    <ion-content class="bg-[#09090b] text-[#f2ebd9] select-none font-sans" fullscreen>
      <div class="min-h-screen bg-[#09090b] pb-28 max-w-[480px] mx-auto px-4 pt-3 sm:px-6">
        <!-- Top App Header -->
        <header class="flex items-center justify-between py-3 mb-4 border-b border-[rgba(201,168,106,0.12)]">
          <div class="flex items-center gap-3">
            <div class="w-8 h-8 rounded-[2px] bg-[#09090b] border border-[rgba(201,168,106,0.3)] flex items-center justify-center text-[#c9a86a] shadow-md">
              <Shield class="w-4.5 h-4.5" />
            </div>
            <div>
              <div class="font-cinzel text-[10px] font-semibold text-[#d6b77e] tracking-[1.8px] leading-tight uppercase">
                TATTOOFLOW ATELIER
              </div>
              <div class="font-playfair text-[17px] font-semibold text-[#f2ebd9] leading-none mt-0.5">
                Equipe Artistas
              </div>
            </div>
          </div>

          <div class="flex items-center gap-2.5">
            <button
              @click="handleAuditLogs"
              class="w-9 h-9 bg-[#141416] border border-[rgba(201,168,106,0.2)] rounded-full flex items-center justify-center text-[#a39e93] hover:text-[#d6b77e] transition-colors cursor-pointer"
            >
              <ShieldCheck class="w-4.5 h-4.5" />
            </button>

            <button
              @click="handleProfile"
              class="w-9 h-9 bg-[#c9a86a] border border-[#b89355] rounded-full flex items-center justify-center text-[#09090b] shadow-inner cursor-pointer hover:bg-[#d6b77e] transition-colors"
            >
              <User class="w-4.5 h-4.5" />
            </button>
          </div>
        </header>

        <!-- Administrative Breadcrumb -->
        <div class="flex items-center gap-1.5 mb-1.5">
          <KeyRound class="w-3.5 h-3.5 text-[#c9a86a]" />
          <span class="font-inter text-[9px] font-bold text-[#c9a86a] tracking-[1.44px] uppercase">
            PAINEL ADMINISTRATIVO • CONTROLE DE ACESSO
          </span>
        </div>

        <!-- Title & Subtitle -->
        <div class="mb-4">
          <h1 class="font-playfair text-[26px] sm:text-[28px] font-semibold text-[#f2ebd9] leading-tight mb-1">
            Corpo de Artistas & Permissões
          </h1>
          <p class="font-montserrat text-[12px] text-[#a39e93] leading-relaxed">
            Gestão de credenciais de bancada, termos de consentimento e cotas operacionais
          </p>
        </div>

        <!-- Stat Counters Summary Bar -->
        <div class="flex items-center gap-6 mb-4">
          <!-- Residentes Ativos -->
          <div class="flex items-baseline gap-2">
            <div>
              <span class="font-inter text-[9px] font-bold text-[#a39e93] tracking-widest uppercase block mb-0.5">
                RESIDENTES
              </span>
              <div class="flex items-baseline gap-1">
                <span class="font-playfair text-[22px] font-semibold text-[#e6c383] leading-none">
                  {{ String(artistsStore.stats.ativos).padStart(2, '0') }}
                </span>
                <span class="font-montserrat text-[11px] text-[#a39e93]">
                  ativos
                </span>
              </div>
            </div>
            <span class="w-1.5 h-1.5 rounded-full bg-[#c9a86a] self-center"></span>
          </div>

          <!-- Revogados Inativos -->
          <div class="flex items-baseline gap-2">
            <div>
              <span class="font-inter text-[9px] font-bold text-[#a39e93] tracking-widest uppercase block mb-0.5">
                REVOGADOS
              </span>
              <div class="flex items-baseline gap-1">
                <span class="font-playfair text-[22px] font-semibold text-[#f2ebd9] leading-none">
                  {{ String(artistsStore.stats.inativos).padStart(2, '0') }}
                </span>
                <span class="font-montserrat text-[11px] text-[#a39e93]">
                  inativo
                </span>
              </div>
            </div>
            <span class="w-1.5 h-1.5 rounded-full bg-[#ef4444] self-center"></span>
          </div>
        </div>

        <!-- Primary Action: Convidar Novo Tatuador -->
        <div class="flex items-center gap-2 mb-4">
          <button
            @click="inviteNewArtist"
            class="flex-1 bg-[#c9a86a] hover:bg-[#d6b77e] text-[#09090b] font-inter text-[11px] font-bold tracking-[1.44px] uppercase py-3.5 px-4 rounded-[2px] shadow-xl flex items-center justify-center gap-2 transition-all cursor-pointer active:scale-[0.99]"
          >
            <KeyRound class="w-4 h-4 stroke-[2.2]" />
            <span>CONVIDAR NOVO TATUADOR</span>
          </button>

          <button
            @click="inviteNewArtist"
            class="w-12 h-11 bg-[#141416] hover:bg-[#1a1a1f] border border-[rgba(201,168,106,0.3)] text-[#e6c383] rounded-[2px] flex items-center justify-center cursor-pointer transition-all active:scale-95 shadow-md shrink-0"
            title="Gerar convite rápido"
          >
            <Ticket class="w-4 h-4" />
          </button>
        </div>

        <!-- Search Bar -->
        <div class="relative mb-3 flex items-center">
          <Search class="w-4 h-4 text-[#a39e93] absolute left-3.5 pointer-events-none" />
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Buscar por nome, bancada ou credencial..."
            class="w-full bg-[#141416] border border-[rgba(255,255,255,0.06)] focus:border-[#c9a86a] rounded-[2px] pl-10 pr-4 py-2.5 font-montserrat text-[12px] text-[#f2ebd9] placeholder-[#a39e93] transition-colors outline-none"
          />
        </div>

        <!-- Filter Tabs (Pills) -->
        <div class="flex items-center gap-2 mb-4">
          <button
            v-for="filter in filterOptions"
            :key="filter.id"
            @click="activeFilter = filter.id"
            :class="[
              'font-inter text-[10px] font-bold tracking-wider uppercase px-3 py-1.5 rounded-[2px] transition-all cursor-pointer',
              activeFilter === filter.id
                ? 'bg-[#e6c383] text-[#09090b] shadow-sm'
                : 'bg-transparent text-[#a39e93] hover:text-[#f2ebd9]'
            ]"
          >
            {{ filter.label }}
          </button>
        </div>

        <!-- Artists List -->
        <div class="space-y-3.5 mb-5">
          <div
            v-for="artist in filteredArtists"
            :key="artist.id"
            class="bg-[#141416] border border-[rgba(255,255,255,0.06)] rounded-[2px] p-3.5 shadow-xl transition-all"
          >
            <!-- Artist Main Info Row -->
            <div class="flex items-start justify-between gap-3 mb-3">
              <div class="flex items-center gap-3">
                <div
                  :class="[
                    'w-12 h-12 rounded-[2px] overflow-hidden bg-[#09090b] shrink-0 border',
                    artist.status === 'revogado'
                      ? 'border-[rgba(255,255,255,0.1)] grayscale opacity-80'
                      : 'border-[rgba(201,168,106,0.3)]'
                  ]"
                >
                  <img :src="artist.avatar" :alt="artist.name" class="w-full h-full object-cover" />
                </div>

                <div>
                  <h3 class="font-playfair text-[18px] font-semibold text-[#f2ebd9] leading-tight">
                    {{ artist.name }}
                  </h3>
                  <p class="font-montserrat text-[11px] text-[#a39e93] mt-0.5">
                    {{ artist.station }} • {{ artist.specialty }}
                  </p>

                  <div class="flex items-center gap-2 mt-1">
                    <span
                      :class="[
                        'font-jetbrains text-[10px] font-bold uppercase',
                        artist.status === 'revogado' ? 'text-[#ef4444]' : 'text-[#e6c383]'
                      ]"
                    >
                      {{ artist.pinLabel }}
                    </span>
                    <span class="text-[#a39e93] text-[9px]">•</span>
                    <span class="font-montserrat text-[10px] text-[#a39e93]">
                      {{ artist.sinceLabel }}
                    </span>
                  </div>
                </div>
              </div>

              <!-- Status Badge -->
              <span
                :class="[
                  'font-inter text-[8px] font-bold tracking-wider uppercase px-2 py-0.5 rounded-[2px] shrink-0',
                  artist.status === 'ativo'
                    ? 'bg-[#102217] border border-[rgba(74,222,128,0.3)] text-[#4ade80]'
                    : 'bg-[#3a1416] border border-[rgba(239,68,68,0.3)] text-[#f87171]'
                ]"
              >
                {{ artist.statusBadge }}
              </span>
            </div>

            <!-- Interrupted Access Alert Box (For Revoked Artist) -->
            <div
              v-if="artist.status === 'revogado'"
              class="bg-[#180d0e] border border-[rgba(239,68,68,0.2)] rounded-[2px] p-3 my-3"
            >
              <div class="flex items-center gap-1.5 text-[#ef4444] mb-1">
                <Scissors class="w-3.5 h-3.5 rotate-90" />
                <span class="font-inter text-[9px] font-bold tracking-[1.2px] uppercase">
                  ACESSO FÍSICO & DIGITAL INTERROMPIDO
                </span>
              </div>
              <p class="font-montserrat text-[11px] text-[#a39e93] leading-relaxed">
                Sem vínculo ativo com tablets, cofres de suprimentos ou banco de dados de clientes.
              </p>
            </div>

            <!-- Action Buttons Grid -->
            <div class="grid grid-cols-2 gap-2 pt-2 border-t border-[rgba(255,255,255,0.04)]">
              <!-- Active Buttons -->
              <template v-if="artist.status === 'ativo'">
                <button
                  @click="editPermissions(artist)"
                  class="bg-[#09090b] hover:bg-[#18181c] border border-[rgba(255,255,255,0.08)] text-[#f2ebd9] font-inter text-[9px] font-bold tracking-wider uppercase py-2.5 px-3 rounded-[2px] flex items-center justify-center gap-1.5 cursor-pointer transition-all"
                >
                  <SlidersHorizontal class="w-3.5 h-3.5 text-[#a39e93]" />
                  <span>EDITAR PERMISSÕES</span>
                </button>

                <button
                  @click="revokeAccess(artist)"
                  class="bg-[#09090b] hover:bg-[#1f1315] border border-[rgba(255,255,255,0.08)] hover:border-red-900/40 text-[#f2ebd9] hover:text-red-400 font-inter text-[9px] font-bold tracking-wider uppercase py-2.5 px-3 rounded-[2px] flex items-center justify-center gap-1.5 cursor-pointer transition-all"
                >
                  <RotateCcw class="w-3.5 h-3.5 text-[#ef4444]" />
                  <span>REVOGAR ACESSO</span>
                </button>
              </template>

              <!-- Revoked Buttons -->
              <template v-else>
                <button
                  @click="reactivateProfile(artist)"
                  class="bg-[#09090b] hover:bg-[#18181c] border border-[rgba(255,255,255,0.08)] text-[#f2ebd9] font-inter text-[9px] font-bold tracking-wider uppercase py-2.5 px-3 rounded-[2px] flex items-center justify-center gap-1.5 cursor-pointer transition-all"
                >
                  <RefreshCw class="w-3.5 h-3.5 text-[#c9a86a]" />
                  <span>REATIVAR PERFIL</span>
                </button>

                <button
                  @click="deletePermanently(artist)"
                  class="bg-[#782828] hover:bg-[#8f3030] text-[#f2ebd9] font-inter text-[9px] font-bold tracking-wider uppercase py-2.5 px-3 rounded-[2px] flex items-center justify-center gap-1.5 cursor-pointer transition-all shadow-md"
                >
                  <Trash2 class="w-3.5 h-3.5" />
                  <span>EXCLUIR DEFINITIVO</span>
                </button>
              </template>
            </div>
          </div>
        </div>

        <!-- Security & Governance Banner Card -->
        <div class="bg-[#0c1017] border border-[rgba(59,130,246,0.2)] rounded-[2px] p-4 mb-6 shadow-xl">
          <div class="flex items-center gap-1.5 text-[#93c5fd] mb-2">
            <ShieldCheck class="w-4 h-4 text-[#93c5fd]" />
            <span class="font-inter text-[9px] font-bold tracking-[1.44px] uppercase">
              AUDITORIA DE SEGURANÇA & GOVERNANÇA NOTARIAL
            </span>
          </div>

          <p class="font-montserrat text-[11px] text-[#94a3b8] leading-relaxed mb-3">
            A revogação de credenciais bloqueia imediatamente o login via PIN no tablet da bancada física e remove o vínculo com prontuários e termos de clientes não executados. Log autenticado via SHA-256 no nó local do estúdio.
          </p>

          <div class="flex items-center justify-between pt-2 border-t border-[rgba(255,255,255,0.04)] font-jetbrains text-[9px] font-bold text-[#64748b] tracking-wider uppercase">
            <span>HASH: 8f9b2d...c3710a</span>
            <span>ATELIER NODE #01 - ATIVO</span>
          </div>
        </div>
      </div>

      <!-- Reusable Adapted Bottom Navigation -->
      <!-- <AccessControlNavigation :items="navItems" active-tab="artistas" /> -->
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { IonPage, IonContent } from '@ionic/vue';
import {
  Shield,
  ShieldCheck,
  User,
  KeyRound,
  Ticket,
  Search,
  SlidersHorizontal,
  RotateCcw,
  RefreshCw,
  Trash2,
  Scissors,
  Calendar,
  Users,
  Landmark,
  Package,
} from '@lucide/vue';
import { useRouter } from 'vue-router';
import AccessControlNavigation, { type NavItem } from '../components/AccessControlNavigation.vue';
import { useArtistsStore, type ArtistUser } from '@/stores/artists';

const router = useRouter();
const artistsStore = useArtistsStore();

// Search & Filter State
const searchQuery = ref('');
const activeFilter = ref('todos');

const filterOptions = computed(() => [
  { id: 'todos', label: `TODOS (${artistsStore.stats.total})` },
  { id: 'residentes', label: `RESIDENTES (${artistsStore.stats.ativos})` },
  { id: 'inativos', label: `INATIVOS (${artistsStore.stats.inativos})` },
]);

// Sincroniza corpo de artistas do backend com fallback mock
onMounted(async () => {
  await artistsStore.fetchArtists();
});

const filteredArtists = computed(() => {
  return artistsStore.artists.filter(artist => {
    // Tab filter
    if (activeFilter.value === 'residentes' && artist.status !== 'ativo') return false;
    if (activeFilter.value === 'inativos' && artist.status !== 'revogado') return false;

    // Search query
    if (!searchQuery.value) return true;
    const q = searchQuery.value.toLowerCase();
    return (
      artist.name.toLowerCase().includes(q) ||
      artist.station.toLowerCase().includes(q) ||
      artist.specialty.toLowerCase().includes(q) ||
      artist.pinLabel.toLowerCase().includes(q)
    );
  });
});

// User Actions
function inviteNewArtist() {
  router.push('/convites');
}

function handleAuditLogs() {
  console.log('Auditoria de logs notariais');
}

function handleProfile() {
  console.log('Perfil administrativo');
}

function editPermissions(artist: ArtistUser) {
  console.log('Editando permissões de:', artist.name);
}

async function revokeAccess(artist: ArtistUser) {
  const result = await artistsStore.revokeAccess(artist.id);
  console.log('[EquipeArtistas]', result.message);
}

async function reactivateProfile(artist: ArtistUser) {
  const result = await artistsStore.reactivateProfile(artist.id);
  console.log('[EquipeArtistas]', result.message);
}

async function deletePermanently(artist: ArtistUser) {
  if (confirm(`Deseja realmente remover em definitivo o cadastro de ${artist.name}?`)) {
    const result = await artistsStore.deletePermanently(artist.id);
    console.log('[EquipeArtistas]', result.message);
  }
}

// Adapted Bottom Navigation Items for Atelier Management
const navItems: NavItem[] = [
  { id: 'agenda', label: 'AGENDA', icon: Calendar, path: '/agenda' },
  { id: 'artistas', label: 'ARTISTAS', icon: Users, path: '/equipe-artistas' },
  { id: 'gestao', label: 'GESTÃO', icon: Landmark, path: '/fechamento' },
  { id: 'estoque', label: 'ESTOQUE', icon: Package, path: '/estoque' },
];
</script>
