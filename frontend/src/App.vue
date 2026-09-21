<script setup lang="ts">
import Dock, { type DockItemData } from '@/components/Dock.vue';
import { Calendar, Hammer, Package, Users, Wallet } from '@lucide/vue';
import { computed, h, onMounted, ref, type Ref } from 'vue';
import { RouterView } from 'vue-router';
const isMobile = ref(false);
const items = computed<DockItemData[]>(() => [
  {
    icon: h(Calendar, { size: isMobile.value ? 20 : 24, color: "#fff" }),
    label: "Agenda",
    onClick: () => console.log("Clicou aqui")
  },
    {
    icon: h(Users, { size: isMobile.value ? 20 : 24, color: "#fff" }),
    label: "Artistas",
    onClick: () => console.log("Clicou aqui")
  },
    {
    icon: h(Wallet, { size: isMobile.value ? 20 : 24, color: "#fff" }),
    label: "Fechamento",
    onClick: () => console.log("Clicou aqui")
  },
    {
    icon: h(Hammer, { size: isMobile.value ? 20 : 24, color: "#fff" }),
    label: "Gestão",
    onClick: () => console.log("Clicou aqui")
  },
     {
    icon: h(Package, { size: isMobile.value ? 20 : 24, color: "#fff" }),
    label: "Estoque",
    onClick: () => console.log("Clicou aqui")
  }
])

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
  <router-view />
  <Dock
  :items="items"
  :panel-height="isMobile ? 50 : 70"
  
  :magnification="50"
  
   />
</template>

<style>
  @import "tailwindcss";
</style>
