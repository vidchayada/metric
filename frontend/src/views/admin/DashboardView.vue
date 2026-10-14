<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { adminApi } from '@/api/admin'
import { useAuthStore } from '@/stores/auth'
import SafeImage from '@/components/common/SafeImage.vue'
import { formatCompact, rankLabel } from '@/utils/format'
import type { DashboardData } from '@/types'

const auth = useAuthStore()
const data = ref<DashboardData | null>(null)
const error = ref(false)
const openCategory = ref<number | null>(null)
const dismissed = ref<Set<string>>(new Set())

const popular = computed(() => (data.value?.itemSold ?? []).slice(0, 3))
const notifications = computed(() => (data.value?.outOfStock ?? []).filter((n) => !dismissed.value.has(n)))
const toggle = (id: number) => (openCategory.value = openCategory.value === id ? null : id)
const dismiss = (name: string) => (dismissed.value = new Set(dismissed.value).add(name))

onMounted(async () => {
  try {
    data.value = await adminApi.dashboard()
  } catch {
    error.value = true
  }
})

const card = 'rounded-2xl bg-white p-5 ring-1 ring-line'
const title = 'mb-3 text-xs font-medium tracking-widest text-muted uppercase'
</script>

<template>
  <p v-if="error" class="text-red-600">Could not load the dashboard.</p>
  <p v-else-if="!data" class="text-sm text-muted">Loading…</p>

  <div v-else class="grid gap-6 lg:grid-cols-[280px_1fr_280px]">
    <!-- left -->
    <aside class="space-y-6">
      <div :class="[card, 'flex items-center gap-4']">
        <SafeImage :src="auth.user?.profileImage" alt="Admin" class="h-14 w-14 rounded-full object-cover" />
        <div>
          <h2 class="font-medium">{{ auth.user?.fullName || 'Admin' }}</h2>
          <span class="text-xs text-muted">Admin</span>
        </div>
      </div>

      <section :class="card">
        <p :class="title">Earning</p>
        <p class="text-xs text-muted">Total Month Income</p>
        <p class="font-serif text-4xl" data-testid="month-income">{{ formatCompact(data.totalMonth) }}</p>
        <p class="mt-3 text-xs text-muted">Total Week Income</p>
        <p class="font-serif text-2xl">{{ data.totalWeek }}</p>
      </section>

      <section :class="card">
        <p :class="title">Item sold</p>
        <ul class="space-y-1.5 text-sm">
          <li v-for="i in data.itemSold" :key="i.name" class="flex justify-between"><span>{{ i.name }}</span><span>{{ i.qty }}</span></li>
        </ul>
      </section>

      <section :class="card">
        <p :class="title">Popular items</p>
        <p v-for="(i, idx) in popular" :key="i.name" class="text-sm"><span class="mr-2 text-blush-700">{{ rankLabel(idx) }}</span>{{ i.name }} : {{ i.qty }} orders</p>
      </section>
    </aside>

    <!-- center -->
    <div class="space-y-6">
      <section :class="card">
        <p :class="title">Product &amp; Categories</p>
        <div v-for="c in data.categories" :key="c.id" class="border-b border-line last:border-0">
          <button class="flex w-full items-center justify-between py-3 text-left text-sm" :aria-expanded="openCategory === c.id" @click="toggle(c.id)">
            <span>{{ c.name }}</span><span>{{ openCategory === c.id ? '⌃' : '⌄' }}</span>
          </button>
          <ul v-if="openCategory === c.id" class="space-y-1 pb-3 pl-4 text-sm text-muted">
            <li v-for="p in c.products" :key="p">{{ p }}</li>
            <li v-if="!c.products.length">No products</li>
          </ul>
        </div>
        <div class="mt-4 flex gap-10">
          <div><p class="text-xs text-muted">Total category</p><p class="font-serif text-3xl">{{ data.totalCategory }}</p></div>
          <div><p class="text-xs text-muted">Total product</p><p class="font-serif text-3xl">{{ data.totalProduct }}</p></div>
        </div>
      </section>

      <section :class="card">
        <p :class="title">Recent Orders</p>
        <div class="overflow-x-auto">
          <table class="w-full text-sm">
            <thead><tr class="text-left text-xs text-muted"><th class="py-2">Order number</th><th>Order date</th><th>Customer acc</th></tr></thead>
            <tbody>
            <tr v-for="o in data.recentOrders" :key="o.number" class="border-t border-line">
              <td class="py-2.5">{{ o.number }}</td><td>{{ o.date }}</td><td>{{ o.customer ?? '—' }}</td>
            </tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>

    <!-- right -->
    <aside class="space-y-3">
      <div v-for="n in notifications" :key="n" class="flex items-start justify-between gap-3 rounded-2xl bg-amber-50 p-4 ring-1 ring-amber-200" role="alert">
        <div>
          <p class="text-sm font-medium">{{ n }} out of stock!</p>
          <p class="text-xs text-muted">Please restock this item.</p>
        </div>
        <button class="text-lg leading-none text-muted" :aria-label="`Dismiss ${n}`" @click="dismiss(n)">×</button>
      </div>
    </aside>
  </div>
</template>
