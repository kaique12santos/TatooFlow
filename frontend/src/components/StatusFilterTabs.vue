<template>
  <div class="mb-5 overflow-x-auto no-scrollbar scroll-smooth">
    <div class="flex items-center gap-1.5 w-max pb-1">
      <button
        v-for="tab in tabs"
        :key="tab.id"
        @click="$emit('select-filter', tab.id)"
        :class="[
          'flex items-center gap-1.5 px-3 py-1.5 rounded-[2px] font-inter text-[11px] font-semibold transition-all duration-200 cursor-pointer',
          modelValue === tab.id
            ? 'bg-[#c9a86a] text-[#412d00] shadow-md font-bold'
            : 'bg-[#141416] text-[#f2ebd9] hover:bg-[#1a1a1e] border border-[rgba(255,255,255,0.03)]'
        ]"
      >
        <span v-if="tab.dotColor && modelValue !== tab.id" :class="['w-2 h-2 rounded-[1px]', tab.dotColor]"></span>
        <span>{{ tab.label }}</span>
        <span
          :class="[
            'px-1.5 py-0.2 font-jetbrains text-[9px] rounded-[2px]',
            modelValue === tab.id
              ? 'bg-[#09090b] text-[#e6c383]'
              : 'bg-[rgba(255,255,255,0.05)] text-[#a39e93]'
          ]"
        >
          {{ tab.count }}
        </span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
export interface FilterTab {
  id: string;
  label: string;
  count: number;
  dotColor?: string;
}

defineProps<{
  modelValue: string;
  tabs: FilterTab[];
}>();

defineEmits(['select-filter', 'update:modelValue']);
</script>
