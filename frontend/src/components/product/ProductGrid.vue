<script setup lang="ts">
import ProductCard from './ProductCard.vue'
import type { ProductSummary } from '@/types'

defineProps<{ products: ProductSummary[]; loading?: boolean; emptyText?: string }>()
</script>

<template>
  <div v-if="loading && !products.length" class="py-24 text-center text-sm text-muted">Loading…</div>
  <p v-else-if="!products.length" class="py-24 text-center text-sm text-muted">{{ emptyText ?? 'No products found.' }}</p>
  <div v-else class="grid grid-cols-2 gap-x-4 gap-y-8 md:grid-cols-4">
    <ProductCard v-for="p in products" :key="p.id" :product="p" />
  </div>
</template>
