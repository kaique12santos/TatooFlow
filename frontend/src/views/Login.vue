<template>
  <ion-page>
    <ion-header translucent>
      <ion-toolbar>
        <ion-title>Login</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding bg-[#09090b] text-[#e6c383]">
      <div class="max-w-md mx-auto">
        <h2 class="text-2xl font-semibold mb-4">Entrar</h2>
        <ion-item>
          <ion-label position="floating">Email</ion-label>
          <ion-input></ion-input>
        </ion-item>
        <ion-item class="mt-3">
          <ion-label position="floating">Senha</ion-label>
          <ion-input type="password"></ion-input>
        </ion-item>
        <div class="mt-4">
          <ion-button expand="block" color="primary">Entrar</ion-button>
        </div>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
// simple placeholder login view
</script>
