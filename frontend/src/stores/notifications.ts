import { defineStore } from 'pinia';
import { computed, ref } from 'vue';
import {
  listarNotificacoes,
  marcarNotificacaoLidaApi,
  marcarTodasNotificacoesLidasApi,
  type NotificacaoItem,
} from '@/api/notificacoes';

export const useNotificationsStore = defineStore('notifications', () => {
  const items = ref<NotificacaoItem[]>([]);
  const isLoading = ref<boolean>(false);
  const isOpen = ref<boolean>(false);
  const error = ref<string | null>(null);

  const unreadCount = computed(() => items.value.filter((n) => !n.lida).length);
  const hasUnread = computed(() => unreadCount.value > 0);

  async function fetchNotifications(): Promise<void> {
    isLoading.value = true;
    error.value = null;
    try {
      const data = await listarNotificacoes();
      items.value = data;
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Falha ao buscar notificações.';
    } finally {
      isLoading.value = false;
    }
  }

  function toggleOpen(): void {
    isOpen.value = !isOpen.value;
    if (isOpen.value && items.value.length === 0) {
      void fetchNotifications();
    }
  }

  function close(): void {
    isOpen.value = false;
  }

  async function markAsRead(id: string): Promise<void> {
    const item = items.value.find((n) => n.id === id);
    if (item && !item.lida) {
      item.lida = true;
      await marcarNotificacaoLidaApi(id);
    }
  }

  async function markAllAsRead(): Promise<void> {
    items.value.forEach((n) => {
      n.lida = true;
    });
    await marcarTodasNotificacoesLidasApi();
  }

  function clearAll(): void {
    items.value = [];
  }

  return {
    items,
    isLoading,
    isOpen,
    error,
    unreadCount,
    hasUnread,
    fetchNotifications,
    toggleOpen,
    close,
    markAsRead,
    markAllAsRead,
    clearAll,
  };
});
