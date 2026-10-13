<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { adminApi } from '@/api/admin'
import { apiError } from '@/api/client'
import { useToastStore } from '@/stores/toast'
import SalesTable from '@/components/common/SalesTable.vue'
import SafeImage from '@/components/common/SafeImage.vue'
import { MONTHS, rankLabel } from '@/utils/format'
import type { SalesHistory } from '@/types'

const toast = useToastStore()
const now = new Date()
const years = Array.from({ length: 5 }, (_, i) => now.getFullYear() - i)

const dailyMonth = ref(now.getMonth() + 1)
const dailyYear = ref(now.getFullYear())
const monthlyYear = ref(now.getFullYear())
const data = ref<SalesHistory | null>(null)

async function load() {
  try {
    data.value = await adminApi.sales(dailyMonth.value, dailyYear.value, monthlyYear.value)
  } catch (e) {
    toast.error(apiError(e, 'Could not load sales history.'))
  }
}

watch([dailyMonth, dailyYear, monthlyYear], load)
onMounted(load)

const card = 'rounded-2xl bg-white p-5 ring-1 ring-line'
</script>

<template>
  <div class="grid gap-6 lg:grid-cols-[260px_1fr]">
    <aside class="space-y-3">
      <p class="field-label">Top Products</p>
      <p v-if="data && !data.topProducts.length" class="text-sm text-muted">No products yet</p>
      <div v-for="(p, i) in data?.topProducts ?? []" :key="p.name" :class="[card, 'flex items-center gap-3 !p-3']">
        <div class="h-16 w-12 shrink-0 overflow-hidden rounded-lg bg-blush-50">
          <SafeImage :src="p.imageUrl" :alt="p.name" class="h-full w-full object-cover" />
        </div>
        <div class="min-w-0 text-sm">
          <p class="text-xs text-blush-700">{{ rankLabel(i) }}</p>
          <p class="truncate">{{ p.name }}</p>
          <p class="text-xs text-muted">{{ p.totalOrders }} orders</p>
        </div>
      </div>
    </aside>

    <div class="min-w-0 space-y-6">
      <h1 class="page-title">Sales History</h1>

      <section :class="card">
        <div class="mb-4 flex flex-wrap items-center justify-between gap-3">
          <h2 class="font-medium">Total daily income</h2>
          <div class="flex gap-2">
            <select v-model.number="dailyMonth" class="field w-auto" aria-label="Month">
              <option v-for="(m, i) in MONTHS" :key="m" :value="i + 1">{{ m }}</option>
            </select>
            <select v-model.number="dailyYear" class="field w-auto" aria-label="Year (daily)">
              <option v-for="y in years" :key="y" :value="y">{{ y }}</option>
            </select>
          </div>
        </div>
        <SalesTable :rows="data?.daily ?? []" period-label="Date" empty-text="No sales data for this period." />
      </section>

      <section :class="card">
        <div class="mb-4 flex flex-wrap items-center justify-between gap-3">
          <h2 class="font-medium">Total monthly income</h2>
          <select v-model.number="monthlyYear" class="field w-auto" aria-label="Year (monthly)">
            <option v-for="y in years" :key="y" :value="y">{{ y }}</option>
          </select>
        </div>
        <SalesTable :rows="data?.monthly ?? []" period-label="Month" empty-text="No sales data for this year." />
      </section>
    </div>
  </div>
</template>
