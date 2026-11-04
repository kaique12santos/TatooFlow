<template>
  <ion-page>
    <ion-content class="bg-[#09090b] text-[#f2ebd9]" fullscreen>
      <main class="min-h-screen bg-[#09090b] pb-28 max-w-[560px] mx-auto px-5 pt-4">
        <AppHeader @click-profile="router.push('/perfil')" />
        <router-link v-if="authStore.isAdmin" to="/triagem" class="inline-flex items-center gap-2 text-sm text-[#c9a86a] py-2 mt-3">
          <ArrowLeft class="w-4 h-4" /> Retornar para triagem
        </router-link>
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

        <StatusFilterTabs v-model="filtro" :tabs="filtros" />
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
      <SinalDialog :sinal="sinalSelecionado" :loading="carregandoSinal" :error="erroSinal" @close="fecharSinal" @retry="carregarComprovante" />
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { computed, onUnmounted, ref } from 'vue';
import { IonPage, IonContent, onIonViewWillEnter, onIonViewWillLeave } from '@ionic/vue';
import { ArrowLeft, Calendar, Plus, RefreshCw, Search } from '@lucide/vue';
import { useRoute, useRouter } from 'vue-router';
import AppHeader from '@/components/AppHeader.vue';
import SessionCard, { type SessionCardItem } from '@/components/SessionCard.vue';
import StatusFilterTabs from '@/components/StatusFilterTabs.vue';
import SinalDialog, { type SinalDetails } from '@/components/SinalDialog.vue';
import { useAgendaStore } from '@/stores/agenda';
import { useAuthStore } from '@/stores/auth';
import { useAlert } from '@/composables/useAlert';
import { buscarComprovanteSinal, type StatusAgendamento } from '@/api/agendamentos';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
const agendaStore = useAgendaStore();
const alert = useAlert();
const busca = ref('');
const filtro = ref('todos');
const dataSelecionada = ref('');
const aviso = ref('');
const sinalSelecionado = ref<SinalDetails | null>(null);
const agendamentoSinalId = ref<number | null>(null);
const carregandoSinal = ref(false);
const erroSinal = ref('');

onIonViewWillLeave(fecharSinal);
onUnmounted(fecharSinal);

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
  { id: 'todos', label: 'TODOS' }, { id: 'agendados', label: 'AGENDADOS' },
  { id: 'confirmados', label: 'CONFIRMADOS' },
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
  if (action === 'ANEXAR COMPROVANTE') {
    await router.push({ path: '/triagem', query: { agendamentoId: card.id } });
    return;
  }
  if (action === 'VER SINAL') {
    const registro = agendaStore.registros.find((sessao) => sessao.id === card.id);
    if (!registro) return;
    fecharSinal();
    agendamentoSinalId.value = card.id;
    sinalSelecionado.value = {
      clientName: card.clientName,
      total: registro.valorEstimado,
      amount: registro.valorEstimado == null ? null : Math.ceil(Math.round(registro.valorEstimado * 100) / 2) / 100,
    };
    await carregarComprovante();
    return;
  }
  const status: Record<string, StatusAgendamento> = { INICIAR: 'EM_ANDAMENTO', CONCLUIR: 'CONCLUIDO', CANCELAR: 'CANCELADO' };
  const proximo = status[action];
  if (!proximo) return;

  if (action === 'CANCELAR') {
    const confirmou = await alert.confirmDestructive({
      title: 'Cancelar Atendimento',
      description: `Tem certeza que deseja cancelar a sessão de ${card.clientName} (${card.time})? O horário na bancada será liberado.`,
      confirmLabel: 'Sim, Cancelar Sessão',
      cancelLabel: 'Manter Agendamento',
    });
    if (!confirmou) return;
  }

  aviso.value = '';
  if (await agendaStore.updateSessionStatus(card.id, proximo)) {
    const msg = proximo === 'CANCELADO' ? `Sessão #${card.id} foi cancelada.` : `Sessão #${card.id} atualizada com sucesso.`;
    aviso.value = msg;
    await alert.success(msg);
  }
}

async function carregarComprovante() {
  const sinal = sinalSelecionado.value;
  const id = agendamentoSinalId.value;
  if (!sinal || id === null || carregandoSinal.value) return;
  carregandoSinal.value = true;
  erroSinal.value = '';
  try {
    const arquivo = await buscarComprovanteSinal(id);
    if (sinalSelecionado.value !== sinal) return;
    sinal.receiptUrl = URL.createObjectURL(arquivo);
    const extensao = { 'application/pdf': 'pdf', 'image/png': 'png', 'image/jpeg': 'jpg' }[arquivo.type] || 'bin';
    sinal.receiptName = `comprovante-sinal-${id}.${extensao}`;
  } catch (err) {
    if (sinalSelecionado.value === sinal) erroSinal.value = err instanceof Error ? err.message : 'Não foi possível carregar o comprovante.';
  } finally {
    if (sinalSelecionado.value === sinal) carregandoSinal.value = false;
  }
}

function fecharSinal() {
  if (sinalSelecionado.value?.receiptUrl) URL.revokeObjectURL(sinalSelecionado.value.receiptUrl);
  sinalSelecionado.value = null;
  agendamentoSinalId.value = null;
  carregandoSinal.value = false;
  erroSinal.value = '';
}
</script>

<style scoped>
button { min-height: 40px; padding: 8px 12px; }
</style>
