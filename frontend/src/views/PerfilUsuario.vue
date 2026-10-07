<template>
  <ion-page>
    <ion-content>
      <main class="min-h-screen bg-[#09090b] text-[#f2ebd9] max-w-xl mx-auto p-6">
        <button class="text-[#e6c383] mb-8" @click="voltar">Voltar</button>
        <section class="bg-[#141416] border border-[#b89355] rounded p-6">
          <User class="w-12 h-12 text-[#e6c383] mb-4" />
          <h1 class="text-2xl mb-6">Meu perfil</h1>
          <dl class="space-y-5">
            <div><dt class="text-sm text-[#a39e93]">Nome</dt><dd class="text-lg">{{ auth.user.name }}</dd></div>
            <div><dt class="text-sm text-[#a39e93]">E-mail</dt><dd class="break-all">{{ auth.user.email }}</dd></div>
            <div><dt class="text-sm text-[#a39e93]">Perfil</dt><dd>{{ auth.isAdmin ? 'Administrador' : 'Tatuador' }}</dd></div>
          </dl>
          <button
            class="w-full mt-8 p-3 border border-[#b89355] rounded text-[#e6c383] hover:bg-[#1d1a15]"
            @click="sair"
          >Sair</button>
        </section>
      </main>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { IonPage, IonContent } from '@ionic/vue';
import { User } from '@lucide/vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
const router = useRouter();
const auth = useAuthStore();
function sair() {
  auth.logout();
  router.replace('/login');
}
function voltar() {
  router.push(auth.isAdmin ? '/triagem' : '/agenda');
}
</script>
