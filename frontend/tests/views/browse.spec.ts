import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import HomeView from '@/views/HomeView.vue'
import AllProductsView from '@/views/AllProductsView.vue'
import CategoryView from '@/views/CategoryView.vue'
import SearchView from '@/views/SearchView.vue'
import NotFoundView from '@/views/NotFoundView.vue'
import ContactView from '@/views/ContactView.vue'
import { shopApiMock } from '../mocks/api'
import { axiosErr, mountWith, settle } from '../helpers'

vi.mock('@/api/shop', async () => (await import('../mocks/api')).shopApiMock)

const product = (id: number) => ({ id, name: `Product ${id}`, price: id * 100, image: null })
const pageOf = (ids: number[], page = 1, totalPages = 1) => ({
  items: ids.map(product),
  page,
  totalPages,
  totalItems: ids.length,
})
const cats = [
  { id: 1, name: 'Tops', slug: 'tops', image: null },
  { id: 2, name: 'Dresses', slug: 'dresses', image: null },
  { id: 3, name: 'Skirts', slug: 'skirts', image: null },
]

beforeEach(() => {
  vi.resetAllMocks()
  shopApiMock.catalogApi.categories.mockResolvedValue(cats)
})
afterEach(() => vi.useRealTimers())

describe('HomeView', () => {
  const home = { bestSellers: [product(1), product(2)], categories: cats, newestImage: null }

  it('shows best sellers and the New Arrival + category tiles', async () => {
    shopApiMock.catalogApi.home.mockResolvedValue(home)
    const { wrapper } = await mountWith(HomeView)
    const text = wrapper.text()
    expect(text).toContain('Best Seller')
    expect(text).toContain('Product 1')
    expect(text).toContain('฿200')
    for (const label of ['New Arrival', 'Tops', 'Dresses', 'Skirts']) expect(text).toContain(label)
  })

  it('groups tiles three per slide and lets you pick a slide', async () => {
    shopApiMock.catalogApi.home.mockResolvedValue(home) // 1 + 3 tiles = 2 slides
    const { wrapper } = await mountWith(HomeView)
    const dots = wrapper.findAll('button[aria-label^="Go to slide"]')
    expect(dots).toHaveLength(2)
    await dots[1]!.trigger('click')
    expect(wrapper.find('.flex.transition-transform').attributes('style')).toContain('-100%')
  })

  it('autoplays to the next slide every 3 seconds', async () => {
    vi.useFakeTimers({ toFake: ['setInterval', 'clearInterval'] })
    shopApiMock.catalogApi.home.mockResolvedValue(home)
    const { wrapper } = await mountWith(HomeView)
    vi.advanceTimersByTime(3000)
    await wrapper.vm.$nextTick()
    expect(wrapper.find('.flex.transition-transform').attributes('style')).toContain('-100%')
    vi.advanceTimersByTime(3000)
    await wrapper.vm.$nextTick()
    expect(wrapper.find('.flex.transition-transform').attributes('style')).toContain('-0%')
    wrapper.unmount()
  })

  it('shows an error message when loading fails', async () => {
    shopApiMock.catalogApi.home.mockRejectedValue(axiosErr(500))
    const { wrapper } = await mountWith(HomeView)
    expect(wrapper.text()).toContain('Could not load products.')
  })

  it('hides the carousel when there is nothing to show', async () => {
    shopApiMock.catalogApi.home.mockResolvedValue({ bestSellers: [], categories: [], newestImage: null })
    const { wrapper } = await mountWith(HomeView)
    expect(wrapper.findAll('button[aria-label^="Go to slide"]')).toHaveLength(0)
  })
})

describe('AllProductsView', () => {
  it('requests page 1 with 15 items and renders the products', async () => {
    shopApiMock.catalogApi.products.mockResolvedValue(pageOf([1, 2, 3], 1, 3))
    const { wrapper } = await mountWith(AllProductsView, { route: '/products' })
    expect(shopApiMock.catalogApi.products).toHaveBeenCalledWith({ page: 1, size: 15 })
    expect(wrapper.text()).toContain('Product 3')
  })

  it('reads the page from the URL and navigates with the pager', async () => {
    shopApiMock.catalogApi.products.mockResolvedValue(pageOf([4], 2, 3))
    const { wrapper, router } = await mountWith(AllProductsView, { route: '/products?page=2' })
    expect(shopApiMock.catalogApi.products).toHaveBeenCalledWith({ page: 2, size: 15 })

    await wrapper.find('[aria-label="Next page"]').trigger('click')
    await settle()
    expect(router.currentRoute.value.query.page).toBe('3')
    expect(shopApiMock.catalogApi.products).toHaveBeenLastCalledWith({ page: 3, size: 15 })
  })

  it('shows the error text', async () => {
    shopApiMock.catalogApi.products.mockRejectedValue(axiosErr(500))
    const { wrapper } = await mountWith(AllProductsView, { route: '/products' })
    expect(wrapper.text()).toContain('Could not load products.')
  })
})

