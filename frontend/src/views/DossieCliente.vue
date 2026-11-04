<template>
  <ion-page>
    <ion-content class="bg-[#09090b] text-[#f2ebd9] select-none font-sans" fullscreen>
      <div class="min-h-screen bg-[#09090b] pb-24 max-w-[480px] mx-auto px-4 pt-3 sm:px-6">
        <!-- Reusable App Header -->
        <AppHeader
          @click-notifications="handleNotifications"
          @click-profile="handleProfile"
        />

        <!-- Secondary Navigation / Dossier Header -->
        <div class="flex items-center justify-between mb-4 mt-1">
          <button
            @click="handleBack"
            class="flex items-center gap-2 text-[#d6b77e] hover:text-[#f2ebd9] transition-colors cursor-pointer group"
          >
            <ArrowLeft class="w-4 h-4 text-[#d6b77e] group-hover:-translate-x-0.5 transition-transform" />
            <span class="font-inter text-[11px] font-semibold tracking-[2px] uppercase text-[#d6b77e]">
              CATÁLOGO DE CLIENTES
            </span>
          </button>

          <div class="flex items-center gap-1.5 bg-[#141416] border border-[rgba(201,168,106,0.25)] px-3 py-1 rounded-full shadow-sm">
            <span class="w-1.5 h-1.5 rounded-full bg-[#c9a86a]"></span>
            <span class="font-jetbrains text-[10px] font-bold tracking-[1.5px] text-[#f2ebd9] uppercase">
              DOSSIÊ {{ client?.dossierNumber || `#${clientId}` }}
            </span>
          </div>
        </div>

        <!-- Client Information Card with Dermatology & Anamnese Badges -->
        <div class="bg-[#141416] rounded-[2px] p-5 border border-[rgba(201,168,106,0.18)] shadow-xl relative mb-4">
          <!-- Top Row: Name, Contacts & Monogram -->
          <div class="flex items-start justify-between">
            <div class="flex-1 pr-3">
              <h1 class="font-playfair text-[27px] sm:text-[29px] font-semibold text-[#f2ebd9] leading-tight mb-2 tracking-wide">
                {{ client?.name || 'Carregando cliente...' }}
              </h1>
              
              <div class="space-y-1 text-[#b8afa0]">
                <div class="flex items-center gap-2">
                  <Phone class="w-3.5 h-3.5 text-[#c9a86a] shrink-0" />
                  <span class="font-jetbrains text-[12.5px] text-[#b8afa0] tracking-wide">
                    {{ client?.phone || 'Telefone não informado' }}
                  </span>
                </div>
                
                <div class="flex items-center gap-2">
                  <CreditCard class="w-3.5 h-3.5 text-[#c9a86a] shrink-0" />
                  <span class="font-jetbrains text-[12px] text-[#a39e93] tracking-wide">
                    Identificador / E-mail: {{ client?.cpf || 'Não informado' }}
                  </span>
                </div>
              </div>
            </div>

            <!-- Monogram Avatar -->
            <div class="w-14 h-14 bg-[#0c0c0e] border border-[rgba(201,168,106,0.25)] rounded-[2px] flex items-center justify-center shrink-0 shadow-inner">
              <span class="font-playfair font-bold text-[22px] text-[#c9a86a] tracking-wider">
                {{ client?.initials || 'CL' }}
              </span>
            </div>
          </div>

          <!-- Mid Row: Acervo Pessoal & Perfil Dérmico Sub-cards -->
          <div class="grid grid-cols-2 gap-2.5 mt-4 mb-3">
            <!-- Box 1: Acervo Pessoal -->
            <div class="bg-[#0c0c0e] border border-[rgba(255,255,255,0.06)] rounded-[2px] p-2.5">
              <span class="block font-inter text-[9px] font-bold tracking-[1px] text-[#a39e93] uppercase mb-1">
                ACERVO PESSOAL
              </span>
              <div class="flex items-baseline">
                <span class="font-jetbrains text-[18px] font-bold text-[#c9a86a] mr-1.5 leading-none">
                  {{ client?.worksCount || 0 }}
                </span>
                <span class="font-inter text-[11.5px] text-[#f2ebd9]">
                  Obras - {{ client?.scheduledWorksCount || 0 }} Agendada(s)
                </span>
              </div>
            </div>

            <!-- Box 2: Perfil Dérmico -->
            <div class="bg-[#0c0c0e] border border-[rgba(255,255,255,0.06)] rounded-[2px] p-2.5">
              <span class="block font-inter text-[9px] font-bold tracking-[1px] text-[#a39e93] uppercase mb-1">
                PERFIL DÉRMICO
              </span>
              <div class="font-jetbrains text-[11px] font-bold text-[#f2ebd9] uppercase tracking-wide truncate">
                {{ client?.dermalProfile?.fitzpatrick || 'ANAMNESE' }}
              </div>
              <div class="font-inter text-[10.5px] text-[#a39e93] mt-0.5 truncate">
                {{ client?.dermalProfile?.description || 'Histórico clínico registrado.' }}
              </div>
            </div>
          </div>

          <!-- Bottom Row: Anamnese Válida / Aprovada -->
          <div class="bg-[#0c0c0e] border border-[rgba(255,255,255,0.06)] rounded-[2px] p-3 flex items-center justify-between">
            <div class="flex items-center gap-2.5 min-w-0 pr-2">
              <ShieldCheck class="w-4.5 h-4.5 text-[#86efac] shrink-0" />
              <div class="truncate">
                <div class="font-inter text-[10px] font-bold tracking-[1px] text-[#f2ebd9] uppercase leading-tight">
                  {{ client?.anamneseStatus?.title || 'ANAMNESE' }}
                </div>
                <div class="font-inter text-[10.5px] text-[#a39e93] truncate leading-tight mt-0.5">
                  {{ client?.anamneseStatus?.notes || 'Consentimento registrado no banco.' }}
                </div>
              </div>
            </div>

            <div class="bg-[#102619] border border-[#2d6342] px-2.5 py-1 rounded-[2px] shrink-0">
              <span class="font-jetbrains text-[9.5px] font-bold text-[#86efac] tracking-[1.2px] uppercase">
                {{ client?.anamneseStatus?.badge || 'CONCLUÍDA' }}
              </span>
            </div>
          </div>
        </div>

        <!-- Segmented Tabs Navigation -->
        <div class="grid grid-cols-2 gap-2.5 mb-3.5">
          <button
            @click="activeTab = 'obras'"
            :class="[
              'py-2.5 px-3 rounded-[2px] font-inter text-[10.5px] font-bold tracking-[1.2px] uppercase flex items-center justify-center transition-all cursor-pointer',
              activeTab === 'obras'
                ? 'bg-[#c9a86a] text-[#09090b] shadow-md border border-[#c9a86a]'
                : 'bg-transparent border border-[rgba(201,168,106,0.35)] text-[#f2ebd9] hover:border-[#c9a86a]'
            ]"
          >
            OBRAS & SESSÕES
          </button>

          <button
            @click="activeTab = 'pos-cuidado'"
            :class="[
              'py-2.5 px-3 rounded-[2px] font-inter text-[10.5px] font-bold tracking-[1.2px] uppercase flex items-center justify-center transition-all cursor-pointer',
              activeTab === 'pos-cuidado'
                ? 'bg-[#c9a86a] text-[#09090b] shadow-md border border-[#c9a86a]'
                : 'bg-transparent border border-[rgba(201,168,106,0.35)] text-[#f2ebd9] hover:border-[#c9a86a]'
            ]"
          >
            PÓS-CUIDADO & CICATRIZAÇÃO
          </button>
        </div>

        <!-- TAB 1: OBRAS & SESSÕES -->
        <div v-if="activeTab === 'obras'">
          <!-- Records Count Pill Badge -->
          <div class="mb-3">
            <span class="bg-[#141416] border border-[rgba(201,168,106,0.25)] px-2.5 py-1 rounded-[2px] font-jetbrains text-[9.5px] font-bold text-[#c9a86a] tracking-[1.2px] uppercase inline-flex items-center">
              {{ (client?.artworks || []).length }} REGISTROS NO BANCO
            </span>
          </div>

          <!-- Dynamic Artworks List -->
          <div v-if="(client?.artworks || []).length > 0" class="space-y-4 mb-6">
            <div
              v-for="art in client?.artworks"
              :key="art.id"
              class="bg-[#141416] border border-[rgba(201,168,106,0.18)] rounded-[2px] p-4 shadow-xl"
            >
              <!-- Header Line: Badge & Time -->
              <div class="flex items-center justify-between mb-2">
                <span class="bg-[#102619] border border-[#2d6342] text-[#86efac] font-jetbrains text-[9.5px] font-bold px-2 py-0.5 rounded-[2px] tracking-wider uppercase">
                  {{ art.badgeStatus }}
                </span>
                <span class="font-jetbrains text-[10.5px] text-[#a39e93] uppercase">
                  {{ art.dateAndDuration }}
                </span>
              </div>

              <!-- Title & Concept Description -->
              <h2 class="font-playfair text-[20px] font-semibold text-[#f2ebd9] leading-tight mb-1">
                {{ art.title }}
              </h2>
              <p class="font-inter text-[12px] text-[#a39e93] leading-relaxed mb-3">
                {{ art.conceptDescription }}
              </p>

              <!-- Tattoo Portfolio Artwork Box -->
              <div class="relative w-full rounded-[2px] overflow-hidden border border-[rgba(255,255,255,0.08)] bg-[#09090b] mb-3 group">
                <img
                  :src="art.image"
                  :alt="art.title"
                  class="w-full h-48 sm:h-52 object-cover object-center group-hover:scale-[1.02] transition-transform duration-500 cursor-pointer"
                  @click="openGalleryModal(art)"
                />

                <!-- Bottom Overlay Tags -->
                <div class="absolute bottom-2 left-2 flex items-center gap-1.5">
                  <span
                    v-for="tag in art.tags"
                    :key="tag"
                    class="bg-[rgba(9,9,11,0.85)] backdrop-blur-sm border border-[rgba(201,168,106,0.3)] text-[#e6c383] font-jetbrains text-[8.5px] font-bold px-1.5 py-0.5 rounded-[2px] uppercase"
                  >
                    {{ tag }}
                  </span>
                </div>

                <!-- Bottom Right RAW Label -->
                <div class="absolute bottom-2 right-2 bg-[rgba(9,9,11,0.7)] backdrop-blur-sm px-2 py-0.5 rounded-[2px]">
                  <span class="font-jetbrains text-[9.5px] text-[#b8afa0]">
                    {{ art.rawLabel }}
                  </span>
                </div>
              </div>

              <!-- Artista Responsável Section -->
              <div class="border-t border-b border-[rgba(255,255,255,0.06)] py-2.5 my-3">
                <span class="block font-inter text-[9px] font-bold tracking-[1.2px] text-[#7a766f] uppercase">
                  ARTISTA RESPONSÁVEL
                </span>
                <div class="font-inter text-[12.5px] font-medium text-[#f2ebd9] mt-0.5">
                  {{ art.artistResponsible }}
                </div>
              </div>

              <!-- Documentação Jurídica & Arquivos -->
              <div>
                <span class="block font-inter text-[9px] font-bold tracking-[1.4px] text-[#c9a86a] uppercase mb-2">
                  DOCUMENTAÇÃO JURÍDICA & ARQUIVOS
                </span>
                <div class="grid grid-cols-2 gap-2">
                  <button
                    @click="openTermoModal(art)"
                    class="bg-[#0c0c0e] hover:bg-[#19191c] border border-[rgba(201,168,106,0.25)] hover:border-[#c9a86a] p-2.5 rounded-[2px] flex items-center justify-center gap-2 cursor-pointer transition-all"
                  >
                    <CheckCircle2 class="w-4 h-4 text-[#c9a86a]" />
                    <span class="font-inter text-[10px] font-bold tracking-[0.8px] text-[#f2ebd9] uppercase">
                      {{ art.signedTermAvailable ? 'TERMO ASSINADO' : 'TERMO PENDENTE' }}
                    </span>
                  </button>

                  <button
                    @click="openGalleryModal(art)"
                    class="bg-[#0c0c0e] hover:bg-[#19191c] border border-[rgba(201,168,106,0.25)] hover:border-[#c9a86a] p-2.5 rounded-[2px] flex items-center justify-center gap-2 cursor-pointer transition-all"
                  >
                    <Camera class="w-4 h-4 text-[#c9a86a]" />
                    <span class="font-inter text-[10px] font-bold tracking-[0.8px] text-[#f2ebd9] uppercase">
                      FOTOS REGISTRADAS ({{ art.rawPhotosCount }})
                    </span>
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Empty State -->
          <div v-else class="text-center py-10 bg-[#141416] rounded-[2px] border border-[rgba(255,255,255,0.06)] mb-6">
            <Camera class="w-8 h-8 text-[#a39e93] mx-auto mb-2 opacity-50" />
            <p class="font-playfair text-[17px] text-[#f2ebd9]">Nenhuma obra ou sessão registrada</p>
            <p class="font-montserrat text-[12px] text-[#a39e93] mt-1">Este cliente ainda não possui sessões vinculadas no banco.</p>
          </div>

          <!-- Bottom Action Buttons: Obras & Sessões Deck (3 Buttons) -->
          <div class="space-y-3 mb-6">
            <!-- Button 1: Gold Primary -->
            <button
              @click="openNovaSessaoModal"
              class="w-full h-12 bg-gradient-to-r from-[#c49f5e] via-[#d6b77e] to-[#ba9557] hover:brightness-105 active:scale-[0.99] text-[#09090b] font-inter text-[11.5px] font-bold tracking-[1.4px] uppercase rounded-[2px] flex items-center justify-center gap-2.5 shadow-[0_4px_24px_rgba(201,168,106,0.28)] transition-all cursor-pointer"
            >
              <FilePenLine class="w-4 h-4 text-[#09090b]" />
              <span>INICIAR NOVA SESSÃO / FICHA</span>
            </button>

            <!-- Button 2: WhatsApp Term Dispatch -->
            <button
              @click="openDisparoTermoModal"
              class="w-full h-12 bg-[#141416] hover:bg-[#1a1917] hover:border-[#c9a86a] border border-[rgba(201,168,106,0.35)] active:scale-[0.99] text-[#c9a86a] hover:text-[#e6c383] font-inter text-[11.5px] font-bold tracking-[1.4px] uppercase rounded-[2px] flex items-center justify-center gap-2.5 transition-all cursor-pointer"
            >
              <Smartphone class="w-4 h-4 text-[#c9a86a]" />
              <span>DISPARAR TERMO / WHATSAPP</span>
            </button>

            <!-- Button 3: Export Artistic Dossier PDF -->
            <button
              @click="exportPdf"
              class="w-full h-12 bg-[#141416] hover:bg-[#1a1917] hover:border-[rgba(201,168,106,0.5)] border border-[rgba(201,168,106,0.25)] active:scale-[0.99] text-[#b8afa0] hover:text-[#f2ebd9] font-inter text-[11.5px] font-bold tracking-[1.4px] uppercase rounded-[2px] flex items-center justify-center gap-2.5 transition-all cursor-pointer"
            >
              <FileDown class="w-4 h-4 text-[#b8afa0]" />
              <span>EXPORTAR DOSSIÊ ARTÍSTICO (PDF)</span>
            </button>
          </div>
        </div>

        <!-- TAB 2: PÓS-CUIDADO & CICATRIZAÇÃO -->
        <div v-else>
          <!-- Section Title Row -->
          <div class="flex items-center justify-between mb-3.5 px-0.5">
            <div class="flex items-center gap-2">
              <ShieldPlus class="w-5 h-5 text-[#c9a86a]" />
              <h2 class="font-playfair text-[20px] font-semibold text-[#f2ebd9] leading-tight">
                Pós-Cuidado & Cicatrização
              </h2>
            </div>

            <div class="bg-[#122017] border border-[rgba(74,222,128,0.25)] px-2.5 py-1 rounded-[2px] flex items-center shadow-sm">
              <span class="font-jetbrains text-[9.5px] font-bold text-[#86efac] tracking-[1.3px] uppercase">
                BOT ATIVO
              </span>
            </div>
          </div>

          <!-- Outer Notification Timeline Container -->
          <div class="border border-[rgba(201,168,106,0.18)] rounded-[4px] p-3.5 bg-[#0e0e11] space-y-3.5 shadow-xl mb-6">
            <!-- Card 1: Disparo Automatizado WhatsApp -->
            <div class="bg-[#141416] border border-[rgba(255,255,255,0.06)] rounded-[2px] p-3.5 transition-all hover:border-[rgba(201,168,106,0.25)]">
              <div class="flex items-start gap-3">
                <div class="w-9 h-9 bg-[#1a1917] border border-[rgba(201,168,106,0.25)] rounded-[2px] flex items-center justify-center shrink-0 mt-0.5">
                  <Bot class="w-4.5 h-4.5 text-[#c9a86a]" />
                </div>

                <div class="flex-1 min-w-0">
                  <div class="flex items-center justify-between mb-1.5">
                    <span class="font-inter text-[11px] font-bold tracking-[0.88px] text-[#f2ebd9] uppercase truncate">
                      DISPARO AUTOMATIZADO WHATSAPP
                    </span>
                    <span class="font-jetbrains text-[10px] text-[#7a766f] shrink-0 ml-2">
                      Hoje, 09:30
                    </span>
                  </div>

                  <p class="font-inter italic text-[12.5px] text-[#a39e93] leading-relaxed">
                    "Lembrete D+15: Solicitação de envio de foto em luz natural para avaliação de regeneração epitelial da sessão."
                  </p>
                </div>
              </div>
            </div>

            <!-- Card 2: Check-in D+15 Recebido -->
            <div class="bg-[#141416] border border-[rgba(201,168,106,0.18)] rounded-[2px] p-3.5 transition-all hover:border-[rgba(201,168,106,0.35)]">
              <div class="flex items-center justify-between mb-2">
                <div class="flex items-center gap-2">
                  <FileImage class="w-4 h-4 text-[#c9a86a]" />
                  <span class="font-inter text-[11px] font-bold tracking-[0.88px] text-[#f2ebd9] uppercase">
                    CHECK-IN D+15 RECEBIDO
                  </span>
                </div>

                <div class="bg-[#0e2116] border border-[rgba(74,222,128,0.25)] px-2 py-0.5 rounded-[2px]">
                  <span class="font-jetbrains text-[9.5px] font-bold text-[#86efac] tracking-[0.5px] uppercase">
                    AVALIAÇÃO: 10/10
                  </span>
                </div>
              </div>

              <p class="font-inter text-[12.5px] text-[#a39e93] leading-relaxed mb-3">
                Foto enviada pela cliente demonstra cicatrização impecável, ausência de vermelhidão ou descamação excessiva. Pomada mantida.
              </p>

              <div class="pt-2.5 border-t border-[rgba(255,255,255,0.06)] flex items-center justify-between">
                <div class="font-jetbrains text-[11px]">
                  <span class="text-[#7a766f]">Aprovado: </span>
                  <span class="text-[#f2ebd9] font-medium">Gabriel D.</span>
                </div>

                <button
                  @click="showPhotoModal = true"
                  class="flex items-center gap-1 font-inter text-[10.5px] font-bold text-[#c9a86a] hover:text-[#e6c383] tracking-[0.84px] uppercase transition-colors cursor-pointer group"
                >
                  <span>VER FOTO ENVIADA</span>
                  <ArrowUpRight class="w-3.5 h-3.5 group-hover:translate-x-0.5 group-hover:-translate-y-0.5 transition-transform" />
                </button>
              </div>
            </div>
          </div>

          <!-- Action Buttons: Pós-Cuidado Deck (2 Buttons) -->
          <div class="space-y-3 mb-6">
            <button
              @click="showPosModal = true"
              class="w-full h-12 bg-gradient-to-r from-[#c49f5e] via-[#d6b77e] to-[#ba9557] hover:brightness-105 active:scale-[0.99] text-[#09090b] font-inter text-[11.5px] font-bold tracking-[1.4px] uppercase rounded-[2px] flex items-center justify-center gap-2.5 shadow-[0_4px_20px_rgba(201,168,106,0.25)] transition-all cursor-pointer"
            >
              <FilePenLine class="w-4 h-4 text-[#09090b]" />
              <span>ALTERAR MENSAGENS PÓS-CUIDADO</span>
            </button>

            <button
              @click="showPreModal = true"
              class="w-full h-12 bg-[#141416] hover:bg-[#1a1917] hover:border-[#c9a86a] border border-[rgba(201,168,106,0.35)] active:scale-[0.99] text-[#c9a86a] hover:text-[#e6c383] font-inter text-[11.5px] font-bold tracking-[1.4px] uppercase rounded-[2px] flex items-center justify-center gap-2.5 transition-all cursor-pointer"
            >
              <SquareArrowRight class="w-4 h-4 text-[#c9a86a]" />
              <span>ALTERAR MENSAGEM PRÉ-CUIDADO</span>
            </button>
          </div>
        </div>

        <!-- MODAL 1: VISUALIZAR FOTO ENVIADA (D+15 Inspection) -->
        <div
          v-if="showPhotoModal"
          class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md transition-opacity"
          @click.self="showPhotoModal = false"
        >
          <div class="bg-[#141416] border border-[rgba(201,168,106,0.3)] rounded-[4px] max-w-sm w-full p-4 shadow-2xl relative overflow-hidden">
            <div class="flex items-center justify-between pb-3 border-b border-[rgba(255,255,255,0.06)] mb-3">
              <div class="flex items-center gap-2">
                <FileImage class="w-4 h-4 text-[#c9a86a]" />
                <h3 class="font-playfair text-[17px] font-semibold text-[#f2ebd9]">
                  Check-in Fotográfico D+15
                </h3>
              </div>
              <button
                @click="showPhotoModal = false"
                class="text-[#a39e93] hover:text-[#f2ebd9] p-1 cursor-pointer"
              >
                <X class="w-4 h-4" />
              </button>
            </div>

            <div class="relative w-full aspect-square rounded-[2px] overflow-hidden border border-[rgba(201,168,106,0.2)] bg-[#09090b] mb-3 group">
              <img
                src="/assets/tattoo_healing_check.jpg"
                alt="Foto de Cicatrização D+15"
                class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
              />
              <div class="absolute top-2 left-2 bg-[rgba(9,9,11,0.85)] border border-[rgba(201,168,106,0.3)] px-2 py-0.5 rounded-[2px] flex items-center gap-1.5">
                <span class="w-1.5 h-1.5 rounded-full bg-[#86efac]"></span>
                <span class="font-jetbrains text-[9px] font-bold text-[#f2ebd9]">LUZ NATURAL · 09:30</span>
              </div>
            </div>

            <div class="bg-[#0e0e11] border border-[rgba(255,255,255,0.05)] p-2.5 rounded-[2px] mb-3 text-[12px] space-y-1">
              <div class="flex justify-between items-center text-[10px] font-jetbrains text-[#a39e93]">
                <span>CLIENTE: {{ client?.name?.toUpperCase() || 'CLIENTE' }}</span>
                <span class="text-[#86efac] font-bold">AVALIAÇÃO: 10/10</span>
              </div>
              <p class="text-[#f2ebd9] font-inter text-[12px] leading-relaxed">
                Regeneração epitelial completa sem falhas de pigmentação. Ausência total de eritema ou crostas espessas.
              </p>
            </div>

            <button
              @click="showPhotoModal = false"
              class="w-full py-2.5 bg-[#c9a86a] text-[#09090b] font-inter text-[11px] font-bold tracking-[1.2px] uppercase rounded-[2px] cursor-pointer hover:bg-[#d6b77e] transition-colors"
            >
              CONCLUIR VISUALIZAÇÃO
            </button>
          </div>
        </div>

        <!-- MODAL 2: GALERIA RAW -->
        <div
          v-if="showGalleryModal"
          class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/85 backdrop-blur-md transition-opacity"
          @click.self="showGalleryModal = false"
        >
          <div class="bg-[#141416] border border-[rgba(201,168,106,0.3)] rounded-[4px] max-w-md w-full p-4 shadow-2xl relative">
            <div class="flex items-center justify-between pb-3 border-b border-[rgba(255,255,255,0.06)] mb-3">
              <div class="flex items-center gap-2">
                <Camera class="w-4 h-4 text-[#c9a86a]" />
                <h3 class="font-playfair text-[17px] font-semibold text-[#f2ebd9]">
                  Acervo RAW 4K · {{ activeModalArtwork?.title || client?.name || 'Obra' }}
                </h3>
              </div>
              <button
                @click="showGalleryModal = false"
                class="text-[#a39e93] hover:text-[#f2ebd9] p-1 cursor-pointer"
              >
                <X class="w-4 h-4" />
              </button>
            </div>

            <div class="rounded-[2px] overflow-hidden border border-[rgba(201,168,106,0.2)] bg-[#09090b] mb-3">
              <img
                :src="activeModalArtwork?.image || '/assets/sao_jeronimo_tattoo.jpg'"
                :alt="activeModalArtwork?.title || 'Obra RAW'"
                class="w-full h-64 object-cover"
              />
            </div>

            <div class="bg-[#09090b] p-3 rounded-[2px] border border-[rgba(255,255,255,0.05)] text-[12px] space-y-1 mb-3">
              <div class="flex justify-between text-[10px] font-jetbrains text-[#a39e93]">
                <span>REGISTRO DE ARQUIVO</span>
                <span class="text-[#c9a86a]">{{ activeModalArtwork?.badgeStatus || 'SESSÃO CONCLUÍDA' }}</span>
              </div>
              <p class="text-[#b8afa0] font-inter text-[11.5px]">
                {{ activeModalArtwork?.conceptDescription || 'Registro de alta precisão técnica arquivado no acervo.' }}
              </p>
            </div>

            <button
              @click="showGalleryModal = false"
              class="w-full py-2.5 bg-[#c9a86a] text-[#09090b] font-inter text-[11px] font-bold tracking-[1.2px] uppercase rounded-[2px] cursor-pointer hover:bg-[#d6b77e]"
            >
              FECHAR GALERIA
            </button>
          </div>
        </div>

        <!-- MODAL 3: TERMO ASSINADO -->
        <div
          v-if="showTermoModal"
          class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md transition-opacity"
          @click.self="showTermoModal = false"
        >
          <div class="bg-[#141416] border border-[rgba(201,168,106,0.3)] rounded-[4px] max-w-sm w-full p-4 shadow-2xl relative">
            <div class="flex items-center justify-between pb-3 border-b border-[rgba(255,255,255,0.06)] mb-3">
              <div class="flex items-center gap-2">
                <CheckCircle2 class="w-4 h-4 text-[#86efac]" />
                <h3 class="font-playfair text-[17px] font-semibold text-[#f2ebd9]">
                  Termo de Consentimento & Ficha
                </h3>
              </div>
              <button
                @click="showTermoModal = false"
                class="text-[#a39e93] hover:text-[#f2ebd9] p-1 cursor-pointer"
              >
                <X class="w-4 h-4" />
              </button>
            </div>

            <div class="bg-[#09090b] p-3 rounded-[2px] border border-[rgba(255,255,255,0.05)] text-[12px] space-y-2 mb-3">
              <div class="text-[#c9a86a] font-jetbrains text-[10px] uppercase font-bold">
                DOCUMENTO ELETRÔNICO #TC-{{ client?.dossierNumber?.replace('#', '') || '0001' }}
              </div>
              <p class="text-[#f2ebd9] font-inter text-[12px] leading-relaxed">
                Termo assinado biometricamente por {{ client?.name || 'Cliente' }}.
              </p>
              <div class="text-[#a39e93] font-jetbrains text-[10px]">
                CONFORMIDADE LGPD · REGISTRO NOTARIAL IMUTÁVEL
              </div>
            </div>

            <div class="flex gap-2">
              <button
                @click="showTermoModal = false"
                class="flex-1 py-2.5 bg-[#141416] border border-[rgba(255,255,255,0.1)] text-[#a39e93] font-inter text-[10.5px] font-bold uppercase rounded-[2px] cursor-pointer hover:text-[#f2ebd9]"
              >
                FECHAR
              </button>
              <button
                @click="router.push('/termo')"
                class="flex-1 py-2.5 bg-[#c9a86a] text-[#09090b] font-inter text-[10.5px] font-bold tracking-[1.2px] uppercase rounded-[2px] cursor-pointer hover:bg-[#d6b77e]"
              >
                ABRIR FICHA
              </button>
            </div>
          </div>
        </div>

        <!-- MODAL 4: ALTERAR MENSAGENS PÓS-CUIDADO -->
        <div
          v-if="showPosModal"
          class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md transition-opacity"
          @click.self="showPosModal = false"
        >
          <div class="bg-[#141416] border border-[rgba(201,168,106,0.3)] rounded-[4px] max-w-md w-full p-4 shadow-2xl relative">
            <div class="flex items-center justify-between pb-3 border-b border-[rgba(255,255,255,0.06)] mb-3">
              <div class="flex items-center gap-2">
                <Bot class="w-4 h-4 text-[#c9a86a]" />
                <h3 class="font-playfair text-[17px] font-semibold text-[#f2ebd9]">
                  Cadência Automatizada Pós-Cuidado
                </h3>
              </div>
              <button
                @click="showPosModal = false"
                class="text-[#a39e93] hover:text-[#f2ebd9] p-1 cursor-pointer"
              >
                <X class="w-4 h-4" />
              </button>
            </div>

            <div class="space-y-3 mb-4">
              <div class="flex items-center gap-2 overflow-x-auto pb-1 text-[10px] font-jetbrains">
                <button
                  v-for="cadence in ['D+1 (Lavagem)', 'D+7 (Hidratação)', 'D+15 (Check-in)', 'D+30 (Retoque)']"
                  :key="cadence"
                  :class="[
                    'px-2.5 py-1 rounded-[2px] whitespace-nowrap cursor-pointer transition-colors',
                    cadence.includes('D+15')
                      ? 'bg-[#c9a86a] text-[#09090b] font-bold'
                      : 'bg-[#09090b] border border-[rgba(255,255,255,0.08)] text-[#a39e93]'
                  ]"
                >
                  {{ cadence }}
                </button>
              </div>

              <div>
                <label class="block font-inter text-[10px] font-bold text-[#c9a86a] tracking-[1px] uppercase mb-1">
                  TEMPLATE WHATSAPP (D+15)
                </label>
                <textarea
                  v-model="posMessageText"
                  rows="4"
                  class="w-full bg-[#09090b] border border-[rgba(201,168,106,0.25)] rounded-[2px] p-2.5 font-inter text-[12px] text-[#f2ebd9] focus:outline-none focus:border-[#c9a86a]"
                ></textarea>
              </div>

              <div class="flex items-center justify-between text-[11px] text-[#a39e93]">
                <span>Horário de Envio: <strong class="text-[#f2ebd9]">09:30 BRT</strong></span>
                <span class="text-[#86efac]">Bot Ativo via WhatsApp API</span>
              </div>
            </div>

            <div class="flex gap-2">
              <button
                @click="showPosModal = false"
                class="flex-1 py-2.5 bg-[#1a1917] border border-[rgba(255,255,255,0.1)] text-[#a39e93] font-inter text-[10.5px] font-bold uppercase rounded-[2px] cursor-pointer hover:text-[#f2ebd9]"
              >
                CANCELAR
              </button>
              <button
                @click="savePosMessage"
                class="flex-1 py-2.5 bg-[#c9a86a] text-[#09090b] font-inter text-[10.5px] font-bold tracking-[1px] uppercase rounded-[2px] cursor-pointer hover:bg-[#d6b77e]"
              >
                SALVAR ALTERAÇÕES
              </button>
            </div>
          </div>
        </div>

        <!-- MODAL 5: ALTERAR MENSAGEM PRÉ-CUIDADO -->
        <div
          v-if="showPreModal"
          class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md transition-opacity"
          @click.self="showPreModal = false"
        >
          <div class="bg-[#141416] border border-[rgba(201,168,106,0.3)] rounded-[4px] max-w-md w-full p-4 shadow-2xl relative">
            <div class="flex items-center justify-between pb-3 border-b border-[rgba(255,255,255,0.06)] mb-3">
              <div class="flex items-center gap-2">
                <SquareArrowRight class="w-4 h-4 text-[#c9a86a]" />
                <h3 class="font-playfair text-[17px] font-semibold text-[#f2ebd9]">
                  Mensagem de Pré-Cuidado
                </h3>
              </div>
              <button
                @click="showPreModal = false"
                class="text-[#a39e93] hover:text-[#f2ebd9] p-1 cursor-pointer"
              >
                <X class="w-4 h-4" />
              </button>
            </div>

            <div class="space-y-3 mb-4">
              <div>
                <label class="block font-inter text-[10px] font-bold text-[#c9a86a] tracking-[1px] uppercase mb-1">
                  TEMPLATE ENVIADO 48H ANTES DA SESSÃO
                </label>
                <textarea
                  v-model="preMessageText"
                  rows="4"
                  class="w-full bg-[#09090b] border border-[rgba(201,168,106,0.25)] rounded-[2px] p-2.5 font-inter text-[12px] text-[#f2ebd9] focus:outline-none focus:border-[#c9a86a]"
                ></textarea>
              </div>

              <div class="text-[11px] text-[#a39e93]">
                Disparo automatizado programado para 2 dias de antecedência do agendamento confirmado na agenda.
              </div>
            </div>

            <div class="flex gap-2">
              <button
                @click="showPreModal = false"
                class="flex-1 py-2.5 bg-[#1a1917] border border-[rgba(255,255,255,0.1)] text-[#a39e93] font-inter text-[10.5px] font-bold uppercase rounded-[2px] cursor-pointer hover:text-[#f2ebd9]"
              >
                CANCELAR
              </button>
              <button
                @click="savePreMessage"
                class="flex-1 py-2.5 bg-[#c9a86a] text-[#09090b] font-inter text-[10.5px] font-bold tracking-[1px] uppercase rounded-[2px] cursor-pointer hover:bg-[#d6b77e]"
              >
                SALVAR PROTOCOLO
              </button>
            </div>
          </div>
        </div>

      </div>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { IonPage, IonContent } from '@ionic/vue';
