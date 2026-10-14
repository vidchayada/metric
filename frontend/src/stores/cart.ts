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
            const res: any = await cartApi.list()
            const rawList = Array.isArray(res) ? res : (res?.items || [])

            const mapped = rawList.map((i: any) => ({
                id: i.cartItemId ?? i.id,
                productId: i.productId,
                name: i.productName ?? i.name,
                price: Number(i.unitPrice ?? i.price ?? 0),
                quantity: i.quantity ?? 1,
                image: i.imageUrl ?? i.image ?? '',
                size: i.size,
                color: i.color,
                subtotal: Number(
                    i.subtotal ?? ((i.unitPrice ?? i.price ?? 0) * i.quantity)
                ),
            }))

            items.value = mapped.sort((a: any, b: any) => a.id - b.id)

            count.value = items.value.reduce(
                (sum: number, i: any) => sum + i.quantity,
                0,
            )
        } catch (e) {
            console.error('Failed to load cart', e)
            items.value = []
            count.value = 0
            throw e
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

    async function checkout(shippingAddress?: string) {
        const res = await cartApi.checkout(shippingAddress)
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
