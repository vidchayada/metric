<script setup lang="ts">
import SafeImage from '@/components/common/SafeImage.vue'
import { formatPrice } from '@/utils/format'
import type { ProductSummary } from '@/types'

defineProps<{ product: ProductSummary }>()
</script>

<template>
  <RouterLink :to="`/product/${product.id}`" class="group block">
    <div class="aspect-[3/4] overflow-hidden rounded-xl bg-blush-50">
      <SafeImage :src="product.image" :alt="product.name" class="h-full w-full object-cover transition duration-500 group-hover:scale-105" />
    </div>
    <div class="mt-3 space-y-0.5">
      <p class="truncate text-sm">{{ product.name }}</p>
      <p class="text-sm text-muted">{{ formatPrice(product.price) }}</p>
    </div>
  </RouterLink>
</template>
