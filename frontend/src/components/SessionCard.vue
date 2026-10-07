<template>
  <div class="bg-[#141416] rounded-[2px] p-4 border border-[rgba(255,255,255,0.05)] shadow-xl relative transition-all duration-300 hover:border-[rgba(201,168,106,0.3)]">
    <!-- Card Top Header (Time + Status Badge) -->
    <div class="flex items-center justify-between mb-3 pb-2.5 border-b border-[rgba(255,255,255,0.04)]">
      <div class="flex items-center gap-2">
        <span :class="['w-2 h-2 rounded-[1px]', card.typeIndicatorColor]"></span>
        <span class="font-jetbrains text-[13px] font-medium text-[#f2ebd9]">
          {{ card.time }}
        </span>
      </div>

      <div :class="['px-2.5 py-1 rounded-[2px] flex items-center gap-1.5 font-inter text-[9px] font-bold tracking-wider uppercase', card.statusBadgeStyle]">
        <component :is="card.statusIcon" class="w-3 h-3" />
        <span>{{ card.statusText }}</span>
      </div>
    </div>

    <!-- Client & Tattoo Info Grid -->
    <div class="flex items-start gap-3.5 mb-4">
      <div class="w-16 h-16 rounded-[2px] overflow-hidden bg-[#09090b] border border-[rgba(255,255,255,0.08)] shrink-0 shadow-md">
        <img :src="card.image" :alt="card.clientName" class="w-full h-full object-cover" />
      </div>

      <div class="flex-1 min-w-0">
        <h3 class="font-playfair text-[20px] font-semibold text-[#f2ebd9] truncate leading-snug">
          {{ card.clientName }}
        </h3>
        <p class="font-montserrat text-[12px] text-[#e5c281] truncate leading-tight">
          {{ card.tattooTitle }}
        </p>
        <p class="font-montserrat text-[12px] text-[#a39e93] truncate mt-0.5">
          {{ card.tattooArea }}
        </p>
        <div class="flex items-center gap-2 mt-1.5">
          <span class="font-inter text-[9px] font-bold text-[#a39e93] tracking-[1.44px] uppercase bg-[#09090b] px-1.5 py-0.5 rounded-[2px]">
            {{ card.sessionMeta }}
          </span>
        </div>
      </div>
    </div>

    <!-- Financial Ledger Ingot -->
    <div class="bg-[#09090b] rounded-[2px] p-2.5 mb-4 grid grid-cols-3 gap-2 border border-[rgba(255,255,255,0.02)]">
      <div class="flex flex-col">
        <span class="font-inter text-[9px] font-bold text-[#a39e93] tracking-[1.44px] uppercase truncate">
          {{ card.ledgerLabels[0] }}
        </span>
        <span class="font-jetbrains text-[13px] font-medium text-[#f2ebd9] mt-0.5 truncate">
          {{ card.ledgerValues[0] }}
        </span>
      </div>

      <div class="flex flex-col border-l border-[#1a1a1e] pl-2.5">
        <span class="font-inter text-[9px] font-bold text-[#a39e93] tracking-[1.44px] uppercase truncate">
          {{ card.ledgerLabels[1] }}
        </span>
        <span :class="['font-jetbrains text-[13px] font-medium mt-0.5 truncate', card.ledgerValueColors?.[1] || 'text-[#c8e6c9]']">
          {{ card.ledgerValues[1] }}
        </span>
      </div>

      <div class="flex flex-col border-l border-[#1a1a1e] pl-2.5">
        <span class="font-inter text-[9px] font-bold text-[#a39e93] tracking-[1.44px] uppercase truncate">
          {{ card.ledgerLabels[2] }}
        </span>
        <span :class="['font-jetbrains text-[13px] font-medium mt-0.5 truncate', card.ledgerValueColors?.[2] || 'text-[#e6c383]']">
          {{ card.ledgerValues[2] }}
        </span>
      </div>
    </div>

    <!-- Micro Action Buttons -->
    <div class="flex items-center gap-2 pt-1">
      <button
        v-for="btn in card.actions"
        :key="btn.label"
        @click="$emit('action', { action: btn.label, card })"
        :class="[
          'flex-1 py-2 px-3 rounded-[2px] font-inter text-[9px] font-bold tracking-wider uppercase flex items-center justify-center gap-1.5 transition-all duration-200 cursor-pointer',
          btn.primary
            ? 'bg-[#e6c383] text-[#09090b] hover:bg-[#d6b77e] shadow-md active:scale-[0.98]'
            : 'bg-[#1a1a1e] text-[#f2ebd9] hover:bg-[#25252a] border border-[rgba(255,255,255,0.04)] active:scale-[0.98]'
        ]"
      >
        <component :is="btn.icon" class="w-3.5 h-3.5" />
        <span>{{ btn.label }}</span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
export interface CardAction {
  label: string;
  icon: any;
  primary?: boolean;
}

export interface SessionCardItem {
  id: number;
  category: string;
  time: string;
  typeIndicatorColor: string;
  statusText: string;
  statusBadgeStyle: string;
  statusIcon: any;
  image: string;
  clientName: string;
  tattooTitle: string;
  tattooArea: string;
  sessionMeta: string;
  ledgerLabels: [string, string, string];
  ledgerValues: [string, string, string];
  ledgerValueColors?: [string, string, string];
  actions: CardAction[];
}

defineProps<{
  card: SessionCardItem;
}>();

defineEmits(['action']);
</script>
