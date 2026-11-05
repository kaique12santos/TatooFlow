import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { useAuthStore } from '@/stores/auth';

export interface AdditionalAngle {
  id: string;
  title: string;
  image: string;
  type: 'image' | 'video';
  verified: boolean;
}

export interface ClientMarketingContext {
  clientName: string;
  station: string;
  artistName: string;
  sessionInfo: string;
  artworkTitle: string;
  artworkStyle: string;
}

export interface TonePreset {
  title: string;
  body: string;
  hashtags: string[];
}

export interface SocialAccount {
  id: string;
  handle: string;
  monogram: string;
  platform: 'instagram' | 'tiktok';
  statusToken: string;
  isValid: boolean;
  engagementPeakTime: string;
  engagementPeakDay: string;
}

export interface PublishSettingsResponse {
  accounts: SocialAccount[];
  activeAccountId: string;
  protocolNumber: string;
  channels: {
    feed: boolean;
    reels: boolean;
    stories: boolean;
    dossie: boolean;
  };
}

export interface ArtworkUploadResponse {
  success: boolean;
  fromBackend: boolean;
  url: string;
  message: string;
}

export interface ArtworkSessionData {
  sessionId?: string;
  clientContext?: Partial<ClientMarketingContext>;
  masterShotUrl?: string;
  additionalAngles?: AdditionalAngle[];
}

