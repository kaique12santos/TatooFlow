<template>
  <div class="bg-[#141416] rounded-[2px] p-4 mb-4 border border-[rgba(255,255,255,0.05)] hover:border-[rgba(201,168,106,0.3)] transition-all shadow-xl">
    <!-- Header Row -->
    <div class="flex items-start justify-between gap-2 mb-3">
      <div>
        <h3 class="font-playfair text-[20px] font-semibold text-[#f2ebd9] leading-tight">
          {{ artist.name }}
        </h3>
        <p class="font-montserrat text-[12px] text-[#a39e93] mt-0.5">
          {{ artist.station }} • {{ artist.specialty }}
        </p>
      </div>

      <div class="text-right shrink-0">
        <span class="font-inter text-[9px] font-bold text-[#a39e93] tracking-[1.44px] uppercase block">
          TOTAL MÊS
        </span>
        <span class="font-jetbrains text-[16px] font-bold text-[#f2ebd9]">
          {{ artist.totalMonth }}
        </span>
      </div>
    </div>

    <!-- Progress Bar -->
    <div class="w-full bg-[#09090b] h-2 rounded-full overflow-hidden mb-3 border border-[rgba(255,255,255,0.02)]">
      <div
        class="bg-[#c9a86a] h-full rounded-full transition-all duration-500"
        :style="{ width: `${artist.progressPercentage}%` }"
      ></div>
    </div>

    <!-- Breakdown Row (70% Artista / 30% Estúdio) -->
    <div class="pt-2 border-t border-[#26262b] grid grid-cols-2 gap-2">
      <div>
        <span class="font-inter text-[9px] font-bold text-[#a39e93] tracking-[1.44px] uppercase block">
          LÍQUIDO ARTISTA (70%)
        </span>
        <span class="font-jetbrains text-[13px] font-medium text-[#e6c383] mt-0.5 block">
          {{ artist.liquidArtist }}
        </span>
      </div>

      <div class="border-l border-[#26262b] pl-3">
        <span class="font-inter text-[9px] font-bold text-[#a39e93] tracking-[1.44px] uppercase block">
          TAXA ESTÚDIO (30%)
        </span>
        <span class="font-jetbrains text-[13px] font-medium text-[#f2ebd9] mt-0.5 block">
          {{ artist.studioTax }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
export interface ArtistPerformance {
  name: string;
  station: string;
  specialty: string;
  totalMonth: string;
  liquidArtist: string;
  studioTax: string;
  progressPercentage: number;
}

defineProps<{
  artist: ArtistPerformance;
}>();
</script>
