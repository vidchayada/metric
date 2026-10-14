import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { DOMWrapper } from '@vue/test-utils'
import DashboardView from '@/views/admin/DashboardView.vue'
import OrdersView from '@/views/admin/OrdersView.vue'
import CategoriesView from '@/views/admin/CategoriesView.vue'
import ProductsView from '@/views/admin/ProductsView.vue'
import SalesHistoryView from '@/views/admin/SalesHistoryView.vue'
import { useToastStore } from '@/stores/toast'
import { useCatalogStore } from '@/stores/catalog'
import { adminApiMock, shopApiMock } from '../../mocks/api'
import { axiosErr, mountWith, settle } from '../../helpers'
import type { Pinia } from 'pinia'
import type { AdminCategory, AdminProduct } from '@/types'

vi.mock('@/api/shop', async () => (await import('../../mocks/api')).shopApiMock)
vi.mock('@/api/admin', async () => (await import('../../mocks/api')).adminApiMock)

const api = adminApiMock.adminApi
const lastToast = (pinia: Pinia) => useToastStore(pinia).toasts.at(-1)
const open = (view: Parameters<typeof mountWith>[0], route: string) => mountWith(view, { route, admin: true })

beforeEach(() => {
  vi.resetAllMocks()
  shopApiMock.catalogApi.categories.mockResolvedValue([])
  vi.stubGlobal('confirm', vi.fn(() => true))
})
afterEach(() => {
  vi.useRealTimers()
  vi.unstubAllGlobals()
})

describe('DashboardView', () => {
  const data = {
    totalMonth: 12500,
    totalWeek: 800,
    totalCategory: 2,
    totalProduct: 5,
    itemSold: [
      { name: 'Linen Shirt', qty: 9 },
      { name: 'Wool Skirt', qty: 6 },
      { name: 'Cotton Tee', qty: 4 },
      { name: 'Scarf', qty: 1 },
    ],
    categories: [
      { id: 1, name: 'Tops', products: ['Linen Shirt', 'Cotton Tee'] },
      { id: 2, name: 'Empty', products: [] },
    ],
    recentOrders: [{ number: '#1001', date: '2026-10-01', customer: null }],
    outOfStock: ['Scarf', 'Hat'],
  }

  it('shows earnings, popular items and recent orders', async () => {
    api.dashboard.mockResolvedValue(data)
    const { wrapper } = await open(DashboardView, '/admin/dashboard')
    expect(wrapper.find('[data-testid="month-income"]').text()).toBe('13K')
    const text = wrapper.text()
    expect(text).toContain('1st')
    expect(text).toContain('Linen Shirt : 9 orders')
    expect(text).not.toContain('Scarf : 1 orders') // only the top 3 are "popular"
    expect(text).toContain('#1001')
    expect(text).toContain('—') // customer unknown
  })

  it('expands and collapses a category', async () => {
    api.dashboard.mockResolvedValue(data)
    const { wrapper } = await open(DashboardView, '/admin/dashboard')
    const buttons = wrapper.findAll('button[aria-expanded]')
    await buttons[0]!.trigger('click')
    expect(wrapper.text()).toContain('Cotton Tee')
    await buttons[1]!.trigger('click')
    expect(wrapper.text()).toContain('No products')
    await buttons[1]!.trigger('click')
    expect(wrapper.text()).not.toContain('No products')
  })

  it('lists out-of-stock alerts and lets the admin dismiss them', async () => {
    api.dashboard.mockResolvedValue(data)
    const { wrapper } = await open(DashboardView, '/admin/dashboard')
    expect(wrapper.findAll('[role="alert"]')).toHaveLength(2)
    await wrapper.find('button[aria-label="Dismiss Scarf"]').trigger('click')
    expect(wrapper.findAll('[role="alert"]')).toHaveLength(1)
    expect(wrapper.text()).not.toContain('Scarf out of stock')
  })

  it('shows an error when loading fails', async () => {
    api.dashboard.mockRejectedValue(axiosErr(500))
    const { wrapper } = await open(DashboardView, '/admin/dashboard')
    expect(wrapper.text()).toContain('Could not load the dashboard.')
  })
})

