<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useProductList } from '@/composables/useProductList'
import { useCatalogStore } from '@/stores/catalog'
import ProductGrid from '@/components/product/ProductGrid.vue'
import AppPagination from '@/components/common/AppPagination.vue'

const route = useRoute()
const router = useRouter()
const catalog = useCatalogStore()
catalog.loadCategories()

const slug = computed(() => String(route.params.slug))
const currentPage = computed(() => Math.max(1, Number(route.query.page) || 1))
const list = useProductList(() => ({ category: slug.value, page: currentPage.value, size: 15 }))

const title = computed(() => catalog.categories.find((c) => c.slug === slug.value)?.name ?? slug.value)
const goTo = (page: number) => router.push({ query: { ...route.query, page } })
</script>

<template>
  <div class="mx-auto max-w-7xl px-4 py-12 lg:px-8">
    <p v-if="list.notFound.value" class="py-24 text-center text-muted">Category not found.</p>
    <template v-else>
      <h1 class="page-title mb-8 uppercase">{{ title }}</h1>
      <p v-if="list.error.value" class="text-sm text-red-600">{{ list.error.value }}</p>
      <ProductGrid :products="list.products.value" :loading="list.loading.value" empty-text="No products found in this category." />
      <AppPagination :page="list.page.value" :total-pages="list.totalPages.value" @change="goTo" />
    </template>
  </div>
</template>
