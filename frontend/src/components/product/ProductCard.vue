<script setup lang="ts">
import { computed } from 'vue'
import SafeImage from '@/components/common/SafeImage.vue'
import { formatPrice } from '@/utils/format'

const props = defineProps<{ product: any }>()

const p = computed(() => {
  const raw = props.product ?? {}
  return {
    id: raw.productId ?? raw.id,
    name: raw.productName ?? raw.name ?? 'No name',
    price: Number(raw.basePrice ?? raw.price ?? 0),
    image: raw.imageUrls?.[0] ?? raw.image ?? raw.imageUrl ?? '',
  }
})
</script>

<template>
  <RouterLink :to="`/product/${p.id}`" class="group block">
    <div class="aspect-[3/4] overflow-hidden rounded-xl bg-blush-50">
      <SafeImage
          :src="p.image"
          :alt="p.name"
          class="h-full w-full object-cover transition duration-500 group-hover:scale-105"
      />
    </div>
    <div class="mt-3 space-y-0.5">
      <p class="truncate text-sm font-medium">{{ p.name }}</p>
      <p class="text-sm text-muted">{{ formatPrice(p.price) }}</p>
    </div>
  </RouterLink>
</template>