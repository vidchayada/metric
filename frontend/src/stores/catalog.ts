import { ref } from 'vue'
import { defineStore } from 'pinia'
import { catalogApi } from '@/api/shop'
import type { CategorySummary } from '@/types'

export const useCatalogStore = defineStore('catalog', () => {
    const categories = ref<CategorySummary[]>([])
    const loaded = ref(false)

    async function loadCategories(force = false) {
        if (loaded.value && !force) return
        try {
            categories.value = await catalogApi.categories()
            loaded.value = true
        } catch {
            categories.value = []
        }
    }

    return { categories, loaded, loadCategories }
})
