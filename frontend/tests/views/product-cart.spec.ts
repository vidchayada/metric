import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import ProductDetailView from '@/views/ProductDetailView.vue'
import CartView from '@/views/CartView.vue'
import { useToastStore } from '@/stores/toast'
import { useCartStore } from '@/stores/cart'
import { shopApiMock } from '../mocks/api'
import { axiosErr, mountWith, settle } from '../helpers'
import type { Pinia } from 'pinia'

vi.mock('@/api/shop', async () => (await import('../mocks/api')).shopApiMock)

const detail = {
  id: 7,
  name: 'Linen Shirt',
  description: 'Soft and airy',
  price: 1250,
  categoryName: 'Tops',
  images: [
    { id: 1, url: '/uploads/a.jpg', primary: false, sortOrder: 2 },
    { id: 2, url: '/uploads/b.jpg', primary: true, sortOrder: 1 },
  ],
  attributes: [
    { id: 11, size: 'S', color: 'Beige', stockQty: 0 },
    { id: 12, size: 'M', color: 'Beige', stockQty: 2 },
    { id: 13, size: 'M', color: 'Black', stockQty: 5 },
    { id: 14, size: 'L', color: 'Black', stockQty: 1 },
  ],
}
const lastToast = (pinia: Pinia) => {
  const toasts = useToastStore(pinia).toasts
  return toasts[toasts.length - 1]
}
const sizeButton = (w: { findAll: (s: string) => { text: () => string }[] }, label: string) =>
  w.findAll('button[aria-pressed]').find((b) => b.text() === label) as never as {
    trigger: (e: string) => Promise<void>
    attributes: (k: string) => string | undefined
    classes: () => string[]
  }

beforeEach(() => {
  vi.resetAllMocks()
  shopApiMock.cartApi.count.mockResolvedValue(0)
})
afterEach(() => vi.unstubAllGlobals())