describe('OrdersView', () => {
  const orders = [
    { id: 1, orderNumber: '#1001', orderDate: '2026-10-01', orderName: 'Linen Shirt', customer: 'mina', status: 'pending' },
    { id: 2, orderNumber: '#1002', orderDate: '2026-10-02', orderName: null, customer: null, status: 'shipped' },
  ]
  // fresh copies per call: the view mutates rows optimistically, and tests must not share that state
  const page = (items = orders, p = 1, totalPages = 1) => ({
    items: items.map((o) => ({ ...o })),
    page: p,
    totalPages,
    totalItems: items.length,
  })

  it('loads the first page of orders', async () => {
    api.orders.mockResolvedValue(page())
    const { wrapper } = await open(OrdersView, '/admin/orders')
    expect(api.orders).toHaveBeenCalledWith({ search: '', month: '', status: '', page: 1 })
    expect(wrapper.text()).toContain('#1001')
    expect(wrapper.text()).toContain('#1002')
  })

  it('shows an empty message', async () => {
    api.orders.mockResolvedValue(page([]))
    const { wrapper } = await open(OrdersView, '/admin/orders')
    expect(wrapper.text()).toContain('No orders found.')
  })

  it('filters by search text, month and status through the URL', async () => {
    api.orders.mockResolvedValue(page())
    const { wrapper, router } = await open(OrdersView, '/admin/orders')

    await wrapper.find('input[aria-label="Search orders"]').setValue('linen')
    await wrapper.find('form[role="search"]').trigger('submit')
    await settle()
    expect(router.currentRoute.value.query.search).toBe('linen')

    await wrapper.find('#month').setValue('3')
    await settle()
    expect(router.currentRoute.value.query.month).toBe('3')

    await wrapper.find('#status').setValue('shipped')
    await settle()
    expect(api.orders).toHaveBeenLastCalledWith({ search: 'linen', month: 3, status: 'shipped', page: 1 })

    await wrapper.find('#status').setValue('')
    await settle()
    expect(router.currentRoute.value.query.status).toBeUndefined()
  })

  it('paginates', async () => {
    api.orders.mockResolvedValue(page(orders, 1, 3))
    const { wrapper, router } = await open(OrdersView, '/admin/orders')
    await wrapper.find('[aria-label="Next page"]').trigger('click')
    await settle()
    expect(router.currentRoute.value.query.page).toBe('2')
    expect(api.orders).toHaveBeenLastCalledWith(expect.objectContaining({ page: 2 }))
  })

  it('updates the status of an order', async () => {
    api.orders.mockResolvedValue(page())
    api.updateOrderStatus.mockResolvedValue({})
    const { wrapper } = await open(OrdersView, '/admin/orders')
    await wrapper.find('select[aria-label="Status of #1001"]').setValue('completed')
    await settle()
    expect(api.updateOrderStatus).toHaveBeenCalledWith(1, 'completed')
    expect((wrapper.find('select[aria-label="Status of #1001"]').element as HTMLSelectElement).value).toBe('completed')
  })

  it('reverts the status and warns when the update fails', async () => {
    api.orders.mockResolvedValue(page())
    api.updateOrderStatus.mockRejectedValue(axiosErr(500, { message: 'Nope' }))
    const { wrapper, pinia } = await open(OrdersView, '/admin/orders')
    await wrapper.find('select[aria-label="Status of #1001"]').setValue('completed')
    await settle()
    expect((wrapper.find('select[aria-label="Status of #1001"]').element as HTMLSelectElement).value).toBe('pending')
    expect(lastToast(pinia)?.message).toBe('Nope')
  })

  it('reports a failed load', async () => {
    api.orders.mockRejectedValue(axiosErr(500))
    const { pinia } = await open(OrdersView, '/admin/orders')
    expect(lastToast(pinia)?.message).toBe('Could not load orders.')
  })
})

