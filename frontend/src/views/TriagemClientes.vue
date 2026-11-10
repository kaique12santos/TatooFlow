<template>
  <ion-page>
    <ion-content class="bg-[#09090b] text-[#f2ebd9] font-sans" fullscreen>
      <div class="screen-container min-h-screen pb-24 pt-3">
        <!-- Reusable App Header -->
        <AppHeader
          @click-profile="router.push('/perfil')"
        />

        

        <!-- Page Title & Subtitle -->
        <div class="mb-4 px-0.5">
          <h1 class="font-playfair text-[25px] sm:text-[28px] font-semibold text-[#f2ebd9] leading-tight mb-1">
            Triagem de Clientes
          </h1>
         
        </div>

        <!-- Search Bar Input -->
        <div class="flex gap-3 mb-3">
          <router-link to="/agenda" class="flex-1 text-center rounded border border-[#c9a86a]/40 py-3 text-xs font-bold text-[#e6c383]">Ver agenda</router-link>
          <router-link to="/agendamento-sessao" class="flex-1 text-center rounded bg-[#c9a86a] py-3 text-xs font-bold text-[#09090b]">Novo agendamento</router-link>
        </div>

        <!-- Webhook WhatsApp Live Status & Test Button -->
        <!-- <div class="flex items-center justify-between bg-[#141416] border border-[#25D366]/25 rounded-[4px] px-3 py-1.5 mb-3 text-[10px] shadow-sm">
          <div class="flex items-center gap-1.5 text-[#25D366]">
            <span class="w-1.5 h-1.5 rounded-full bg-[#25D366] animate-pulse"></span>
            <span class="font-jetbrains font-bold tracking-wider uppercase text-[9px]">WEBHOOK WHATSAPP ATIVO</span>
          </div>
          <button
            @click="simularNovaMensagemWhatsApp"
            class="font-jetbrains text-[9.5px] font-bold text-[#e6c383] hover:text-[#d6b77e] underline decoration-[#c9a86a]/40 cursor-pointer flex items-center gap-1"
            title="Disparar simulação de webhook push para testar toast de notificação móvel"
          >
            <span>SIMULAR PUSH</span>
            <ArrowRight class="w-2.5 h-2.5" />
          </button>
        </div> -->

        <div class="bg-[#141416] border border-[rgba(255,255,255,0.08)] rounded-[4px] px-3.5 py-2.5 flex items-center gap-2.5 mb-3 shadow-sm">
          <Search class="w-4 h-4 text-[#8e8a82] shrink-0" />
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Buscar por cliente, estilo ou projeto..."
            class="font-inter text-[12px] text-[#f2ebd9] placeholder-[#5a5852] bg-transparent outline-none flex-1"
          />
          <button
            @click="carregar"
            :disabled="triageStore.isLoading"
            aria-label="Atualizar triagem"
            class="text-[#8e8a82] hover:text-[#f2ebd9] transition-colors cursor-pointer shrink-0"
            title="Atualizar triagem"
          >
            <RefreshCw class="w-4 h-4" />
          </button>
        </div>

        <StatusFilterTabs v-model="activeFilter" :tabs="filterPills" />
        <div v-if="triageStore.error" role="alert" class="rounded-[4px] border border-red-300/30 bg-[#141416] p-4 text-sm text-[#ef8b83] mb-4">
          {{ triageStore.error }}
          <button type="button" @click="carregar" class="block underline mt-2">Tentar novamente</button>
        </div>
        <output v-if="triageStore.isLoading" class="block py-8 text-sm text-[#a39e93]">Carregando triagem...</output>
        <div v-else-if="!triageStore.error && !triageStore.clients.length" class="rounded-[4px] border border-white/5 bg-[#141416] p-5 mb-4 text-center">
          <h2 class="font-playfair text-xl">Nenhum atendimento em triagem</h2>
          <p class="font-inter text-xs text-[#a39e93] mt-2">Cadastre um cliente ou crie um novo agendamento para iniciar o atendimento.</p>
          <FileUp class="w-6 h-6 text-[#c9a86a] mx-auto mt-4 mb-2" />
          <p class="font-jetbrains text-[10px] text-[#c9a86a] uppercase">Anexar comprovante do sinal</p>
          <router-link to="/agendamento-sessao" class="inline-block text-xs text-[#e6c383] underline mt-3">Criar agendamento</router-link>
        </div>

        <!-- Main Responsive Content Grid (Cards & Listas) -->
        <ion-grid v-if="!triageStore.isLoading && !triageStore.error" class="ion-no-padding">
          <ion-row class="triage-row">
            <!-- Atendimento selecionado -->
            <ion-col v-if="featuredClient" size="12" size-md="6" class="ion-no-padding flex flex-col md:pr-3">
              <div class="flex items-center justify-between mb-2 px-0.5">
                <span class="font-jetbrains text-[9px] font-bold tracking-[1.5px] text-[#c9a86a] uppercase">
                  ATENDIMENTO EM DESTAQUE
                </span>
                <span class="font-jetbrains text-[8.5px] text-[#8e8a82]">
                  {{ featuredClient.statusLabel }}
                </span>
              </div>

              <div ref="atendimentoRef" class="bg-[#141416] border border-[rgba(255,255,255,0.07)] rounded-[4px] p-4 shadow-lg flex flex-1 flex-col justify-between">
                <div>
                  <!-- Cliente selecionado -->
                  <div class="flex items-start justify-between mb-3">
                    <div class="flex items-center gap-3">
                      <div class="w-11 h-11 rounded-[2px] overflow-hidden border border-[rgba(255,255,255,0.1)] shrink-0 bg-[#09090b]">
                        <span class="w-full h-full flex items-center justify-center font-playfair text-xl text-[#c9a86a]">{{ featuredClient.name.slice(0, 1).toUpperCase() }}</span>
                      </div>
                      <div>
                        <div class="flex items-center gap-1.5">
                          <h2 class="font-playfair text-[16px] font-semibold text-[#f2ebd9] leading-tight">
                            {{ featuredClient.name }}
                          </h2>
                          <CheckCircle2 class="w-3.5 h-3.5 text-[#8e8a82]" />
                        </div>
                        <div class="flex items-center gap-1 text-[#8e8a82] font-jetbrains text-[9.5px] mt-0.5">
                          <Clock class="w-3 h-3 text-[#8e8a82]" />
                          <span>{{ featuredClient.timeAgo }}</span>
                        </div>
                      </div>
                    </div>
                  </div>

                  <!-- Project Details -->
                  <div class="bg-[#09090b] border border-[rgba(255,255,255,0.05)] rounded-[3px] p-2.5 mb-2.5">
                    <div class="flex items-center justify-between text-[10px] font-jetbrains text-[#a39e93]">
                      <span>PROJETO: {{ featuredClient.style || featuredClient.projectTitle || 'Não informado' }}</span>
                      <span class="text-[#c9a86a]">{{ featuredClient.lotId }}</span>
                    </div>
                    <div class="font-inter text-[12px] text-[#f2ebd9] mt-0.5 font-medium">
                      {{ featuredClient.conceptDescription }}
                    </div>
                  </div>

                  <!-- Deposit & Guarantee Row -->
                  <div class="flex items-end justify-between py-2 border-t border-[rgba(255,255,255,0.05)] mt-1 mb-2">
                    <div>
                      <div class="flex items-center gap-1 text-[#c9a86a] mb-1">
                        <CreditCard class="w-4 h-4 text-[#c9a86a]" />
                      </div>
                      <div class="flex items-center gap-1 text-[#8e8a82] font-inter text-[10px]">
                        <ShieldCheck class="w-3.5 h-3.5 text-[#c9a86a]" />
                        <span>Garantia de bancada • 50% obrigatório</span>
                      </div>
                    </div>

                    <!-- Price Tag Display -->
                    <div class="text-right">
                      <div class="flex items-baseline justify-end gap-1">
                        <span v-if="featuredClient.depositAmount != null" class="font-jetbrains text-[13px] font-bold text-[#d6b77e] leading-none">R$</span>
                        <span class="font-playfair text-[24px] font-bold text-[#f2ebd9] leading-none">
                          {{ valor(featuredClient.depositAmount) }}
                        </span>
                      </div>
                      <div class="font-jetbrains text-[8px] text-[#8e8a82] uppercase tracking-wider mt-0.5">
                        {{ featuredClient.priceTotal == null ? 'VALOR ESTIMADO NÃO INFORMADO' : 'REF: 50% DE ' + moeda(featuredClient.priceTotal) }}
                      </div>
                    </div>
                  </div>

                  <!-- Attach Receipt Box (Dotted Border) -->
                  <div v-if="featuredClient.status !== 'confirmados'" class="bg-[#09090b] border border-dashed border-[rgba(201,168,106,0.3)] rounded-[4px] p-4 text-center my-3 relative">
                    <FileUp class="w-6 h-6 text-[#c9a86a] mx-auto mb-1.5" />
                    <div class="font-jetbrains text-[10px] font-bold text-[#f2ebd9] tracking-[1px] uppercase">
                      ANEXAR COMPROVANTE (JPG/PNG/PDF)
                    </div>
                    <p class="font-inter text-[10.5px] text-[#8e8a82] mt-0.5 mb-3">
                      Comprovante de transferência bancária ou PIX · até 5 MB
                    </p>

                    <!-- File Input Row -->
                    <div class="bg-[#141416] border border-[rgba(255,255,255,0.06)] rounded-[2px] p-1.5 px-3 flex items-center justify-between">
                      <div class="flex items-center gap-2 truncate pr-2">
                        <FileText class="w-3.5 h-3.5 text-[#8e8a82] shrink-0" />
                        <span class="font-inter text-[10.5px] text-[#8e8a82] italic truncate">
                          {{ attachedFile?.name || 'Nenhum arquivo selecionado' }}
                        </span>
                      </div>
                      <button
                        @click="fileInputRef?.click()"
                        :disabled="isSubmittingDeposit || !featuredClient.agendamentoId"
                        class="bg-[#1c1a17] hover:bg-[#25221d] border border-[rgba(201,168,106,0.3)] px-3 py-1 rounded-[2px] font-jetbrains text-[9.5px] font-bold text-[#c9a86a] tracking-[1px] uppercase cursor-pointer shrink-0 transition-colors"
                      >
                        PROCURAR
                      </button>
                    </div>
                    <input
                      :key="featuredClient.id"
                      ref="fileInputRef"
                      :disabled="isSubmittingDeposit || !featuredClient.agendamentoId"
                      aria-label="Anexar comprovante do sinal"
                      type="file"
                      accept=".jpg,.jpeg,.png,.pdf"
                      class="hidden"
                      @change="selecionarComprovante"
                    />
                  </div>
                </div>

                <p v-if="depositError" role="alert" class="font-inter text-xs text-[#ef8b83] mt-2">{{ depositError }}</p>
                <button v-if="featuredClient.status === 'sinal'" type="button" @click="registrarSinal"
                  :disabled="isSubmittingDeposit || !attachedFile || featuredClient.depositAmount == null"
                  class="w-full bg-[#c9a86a] hover:bg-[#d6b77e] text-[#09090b] font-inter text-[11px] font-bold tracking-[1.4px] uppercase py-3.5 px-4 rounded-[2px] shadow-xl flex items-center justify-center gap-2 mt-2 disabled:opacity-40">
                  <Lock class="w-3.5 h-3.5" />
                  <span>{{ isSubmittingDeposit ? 'REGISTRANDO NO SERVIDOR...' : 'REGISTRAR SINAL E LIBERAR MACA' }}</span>
                </button>
                <button v-else-if="featuredClient.status === 'confirmados'" type="button" @click="verSinal(featuredClient)"
                  class="w-full bg-[#c9a86a] hover:bg-[#d6b77e] text-[#09090b] font-inter text-[11px] font-bold tracking-[1.4px] uppercase py-3.5 px-4 rounded-[2px] flex items-center justify-center gap-2 mt-2">
                  <FileText class="w-3.5 h-3.5" /> VER SINAL
                </button>
                <template v-else>
                  <p class="font-inter text-xs text-[#a39e93] mt-2">Crie o agendamento para vincular o comprovante à sessão.</p>
                  <button type="button" @click="iniciarAgendamento(featuredClient)" class="w-full bg-[#c9a86a] text-[#09090b] font-inter text-[11px] font-bold uppercase py-3.5 px-4 rounded-[2px] mt-2">AGENDAR SESSÃO</button>
                </template>
              </div>
            </ion-col>

            <!-- COLUNA 2: FILA GERAL DE ATENDIMENTO -->
            <ion-col size="12" :size-md="featuredClient ? '6' : '12'" class="ion-no-padding md:pl-3 flex flex-col justify-between">
              <div>
                <!-- Section Header Row -->
                <div class="flex flex-wrap items-center justify-between gap-x-3 gap-y-2 mb-2.5 px-0.5">
                  <span class="min-w-0 font-jetbrains text-[9px] font-bold tracking-[1.5px] text-[#8e8a82] uppercase">
                    FILA GERAL DE ATENDIMENTO
                  </span>
                  <span class="shrink-0 font-jetbrains text-[9px] text-[#8e8a82] flex items-center gap-1">
                    <span class="text-[#4ade80] text-[10px] leading-none">●</span>
                    {{ filteredQueueClients.length }} na fila
                  </span>
                </div>

                <!-- Lista Dinâmica de Clientes da Fila -->
                <div v-if="filteredQueueClients.length > 0" class="space-y-3">
                  <div
                    v-for="item in filteredQueueClients"
                    :key="item.id"
                    class="bg-[#141416] border border-[rgba(255,255,255,0.07)] hover:border-[rgba(201,168,106,0.3)] transition-all rounded-[4px] p-3.5 shadow-md"
                  >
                    <div class="flex items-start justify-between">
                      <div class="flex items-center gap-2.5">
                        <div class="w-10 h-10 rounded-[2px] overflow-hidden border border-[rgba(255,255,255,0.08)] shrink-0 bg-[#09090b]">
                          <span class="w-full h-full flex items-center justify-center font-playfair text-lg text-[#c9a86a]">{{ item.name.slice(0, 1).toUpperCase() }}</span>
                        </div>
                        <div>
                          <h3 class="font-playfair text-[15px] font-semibold text-[#f2ebd9] leading-tight">
                            {{ item.name }}
                          </h3>
                          <div class="font-inter text-[11px] text-[#8e8a82]">
                            {{ item.style }}
                          </div>
                          <div class="font-inter text-[10.5px] text-[#8e8a82] italic truncate max-w-[190px]">
                            ({{ item.conceptDescription }})
                          </div>
                        </div>
                      </div>

                      <!-- Badge Dinâmica por Status -->
                      <div>
                        <div
                          v-if="item.status === 'confirmados'"
                          class="bg-[#0e2417] border border-[#225533] px-2.5 py-0.5 rounded-[2px] flex items-center gap-1 shrink-0"
                        >
                          <CheckCircle2 class="w-3 h-3 text-[#4ade80]" />
                          <span class="font-jetbrains text-[9px] font-bold text-[#4ade80] tracking-[1px] uppercase">
                            CONFIRMADO
                          </span>
                        </div>

                        <div
                          v-else-if="item.status === 'aguardando'"
                          class="bg-[#1a1917] border border-[rgba(255,255,255,0.08)] px-2.5 py-0.5 rounded-[2px] shrink-0"
                        >
                          <span class="font-jetbrains text-[9px] font-bold text-[#e6c383] tracking-[1px] uppercase">
                            AGUARDANDO
                          </span>
                        </div>

                        <div
                          v-else
                          class="bg-[#241e17] border border-[rgba(201,168,106,0.25)] px-2.5 py-0.5 rounded-[2px] shrink-0"
                        >
                          <span class="font-jetbrains text-[9px] font-bold text-[#d6b77e] tracking-[1px] uppercase">
                            SINAL PENDENTE
                          </span>
                        </div>
                      </div>
                    </div>

                    <!-- Footer: Informações e Botão de Ação -->
                    <div class="border-t border-[rgba(255,255,255,0.05)] pt-2.5 mt-2.5 flex items-center justify-between">
                      <div class="flex items-center gap-1 text-[11px] text-[#8e8a82] font-inter">
                        <template v-if="item.status === 'confirmados'">
                          <CheckCircle2 class="w-3.5 h-3.5 text-[#4ade80]" />
                          <span>Sinal Recebido: <strong class="text-[#f2ebd9] font-semibold">{{ moeda(item.depositAmount) }}</strong></span>
                        </template>
                        <template v-else-if="item.status === 'aguardando'">
                          <FileText class="w-3.5 h-3.5 text-[#8e8a82]" />
                          <span>{{ item.priceTotal == null ? 'Aguardando orçamento' : 'Orçamento: ' + moeda(item.priceTotal) }}</span>
                        </template>
                        <template v-else>
                          <Clock class="w-3.5 h-3.5 text-[#c9a86a]" />
                          <span>Sinal pendente ({{ moeda(item.depositAmount) }})</span>
                        </template>
                      </div>

                      <div class="flex flex-wrap justify-end gap-2">
                        <button v-if="item.status === 'confirmados'" type="button" @click="verSinal(item)" :aria-label="'Ver sinal de ' + item.name"
                          class="font-jetbrains text-[9.5px] font-bold text-[#c9a86a] tracking-[1px] uppercase flex items-center gap-1">
                          <FileText class="w-3.5 h-3.5" /> VER SINAL
                        </button>
                        <button v-else-if="item.agendamentoId" type="button" @click="selecionarCliente(item)" :disabled="isSubmittingDeposit"
                          class="font-jetbrains text-[9.5px] font-bold text-[#c9a86a] tracking-[1px] uppercase">ANEXAR COMPROVANTE →</button>
                        <button v-else type="button" @click="iniciarAgendamento(item)"
                          class="font-jetbrains text-[9.5px] font-bold text-[#c9a86a] tracking-[1px] uppercase">AGENDAR SESSÃO →</button>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- Empty State se nenhum cliente no filtro -->
                <div
                  v-else
                  class="bg-[#141416] border border-[rgba(255,255,255,0.05)] rounded-[4px] p-8 text-center my-3"
                >
                  <p class="font-playfair text-[15px] text-[#f2ebd9]">{{ featuredClient ? 'Nenhum outro atendimento na fila' : 'Nenhum atendimento neste filtro' }}</p>
                  <p class="font-inter text-[11px] text-[#8e8a82] mt-1">{{ featuredClient ? 'O atendimento deste filtro está em destaque acima.' : 'Selecione outra categoria ou limpe a busca.' }}</p>
                </div>
              </div>

              <!-- ACTION BUTTON: + NOVO CLIENTE -->
              <button
                @click="showClienteModal = true"
                class="w-full bg-[#09090b] hover:bg-[#141416] border border-[rgba(201,168,106,0.35)] hover:border-[#c9a86a] py-3.5 px-4 rounded-[2px] flex items-center justify-center gap-2 my-4 transition-all cursor-pointer active:scale-[0.99]"
              >
                <PlusCircle class="w-4 h-4 text-[#c9a86a]" />
                <span class="font-jetbrains text-[10px] font-bold text-[#e6c383] tracking-[1.5px] uppercase">
                  + NOVO CLIENTE
                </span>
              </button>
            </ion-col>
          </ion-row>
        </ion-grid>
      </div>

      <CompactModal :is-open="depositoRegistrado !== null" labelled-by="titulo-confirmacao" @close="depositoRegistrado = null">
        <template v-if="depositoRegistrado">
          <div class="flex items-center justify-between pb-3 border-b border-white/5 mb-3">
            <div class="flex items-center gap-2"><CheckCircle2 class="w-5 h-5 text-[#86efac]" /><h2 id="titulo-confirmacao" class="font-playfair text-[18px] font-semibold">Sinal Confirmado</h2></div>
            <button type="button" @click="depositoRegistrado = null" aria-label="Fechar confirmação do sinal" class="text-[#a39e93] p-1"><X class="w-4 h-4" /></button>
          </div>
          <div class="bg-[#09090b] p-3 rounded-[2px] border border-white/5 space-y-2 mb-4">
            <div class="text-[#c9a86a] font-jetbrains text-[10.5px] uppercase font-bold tracking-wider">SINAL {{ moeda(depositoRegistrado.depositAmount) }} · CONFIRMADO</div>
            <p class="font-inter text-[12px] leading-relaxed">O pagamento do sinal de {{ depositoRegistrado.name }} foi registrado e o agendamento foi confirmado.</p>
            <div class="text-[#a39e93] font-jetbrains text-[10px] space-y-1 pt-2 border-t border-white/5">
              <div class="break-words">COMPROVANTE: {{ depositoRegistrado.depositReceipt }}</div>
              <div>STATUS: SINAL 50% LIQUIDADO</div>
            </div>
          </div>
          <button type="button" @click="irParaTermo" class="w-full py-2.5 bg-[#c9a86a] text-[#09090b] font-inter text-[10.5px] font-bold tracking-[1px] uppercase rounded-[2px] hover:bg-[#d6b77e]">GERAR TERMO</button>
          <button type="button" @click="depositoRegistrado = null" class="w-full py-2 mt-2 text-[#8e8a82] font-inter text-[10px] uppercase font-semibold">FECHAR</button>
        </template>
      </CompactModal>
      <CompactModal :is-open="showClienteModal" labelled-by="titulo-cliente" @close="showClienteModal = false">
        <form @submit.prevent="cadastrarCliente" class="space-y-4">
          <div class="flex items-center justify-between pb-3 border-b border-white/5">
            <h2 id="titulo-cliente" class="font-playfair text-[18px] font-semibold">Novo Cliente</h2>
            <button type="button" @click="showClienteModal = false" aria-label="Fechar cadastro de cliente" class="text-[#a39e93] p-1"><X class="w-4 h-4" /></button>
          </div>
          <label class="block font-inter text-xs text-[#a39e93]">Nome<input v-model="nomeCliente" required maxlength="255" autocomplete="name" class="campo mt-2" /></label>
          <label class="block font-inter text-xs text-[#a39e93]">WhatsApp com país e DDD<input v-model="telefoneCliente" required type="tel" autocomplete="tel" placeholder="5511999999999" class="campo mt-2" /></label>
          <p v-if="clienteError" role="alert" class="font-inter text-xs text-[#ef8b83]">{{ clienteError }}</p>
          <button type="submit" :disabled="salvandoCliente" class="w-full bg-[#c9a86a] text-[#09090b] py-2.5 rounded-[2px] font-inter text-[11px] font-bold uppercase disabled:opacity-40">{{ salvandoCliente ? 'Salvando...' : 'Salvar cliente' }}</button>
          <button type="button" @click="showClienteModal = false" class="w-full py-2 text-[#8e8a82] font-inter text-[10px] uppercase font-semibold">FECHAR</button>
        </form>
      </CompactModal>
      <SinalDialog :sinal="sinalSelecionado" :loading="carregandoSinal" :error="erroSinal" @close="fecharSinal" @retry="carregarComprovante" />
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { computed, nextTick, onUnmounted, ref, watch } from 'vue';
import { IonPage, IonContent, IonGrid, IonRow, IonCol, onIonViewWillEnter, onIonViewWillLeave } from '@ionic/vue';
import { CheckCircle2, Clock, CreditCard, FileText, FileUp, Lock, PlusCircle, Search, ShieldCheck, RefreshCw, X, ArrowRight } from '@lucide/vue';
import { useRoute, useRouter } from 'vue-router';
import AppHeader from '@/components/AppHeader.vue';
import StatusFilterTabs from '@/components/StatusFilterTabs.vue';
import CompactModal from '@/components/CompactModal.vue';
import SinalDialog, { type SinalDetails } from '@/components/SinalDialog.vue';
import { buscarComprovanteSinal, criarClienteAgenda } from '@/api/agendamentos';
import { useTriageStore, type TriageClient } from '@/stores/triage';
import { useAlert } from '@/composables/useAlert';

