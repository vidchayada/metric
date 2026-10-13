import { ref, watch } from 'vue'
import { catalogApi, type ProductQuery } from '@/api/shop'
import { statusOf } from '@/api/client'
import type { ProductSummary } from '@/types'

/** Loads one page of products and reloads whenever the query changes. */
export function useProductList(query: () => ProductQuery) {
    const products = ref<ProductSummary[]>([])
    const page = ref(1)
    const totalPages = ref(0)
    const totalItems = ref(0)
    const loading = ref(false)
    const notFound = ref(false)
    const error = ref<string | null>(null)

    async function load() {
        loading.value = true
        error.value = null
        notFound.value = false
        try {
            const res = await catalogApi.products(query())
            products.value = res.items
            page.value = res.page
            totalPages.value = res.totalPages
            totalItems.value = res.totalItems
        } catch (e) {
            products.value = []
            totalPages.value = 0
            totalItems.value = 0
            if (statusOf(e) === 404) notFound.value = true
            else error.value = 'Could not load products.'
        } finally {
            loading.value = false
        }
    }

    watch(query, load, { immediate: true, deep: true })

    return { products, page, totalPages, totalItems, loading, notFound, error, reload: load }
}
