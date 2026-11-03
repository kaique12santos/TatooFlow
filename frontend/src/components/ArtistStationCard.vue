<template>
  <section class="bg-[#141416] rounded-[2px] p-4 mb-4 border border-[rgba(201,168,106,0.15)] shadow-xl relative overflow-hidden">
    <div class="absolute -right-10 -top-10 w-32 h-32 bg-[rgba(201,168,106,0.03)] rounded-full blur-2xl pointer-events-none"></div>

    <ion-grid class="ion-no-padding">
      <!-- Top Row: Artist & Station on left, Status & Date on right -->
      <ion-row class="ion-align-items-center ion-justify-content-between mb-3.5">
        <ion-col size="12" size-sm="7" class="ion-no-padding">
          <div class="flex items-center gap-3">
            <div class="w-13 h-13 rounded-[2px] overflow-hidden border border-[rgba(201,168,106,0.3)] bg-[#09090b] shrink-0">
              <img :src="avatarUrl" :alt="artistName" class="w-full h-full object-cover" />
            </div>
            <div>
              <span class="font-inter text-[9px] font-bold text-[#e6c383] tracking-[1.62px] uppercase block">
                {{ stationLabel }}
              </span>
              <h1 class="font-playfair text-[20px] font-semibold text-[#f2ebd9] leading-tight">
                {{ artistName }}
              </h1>
            </div>
          </div>
        </ion-col>

        <ion-col size="12" size-sm="5" class="ion-no-padding flex flex-row sm:flex-col items-center sm:items-end justify-between sm:justify-start mt-2.5 sm:mt-0 gap-1.5 sm:gap-0">
          <div class="bg-[#3d301b] border border-[rgba(230,195,131,0.2)] rounded-[2px] px-2 py-0.5 flex items-center gap-1 shrink-0">
            <ShieldCheck class="w-3 h-3 text-[#e6c383]" />
            <span class="font-inter text-[9px] font-bold text-[#eedaa2] tracking-wider">{{ statusText }}</span>
          </div>
          <span class="font-jetbrains text-[13px] font-medium text-[#e6c383] sm:mt-1.5">{{ dateText }}</span>
          <span class="font-inter text-[9px] font-bold text-[#a39e93] tracking-[1.44px] uppercase hidden sm:block">{{ weekdayText }}</span>
        </ion-col>
      </ion-row>

      <!-- Stats Grid using Ionic Row & Col -->
      <ion-row class="pt-2 border-t border-[rgba(255,255,255,0.04)] ion-align-items-stretch">
        <ion-col
          v-for="stat in stats"
          :key="stat.label"
          size="4"
          class="ion-no-padding px-1"
        >
          <div class="bg-[#09090b] rounded-[2px] p-2.5 h-full flex flex-col justify-center items-center text-center border border-[rgba(255,255,255,0.02)]">
            <span class="font-inter text-[9px] font-bold text-[#a39e93] tracking-[1.44px] uppercase mb-1">
              {{ stat.label }}
            </span>
            <span :class="['leading-none', stat.valueClass || 'font-jetbrains text-[13px] font-medium text-[#f2ebd9]']">
              {{ stat.value }}
            </span>
          </div>
        </ion-col>
      </ion-row>
    </ion-grid>
  </section>
</template>

<script setup lang="ts">
import { IonGrid, IonRow, IonCol } from '@ionic/vue';
import { ShieldCheck } from '@lucide/vue';

export interface StationStat {
  label: string;
  value: string;
  valueClass?: string;
}

withDefaults(
  defineProps<{
    artistName?: string;
    stationLabel?: string;
    avatarUrl?: string;
    statusText?: string;
    dateText?: string;
    weekdayText?: string;
    stats?: StationStat[];
  }>(),
  {
    artistName: 'Gabriel Dornelles',
    stationLabel: 'BANCADA 01',
    avatarUrl: 'https://s3-alpha-sig.figma.com/img/5d0c/dcc0/9526114594d0aed5412f2474cb8e9edd?Expires=1792368000&Key-Pair-Id=APKAQ4GOSFWCW27IBOMQ&Signature=P1mpY0e-3jYcKZZ4V6lJa-wZX15noDjlie3AaNFYXRFtKCc2JtVtcHKTVPPOKZwlKgjkYuYoSskAd3EeVIl1XhQ~4sdzBkuizJhyHXE~nZd24V6mlagidhhwr0cD5wFHqfJCR9YjP5-LiuIczdRpZaKKHT3Fy8W5OwIyLnEIOSyAvXmD8dJOtSilrMNZKm1gchqHZyy8ymyeSU~ufKlI8L5vrIViaV-RPTBLi18xmOTZc1s~UsqtHIjSOcq-dcYtq2bGGF8HHQbKTt1fo-WM-CrvuCbhBoZk9TlFu4SGOn9SIDoK~QlnKyJzCIFBhL-6kuS61CuNvHxZytSNceFVjw__',
    statusText: 'ATIVO',
    dateText: '24.OUT.2026',
    weekdayText: 'QUINTA-FEIRA',
    stats: () => [
      { label: 'SESSÕES HOJE', value: '03', valueClass: 'font-playfair text-[20px] font-semibold text-[#e6c383]' },
      { label: 'HORAS BANCADA', value: '07h 30m', valueClass: 'font-jetbrains text-[13px] font-medium text-[#f2ebd9]' },
      { label: 'CAUÇÃO RETIDA', value: 'R$ 1.850', valueClass: 'font-jetbrains text-[13px] font-medium text-[#e6c383]' },
    ],
  }
);
</script>