describe('ProductDetailView', () => {
  const open = (opts: { loggedIn?: boolean } = {}) =>
    mountWith(ProductDetailView, { route: '/product/7', ...opts })

  it('shows the product, uses the primary image first and lists sizes of the first colour', async () => {
    shopApiMock.catalogApi.product.mockResolvedValue(detail)
    const { wrapper } = await open()
    const text = wrapper.text()
    expect(text).toContain('Linen Shirt')
    expect(text).toContain('฿1,250')
    expect(text).toContain('Soft and airy')
    expect(text).toContain('Tops')
    expect(wrapper.find('img').attributes('src')).toBe('/uploads/b.jpg')
    expect(shopApiMock.catalogApi.product).toHaveBeenCalledWith(7)
    expect(sizeButton(wrapper, 'S').attributes('disabled')).toBeDefined() // out of stock for Beige
    expect(sizeButton(wrapper, 'M').attributes('disabled')).toBeUndefined()
  })

  it('switches the main image from the thumbnails', async () => {
    shopApiMock.catalogApi.product.mockResolvedValue(detail)
    const { wrapper } = await open()
    await wrapper.findAll('button[aria-label="View image"]')[0]!.trigger('click')
    expect(wrapper.find('img').attributes('src')).toBe('/uploads/a.jpg')
  })

  it('changing colour resets the size and updates availability', async () => {
    shopApiMock.catalogApi.product.mockResolvedValue(detail)
    const { wrapper } = await open()
    await sizeButton(wrapper, 'M').trigger('click')
    expect(wrapper.text()).toContain('2 items in stock')

    await wrapper.find('button[aria-label="Black"]').trigger('click')
    expect(wrapper.text()).not.toContain('items in stock')
    await sizeButton(wrapper, 'M').trigger('click')
    expect(wrapper.text()).toContain('5 items in stock')
  })

  it('renders colour swatches when the colour is a valid CSS colour', async () => {
    vi.stubGlobal('CSS', { supports: () => true })
    shopApiMock.catalogApi.product.mockResolvedValue(detail)
    const { wrapper } = await open()
    expect(wrapper.find('button[aria-label="Beige"]').attributes('style')).toContain('background-color')
  })

  it('asks for a size before adding to the cart', async () => {
    shopApiMock.catalogApi.product.mockResolvedValue(detail)
    const { wrapper, pinia } = await open({ loggedIn: true })
    await wrapper.findAll('button').find((b) => b.text() === 'Add to cart')!.trigger('click')
    expect(lastToast(pinia)?.message).toBe('Please select a size first!')
    expect(shopApiMock.cartApi.add).not.toHaveBeenCalled()
  })

  it('sends guests to the login page and remembers the product', async () => {
    shopApiMock.catalogApi.product.mockResolvedValue(detail)
    const { wrapper, router } = await open()
    await sizeButton(wrapper, 'M').trigger('click')
    await wrapper.findAll('button').find((b) => b.text() === 'Add to cart')!.trigger('click')
    await settle()
    expect(router.currentRoute.value.name).toBe('login')
    expect(router.currentRoute.value.query.returnTo).toBe('/product/7')
  })

  it('adds the chosen colour+size variant with the chosen quantity', async () => {
    shopApiMock.catalogApi.product.mockResolvedValue(detail)
    shopApiMock.cartApi.add.mockResolvedValue({})
    shopApiMock.cartApi.count.mockResolvedValue(2)
    const { wrapper, pinia } = await open({ loggedIn: true })
    await wrapper.find('button[aria-label="Black"]').trigger('click')
    await sizeButton(wrapper, 'M').trigger('click')
    await wrapper.find('button[aria-label="Increase"]').trigger('click')
    await wrapper.findAll('button').find((b) => b.text() === 'Add to cart')!.trigger('click')
    await settle()
    expect(shopApiMock.cartApi.add).toHaveBeenCalledWith(7, 13, 2)
    expect(lastToast(pinia)?.message).toBe('Added to cart!')
    expect(useCartStore(pinia).count).toBe(2)
  })

  it('limits the quantity to the stock and never goes below 1', async () => {
    shopApiMock.catalogApi.product.mockResolvedValue(detail)
    const { wrapper, pinia } = await open({ loggedIn: true })
    await sizeButton(wrapper, 'M').trigger('click') // Beige/M has 2 in stock
    const plus = wrapper.find('button[aria-label="Increase"]')
    const minus = wrapper.find('button[aria-label="Decrease"]')
    await minus.trigger('click')
    expect(wrapper.find('[data-testid="qty"]').text()).toBe('1')
    await plus.trigger('click')
    await plus.trigger('click')
    expect(wrapper.find('[data-testid="qty"]').text()).toBe('2')
    expect(lastToast(pinia)?.message).toContain('only 2 items')
  })

  it('shows an error toast when the cart request fails', async () => {
    shopApiMock.catalogApi.product.mockResolvedValue(detail)
    shopApiMock.cartApi.add.mockRejectedValue(axiosErr(400, { message: 'Not enough stock' }))
    const { wrapper, pinia } = await open({ loggedIn: true })
    await sizeButton(wrapper, 'M').trigger('click')
    await wrapper.findAll('button').find((b) => b.text() === 'Add to cart')!.trigger('click')
    await settle()
    expect(lastToast(pinia)?.message).toBe('Not enough stock')
  })

  it('handles products without sizes or colours', async () => {
    shopApiMock.catalogApi.product.mockResolvedValue({
      ...detail,
      images: [],
      description: null,
      categoryName: null,
      attributes: [{ id: 21, size: null, color: null, stockQty: 4 }],
    })
    shopApiMock.cartApi.add.mockResolvedValue({})
    const { wrapper } = await open({ loggedIn: true })
    expect(wrapper.text()).not.toContain('Size')
    expect(wrapper.text()).not.toContain('Description')
    await wrapper.findAll('button').find((b) => b.text() === 'Add to cart')!.trigger('click')
    await settle()
    expect(shopApiMock.cartApi.add).toHaveBeenCalledWith(7, 21, 1)
  })

  it('refuses to add when the variant is out of stock', async () => {
    shopApiMock.catalogApi.product.mockResolvedValue({
      ...detail,
      attributes: [{ id: 31, size: null, color: null, stockQty: 0 }],
    })
    const { wrapper, pinia } = await open({ loggedIn: true })
    await wrapper.findAll('button').find((b) => b.text() === 'Add to cart')!.trigger('click')
    expect(lastToast(pinia)?.message).toBe('Quantity exceeds available stock.')
    expect(shopApiMock.cartApi.add).not.toHaveBeenCalled()
  })

  it('shows not-found and error states', async () => {
    shopApiMock.catalogApi.product.mockRejectedValue(axiosErr(404))
    const missing = await open()
    expect(missing.wrapper.text()).toContain('Product not found.')

    shopApiMock.catalogApi.product.mockRejectedValue(axiosErr(500))
    const failed = await open()
    expect(failed.wrapper.text()).toContain('Something went wrong.')
  })

  it('goes back when the back button is pressed', async () => {
    shopApiMock.catalogApi.product.mockResolvedValue(detail)
    const { wrapper, router } = await open()
    const back = vi.spyOn(router, 'back').mockImplementation(() => {})
    await wrapper.find('button[aria-label="Go back"]').trigger('click')
    expect(back).toHaveBeenCalled()
  })
})