const router = useRouter();
const route = useRoute();
const triageStore = useTriageStore();
const alert = useAlert();
const searchQuery = ref('');
const activeFilter = ref('todos');
const attachedFile = ref<File | null>(null);
const fileInputRef = ref<HTMLInputElement | null>(null);
const atendimentoRef = ref<HTMLElement | null>(null);
const isSubmittingDeposit = ref(false);
const depositError = ref('');
const depositoRegistrado = ref<TriageClient | null>(null);
const showClienteModal = ref(false);
const nomeCliente = ref('');
const telefoneCliente = ref('');
const clienteError = ref('');
const salvandoCliente = ref(false);
const sinalSelecionado = ref<SinalDetails | null>(null);
const sinalAgendamentoId = ref<number | null>(null);
const carregandoSinal = ref(false);
const erroSinal = ref('');

// Controle de Polling & Push Notifications
let pollInterval: ReturnType<typeof setInterval> | null = null;
const previousClientIds = ref<Set<string>>(new Set());

const filterPills = computed(() => [
  { id: 'todos', label: 'TODOS' }, { id: 'agendados', label: 'AGENDADOS' },
  { id: 'aguardando', label: 'AGUARDANDO ORÇAMENTO' },
  { id: 'sinal', label: 'SINAL PENDENTE' }, { id: 'confirmados', label: 'CONFIRMADOS' },
].map(tab => ({ ...tab, count: triageStore.clients.filter(c => correspondeFiltro(c, tab.id)).length })));
function correspondeFiltro(client: TriageClient, filtro: string) {
  return filtro === 'todos' || (filtro === 'agendados' ? client.statusAgendamento === 'AGENDADO' : client.status === filtro);
}
const filteredClients = computed(() => triageStore.clients.filter(c => {
  const texto = `${c.name} ${c.projectTitle} ${c.style} ${c.conceptDescription}`.toLocaleLowerCase('pt-BR');
  return correspondeFiltro(c, activeFilter.value) && texto.includes(searchQuery.value.trim().toLocaleLowerCase('pt-BR'));
}));
const featuredClient = computed(() => filteredClients.value.find(c => c.id === triageStore.selectedClientId) || filteredClients.value[0]);
const filteredQueueClients = computed(() => filteredClients.value.filter(c => c.id !== featuredClient.value?.id));
watch(() => featuredClient.value?.id, () => { attachedFile.value = null; depositError.value = ''; });
onIonViewWillEnter(async () => {
  await carregarComVerificacaoPush();
  const id = Number(route.query.agendamentoId);
  const client = triageStore.clients.find(c => c.agendamentoId === id);
  if (client) {
    activeFilter.value = client.status;
    searchQuery.value = '';
    await selecionarCliente(client);
  }

  // Inicia polling periódico (a cada 25 segundos) enquanto a visualização da triagem estiver ativa
  if (!pollInterval) {
    pollInterval = setInterval(carregarComVerificacaoPush, 25000);
  }
});
onIonViewWillLeave(() => {
  fecharSinal();
  showClienteModal.value = false;
  depositoRegistrado.value = null;
  if (pollInterval) {
    clearInterval(pollInterval);
    pollInterval = null;
  }
});
onUnmounted(() => {
  fecharSinal();
  if (pollInterval) {
    clearInterval(pollInterval);
    pollInterval = null;
  }
});

