<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useProductList } from '@/composables/useProductList'
import ProductGrid from '@/components/product/ProductGrid.vue'
import AppPagination from '@/components/common/AppPagination.vue'

const route = useRoute()
const router = useRouter()
const currentPage = computed(() => Math.max(1, Number(route.query.page) || 1))
const list = useProductList(() => ({ page: currentPage.value, size: 15 }))

const goTo = (page: number) => router.push({ query: { ...route.query, page } })
</script>

<template>
  <div class="mx-auto max-w-7xl px-4 py-12 lg:px-8">
    <h1 class="page-title mb-8">All products</h1>
    <p v-if="list.error.value" class="text-sm text-red-600">{{ list.error.value }}</p>
    <ProductGrid :products="list.products.value" :loading="list.loading.value" />
    <AppPagination :page="list.page.value" :total-pages="list.totalPages.value" @change="goTo" />
  </div>
</template>
