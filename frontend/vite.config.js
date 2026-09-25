import react from '@vitejs/plugin-react';
import { defineConfig } from 'vite';

const target = process.env.VITE_PROXY_TARGET || 'http://localhost:8080'

// https://vite.dev/config/
export default defineConfig({
  server: {
    watch: { usePolling: true},
    proxy: {
      '/api': target
    },
  },
  plugins: [react()],
});
