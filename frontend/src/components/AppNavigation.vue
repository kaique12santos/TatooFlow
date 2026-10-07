<template>
  <div v-if="!isTabBarHidden" class="fixed bottom-0 left-0 right-0 z-50">
    <!-- Quick Role Switcher Pill for seamless preview/testing between Admin & Tatuador -->
    <div class="flex justify-center -mb-2 pointer-events-auto">
      <button
        @click="switchProfile"
        class="bg-[#141416]/95 hover:bg-[#1a1a1e] border border-[rgba(201,168,106,0.35)] backdrop-blur-md px-3 py-0.5 rounded-full shadow-lg flex items-center gap-1.5 transition-all active:scale-95 cursor-pointer group"
        title="Alternar entre perfil Admin e Tatuador"
      >
        <span class="w-1.5 h-1.5 rounded-full bg-[#d6b77e] animate-pulse"></span>
        <span class="font-jetbrains text-[9px] font-bold tracking-wider text-[#d6b77e] uppercase">
          PERFIL: {{ authStore.currentRole === 'admin' ? 'ADMIN (4 ABAS)' : 'TATUADOR (3 ABAS)' }}
        </span>
        <ArrowLeftRight class="w-2.5 h-2.5 text-[#a39e93] group-hover:text-[#d6b77e] transition-colors" />
      </button>
    </div>

    <!-- Ionic Tab Bar -->
    <ion-tab-bar
      slot="bottom"
      class="ion-no-border bg-[#09090b] border-t border-[#1a1a1e] py-1 h-[62px] shadow-[0_-4px_20px_rgba(0,0,0,0.6)]"
    >
      <ion-tab-button
        v-for="item in visibleNavItems"
        :key="item.id"
        @click="navigate(item)"
        :class="[
          'bg-transparent transition-all relative overflow-hidden flex flex-col items-center justify-center cursor-pointer flex-1',
          isItemActive(item)
            ? 'text-[#d6b77e]'
            : 'text-[#7a766f] hover:text-[#a39e93]'
        ]"
      >
        <!-- Top Indicator Line & Glow for Active Tab -->
        <div
          v-if="isItemActive(item)"
          class="absolute top-0 left-1/2 -translate-x-1/2 w-12 h-[2.5px] bg-[#d6b77e] shadow-[0_0_10px_#d6b77e] rounded-full"
        ></div>
        <div
          v-if="isItemActive(item)"
          class="absolute inset-0 bg-gradient-to-b from-[rgba(214,183,126,0.15)] via-[rgba(214,183,126,0.03)] to-transparent pointer-events-none"
        ></div>

        <!-- Tab Icon -->
        <component
          :is="item.icon"
          :size="19"
          :class="[
            'mb-0.5 transition-colors relative z-10',
            isItemActive(item) ? 'text-[#d6b77e]' : 'text-[#7a766f]'
          ]"
        />

        <!-- Tab Label -->
        <ion-label
          :class="[
            'font-inter text-[9px] font-bold tracking-[0.55px] uppercase transition-colors relative z-10',
            isItemActive(item) ? 'text-[#d6b77e]' : 'text-[#7a766f]'
          ]"
        >
          {{ item.name }}
        </ion-label>
      </ion-tab-button>
    </ion-tab-bar>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { IonTabBar, IonTabButton, IonLabel } from '@ionic/vue';
import {
  Calendar,
  Users,
  Camera,
  ClipboardList,
  Landmark,
  Package,
  ArrowLeftRight,
} from '@lucide/vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import type { AppNavigationItem } from '@/types/navigation';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();

// ==========================================
// STRICT CONDITIONAL TAB DEFINITIONS
// ==========================================

