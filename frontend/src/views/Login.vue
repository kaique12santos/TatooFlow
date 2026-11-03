<template>
  <ion-page>
    <ion-content class="bg-[#09090b] text-[#f2ebd9]">
      <main class="min-h-screen max-w-[480px] mx-auto px-6 py-12 flex flex-col justify-center">
        <div class="mb-10">
          <p class="font-inter text-[10px] tracking-[3px] text-[#d6b77e] uppercase">New Concept Tattoo</p>
          <h1 class="font-playfair text-4xl text-[#f2ebd9] mt-3">Bem-vindo de volta.</h1>
          <p class="font-inter text-sm text-[#a39e93] mt-3 leading-relaxed">Entre com o PIN e o aparelho autorizado para acessar o TattooFlow.</p>
        </div>
        <form @submit.prevent="submitLogin" class="space-y-5">
          <label class="block">
            <span class="font-inter text-[11px] uppercase tracking-[1.5px] text-[#b8afa0]">PIN</span>
            <input v-model="pin" inputmode="numeric" pattern="[0-9]*" maxlength="6" type="password" autocomplete="one-time-code" class="mt-2 w-full bg-[#141416] border border-[rgba(201,168,106,0.2)] rounded-md px-4 py-3.5 text-lg tracking-[5px] text-[#f2ebd9] focus:outline-none focus:border-[#c9a86a]" placeholder="••••••" />
          </label>
          <label class="block">
            <span class="font-inter text-[11px] uppercase tracking-[1.5px] text-[#b8afa0]">ID do aparelho</span>
            <input v-model="deviceId" type="text" autocomplete="off" class="mt-2 w-full bg-[#141416] border border-[rgba(201,168,106,0.2)] rounded-md px-4 py-3.5 text-sm text-[#f2ebd9] focus:outline-none focus:border-[#c9a86a]" placeholder="ID autorizado pela gestão" />
          </label>
          <p v-if="errorMessage" class="font-inter text-xs text-[#ef8b83]">{{ errorMessage }}</p>
          <button type="submit" :disabled="authStore.isLoading" class="w-full bg-[#c9a86a] text-[#09090b] rounded-md py-3.5 font-inter text-xs font-bold tracking-[1.5px] uppercase disabled:opacity-50">
            {{ authStore.isLoading ? 'Entrando...' : 'Entrar' }}
          </button>
        </form>
      </main>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { IonPage, IonContent } from '@ionic/vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();
const pin = ref('');
const deviceId = ref(authStore.deviceId);
const errorMessage = ref('');

async function submitLogin() {
  errorMessage.value = '';
  if (!/^[0-9]{4,6}$/.test(pin.value) || !deviceId.value.trim()) {
    errorMessage.value = 'Informe um PIN de 4 a 6 dígitos e o ID do aparelho.';
    return;
  }
  const result = await authStore.login(pin.value, deviceId.value.trim());
  if (!result.success) {
    errorMessage.value = result.message;
    return;
  }
  router.replace('/');
}
</script>
