<script setup lang="ts">
import Dock, { type DockItemData } from '@/components/Dock.vue';
import { Users } from '@lucide/vue';
import { computed, h, onMounted, ref, type Ref } from 'vue';
import { RouterView } from 'vue-router';
const isMobile = ref(false);
const items = computed<DockItemData[]>(() => [
  {
    icon: h(Users, { size: isMobile.value ? 20 : 24, color: "#fff" }),
    label: "Agenda",
    onClick: () => console.log("Clicou aqui")
  },
    {
    icon: h(Users, { size: isMobile.value ? 20 : 24, color: "#fff" }),
    label: "Artistas",
    onClick: () => console.log("Clicou aqui")
  },
    {
    icon: h(Users, { size: isMobile.value ? 20 : 24, color: "#fff" }),
    label: "Artistas",
    onClick: () => console.log("Clicou aqui")
  },
    {
    icon: h(Users, { size: isMobile.value ? 20 : 24, color: "#fff" }),
    label: "Artistas",
    onClick: () => console.log("Clicou aqui")
  }
])

const checkScreenSize = () => {
  isMobile.value = window.innerWidth < 640; // 640px é o padrão do 'sm:' no Tailwind
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
  :dock-height="70"
  :magnification="50"
    :distance="isMobile ? 0 : 200" 
  
  class-name="!w-[calc(100%-2rem)] !left-4 !right-4 !translate-x-0 sm:!w-auto sm:!left-1/2 sm:!-translate-x-1/2"
   />
</template>

<style>
  @import "tailwindcss";
</style>
