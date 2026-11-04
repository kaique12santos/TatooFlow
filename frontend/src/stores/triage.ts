import { defineStore } from 'pinia';
import { computed, ref } from 'vue';
import { apiRequest } from '@/api/http';
import { listarAgendamentos, listarClientesAgenda, listarUsuariosAgenda, registrarSinalAgendamento,
  type ClienteAgenda, type PagamentoSinal, type StatusAgendamento } from '@/api/agendamentos';

export interface AnamneseData {
  allergies: boolean | null;
  anticoagulants: boolean | null;
  keloids: boolean | null;
  diabetesOrBleeding: boolean | null;
  pregnantOrLactating: boolean | null;
  recentMeal: boolean | null;
  recentMealDetail: string;
  receivedAt: string;
  botVerificationId: string;
}
export interface TriageClient {
  id: string;
  clientId: number;
  agendamentoId?: number;
  statusAgendamento?: StatusAgendamento;
  name: string;
  phone: string;
  cpf: string;
  projectTitle: string;
  style: string;
  conceptDescription: string;
  status: 'aguardando' | 'sinal' | 'confirmados';
  statusLabel: string;
  priceTotal: number | null;
  depositAmount: number | null;
  depositPaid: boolean;
  depositReceipt?: string;
  timeAgo: string;
  assignedArtist: string;
  station: string;
  estimatedDuration: string;
  lotId: string;
  termNumber: string;
  isTermSigned?: boolean;
  termSignedAt?: string;
  termPdfUrl?: string;
  anamnese: AnamneseData;
  scheduledSlot?: { day: string; dayLabel: string; shift: string; shiftLabel: string; time: string };
}
interface PreAgendamento {
  id: number;
  cliente: ClienteAgenda;
  ideiasDescricao?: string;
  referencia?: string;
  localCorpo?: string;
  statusTriagem?: string;
  dataCriacao?: string;
}
interface Termo { id: number; agendamentoId: number; dataAssinatura?: string }
export interface SignTermoPayload {
  clientId: string;
  signatureBase64: string;
  termNumber: string;
  agreedAt: string;
  botVerificationId?: string;
  clientCpf?: string;
}
export interface TermoActionResponse {
  success: boolean;
  fromBackend: boolean;
  protocol: string;
  message: string;
  pdfUrl?: string;
}

