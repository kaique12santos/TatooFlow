import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { apiRequest } from '@/api/http';

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

interface BackendItemEstoque {
  id: number;
  nome: string;
  categoria?: string;
  quantidade?: number;
  quantidadeMinima?: number;
  unidadeMedida?: string;
}

export const useInventoryStore = defineStore('inventory', () => {
  // Itens em estoque reais vindos do banco de dados (tabela estoque_itens)
  const stockItems = ref<StockItem[]>([]);
  const allocationHistory = ref<AllocationRecord[]>([]);
  const isAllocating = ref<boolean>(false);
  const isLoading = ref<boolean>(false);
  const error = ref<string | null>(null);
  const lastAllocationProtocol = ref<string>('');

  // Itens críticos baseados nos limites cadastrados no banco
  const criticalItemsCount = computed(() => {
    return stockItems.value.filter(i => i.currentStock <= i.minStock).length;
  });

  const getItemById = (id: string) => {
    return stockItems.value.find(i => i.id === id || i.id === String(id));
  };

  // Carregar dados de Estoque direto do backend (/api/estoque)
  async function fetchStock(): Promise<void> {
    isLoading.value = true;
    error.value = null;

    try {
      const data = await apiRequest<BackendItemEstoque[]>('/api/estoque');
      if (Array.isArray(data)) {
        stockItems.value = data.map((item: BackendItemEstoque) => {
          const qty = Number(item.quantidade ?? 0);
          const min = Number(item.quantidadeMinima ?? 0);
          const status: 'ok' | 'alerta' | 'critico' =
            qty <= min
              ? 'critico'
              : (qty <= min * 1.5 ? 'alerta' : 'ok');

          const cat = (item.categoria || 'outros').toLowerCase();
          const category: StockItem['category'] = ['agulhas', 'tintas', 'biosseguranca', 'assepsia', 'esterilizacao'].includes(cat)
            ? (cat as any)
            : 'outros';

          return {
            id: String(item.id),
            name: item.nome,
            category,
            brand: 'Atelier',
            currentStock: qty,
            minStock: min,
            unit: item.unidadeMedida || 'unidades',
            lotNumber: `LT-${item.id}`,
            expirationDate: '-',
            status,
          };
        });
      } else {
        stockItems.value = [];
      }
    } catch (err: any) {
      stockItems.value = [];
      error.value = err?.message || 'Erro ao carregar estoque do banco de dados.';
      console.warn('[InventoryStore] Erro ao sincronizar estoque:', err);
    } finally {
      isLoading.value = false;
    }
  }

  // Alocação de Materiais via endpoint /api/estoque/alocar
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

    try {
      await apiRequest<any>('/api/estoque/alocar', {
        method: 'POST',
        body: JSON.stringify(payload),
      });

      const protocol = `BX-SRV-${Date.now().toString().slice(-6)}`;
      lastAllocationProtocol.value = protocol;

      allocationHistory.value.unshift({
        id: `alloc-${Date.now()}`,
        sessionId,
        clientId,
        allocatedAt: payload.allocatedAt,
        items,
        protocol,
      });

      // Recarrega o estoque atualizado do banco
      await fetchStock();

      return {
        success: true,
        fromBackend: true,
        protocol,
        message: 'Alocação de insumos concluída no servidor com sucesso.',
      };
    } catch (err: any) {
      return {
        success: false,
        fromBackend: false,
        protocol: '',
        message: err?.message || 'Erro ao comunicar com o servidor de estoque.',
      };
    } finally {
      isAllocating.value = false;
    }
  }

  // Ajuste manual de estoque no servidor
  async function adjustItemStock(itemId: string, delta: number) {
    const item = stockItems.value.find(s => s.id === itemId);
    if (!item) return;

    const novaQuantidade = Math.max(0, item.currentStock + delta);
    try {
      await apiRequest<any>(`/api/estoque/${itemId}`, {
        method: 'PUT',
        body: JSON.stringify({
          nome: item.name,
          categoria: item.category.toUpperCase(),
          quantidade: novaQuantidade,
          quantidadeMinima: item.minStock,
          unidadeMedida: item.unit,
        }),
      });
      item.currentStock = novaQuantidade;
      item.status = novaQuantidade <= item.minStock ? 'critico' : 'ok';
    } catch (err) {
      console.warn('[InventoryStore] Falha ao atualizar saldo no backend:', err);
    }
  }

  // Cadastrar novo insumo no banco (/api/estoque)
  async function createStockItem(payload: {
    name: string;
    category: string;
    brand?: string;
    unitType?: string;
    yieldEstimate?: string;
    initialStock: number;
    minLevel: number;
    location?: string;
  }): Promise<{ success: boolean; fromBackend: boolean; item?: StockItem; protocol: string; message: string }> {
    try {
      const created = await apiRequest<BackendItemEstoque>('/api/estoque', {
        method: 'POST',
        body: JSON.stringify({
          nome: payload.name,
          categoria: payload.category.toUpperCase(),
          quantidade: Number(payload.initialStock) || 0,
          quantidadeMinima: Number(payload.minLevel) || 1,
          unidadeMedida: payload.unitType || 'unidades',
        }),
      });

      await fetchStock();

      return {
        success: true,
        fromBackend: true,
        protocol: `CAD-SRV-${created.id}`,
        message: 'Insumo cadastrado no banco de dados com sucesso!',
      };
    } catch (err: any) {
      return {
        success: false,
        fromBackend: false,
        protocol: '',
        message: err?.message || 'Falha ao cadastrar insumo no servidor.',
      };
    }
  }

  return {
    stockItems,
    allocationHistory,
    isAllocating,
    isLoading,
    error,
    lastAllocationProtocol,
    criticalItemsCount,
    getItemById,
    allocateMaterials,
    fetchStock,
    adjustItemStock,
    createStockItem,
  };
});
