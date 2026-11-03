<template>
  <ion-page>
    <ion-content class="bg-[#09090b] text-[#f2ebd9] select-none font-sans" fullscreen>
      <div class="min-h-screen bg-[#09090b] pb-24 max-w-[480px] mx-auto px-4 pt-3 sm:px-6">
        <!-- Reusable App Header -->
        <AppHeader />

        <!-- Page Context Header -->
        <div class="mb-4">
          <div class="flex items-center gap-1.5 mb-1 px-0.5">
            <span class="text-[#c9a86a] text-[10px] leading-none">✦</span>
            <span class="font-jetbrains text-[10px] font-bold tracking-[1.8px] text-[#c9a86a] uppercase">
              GESTÃO DE PRONTUÁRIOS
            </span>
          </div>
          <div class="flex items-center justify-between">
            <h1 class="font-playfair text-[26px] font-semibold text-[#f2ebd9] leading-tight">
              Catálogo de Clientes
            </h1>
            <span class="bg-[#141416] border border-[rgba(201,168,106,0.3)] text-[#e6c383] font-jetbrains text-[10px] font-bold px-2 py-0.5 rounded-[2px] tracking-wider uppercase">
              {{ filteredClients.length }} CLIENTES
            </span>
          </div>
          <p class="font-montserrat text-[12px] text-[#a39e93] leading-relaxed mt-1">
            Dossiês clínicos e artísticos, histórico dermatológico e controle de sessões.
          </p>
        </div>

        <!-- Search Bar -->
        <div class="mb-3.5">
          <div class="relative bg-[#141416] rounded-[2px] border border-[rgba(255,255,255,0.06)] flex items-center px-3 py-2.5 shadow-inner focus-within:border-[rgba(201,168,106,0.5)] transition-all">
            <Search class="w-4 h-4 text-[#a39e93] mr-2.5 shrink-0" />
            <input
              v-model="searchQuery"
              type="text"
              placeholder="Buscar por nome, CPF, dossiê ou projeto..."
              class="w-full bg-transparent text-[13px] font-montserrat text-[#f2ebd9] placeholder-[#a39e93] focus:outline-none"
            />
            <button
              v-if="searchQuery"
              @click="searchQuery = ''"
              class="text-[#a39e93] hover:text-[#f2ebd9] p-1 transition-colors"
            >
              <X class="w-3.5 h-3.5" />
            </button>
          </div>
        </div>

        <!-- Filter Tabs -->
        <div class="flex items-center gap-1.5 overflow-x-auto no-scrollbar pb-1 mb-4">
          <button
            v-for="filter in filters"
            :key="filter.id"
            @click="activeFilter = filter.id"
            :class="[
              'px-3 py-1.5 rounded-[2px] font-jetbrains text-[10px] font-bold tracking-wider uppercase whitespace-nowrap cursor-pointer transition-all',
              activeFilter === filter.id
                ? 'bg-[#c9a86a] text-[#09090b] shadow-sm'
                : 'bg-[#141416] border border-[rgba(255,255,255,0.06)] text-[#a39e93] hover:text-[#f2ebd9]'
            ]"
          >
            {{ filter.label }}
          </button>
        </div>

        <!-- Client Cards Deck -->
        <div class="space-y-3.5">
          <div
            v-for="client in filteredClients"
            :key="client.id"
            class="bg-[#141416] rounded-[2px] p-4 border border-[rgba(255,255,255,0.06)] hover:border-[rgba(201,168,106,0.3)] shadow-xl transition-all duration-200"
          >
            <!-- Card Header: Monogram/Avatar, Name, Dossier # and Status -->
            <div class="flex items-start justify-between gap-3 mb-3">
              <div class="flex items-center gap-3">
                <div class="w-12 h-12 bg-[#09090b] border border-[rgba(201,168,106,0.3)] rounded-[2px] flex items-center justify-center shrink-0 shadow-inner">
                  <span class="font-playfair font-bold text-[18px] text-[#c9a86a] tracking-wider">
                    {{ client.initials }}
                  </span>
                </div>
                <div>
                  <h3 class="font-playfair text-[18px] font-semibold text-[#f2ebd9] leading-tight">
                    {{ client.name }}
                  </h3>
                  <div class="flex items-center gap-2 mt-0.5">
                    <span class="font-jetbrains text-[11px] text-[#c9a86a] font-medium">
                      {{ client.dossierNumber }}
                    </span>
                    <span class="text-[9px] text-[#63615b]">•</span>
                    <span class="font-jetbrains text-[11px] text-[#a39e93]">
                      {{ client.phone }}
                    </span>
                  </div>
                </div>
              </div>

              <span
                :class="[
                  'px-2 py-0.5 rounded-[2px] font-inter text-[9px] font-bold tracking-wider uppercase border',
                  client.status === 'valid'
                    ? 'bg-[#0f2416] text-[#81c784] border-[#2e7d32]/30'
                    : client.status === 'pending'
                    ? 'bg-[#291b08] text-[#ffb74d] border-[#f57c00]/30'
                    : 'bg-[#18181b] text-[#a39e93] border-[#3f3f46]/40'
                ]"
              >
                {{ client.statusLabel }}
              </span>
            </div>

            <!-- Project Details Ingot -->
            <div class="bg-[#09090b] rounded-[2px] p-2.5 mb-3 border border-[rgba(255,255,255,0.03)] space-y-1">
              <div class="flex items-center justify-between text-[11px]">
                <span class="font-inter text-[9px] font-bold text-[#a39e93] tracking-widest uppercase">
                  OBRA DESIGNADA
                </span>
                <span class="font-jetbrains text-[10px] text-[#e6c383]">
                  {{ client.sessionsCount }} SESSÕES
                </span>
              </div>
              <p class="font-montserrat text-[12.5px] text-[#f2ebd9] font-medium truncate">
                {{ client.projectTitle }}
              </p>
              <p class="font-montserrat text-[11px] text-[#a39e93] truncate">
                Local: {{ client.tattooLocation }} • Estilo: {{ client.style }}
              </p>
            </div>

            <!-- Internal Navigation Action Buttons -->
            <div class="flex items-center gap-2 pt-0.5">
              <button
                @click="openDossier(client)"
                class="flex-1 py-2 px-3 rounded-[2px] bg-[#e6c383] text-[#09090b] hover:bg-[#d6b77e] font-inter text-[9px] font-bold tracking-wider uppercase flex items-center justify-center gap-1.5 shadow-md transition-all active:scale-[0.98] cursor-pointer"
              >
                <FolderOpen class="w-3.5 h-3.5" />
                <span>ABRIR DOSSIÊ</span>
              </button>

              <button
                @click="startNewSession(client)"
                class="py-2 px-3 rounded-[2px] bg-[#1a1a1e] text-[#f2ebd9] hover:bg-[#25252a] border border-[rgba(255,255,255,0.06)] font-inter text-[9px] font-bold tracking-wider uppercase flex items-center justify-center gap-1.5 transition-all active:scale-[0.98] cursor-pointer"
                title="Nova Sessão / Mídia"
              >
                <Camera class="w-3.5 h-3.5 text-[#e6c383]" />
                <span>MÍDIA</span>
              </button>

              <button
                @click="contactWhatsapp(client)"
                class="py-2 px-2.5 rounded-[2px] bg-[#1a1a1e] text-[#a39e93] hover:text-[#81c784] hover:bg-[#25252a] border border-[rgba(255,255,255,0.06)] transition-all cursor-pointer"
                title="WhatsApp"
              >
                <MessageSquare class="w-3.5 h-3.5" />
              </button>
            </div>
          </div>

          <!-- Empty State -->
          <div
            v-if="filteredClients.length === 0"
            class="text-center py-12 bg-[#141416] rounded-[2px] border border-[rgba(255,255,255,0.04)]"
          >
            <Users class="w-8 h-8 text-[#a39e93] mx-auto mb-2 opacity-40" />
            <p class="font-playfair text-[16px] text-[#f2ebd9]">Nenhum cliente encontrado</p>
            <p class="font-montserrat text-[12px] text-[#a39e93] mt-1">
              Ajuste o termo de busca ou filtros selecionados.
            </p>
          </div>
        </div>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { IonPage, IonContent } from '@ionic/vue';
