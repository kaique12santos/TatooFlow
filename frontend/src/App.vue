<script setup lang="ts">
import { Calendar, Hammer, Package, Users, Wallet } from '@lucide/vue';
import { IonApp, IonTabs, IonTabBar, IonTabButton, IonLabel, IonRouterOutlet } from '@ionic/vue';
import { onMounted, ref } from 'vue';
import { RouterView } from 'vue-router';
import router from './router';
const isMobile = ref(false);
const navItems = [
  { name: 'Agenda', icon: Calendar, route: { name: 'Agenda' } },
  { name: 'Clientes', icon: Users, route: { name: 'Clientes' } },
  { name: 'Fechamento', icon: Wallet, route: { name: 'Fechamento' } },
  { name: 'Gestão', icon: Hammer, route: { name: 'Gestao' } },
  { name: 'Estoque', icon: Package, route: { name: 'Estoque' } }
];

const checkScreenSize = () => {
  isMobile.value = window.innerWidth < 640;
};

onMounted(() => {
  checkScreenSize();
  window.addEventListener('resize', checkScreenSize);
  return () => {
    window.removeEventListener('resize', checkScreenSize);

  }
});

</script>

<template>
  <ion-app>
    <ion-tabs>
      <ion-router-outlet />

      <ion-tab-bar slot="bottom" class="ion-no-border" style="--background: #0b0b0c;">
        <ion-tab-button v-for="(it, idx) in navItems" :key="idx" @click="() => router.push(it.route)">
          <component :is="it.icon" :size="20" :color="'#e6c383'" />
          <ion-label>{{ it.name }}</ion-label>
        </ion-tab-button>
      </ion-tab-bar>
    </ion-tabs>
  </ion-app>
</template>

<style>
  @import "tailwindcss";
</style>