export const useMarketingStore = defineStore('marketing', () => {
  // ==========================================
  // CONTEXTO DO CLIENTE E BANCADA (Vindo do Banco de Dados)
  // ==========================================
  const clientContext = ref<ClientMarketingContext>({
    clientName: '',
    station: '',
    artistName: '',
    sessionInfo: '',
    artworkTitle: '',
    artworkStyle: '',
  });

  // ==========================================
  // PASSO 1: FOTOGRAFIA PRINCIPAL & ÂNGULOS
  // ==========================================
  const masterShotUrl = ref<string | null>(null);
  const masterShotFile = ref<File | null>(null);
  const colorProfileActive = ref<boolean>(true);
  const additionalAngles = ref<AdditionalAngle[]>([]);

  // ==========================================
  // PASSO 2: LEGENDA & CURADORIA IA
  // PRESERVADO: Fallback local para caso de falha da API Gemini
  // ==========================================
  const selectedTone = ref<'chiaroscuro' | 'solene' | 'story'>('chiaroscuro');
  const isGeneratingAi = ref<boolean>(false);

  const tonePresets: Record<'chiaroscuro' | 'solene' | 'story', TonePreset> = {
    chiaroscuro: {
      title: 'Estudo de São Jerônimo · Chiaroscuro Renascentista',
      body: "A profundidade do claro-escuro renascentista materializada na pele. [Descrição da Arte: 'Estudo anatômico e iluminação dramática de São Jerônimo em sua biblioteca, trabalhado com pigmento Carbon Black puro e agulhas 03RL para texturas milimétricas']. Executado na região do [Local do Corpo: Antebraço Externo Esquerdo].",
      hashtags: [
        '#NewConceptTattoo',
        '#ChiaroscuroTattoo',
        '#RenaissanceArt',
        '#BlackAndGreyTattoo',
        '#FineLineAtelier',
        '#SaoJeronimoTattoo',
        '#DarkArtGallery',
        '#AtelierMaster',
      ],
    },
    solene: {
      title: 'Registro Notarial de Obra · Acervo São Jerônimo',
      body: 'Lavrado em sessão presencial de alta precisão técnica. A representação clássica do eremita traduzida em linhas 03RL e pigmentação de grau cirúrgico. Protocolo formal de assepsia e curadoria anatômica concluído com rigor notarial.',
      hashtags: [
        '#TattooAtelier',
        '#NotaryTattoo',
        '#FineArtTattoo',
        '#FineLineSpecialist',
        '#SaoJeronimo',
        '#RenaissanceLegacy',
      ],
    },
    story: {
      title: 'Memória Gravada na Derme · A Busca Pelo Silêncio',
      body: 'Mais do que um projeto na pele, uma narrativa de introspecção e disciplina intelectual. Horas de diálogo silencioso entre a máquina e a derme para eternizar São Jerônimo sob uma perspectiva humana e visceral.',
      hashtags: [
        '#TattooJourney',
        '#InkedMemories',
        '#ArtOnSkin',
        '#MeaningfulTattoo',
        '#SaintJerome',
        '#StorytellingTattoo',
      ],
    },
  };

  const captionTitle = ref<string>('');
  const captionBody = ref<string>('');
  const hashtagsList = ref<string[]>([]);
  const selectedHashtags = ref<string[]>([]);

  // ==========================================
  // PASSO 3: PUBLICAÇÃO & DISTRIBUIÇÃO
  // ==========================================
  const selectedPreset = ref<'hoje' | 'amanha' | 'melhor' | null>('melhor');
  const now = new Date();
  const scheduleDate = ref<string>(now.toLocaleDateString('pt-BR'));
  const scheduleDateFormatted = ref<string>(now.toLocaleDateString('pt-BR', { weekday: 'long', day: '2-digit', month: 'long', year: 'numeric' }));
  // Native date inputs use ISO dates; the API payload keeps its existing pt-BR format.
  const scheduleDateInput = computed({
    get: () => scheduleDate.value.split('/').reverse().join('-'),
    set: (value: string) => {
      if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) return;
      const [year, month, day] = value.split('-').map(Number);
      const date = new Date(year!, month! - 1, day!);
      scheduleDate.value = date.toLocaleDateString('pt-BR');
      scheduleDateFormatted.value = date.toLocaleDateString('pt-BR', {
        weekday: 'long', day: '2-digit', month: 'long', year: 'numeric',
      });
      selectedPreset.value = null;
    },
  });
  const scheduleTime = ref<string>('20:45');
  const protocolNumber = ref<string>('');
  const isSubmittingSchedule = ref<boolean>(false);

  const channels = ref({
    feed: true,
    reels: true,
    stories: true,
    dossie: true,
  });

  const socialAccounts = ref<SocialAccount[]>([]);
  const activeAccountId = ref<string>('');

  const activeSocialAccount = computed<SocialAccount | null>(() => {
    const found = socialAccounts.value.find((acc) => acc.id === activeAccountId.value);
    return found || socialAccounts.value[0] || null;
  });

  const fullFormattedCaption = computed(() => {
    if (!captionBody.value) return '';
    return selectedHashtags.value.length
      ? `${captionBody.value}\n\n${selectedHashtags.value.join(' ')}`
      : captionBody.value;
  });

  // ==========================================
  // HELPERS DE REQUISIÇÃO
  // ==========================================
  const apiBaseUrl = ((import.meta.env.VITE_API_BASE_URL || import.meta.env.VITE_API_URL || '') as string).replace(/\/$/, '');

  function getAuthHeaders(extraHeaders: Record<string, string> = {}): Record<string, string> {
    const authStore = useAuthStore();
    const headers: Record<string, string> = { ...extraHeaders };
    if (authStore.token) {
      headers['Authorization'] = `Bearer ${authStore.token}`;
    }
    return headers;
  }

  function setMasterShot(previewUrl: string, file: File | null = null) {
    masterShotUrl.value = previewUrl;
    masterShotFile.value = file;
  }

  function addAdditionalAngle(angle: Omit<AdditionalAngle, 'id'>) {
    const id = `angle-${Date.now()}`;
    additionalAngles.value.push({ ...angle, id });
  }

  // ==========================================
  // ACTIONS DE INTEGRAÇÃO COM BACKEND
  // ==========================================

  // Sincroniza rascunho de curadoria da obra do backend
  async function fetchCuradoria(sessionId?: string): Promise<void> {
    if (!apiBaseUrl) return;
    try {
      const query = sessionId ? `?sessionId=${encodeURIComponent(sessionId)}` : '';
      const response = await fetch(`${apiBaseUrl}/api/marketing/curadoria${query}`, {
        headers: getAuthHeaders(),
      });
      if (response.ok) {
        const data = await response.json();
        if (data && data.body) {
          captionTitle.value = data.title || captionTitle.value || tonePresets.chiaroscuro.title;
          captionBody.value = data.body;
          if (Array.isArray(data.hashtags) && data.hashtags.length > 0) {
            hashtagsList.value = data.hashtags;
            selectedHashtags.value = [...data.hashtags];
          }
          if (data.client) {
            clientContext.value = { ...clientContext.value, ...data.client };
          }
        }
      }
    } catch (err) {
      console.warn('[MarketingStore] Falha ao sincronizar curadoria do backend:', err);
    }
  }

  // Gera ou refina a legenda via API com fallback local para falha do Gemini
  async function generateAiCaption(tone: 'chiaroscuro' | 'solene' | 'story' = selectedTone.value) {
    selectedTone.value = tone;
    isGeneratingAi.value = true;

    try {
      if (apiBaseUrl) {
        const response = await fetch(`${apiBaseUrl}/api/marketing/curadoria-ia`, {
          method: 'POST',
          headers: getAuthHeaders({ 'Content-Type': 'application/json' }),
          body: JSON.stringify({
            tone,
            imageUrl: masterShotUrl.value,
            client: clientContext.value,
          }),
        });

        if (response.ok) {
          const data = await response.json();
          if (data && data.body) {
            captionTitle.value = data.title || tonePresets[tone].title;
            captionBody.value = data.body;
            if (Array.isArray(data.hashtags) && data.hashtags.length > 0) {
              hashtagsList.value = data.hashtags;
              selectedHashtags.value = [...data.hashtags];
            }
            return;
          }
        }
      }
    } catch (error) {
      console.warn('[MarketingStore] Falha na API Gemini do backend. Usando fallback de curadoria IA:', error);
    } finally {
      await new Promise(resolve => setTimeout(resolve, 300));
      isGeneratingAi.value = false;
    }

    // Fallback local do Gemini mantido explicitamente por solicitação
    const preset = tonePresets[tone] || tonePresets.chiaroscuro;
    captionTitle.value = preset.title;
    captionBody.value = preset.body;
    hashtagsList.value = [...preset.hashtags];
    selectedHashtags.value = [...preset.hashtags];
  }

  function toggleHashtag(tag: string) {
    if (selectedHashtags.value.includes(tag)) {
      selectedHashtags.value = selectedHashtags.value.filter(t => t !== tag);
    } else {
      selectedHashtags.value.push(tag);
    }
  }

  function addCustomHashtag(rawTag: string) {
    if (!rawTag || !rawTag.trim()) return;
    const formatted = rawTag.startsWith('#') ? rawTag.trim() : `#${rawTag.trim()}`;
    if (!hashtagsList.value.includes(formatted)) {
      hashtagsList.value.push(formatted);
      selectedHashtags.value.push(formatted);
    } else if (!selectedHashtags.value.includes(formatted)) {
      selectedHashtags.value.push(formatted);
    }
  }

  function updateCaptionBody(text: string) {
    captionBody.value = text;
  }

  function setPreset(preset: 'hoje' | 'amanha' | 'melhor') {
    selectedPreset.value = preset;
    const current = new Date();
    if (preset === 'hoje') {
      scheduleDate.value = current.toLocaleDateString('pt-BR');
      scheduleDateFormatted.value = `Hoje, ${current.toLocaleDateString('pt-BR', { day: '2-digit', month: 'long', year: 'numeric' })}`;
      scheduleTime.value = '21:30';
    } else if (preset === 'amanha') {
      const tomorrow = new Date(current.getTime() + 86400000);
      scheduleDate.value = tomorrow.toLocaleDateString('pt-BR');
      scheduleDateFormatted.value = `Amanhã, ${tomorrow.toLocaleDateString('pt-BR', { day: '2-digit', month: 'long', year: 'numeric' })}`;
      scheduleTime.value = '19:00';
    } else {
      scheduleDate.value = current.toLocaleDateString('pt-BR');
      scheduleDateFormatted.value = current.toLocaleDateString('pt-BR', { weekday: 'long', day: '2-digit', month: 'long', year: 'numeric' });
      scheduleTime.value = activeSocialAccount.value?.engagementPeakTime || '20:45';
    }
  }

  function toggleChannel(channel: 'feed' | 'reels' | 'stories' | 'dossie') {
    channels.value[channel] = !channels.value[channel];
  }

  // Agendamento / Publicação via API
  async function submitSchedule(): Promise<{ success: boolean; protocol: string }> {
    isSubmittingSchedule.value = true;
    try {
      if (!apiBaseUrl) throw new Error('API não configurada');
      const response = await fetch(`${apiBaseUrl}/api/marketing/schedule`, {
        method: 'POST',
        headers: getAuthHeaders({ 'Content-Type': 'application/json' }),
        body: JSON.stringify({
          masterShotUrl: masterShotUrl.value,
          caption: fullFormattedCaption.value,
          scheduleDate: scheduleDate.value,
          scheduleTime: scheduleTime.value,
          channels: channels.value,
          client: clientContext.value,
        }),
      });

      if (response.ok) {
        const data = await response.json();
        const protocol = data.protocol || data.protocolo || `#NC-${Date.now().toString().slice(-6)}`;
        protocolNumber.value = protocol;
        return { success: true, protocol };
      } else {
        throw new Error(`Falha HTTP ${response.status} ao agendar publicação.`);
      }
    } finally {
      isSubmittingSchedule.value = false;
    }
  }

  // Sincroniza configurações de publicação, canais e contas do backend
  async function fetchPublishSettings(): Promise<void> {
    if (!apiBaseUrl) return;
    try {
      const response = await fetch(`${apiBaseUrl}/api/marketing/distribuicao/settings`, {
        headers: getAuthHeaders(),
      });
      if (response.ok) {
        const data = await response.json();
        if (data) {
          if (Array.isArray(data.accounts)) {
            socialAccounts.value = data.accounts;
          }
          if (data.activeAccountId) {
            activeAccountId.value = data.activeAccountId;
          }
          if (data.protocolNumber) {
            protocolNumber.value = data.protocolNumber;
          }
          if (data.channels) {
            channels.value = { ...channels.value, ...data.channels };
          }
        }
      }
    } catch (err) {
      console.warn('[MarketingStore] Erro ao carregar configurações de publicação do backend:', err);
    }
  }

  // Alternar conta social vinculada
  async function switchSocialAccount(targetId?: string): Promise<{ success: boolean; account: SocialAccount | null }> {
    const currentIdx = socialAccounts.value.findIndex((acc) => acc.id === activeAccountId.value);
    const nextAccount = socialAccounts.value[(currentIdx + 1) % (socialAccounts.value.length || 1)];
    const newId = targetId || nextAccount?.id || '';

    if (apiBaseUrl && newId) {
      try {
        const response = await fetch(`${apiBaseUrl}/api/marketing/instagram/switch`, {
          method: 'POST',
          headers: getAuthHeaders({ 'Content-Type': 'application/json' }),
          body: JSON.stringify({ accountId: newId }),
        });
        if (response.ok) {
          const data = await response.json();
          activeAccountId.value = data.activeAccountId || newId;
          return { success: true, account: activeSocialAccount.value };
        }
      } catch (err) {
        console.warn('[MarketingStore] Falha ao alternar conta no backend:', err);
      }
    }

    if (newId) {
      activeAccountId.value = newId;
    }
    return { success: true, account: activeSocialAccount.value };
  }

  // Sincroniza sessão de arte e fotografia real do backend
  async function fetchArtworkSession(sessionId?: string): Promise<void> {
    if (!apiBaseUrl) return;
    try {
      let targetSessionId = sessionId;

      if (!targetSessionId) {
        const agendamentosRes = await fetch(`${apiBaseUrl}/api/agendamentos`, {
          headers: getAuthHeaders(),
        });
        if (agendamentosRes.ok) {
          const envelope = await agendamentosRes.json();
          const list = Array.isArray(envelope) ? envelope : envelope?.data;
          if (Array.isArray(list) && list.length > 0) {
            targetSessionId = String(list[0].id);
          }
        }
      }

      if (targetSessionId) {
        const sessaoRes = await fetch(`${apiBaseUrl}/api/agendamentos/${targetSessionId}`, {
          headers: getAuthHeaders(),
        });
        if (sessaoRes.ok) {
          const envelope = await sessaoRes.json();
          const sessao = envelope?.data || envelope;
          if (sessao) {
            let nomeCliente = 'Cliente';
            if (sessao.clienteId) {
              const cliRes = await fetch(`${apiBaseUrl}/api/clientes/${sessao.clienteId}`, {
                headers: getAuthHeaders(),
              });
              if (cliRes.ok) {
                const cliEnvelope = await cliRes.json();
                const cliData = cliEnvelope?.data || cliEnvelope;
                if (cliData?.nome) nomeCliente = cliData.nome;
              }
            }

            let nomeTatuador = 'Artista';
            if (sessao.tatuadorId) {
              const userRes = await fetch(`${apiBaseUrl}/api/usuarios`, {
                headers: getAuthHeaders(),
              });
              if (userRes.ok) {
                const userEnvelope = await userRes.json();
                const users = userEnvelope?.data || userEnvelope || [];
                const foundUser = Array.isArray(users) ? users.find((u: any) => u.id === sessao.tatuadorId) : null;
                if (foundUser?.nome) nomeTatuador = foundUser.nome;
              }
            }

            clientContext.value = {
              clientName: nomeCliente,
              station: `BANCADA 0${sessao.tatuadorId || 1}`,
              artistName: nomeTatuador,
              sessionInfo: sessao.descricaoSessao || `Sessão #${sessao.id}`,
              artworkTitle: sessao.descricaoSessao || `Obra #${sessao.id}`,
              artworkStyle: 'FINELINE / BLACKWORK',
            };
          }
        }

        const midiasRes = await fetch(`${apiBaseUrl}/api/midias/agendamento/${targetSessionId}`, {
          headers: getAuthHeaders(),
        });
        if (midiasRes.ok) {
          const envelope = await midiasRes.json();
          const midias = Array.isArray(envelope) ? envelope : envelope?.data;
          if (Array.isArray(midias) && midias.length > 0) {
            masterShotUrl.value = midias[0].url;
            additionalAngles.value = midias.slice(1).map((m: any, idx: number) => ({
              id: String(m.id || `angle-${idx}`),
              title: m.tipo || `DETALHE 0${idx + 1}`,
              image: m.url,
              type: m.tipo === 'VIDEO' ? 'video' : 'image',
              verified: true,
            }));
          }
        }
      }
    } catch (err) {
      console.warn('[MarketingStore] Erro ao sincronizar arte do backend:', err);
    }
  }

  // Upload da fotografia principal para o backend
  async function uploadMasterShotFile(file: File, agendamentoId: number = 1): Promise<ArtworkUploadResponse> {
    const localUrl = URL.createObjectURL(file);
    setMasterShot(localUrl, file);

    if (apiBaseUrl) {
      try {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('agendamentoId', String(agendamentoId));
        formData.append('tipo', 'FOTO_DEPOIS');

        const response = await fetch(`${apiBaseUrl}/api/midias/upload`, {
          method: 'POST',
          headers: getAuthHeaders(),
          body: formData,
        });

        if (response.ok) {
          const res = await response.json();
          const midia = res?.data || res;
          if (midia && midia.url) {
            masterShotUrl.value = midia.url;
            return {
              success: true,
              fromBackend: true,
              url: midia.url,
              message: 'Fotografia enviada com sucesso para o banco de dados!',
            };
          }
        }
      } catch (err) {
        console.warn('[MarketingStore] Falha no upload para o backend:', err);
      }
    }

    return {
      success: true,
      fromBackend: false,
      url: localUrl,
      message: 'Fotografia carregada localmente.',
    };
  }

  // Upload de ângulo adicional para o backend
  async function uploadAdditionalAngleFile(
    file: File,
    title = 'DETALHE DA OBRA',
    type: 'image' | 'video' = 'image',
    agendamentoId: number = 1
  ): Promise<ArtworkUploadResponse> {
    const localUrl = URL.createObjectURL(file);
    const tempAngle: AdditionalAngle = {
      id: `angle-${Date.now()}`,
      title,
      image: localUrl,
      type,
      verified: true,
    };
    additionalAngles.value.push(tempAngle);

    if (apiBaseUrl) {
      try {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('agendamentoId', String(agendamentoId));
        formData.append('tipo', type === 'video' ? 'VIDEO' : 'FOTO_DEPOIS');

        const response = await fetch(`${apiBaseUrl}/api/midias/upload`, {
          method: 'POST',
          headers: getAuthHeaders(),
          body: formData,
        });

        if (response.ok) {
          const res = await response.json();
          const midia = res?.data || res;
          if (midia && midia.url) {
            tempAngle.image = midia.url;
            return {
              success: true,
              fromBackend: true,
              url: midia.url,
              message: 'Novo ângulo enviado com sucesso para o banco de dados!',
            };
          }
        }
      } catch (err) {
        console.warn('[MarketingStore] Falha no upload do ângulo adicional:', err);
      }
    }

    return {
      success: true,
      fromBackend: false,
      url: localUrl,
      message: 'Novo ângulo adicionado localmente à galeria da sessão.',
    };
  }

  function resetToDefaults() {
    masterShotUrl.value = null;
    masterShotFile.value = null;
    selectedTone.value = 'chiaroscuro';
    captionTitle.value = '';
    captionBody.value = '';
    hashtagsList.value = [];
    selectedHashtags.value = [];
    selectedPreset.value = 'melhor';
  }

  return {
    clientContext,
    masterShotUrl,
    masterShotFile,
    colorProfileActive,
    additionalAngles,
    selectedTone,
    isGeneratingAi,
    captionTitle,
    captionBody,
    hashtagsList,
    selectedHashtags,
    fullFormattedCaption,
    selectedPreset,
    scheduleDate,
    scheduleDateFormatted,
    scheduleDateInput,
    scheduleTime,
    protocolNumber,
    isSubmittingSchedule,
    channels,
    socialAccounts,
    activeAccountId,
    activeSocialAccount,
    setMasterShot,
    addAdditionalAngle,
    fetchCuradoria,
    fetchPublishSettings,
    fetchArtworkSession,
    uploadMasterShotFile,
    uploadAdditionalAngleFile,
    switchSocialAccount,
    generateAiCaption,
    toggleHashtag,
    addCustomHashtag,
    updateCaptionBody,
    setPreset,
    toggleChannel,
    submitSchedule,
    resetToDefaults,
  };
});
