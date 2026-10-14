import { beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick, ref } from 'vue'
import { flushPromises } from '@vue/test-utils'
import { useProductList } from '@/composables/useProductList'
import { shopApiMock } from '../mocks/api'
import { axiosErr } from '../helpers'

vi.mock('@/api/shop', async () => (await import('../mocks/api')).shopApiMock)

const page = (items: unknown[], p = 1, totalPages = 1) => ({ items, page: p, totalPages, totalItems: items.length })

describe('useProductList', () => {
  beforeEach(() => vi.resetAllMocks())

  it('loads immediately and exposes the page', async () => {
    shopApiMock.catalogApi.products.mockResolvedValue(page([{ id: 1 }], 1, 3))
    const list = useProductList(() => ({ page: 1, size: 15 }))
    expect(list.loading.value).toBe(true)
    await flushPromises()
    expect(list.products.value).toEqual([{ id: 1 }])
    expect(list.totalPages.value).toBe(3)
    expect(list.totalItems.value).toBe(1)
    expect(list.loading.value).toBe(false)
  })

  it('reloads when the query changes', async () => {
    shopApiMock.catalogApi.products.mockResolvedValue(page([]))
    const current = ref(1)
    useProductList(() => ({ page: current.value }))
    await flushPromises()
    current.value = 2
    await nextTick()
    await flushPromises()
    expect(shopApiMock.catalogApi.products).toHaveBeenLastCalledWith({ page: 2 })
    expect(shopApiMock.catalogApi.products).toHaveBeenCalledTimes(2)
  })

  it('flags a 404 as notFound', async () => {
    shopApiMock.catalogApi.products.mockRejectedValue(axiosErr(404))
    const list = useProductList(() => ({ category: 'nope' }))
    await flushPromises()
    expect(list.notFound.value).toBe(true)
    expect(list.error.value).toBeNull()
    expect(list.products.value).toEqual([])
  })

  it('reports other failures as an error message', async () => {
    shopApiMock.catalogApi.products.mockRejectedValue(axiosErr(500))
    const list = useProductList(() => ({}))
    await flushPromises()
    expect(list.error.value).toBe('Could not load products.')
    expect(list.notFound.value).toBe(false)
  })
})
