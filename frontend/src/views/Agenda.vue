<template>
  <ion-page>
    <ion-content class="bg-[#09090b] text-[#f2ebd9] select-none font-sans" fullscreen>
      <div class="min-h-screen bg-[#09090b] pb-24 max-w-[480px] mx-auto px-4 pt-3 sm:px-6">
        <!-- Reusable Header Component -->
        <AppHeader
          @click-notifications="handleNotifications"
          @click-profile="handleProfile"
        />

        <!-- Reusable Artist & Station Card Component -->
        <ArtistStationCard />

        <!-- Search Field -->
        <div class="mb-4">
          <div class="relative bg-[#141416] rounded-[2px] border border-[rgba(255,255,255,0.06)] flex items-center px-3 py-2.5 shadow-inner focus-within:border-[rgba(201,168,106,0.5)] transition-all">
            <Search class="w-4 h-4 text-[#a39e93] mr-2.5 shrink-0" />
            <input
              v-model="searchQuery"
              type="text"
              placeholder="Buscar cliente, CPF, termo ou projeto..."
              class="w-full bg-transparent text-[13px] font-montserrat text-[#f2ebd9] placeholder-[#a39e93] focus:outline-none"
            />
            <button class="ml-2 text-[#a39e93] hover:text-[#e6c383] transition-colors p-1 cursor-pointer">
              <SlidersHorizontal class="w-4 h-4" />
            </button>
          </div>
        </div>

        <!-- Reusable Status Filter Tabs Component -->
        <StatusFilterTabs
          v-model="activeFilter"
          :tabs="filterTabs"
          @select-filter="activeFilter = $event"
        />

        <!-- Reusable Weekly Calendar Strip Component -->
        <WeekCalendarStrip
          :selected-day="selectedDay"
          :days="weekDays"
          @select-day="selectedDay = $event"
        />

        <!-- Section Header -->
        <div class="flex items-center justify-between mb-3 px-0.5">
          <div class="flex items-center gap-2">
            <h2 class="font-playfair text-[20px] font-semibold text-[#f2ebd9] leading-tight">
              Ordem das Sessões
            </h2>
            <span class="bg-[#141416] border border-[rgba(230,195,131,0.15)] text-[#e6c383] font-inter text-[9px] font-bold px-2 py-0.5 rounded-[2px] tracking-wider uppercase">
              {{ filteredCards.length }} REGISTRADAS
            </span>
          </div>
          <span class="font-jetbrains text-[11px] text-[#a39e93]">
            Bancada #01 Livre às 18:00
          </span>
        </div>

        <!-- Appointments Deck with Reusable SessionCard Component -->
        <div class="space-y-4">
          <SessionCard
            v-for="card in filteredCards"
            :key="card.id"
            :card="card"
            @action="onCardAction"
          />

          <div v-if="filteredCards.length === 0" class="text-center py-12 bg-[#141416] rounded-[2px] border border-[rgba(255,255,255,0.04)]">
            <Calendar class="w-8 h-8 text-[#a39e93] mx-auto mb-2 opacity-40" />
            <p class="font-playfair text-[16px] text-[#f2ebd9]">Nenhuma sessão encontrada</p>
            <p class="font-montserrat text-[12px] text-[#a39e93] mt-1">Tente ajustar a busca ou os filtros acima.</p>
          </div>
        </div>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { IonPage, IonContent } from '@ionic/vue';
import { Search, SlidersHorizontal, Calendar, Syringe, CheckCircle2, FileText, MessageSquare, Pause, Play, AlertCircle, Bell } from '@lucide/vue';

// Components imports
import AppHeader from '../components/AppHeader.vue';
import ArtistStationCard from '../components/ArtistStationCard.vue';
import StatusFilterTabs, { type FilterTab } from '../components/StatusFilterTabs.vue';
import WeekCalendarStrip, { type CalendarDay } from '../components/WeekCalendarStrip.vue';
import SessionCard, { type SessionCardItem } from '../components/SessionCard.vue';
import router from '@/router/index.ts';
import { useAgendaStore } from '@/stores/agenda';

const agendaStore = useAgendaStore();

// Reactive state
const searchQuery = ref('');
const activeFilter = ref('todos');
const selectedDay = ref(24);

// Filter tabs definition
const filterTabs: FilterTab[] = [
  { id: 'todos', label: 'TODOS', count: 5 },
  { id: 'confirmados', label: 'CONFIRMADOS', count: 2, dotColor: 'bg-[#2a3b2e]' },
  { id: 'andamento', label: 'EM ANDAMENTO', count: 1, dotColor: 'bg-[#3d301b]' },
  { id: 'orcamentos', label: 'ORÇAMENTOS', count: 1, dotColor: 'bg-[#182232]' },
  { id: 'concluidos', label: 'CONCLUÍDOS', count: 1, dotColor: 'bg-[#2a2a2c]' },
];

// Week days definition
const weekDays: CalendarDay[] = [
  { dayName: 'SEG', dayNum: 21, status: 'FOLGA', badgeColor: 'text-[#a39e93]' },
  { dayName: 'TER', dayNum: 22, status: '2 SESS', badgeColor: 'text-[#e6c383]' },
  { dayName: 'QUA', dayNum: 23, status: '3 SESS', badgeColor: 'text-[#e6c383]' },
  { dayName: 'QUI', dayNum: 24, status: 'HOJE', badgeColor: 'text-[#09090b]' },
  { dayName: 'SEX', dayNum: 25, status: '4 SESS', badgeColor: 'text-[#e6c383]' },
  { dayName: 'SÁB', dayNum: 26, status: 'LOTADO', badgeColor: 'text-[#e5c281]' },
  { dayName: 'DOM', dayNum: 27, status: 'FECH', badgeColor: 'text-[#a39e93]' },
];

// Sincroniza com a store que implementa a verificação condicional (backend vs mock fallback)
onMounted(async () => {
  await agendaStore.fetchAppointments();
});

const appointments = computed<SessionCardItem[]>(() => agendaStore.appointments);

// Computed filtered list
const filteredCards = computed(() => {
  return appointments.value.filter(card => {
    const matchesFilter = activeFilter.value === 'todos' || card.category === activeFilter.value;
    const q = searchQuery.value.toLowerCase().trim();
    const matchesSearch = !q ||
      card.clientName.toLowerCase().includes(q) ||
      card.tattooTitle.toLowerCase().includes(q) ||
      card.statusText.toLowerCase().includes(q);
    return matchesFilter && matchesSearch;
  });
});

function handleNotifications() {
  console.log('Notifications opened');
}

function handleProfile() {
  console.log('Profile opened');
}

function onCardAction(payload: { action: string; card: SessionCardItem }) {
  console.log(`Action: ${payload.action} for ${payload.card.clientName}`);

  if (payload.action === 'VER TERMO' || payload.action === 'ANAMNESE') {
    router.push({ name: 'TermoAnamnese' });
    return;
  }

  if (payload.action === 'INICIAR' || payload.action === 'PREPARAR BANCADA') {
    // Redireciona para o Kit Cirúrgico & Insumos com contexto da sessão e cliente
    const clientId = payload.card.id === 2 ? 'cli-lucas' : (payload.card.id === 1 ? 'cli-beatriz' : 'cli-helena');
    router.push({
      path: '/alocacao-materiais',
      query: {
        clientId,
        sessionId: String(payload.card.id),
        from: 'agenda',
      },
    });
    return;
  }
}
</script>