describe('CategoriesView', () => {
  const rows: AdminCategory[] = [
    { id: 1, name: 'Tops', slug: 'tops', visibility: 'show', productCount: 4 },
    { id: 2, name: 'Old Stock', slug: 'old-stock', visibility: 'hidden', productCount: 0 },
  ]

  it('lists categories with counts and visibility', async () => {
    api.categories.mockResolvedValue(rows)
    const { wrapper } = await open(CategoriesView, '/admin/categories')
    expect(api.categories).toHaveBeenCalledWith('', '')
    const text = wrapper.text()
    expect(text).toContain('#tops')
    expect(text).toContain('#old stock')
    expect(text).toContain('Hide')
    expect(text).toContain('Show')
  })

  it('shows an empty state', async () => {
    api.categories.mockResolvedValue([])
    const { wrapper } = await open(CategoriesView, '/admin/categories')
    expect(wrapper.text()).toContain('No categories found.')
  })

  it('filters by visibility immediately', async () => {
    api.categories.mockResolvedValue(rows)
    const { wrapper } = await open(CategoriesView, '/admin/categories')
    await wrapper.find('select[aria-label="Filter by visibility"]').setValue('hidden')
    await settle()
    expect(api.categories).toHaveBeenLastCalledWith('hidden', '')
  })

  it('debounces the search box', async () => {
    api.categories.mockResolvedValue(rows)
    const { wrapper } = await open(CategoriesView, '/admin/categories')
    vi.useFakeTimers({ toFake: ['setTimeout', 'clearTimeout'] })
    const box = wrapper.find('input[aria-label="Search categories"]')
    await box.setValue('t')
    await box.setValue('to')
    expect(api.categories).toHaveBeenCalledTimes(1)
    vi.advanceTimersByTime(400)
    await settle()
    expect(api.categories).toHaveBeenCalledTimes(2)
    expect(api.categories).toHaveBeenLastCalledWith('', 'to')
  })

  it('creates a category and refreshes the storefront menu', async () => {
    api.categories.mockResolvedValue(rows)
    api.createCategory.mockResolvedValue({})
    const { wrapper, pinia } = await open(CategoriesView, '/admin/categories')
    const catalog = useCatalogStore(pinia)
    await wrapper.find('button[aria-label="Add category"]').trigger('click')
    expect(wrapper.text()).toContain('New Category')
    await wrapper.find('#cat-name').setValue('  Sale ')
    await wrapper.find('#cat-vis').setValue('hidden')
    await wrapper.find('#category-form').trigger('submit')
    await settle()
    expect(api.createCategory).toHaveBeenCalledWith('Sale', 'hidden')
    expect(lastToast(pinia)?.message).toBe('Category created')
    expect(catalog.loaded).toBe(true) // forced reload ran
    expect(wrapper.text()).not.toContain('New Category')
  })

  it('does not save a blank name', async () => {
    api.categories.mockResolvedValue(rows)
    const { wrapper } = await open(CategoriesView, '/admin/categories')
    await wrapper.find('button[aria-label="Add category"]').trigger('click')
    await wrapper.find('#cat-name').setValue('   ')
    await wrapper.find('#category-form').trigger('submit')
    await settle()
    expect(api.createCategory).not.toHaveBeenCalled()
  })

  it('edits an existing category', async () => {
    api.categories.mockResolvedValue(rows)
    api.updateCategory.mockResolvedValue({})
    const { wrapper, pinia } = await open(CategoriesView, '/admin/categories')
    await wrapper.findAll('button').find((b) => b.text() === 'Edit')!.trigger('click')
    expect((wrapper.find('#cat-name').element as HTMLInputElement).value).toBe('Tops')
    await wrapper.find('#cat-name').setValue('Tees')
    await wrapper.find('#category-form').trigger('submit')
    await settle()
    expect(api.updateCategory).toHaveBeenCalledWith(1, 'Tees', 'show')
    expect(lastToast(pinia)?.message).toBe('Category updated')
  })

  it('toggles visibility and deletes with confirmation', async () => {
    api.categories.mockResolvedValue(rows)
    api.setCategoryVisibility.mockResolvedValue({})
    api.deleteCategory.mockResolvedValue({})
    const { wrapper } = await open(CategoriesView, '/admin/categories')

    await wrapper.findAll('button').find((b) => b.text() === 'Hide')!.trigger('click')
    await settle()
    expect(api.setCategoryVisibility).toHaveBeenCalledWith(1, 'hidden')
    await wrapper.findAll('button').find((b) => b.text() === 'Show')!.trigger('click')
    await settle()
    expect(api.setCategoryVisibility).toHaveBeenCalledWith(2, 'show')

    await wrapper.findAll('button').find((b) => b.text() === 'Delete')!.trigger('click')
    await settle()
    expect(api.deleteCategory).toHaveBeenCalledWith(1)
  })

  it('keeps the category when the delete is cancelled', async () => {
    api.categories.mockResolvedValue(rows)
    vi.stubGlobal('confirm', vi.fn(() => false))
    const { wrapper } = await open(CategoriesView, '/admin/categories')
    await wrapper.findAll('button').find((b) => b.text() === 'Delete')!.trigger('click')
    expect(api.deleteCategory).not.toHaveBeenCalled()
  })

  it('shows the server message when an action fails', async () => {
    api.categories.mockResolvedValue(rows)
    api.deleteCategory.mockRejectedValue(axiosErr(409, { message: 'Category still has products' }))
    const { wrapper, pinia } = await open(CategoriesView, '/admin/categories')
    await wrapper.findAll('button').find((b) => b.text() === 'Delete')!.trigger('click')
    await settle()
    expect(lastToast(pinia)?.message).toBe('Category still has products')
  })

  it('reports a failed load and closes the modal with Cancel', async () => {
    api.categories.mockRejectedValue(axiosErr(500))
    const { wrapper, pinia } = await open(CategoriesView, '/admin/categories')
    expect(lastToast(pinia)?.message).toBe('Could not load categories.')
    await wrapper.find('button[aria-label="Add category"]').trigger('click')
    await wrapper.findAll('button').find((b) => b.text() === 'Cancel')!.trigger('click')
    expect(wrapper.text()).not.toContain('New Category')
  })
})