import { useRouter, useRoute } from 'vue-router';
import {
  ArrowLeft,
  Phone,
  CreditCard,
  ShieldCheck,
  ShieldPlus,
  Bot,
  FileImage,
  ArrowUpRight,
  FilePenLine,
  SquareArrowRight,
  CheckCircle2,
  Camera,
  Smartphone,
  FileDown,
  X
} from '@lucide/vue';
import AppHeader from '../components/AppHeader.vue';
import { useClientsStore, type ClientArtwork } from '@/stores/clients';
import { useAlert } from '@/composables/useAlert';

const router = useRouter();
const route = useRoute();
const clientsStore = useClientsStore();
const alert = useAlert();

// Client ID dinâmico vindo da rota ou padrão
const clientId = computed(() => (route.query.id as string) || '1');

// State - Default to 'obras'
const activeTab = ref<'obras' | 'pos-cuidado'>('obras');
const showPhotoModal = ref(false);
const showGalleryModal = ref(false);
const showTermoModal = ref(false);
const showPosModal = ref(false);
const showPreModal = ref(false);
const activeModalArtwork = ref<ClientArtwork | null>(null);

const client = computed(() => clientsStore.activeDossier);

// Messages state
const posMessageText = ref('');
const preMessageText = ref('');

// Sincroniza dados do dossiê com o backend
onMounted(async () => {
  await clientsStore.fetchClientDossier(clientId.value);
  posMessageText.value = client.value?.posMessageText || '';
  preMessageText.value = client.value?.preMessageText || '';
});

