import { vi } from 'vitest'

/** Every network function replaced by a vi.fn(); tests set return values per case. */
export const shopApiMock = {
  authApi: { login: vi.fn(), register: vi.fn(), me: vi.fn() },
  userApi: { update: vi.fn(), uploadAvatar: vi.fn() },
  catalogApi: { home: vi.fn(), categories: vi.fn(), products: vi.fn(), product: vi.fn() },
  cartApi: { list: vi.fn(), count: vi.fn(), add: vi.fn(), setQuantity: vi.fn(), remove: vi.fn(), checkout: vi.fn() },
  contactApi: { subscribe: vi.fn() },
}

export const adminApiMock = {
  adminApi: {
    dashboard: vi.fn(),
    orders: vi.fn(),
    updateOrderStatus: vi.fn(),
    categories: vi.fn(),
    createCategory: vi.fn(),
    updateCategory: vi.fn(),
    setCategoryVisibility: vi.fn(),
    deleteCategory: vi.fn(),
    products: vi.fn(),
    createProduct: vi.fn(),
    updateProduct: vi.fn(),
    deleteProduct: vi.fn(),
    sales: vi.fn(),
  },
}
