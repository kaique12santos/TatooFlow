<template>
  <nav class="fixed bottom-0 left-0 right-0 bg-[#09090b] border-t border-[#1a1a1e] z-50 py-1.5 px-4 shadow-2xl">
    <div class="max-w-[480px] mx-auto flex items-center justify-between">
      <button
        v-for="nav in navItems"
        :key="nav.id"
        @click="navigate(nav)"
        :class="[
          'flex-1 flex flex-col items-center justify-center py-1.5 transition-colors cursor-pointer relative',
          currentActiveTab === nav.id ? 'text-[#e6c383]' : 'text-[#a39e93] hover:text-[#f2ebd9]'
        ]"
      >
        <component :is="nav.icon" class="w-4.5 h-4.5 mb-1" />
        <span class="font-inter text-[9px] font-bold tracking-[0.45px] uppercase">
          {{ nav.label }}
        </span>
        <span
          v-if="currentActiveTab === nav.id"
          class="absolute bottom-0 left-1/2 -translate-x-1/2 w-6 h-0.5 bg-[#c9a86a] rounded-full"
        ></span>
      </button>
    </div>
  </nav>
</template>

<script lang="ts">
export interface NavItem {
  id: string;
  label: string;
  icon: any;
  routeName?: string;
  path?: string;
}
</script>

<script setup lang="ts">
import { computed } from 'vue';
import { Users, Mail, KeyRound, SlidersHorizontal } from '@lucide/vue';
import { useRouter, useRoute } from 'vue-router';

const defaultNavItems: NavItem[] = [
  { id: 'membros', label: 'MEMBROS', icon: Users, routeName: 'Membros', path: '/membros' },
  { id: 'convites', label: 'CONVITES', icon: Mail, routeName: 'Convites', path: '/convites' },
  { id: 'passes', label: 'PASSES', icon: KeyRound, routeName: 'Passes', path: '/passes' },
  { id: 'atelier', label: 'ATELIER', icon: SlidersHorizontal, routeName: 'AtelierGestao', path: '/atelier-gestao' },
];

const props = withDefaults(
  defineProps<{
    activeTab?: string;
    items?: NavItem[];
  }>(),
  {
    activeTab: 'convites',
  }
);

const router = useRouter();
const route = useRoute();

const currentActiveTab = computed(() => {
  if (props.activeTab) return props.activeTab;
  if (route.name === 'Convites') return 'convites';
  if (route.name === 'Membros') return 'membros';
  if (route.name === 'Passes') return 'passes';
  if (route.name === 'AtelierGestao') return 'atelier';
  return props.items?.[0]?.id || 'convites';
});

const navItems = computed(() => (props.items && props.items.length > 0 ? props.items : defaultNavItems));

function navigate(navItem: NavItem) {
  if (navItem.path) {
    router.push(navItem.path).catch(() => {});
  }
}
</script>