import { Search, X, Users, FolderOpen, Camera, MessageSquare } from '@lucide/vue';
import { useRouter } from 'vue-router';
import AppHeader from '../components/AppHeader.vue';
import { useClientsStore, type ClientItem } from '@/stores/clients';

const router = useRouter();
const clientsStore = useClientsStore();

const searchQuery = ref('');
const activeFilter = ref('todos');

const filters = [
  { id: 'todos', label: 'TODOS (4)' },
  { id: 'valid', label: 'ANAMNESE OK (2)' },
  { id: 'pending', label: 'PENDENTE (1)' },
  { id: 'finished', label: 'CONCLUÍDO (1)' },
];

// Sincroniza com o endpoint /api/clientes (com verificação condicional e fallback mock)
onMounted(async () => {
  await clientsStore.fetchClients();
});

const clients = computed<ClientItem[]>(() => clientsStore.clients);

const filteredClients = computed(() => {
  return clients.value.filter((client) => {
    const matchesFilter = activeFilter.value === 'todos' || client.status === activeFilter.value;
    const q = searchQuery.value.toLowerCase().trim();
    const matchesQuery =
      !q ||
      client.name.toLowerCase().includes(q) ||
      client.dossierNumber.toLowerCase().includes(q) ||
      client.cpf.toLowerCase().includes(q) ||
      client.projectTitle.toLowerCase().includes(q) ||
      client.style.toLowerCase().includes(q);
    return matchesFilter && matchesQuery;
  });
});

// Internal navigation to secondary views
function openDossier(client: ClientItem) {
  router.push({ name: 'DossieCliente', query: { id: client.id } });
}

function startNewSession(_client: ClientItem) {
  router.push({ name: 'UploadArte' });
}

function contactWhatsapp(client: ClientItem) {
  alert(`Abrindo WhatsApp para contato com ${client.name} (${client.phone})`);
}
</script>
