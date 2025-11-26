/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      fontFamily: {
         sans: ['"Inter"', '-apple-system', 'BlinkMacSystemFont', '"PingFang SC"', '"Microsoft YaHei"', 'sans-serif'],
         mono: ['"JetBrains Mono"', '"Fira Code"', 'monospace'],
      },
      colors: {
        'vibrant-bg': '#F5F7FA',      // 稍微再亮一点点的冷灰背景
        'vibrant-main': '#0F172A',    // 主文字
        'vibrant-muted': '#64748B',   // 次要文字
        'brand-blue': '#2563EB',      // 活力蓝
        'brand-indigo': '#4F46E5',    // 沉稳紫
        'functional-red': '#DC2626',  // 警示红
        'functional-green': '#059669',// 成功绿
        'functional-amber': '#D97706',// 警告黄
      },
      boxShadow: {
        // 更细腻的基础阴影，减少边框依赖
        'card': '0 0 0 1px rgba(0, 0, 0, 0.03), 0 2px 8px -2px rgba(0, 0, 0, 0.06)',
        // 悬浮时更柔和的扩散
        'card-hover': '0 0 0 1px rgba(0, 0, 0, 0.04), 0 8px 24px -6px rgba(0, 0, 0, 0.12)',
      }
    },
  },
  plugins: [],
}