function valor(amount: number | null) { return amount == null ? 'Não informado' : amount.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 }); }
function moeda(valor: number | null) { return valor == null ? 'Não informado' : valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' }); }

async function carregar() {
  await carregarComVerificacaoPush();
}

async function carregarComVerificacaoPush() {
  const prevIds = new Set(previousClientIds.value);
  await triageStore.fetchTriageQueue();

  // Se já havia registros conhecidos, verifica se surgiu um novo da integração WhatsApp
  if (prevIds.size > 0) {
    const novos = triageStore.clients.filter(c => !prevIds.has(c.id));
    const rec = novos[0];
    if (rec) {
      alert.notifyWhatsAppTriage({
        clientName: rec.name,
        text: rec.projectTitle || rec.conceptDescription || 'Novo orçamento via WhatsApp',
        timeAgo: 'Agora',
        onAction: () => {
          activeFilter.value = 'todos';
          searchQuery.value = '';
          void selecionarCliente(rec);
        },
      });
    }
  }

  previousClientIds.value = new Set(triageStore.clients.map(c => c.id));
}

function simularNovaMensagemWhatsApp() {
  const mockCandidates = [
    { name: 'Isabela Fontes', project: 'Fechamento de antebraço Neotradicional botânico' },
    { name: 'Thiago Albuquerque', project: 'Cobertura (blast-over) em Blackwork geométrico' },
    { name: 'Renata Vasconcellos', project: 'Micro-realismo floral fine line no pulso' },
    { name: 'Lucas Brandão', project: 'Lettering Chicano na clavícula' },
  ];
  const item = mockCandidates[Math.floor(Math.random() * mockCandidates.length)] || mockCandidates[0]!;

  alert.notifyWhatsAppTriage({
    clientName: item.name,
    text: `Orçamento solicitado: ${item.project}`,
    timeAgo: 'Agora',
    onAction: () => {
      searchQuery.value = item.name.split(' ')[0] || '';
      const match = triageStore.clients.find(c => c.name.toLowerCase().includes(item.name.toLowerCase()));
      if (match) {
        void selecionarCliente(match);
      }
    },
  });
}
async function selecionarCliente(client: TriageClient) {
  triageStore.setSelectedClient(client.id);
  await nextTick();
  atendimentoRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' });
}
function iniciarAgendamento(client: TriageClient) { triageStore.setSelectedClient(client.id); void router.push({ path: '/agendamento-sessao', query: { clientId: client.clientId } }); }
function selecionarComprovante(event: Event) { attachedFile.value = (event.target as HTMLInputElement).files?.[0] || null; depositError.value = ''; }
async function registrarSinal() {
  const client = featuredClient.value;
  if (!client || !attachedFile.value || isSubmittingDeposit.value) return;
  if (attachedFile.value.size > 5 * 1024 * 1024 || !/\.(pdf|png|jpe?g)$/i.test(attachedFile.value.name)) {
    depositError.value = 'Selecione um PDF, PNG ou JPEG de até 5 MB.'; return;
  }
  isSubmittingDeposit.value = true;
  depositError.value = '';
  try {
    await triageStore.registerDepositWithReceipt(client.id, attachedFile.value);
    depositoRegistrado.value = { ...client };
    attachedFile.value = null;
  } catch (err) { depositError.value = err instanceof Error ? err.message : 'Não foi possível registrar o sinal.'; }
  finally { isSubmittingDeposit.value = false; }
}
async function cadastrarCliente() {
  if (salvandoCliente.value) return;
  clienteError.value = '';
  const telefone = telefoneCliente.value.replace(/\D/g, '');
  if (!nomeCliente.value.trim() || !/^[1-9]\d{9,14}$/.test(telefone)) { clienteError.value = 'Informe nome e WhatsApp com país e DDD (10 a 15 dígitos).'; return; }
  salvandoCliente.value = true;
  try {
    const client = await criarClienteAgenda(nomeCliente.value.trim(), telefone);
    showClienteModal.value = false;
    nomeCliente.value = ''; telefoneCliente.value = ''; searchQuery.value = ''; activeFilter.value = 'todos';
    await carregar(); triageStore.setSelectedClient(String(client.id));
  } catch (err) { clienteError.value = err instanceof Error ? err.message : 'Não foi possível cadastrar o cliente.'; }
  finally { salvandoCliente.value = false; }
}
function irParaTermo() {
  const client = depositoRegistrado.value;
  if (!client) return;
  triageStore.setSelectedClient(client.id);
  depositoRegistrado.value = null;
  void router.push({ path: '/termo', query: { clientId: client.clientId, agendamentoId: client.agendamentoId } });
}
async function verSinal(client: TriageClient) {
  if (!client.agendamentoId) return;
  fecharSinal(); sinalAgendamentoId.value = client.agendamentoId;
  sinalSelecionado.value = { clientName: client.name, total: client.priceTotal, amount: client.depositAmount, receiptName: client.depositReceipt };
  await carregarComprovante();
}
async function carregarComprovante() {
  const sinal = sinalSelecionado.value;
  const id = sinalAgendamentoId.value;
  if (!sinal || id === null || carregandoSinal.value) return;
  carregandoSinal.value = true; erroSinal.value = '';
  try {
    const blob = await buscarComprovanteSinal(id);
    if (sinalSelecionado.value === sinal) sinal.receiptUrl = URL.createObjectURL(blob);
  } catch (err) { if (sinalSelecionado.value === sinal) erroSinal.value = err instanceof Error ? err.message : 'Não foi possível carregar o comprovante.'; }
  finally { if (sinalSelecionado.value === sinal) carregandoSinal.value = false; }
}
function fecharSinal() {
  if (sinalSelecionado.value?.receiptUrl) URL.revokeObjectURL(sinalSelecionado.value.receiptUrl);
  sinalSelecionado.value = null; sinalAgendamentoId.value = null; carregandoSinal.value = false; erroSinal.value = '';
}
</script>

<style scoped>
ion-content { --background: #09090b; }
.triage-row { row-gap: 24px; }
button { min-height: 40px; }
.campo { display: block; width: 100%; background: #09090b; color: #f2ebd9; border: 1px solid #c9a86a55; border-radius: 4px; padding: 12px; }
</style>