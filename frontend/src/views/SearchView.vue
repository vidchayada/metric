<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useProductList } from '@/composables/useProductList'
import ProductGrid from '@/components/product/ProductGrid.vue'
import AppPagination from '@/components/common/AppPagination.vue'

const route = useRoute()
const router = useRouter()
const q = computed(() => (typeof route.query.q === 'string' ? route.query.q : ''))
const currentPage = computed(() => Math.max(1, Number(route.query.page) || 1))
const list = useProductList(() => ({ q: q.value, page: currentPage.value, size: 8 }))

const goTo = (page: number) => router.push({ query: { ...route.query, page } })
</script>

<template>
  <div class="mx-auto max-w-7xl px-4 py-12 lg:px-8">
    <h1 class="page-title">“{{ q }}”</h1>
    <p class="mt-2 mb-8 text-sm text-muted">
      <template v-if="!list.loading.value && list.totalItems.value === 0">No results found for “{{ q }}”</template>
      <template v-else-if="list.totalItems.value > 0">
        {{ list.totalItems.value }} result{{ list.totalItems.value !== 1 ? 's' : '' }} found for “{{ q }}”
      </template>
    </p>

    <div v-if="!list.loading.value && list.totalItems.value === 0" class="py-16 text-center">
      <p class="text-4xl text-blush-300">✿</p>
      <p class="mt-3 text-muted">We couldn't find anything matching your search.</p>
      <RouterLink to="/products" class="btn mt-6">Browse all products</RouterLink>
    </div>
    <template v-else>
      <ProductGrid :products="list.products.value" :loading="list.loading.value" />
      <AppPagination :page="list.page.value" :total-pages="list.totalPages.value" @change="goTo" />
    </template>
  </div>
</template>
