import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { routes, router } from '@/router'
import { useAuthStore } from '@/stores/auth'
import { TOKEN_KEY } from '@/api/client'
import { shopApiMock } from '../mocks/api'
import { user } from '../helpers'

vi.mock('@/api/shop', async () => (await import('../mocks/api')).shopApiMock)

async function go(path: string) {
  await router.push(path)
  await router.isReady()
  return router.currentRoute.value
}

describe('router', () => {
  beforeEach(async () => {
    vi.resetAllMocks()
    localStorage.clear()
    setActivePinia(createPinia())
    await router.replace('/contact') // neutral starting point
  })

  it('defines the storefront, auth and admin routes', () => {
    const names = routes.flatMap((r) => r.children?.map((c) => c.name) ?? [])
    for (const n of ['home', 'products', 'category', 'search', 'product', 'cart', 'contact', 'profile', 'login', 'register', 'admin-dashboard', 'admin-orders', 'admin-products', 'admin-categories', 'admin-sales']) {
      expect(names).toContain(n)
    }
  })

  it('lets guests browse public pages', async () => {
    expect((await go('/products')).name).toBe('products')
    expect((await go('/product/12')).params.id).toBe('12')
  })

  it('sends guests to login, remembering the target', async () => {
    const route = await go('/cart')
    expect(route.name).toBe('login')
    expect(route.query.returnTo).toBe('/cart')
  })

  it('restores the user from the stored token before checking the role', async () => {
    localStorage.setItem(TOKEN_KEY, 't')
    setActivePinia(createPinia())
    shopApiMock.authApi.me.mockResolvedValue(user())
    const route = await go('/profile')
    expect(shopApiMock.authApi.me).toHaveBeenCalled()
    expect(route.name).toBe('profile')
  })

  it('keeps non-admins out of the admin area', async () => {
    localStorage.setItem(TOKEN_KEY, 't')
    setActivePinia(createPinia())
    shopApiMock.authApi.me.mockResolvedValue(user({ role: 'USER' }))
    expect((await go('/admin/orders')).name).toBe('home')
  })

  it('lets admins in and redirects /admin to the dashboard', async () => {
    localStorage.setItem(TOKEN_KEY, 't')
    setActivePinia(createPinia())
    shopApiMock.authApi.me.mockResolvedValue(user({ role: 'ADMIN' }))
    expect((await go('/admin')).name).toBe('admin-dashboard')
  })

  it('bounces logged-in users away from login and register', async () => {
    localStorage.setItem(TOKEN_KEY, 't')
    setActivePinia(createPinia())
    shopApiMock.authApi.me.mockResolvedValue(user({ role: 'USER' }))
    expect((await go('/login')).name).toBe('home')
    shopApiMock.authApi.me.mockResolvedValue(user({ role: 'ADMIN' }))
    useAuthStore().user = user({ role: 'ADMIN' })
    expect((await go('/register')).name).toBe('admin-dashboard')
  })

  it('shows the not-found page for unknown URLs', async () => {
    expect((await go('/definitely/not/here')).name).toBe('not-found')
  })
})
