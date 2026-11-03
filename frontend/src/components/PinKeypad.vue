<template>
  <div class="grid grid-cols-3 gap-2.5 max-w-sm mx-auto mb-4">
    <button
      v-for="key in keys"
      :key="key.id"
      @click="$emit('press', key)"
      class="bg-[#141416] border border-[rgba(255,255,255,0.05)] rounded-[2px] py-3.5 flex flex-col items-center justify-center cursor-pointer hover:bg-[#1a1a1e] active:scale-[0.97] transition-all shadow-md"
    >
      <template v-if="key.type === 'digit'">
        <span class="font-playfair text-[20px] font-semibold text-[#f2ebd9] leading-tight">
          {{ key.digit }}
        </span>
        <span class="font-inter text-[8px] font-bold text-[#a39e93] tracking-wider uppercase leading-none mt-0.5">
          {{ key.subtext }}
        </span>
      </template>

      <template v-else-if="key.type === 'bio'">
        <Fingerprint class="w-5 h-5 text-[#e6c383] mb-0.5" />
        <span class="font-inter text-[8px] font-bold text-[#e6c383] tracking-wider uppercase leading-none">
          BIO
        </span>
      </template>

      <template v-else-if="key.type === 'delete'">
        <Delete class="w-5 h-5 text-[#a39e93]" />
      </template>
    </button>
  </div>
</template>

<script setup lang="ts">
import { Fingerprint, Delete } from '@lucide/vue';

export interface KeypadItem {
  id: string;
  type: 'digit' | 'bio' | 'delete';
  digit?: string;
  subtext?: string;
}

const keys: KeypadItem[] = [
  { id: '1', type: 'digit', digit: '1', subtext: '.' },
  { id: '2', type: 'digit', digit: '2', subtext: 'ABC' },
  { id: '3', type: 'digit', digit: '3', subtext: 'DEF' },
  { id: '4', type: 'digit', digit: '4', subtext: 'GHI' },
  { id: '5', type: 'digit', digit: '5', subtext: 'JKL' },
  { id: '6', type: 'digit', digit: '6', subtext: 'MNO' },
  { id: '7', type: 'digit', digit: '7', subtext: 'PQRS' },
  { id: '8', type: 'digit', digit: '8', subtext: 'TUV' },
  { id: '9', type: 'digit', digit: '9', subtext: 'WXYZ' },
  { id: 'bio', type: 'bio' },
  { id: '0', type: 'digit', digit: '0', subtext: '+' },
  { id: 'delete', type: 'delete' },
];

defineEmits<{
  (e: 'press', key: KeypadItem): void;
}>();
</script>