describe('CategoryView', () => {
  it('shows the category name and its products', async () => {
    shopApiMock.catalogApi.products.mockResolvedValue(pageOf([1]))
    const { wrapper } = await mountWith(CategoryView, { route: '/category/dresses' })
    expect(shopApiMock.catalogApi.products).toHaveBeenCalledWith({ category: 'dresses', page: 1, size: 15 })
    expect(wrapper.find('h1').text()).toBe('Dresses')
    expect(wrapper.text()).toContain('Product 1')
  })

  it('falls back to the slug when the category is not in the menu', async () => {
    shopApiMock.catalogApi.products.mockResolvedValue(pageOf([]))
    const { wrapper } = await mountWith(CategoryView, { route: '/category/unknown' })
    expect(wrapper.find('h1').text()).toBe('unknown')
    expect(wrapper.text()).toContain('No products found in this category.')
  })

  it('shows "Category not found" on a 404', async () => {
    shopApiMock.catalogApi.products.mockRejectedValue(axiosErr(404))
    const { wrapper } = await mountWith(CategoryView, { route: '/category/nope' })
    expect(wrapper.text()).toContain('Category not found.')
  })

  it('shows an error for other failures and pages with the pager', async () => {
    shopApiMock.catalogApi.products.mockRejectedValue(axiosErr(500))
    const failed = await mountWith(CategoryView, { route: '/category/tops' })
    expect(failed.wrapper.text()).toContain('Could not load products.')

    shopApiMock.catalogApi.products.mockResolvedValue(pageOf([1], 1, 2))
    const { wrapper, router } = await mountWith(CategoryView, { route: '/category/tops' })
    await wrapper.find('[aria-label="Next page"]').trigger('click')
    await settle()
    expect(router.currentRoute.value.query.page).toBe('2')
  })
})

describe('SearchView', () => {
  it('shows the result count for the query', async () => {
    shopApiMock.catalogApi.products.mockResolvedValue(pageOf([1, 2]))
    const { wrapper } = await mountWith(SearchView, { route: '/search?q=linen' })
    expect(shopApiMock.catalogApi.products).toHaveBeenCalledWith({ q: 'linen', page: 1, size: 8 })
    expect(wrapper.text()).toContain('2 results found for “linen”')
    expect(wrapper.text()).toContain('Product 2')
  })

  it('uses the singular for one result', async () => {
    shopApiMock.catalogApi.products.mockResolvedValue(pageOf([1]))
    const { wrapper } = await mountWith(SearchView, { route: '/search?q=linen' })
    expect(wrapper.text()).toContain('1 result found')
  })

  it('shows an empty state with a link back to all products', async () => {
    shopApiMock.catalogApi.products.mockResolvedValue(pageOf([]))
    const { wrapper } = await mountWith(SearchView, { route: '/search?q=zzz' })
    expect(wrapper.text()).toContain('No results found for “zzz”')
    expect(wrapper.text()).toContain('Browse all products')
  })

  it('handles a missing query and paginates', async () => {
    shopApiMock.catalogApi.products.mockResolvedValue(pageOf([1], 1, 2))
    const { wrapper, router } = await mountWith(SearchView, { route: '/search' })
    expect(shopApiMock.catalogApi.products).toHaveBeenCalledWith({ q: '', page: 1, size: 8 })
    await wrapper.find('[aria-label="Next page"]').trigger('click')
    await settle()
    expect(router.currentRoute.value.query.page).toBe('2')
  })
})

describe('NotFoundView and ContactView', () => {
  it('NotFoundView links home', async () => {
    const { wrapper } = await mountWith(NotFoundView)
    expect(wrapper.text()).toContain('Page not found')
    expect(wrapper.find('a').attributes('href')).toBe('/')
  })

  it('ContactView shows the map, address and newsletter form', async () => {
    const { wrapper } = await mountWith(ContactView)
    expect(wrapper.find('iframe').attributes('src')).toContain('maps.google.com')
    expect(wrapper.text()).toContain('Visit Us')
    expect(wrapper.text()).toContain('+66 82-146-1435')
    expect(wrapper.find('input[type="email"]').exists()).toBe(true)
  })
})
