<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { catalogApi } from '@/api/shop'
import { apiError, statusOf } from '@/api/client'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'
import { useToastStore } from '@/stores/toast'
import SafeImage from '@/components/common/SafeImage.vue'
import { formatPrice, isCssColor, sortSizes } from '@/utils/format'

interface ProductAttribute {
  attributeId?: number
  id?: number
  size?: string
  color?: string
  stockQty?: number
}

interface ProductImage {
  id: number | string
  url: string
}

interface ProductData {
  productId?: number
  id?: number
  productName?: string
  name?: string
  basePrice?: number
  price?: number
  description?: string
  categoryName?: string
  imageUrls?: string[]
  images?: Array<string | { id?: number | string; url?: string; imageUrl?: string }>
  attributes?: ProductAttribute[]
}

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const cart = useCartStore()
const toast = useToastStore()

const product = ref<ProductData | null>(null)
const status = ref<'loading' | 'ready' | 'missing' | 'error'>('loading')
const activeImage = ref<string | null>(null)
const color = ref<string | null>(null)
const size = ref<string | null>(null)
const qty = ref(1)
const adding = ref(false)

const images = computed<ProductImage[]>(() => {
  if (!product.value) return []
  if (Array.isArray(product.value.imageUrls)) {
    return product.value.imageUrls.map((url, idx) => ({ id: idx, url }))
  }
  if (Array.isArray(product.value.images)) {
    return product.value.images.map((img, idx) => ({
      id: typeof img === 'object' && img && 'id' in img && img.id ? img.id : idx,
      url: typeof img === 'string' ? img : (img?.url ?? (img as any)?.imageUrl ?? ''),
    }))
  }
  return []
})

const attributes = computed<ProductAttribute[]>(() => product.value?.attributes ?? [])

const colors = computed<string[]>(() => {
  const set = new Set<string>()
  for (const a of attributes.value) {
    if (a.color) set.add(a.color)
  }
  return Array.from(set)
})

const colorAttributes = computed<ProductAttribute[]>(() =>
    attributes.value.filter((a) => !color.value || a.color === color.value)
)

const sizes = computed<string[]>(() => {
  const set = new Set<string>()
  for (const a of colorAttributes.value) {
    if (a.size) set.add(a.size)
  }
  return sortSizes(Array.from(set))
})

const selected = computed<ProductAttribute | null>(() =>
    colorAttributes.value.find((a) => (size.value ? a.size === size.value : !a.size)) ?? null
)

const hasSizes = computed(() => sizes.value.length > 0)
const stock = computed(() => (hasSizes.value && !size.value ? 0 : (selected.value?.stockQty ?? 0)))
const stockFor = (s: string) => colorAttributes.value.find((a) => a.size === s)?.stockQty ?? 0

async function load(id: number) {
  status.value = 'loading'
  try {
    const p = (await catalogApi.product(id)) as ProductData
    product.value = p

    const imgList = Array.isArray(p.imageUrls) ? p.imageUrls : (p.images ?? [])
    const firstImg = imgList[0]
    activeImage.value = typeof firstImg === 'string' ? firstImg : (firstImg?.url ?? null)

    color.value = colors.value[0] ?? null
    size.value = null
    qty.value = 1
    status.value = 'ready'
  } catch (e) {
    status.value = statusOf(e) === 404 ? 'missing' : 'error'
  }
}

watch(() => route.params.id, (id) => id && load(Number(id)), { immediate: true })

function pickColor(c: string) {
  color.value = c
  size.value = null
  qty.value = 1
}

function pickSize(s: string) {
  size.value = s
  qty.value = 1
}

function changeQty(delta: number) {
  const next = qty.value + delta
  if (next < 1) return
  if (next > stock.value) return toast.error(`Sorry, only ${stock.value} items available for this size.`)
  qty.value = next
}

async function addToCart() {
  if (!product.value) return
  if (hasSizes.value && !size.value) return toast.error('Please select a size first!')
  if (!auth.isLoggedIn) return router.push({ name: 'login', query: { returnTo: route.fullPath } })
  if (!selected.value || qty.value > stock.value) return toast.error('Quantity exceeds available stock.')

  adding.value = true
  try {
    const prodId = product.value.productId ?? product.value.id ?? 0
    const attrId = selected.value.attributeId ?? selected.value.id ?? 0
    await cart.add(prodId, attrId, qty.value)
    toast.success('Added to cart!')
  } catch (e) {
    toast.error(apiError(e, 'Failed to add to cart.'))
  } finally {
    adding.value = false
  }
}
</script>

