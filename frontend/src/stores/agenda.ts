import { defineStore } from 'pinia';
import { computed, markRaw, ref } from 'vue';
import { Calendar, CheckCircle2, FileText, FileUp, Play, Syringe, X } from '@lucide/vue';
import type { SessionCardItem } from '@/components/SessionCard.vue';
import {
  listarAgendamentos, listarClientesAgenda, listarUsuariosAgenda, salvarAgendamento,
  type Agendamento, type ClienteAgenda, type StatusAgendamento, type UsuarioAgenda,
} from '@/api/agendamentos';

export type { SessionCardItem };

export const useAgendaStore = defineStore('agenda', () => {
  const registros = ref<Agendamento[]>([]);
  const clientes = ref<ClienteAgenda[]>([]);
  const usuarios = ref<UsuarioAgenda[]>([]);
  const isLoading = ref(false);
  const error = ref('');
  const updatingId = ref<number | null>(null);

  const statusInfo = {
    AGENDADO: { label: 'AGENDADO', category: 'agendados', icon: Calendar, color: 'text-[#e6c383]' },
    CONFIRMADO: { label: 'CONFIRMADO', category: 'confirmados', icon: CheckCircle2, color: 'text-[#c8e6c9]' },
    EM_ANDAMENTO: { label: 'EM ANDAMENTO', category: 'andamento', icon: Syringe, color: 'text-[#b8d5f5]' },
    CONCLUIDO: { label: 'CONCLUÍDO', category: 'concluidos', icon: CheckCircle2, color: 'text-[#c8e6c9]' },
    CANCELADO: { label: 'CANCELADO', category: 'cancelados', icon: X, color: 'text-[#ef8b83]' },
  };

  const appointments = computed<SessionCardItem[]>(() => registros.value.map((registro) => {
    const status = statusInfo[registro.status] || statusInfo.AGENDADO;
    const inicio = new Date(registro.dataHora);
    const fim = new Date(inicio.getTime() + registro.duracaoMinutos * 60000);
    const hora = (data: Date) => data.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
    const podeAlterar = registro.status === 'AGENDADO' || registro.status === 'CONFIRMADO' || registro.status === 'EM_ANDAMENTO';
    const podeVerSinal = ['CONFIRMADO', 'EM_ANDAMENTO', 'CONCLUIDO'].includes(registro.status);
    return {
      id: registro.id,
      category: status.category,
      time: `${hora(inicio)} — ${hora(fim)}`,
      typeIndicatorColor: 'bg-[#c9a86a]',
      statusText: status.label,
      statusBadgeStyle: `bg-[#09090b] border border-white/10 ${status.color}`,
      statusIcon: markRaw(status.icon),
      image: '',
      clientName: clientes.value.find((cliente) => cliente.id === registro.clienteId)?.nome || `Cliente #${registro.clienteId}`,
      tattooTitle: registro.descricaoSessao || 'Sessão de tatuagem',
      tattooArea: inicio.toLocaleDateString('pt-BR', { day: '2-digit', month: 'long', year: 'numeric' }),
      sessionMeta: usuarios.value.find((usuario) => usuario.id === registro.tatuadorId)?.nome || `Tatuador #${registro.tatuadorId}`,
      ledgerLabels: ['VALOR ESTIMADO', 'DURAÇÃO', 'SESSÃO'],
      ledgerValues: [
        registro.valorEstimado == null ? 'Não informado' : registro.valorEstimado.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' }),
        `${registro.duracaoMinutos} min`,
        `#${registro.id}`,
      ],
      actions: [
        ...(podeVerSinal ? [{ label: 'VER SINAL', icon: markRaw(FileText) }] : []),
        ...(registro.status === 'AGENDADO' ? [{ label: 'ANEXAR COMPROVANTE', icon: markRaw(FileUp), primary: true }] : []),
        ...(registro.status === 'CONFIRMADO' ? [{ label: 'INICIAR', icon: markRaw(Play), primary: true }] : []),
        ...(registro.status === 'CONFIRMADO' || registro.status === 'EM_ANDAMENTO' ? [{ label: 'CONCLUIR', icon: markRaw(CheckCircle2) }] : []),
        ...(podeAlterar ? [{ label: 'CANCELAR', icon: markRaw(X) }] : []),
      ],
    };
  }));

  async function fetchAppointments(): Promise<void> {
    isLoading.value = true;
    error.value = '';
    try {
      const [agenda, pessoas, equipe] = await Promise.all([
        listarAgendamentos(), listarClientesAgenda(), listarUsuariosAgenda(),
      ]);
      registros.value = agenda.sort((a, b) => a.dataHora.localeCompare(b.dataHora) || a.id - b.id);
      clientes.value = pessoas;
      usuarios.value = equipe;
    } catch (err) {
      registros.value = [];
      clientes.value = [];
      usuarios.value = [];
      error.value = err instanceof Error ? err.message : 'Não foi possível carregar a agenda.';
    } finally {
      isLoading.value = false;
    }
  }

  async function updateSessionStatus(id: number, status: StatusAgendamento): Promise<boolean> {
    if (updatingId.value !== null) return false;
    const registro = registros.value.find((sessao) => sessao.id === id);
    if (!registro) return false;
    updatingId.value = id;
    error.value = '';
    try {
      const salvo = await salvarAgendamento({ ...registro, status });
      registros.value = registros.value.map((sessao) => sessao.id === id ? salvo : sessao);
      return true;
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Não foi possível alterar a sessão.';
      return false;
    } finally {
      updatingId.value = null;
    }
  }

  return { registros, appointments, isLoading, error, updatingId, fetchAppointments, updateSessionStatus };
});
