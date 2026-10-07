<<<<<<< Updated upstream
import { createRouter, createWebHashHistory } from '@ionic/vue-router'

const router = createRouter({
  history: createWebHashHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', redirect: { name: 'Agenda' } },
    { path: '/agenda', name: 'Agenda', component: () => import('../views/Agenda.vue') },
    { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  ],
})

export default router
=======
import { createRouter, createWebHashHistory } from '@ionic/vue-router';
import type { RouteRecordRaw } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import type { UserRole } from '@/types/navigation';

export const routes: Array<RouteRecordRaw> = [
  {
    path: '/perfil',
    name: 'PerfilUsuario',
    component: () => import('../views/PerfilUsuario.vue'),
    meta: { title: 'Meu perfil', hideTabBar: true },
  },
  // Root Redirect (Handled dynamically in navigation guard)
  {
    path: '/',
    redirect: () => {
      try {
        const authStore = useAuthStore();
        return authStore.currentRole === 'admin' ? '/triagem' : '/agenda';
      } catch {
        return '/agenda';
      }
    },
  },

  // ==========================================
  // TATUADOR PRIMARY VIEWS (Tab Bar items)
  // ==========================================
  {
    path: '/agenda',
    name: 'Agenda',
    component: () => import('../views/Agenda.vue'),
    meta: {
      title: 'Agenda do Tatuador',
      role: 'tatuador',
      activeTab: 'agenda',
    },
  },
  {
    path: '/agenda2',
    redirect: '/agenda',
  },
  {
    path: '/clientes',
    name: 'Clientes',
    component: () => import('../views/Clientes.vue'),
    meta: {
      title: 'Catálogo de Clientes',
      role: 'tatuador',
      activeTab: 'clientes',
    },
  },
  {
    path: '/catalogo-clientes',
    redirect: '/clientes',
  },
  {
    path: '/upload-arte',
    name: 'UploadArte',
    component: () => import('../views/UploadArte.vue'),
    meta: {
      title: 'Curadoria & Upload de Arte',
      role: 'tatuador',
      activeTab: 'midia',
    },
  },
  {
    path: '/midia',
    redirect: '/upload-arte',
  },
  {
    path: '/upload-da-arte',
    redirect: '/upload-arte',
  },
  {
    path: '/curadoria-arte',
    redirect: '/upload-arte',
  },

  // ==========================================
  // ADMIN PRIMARY VIEWS (Tab Bar items)
  // ==========================================
  {
    path: '/triagem',
    name: 'Triagem',
    component: () => import('../views/TriagemClientes.vue'),
    meta: {
      title: 'Triagem de Clientes',
      role: 'admin',
      activeTab: 'triagem',
    },
  },
  {
    path: '/triagem-clientes',
    redirect: '/triagem',
  },
  {
    path: '/recepcao',
    redirect: '/triagem',
  },
  {
    path: '/fechamento',
    name: 'Fechamento',
    component: () => import('../views/Fechamento.vue'),
    meta: {
      title: 'Gestão Financeira & Auditoria',
      role: 'admin',
      activeTab: 'gestao',
    },
  },
  {
    path: '/gestao',
    redirect: '/fechamento',
  },
  {
    path: '/estoque',
    name: 'Estoque',
    component: () => import('../views/Estoque.vue'),
    meta: {
      title: 'Controle de Almoxarifado',
      role: 'admin',
      activeTab: 'estoque',
    },
  },
  {
    path: '/equipe-artistas',
    name: 'EquipeArtistas',
    component: () => import('../views/EquipeArtistas.vue'),
    meta: {
      title: 'Equipe & Controle de Acesso',
      role: 'admin',
      activeTab: 'equipe',
    },
  },
  {
    path: '/equipe',
    redirect: '/equipe-artistas',
  },
  {
    path: '/artistas',
    redirect: '/equipe-artistas',
  },

  // ==========================================
  // SECONDARY VIEWS (Navegação Interna - Sem ícone na Tab Bar)
  // ==========================================
  {
    path: '/dossie-cliente',
    name: 'DossieCliente',
    component: () => import('../views/DossieCliente.vue'),
    meta: {
      title: 'Dossiê do Cliente',
      activeTab: 'clientes',
    },
  },
  {
    path: '/dossie',
    redirect: '/dossie-cliente',
  },
  {
    path: '/convites',
    name: 'Convites',
    component: () => import('../views/Convites.vue'),
    meta: {
      title: 'Convites de Primeiro Acesso',
      activeTab: 'equipe',
    },
  },
  {
    path: '/definir-pin',
    name: 'DefinirPin',
    component: () => import('../views/DefinirPin.vue'),
    meta: {
      title: 'Definição de PIN Definitivo',
      hideTabBar: true,
    },
  },
  {
    path: '/definir-pin-ativacao',
    name: 'DefinirPinAtivacao',
    component: () => import('../views/DefinirPin.vue'),
    meta: {
      title: 'Ativação de PIN Provisório',
      hideTabBar: true,
    },
  },
  {
    path: '/pin-lock',
    name: 'PinLock',
    component: () => import('../views/PinLock.vue'),
    meta: {
      title: 'Autenticação PIN',
      hideTabBar: true,
    },
  },
  {
    path: '/pin',
    redirect: '/pin-lock',
  },
  {
    path: '/login',
    redirect: '/pin-lock',
  },

  // Sub-flows / Steps
  {
    path: '/legenda-curadoria',
    name: 'LegendaCuradoria',
    component: () => import('../views/LegendaCuradoria.vue'),
    meta: {
      title: 'Curadoria com IA',
      activeTab: 'midia',
      hideTabBar: true,
    },
  },
  {
    path: '/curadoria-legenda',
    redirect: '/legenda-curadoria',
  },
  {
    path: '/passo-2',
    redirect: '/legenda-curadoria',
  },
  {
    path: '/publicacao-distribuicao',
    name: 'PublicacaoDistribuicao',
    component: () => import('../views/PublicacaoDistribuicao.vue'),
    meta: {
      title: 'Publicação & Distribuição',
      activeTab: 'midia',
      hideTabBar: true,
    },
  },
  {
    path: '/agendar-publicacao',
    redirect: '/publicacao-distribuicao',
  },
  {
    path: '/passo-3',
    redirect: '/publicacao-distribuicao',
  },
  {
    path: '/cadastrar-insumo',
    name: 'CadastrarInsumo',
    component: () => import('../views/CadastrarInsumo.vue'),
    meta: {
      title: 'Cadastrar Insumo',
      activeTab: 'estoque',
    },
  },
  {
    path: '/performance-artistas',
    name: 'PerformanceArtistas',
    component: () => import('../views/PerformanceArtistas.vue'),
    meta: {
      title: 'Performance por Artista',
      activeTab: 'gestao',
    },
  },
  {
    path: '/termo',
    name: 'TermoAnamnese',
    component: () => import('../views/TermoAnamnese.vue'),
    meta: {
      title: 'Termo de Consentimento & Anamnese',
      activeTab: 'agenda',
    },
  },
  {
    path: '/termo-anamnese',
    redirect: '/termo',
  },
  {
    path: '/anamnese',
    redirect: '/termo',
  },
  {
    path: '/agendamento-sessao',
    name: 'AgendamentoSessao',
    component: () => import('../views/AgendamentoSessao.vue'),
    meta: {
      title: 'Reserva de Espaço & Agenda',
      activeTab: 'agenda',
    },
  },
  {
    path: '/reserva-espaco',
    redirect: '/agendamento-sessao',
  },
  {
    path: '/espaco-agenda',
    redirect: '/agendamento-sessao',
  },
  {
    path: '/etapa-1',
    redirect: '/agendamento-sessao',
  },
  {
    path: '/alocacao-materiais',
    name: 'AlocacaoMateriais',
    component: () => import('../views/AlocacaoMateriais.vue'),
    meta: {
      title: 'Kit Cirúrgico & Insumos',
      activeTab: 'agenda',
    },
  },
  {
    path: '/reserva-insumos',
    redirect: '/alocacao-materiais',
  },
  {
    path: '/kit-cirurgico',
    redirect: '/alocacao-materiais',
  },
  {
    path: '/etapa-2',
    redirect: '/alocacao-materiais',
  },
];

