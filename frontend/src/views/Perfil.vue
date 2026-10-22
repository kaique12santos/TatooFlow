<template>
  <ion-page>
    <ion-content class="bg-[#09090b] text-[#f2ebd9]">
      <main class="min-h-screen max-w-[480px] mx-auto px-5 py-5 pb-10">
        <button @click="router.back()" aria-label="Voltar" class="w-10 h-10 flex items-center justify-center text-[#d6b77e] mb-8">
          <ArrowLeft class="w-5 h-5" />
        </button>
        <p class="font-inter text-[10px] tracking-[3px] text-[#d6b77e] uppercase">Conta</p>
        <h1 class="font-playfair text-3xl text-[#f2ebd9] mt-2">Meu perfil</h1>
        <section class="mt-8 bg-[#141416] border border-[rgba(201,168,106,0.18)] rounded-lg p-5">
          <div class="w-16 h-16 rounded-full bg-[#1d1a15] border border-[#b89355] flex items-center justify-center text-[#d6b77e] font-playfair text-2xl">{{ initials }}</div>
          <h2 class="font-playfair text-2xl mt-5">{{ authStore.user.name }}</h2>
          <p class="font-inter text-sm text-[#a39e93] mt-1">{{ authStore.user.email }}</p>
          <div class="mt-6 pt-4 border-t border-[rgba(201,168,106,0.14)] space-y-4">
            <div><p class="font-inter text-[10px] uppercase tracking-[1.5px] text-[#a39e93]">Acesso</p><p class="font-inter text-sm text-[#f2ebd9] mt-1">{{ roleLabel }}</p></div>
            <div><p class="font-inter text-[10px] uppercase tracking-[1.5px] text-[#a39e93]">Estação</p><p class="font-inter text-sm text-[#f2ebd9] mt-1">{{ authStore.user.station }}</p></div>
            <div><p class="font-inter text-[10px] uppercase tracking-[1.5px] text-[#a39e93]">ID do aparelho</p><p class="font-mono text-xs text-[#b8afa0] mt-1 break-all">{{ authStore.deviceId }}</p></div>
          </div>
        </section>
        <button @click="signOut" class="mt-5 w-full border border-[rgba(239,139,131,0.4)] text-[#ef8b83] rounded-md py-3 font-inter text-xs font-bold tracking-[1.5px] uppercase">Sair da conta</button>
      </main>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { IonPage, IonContent } from '@ionic/vue';
import { ArrowLeft } from '@lucide/vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();
const roleLabel = computed(() => authStore.user.role === 'admin' ? 'Administrador' : 'Tatuador');
const initials = computed(() => authStore.user.name.split(' ').map((part) => part[0]).slice(0, 2).join('').toUpperCase());

function signOut() {
  authStore.logout();
  router.replace('/login');
}
</script>
