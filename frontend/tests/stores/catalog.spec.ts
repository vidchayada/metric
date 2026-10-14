import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useCatalogStore } from '@/stores/catalog'
import { shopApiMock } from '../mocks/api'

vi.mock('@/api/shop', async () => (await import('../mocks/api')).shopApiMock)

describe('catalog store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.resetAllMocks()
  })

  it('loads categories once and caches them', async () => {
    shopApiMock.catalogApi.categories.mockResolvedValue([{ id: 1, name: 'Tops', slug: 'tops', image: null }])
    const store = useCatalogStore()
    await store.loadCategories()
    await store.loadCategories()
    expect(store.categories).toHaveLength(1)
    expect(shopApiMock.catalogApi.categories).toHaveBeenCalledTimes(1)
  })

  it('reloads when forced', async () => {
    shopApiMock.catalogApi.categories.mockResolvedValue([])
    const store = useCatalogStore()
    await store.loadCategories()
    await store.loadCategories(true)
    expect(shopApiMock.catalogApi.categories).toHaveBeenCalledTimes(2)
  })

  it('falls back to an empty list on failure', async () => {
    shopApiMock.catalogApi.categories.mockRejectedValue(new Error('down'))
    const store = useCatalogStore()
    await store.loadCategories()
    expect(store.categories).toEqual([])
    expect(store.loaded).toBe(false)
  })
})
