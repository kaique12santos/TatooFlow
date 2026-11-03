<script setup lang="ts">
import { IonApp, IonTabs, IonRouterOutlet } from '@ionic/vue';
import { useRoute } from 'vue-router';
import AppNavigation from './components/AppNavigation.vue';

const route = useRoute();
</script>

<template>
  <ion-app>
    <ion-tabs>
      <ion-router-outlet />
      <AppNavigation v-if="route.meta.hideTabBar !== true" />
    </ion-tabs>
  </ion-app>
</template>
