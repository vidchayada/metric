<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'
import { useCatalogStore } from '@/stores/catalog'

const auth = useAuthStore()
const cart = useCartStore()
const catalog = useCatalogStore()
const route = useRoute()
const router = useRouter()

const scrolled = ref(false)
const menuOpen = ref(false)
const categoriesOpen = ref(false)
const query = ref(typeof route.query.q === 'string' ? route.query.q : '')

const onScroll = () => (scrolled.value = window.scrollY > 10)

function search() {
  const q = query.value.trim()
  if (!q) return
  menuOpen.value = false
  router.push({ path: '/search', query: { q } })
}

onMounted(() => {
  window.addEventListener('scroll', onScroll, { passive: true })
  catalog.loadCategories()
  if (auth.isLoggedIn) cart.loadCount()
})
onBeforeUnmount(() => window.removeEventListener('scroll', onScroll))

watch(
    () => route.fullPath,
    () => {
      menuOpen.value = false
      categoriesOpen.value = false
    },
)
watch(
    () => auth.isLoggedIn,
    (loggedIn) => (loggedIn ? cart.loadCount() : cart.reset()),
)

const link = 'text-sm tracking-wide text-ink/80 transition hover:text-blush-700'
</script>

<template>
  <header :class="['sticky top-0 z-40 bg-white/90 backdrop-blur transition', scrolled ? 'shadow-sm' : '']">
    <nav class="mx-auto flex h-16 max-w-7xl items-center gap-8 px-4 lg:px-8">
      <RouterLink to="/" class="font-serif text-2xl tracking-wide">Girlette <span class="text-blush-500">✿</span></RouterLink>

      <ul class="hidden flex-1 items-center gap-7 lg:flex">
        <li><RouterLink to="/products" :class="link">All products</RouterLink></li>
        <li class="relative" @mouseenter="categoriesOpen = true" @mouseleave="categoriesOpen = false">
          <button :class="link" aria-haspopup="true" :aria-expanded="categoriesOpen" @click="categoriesOpen = !categoriesOpen">Categories ▾</button>
          <ul v-if="categoriesOpen" class="absolute top-full left-0 min-w-44 rounded-xl border border-line bg-white py-2 shadow-lg">
            <li v-for="c in catalog.categories" :key="c.id">
              <RouterLink :to="`/category/${c.slug}`" class="block px-4 py-2 text-sm hover:bg-blush-50">{{ c.name }}</RouterLink>
            </li>
          </ul>
        </li>
        <li><RouterLink to="/contact" :class="link">Contact</RouterLink></li>
      </ul>

      <form class="ml-auto hidden items-center rounded-full border border-line px-4 lg:flex" role="search" @submit.prevent="search">
        <input v-model="query" type="search" placeholder="Search" aria-label="Search products" class="w-44 bg-transparent py-1.5 text-sm outline-none" />
        <button type="submit" aria-label="Submit search" class="text-muted hover:text-ink">⌕</button>
      </form>

      <div class="ml-auto flex items-center gap-4 lg:ml-0">
        <RouterLink to="/cart" class="relative text-xl" aria-label="Cart">
          🛍
          <span v-if="cart.count > 0" data-testid="cart-count" class="absolute -top-2 -right-3 flex h-5 min-w-5 items-center justify-center rounded-full bg-blush-700 px-1 text-[10px] text-white">{{ cart.count }}</span>
        </RouterLink>
        <RouterLink :to="auth.isLoggedIn ? '/profile' : '/login'" class="text-xl" aria-label="Account">👤</RouterLink>
        <button class="text-xl lg:hidden" aria-label="Open menu" @click="menuOpen = true">☰</button>
      </div>
    </nav>

    <!-- mobile drawer -->
    <div v-if="menuOpen" class="fixed inset-0 z-50 bg-ink/40 lg:hidden" @click="menuOpen = false" />
    <aside :class="['fixed top-0 right-0 z-50 h-full w-72 bg-white p-6 shadow-xl transition-transform lg:hidden', menuOpen ? 'translate-x-0' : 'translate-x-full']">
      <div class="mb-6 flex items-center justify-between">
        <span class="font-serif text-xl">Girlette ✿</span>
        <button aria-label="Close menu" @click="menuOpen = false">✕</button>
      </div>
      <form class="mb-6 flex items-center rounded-full border border-line px-4" role="search" @submit.prevent="search">
        <input v-model="query" type="search" placeholder="Search" aria-label="Search products" class="w-full bg-transparent py-2 text-sm outline-none" />
        <button type="submit" aria-label="Submit search">⌕</button>
      </form>
      <ul class="space-y-4 text-sm">
        <li><RouterLink to="/products">All products</RouterLink></li>
        <li>
          <button class="flex w-full justify-between" @click="categoriesOpen = !categoriesOpen">Categories <span>{{ categoriesOpen ? '⌄' : '›' }}</span></button>
          <ul v-if="categoriesOpen" class="mt-3 space-y-3 border-l border-line pl-4 text-muted">
            <li v-for="c in catalog.categories" :key="c.id"><RouterLink :to="`/category/${c.slug}`">{{ c.name }}</RouterLink></li>
          </ul>
        </li>
        <li><RouterLink to="/contact">Contact</RouterLink></li>
      </ul>
    </aside>
  </header>
</template>
