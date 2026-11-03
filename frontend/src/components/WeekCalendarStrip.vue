<template>
  <section class="bg-[#141416] rounded-[2px] p-3 mb-5 border border-[rgba(255,255,255,0.04)]">
    <div class="flex items-center justify-between mb-3 px-1">
      <div class="flex items-center gap-2">
        <Calendar class="w-4 h-4 text-[#e6c383]" />
        <span class="font-inter text-[11px] font-bold text-[#f2ebd9] tracking-[1.2px] uppercase">
          {{ title }}
        </span>
      </div>
      <div class="flex items-center gap-1">
        <button @click="$emit('prev-week')" class="p-1 text-[#a39e93] hover:text-[#f2ebd9] rounded transition-colors cursor-pointer">
          <ChevronLeft class="w-4 h-4" />
        </button>
        <button @click="$emit('next-week')" class="p-1 text-[#a39e93] hover:text-[#f2ebd9] rounded transition-colors cursor-pointer">
          <ChevronRight class="w-4 h-4" />
        </button>
      </div>
    </div>

    <!-- 7 Days Grid -->
    <div class="grid grid-cols-7 gap-1 text-center">
      <div
        v-for="day in days"
        :key="day.dayNum"
        @click="$emit('select-day', day.dayNum)"
        :class="[
          'flex flex-col items-center py-2 px-1 rounded-[2px] transition-all cursor-pointer border',
          selectedDay === day.dayNum
            ? 'bg-[#c9a86a] text-[#09090b] border-[#c9a86a] shadow-lg font-bold scale-[1.02]'
            : 'bg-[#09090b] text-[#f2ebd9] border-[rgba(255,255,255,0.02)] hover:border-[rgba(201,168,106,0.3)]'
        ]"
      >
        <span :class="['font-inter text-[9px] uppercase font-semibold', selectedDay === day.dayNum ? 'text-[#412d00]' : 'text-[#a39e93]']">
          {{ day.dayName }}
        </span>
        <span :class="['font-jetbrains text-[15px] font-semibold my-0.5', selectedDay === day.dayNum ? 'text-[#09090b]' : 'text-[#f2ebd9]']">
          {{ day.dayNum }}
        </span>
        <span v-if="selectedDay === day.dayNum" class="w-1.5 h-1.5 rounded-full bg-[#09090b] my-0.5"></span>
        <span
          :class="[
            'font-inter text-[8px] font-bold tracking-tighter uppercase truncate max-w-full',
            selectedDay === day.dayNum ? 'text-[#412d00]' : day.badgeColor || 'text-[#a39e93]'
          ]"
        >
          {{ day.status }}
        </span>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { Calendar, ChevronLeft, ChevronRight } from '@lucide/vue';

export interface CalendarDay {
  dayName: string;
  dayNum: number;
  status: string;
  badgeColor?: string;
}

withDefaults(
  defineProps<{
    title?: string;
    selectedDay: number;
    days: CalendarDay[];
  }>(),
  {
    title: 'SEMANA 43 • OUTUBRO',
  }
);

defineEmits(['select-day', 'prev-week', 'next-week']);
</script>
