<script setup lang="ts">
import { formatPrice } from '@/utils/format'
import type { SalesRow } from '@/types'

defineProps<{ rows: SalesRow[]; periodLabel: string; emptyText: string }>()

/** 12500 -> "฿13K"; amounts under 1000 are shown in full. */
const amount = (v: number) => (v >= 1000 ? `฿${Math.round(v / 1000)}K` : formatPrice(v))
</script>

<template>
  <p v-if="!rows.length" class="py-10 text-center text-sm text-muted">{{ emptyText }}</p>
  <div v-else class="overflow-x-auto">
    <table class="w-full min-w-max text-sm">
      <thead>
      <tr class="border-b border-line text-left text-xs tracking-wider text-muted uppercase">
        <th class="py-2 pr-6">{{ periodLabel }}</th>
        <th v-for="r in rows" :key="r.label" class="px-3 py-2 text-right font-medium">{{ r.label }}</th>
      </tr>
      </thead>
      <tbody>
      <tr class="border-b border-line">
        <td class="py-3 pr-6 text-muted">Number of products</td>
        <td v-for="r in rows" :key="r.label" class="px-3 py-3 text-right">{{ r.totalQty || 0 }}</td>
      </tr>
      <tr>
        <td class="py-3 pr-6 text-muted">Sales amount</td>
        <td v-for="r in rows" :key="r.label" :class="['px-3 py-3 text-right', r.totalSales < 1000 ? 'text-red-600' : '']">{{ amount(r.totalSales) }}</td>
      </tr>
      </tbody>
    </table>
  </div>
</template>
