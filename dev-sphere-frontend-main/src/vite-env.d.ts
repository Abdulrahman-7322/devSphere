/// <reference types="vite/client" />

// 这段代码告诉 TypeScript：所有以 .vue 结尾的文件都是 Vue 组件
declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

declare module 'vue3-puzzle-vcode' {
  import { DefineComponent } from 'vue'
  const Vcode: DefineComponent<{}, {}, any>
  export default Vcode
}