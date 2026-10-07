import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

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
  // CONTEXTO DO CLIENTE E BANCADA
  // ==========================================
  const clientContext = ref<ClientMarketingContext>({
    clientName: 'Camila Albuquerque',
    station: 'BANCADA 01',
    artistName: 'Gabriel Dornelles',
    sessionInfo: 'Sessão 03/03 • Estudo de São Jerônimo...',
    artworkTitle: 'Estudo de São Jerônimo · FineLine Chiaroscuro',
    artworkStyle: '03RL FINELINE • ANTEBRAÇO EXT.',
  });

  // ==========================================
  // PASSO 1: FOTOGRAFIA PRINCIPAL & ÂNGULOS
  // ==========================================
  // Se não houver upload do usuário, mantém fallback para a imagem padrão da sessão
  const masterShotUrl = ref<string | null>('/assets/sao_jeronimo_tattoo.jpg');
  const masterShotFile = ref<File | null>(null);
  const colorProfileActive = ref<boolean>(true);

  const additionalAngles = ref<AdditionalAngle[]>([
    {
      id: 'macro',
      title: 'MACRO LINEWORK',
      image: '/assets/macro_linework_tattoo.jpg',
      type: 'image',
      verified: true,
    },
    {
      id: 'reels',
      title: 'VÍDEO 4K (REELS)',
      image: '/assets/sao_jeronimo_tattoo.jpg',
      type: 'video',
      verified: true,
    },
  ]);

  // ==========================================
  // PASSO 2: LEGENDA & CURADORIA IA
  // ==========================================
  const selectedTone = ref<'chiaroscuro' | 'solene' | 'story'>('chiaroscuro');
  const isGeneratingAi = ref<boolean>(false);

  // Mocks de inteligência artificial por tom
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

  const captionTitle = ref<string>(tonePresets.chiaroscuro.title);
  const captionBody = ref<string>(tonePresets.chiaroscuro.body);
  const hashtagsList = ref<string[]>([...tonePresets.chiaroscuro.hashtags]);
  const selectedHashtags = ref<string[]>([...tonePresets.chiaroscuro.hashtags]);

  // ==========================================
  // PASSO 3: PUBLICAÇÃO & DISTRIBUIÇÃO
  // ==========================================
  const selectedPreset = ref<'hoje' | 'amanha' | 'melhor'>('melhor');
  const scheduleDate = ref<string>('24/MAI/2025');
  const scheduleDateFormatted = ref<string>('Sexta-feira, 24 de Maio de 2025');
  const scheduleTime = ref<string>('20:45');
  const protocolNumber = ref<string>('#NC-2024-0524-78');
  const isSubmittingSchedule = ref<boolean>(false);

  const channels = ref({
    feed: true,
    reels: true,
    stories: true,
    dossie: true,
  });

  const defaultSocialAccounts: SocialAccount[] = [
    {
      id: 'acc-inst-01',
      handle: '@Gabriel_dornelles',
      monogram: 'NC',
      platform: 'instagram',
      statusToken: 'TOKEN META GRAPH V20.0 VÁLIDO',
      isValid: true,
      engagementPeakTime: '20:45',
      engagementPeakDay: 'sextas-feiras',
    },
    {
      id: 'acc-inst-02',
      handle: '@newconcept.tattoo',
      monogram: 'AT',
      platform: 'instagram',
      statusToken: 'TOKEN META GRAPH V20.0 VÁLIDO',
      isValid: true,
      engagementPeakTime: '19:30',
      engagementPeakDay: 'sábados',
    },
  ];

  const socialAccounts = ref<SocialAccount[]>([...defaultSocialAccounts]);

  const activeAccountId = ref<string>('acc-inst-01');

  const activeSocialAccount = computed<SocialAccount>(() => {
    const found = socialAccounts.value.find((acc) => acc.id === activeAccountId.value);
    return found || socialAccounts.value[0] || defaultSocialAccounts[0]!;
  });

  // Resumo da legenda formatada com hashtags
  const fullFormattedCaption = computed(() => {
    return `${captionBody.value}\n\n${selectedHashtags.value.join(' ')}`;
  });

  // ==========================================
  // ACTIONS COM SUPORTE A BACKEND & FALLBACK MOCK
  // ==========================================

  function setMasterShot(previewUrl: string, file: File | null = null) {
    masterShotUrl.value = previewUrl;
    masterShotFile.value = file;
  }

  function addAdditionalAngle(angle: Omit<AdditionalAngle, 'id'>) {
    const id = `angle-${Date.now()}`;
    additionalAngles.value.push({ ...angle, id });
  }

  const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL as string) || (import.meta.env.VITE_API_URL as string) || '';

  // Action com IF: Sincroniza rascunho de curadoria da obra do backend ou mantém fallback do atelier
  async function fetchCuradoria(sessionId?: string): Promise<void> {
    if (apiBaseUrl) {
      try {
        const query = sessionId ? `?sessionId=${encodeURIComponent(sessionId)}` : '';
        const response = await fetch(`${apiBaseUrl}/api/marketing/curadoria${query}`);
        if (response.ok) {
          const data = await response.json();
          if (data && data.body) {
            captionTitle.value = data.title || captionTitle.value;
            captionBody.value = data.body;
            if (Array.isArray(data.hashtags) && data.hashtags.length > 0) {
              hashtagsList.value = data.hashtags;
              selectedHashtags.value = [...data.hashtags];
            }
            if (data.client) {
              clientContext.value = { ...clientContext.value, ...data.client };
            }
            console.log('[MarketingStore] Curadoria carregada do backend:', data);
            return;
          }
        } else {
          console.warn(`[MarketingStore] Backend retornou HTTP ${response.status} na curadoria. Usando fallback.`);
        }
      } catch (err) {
        console.warn('[MarketingStore] Falha ao sincronizar curadoria do backend. Mantendo rascunho local:', err);
      }
    }
  }

  // Gera ou refina a legenda via API com fallback mock se backend estiver ausente
  async function generateAiCaption(tone: 'chiaroscuro' | 'solene' | 'story' = selectedTone.value) {
    selectedTone.value = tone;
    isGeneratingAi.value = true;

    try {
      if (apiBaseUrl) {
        const response = await fetch(`${apiBaseUrl}/api/marketing/curadoria-ia`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
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
      console.warn('Backend indisponível para curadoria IA. Consumindo fallback mock do Atelier:', error);
    } finally {
      // Simula uma resposta fluida de geração caso caia no fallback
      await new Promise(resolve => setTimeout(resolve, 500));
      isGeneratingAi.value = false;
    }

    // Fallback Mock com base no tom literário
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
    if (preset === 'hoje') {
      scheduleDate.value = '23/MAI/2025';
      scheduleDateFormatted.value = 'Hoje, 23 de Maio de 2025';
      scheduleTime.value = '21:30';
    } else if (preset === 'amanha') {
      scheduleDate.value = '24/MAI/2025';
      scheduleDateFormatted.value = 'Amanhã, 24 de Maio de 2025';
      scheduleTime.value = '19:00';
    } else {
      scheduleDate.value = '24/MAI/2025';
      scheduleDateFormatted.value = 'Sexta-feira, 24 de Maio de 2025';
      scheduleTime.value = '20:45';
    }
  }

  function toggleChannel(channel: 'feed' | 'reels' | 'stories' | 'dossie') {
    channels.value[channel] = !channels.value[channel];
  }

  // Agendamento / Publicação via API com fallback mock
  async function submitSchedule(): Promise<{ success: boolean; protocol: string }> {
    isSubmittingSchedule.value = true;
    try {
      if (apiBaseUrl) {
        const response = await fetch(`${apiBaseUrl}/api/marketing/schedule`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
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
          if (data && data.protocol) {
            protocolNumber.value = data.protocol;
            return { success: true, protocol: data.protocol };
          }
        }
      }
    } catch (err) {
      console.warn('Backend indisponível para agendamento. Consumindo fallback mock do protocolo:', err);
    } finally {
      await new Promise(resolve => setTimeout(resolve, 400));
      isSubmittingSchedule.value = false;
    }

    // Fallback Mock do registro
    return { success: true, protocol: protocolNumber.value };
  }

  // Action com IF: Sincroniza configurações de publicação, canais e contas conectadas do backend
  async function fetchPublishSettings(): Promise<void> {
    if (apiBaseUrl) {
      try {
        const response = await fetch(`${apiBaseUrl}/api/marketing/distribuicao/settings`);
        if (response.ok) {
          const data = await response.json();
          if (data) {
            if (Array.isArray(data.accounts) && data.accounts.length > 0) {
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
            console.log('[MarketingStore] Configurações de publicação sincronizadas do backend:', data);
            return;
          }
        } else {
          console.warn(`[MarketingStore] Backend HTTP ${response.status} para configurações de publicação. Mantendo mock.`);
        }
      } catch (err) {
        console.warn('[MarketingStore] Erro ao carregar configurações de publicação do backend:', err);
      }
    } else {
      console.info('[MarketingStore] VITE_API_URL não definida. Mantendo configurações mock de distribuição.');
    }
  }

  // Action com IF: Alternar conta social vinculada (Instagram/Redes)
  async function switchSocialAccount(targetId?: string): Promise<{ success: boolean; account: SocialAccount }> {
    const currentIdx = socialAccounts.value.findIndex((acc) => acc.id === activeAccountId.value);
    const nextAccount = socialAccounts.value[(currentIdx + 1) % socialAccounts.value.length];
    const newId = targetId || nextAccount?.id || 'acc-inst-01';

    if (apiBaseUrl) {
      try {
        const response = await fetch(`${apiBaseUrl}/api/marketing/instagram/switch`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ accountId: newId }),
        });
        if (response.ok) {
          const data = await response.json();
          activeAccountId.value = data.activeAccountId || newId;
          return { success: true, account: activeSocialAccount.value };
        }
      } catch (err) {
        console.warn('[MarketingStore] Falha ao alternar conta no backend. Alternando localmente:', err);
      }
    }

    // Fallback Mock seguro
    activeAccountId.value = newId;
    return { success: true, account: activeSocialAccount.value };
  }

  function resetToDefaults() {
    masterShotUrl.value = '/assets/sao_jeronimo_tattoo.jpg';
    masterShotFile.value = null;
    selectedTone.value = 'chiaroscuro';
    captionTitle.value = tonePresets.chiaroscuro.title;
    captionBody.value = tonePresets.chiaroscuro.body;
    hashtagsList.value = [...tonePresets.chiaroscuro.hashtags];
    selectedHashtags.value = [...tonePresets.chiaroscuro.hashtags];
    selectedPreset.value = 'melhor';
  }

  // Action com IF: Sincroniza sessão de arte e fotografia da bancada do backend
  async function fetchArtworkSession(sessionId?: string): Promise<void> {
    if (apiBaseUrl) {
      try {
        const query = sessionId ? `?sessionId=${encodeURIComponent(sessionId)}` : '';
        const response = await fetch(`${apiBaseUrl}/api/marketing/artes${query}`);
        if (response.ok) {
          const data = await response.json();
          if (data) {
            if (data.clientContext) {
              clientContext.value = { ...clientContext.value, ...data.clientContext };
            }
            if (data.masterShotUrl) {
              masterShotUrl.value = data.masterShotUrl;
            }
            if (Array.isArray(data.additionalAngles) && data.additionalAngles.length > 0) {
              additionalAngles.value = data.additionalAngles;
            }
            console.log('[MarketingStore] Sessão de arte carregada do backend:', data);
            return;
          }
        } else {
          console.warn(`[MarketingStore] Backend HTTP ${response.status} na consulta de arte. Mantendo mock.`);
        }
      } catch (err) {
        console.warn('[MarketingStore] Erro ao sincronizar arte do backend:', err);
      }
    } else {
      console.info('[MarketingStore] VITE_API_URL não definida. Mantendo sessão mock da bancada.');
    }
  }

  // Action com IF: Upload da fotografia principal para o backend (com fallback mock)
  async function uploadMasterShotFile(file: File): Promise<ArtworkUploadResponse> {
    const localUrl = URL.createObjectURL(file);
    setMasterShot(localUrl, file);

    if (apiBaseUrl) {
      try {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('clientName', clientContext.value.clientName);
        formData.append('station', clientContext.value.station);

        const response = await fetch(`${apiBaseUrl}/api/marketing/upload/master`, {
          method: 'POST',
          body: formData,
        });

        if (response.ok) {
          const data = await response.json();
          if (data && data.url) {
            masterShotUrl.value = data.url;
            return {
              success: true,
              fromBackend: true,
              url: data.url,
              message: 'Fotografia principal carregada e armazenada no servidor central!',
            };
          }
        } else {
          console.warn(`[MarketingStore] Erro HTTP ${response.status} no upload da imagem principal.`);
        }
      } catch (err) {
        console.warn('[MarketingStore] Falha no upload da foto principal para o backend:', err);
      }
    }

    // Fallback Mock seguro
    return {
      success: true,
      fromBackend: false,
      url: localUrl,
      message: 'Fotografia principal carregada na bancada (Modo Simulação Local).',
    };
  }

  // Action com IF: Upload de ângulo adicional/vídeo para o backend
  async function uploadAdditionalAngleFile(
    file: File,
    title = 'DETALHE DA OBRA',
    type: 'image' | 'video' = 'image'
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
        formData.append('type', type);
        formData.append('title', title);

        const response = await fetch(`${apiBaseUrl}/api/marketing/upload/angulo`, {
          method: 'POST',
          body: formData,
        });

        if (response.ok) {
          const data = await response.json();
          if (data && data.url) {
            tempAngle.image = data.url;
            return {
              success: true,
              fromBackend: true,
              url: data.url,
              message: 'Novo ângulo enviado com sucesso para o servidor!',
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