describe('CartView', () => {
  const items = [
    { id: 1, productId: 7, attributeId: 12, name: 'Linen Shirt', price: 500, quantity: 2, size: 'M', color: 'Beige', image: null },
    { id: 2, productId: 8, attributeId: 20, name: 'Wool Skirt', price: 300, quantity: 1, size: 'S', color: null, image: null },
  ]
  const open = () => mountWith(CartView, { route: '/cart', loggedIn: true })

  it('lists the items with the grand total', async () => {
    shopApiMock.cartApi.list.mockResolvedValue(items)
    const { wrapper } = await open()
    expect(wrapper.text()).toContain('Linen Shirt')
    expect(wrapper.text()).toContain('Wool Skirt')
    expect(wrapper.find('[data-testid="cart-total"]').text()).toBe('Total: ฿1,300')
  })

  it('shows the empty state', async () => {
    shopApiMock.cartApi.list.mockResolvedValue([])
    const { wrapper } = await open()
    expect(wrapper.text()).toContain('Your cart is empty.')
    expect(wrapper.text()).toContain('Continue Shopping')
  })

  it('changes quantity and removes items, reloading the cart each time', async () => {
    shopApiMock.cartApi.list.mockResolvedValue(items)
    shopApiMock.cartApi.setQuantity.mockResolvedValue({})
    shopApiMock.cartApi.remove.mockResolvedValue({})
    const { wrapper } = await open()

    await wrapper.find('[aria-label="Increase quantity"]').trigger('click')
    await settle()
    expect(shopApiMock.cartApi.setQuantity).toHaveBeenCalledWith(1, 3)

    await wrapper.find('[aria-label="Remove item"]').trigger('click')
    await settle()
    expect(shopApiMock.cartApi.remove).toHaveBeenCalledWith(1)
    expect(shopApiMock.cartApi.list).toHaveBeenCalledTimes(3)
  })

  it('checks out and shows the order number in a thank-you dialog', async () => {
    shopApiMock.cartApi.list.mockResolvedValue(items)
    shopApiMock.cartApi.checkout.mockResolvedValue({ orderNumber: 'ORD-ABC123' })
    const { wrapper } = await open()
    await wrapper.findAll('button').find((b) => b.text() === 'Checkout')!.trigger('click')
    await settle()
    expect(wrapper.text()).toContain('Thank you for your order!')
    expect(wrapper.text()).toContain('ORD-ABC123')
    expect(wrapper.text()).toContain('Your cart is empty.')

    await wrapper.findAll('button').find((b) => b.text() === 'Close')!.trigger('click')
    expect(wrapper.text()).not.toContain('Thank you for your order!')
  })

  it('reports a failed load as a toast', async () => {
    shopApiMock.cartApi.list.mockRejectedValue(axiosErr(500, { message: 'Cart unavailable' }))
    const { pinia } = await open()
    expect(lastToast(pinia)?.message).toBe('Cart unavailable')
  })
})
