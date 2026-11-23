<template>
  <AnexarImagem v-bind="$attrs" />
</template>

<script lang="ts">
export { MOCK_AVATARS, getRandomAvatar, getMockAvatar } from './AnexarImagem.vue';
</script>

<script setup lang="ts">
import AnexarImagem from './AnexarImagem.vue';
</script>
