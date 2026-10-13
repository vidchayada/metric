<script setup lang="ts">
import SafeImage from '@/components/common/SafeImage.vue'
import { formatPrice } from '@/utils/format'
import type { CartItem } from '@/types'

defineProps<{ item: CartItem; busy?: boolean }>()
const emit = defineEmits<{ quantity: [id: number, quantity: number]; remove: [id: number] }>()
</script>

<template>
  <li class="grid grid-cols-[88px_1fr_auto] items-center gap-4 border-b border-line py-5 md:grid-cols-[110px_1fr_140px_100px_110px_40px]">
    <RouterLink :to="`/product/${item.productId}`" class="aspect-[3/4] overflow-hidden rounded-lg bg-blush-50">
      <SafeImage :src="item.image" :alt="item.name" class="h-full w-full object-cover" />
    </RouterLink>

    <div class="min-w-0">
      <p class="truncate text-sm font-medium">{{ item.name }}</p>
      <p class="mt-1 text-xs text-muted">
        <span v-if="item.color">Color: {{ item.color }}</span><br v-if="item.color && item.size" />
        <span v-if="item.size">Size: {{ item.size }}</span>
      </p>
      <p class="mt-2 text-sm md:hidden">{{ formatPrice(item.price * item.quantity) }}</p>
    </div>

    <div class="flex items-center justify-self-end rounded-full border border-line md:justify-self-start">
      <button class="h-8 w-8" :disabled="busy" aria-label="Decrease quantity" @click="emit('quantity', item.id, item.quantity - 1)">−</button>
      <span class="w-8 text-center text-sm" data-testid="qty">{{ item.quantity }}</span>
      <button class="h-8 w-8" :disabled="busy" aria-label="Increase quantity" @click="emit('quantity', item.id, item.quantity + 1)">+</button>
    </div>

    <p class="hidden text-sm md:block">{{ formatPrice(item.price) }}</p>
    <p class="hidden text-sm font-medium md:block">{{ formatPrice(item.price * item.quantity) }}</p>
    <button class="hidden text-muted transition hover:text-red-600 md:block" aria-label="Remove item" @click="emit('remove', item.id)">✕</button>
    <button class="col-span-3 text-left text-xs text-muted underline md:hidden" @click="emit('remove', item.id)">Remove</button>
  </li>
</template>
