<template>
  <header class="relative flex items-center justify-between py-3 mb-4 border-b border-[rgba(201,168,106,0.12)]">
    <div class="flex items-center gap-3">
      <div class="w-10 h-10 bg-[#000000] border border-[#b89355] rounded-[2px] p-1 flex items-center justify-center shadow-lg">
        <img :src="logoUrl" alt="New Concept Tattoo" class="w-full h-full object-contain" />
      </div>
      <div>
        <div class="font-cinzel text-[13px] font-semibold text-[#d6b77e] tracking-[2.08px] leading-tight uppercase">
          {{ studioName }}
        </div>
        <div class="font-inter text-[9px] text-[#b8afa0] tracking-[2.52px] leading-none uppercase mt-0.5">
          {{ systemName }}
        </div>
      </div>
    </div>

    <div class="flex items-center gap-2.5">
      <button
        @click="$emit('click-notifications')"
        class="relative w-9 h-9 bg-[#141416] border border-[rgba(201,168,106,0.2)] rounded-full flex items-center justify-center text-[#b8afa0] hover:text-[#d6b77e] transition-colors cursor-pointer"
      >
        <Bell class="w-4 h-4" />
        <span v-if="hasNotifications" class="absolute top-1.5 right-1.5 w-2 h-2 bg-[#c9a86a] rounded-full ring-2 ring-[#09090b]"></span>
      </button>

      <button
        @click="isProfileMenuOpen = !isProfileMenuOpen"
        :aria-expanded="isProfileMenuOpen"
        aria-label="Abrir menu do perfil"
        class="w-9 h-9 bg-[#1d1a15] border border-[#b89355] rounded-full flex items-center justify-center text-[#c9a86a] shadow-inner cursor-pointer hover:border-[#c9a86a] transition-colors"
      >
        <User class="w-4.5 h-4.5" />
      </button>
    </div>
    <div v-if="isProfileMenuOpen" class="absolute right-0 top-14 z-50 w-64 bg-[#141416] border border-[rgba(201,168,106,0.35)] rounded-lg shadow-2xl p-3">
      <div class="border-b border-[rgba(201,168,106,0.14)] pb-3 mb-2">
        <p class="font-inter text-[13px] font-semibold text-[#f2ebd9] truncate">{{ authStore.user.name }}</p>
        <p class="font-inter text-[11px] text-[#a39e93] truncate mt-1">{{ authStore.user.email }}</p>
      </div>
      <button @click="openProfile" class="w-full flex items-center gap-2 px-2 py-2.5 text-left font-inter text-[12px] text-[#f2ebd9] hover:bg-[#1d1a15] rounded-md mt-2">
        <UserRound class="w-4 h-4 text-[#d6b77e]" />
        Ver meu perfil
      </button>
      <button @click="signOut" class="w-full flex items-center gap-2 px-2 py-2.5 text-left font-inter text-[12px] text-[#ef8b83] hover:bg-[#24191a] rounded-md mt-2">
        <LogOut class="w-4 h-4" />
        Sair
      </button>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { Bell, User, UserRound, LogOut } from '@lucide/vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();
const isProfileMenuOpen = ref(false);

function openProfile() {
  isProfileMenuOpen.value = false;
  router.push('/perfil');
}

function signOut() {
  isProfileMenuOpen.value = false;
  authStore.logout();
  router.push('/login');
}

withDefaults(
  defineProps<{
    studioName?: string;
    systemName?: string;
    logoUrl?: string;
    hasNotifications?: boolean;
  }>(),
  {
    studioName: 'NEW CONCEPT TATTOO',
    systemName: 'TATTOOFLOW',
    logoUrl: 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQzGCVzlhfSXkiIXZZP2qsoC7pPL62DUrY1oKrIlnrDXehqLz0M4swPJGqz&s=10',
    hasNotifications: true,
  }
);

defineEmits(['click-notifications', 'click-profile']);
</script>