// TATUADOR: Estritamente Agenda, Clientes e Mídia (3 abas)
const tatuadorNavItems: AppNavigationItem[] = [
  {
    id: 'agenda',
    name: 'Agenda',
    icon: Calendar,
    route: { name: 'Agenda' },
    match: ['Agenda', 'AgendamentoSessao', 'AlocacaoMateriais', 'TermoAnamnese'],
    role: 'tatuador',
  },
  {
    id: 'clientes',
    name: 'Clientes',
    icon: Users,
    route: { name: 'Clientes' },
    match: ['Clientes', 'DossieCliente'],
    role: 'tatuador',
  },
  {
    id: 'midia',
    name: 'Mídia',
    icon: Camera,
    route: { name: 'UploadArte' },
    match: ['UploadArte', 'LegendaCuradoria', 'PublicacaoDistribuicao'],
    role: 'tatuador',
  },
];

// ADMIN: Estritamente Triagem, Gestão, Estoque e Equipe (4 abas)
const adminNavItems: AppNavigationItem[] = [
  {
    id: 'triagem',
    name: 'Triagem',
    icon: ClipboardList,
    route: { name: 'Triagem' },
    match: ['Triagem', 'TriagemClientes'],
    role: 'admin',
  },
  {
    id: 'gestao',
    name: 'Gestão',
    icon: Landmark,
    route: { name: 'Fechamento' },
    match: ['Fechamento', 'PerformanceArtistas'],
    role: 'admin',
  },
  {
    id: 'estoque',
    name: 'Estoque',
    icon: Package,
    route: { name: 'Estoque' },
    match: ['Estoque', 'CadastrarInsumo'],
    role: 'admin',
  },
  {
    id: 'equipe',
    name: 'Equipe',
    icon: Users,
    route: { name: 'EquipeArtistas' },
    match: ['EquipeArtistas', 'Convites'],
    role: 'admin',
  },
];

// Visible items conditioned strictly by the active user profile
const visibleNavItems = computed<AppNavigationItem[]>(() => {
  return authStore.currentRole === 'admin' ? adminNavItems : tatuadorNavItems;
});

// Hide tab bar on specific views (e.g. PinLock, DefinirPin)
const isTabBarHidden = computed<boolean>(() => {
  return Boolean(route.meta.hideTabBar);
});

// Active state detection supporting secondary views
function isItemActive(item: AppNavigationItem): boolean {
  // 1. Explicit meta.activeTab match
  if (route.meta.activeTab === item.id) {
    return true;
  }

  // 2. Named route matching
  const currentName = String(route.name || '');
  if (item.match.includes(currentName)) {
    return true;
  }

  // 3. Path-based heuristic fallback
  const currentPath = String(route.path || '').toLowerCase();
  if (item.id === 'agenda' && (currentPath === '/' || currentPath.includes('agenda'))) {
    return true;
  }
  if (item.id === 'clientes' && (currentPath.includes('cliente') || currentPath.includes('dossie'))) {
    return true;
  }
  if (item.id === 'midia' && (currentPath.includes('upload') || currentPath.includes('curadoria') || currentPath.includes('publicacao'))) {
    return true;
  }
  if (item.id === 'triagem' && (currentPath.includes('triagem') || currentPath.includes('recepcao'))) {
    return true;
  }
  if (item.id === 'gestao' && (currentPath.includes('fechamento') || currentPath.includes('gestao') || currentPath.includes('performance'))) {
    return true;
  }
  if (item.id === 'estoque' && (currentPath.includes('estoque') || currentPath.includes('insumo'))) {
    return true;
  }
  if (item.id === 'equipe' && (currentPath.includes('equipe') || currentPath.includes('convite') || currentPath.includes('artista'))) {
    return true;
  }

  return false;
}

// Navigation handler
function navigate(item: AppNavigationItem) {
  if (item.route.name) {
    router.push({ name: item.route.name }).catch(() => {});
  } else if (item.route.path) {
    router.push(item.route.path).catch(() => {});
  }
}

// Profile switcher for seamless preview
function switchProfile() {
  authStore.toggleRole();
  const nextTarget = authStore.currentRole === 'admin' ? '/triagem' : '/agenda';
  router.push(nextTarget).catch(() => {});
}
</script>
