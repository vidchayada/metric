<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useCartStore } from '@/stores/cart'
import { useToastStore } from '@/stores/toast'
import { apiError } from '@/api/client'
import CartItemRow from '@/components/product/CartItemRow.vue'
import AppModal from '@/components/common/AppModal.vue'
import { formatPrice } from '@/utils/format'

const cart = useCartStore()
const toast = useToastStore()
const busy = ref(false)
const thanks = ref<string | null>(null)

async function run(action: () => Promise<unknown>) {
  busy.value = true
  try {
    await action()
  } catch (e) {
    toast.error(apiError(e))
  } finally {
    busy.value = false
  }
}

const changeQty = (id: number, qty: number) => run(() => cart.setQuantity(id, qty))
const remove = (id: number) => run(() => cart.remove(id))
const checkout = () => run(async () => (thanks.value = await cart.checkout('Default Shipping Address')))

onMounted(() => run(() => cart.load()))
</script>

<template>
  <div class="mx-auto max-w-5xl px-4 py-12 lg:px-8">
    <h1 class="page-title mb-8">Your Shopping Cart</h1>

    <p v-if="cart.loading && !cart.items.length" class="py-16 text-center text-sm text-muted">Loading…</p>

    <div v-else-if="!cart.items.length" class="py-16 text-center">
      <p class="text-muted">Your cart is empty.</p>
      <RouterLink to="/products" class="btn mt-6">Continue Shopping</RouterLink>
    </div>

    <template v-else>
      <div class="hidden grid-cols-[110px_1fr_140px_100px_110px_40px] gap-4 border-b border-ink pb-3 text-xs tracking-widest text-muted uppercase md:grid">
        <span>Product</span><span>Description</span><span>Quantity</span><span>Price</span><span>Total</span><span />
      </div>
      <ul>
        <CartItemRow v-for="item in cart.items" :key="item.id" :item="item" :busy="busy" @quantity="changeQty" @remove="remove" />
      </ul>
      <div class="mt-8 flex flex-col items-end gap-4">
        <p class="font-serif text-2xl" data-testid="cart-total">Total: {{ formatPrice(cart.total) }}</p>
        <button class="btn" :disabled="busy" @click="checkout">Checkout</button>
      </div>
    </template>

    <AppModal :open="!!thanks" @close="thanks = null">
      <div class="py-4 text-center">
        <p class="font-serif text-2xl">Thank you for your order!</p>
        <p v-if="thanks" class="mt-2 text-sm text-muted">Order {{ thanks }}</p>
      </div>
      <template #actions><button class="btn" @click="thanks = null">Close</button></template>
    </AppModal>
  </div>
</template>