watch(
  () => client.value,
  (newVal) => {
    if (newVal) {
      posMessageText.value = newVal.posMessageText || '';
      preMessageText.value = newVal.preMessageText || '';
    }
  }
);

function handleBack() {
  if (window.history.length > 1) {
    router.back();
  } else {
    router.push({ name: 'Clientes' });
  }
}

function handleNotifications() {
  console.log('Notificações abertas');
}

function handleProfile() {
  console.log('Perfil aberto');
}

function openGalleryModal(art?: ClientArtwork) {
  activeModalArtwork.value = art || client.value?.artworks?.[0] || null;
  showGalleryModal.value = true;
}

function openTermoModal(art?: ClientArtwork) {
  activeModalArtwork.value = art || client.value?.artworks?.[0] || null;
  showTermoModal.value = true;
}

function openNovaSessaoModal() {
  router.push({ name: 'UploadArte', query: { id: clientId.value } });
}

async function openDisparoTermoModal() {
  const result = await clientsStore.dispatchTermoWhatsapp(clientId.value);
  await alert.success(result.message, 'Termo Notarial');
}

async function exportPdf() {
  const c = client.value;
  await alert.info(`Exportando Dossiê Artístico ${c?.dossierNumber || ''} de ${c?.name || 'Cliente'} em PDF notarial de alta resolução...`, 'Exportação de Dossiê');
}

async function savePosMessage() {
  await clientsStore.updateDossierMessage(clientId.value, 'pos', posMessageText.value);
  showPosModal.value = false;
}

async function savePreMessage() {
  await clientsStore.updateDossierMessage(clientId.value, 'pre', preMessageText.value);
  showPreModal.value = false;
}
</script>