<template>
  <div class="mx-auto max-w-7xl px-4 py-10 lg:px-8">
    <button class="mb-6 text-sm text-muted hover:text-ink" aria-label="Go back" @click="router.back()">← Back</button>

    <p v-if="status === 'loading'" class="py-24 text-center text-sm text-muted">Loading…</p>
    <p v-else-if="status === 'missing'" class="py-24 text-center text-muted">Product not found.</p>
    <p v-else-if="status === 'error'" class="py-24 text-center text-red-600">Something went wrong.</p>

    <div v-else-if="product" class="grid gap-10 md:grid-cols-2">
      <!-- gallery -->
      <section>
        <div class="aspect-[3/4] overflow-hidden rounded-2xl bg-blush-50">
          <SafeImage :src="activeImage" :alt="(product.productName ?? product.name) || ''" class="h-full w-full object-cover" />
        </div>
        <div v-if="images.length > 1" class="mt-3 flex gap-2 overflow-x-auto">
          <button
              v-for="img in images"
              :key="img.id"
              :class="['h-20 w-16 shrink-0 overflow-hidden rounded-lg ring-2 transition', img.url === activeImage ? 'ring-ink' : 'ring-transparent']"
              aria-label="View image"
              @click="activeImage = img.url"
          >
            <SafeImage :src="img.url" :alt="(product.productName ?? product.name) || ''" class="h-full w-full object-cover" />
          </button>
        </div>
      </section>

      <!-- info -->
      <section class="space-y-6">
        <div>
          <p v-if="product.categoryName" class="text-xs tracking-widest text-muted uppercase">{{ product.categoryName }}</p>
          <h1 class="mt-1 font-serif text-4xl font-light">{{ product.productName ?? product.name }}</h1>
          <p class="mt-2 text-xl font-medium">{{ formatPrice(product.basePrice ?? product.price ?? 0) }}</p>
        </div>

        <div v-if="colors.length">
          <p class="field-label">Color <span class="ml-2 normal-case text-ink">{{ color }}</span></p>
          <div class="flex flex-wrap gap-2">
            <button
                v-for="c in colors"
                :key="c"
                :title="c"
                :aria-label="c"
                :aria-pressed="c === color"
                :style="isCssColor(c) ? { backgroundColor: c } : undefined"
                :class="[
                'h-8 rounded-full border transition',
                isCssColor(c) ? 'w-8' : 'px-3 text-xs',
                c === color ? 'ring-2 ring-ink ring-offset-2' : 'border-line',
              ]"
                @click="pickColor(c)"
            >
              <template v-if="!isCssColor(c)">{{ c }}</template>
            </button>
          </div>
        </div>

        <div v-if="hasSizes">
          <p class="field-label">Size</p>
          <div class="flex flex-wrap gap-2">
            <button
                v-for="s in sizes"
                :key="s"
                :disabled="stockFor(s) === 0"
                :aria-pressed="s === size"
                :class="[
                'h-10 min-w-12 rounded-full border px-3 text-sm transition disabled:cursor-not-allowed disabled:text-muted/50 disabled:line-through',
                s === size ? 'border-ink bg-ink text-white' : 'border-line hover:border-ink',
              ]"
                @click="pickSize(s)"
            >
              {{ s }}
            </button>
          </div>
          <p v-if="size" :class="['mt-2 text-xs', stock > 0 ? 'text-emerald-700' : 'text-red-600']">
            {{ stock > 0 ? `${stock} items in stock` : 'Out of stock' }}
          </p>
        </div>

        <div class="flex items-center gap-4">
          <div class="flex items-center rounded-full border border-line">
            <button class="h-10 w-10" aria-label="Decrease" @click="changeQty(-1)">−</button>
            <span class="w-8 text-center" data-testid="qty">{{ qty }}</span>
            <button class="h-10 w-10" aria-label="Increase" @click="changeQty(1)">+</button>
          </div>
          <button class="btn flex-1" :disabled="adding" @click="addToCart">Add to cart</button>
        </div>

        <div v-if="product.description" class="border-t border-line pt-6">
          <h3 class="field-label">Description</h3>
          <p class="text-sm leading-relaxed text-muted">{{ product.description }}</p>
        </div>
      </section>
    </div>
  </div>
</template>
