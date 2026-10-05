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
