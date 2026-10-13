<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { catalogApi } from '@/api/shop'
import ProductGrid from '@/components/product/ProductGrid.vue'
import SafeImage from '@/components/common/SafeImage.vue'
import type { HomeData } from '@/types'

const data = ref<HomeData | null>(null)
const error = ref(false)
const slide = ref(0)
let timer: ReturnType<typeof setInterval> | undefined

/** "New Arrival" tile followed by every visible category. */
const tiles = computed(() => {
  if (!data.value) return []
  return [
    { key: 'new', name: 'New Arrival', to: '/products', image: data.value.newestImage },
    ...data.value.categories.map((c) => ({ key: c.slug, name: c.name, to: `/category/${c.slug}`, image: c.image })),
  ]
})

const slides = computed(() => {
  const out: (typeof tiles.value)[] = []
  for (let i = 0; i < tiles.value.length; i += 3) out.push(tiles.value.slice(i, i + 3))
  return out
})

function startAutoplay() {
  timer = setInterval(() => {
    if (slides.value.length > 1) slide.value = (slide.value + 1) % slides.value.length
  }, 3000)
}

onMounted(async () => {
  try {
    data.value = await catalogApi.home()
  } catch {
    error.value = true
  }
  startAutoplay()
})
onBeforeUnmount(() => clearInterval(timer))
</script>

<template>
  <div>
    <section class="relative">
      <SafeImage src="/images/hero.jpg" alt="Hero collection" class="h-[70vh] w-full object-cover" />
      <div class="absolute inset-0 flex items-end bg-gradient-to-t from-ink/50 to-transparent">
        <div class="mx-auto w-full max-w-7xl px-4 pb-14 lg:px-8">
          <h1 class="max-w-xl font-serif text-4xl leading-tight font-light text-white md:text-6xl">
            Elevate Your Style<br />Timeless Fashion, Sustainable Choices
          </h1>
          <RouterLink to="/products" class="btn mt-6 !bg-white !text-ink hover:!bg-blush-100">Shop Now</RouterLink>
        </div>
      </div>
    </section>

    <section class="mx-auto max-w-7xl px-4 pt-16 lg:px-8">
      <h2 v-fade-up class="page-title mb-8">Best Seller</h2>
      <p v-if="error" class="text-sm text-red-600">Could not load products.</p>
      <ProductGrid v-else v-fade-up :products="data?.bestSellers ?? []" :loading="!data" />
    </section>

    <section v-fade-up class="bg-blush-50 px-4 py-20 text-center">
      <div class="mx-auto max-w-2xl">
        <h2 class="font-serif text-3xl font-light">The Art of Fewer, Better Choices</h2>
        <p class="mt-4 leading-relaxed text-muted">
          Opting for quality over quantity means selecting timeless, durable, and responsibly made items. This approach
          simplifies our lives and fosters a deeper appreciation for our belongings, emphasising longevity and
          responsible production within a more sustainable and mindful lifestyle.
        </p>
      </div>
    </section>

    <section v-if="slides.length" v-fade-up class="mx-auto max-w-7xl px-4 py-16 lg:px-8">
      <div class="overflow-hidden">
        <div class="flex transition-transform duration-700" :style="{ transform: `translateX(-${slide * 100}%)` }">
          <div v-for="(group, i) in slides" :key="i" class="grid w-full shrink-0 grid-cols-3 gap-2">
            <RouterLink v-for="t in group" :key="t.key" :to="t.to" class="group relative block aspect-[3/4] overflow-hidden rounded-xl">
              <SafeImage :src="t.image" :alt="t.name" class="h-full w-full object-cover transition duration-500 group-hover:scale-105" />
              <span class="absolute inset-x-0 bottom-0 bg-gradient-to-t from-ink/60 to-transparent p-3 text-xs tracking-widest text-white uppercase md:text-sm">{{ t.name }}</span>
            </RouterLink>
          </div>
        </div>
      </div>
      <div v-if="slides.length > 1" class="mt-4 flex justify-center gap-2">
        <button
            v-for="(_, i) in slides"
            :key="i"
            :aria-label="`Go to slide ${i + 1}`"
            :class="['h-2 w-2 rounded-full transition', i === slide ? 'bg-ink' : 'bg-line']"
            @click="slide = i"
        />
      </div>
    </section>
  </div>
</template>
