<template>
  <div class="bg-[#141416] rounded-[2px] p-3 mb-5 border border-[rgba(201,168,106,0.2)] shadow-lg flex items-center justify-between">
    <button
      @click="$emit('prev')"
      class="w-9 h-9 bg-[#09090b] border border-[rgba(255,255,255,0.06)] rounded-[2px] flex items-center justify-center text-[#a39e93] hover:text-[#e6c383] hover:border-[rgba(201,168,106,0.3)] transition-all cursor-pointer"
    >
      <ChevronLeft class="w-4 h-4" />
    </button>

    <div class="flex flex-col items-center text-center">
      <div class="flex items-center gap-2">
        <Calendar class="w-4 h-4 text-[#e6c383]" />
        <span class="font-playfair text-[16px] font-semibold text-[#d6b77e] tracking-[1.5px] uppercase">
          {{ monthYear }}
        </span>
      </div>
      <span class="font-inter text-[9px] font-bold text-[#a39e93] tracking-[1.44px] uppercase mt-0.5">
        {{ subtitle }}
      </span>
    </div>

    <button
      @click="$emit('next')"
      class="w-9 h-9 bg-[#09090b] border border-[rgba(255,255,255,0.06)] rounded-[2px] flex items-center justify-center text-[#a39e93] hover:text-[#e6c383] hover:border-[rgba(201,168,106,0.3)] transition-all cursor-pointer"
    >
      <ChevronRight class="w-4 h-4" />
    </button>
  </div>
</template>

<script setup lang="ts">
import { Calendar, ChevronLeft, ChevronRight } from '@lucide/vue';

withDefaults(
  defineProps<{
    monthYear?: string;
    subtitle?: string;
  }>(),
  {
    monthYear: 'DEZEMBRO / 2026',
    subtitle: 'MÊS VIGENTE · FECHAMENTO CONSOLIDADO',
  }
);

defineEmits(['prev', 'next']);
</script>
