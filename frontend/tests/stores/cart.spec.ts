import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useCartStore } from '@/stores/cart'
import { cartApi } from '@/api/shop'
import type { CartItem } from '@/types'

vi.mock('@/api/shop', () => ({
  cartApi: {
    list: vi.fn(),
    count: vi.fn(),
    add: vi.fn(),
    setQuantity: vi.fn(),
    remove: vi.fn(),
    checkout: vi.fn(),
  },
}))

const item = (id: number, price: number, quantity: number): CartItem => ({
  id,
  productId: id,
  attributeId: id,
  name: `Item ${id}`,
  price,
  quantity,
  size: 'M',
  color: 'red',
  image: null,
})

describe('cart store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('loads items and derives count and total', async () => {
    vi.mocked(cartApi.list).mockResolvedValue([item(1, 100, 2), item(2, 250, 1)])
    const cart = useCartStore()
    await cart.load()
    expect(cart.count).toBe(3)
    expect(cart.total).toBe(450)
    expect(cart.loading).toBe(false)
  })

  it('refreshes the badge count after adding', async () => {
    vi.mocked(cartApi.add).mockResolvedValue({} as never)
    vi.mocked(cartApi.count).mockResolvedValue(5)
    const cart = useCartStore()
    await cart.add(1, 10, 2)
    expect(cartApi.add).toHaveBeenCalledWith(1, 10, 2)
    expect(cart.count).toBe(5)
  })

  it('falls back to 0 when the count request fails', async () => {
    vi.mocked(cartApi.count).mockRejectedValue(new Error('offline'))
    const cart = useCartStore()
    cart.count = 4
    await cart.loadCount()
    expect(cart.count).toBe(0)
  })

  it('updates the quantity and reloads', async () => {
    vi.mocked(cartApi.setQuantity).mockResolvedValue({} as never)
    vi.mocked(cartApi.list).mockResolvedValue([item(1, 100, 3)])
    const cart = useCartStore()
    await cart.setQuantity(1, 3)
    expect(cartApi.setQuantity).toHaveBeenCalledWith(1, 3)
    expect(cart.count).toBe(3)
  })

  it('removes the item when quantity drops below 1 (same as the old EJS behaviour)', async () => {
    vi.mocked(cartApi.remove).mockResolvedValue({} as never)
    vi.mocked(cartApi.list).mockResolvedValue([])
    const cart = useCartStore()
    await cart.setQuantity(1, 0)
    expect(cartApi.remove).toHaveBeenCalledWith(1)
    expect(cartApi.setQuantity).not.toHaveBeenCalled()
    expect(cart.items).toEqual([])
  })

  it('removes an item and reloads', async () => {
    vi.mocked(cartApi.remove).mockResolvedValue({} as never)
    vi.mocked(cartApi.list).mockResolvedValue([item(2, 50, 1)])
    const cart = useCartStore()
    await cart.remove(1)
    expect(cart.items).toHaveLength(1)
  })

  it('clears the cart after checkout and returns the order number', async () => {
    vi.mocked(cartApi.checkout).mockResolvedValue({ orderNumber: '#123456' })
    const cart = useCartStore()
    cart.items = [item(1, 100, 1)]
    cart.count = 1
    expect(await cart.checkout()).toBe('#123456')
    expect(cart.items).toEqual([])
    expect(cart.count).toBe(0)
  })

  it('reset empties state', () => {
    const cart = useCartStore()
    cart.items = [item(1, 1, 1)]
    cart.count = 1
    cart.reset()
    expect(cart.count).toBe(0)
    expect(cart.items).toEqual([])
  })
})
