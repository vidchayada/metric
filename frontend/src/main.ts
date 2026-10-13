import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import { router } from './router'
import { vFadeUp } from './directives/fadeUp'
import './assets/main.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.directive('fade-up', vFadeUp)
app.mount('#app')
