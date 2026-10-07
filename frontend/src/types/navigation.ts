import type { Component } from 'vue';

export type UserRole = 'tatuador' | 'admin';

export type MainTabTatuador = 'agenda' | 'clientes' | 'midia';
export type MainTabAdmin = 'triagem' | 'gestao' | 'estoque' | 'equipe';
export type TabKey = MainTabTatuador | MainTabAdmin;

export interface AppNavigationItem {
  id: TabKey;
  name: string;
  icon: Component;
  route: { name?: string; path?: string };
  match: string[];
  role: UserRole;
  badge?: string | number;
}

declare module 'vue-router' {
  interface RouteMeta {
    title?: string;
    role?: UserRole | UserRole[];
    activeTab?: TabKey;
    hideTabBar?: boolean;
    requiresAuth?: boolean;
  }
}