describe('ProductsView', () => {
  const products: AdminProduct[] = [
    {
      id: 1,
      name: 'Linen Shirt',
      detail: 'Airy',
      price: 1250,
      categoryId: 1,
      categoryName: 'Tops',
      mainImage: null,
      totalStock: 7,
      attributes: [
        { size: 'M', color: 'Beige', stockQty: 3 },
        { size: 'L', color: 'Black', stockQty: 4 },
      ],
    },
    {
      id: 2,
      name: 'Scarf',
      detail: null,
      price: 200,
      categoryId: null,
      categoryName: null,
      mainImage: null,
      totalStock: 0,
      attributes: [],
    },
  ]
  const cats: AdminCategory[] = [{ id: 1, name: 'Tops', slug: 'tops', visibility: 'show', productCount: 1 }]

  beforeEach(() => {
    api.categories.mockResolvedValue(cats)
    api.products.mockResolvedValue(products)
  })

  it('lists products with stock and variants', async () => {
    const { wrapper } = await open(ProductsView, '/admin/products')
    expect(api.products).toHaveBeenCalledWith('', '')
    const text = wrapper.text()
    expect(text).toContain('Linen Shirt')
    expect(text).toContain('฿1,250')
    expect(text).toContain('7 items')
    expect(text).toContain('M / Beige, L / Black')
    expect(text).toContain('No detail')
  })

  it('shows an empty state', async () => {
    api.products.mockResolvedValue([])
    const { wrapper } = await open(ProductsView, '/admin/products')
    expect(wrapper.text()).toContain('Add your first product!')
  })

  it('filters by category and by search (Enter)', async () => {
    const { wrapper } = await open(ProductsView, '/admin/products')
    await wrapper.find('select[aria-label="Filter by category"]').setValue('1')
    await settle()
    expect(api.products).toHaveBeenLastCalledWith(1, '')
    const box = wrapper.find('input[aria-label="Search products"]')
    await box.setValue('shirt')
    await box.trigger('keyup.enter')
    await settle()
    expect(api.products).toHaveBeenLastCalledWith(1, 'shirt')
  })

  it('creates a product with its variations and images', async () => {
    api.createProduct.mockResolvedValue({})
    const { wrapper, pinia } = await open(ProductsView, '/admin/products')
    await wrapper.find('button[aria-label="Add product"]').trigger('click')
    expect(wrapper.text()).toContain('New Product')

    await wrapper.find('#p-cat').setValue('1')
    await wrapper.find('#p-name').setValue(' Denim Jacket ')
    await wrapper.find('#p-detail').setValue(' Sturdy ')
    await wrapper.find('#p-price').setValue('1990')
    const file = new File(['x'], 'jacket.png', { type: 'image/png' })
    const picker = wrapper.find('#p-images').element as HTMLInputElement
    Object.defineProperty(picker, 'files', { value: [file], configurable: true })
    await new DOMWrapper(picker).trigger('change')

    const row = wrapper.find('fieldset')
    await row.find('input[aria-label="Size"]').setValue('M')
    await row.find('input[aria-label="Color"]').setValue('Blue')
    await row.find('input[aria-label="Quantity"]').setValue('5')
    await wrapper.findAll('button').find((b) => b.text() === '+ Add variation')!.trigger('click')
    expect(wrapper.findAll('input[aria-label="Size"]')).toHaveLength(2) // second row left empty on purpose

    await wrapper.find('#product-form').trigger('submit')
    await settle()
    expect(api.createProduct).toHaveBeenCalledWith(
      {
        name: 'Denim Jacket',
        detail: 'Sturdy',
        price: 1990,
        categoryId: 1,
        attributes: [{ size: 'M', color: 'Blue', stockQty: 5 }],
      },
      [file],
    )
    expect(lastToast(pinia)?.message).toBe('Product created')
    expect(wrapper.text()).not.toContain('New Product')
  })

  it('requires a category', async () => {
    const { wrapper, pinia } = await open(ProductsView, '/admin/products')
    await wrapper.find('button[aria-label="Add product"]').trigger('click')
    await wrapper.find('#p-name').setValue('No category')
    await wrapper.find('#product-form').trigger('submit')
    await settle()
    expect(api.createProduct).not.toHaveBeenCalled()
    expect(lastToast(pinia)?.message).toBe('Please select a category.')
  })

  it('edits a product, keeping its current variations', async () => {
    api.updateProduct.mockResolvedValue({})
    const { wrapper, pinia } = await open(ProductsView, '/admin/products')
    await wrapper.findAll('button').find((b) => b.text() === 'Edit')!.trigger('click')
    expect((wrapper.find('#p-name').element as HTMLInputElement).value).toBe('Linen Shirt')
    expect(wrapper.findAll('input[aria-label="Size"]')).toHaveLength(2)

    await wrapper.find('#p-price').setValue('1300')
    await wrapper.find('#product-form').trigger('submit')
    await settle()
    expect(api.updateProduct).toHaveBeenCalledWith(
      1,
      expect.objectContaining({ name: 'Linen Shirt', price: 1300, categoryId: 1 }),
      [],
    )
    expect(lastToast(pinia)?.message).toBe('Product updated')
  })

  it('opens an uncategorised product with one blank variation row', async () => {
    const { wrapper } = await open(ProductsView, '/admin/products')
    await wrapper.findAll('button').filter((b) => b.text() === 'Edit')[1]!.trigger('click')
    expect((wrapper.find('#p-cat').element as HTMLSelectElement).value).toBe('')
    expect(wrapper.findAll('input[aria-label="Size"]')).toHaveLength(1)
    await wrapper.find('button[aria-label="Remove variation"]').trigger('click')
    expect(wrapper.findAll('input[aria-label="Size"]')).toHaveLength(1) // always keeps one row
  })

  it('shows the server message when saving fails', async () => {
    api.createProduct.mockRejectedValue(axiosErr(400, { message: 'Price must be positive' }))
    const { wrapper, pinia } = await open(ProductsView, '/admin/products')
    await wrapper.find('button[aria-label="Add product"]').trigger('click')
    await wrapper.find('#p-cat').setValue('1')
    await wrapper.find('#p-name').setValue('X')
    await wrapper.find('#product-form').trigger('submit')
    await settle()
    expect(lastToast(pinia)?.message).toBe('Price must be positive')
  })

  it('deletes after confirmation, and not when cancelled', async () => {
    api.deleteProduct.mockResolvedValue({})
    const { wrapper } = await open(ProductsView, '/admin/products')
    vi.stubGlobal('confirm', vi.fn(() => false))
    await wrapper.findAll('button').find((b) => b.text() === 'Delete')!.trigger('click')
    expect(api.deleteProduct).not.toHaveBeenCalled()

    vi.stubGlobal('confirm', vi.fn(() => true))
    await wrapper.findAll('button').find((b) => b.text() === 'Delete')!.trigger('click')
    await settle()
    expect(api.deleteProduct).toHaveBeenCalledWith(1)
  })

  it('reports delete and load failures', async () => {
    api.deleteProduct.mockRejectedValue(axiosErr(500, { message: 'Cannot delete' }))
    const { wrapper, pinia } = await open(ProductsView, '/admin/products')
    await wrapper.findAll('button').find((b) => b.text() === 'Delete')!.trigger('click')
    await settle()
    expect(lastToast(pinia)?.message).toBe('Cannot delete')

    api.products.mockRejectedValue(axiosErr(500))
    api.categories.mockRejectedValue(axiosErr(500))
    const failed = await open(ProductsView, '/admin/products')
    expect(failed.pinia && useToastStore(failed.pinia).toasts.map((t) => t.message)).toContain('Could not load products.')
  })

  it('closes the dialog with Cancel', async () => {
    const { wrapper } = await open(ProductsView, '/admin/products')
    await wrapper.find('button[aria-label="Add product"]').trigger('click')
    await wrapper.findAll('button').find((b) => b.text() === 'Cancel')!.trigger('click')
    expect(wrapper.text()).not.toContain('New Product')
  })
})

