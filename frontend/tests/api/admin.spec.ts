import { afterEach, describe, expect, it, vi } from 'vitest'
import { http } from '@/api/client'
import { adminApi } from '@/api/admin'
import type { ProductPayload } from '@/types'

const reply = (data: unknown) => ({ data }) as never
const payload: ProductPayload = {
  name: 'Shirt',
  detail: 'Nice',
  price: 100,
  categoryId: 1,
  attributes: [{ size: 'M', color: 'Red', stockQty: 3 }],
}

describe('admin api', () => {
  afterEach(() => vi.restoreAllMocks())

  it('reads dashboard, orders (page size 8) and sales', async () => {
    const get = vi.spyOn(http, 'get').mockResolvedValue(reply('x'))
    await adminApi.dashboard()
    await adminApi.orders({ search: 'a', page: 2 })
    await adminApi.sales(3, 2026, 2025)
    expect(get.mock.calls).toEqual([
      ['/admin/dashboard'],
      ['/admin/orders', { params: { search: 'a', page: 2, size: 8 } }],
      ['/admin/sales', { params: { dailyMonth: 3, dailyYear: 2026, monthlyYear: 2025 } }],
    ])
  })

  it('updates an order status', async () => {
    const patch = vi.spyOn(http, 'patch').mockResolvedValue(reply(null))
    await adminApi.updateOrderStatus(5, 'SHIPPED')
    expect(patch).toHaveBeenCalledWith('/admin/orders/5/status', { status: 'SHIPPED' })
  })

  it('manages categories', async () => {
    const get = vi.spyOn(http, 'get').mockResolvedValue(reply([]))
    const post = vi.spyOn(http, 'post').mockResolvedValue(reply(null))
    const put = vi.spyOn(http, 'put').mockResolvedValue(reply(null))
    const patch = vi.spyOn(http, 'patch').mockResolvedValue(reply(null))
    const del = vi.spyOn(http, 'delete').mockResolvedValue(reply(null))

    await adminApi.categories()
    await adminApi.categories('show', 'top')
    await adminApi.createCategory('Tops', 'show')
    await adminApi.updateCategory(1, 'Tees', 'hidden')
    await adminApi.setCategoryVisibility(1, 'show')
    await adminApi.deleteCategory(1)

    expect(get.mock.calls[0]).toEqual(['/admin/categories', { params: { filter: '', search: '' } }])
    expect(get.mock.calls[1]).toEqual(['/admin/categories', { params: { filter: 'show', search: 'top' } }])
    expect(post).toHaveBeenCalledWith('/admin/categories', { name: 'Tops', visibility: 'show' })
    expect(put).toHaveBeenCalledWith('/admin/categories/1', { name: 'Tees', visibility: 'hidden' })
    expect(patch).toHaveBeenCalledWith('/admin/categories/1/visibility', { visibility: 'show' })
    expect(del).toHaveBeenCalledWith('/admin/categories/1')
  })

  it('lists products and omits an empty category filter', async () => {
    const get = vi.spyOn(http, 'get').mockResolvedValue(reply([]))
    await adminApi.products()
    await adminApi.products(4, 'shirt')
    expect(get.mock.calls[0]).toEqual(['/admin/products', { params: { category: undefined, search: '' } }])
    expect(get.mock.calls[1]).toEqual(['/admin/products', { params: { category: 4, search: 'shirt' } }])
  })

  it('sends products as multipart: JSON part "data" plus "images"', async () => {
    const post = vi.spyOn(http, 'post').mockResolvedValue(reply(null))
    const put = vi.spyOn(http, 'put').mockResolvedValue(reply(null))
    const del = vi.spyOn(http, 'delete').mockResolvedValue(reply(null))
    const img = new File(['x'], 'a.png', { type: 'image/png' })

    await adminApi.createProduct(payload, [img])
    await adminApi.updateProduct(3, payload, [])
    await adminApi.deleteProduct(3)

    const [url, body] = post.mock.calls[0] as [string, FormData]
    expect(url).toBe('/admin/products')
    expect(body.get('images')).toBe(img)
    const json = body.get('data') as Blob
    expect(json.type).toBe('application/json')
    const text = await new Promise<string>((resolve) => {
      const reader = new FileReader()
      reader.onload = () => resolve(String(reader.result))
      reader.readAsText(json)
    })
    expect(JSON.parse(text)).toEqual(payload)

    expect((put.mock.calls[0] as [string])[0]).toBe('/admin/products/3')
    expect(del).toHaveBeenCalledWith('/admin/products/3')
  })
})
