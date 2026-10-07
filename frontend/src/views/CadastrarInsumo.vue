<template>
  <ion-page>
    <ion-content class="bg-[#09090b] text-[#f2ebd9] select-none font-sans" fullscreen>
      <div class="min-h-screen bg-[#09090b] pb-28 max-w-[480px] mx-auto px-4 pt-3 sm:px-6">
        <!-- Reusable App Header -->
        <AppHeader />

        <!-- Back Link -->
        <button
          @click="goBack"
          class="flex items-center gap-1.5 font-inter text-[10px] font-bold text-[#e6c383] uppercase tracking-wider mb-3 hover:text-[#d6b77e] transition-colors cursor-pointer"
        >
          <ArrowLeft class="w-3.5 h-3.5" />
          <span>VOLTAR AO ESTOQUE</span>
        </button>

        <!-- Top Header Card -->
        <div class="bg-[#141416] border border-[rgba(255,255,255,0.06)] rounded-[2px] p-4 mb-4 shadow-xl">
          <div class="flex items-center gap-1.5 mb-1.5">
            <span class="w-1.5 h-1.5 bg-[#e6c383] rounded-[1px]"></span>
            <span class="font-inter text-[9px] font-bold text-[#e6c383] tracking-[1.44px] uppercase">
              ALMOXARIFADO & BANCADAS • CADASTRO DE ITEM
            </span>
          </div>

          <h1 class="font-playfair text-[26px] sm:text-[28px] font-semibold text-[#f2ebd9] leading-tight mb-1">
            Cadastrar Novo Insumo
          </h1>

          <p class="font-montserrat text-[12px] text-[#a39e93] leading-relaxed">
            Cadastre novos consumíveis e materiais para controle de reposição e kit de bancada.
          </p>
        </div>

        <!-- Section 01: Identificação do Insumo -->
        <div class="bg-[#141416] border border-[rgba(255,255,255,0.06)] rounded-[2px] p-4 mb-4 shadow-xl">
          <!-- Section Title Row -->
          <div class="flex items-center justify-between mb-4">
            <div class="flex items-center gap-2">
              <Package class="w-4 h-4 text-[#e6c383]" />
              <h2 class="font-inter text-[11px] font-bold text-[#f2ebd9] tracking-wider uppercase">
                01. IDENTIFICAÇÃO DO INSUMO
              </h2>
            </div>

            <span class="bg-[#2a2012] border border-[rgba(201,168,106,0.3)] text-[#e6c383] font-inter text-[8px] font-bold tracking-wider uppercase px-2 py-0.5 rounded-[2px]">
              OBRIGATÓRIO
            </span>
          </div>

          <!-- Field 1: Nome do Insumo -->
          <div class="mb-4">
            <div class="flex items-center justify-between mb-1.5">
              <label class="font-inter text-[9px] font-bold text-[#c9a86a] tracking-[1.44px] uppercase">
                NOME DO INSUMO
              </label>
              <span class="font-inter text-[9px] text-[#a39e93] tracking-widest uppercase">
                EXCLUSIVO
              </span>
            </div>

            <input
              v-model="form.name"
              type="text"
              placeholder="ex: Cartucho Cheyenne 03RL Craft, Tinta Dynamic Black 240ml"
              class="w-full bg-[#09090b] border border-[rgba(255,255,255,0.06)] focus:border-[#c9a86a] rounded-[2px] p-3 font-montserrat text-[12px] text-[#f2ebd9] placeholder-[#52525b] transition-colors outline-none"
            />
          </div>

          <!-- Field 2: Categoria do Material -->
          <div class="mb-4">
            <label class="font-inter text-[9px] font-bold text-[#c9a86a] tracking-[1.44px] uppercase block mb-2">
              CATEGORIA DO MATERIAL
            </label>

            <div class="grid grid-cols-2 gap-2">
              <button
                v-for="cat in categories"
                :key="cat.id"
                @click="form.category = cat.id"
                :class="[
                  'py-2.5 px-2.5 rounded-[2px] font-inter text-[9px] font-bold tracking-wider uppercase flex items-center gap-2 transition-all cursor-pointer text-left',
                  form.category === cat.id
                    ? 'bg-[#eedaa2] text-[#09090b] shadow-md font-extrabold'
                    : 'bg-[#09090b] text-[#a39e93] border border-[rgba(255,255,255,0.06)] hover:border-[rgba(201,168,106,0.3)] hover:text-[#f2ebd9]'
                ]"
              >
                <component :is="cat.icon" class="w-3.5 h-3.5 shrink-0" />
                <span class="truncate">{{ cat.label }}</span>
              </button>
            </div>
          </div>

          <!-- Field 3: Marca / Fabricante -->
          <div>
            <label class="font-inter text-[9px] font-bold text-[#c9a86a] tracking-[1.44px] uppercase block mb-1.5">
              MARCA / FABRICANTE
            </label>

            <input
              v-model="form.brand"
              type="text"
              placeholder="ex: Kwadron, Electric Ink, Hype, Cheyenne"
              class="w-full bg-[#09090b] border border-[rgba(255,255,255,0.06)] focus:border-[#c9a86a] rounded-[2px] p-3 font-montserrat text-[12px] text-[#f2ebd9] placeholder-[#52525b] transition-colors outline-none"
            />
          </div>
        </div>

        <!-- Section 02: Fracionamento & Unidade -->
        <div class="bg-[#141416] border border-[rgba(255,255,255,0.06)] rounded-[2px] p-4 mb-4 shadow-xl">
          <!-- Section Title Row -->
          <div class="flex items-center justify-between mb-4">
            <div class="flex items-center gap-2">
              <Ruler class="w-4 h-4 text-[#e6c383]" />
              <h2 class="font-inter text-[11px] font-bold text-[#f2ebd9] tracking-wider uppercase">
                02. FRACIONAMENTO & UNIDADE
              </h2>
            </div>

            <span class="font-inter text-[9px] text-[#a39e93] tracking-widest uppercase">
              MÉTRICA
            </span>
          </div>

          <!-- Unit Type Options -->
          <div class="mb-4">
            <label class="font-inter text-[9px] font-bold text-[#c9a86a] tracking-[1.44px] uppercase block mb-2">
              TIPO DE FRACIONAMENTO / MEDIDA
            </label>

            <!-- Row 1: 3 columns -->
            <div class="grid grid-cols-3 gap-2 mb-2">
              <button
                v-for="unit in unitOptionsRow1"
                :key="unit.id"
                @click="form.unitType = unit.id"
                :class="[
                  'py-2 px-1.5 rounded-[2px] font-inter text-[9px] font-bold tracking-wider uppercase transition-all cursor-pointer text-center truncate',
                  form.unitType === unit.id
                    ? 'bg-[#eedaa2] text-[#09090b] shadow-md font-extrabold'
                    : 'bg-[#09090b] text-[#a39e93] border border-[rgba(255,255,255,0.06)] hover:border-[rgba(201,168,106,0.3)] hover:text-[#f2ebd9]'
                ]"
              >
                {{ unit.label }}
              </button>
            </div>

            <!-- Row 2: 2 columns -->
            <div class="grid grid-cols-2 gap-2">
              <button
                v-for="unit in unitOptionsRow2"
                :key="unit.id"
                @click="form.unitType = unit.id"
                :class="[
                  'py-2 px-2 rounded-[2px] font-inter text-[9px] font-bold tracking-wider uppercase transition-all cursor-pointer text-center truncate',
                  form.unitType === unit.id
                    ? 'bg-[#eedaa2] text-[#09090b] shadow-md font-extrabold'
                    : 'bg-[#09090b] text-[#a39e93] border border-[rgba(255,255,255,0.06)] hover:border-[rgba(201,168,106,0.3)] hover:text-[#f2ebd9]'
                ]"
              >
                {{ unit.label }}
              </button>
            </div>
          </div>

          <!-- Yield / Items per box -->
          <div>
            <div class="flex items-center justify-between mb-1.5">
              <label class="font-inter text-[9px] font-bold text-[#c9a86a] tracking-[1.44px] uppercase">
                RENDIMENTO ESTIMADO / ITENS POR CAIXA
              </label>
              <span class="font-inter text-[8px] text-[#a39e93] tracking-widest uppercase">
                (OPCIONAL)
              </span>
            </div>

            <div class="relative flex items-center">
              <input
                v-model="form.yieldEstimate"
                type="text"
                placeholder="ex: 20 un por caixa, ou 120 borrifadas"
                class="w-full bg-[#09090b] border border-[rgba(255,255,255,0.06)] focus:border-[#c9a86a] rounded-[2px] p-3 pr-10 font-montserrat text-[12px] text-[#f2ebd9] placeholder-[#52525b] transition-colors outline-none"
              />
              <Calculator class="w-4 h-4 text-[#a39e93] absolute right-3 pointer-events-none" />
            </div>
          </div>
        </div>

        <!-- Section 03: Parâmetros de Reposição -->
        <div class="bg-[#141416] border border-[rgba(255,255,255,0.06)] rounded-[2px] p-4 mb-4 shadow-xl">
          <!-- Section Title Row -->
          <div class="flex items-center justify-between mb-4">
            <div class="flex items-center gap-2">
              <SlidersHorizontal class="w-4 h-4 text-[#e6c383]" />
              <h2 class="font-inter text-[11px] font-bold text-[#f2ebd9] tracking-wider uppercase">
                03. PARÂMETROS DE REPOSIÇÃO
              </h2>
            </div>

            <span class="bg-[#8c2b2b] text-[#f2ebd9] font-inter text-[8px] font-bold tracking-wider uppercase px-2 py-0.5 rounded-[2px]">
              ALERTA ATIVO
            </span>
          </div>

          <!-- Quantities Grid -->
          <div class="grid grid-cols-2 gap-3 mb-3">
            <!-- Estoque Inicial -->
            <div>
              <label class="font-inter text-[9px] font-bold text-[#a39e93] tracking-wider uppercase block mb-1">
                ESTOQUE INICIAL
              </label>
              <div class="bg-[#09090b] border border-[rgba(255,255,255,0.06)] rounded-[2px] px-3 py-2.5 flex items-center justify-between">
                <input
                  v-model.number="form.initialStock"
                  type="number"
                  class="bg-transparent font-jetbrains text-[16px] font-bold text-[#f2ebd9] w-20 outline-none"
                />
                <span class="font-inter text-[9px] font-bold text-[#a39e93] uppercase">
                  UN
                </span>
              </div>
            </div>

            <!-- Nível Mínimo -->
            <div>
              <label class="font-inter text-[9px] font-bold text-[#a39e93] tracking-wider uppercase block mb-1">
                NÍVEL MÍNIMO
              </label>
              <div class="bg-[#09090b] border border-[rgba(255,255,255,0.06)] rounded-[2px] px-3 py-2.5 flex items-center justify-between">
                <input
                  v-model.number="form.minLevel"
                  type="number"
                  class="bg-transparent font-jetbrains text-[16px] font-bold text-[#f2ebd9] w-20 outline-none"
                />
                <span class="font-inter text-[9px] font-bold text-[#a39e93] uppercase">
                  GATILHO
                </span>
              </div>
            </div>
          </div>

          <!-- Critical Notification Notice Box -->
          <div class="bg-[#1c1813] border border-[rgba(201,168,106,0.2)] rounded-[2px] p-3 mb-4 flex items-start gap-2.5">
            <Bell class="w-4 h-4 text-[#e6c383] shrink-0 mt-0.5" />
            <p class="font-montserrat text-[11px] text-[#eedaa2] leading-relaxed">
              Ao atingir {{ form.minLevel || 10 }} unidades, o atelier será notificado no painel crítico para requisição automática de compra.
            </p>
          </div>

          <!-- Localização no Atelier -->
          <div>
            <label class="font-inter text-[9px] font-bold text-[#c9a86a] tracking-[1.44px] uppercase block mb-1.5">
              LOCALIZAÇÃO NO ATELIER / GAVETEIRO
            </label>

            <div class="relative flex items-center">
              <Archive class="w-4 h-4 text-[#a39e93] absolute left-3 pointer-events-none" />
              <input
                v-model="form.location"
                type="text"
                placeholder="ex: Armário A • Gaveta 03 • Prateleira Esterilizada"
                class="w-full bg-[#09090b] border border-[rgba(255,255,255,0.06)] focus:border-[#c9a86a] rounded-[2px] pl-10 pr-3 py-3 font-montserrat text-[12px] text-[#f2ebd9] placeholder-[#52525b] transition-colors outline-none"
              />
            </div>
          </div>
        </div>

        <!-- Protocol Ingot Bar -->
        <div class="bg-[#141416] border border-[rgba(255,255,255,0.06)] rounded-[2px] p-3 mb-5 flex items-center justify-between shadow-lg">
          <div class="flex items-center gap-2.5">
            <Cpu class="w-4 h-4 text-[#e6c383]" />
            <div>
              <span class="font-inter text-[8px] font-bold text-[#a39e93] uppercase tracking-wider block">
                PROTOCOLO DE REGISTRO
              </span>
              <span class="font-jetbrains text-[11px] font-bold text-[#f2ebd9]">
                ATL-INV-HASH-9812B
              </span>
            </div>
          </div>

          <div class="inline-flex items-center gap-1 bg-[#102217] border border-[rgba(74,222,128,0.3)] text-[#4ade80] px-2 py-0.5 rounded-[2px] font-inter text-[9px] font-bold tracking-wider uppercase">
            <Lock class="w-2.5 h-2.5" />
            <span>ATIVO</span>
          </div>
        </div>

        <!-- Save Button CTA -->
        <button
          @click="saveItem"
          :disabled="isSaving"
          class="w-full bg-[#eedaa2] hover:bg-[#d6b77e] disabled:opacity-60 text-[#09090b] font-inter text-[11px] font-bold tracking-[1.44px] uppercase py-3.5 px-4 rounded-[2px] shadow-xl flex items-center justify-center gap-2 mb-3 transition-all cursor-pointer active:scale-[0.99]"
        >
          <span>{{ isSaving ? 'SALVANDO INSUMO...' : 'SALVAR E ADICIONAR AO ESTOQUE' }}</span>
          <ArrowRight class="w-4 h-4 stroke-[2.5]" />
        </button>

        <!-- Cancel Link -->
        <button
          @click="goBack"
          :disabled="isSaving"
          class="font-inter text-[10px] font-bold tracking-[1.2px] text-[#a39e93] hover:text-[#f2ebd9] uppercase text-center block w-full transition-colors cursor-pointer mb-6"
        >
          CANCELAR E DESCARTAR
        </button>
      </div>

    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { IonPage, IonContent } from '@ionic/vue';
