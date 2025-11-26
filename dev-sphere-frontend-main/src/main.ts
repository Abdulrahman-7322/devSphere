import { createApp } from 'vue'
import { createPinia } from 'pinia' // 1. 引入 createPinia
import './style.css'
import App from './App.vue'
import router from './router'

const app = createApp(App)
const pinia = createPinia() // 2. 创建 Pinia 实例

// 3. 挂载 Pinia (注意：最好在 router 之前挂载)
app.use(pinia)
app.use(router)

app.mount('#app')