export const useTriageStore = defineStore('triage', () => {
  const clients = ref<TriageClient[]>([]);
  const selectedClientId = ref('');
  const isLoading = ref(false);
  const error = ref('');
  const selectedClient = computed(() => getClientById(selectedClientId.value));

  function getClientById(id?: string | null): TriageClient | undefined {
    if (!id) return undefined;
    return clients.value.find(c => c.id === id) || clients.value.find(c => String(c.clientId) === id);
  }
  function setSelectedClient(id: string) {
    const client = getClientById(id);
    if (client) selectedClientId.value = client.id;
  }
  async function fetchTriageQueue(): Promise<void> {
    if (isLoading.value) return;
    isLoading.value = true;
    error.value = '';
    try {
      const [pessoas, preAgendamentos, agendamentos, usuarios, pagamentos] = await Promise.all([
        listarClientesAgenda(), apiRequest<PreAgendamento[]>('/api/triagem/pre-agendamentos'),
        listarAgendamentos(), listarUsuariosAgenda(), apiRequest<PagamentoSinal[]>('/api/financeiro/pagamentos'),
      ]);
      const fila: TriageClient[] = [];
      for (const pessoa of pessoas) {
        const pre = preAgendamentos.filter(p => p.cliente?.id === pessoa.id && p.statusTriagem !== 'REJEITADO').sort((a, b) => b.id - a.id)[0];
        const historico = agendamentos.filter(a => a.clienteId === pessoa.id);
        const sessoes = historico.filter(a => ['AGENDADO', 'CONFIRMADO', 'EM_ANDAMENTO'].includes(a.status));
        // Encerrar a última sessão não cria um novo pedido de orçamento.
        if (!sessoes.length && historico.length && !pre) continue;
        for (const sessao of sessoes.length ? sessoes : [undefined]) {
          const sinal = sessao ? pagamentos.find(p => p.agendamentoId === sessao.id && p.tipo === 'SINAL' && p.status === 'CONCLUIDO') : undefined;
          const total = sessao?.valorEstimado ?? null;
          const status = sinal ? 'confirmados' : sessao ? 'sinal' : 'aguardando';
          const inicio = sessao ? new Date(sessao.dataHora) : null;
          const fim = sessao && inicio ? new Date(inicio.getTime() + sessao.duracaoMinutos * 60000) : null;
          const hora = (d: Date) => d.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
          fila.push({
            id: sessao ? `agendamento-${sessao.id}` : `cliente-${pessoa.id}`, clientId: pessoa.id, agendamentoId: sessao?.id,
            statusAgendamento: sessao?.status,
            name: pessoa.nome, phone: pessoa.telefone || '', cpf: '', projectTitle: sessao?.descricaoSessao || pre?.ideiasDescricao || '',
            style: pre?.referencia || '', conceptDescription: pre?.localCorpo || '', status,
            statusLabel: { aguardando: 'AGUARDANDO ORÇAMENTO', sinal: 'SINAL PENDENTE', confirmados: 'CONFIRMADO' }[status],
            priceTotal: total, depositAmount: sinal?.valor ?? (total == null ? null : Math.ceil(Math.round(total * 100) / 2) / 100),
            depositPaid: Boolean(sinal), depositReceipt: sinal?.comprovanteNome,
            timeAgo: inicio ? inicio.toLocaleString('pt-BR') : pre?.dataCriacao ? new Date(pre.dataCriacao).toLocaleString('pt-BR') : '',
            assignedArtist: usuarios.find(u => u.id === sessao?.tatuadorId)?.nome || '', station: '',
            estimatedDuration: sessao ? `${sessao.duracaoMinutos} min` : '', lotId: sessao ? `#${sessao.id}` : pre ? `#${pre.id}` : '', termNumber: '',
            // Ausência de respostas no banco não significa resposta negativa.
            anamnese: { allergies: null, anticoagulants: null, keloids: null, diabetesOrBleeding: null,
              pregnantOrLactating: null, recentMeal: null, recentMealDetail: '', receivedAt: '', botVerificationId: '' },
            scheduledSlot: inicio && fim ? { day: String(inicio.getDate()), dayLabel: inicio.toLocaleDateString('pt-BR'),
              shift: '', shiftLabel: '', time: `${hora(inicio)} — ${hora(fim)}` } : undefined,
          });
        }
      }
      clients.value = fila;
      if (!getClientById(selectedClientId.value)) selectedClientId.value = fila.find(c => c.status === 'sinal')?.id || fila[0]?.id || '';
    } catch (err) {
      clients.value = [];
      selectedClientId.value = '';
      error.value = err instanceof Error ? err.message : 'Não foi possível carregar a triagem.';
    } finally { isLoading.value = false; }
  }
  async function registerDepositWithReceipt(clientId: string, comprovante: File) {
    const client = getClientById(clientId);
    if (!client?.agendamentoId || client.depositAmount == null) throw new Error('Agende a sessão com valor estimado antes de registrar o sinal.');
    const pagamento = await registrarSinalAgendamento(client.agendamentoId, client.depositAmount, comprovante);
    client.depositPaid = true;
    client.depositReceipt = pagamento.comprovanteNome;
    client.depositAmount = pagamento.valor;
    client.status = 'confirmados';
    client.statusAgendamento = 'CONFIRMADO';
    client.statusLabel = 'CONFIRMADO';
    return pagamento;
  }
  async function fetchTermoAnamnese(clientId: string) {
    if (!clients.value.length) await fetchTriageQueue();
    const client = getClientById(clientId);
    if (!client) throw new Error('Cliente não encontrado. Retorne para a triagem.');
    const termos = await apiRequest<Termo[]>(`/api/termos/cliente/${client.clientId}`);
    const termo = termos.filter(t => t.agendamentoId === client.agendamentoId).sort((a, b) => b.id - a.id)[0];
    client.isTermSigned = Boolean(termo?.dataAssinatura);
    client.termNumber = termo ? `#${termo.id}` : '';
    client.termSignedAt = termo?.dataAssinatura;
    client.termPdfUrl = termo ? `/api/termos/${termo.id}/pdf` : undefined;
  }
  async function signTermo(payload: SignTermoPayload): Promise<TermoActionResponse> {
    const client = getClientById(payload.clientId);
    if (!client?.agendamentoId) throw new Error('Selecione um agendamento válido para assinar o termo.');
    const result = await apiRequest<TermoActionResponse>('/api/termos/assinar', {
      method: 'POST', body: JSON.stringify({ ...payload, clientId: String(client.clientId), agendamentoId: client.agendamentoId }),
    });
    await fetchTermoAnamnese(client.id);
    return result;
  }
  async function sendTermoWhatsapp(): Promise<TermoActionResponse> {
    throw new Error('O envio do termo por WhatsApp ainda não está disponível na API.');
  }
  return { clients, selectedClientId, selectedClient, isLoading, error, getClientById, setSelectedClient,
    fetchTriageQueue, registerDepositWithReceipt, fetchTermoAnamnese, signTermo, sendTermoWhatsapp };
});
