import { defineStore } from 'pinia';
import { ref } from 'vue';
import { apiRequest } from '@/api/http';

export interface ClientItem {
  id: string;
  name: string;
  initials: string;
  dossierNumber: string;
  phone: string;
  cpf: string;
  projectTitle: string;
  tattooLocation: string;
  style: string;
  sessionsCount: number;
  status: 'valid' | 'pending' | 'finished';
  statusLabel: string;
}

export interface ClientArtwork {
  id: string;
  badgeStatus: string;
  dateAndDuration: string;
  title: string;
  conceptDescription: string;
  image: string;
  tags: string[];
  rawLabel: string;
  artistResponsible: string;
  signedTermAvailable: boolean;
  rawPhotosCount: number;
  healingStatus?: string;
}

export interface ClientDossier {
  id: string;
  name: string;
  initials: string;
  phone: string;
  cpf: string;
  dossierNumber: string;
  worksCount: number;
  scheduledWorksCount: number;
  dermalProfile: {
    fitzpatrick: string;
    description: string;
  };
  anamneseStatus: {
    title: string;
    notes: string;
    badge: string;
  };
  artworks: ClientArtwork[];
  preMessageText: string;
  posMessageText: string;
}

export const useClientsStore = defineStore('clients', () => {
  const clients = ref<ClientItem[]>([]);
  const activeDossier = ref<ClientDossier | null>(null);
  const isLoading = ref<boolean>(false);
  const error = ref<string | null>(null);

  // Buscar clientes reais no backend via /api/clientes/busca ou /api/clientes
  async function fetchClients(): Promise<void> {
    isLoading.value = true;
    error.value = null;

    try {
      let data: any[] = [];
      try {
        data = await apiRequest<any[]>('/api/clientes/busca');
      } catch {
        data = await apiRequest<any[]>('/api/clientes');
      }

      if (Array.isArray(data)) {
        clients.value = data.map((c: any) => {
          const initials = c.nome
            ? c.nome.split(' ').filter(Boolean).map((n: string) => n[0]).slice(0, 2).join('').toUpperCase()
            : 'CL';
          const sessoesList = Array.isArray(c.sessoes) ? c.sessoes : [];
          const orcamentosList = Array.isArray(c.orcamentos) ? c.orcamentos : [];
          const latestSession = sessoesList[0];
          const latestOrcamento = orcamentosList[0];

          const sessionsCount = sessoesList.length;
          const isFinished = sessoesList.length > 0 && sessoesList.every((s: any) => s.status === 'CONCLUIDO');
          const status: 'valid' | 'pending' | 'finished' = isFinished
            ? 'finished'
            : (sessionsCount > 0 ? 'valid' : 'pending');

          const statusLabel = isFinished
            ? 'CONCLUÍDO'
            : (status === 'valid' ? 'ANAMNESE VÁLIDA' : 'TERMO PENDENTE');

          return {
            id: String(c.id),
            name: c.nome,
            initials,
            dossierNumber: `DOSSIÊ #${String(c.id).padStart(4, '0')}`,
            phone: c.telefone || '',
            cpf: c.cpf || c.email || '',
            projectTitle: latestSession?.descricaoSessao || latestOrcamento?.ideiasDescricao || c.historicoSaude || 'Sem projeto ativo',
            tattooLocation: latestOrcamento?.localCorpo || 'Local a definir',
            style: c.tipoTrabalho || latestOrcamento?.referencia || 'Estilo a definir',
            sessionsCount,
            status,
            statusLabel,
          };
        });
      } else {
        clients.value = [];
      }
    } catch (err: any) {
      clients.value = [];
      error.value = err?.message || 'Erro ao carregar clientes do banco de dados.';
      console.warn('[ClientsStore] Erro ao sincronizar clientes:', err);
    } finally {
      isLoading.value = false;
    }
  }

  // Buscar Dossiê completo do cliente pelo ID a partir do histórico do banco
  async function fetchClientDossier(clientId: string): Promise<ClientDossier | null> {
    isLoading.value = true;
    error.value = null;

    try {
      const res = await apiRequest<any>(`/api/clientes/${clientId}/historico`);
      const cli = res?.cliente || {};
      const initials = cli.nome
        ? cli.nome.split(' ').filter(Boolean).map((n: string) => n[0]).slice(0, 2).join('').toUpperCase()
        : 'CL';

      const sessoes = Array.isArray(res?.sessoes) ? res.sessoes : [];
      const midias = Array.isArray(res?.midias) ? res.midias : [];
      const termos = Array.isArray(res?.termos) ? res.termos : [];

      const artworks: ClientArtwork[] = sessoes.map((s: any, idx: number) => {
        const midia = midias.find((m: any) => m.agendamentoId === s.id) || midias[idx];
        return {
          id: `art-${s.id}`,
          badgeStatus: s.status === 'CONCLUIDO' ? 'OBRA SELADA & CURADA' : `STATUS: ${s.status}`,
          dateAndDuration: `${new Date(s.dataHora).toLocaleDateString('pt-BR')} • ${s.duracaoMinutos || 120} min`,
          title: s.descricaoSessao || `Sessão #${s.id}`,
          conceptDescription: s.descricaoSessao || 'Registro fotográfico da sessão.',
          image: midia?.url || '/assets/sao_jeronimo_tattoo.jpg',
          tags: ['#TATTOOFLOW', `#${s.status}`],
          rawLabel: 'Foto RAW 4K',
          artistResponsible: `Tatuador #${s.tatuadorId}`,
          signedTermAvailable: termos.some((t: any) => t.agendamentoId === s.id),
          rawPhotosCount: midias.filter((m: any) => m.agendamentoId === s.id).length || 1,
          healingStatus: s.status === 'CONCLUIDO' ? 'Curada 100%' : 'Em andamento',
        };
      });

      const totalTermosCount = res?.totalTermos ?? termos.length;
      const scheduledCount = sessoes.filter((s: any) => s.status === 'AGENDADO' || s.status === 'CONFIRMADO').length;

      const dossier: ClientDossier = {
        id: String(cli.id || clientId),
        name: cli.nome || `Cliente #${clientId}`,
        initials,
        phone: cli.telefone || '',
        cpf: cli.email || '',
        dossierNumber: `#${String(cli.id || clientId).padStart(4, '0')}`,
        worksCount: res?.totalSessoes ?? sessoes.length,
        scheduledWorksCount: scheduledCount,
        dermalProfile: {
          fitzpatrick: 'PERFIL DÉRMICO REGISTRADO',
          description: cli.historicoSaude || 'Histórico clínico cadastrado no prontuário do atelier.',
        },
        anamneseStatus: {
          title: totalTermosCount > 0 ? 'ANAMNESE VÁLIDA' : 'ANAMNESE PENDENTE',
          notes: cli.historicoSaude || 'Sem restrições relatadas.',
          badge: totalTermosCount > 0 ? 'APROVADA' : 'PENDENTE',
        },
        artworks,
        preMessageText:
          'Olá! Seu horário no New Concept Tattoo está próximo. Recomendamos boa hidratação da pele, evitar exposição solar direta e não ingerir bebidas alcoólicas 24h antes.',
        posMessageText:
          'Lembrete D+15: Solicitação de envio de foto em luz natural para avaliação de regeneração epitelial da sessão.',
      };

      activeDossier.value = dossier;
      return dossier;
    } catch (err: any) {
      console.warn('[ClientsStore] Erro ao obter dossiê do backend:', err);
      error.value = err?.message || 'Falha ao carregar dossiê.';
      activeDossier.value = null;
      return null;
    } finally {
      isLoading.value = false;
    }
  }

  // Atualizar mensagem de pré ou pós-cuidado
  async function updateDossierMessage(
    clientId: string,
    type: 'pre' | 'pos',
    message: string
  ): Promise<{ success: boolean; fromBackend: boolean; message: string }> {
    if (activeDossier.value) {
      if (type === 'pre') {
        activeDossier.value.preMessageText = message;
      } else {
        activeDossier.value.posMessageText = message;
      }
    }

    try {
      await apiRequest<any>(`/api/clientes/${clientId}/mensagens`, {
        method: 'PATCH',
        body: JSON.stringify({ type, message }),
      });
      return {
        success: true,
        fromBackend: true,
        message: 'Mensagem atualizada com sucesso no servidor!',
      };
    } catch {
      return {
        success: true,
        fromBackend: false,
        message: 'Mensagem atualizada localmente.',
      };
    }
  }

  // Disparar termo por WhatsApp
  async function dispatchTermoWhatsapp(
    clientId: string
  ): Promise<{ success: boolean; fromBackend: boolean; message: string }> {
    const client = activeDossier.value;
    const clientName = client?.name || `Cliente #${clientId}`;
    const clientPhone = client?.phone || '';

    try {
      await apiRequest<any>(`/api/clientes/${clientId}/disparo-termo`, {
        method: 'POST',
      });
      return {
        success: true,
        fromBackend: true,
        message: `Termo disparado com sucesso via servidor para ${clientName} (${clientPhone}).`,
      };
    } catch (err: any) {
      return {
        success: false,
        fromBackend: false,
        message: err?.message || `Não foi possível disparar o termo no momento.`,
      };
    }
  }

  function getClientById(id: string): ClientItem | undefined {
    return clients.value.find((c) => c.id === id);
  }

  return {
    clients,
    activeDossier,
    isLoading,
    error,
    fetchClients,
    fetchClientDossier,
    updateDossierMessage,
    dispatchTermoWhatsapp,
    getClientById,
  };
});
