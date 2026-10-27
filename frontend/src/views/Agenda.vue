<template>
  <ion-page>
    <ion-content class="bg-[#09090b] text-[#f2ebd9]" fullscreen>
      <main class="min-h-screen bg-[#09090b] pb-28 max-w-[560px] mx-auto px-5 pt-4">
        <AppHeader @click-profile="router.push('/perfil')" />
        <div class="flex items-end justify-between gap-3 mt-6 mb-5">
          <div>
            <p class="font-inter text-[10px] tracking-[2px] text-[#c9a86a] uppercase">Sessões do estúdio</p>
            <h1 class="font-playfair text-3xl mt-1">Agenda</h1>
          </div>
          <button @click="router.push('/agendamento-sessao')" class="bg-[#c9a86a] text-[#09090b] rounded px-4 py-3 flex items-center gap-2 text-xs font-bold">
            <Plus class="w-4 h-4" /> Novo agendamento
          </button>
        </div>

        <div class="grid grid-cols-3 gap-2 mb-5">
          <div v-for="stat in resumo" :key="stat.label" class="bg-[#141416] border border-white/5 rounded p-3">
            <p class="text-[9px] tracking-wider text-[#a39e93] uppercase">{{ stat.label }}</p>
            <p class="font-playfair text-2xl text-[#e6c383] mt-1">{{ stat.value }}</p>
          </div>
        </div>

        <p v-if="aviso" role="status" class="mb-4 rounded border border-green-200/20 bg-green-950/30 p-3 text-sm text-[#c8e6c9]">{{ aviso }}</p>
        <div v-if="agendaStore.error" role="alert" class="mb-4 rounded border border-red-300/30 p-3 text-sm text-[#ef8b83]">
          {{ agendaStore.error }}
          <button @click="agendaStore.fetchAppointments()" class="block mt-2 underline">Tentar novamente</button>
        </div>

        <div class="bg-[#141416] border border-white/10 rounded p-3 mb-4 space-y-3">
          <label class="flex items-center gap-2">
            <Search class="w-4 h-4 text-[#a39e93]" />
            <input v-model="busca" aria-label="Buscar sessão" placeholder="Buscar cliente, tatuador ou sessão" class="w-full bg-transparent text-sm outline-none" />
          </label>
          <div class="flex items-center gap-3 border-t border-white/5 pt-3">
            <label for="data-agenda" class="text-xs text-[#a39e93]">Data</label>
            <input id="data-agenda" v-model="dataSelecionada" type="date" class="bg-[#09090b] text-sm rounded px-2 py-1 [color-scheme:dark]" />
            <button v-if="dataSelecionada" @click="dataSelecionada = ''" class="text-xs text-[#e6c383]">Todas as datas</button>
            <button @click="agendaStore.fetchAppointments()" :disabled="agendaStore.isLoading" aria-label="Atualizar agenda" class="ml-auto p-2 text-[#e6c383] disabled:opacity-40">
              <RefreshCw :class="['w-4 h-4', { 'animate-spin': agendaStore.isLoading }]" />
            </button>
          </div>
        </div>

        <StatusFilterTabs v-model="filtro" :tabs="filtros" @select-filter="filtro = $event" />
        <p v-if="agendaStore.isLoading" role="status" class="py-5 text-center text-sm text-[#a39e93]">Carregando sessões...</p>
        <div v-else class="space-y-4">
          <SessionCard v-for="card in cards" :key="card.id" :card="card" :busy="agendaStore.updatingId !== null" @action="alterarStatus" />
          <div v-if="cards.length === 0 && !agendaStore.error" class="text-center py-10 bg-[#141416] rounded border border-white/5">
            <Calendar class="w-8 h-8 text-[#c9a86a] mx-auto mb-3" />
            <h2 class="font-playfair text-xl">Nenhuma sessão encontrada</h2>
            <p class="text-sm text-[#a39e93] mt-2 px-5">Cadastre uma sessão ou ajuste os filtros.</p>
            <button @click="router.push('/agendamento-sessao')" class="text-sm text-[#e6c383] underline mt-4">Criar agendamento</button>
          </div>
        </div>
        <p class="mt-6 text-xs leading-relaxed text-[#a39e93]">Conclua a sessão para habilitar os cuidados pós-sessão. O envio respeita o prazo configurado após o término previsto.</p>
      </main>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { IonPage, IonContent, onIonViewWillEnter } from '@ionic/vue';
import { Calendar, Plus, RefreshCw, Search } from '@lucide/vue';
import { useRoute, useRouter } from 'vue-router';
import AppHeader from '@/components/AppHeader.vue';
import SessionCard, { type SessionCardItem } from '@/components/SessionCard.vue';
import StatusFilterTabs from '@/components/StatusFilterTabs.vue';
import { useAgendaStore } from '@/stores/agenda';
import type { StatusAgendamento } from '@/api/agendamentos';

const router = useRouter();
const route = useRoute();
const agendaStore = useAgendaStore();
const busca = ref('');
const filtro = ref('todos');
const dataSelecionada = ref('');
const aviso = ref('');

onIonViewWillEnter(async () => {
  aviso.value = route.query.criado ? `Agendamento #${route.query.criado} salvo com sucesso.` : '';
  if (route.query.criado) {
    busca.value = '';
    filtro.value = 'todos';
    dataSelecionada.value = '';
    await router.replace({ path: '/agenda', query: {} });
  }
  await agendaStore.fetchAppointments();
});

const daData = computed(() => agendaStore.appointments.filter((card) =>
  !dataSelecionada.value || agendaStore.registros.find((registro) => registro.id === card.id)?.dataHora.startsWith(dataSelecionada.value)));
const filtros = computed(() => [
  { id: 'todos', label: 'TODOS' }, { id: 'confirmados', label: 'AGENDADOS' },
  { id: 'andamento', label: 'EM ANDAMENTO' }, { id: 'concluidos', label: 'CONCLUÍDOS' },
  { id: 'cancelados', label: 'CANCELADOS' },
].map((item) => ({ ...item, count: daData.value.filter((card) => item.id === 'todos' || card.category === item.id).length })));
const cards = computed(() => daData.value.filter((card) => {
  const texto = `${card.clientName} ${card.tattooTitle} ${card.sessionMeta} ${card.id}`.toLocaleLowerCase('pt-BR');
  return (filtro.value === 'todos' || card.category === filtro.value) && texto.includes(busca.value.trim().toLocaleLowerCase('pt-BR'));
}));
const resumo = computed(() => {
  const hoje = new Date().toLocaleDateString('sv-SE');
  return [
    { label: 'Registradas', value: agendaStore.registros.length },
    { label: 'Hoje', value: agendaStore.registros.filter((sessao) => sessao.dataHora.startsWith(hoje)).length },
    { label: 'Em andamento', value: agendaStore.registros.filter((sessao) => sessao.status === 'EM_ANDAMENTO').length },
  ];
});

async function alterarStatus({ action, card }: { action: string; card: SessionCardItem }) {
  const status: Record<string, StatusAgendamento> = { INICIAR: 'EM_ANDAMENTO', CONCLUIR: 'CONCLUIDO', CANCELAR: 'CANCELADO' };
  const proximo = status[action];
  if (!proximo) return;
  if (action === 'CANCELAR' && !window.confirm(`Cancelar a sessão de ${card.clientName}?`)) return;
  aviso.value = '';
  if (await agendaStore.updateSessionStatus(card.id, proximo)) aviso.value = `Sessão #${card.id} atualizada com sucesso.`;
}
</script>

<style scoped>
button { min-height: 40px; padding: 8px 12px; }
</style>
