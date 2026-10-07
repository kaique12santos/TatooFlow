import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

export interface StockItem {
  id: string;
  name: string;
  category: 'agulhas' | 'tintas' | 'biosseguranca' | 'assepsia' | 'esterilizacao' | 'outros';
  brand?: string;
  currentStock: number;
  minStock: number;
  unit: string;
  lotNumber: string;
  expirationDate: string;
  status: 'ok' | 'alerta' | 'critico';
}

export interface AllocationItemPayload {
  id: string;
  title: string;
  quantity: number;
}

export interface AllocationRecord {
  id: string;
  sessionId: string;
  clientId: string;
  allocatedAt: string;
  items: AllocationItemPayload[];
  protocol: string;
}

export const useInventoryStore = defineStore('inventory', () => {
  // Configuração de API
  const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL as string) || (import.meta.env.VITE_API_URL as string) || '';

  // Estado dos Itens em Estoque (Insumos Gerais e Críticos)
  const stockItems = ref<StockItem[]>([
    {
      id: 'cartuchos-03rl',
      name: 'Cartuchos 03RL Cheyenne',
      category: 'agulhas',
      brand: 'Cheyenne',
      currentStock: 4,
      minStock: 15,
      unit: 'unidades',
      lotNumber: 'LT-2026-CH03',
      expirationDate: '10/2027',
      status: 'critico',
    },
    {
      id: 'tinta-dynamic-black',
      name: 'Tinta Dynamic Black (240ml)',
      category: 'tintas',
      brand: 'Dynamic Ink',
      currentStock: 2,
      minStock: 5,
      unit: 'frascos',
      lotNumber: 'LT-DB-8841',
      expirationDate: '05/2028',
      status: 'alerta',
    },
    {
      id: 'cartuchos-07mg',
      name: 'Cartuchos 07MG Magnum',
      category: 'agulhas',
      brand: 'Kwadron',
      currentStock: 28,
      minStock: 10,
      unit: 'unidades',
      lotNumber: 'LT-KW-07MG',
      expirationDate: '12/2027',
      status: 'ok',
    },
    {
      id: 'luvas-m',
      name: 'Luvas Nitrílicas Pretas (M)',
      category: 'biosseguranca',
      brand: 'Supermax',
      currentStock: 180,
      minStock: 40,
      unit: 'pares',
      lotNumber: 'LT-SM-9921',
      expirationDate: '01/2029',
      status: 'ok',
    },
    {
      id: 'stencil-stuff',
      name: 'Stencil Stuff Transfer 250ml',
      category: 'assepsia',
      brand: 'Stencil Stuff',
      currentStock: 7,
      minStock: 3,
      unit: 'frascos',
      lotNumber: 'LT-SS-2026',
      expirationDate: '08/2027',
      status: 'ok',
    },
    {
      id: 'tiras-biologicas',
      name: 'Tiras Indicadoras Biológicas',
      category: 'esterilizacao',
      brand: 'Cristófoli',
      currentStock: 48,
      minStock: 15,
      unit: 'fitas',
      lotNumber: 'LT-CR-891',
      expirationDate: '11/2027',
      status: 'ok',
    },
    {
      id: 'agulha-03rl',
      name: 'Cartucho Agulha 03RL (Chiaroscuro)',
      category: 'agulhas',
      brand: 'Cheyenne Precision',
      currentStock: 38,
      minStock: 10,
      unit: 'un.',
      lotNumber: 'LT-EST-2027',
      expirationDate: '07/2027',
      status: 'ok',
    },
    {
      id: 'carbon-black',
      name: 'Pigmento Carbon Black Puro (#882)',
      category: 'tintas',
      brand: 'Notarial Black',
      currentStock: 14,
      minStock: 4,
      unit: 'un.',
      lotNumber: 'LT-CB-882',
      expirationDate: '04/2028',
      status: 'ok',
    },
    {
      id: 'stencil',
      name: 'Papel Stencil Hectográfico Especial',
      category: 'assepsia',
      brand: 'Spirit Master',
      currentStock: 92,
      minStock: 20,
      unit: 'fl.',
      lotNumber: 'LT-SP-441',
      expirationDate: '09/2028',
      status: 'ok',
    },
    {
      id: 'filme-dermico',
      name: 'Filme Curativo Dérmico Protetor',
      category: 'biosseguranca',
      brand: 'Dermalize Pro',
      currentStock: 12,
      minStock: 3,
      unit: 'rolos',
      lotNumber: 'LT-DP-1510',
      expirationDate: '03/2028',
      status: 'ok',
    },
    {
      id: 'vaselina',
      name: 'Vaselina Dermocosmética Notarial',
      category: 'assepsia',
      brand: 'Atelier Labs',
      currentStock: 50,
      minStock: 10,
      unit: 'un.',
      lotNumber: 'LT-VS-30G',
      expirationDate: '12/2028',
      status: 'ok',
    },
  ]);

  // Histórico de Alocações Realizadas
  const allocationHistory = ref<AllocationRecord[]>([]);
  const isAllocating = ref<boolean>(false);
  const lastAllocationProtocol = ref<string>('');

  // Getters auxiliares
  const criticalItemsCount = computed(() => {
    return stockItems.value.filter(i => i.currentStock <= i.minStock).length;
  });

  const getItemById = (id: string) => {
    return stockItems.value.find(i => i.id === id);
  };

  // Aplicação de dedução no estado local em memória (mock fallback)
  function applyLocalDeduction(items: AllocationItemPayload[]) {
    for (const item of items) {
      // Tenta encontrar o item exato por id ou por correspondência de chave
      const target = stockItems.value.find(
        s => s.id === item.id || s.id.includes(item.id) || item.id.includes(s.id)
      );

      if (target) {
        target.currentStock = Math.max(0, target.currentStock - item.quantity);
        // Atualiza status de criticidade com base no novo saldo
        if (target.currentStock === 0) {
          target.status = 'critico';
        } else if (target.currentStock <= target.minStock) {
          target.status = 'critico';
        } else if (target.currentStock <= target.minStock * 1.5) {
          target.status = 'alerta';
        } else {
          target.status = 'ok';
        }
      }
    }
  }

  // Registra o protocolo de alocação no histórico
  function recordAllocation(payload: {
    sessionId: string;
    clientId: string;
    allocatedAt: string;
    items: AllocationItemPayload[];
  }) {
    const protocol = `BX-EST-${Date.now().toString().slice(-6)}`;
    lastAllocationProtocol.value = protocol;

    allocationHistory.value.unshift({
      id: `alloc-${Date.now()}`,
      sessionId: payload.sessionId,
      clientId: payload.clientId,
      allocatedAt: payload.allocatedAt,
      items: payload.items,
      protocol,
    });
  }

  // Sincroniza lista recebida do backend
  function syncStockFromBackend(remoteItems: Partial<StockItem>[]) {
    if (!Array.isArray(remoteItems)) return;
    for (const remote of remoteItems) {
      if (!remote.id) continue;
      const local = stockItems.value.find(s => s.id === remote.id);
      if (local) {
        Object.assign(local, remote);
      } else {
        stockItems.value.push(remote as StockItem);
      }
    }
  }

  // Action Principal: Alocação de Materiais com Verificação de Backend e Fallback Mock
  async function allocateMaterials(
    sessionId: string,
    clientId: string,
    items: AllocationItemPayload[]
  ): Promise<{ success: boolean; fromBackend: boolean; protocol: string; message: string }> {
    isAllocating.value = true;

    const payload = {
      sessionId,
      clientId,
      allocatedAt: new Date().toISOString(),
      items: items.map(i => ({ id: i.id, title: i.title, quantity: i.quantity })),
    };

    // IF 1: Verifica se há backend configurado com URL
    if (apiBaseUrl) {
      try {
        const response = await fetch(`${apiBaseUrl}/api/estoque/alocar`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(payload),
        });

        // IF 2: Se o backend respondeu com sucesso HTTP 200/201
        if (response.ok) {
          const data = await response.json();
          console.log('[InventoryStore] Baixa de insumos confirmada pelo backend:', data);

          if (data.updatedStock) {
            syncStockFromBackend(data.updatedStock);
          } else {
            applyLocalDeduction(items);
          }

          recordAllocation(payload);
          isAllocating.value = false;

          return {
            success: true,
            fromBackend: true,
            protocol: data.protocol || `BX-SRV-${Date.now().toString().slice(-6)}`,
            message: data.message || 'Alocação e baixa efetuadas no servidor com sucesso.',
          };
        } else {
          console.warn(`[InventoryStore] Backend retornou erro HTTP ${response.status}. Utilizando fallback mock local.`);
        }
      } catch (err) {
        console.warn('[InventoryStore] Falha ao comunicar com backend de estoque. Ativando fallback mock:', err);
      }
    } else {
      console.info('[InventoryStore] VITE_API_URL não configurada. Executando alocação localmente (Mock Reativo).');
    }

    // Fallback Mock: Executa dedução localmente e simula latência de rede realista (250ms)
    await new Promise(resolve => setTimeout(resolve, 250));
    applyLocalDeduction(items);
    recordAllocation(payload);
    isAllocating.value = false;

    return {
      success: true,
      fromBackend: false,
      protocol: lastAllocationProtocol.value,
      message: 'Baixa de materiais registrada no almoxarifado local com sucesso!',
    };
  }

  // Action: Carregar dados do Estoque (com verificação de backend e fallback)
  async function fetchStock(): Promise<void> {
    if (apiBaseUrl) {
      try {
        const response = await fetch(`${apiBaseUrl}/api/estoque/itens`);
        if (response.ok) {
          const data = await response.json();
          if (Array.isArray(data)) {
            syncStockFromBackend(data);
            return;
          }
        }
      } catch (err) {
        console.warn('[InventoryStore] Backend indisponível para fetchStock. Mantendo estoque mockado:', err);
      }
    }
  }

  // Ajuste manual (usado pelo almoxarife na tela de Estoque)
  function adjustItemStock(itemId: string, delta: number) {
    const item = stockItems.value.find(s => s.id === itemId);
    if (item) {
      item.currentStock = Math.max(0, item.currentStock + delta);
    }
  }

  // Action com IF: Cadastrar novo insumo (Backend vs Fallback Mock)
  async function createStockItem(payload: {
    name: string;
    category: string;
    brand?: string;
    unitType?: string;
    yieldEstimate?: string;
    initialStock: number;
    minLevel: number;
    location?: string;
  }): Promise<{ success: boolean; fromBackend: boolean; item: StockItem; protocol: string; message: string }> {
    const status: 'ok' | 'alerta' | 'critico' =
      payload.initialStock <= payload.minLevel * 0.5
        ? 'critico'
        : payload.initialStock <= payload.minLevel
        ? 'alerta'
        : 'ok';

    const newItem: StockItem = {
      id: `item-${Date.now()}`,
      name: payload.name,
      category: (payload.category as any) || 'outros',
      brand: payload.brand || 'Atelier Standard',
      currentStock: Number(payload.initialStock) || 0,
      minStock: Number(payload.minLevel) || 1,
      unit: payload.unitType || 'unidades',
      lotNumber: `LT-${new Date().getFullYear()}-${Math.floor(1000 + Math.random() * 9000)}`,
      expirationDate: '12/2028',
      status,
    };

    // IF 1: Verifica se há endpoint configurado
    if (apiBaseUrl) {
      try {
        const response = await fetch(`${apiBaseUrl}/api/estoque/itens`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(newItem),
        });

        // IF 2: Se o backend respondeu com sucesso
        if (response.ok) {
          const data = await response.json();
          const savedItem: StockItem = data.item || newItem;
          stockItems.value.unshift(savedItem);
          console.log('[InventoryStore] Insumo cadastrado no backend:', savedItem);
          return {
            success: true,
            fromBackend: true,
            item: savedItem,
            protocol: data.protocol || `CAD-SRV-${Date.now().toString().slice(-6)}`,
            message: 'Insumo cadastrado no servidor com sucesso!',
          };
        } else {
          console.warn(`[InventoryStore] Erro HTTP ${response.status} ao cadastrar insumo. Usando fallback mock.`);
        }
      } catch (err) {
        console.warn('[InventoryStore] Falha ao comunicar com backend para cadastrar item. Ativando fallback mock:', err);
      }
    } else {
      console.info('[InventoryStore] VITE_API_URL não configurada. Cadastrando insumo no mock reativo local.');
    }

    // Fallback Mock garantido
    await new Promise(resolve => setTimeout(resolve, 200));
    stockItems.value.unshift(newItem);
    return {
      success: true,
      fromBackend: false,
      item: newItem,
      protocol: `CAD-LOC-${Date.now().toString().slice(-6)}`,
      message: 'Insumo registrado no almoxarifado local com sucesso!',
    };
  }

  return {
    stockItems,
    allocationHistory,
    isAllocating,
    lastAllocationProtocol,
    criticalItemsCount,
    getItemById,
    allocateMaterials,
    fetchStock,
    adjustItemStock,
    createStockItem,
  };
});
