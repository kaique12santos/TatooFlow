<template>
  <ion-page><ion-content><main class="min-h-screen bg-[#09090b] text-[#f2ebd9] p-6 max-w-lg mx-auto">
    <h1 class="text-3xl mb-3">Ativar acesso de tatuador</h1>
    <p class="text-[#a39e93] mb-6">Defina seu PIN de 4 a 6 d?gitos. Seu acesso ficar? vinculado a este aparelho.</p>
    <p v-if="!codigo" role="alert">Abra o link de convite recebido do administrador.</p>
    <form v-else @submit.prevent="accept" class="space-y-4">
      <label class="block">Nome<input v-model="nome" required maxlength="255" class="block w-full bg-[#141416] border p-3 mt-2" /></label>
      <label class="block">PIN<input v-model="pin" required type="password" inputmode="numeric" pattern="[0-9]{4,6}" minlength="4" maxlength="6" autocomplete="new-password" class="block w-full bg-[#141416] border p-3 mt-2" /></label>
      <label class="block">Confirme o PIN<input v-model="confirmacao" required type="password" inputmode="numeric" maxlength="6" autocomplete="new-password" class="block w-full bg-[#141416] border p-3 mt-2" /></label>
      <button :disabled="loading" class="border border-[#b89355] p-3 w-full disabled:opacity-40">{{ loading ? 'Ativando...' : 'Aceitar convite' }}</button>
    </form>
    <p v-if="error" role="alert" class="text-red-400 mt-4">{{ error }}</p>
    <button class="mt-6 text-[#e6c383]" @click="router.push('/login')">Ir para login</button>
  </main></ion-content></ion-page>
</template>
<script setup lang="ts">
import { ref, computed } from 'vue';
import { IonPage, IonContent } from '@ionic/vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
const route = useRoute(); const router = useRouter(); const auth = useAuthStore();
const codigo = computed(() => typeof route.query.codigo === 'string' ? route.query.codigo : '');
const nome = ref(''); const pin = ref(''); const confirmacao = ref(''); const loading = ref(false); const error = ref('');
async function accept() {
  if (loading.value) return;
  error.value = '';
  if (pin.value !== confirmacao.value) { error.value = 'Os PINs n?o coincidem.'; return; }
  loading.value = true;
  try {
    const base = (import.meta.env.VITE_API_BASE_URL || import.meta.env.VITE_API_URL || '').replace(/\/$/, '');
    const response = await fetch(`${base}/api/convites/${encodeURIComponent(codigo.value)}/aceitar`, {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ nome: nome.value.trim(), pin: pin.value, aparelhoId: auth.aparelhoId }),
    });
    const result = await response.json();
    if (!response.ok || !result.success) throw new Error(result.message || 'N?o foi poss?vel aceitar o convite.');
    pin.value = ''; confirmacao.value = '';
    alert('Acesso criado. Entre com o PIN que voc? definiu neste aparelho.');
    await router.push('/login');
  } catch (err) { error.value = err instanceof Error ? err.message : 'Falha de conex?o.'; }
  finally { loading.value = false; }
}
</script>
