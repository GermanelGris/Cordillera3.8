import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      '/auth':     { target: 'http://localhost:8080', changeOrigin: true },
      '/datos':    { target: 'http://localhost:8080', changeOrigin: true },
      '/kpi':      { target: 'http://localhost:8080', changeOrigin: true },
      '/reportes': { target: 'http://localhost:8080', changeOrigin: true },
      '/usuarios': { target: 'http://localhost:8080', changeOrigin: true },
    }
  }
})