const router = createRouter({
  history: createWebHashHistory(import.meta.env.BASE_URL),
  routes,
});

// Navigation Guard: Route protection & dynamic redirection
router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore();
  const currentRole: UserRole = authStore.currentRole;

  const publicRoutes = ['/pin-lock', '/login', '/pin', '/definir-pin', '/definir-pin-ativacao'];
  if (!authStore.isAuthenticated && !publicRoutes.includes(to.path)) return next('/pin-lock');

  if (to.path === '/convites' && !authStore.isAdmin) return next('/agenda');

  // Handle root
  if (to.path === '/') {
    return next(currentRole === 'admin' ? '/triagem' : '/agenda');
  }

  // If route explicitly requires a role that does not match current profile,
  // allow smooth redirection to corresponding role home
  if (to.meta.role && to.meta.role !== currentRole) {
    if (currentRole === 'admin' && to.meta.role === 'tatuador') {
      // Trying to access tatuador-only route as admin
      return next(); // Still permit direct link access, or next('/triagem') if strict
    }
    if (currentRole === 'tatuador' && to.meta.role === 'admin') {
      // Trying to access admin-only route as tatuador
      return next(); // Still permit direct link access, or next('/agenda') if strict
    }
  }

  next();
});

export default router;
>>>>>>> Stashed changes
