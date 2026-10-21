import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { IonicVue } from '@ionic/vue';

import App from './App.vue'
import router from './router'
/* Core CSS required for Ionic components to work properly */
import '@ionic/vue/css/core.css';
import '@ionic/vue/css/normalize.css';
import '@ionic/vue/css/structure.css';
import '@ionic/vue/css/typography.css';

/* Optional CSS utils that can be commented out */
import '@ionic/vue/css/padding.css';
import '@ionic/vue/css/float-elements.css';
import '@ionic/vue/css/text-alignment.css';
import '@ionic/vue/css/text-transformation.css';
import '@ionic/vue/css/flex-utils.css';
import '@ionic/vue/css/display.css';

/* Global Design System & Theme */
import './global.css';

const nativeFetch = window.fetch.bind(window);
const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL as string) || (import.meta.env.VITE_API_URL as string) || '';

window.fetch = (input: RequestInfo | URL, init: RequestInit = {}) => {
	const requestUrl = typeof input === 'string' ? input : input instanceof URL ? input.toString() : input.url;
	const token = localStorage.getItem('tattooflow_auth_token');
	if (apiBaseUrl && token && requestUrl.startsWith(apiBaseUrl) && !requestUrl.endsWith('/auth/login')) {
		const headers = new Headers(init.headers);
		if (!headers.has('Authorization')) headers.set('Authorization', `Bearer ${token}`);
		return nativeFetch(input, { ...init, headers });
	}
	return nativeFetch(input, init);
};

const app = createApp(App)

app.use(createPinia())
app.use(IonicVue)
app.use(router)

router.isReady().then(() => {
	app.mount('#app')
});
