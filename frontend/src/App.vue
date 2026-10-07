<script setup lang="ts">
import { IonApp, IonTabs, IonRouterOutlet } from '@ionic/vue';
import AppNavigation from './components/AppNavigation.vue';
</script>

<template>
  <ion-app>
    <ion-tabs>
      <ion-router-outlet />
      <AppNavigation />
    </ion-tabs>
  </ion-app>
</template>
