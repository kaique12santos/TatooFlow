<template>
  <div
    v-if="isOpen"
    class="absolute right-0 top-14 z-50 w-[340px] max-w-[calc(100vw-32px)] bg-[#141416] border border-[rgba(201,168,106,0.35)] rounded-lg shadow-2xl overflow-hidden animate-in fade-in slide-in-from-top-2 duration-200"
  >
    <!-- Header -->
    <div class="p-3.5 border-b border-[rgba(201,168,106,0.15)] bg-[#101012] flex items-center justify-between">
      <div class="flex items-center gap-2">
        <Bell class="w-4 h-4 text-[#c9a86a]" />
        <span class="font-jetbrains text-[10px] font-bold text-[#e6c383] tracking-[1.5px] uppercase">
          NOTIFICAÇÕES DO ESTÚDIO
        </span>
        <span
          v-if="unreadCount > 0"
          class="bg-[#3d301b] border border-[rgba(230,195,131,0.3)] text-[#eedaa2] font-jetbrains text-[9px] font-bold px-1.5 py-0.2 rounded-full"
        >
          {{ unreadCount }}
        </span>
      </div>

      <div class="flex items-center gap-2">
        <button
          v-if="unreadCount > 0"
          @click="markAllAsRead"
          class="text-[10px] font-inter text-[#a39e93] hover:text-[#e6c383] transition-colors underline cursor-pointer"
          title="Marcar todas como lidas"
        >
          Marcar lidas
        </button>
        <button
          @click="$emit('close')"
          aria-label="Fechar notificações"
          class="text-[#a39e93] hover:text-[#f2ebd9] p-0.5 transition-colors cursor-pointer"
        >
          <X class="w-4 h-4" />
        </button>
      </div>
    </div>

    <!-- Notification List -->
    <div class="max-h-[380px] overflow-y-auto divide-y divide-[rgba(255,255,255,0.04)]">
      <div
        v-if="items.length === 0"
        class="py-10 px-4 text-center text-[#a39e93] font-inter text-xs"
      >
        <BellOff class="w-8 h-8 mx-auto text-[#63615b] mb-2 opacity-60" />
        <p class="text-[#f2ebd9] font-medium">Nenhuma notificação</p>
        <p class="text-[11px] text-[#716e67] mt-0.5">Tudo em ordem na rotina do atelier.</p>
      </div>

      <div
        v-for="item in items"
        :key="item.id"
        role="button"
        tabindex="0"
        @click="handleClickItem(item)"
        @keydown.enter="handleClickItem(item)"
        @keydown.space.prevent="handleClickItem(item)"
        :class="[
          'p-3 transition-colors cursor-pointer flex items-start gap-3 relative',
          item.lida
            ? 'bg-transparent hover:bg-[#1a1a1e]/60 opacity-75'
            : 'bg-[#18181c] hover:bg-[#202026]'
        ]"
      >
        <!-- Unread dot -->
        <span
          v-if="!item.lida"
          class="absolute left-1.5 top-4 w-1.5 h-1.5 rounded-full bg-[#c9a86a] ring-2 ring-[#141416]"
        ></span>

        <!-- Type Icon -->
        <div
          :class="[
            'w-8 h-8 rounded-[4px] shrink-0 flex items-center justify-center border',
            getIconBadgeClass(item.tipo)
          ]"
        >
          <component :is="getTypeIcon(item.tipo)" class="w-4 h-4" />
        </div>

        <!-- Content -->
        <div class="flex-1 min-w-0 pr-1">
          <div class="flex items-center justify-between gap-1 mb-0.5">
            <h4
              :class="[
                'font-inter text-[12px] truncate leading-tight',
                item.lida ? 'text-[#b8afa0] font-medium' : 'text-[#f2ebd9] font-semibold'
              ]"
            >
              {{ item.titulo }}
            </h4>
            <span class="font-jetbrains text-[9px] text-[#716e67] shrink-0">
              {{ item.tempoAtras }}
            </span>
          </div>

          <p class="font-inter text-[11px] text-[#a39e93] leading-relaxed line-clamp-2">
            {{ item.mensagem }}
          </p>

          <div v-if="item.link" class="mt-1.5 flex items-center gap-1 text-[10px] font-jetbrains text-[#c9a86a] font-medium">
            <span>Visualizar</span>
            <ArrowRight class="w-2.5 h-2.5" />
          </div>
        </div>
      </div>
    </div>

    <!-- Footer -->
    <div
      v-if="items.length > 0"
      class="p-2.5 bg-[#101012] border-t border-[rgba(201,168,106,0.12)] flex items-center justify-between text-[11px]"
    >
      <span class="font-jetbrains text-[9.5px] text-[#716e67]">
        SISTEMA NOTARIAL TATTOOFLOW
      </span>
      <button
        @click="clearAll"
        class="text-[10px] font-inter text-[#ef8b83] hover:underline cursor-pointer"
      >
        Limpar tudo
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import {
  Bell,
  BellOff,
  X,
  AlertTriangle,
  Coins,
  ShieldCheck,
  Calendar,
  MessageSquare,
  ArrowRight,
} from '@lucide/vue';
import { useNotificationsStore } from '@/stores/notifications';
import type { NotificacaoItem } from '@/api/notificacoes';

defineProps<{
  isOpen: boolean;
}>();

const emit = defineEmits<{
  close: [];
}>();

const router = useRouter();
const notificationStore = useNotificationsStore();

const items = computed(() => notificationStore.items);
const unreadCount = computed(() => notificationStore.unreadCount);

function getTypeIcon(tipo: NotificacaoItem['tipo']) {
  switch (tipo) {
    case 'estoque':
      return AlertTriangle;
    case 'sinal':
      return Coins;
    case 'anamnese':
      return ShieldCheck;
    case 'agendamento':
      return Calendar;
    case 'sistema':
    default:
      return MessageSquare;
  }
}

function getIconBadgeClass(tipo: NotificacaoItem['tipo']) {
  switch (tipo) {
    case 'estoque':
      return 'bg-[#3a1416] border-[#ef4444]/40 text-[#f87171]';
    case 'sinal':
      return 'bg-[#102217] border-[#4ade80]/40 text-[#4ade80]';
    case 'anamnese':
      return 'bg-[#2a2416] border-[#e6c383]/40 text-[#e6c383]';
    case 'agendamento':
      return 'bg-[#142338] border-[#60a5fa]/40 text-[#60a5fa]';
    case 'sistema':
    default:
      return 'bg-[#1d1a15] border-[#c9a86a]/40 text-[#c9a86a]';
  }
}

async function handleClickItem(item: NotificacaoItem) {
  await notificationStore.markAsRead(item.id);
  if (item.link) {
    emit('close');
    router.push(item.link);
  }
}

async function markAllAsRead() {
  await notificationStore.markAllAsRead();
}

function clearAll() {
  notificationStore.clearAll();
}
</script>
