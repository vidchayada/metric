<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'

const links = [
  { to: '/admin/sales-history', label: 'Sales History' },
  { to: '/admin/orders', label: 'Orders' },
  { to: '/admin/products', label: 'Product' },
  { to: '/admin/categories', label: 'Categories' },
]

const auth = useAuthStore()
const cart = useCartStore()
const router = useRouter()
const open = ref(false)

function logout() {
  auth.logout()
  cart.reset()
  router.push('/login')
}
</script>

<template>
  <header class="sticky top-0 z-40 border-b border-line bg-white">
    <nav class="mx-auto flex h-16 max-w-7xl items-center gap-8 px-4 lg:px-8">
      <RouterLink to="/admin/dashboard" class="font-serif text-2xl">Girlette</RouterLink>
      <ul class="hidden flex-1 items-center gap-7 md:flex">
        <li v-for="l in links" :key="l.to">
          <RouterLink :to="l.to" class="pb-1 text-sm text-muted transition hover:text-ink" active-class="border-b-2 border-ink !text-ink">{{ l.label }}</RouterLink>
        </li>
      </ul>
      <div class="ml-auto flex items-center gap-4">
        <button class="text-sm text-muted hover:text-ink" title="Logout" @click="logout">Log out</button>
        <button class="text-xl md:hidden" aria-label="Open menu" @click="open = true">☰</button>
      </div>
    </nav>

    <div v-if="open" class="fixed inset-0 z-50 bg-ink/40 md:hidden" @click="open = false" />
    <aside :class="['fixed top-0 left-0 z-50 h-full w-64 bg-white p-6 shadow-xl transition-transform md:hidden', open ? 'translate-x-0' : '-translate-x-full']">
      <p class="mb-6 font-serif text-xl">Girlette</p>
      <ul class="space-y-4 text-sm">
        <li><RouterLink to="/admin/dashboard" @click="open = false">Dashboard</RouterLink></li>
        <li v-for="l in links" :key="l.to"><RouterLink :to="l.to" @click="open = false">{{ l.label }}</RouterLink></li>
      </ul>
    </aside>
  </header>
</template>