describe('SalesHistoryView', () => {
  const data = {
    topProducts: [
      { name: 'Linen Shirt', imageUrl: null, totalOrders: 9 },
      { name: 'Wool Skirt', imageUrl: '/uploads/s.jpg', totalOrders: 6 },
    ],
    daily: [
      { label: '01', totalQty: 2, totalSales: 2500 },
      { label: '02', totalQty: 1, totalSales: 300 },
    ],
    monthly: [{ label: 'Jan', totalQty: 10, totalSales: 12000 }],
  }

  it('loads this month and year and shows ranked top products', async () => {
    api.sales.mockResolvedValue(data)
    const { wrapper } = await open(SalesHistoryView, '/admin/sales-history')
    const now = new Date()
    expect(api.sales).toHaveBeenCalledWith(now.getMonth() + 1, now.getFullYear(), now.getFullYear())
    const text = wrapper.text()
    expect(text).toContain('1st')
    expect(text).toContain('Linen Shirt')
    expect(text).toContain('9 orders')
    expect(text).toContain('฿3K') // 2500 rounds to 3K
    expect(text).toContain('฿300')
    expect(text).toContain('฿12K')
  })

  it('reloads when the month or either year changes', async () => {
    api.sales.mockResolvedValue(data)
    const { wrapper } = await open(SalesHistoryView, '/admin/sales-history')
    const now = new Date()
    const year = now.getFullYear()

    await wrapper.find('select[aria-label="Month"]').setValue('1')
    await settle()
    expect(api.sales).toHaveBeenLastCalledWith(1, year, year)

    await wrapper.find('select[aria-label="Year (daily)"]').setValue(String(year - 1))
    await settle()
    expect(api.sales).toHaveBeenLastCalledWith(1, year - 1, year)

    await wrapper.find('select[aria-label="Year (monthly)"]').setValue(String(year - 2))
    await settle()
    expect(api.sales).toHaveBeenLastCalledWith(1, year - 1, year - 2)
  })

  it('shows empty messages and "No products yet"', async () => {
    api.sales.mockResolvedValue({ topProducts: [], daily: [], monthly: [] })
    const { wrapper } = await open(SalesHistoryView, '/admin/sales-history')
    const text = wrapper.text()
    expect(text).toContain('No products yet')
    expect(text).toContain('No sales data for this period.')
    expect(text).toContain('No sales data for this year.')
  })

  it('reports a failed load', async () => {
    api.sales.mockRejectedValue(axiosErr(500))
    const { pinia } = await open(SalesHistoryView, '/admin/sales-history')
    expect(lastToast(pinia)?.message).toBe('Could not load sales history.')
  })
})
