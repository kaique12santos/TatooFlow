<template>
  <header class="flex items-center justify-between py-3 mb-4 border-b border-[rgba(201,168,106,0.12)]">
    <div class="flex items-center gap-3">
      <div class="w-10 h-10 bg-[#000000] border border-[#b89355] rounded-[2px] p-1 flex items-center justify-center shadow-lg">
        <img :src="logoUrl" alt="New Concept Tattoo" class="w-full h-full object-contain" />
      </div>
      <div>
        <div class="font-cinzel text-[13px] font-semibold text-[#d6b77e] tracking-[2.08px] leading-tight uppercase">
          {{ studioName }}
        </div>
        <div class="font-inter text-[9px] text-[#b8afa0] tracking-[2.52px] leading-none uppercase mt-0.5">
          {{ systemName }}
        </div>
      </div>
    </div>

    <div class="flex items-center gap-2.5">
      <button
        @click="$emit('click-notifications')"
        class="relative w-9 h-9 bg-[#141416] border border-[rgba(201,168,106,0.2)] rounded-full flex items-center justify-center text-[#b8afa0] hover:text-[#d6b77e] transition-colors cursor-pointer"
      >
        <Bell class="w-4 h-4" />
        <span v-if="hasNotifications" class="absolute top-1.5 right-1.5 w-2 h-2 bg-[#c9a86a] rounded-full ring-2 ring-[#09090b]"></span>
      </button>

      <button
        @click="$emit('click-profile')"
        class="w-9 h-9 bg-[#1d1a15] border border-[#b89355] rounded-full flex items-center justify-center text-[#c9a86a] shadow-inner cursor-pointer hover:border-[#c9a86a] transition-colors"
      >
        <User class="w-4.5 h-4.5" />
      </button>
    </div>
  </header>
</template>

<script setup lang="ts">
import { Bell, User } from '@lucide/vue';

withDefaults(
  defineProps<{
    studioName?: string;
    systemName?: string;
    logoUrl?: string;
    hasNotifications?: boolean;
  }>(),
  {
    studioName: 'NEW CONCEPT TATTOO',
    systemName: 'TATTOOFLOW',
    logoUrl: 'https://s3-alpha-sig.figma.com/img/3c73/8bb6/09078c4aab5df9f0c9aa2e5b4489ac50?Expires=1792368000&Key-Pair-Id=APKAQ4GOSFWCW27IBOMQ&Signature=SCdgUzfuAfo61MxW3nRPxZOcJqBEhQi-8pORR9hhvKMCiPgugvyqSkk5p5z~NsYx4HWlSe8d0~9xPpbGi4f0np1DwPhWBszD0Gtf2B-5-ZuCuO~lDpnfdBGO~MsVmMg7G7G~0X2qrANB-bLoTOBm7T3s7JPpz5pmaCt1bhEI3Ja4DW8T1u0TVhCJ-n~ZU3t6Hi37tnzBY1fUiCqqStql4MyyqsQSyU4~bwbhMxIZimqefof0zrJi1lvERse9cv-po2bpTcfnoftzt0aTLVsIoMurlP4ZphisFdZ2MFrErItliBLSAQkiGd7jHKFImnqew8JRbdJ2AI5CYU3ebuHZZQ__',
    hasNotifications: true,
  }
);

defineEmits(['click-notifications', 'click-profile']);
</script>
