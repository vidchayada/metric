// import { ref, watch } from 'vue'
// import { catalogApi, type ProductQuery } from '@/api/shop'
// import { statusOf } from '@/api/client'
// import type { ProductSummary } from '@/types'
//
// /** Loads one page of products and reloads whenever the query changes. */
// export function useProductList(query: () => ProductQuery) {
//     const products = ref<ProductSummary[]>([])
//     const page = ref(1)
//     const totalPages = ref(0)
//     const totalItems = ref(0)
//     const loading = ref(false)
//     const notFound = ref(false)
//     const error = ref<string | null>(null)
//
//     async function load() {
//         loading.value = true
//         error.value = null
//         notFound.value = false
//         try {
//             const res = await catalogApi.products(query())
//             products.value = res.items
//             page.value = res.page
//             totalPages.value = res.totalPages
//             totalItems.value = res.totalItems
//         } catch (e) {
//             products.value = []
//             totalPages.value = 0
//             totalItems.value = 0
//             if (statusOf(e) === 404) notFound.value = true
//             else error.value = 'Could not load products.'
//         } finally {
//             loading.value = false
//         }
//     }
//
//     watch(query, load, { immediate: true, deep: true })
//
//     return { products, page, totalPages, totalItems, loading, notFound, error, reload: load }
// }

import { ref, watch } from 'vue'
import { catalogApi, type ProductQuery } from '@/api/shop'
import { statusOf } from '@/api/client'
import type { ProductSummary } from '@/types'

/** Loads one page of products and reloads whenever the query changes. */
export function useProductList(query: () => ProductQuery) {
    const products = ref<ProductSummary[]>([])
    const page = ref(1)
    const totalPages = ref(1)
    const totalItems = ref(0)
    const loading = ref(false)
    const notFound = ref(false)
    const error = ref<string | null>(null)

    async function load() {
        loading.value = true
        error.value = null
        notFound.value = false
        try {
            const res: any = await catalogApi.products(query())

            // เช็คว่า Backend ส่งมาเป็น Array ตรงๆ หรือห่อด้วย items / content
            if (Array.isArray(res)) {
                products.value = res
                totalItems.value = res.length
                totalPages.value = 1
                page.value = 1
            } else if (res && Array.isArray(res.items)) {
                products.value = res.items
                page.value = res.page ?? 1
                totalPages.value = res.totalPages ?? 1
                totalItems.value = res.totalItems ?? res.items.length
            } else if (res && Array.isArray(res.content)) { // เผื่อกรณีใช้ Spring Pageable
                products.value = res.content
                page.value = (res.number ?? 0) + 1
                totalPages.value = res.totalPages ?? 1
                totalItems.value = res.totalElements ?? res.content.length
            } else {
                products.value = []
            }
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