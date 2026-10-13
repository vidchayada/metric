import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { cartApi } from '@/api/shop'
import type { CartItem } from '@/types'

export const useCartStore = defineStore('cart', () => {
    const items = ref<CartItem[]>([])
    const count = ref(0)
    const loading = ref(false)

    const total = computed(() => items.value.reduce((sum, i) => sum + i.price * i.quantity, 0))

    async function load() {
        loading.value = true
        try {
            items.value = await cartApi.list()
            count.value = items.value.reduce((sum, i) => sum + i.quantity, 0)
        } finally {
            loading.value = false
        }
    }

    async function loadCount() {
        try {
            count.value = await cartApi.count()
        } catch {
            count.value = 0
        }
    }

    async function add(productId: number, attributeId: number | null, quantity: number) {
        await cartApi.add(productId, attributeId, quantity)
        await loadCount()
    }

    async function setQuantity(id: number, quantity: number) {
        if (quantity < 1) await cartApi.remove(id)
        else await cartApi.setQuantity(id, quantity)
        await load()
    }

    async function remove(id: number) {
        await cartApi.remove(id)
        await load()
    }

    async function checkout() {
        const res = await cartApi.checkout()
        items.value = []
        count.value = 0
        return res.orderNumber
    }

    function reset() {
        items.value = []
        count.value = 0
    }

    return { items, count, loading, total, load, loadCount, add, setQuantity, remove, checkout, reset }
})
