<template>
  <div class="flex items-center justify-center gap-2 mb-6">
    <div
      v-for="i in maxLength"
      :key="i"
      :class="[
        'w-10 h-10 bg-[#09090b] border rounded-[2px] flex items-center justify-center transition-all duration-200',
        i <= length
          ? 'border-[rgba(201,168,106,0.6)] shadow-md shadow-[rgba(201,168,106,0.1)]'
          : 'border-[rgba(255,255,255,0.06)]'
      ]"
    >
      <span
        v-if="i <= length"
        class="w-2.5 h-2.5 bg-[#e6c383] rounded-[1px] animate-pulse"
      ></span>
    </div>
  </div>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    length: number;
    maxLength?: number;
  }>(),
  {
    maxLength: 6,
  }
);
</script>
