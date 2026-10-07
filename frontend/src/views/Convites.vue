<template>
  <ion-page><ion-content><main class="min-h-screen bg-[#09090b] text-[#f2ebd9] p-6 max-w-3xl mx-auto">
    <button class="text-[#e6c383] mb-6" @click="router.push('/equipe-artistas')">Voltar para equipe</button>
    <h1 class="text-3xl mb-3">Convidar novo tatuador</h1>
    <p class="text-[#a39e93] mb-6">O convite vale por 7 dias. O artista define seu nome e PIN ao aceitar.</p>
    <form class="space-y-3" @submit.prevent="create">
      <label class="block">E-mail do artista<input v-model="email" type="email" required maxlength="255" class="block w-full bg-[#141416] border border-[#b89355] p-3 mt-2" /></label>
      <button :disabled="store.isLoading" class="border border-[#b89355] p-3 disabled:opacity-40">Gerar convite</button>
    </form>
    <p v-if="store.error" role="alert" class="text-red-400 mt-4">{{ store.error }}</p>
    <p v-if="message" role="status" class="text-[#e6c383] mt-4">{{ message }}</p>
    <h2 class="text-xl mt-8 mb-4">Convites</h2>
    <p v-if="store.isLoading">Carregando...</p>
    <p v-else-if="!store.invites.length">Nenhum convite cadastrado.</p>
    <article v-for="invite in store.invites" :key="invite.id" class="bg-[#141416] p-4 mb-3 border border-[#b89355]">
      <p>{{ invite.emailConvidado }}</p>
      <p class="text-sm text-[#a39e93]">{{ status(invite) }} ? Expira em {{ new Date(invite.dataExpiracao).toLocaleString('pt-BR') }}</p>
      <template v-if="status(invite) === 'Pendente'">
        <input :value="link(invite.codigo)" readonly aria-label="Link de convite" class="w-full bg-[#09090b] p-2 mt-3" />
        <button class="text-[#e6c383] mt-3" @click="copy(invite.codigo)">Copiar link</button>
      </template>
    </article>
  </main></ion-content></ion-page>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { IonPage, IonContent } from '@ionic/vue';
import { useRouter } from 'vue-router';
import { useInvitesStore, type InviteItem } from '@/stores/invites';
const router = useRouter(); const store = useInvitesStore();
const email = ref(''); const message = ref('');
onMounted(() => store.fetchInvites());
const link = (codigo: string) => `${location.origin}${location.pathname}#/definir-pin?codigo=${encodeURIComponent(codigo)}`;
const status = (invite: InviteItem) => invite.utilizado ? 'Aceito' : new Date(invite.dataExpiracao).getTime() <= Date.now() ? 'Expirado' : 'Pendente';
async function create() {
  message.value = '';
  if (await store.createInvite(email.value.trim())) { email.value = ''; message.value = 'Convite criado. Copie o link e compartilhe com o artista.'; }
}
async function copy(codigo: string) {
  try { await navigator.clipboard.writeText(link(codigo)); message.value = 'Link copiado.'; }
  catch { message.value = 'Selecione e copie o link no campo acima.'; }
}
</script>