import {
  ArrowLeft,
  ArrowRight,
  Package,
  Ruler,
  SlidersHorizontal,
  Bell,
  Archive,
  Cpu,
  Lock,
  Calculator,
  PenTool,
  Droplet,
  Shield,
  FlaskConical,
  Layers,
  HeartPulse,
  Calendar,
  Users,
  ScrollText,
  Boxes,
  Scale,
} from '@lucide/vue';
import { useRouter } from 'vue-router';
import AppHeader from '../components/AppHeader.vue';
import { useInventoryStore } from '@/stores/inventory';

const router = useRouter();
const inventoryStore = useInventoryStore();

const isSaving = ref(false);

// Form Data
const form = ref({
  name: '',
  category: 'agulhas',
  brand: '',
  unitType: 'unidade',
  yieldEstimate: '',
  initialStock: 40,
  minLevel: 10,
  location: '',
});

// Category Options (6 items)
const categories = [
  { id: 'agulhas', label: 'AGULHAS & CARTUCHOS', icon: PenTool },
  { id: 'tintas', label: 'PIGMENTOS & TINTAS', icon: Droplet },
  { id: 'biosseguranca', label: 'BIOSSEGURANÇA & EPIS', icon: Shield },
  { id: 'assepsia', label: 'ASSEPSIA & CIRÚRGICO', icon: FlaskConical },
  { id: 'decalque', label: 'DECALQUE & STENCIL', icon: Layers },
  { id: 'pos_tattoo', label: 'PÓS-TATTOO & CURATIVO', icon: HeartPulse },
];

// Unit Options
const unitOptionsRow1 = [
  { id: 'unidade', label: 'UNIDADE (UN)' },
  { id: 'caixa', label: 'CAIXA (CX)' },
  { id: 'frasco', label: 'FRASCO (ML)' },
];

const unitOptionsRow2 = [
  { id: 'par', label: 'PAR (PARES)' },
  { id: 'folha', label: 'FOLHA / ROLO (FL/RL)' },
];

function goBack() {
  router.push('/estoque');
}

async function saveItem() {
  if (!form.value.name.trim()) {
    alert('Por favor, informe o nome do insumo antes de salvar.');
    return;
  }

  isSaving.value = true;
  try {
    // Executa cadastro com verificação condicional (backend se disponível vs fallback mock local)
    const result = await inventoryStore.createStockItem(form.value);
    console.log('[CadastrarInsumo] Insumo processado:', result);
    router.push('/estoque');
  } catch (err) {
    console.warn('[CadastrarInsumo] Erro no cadastro:', err);
    router.push('/estoque');
  } finally {
    isSaving.value = false;
  }
}
</script>
