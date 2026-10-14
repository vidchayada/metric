import { beforeEach, describe, expect, it, vi } from 'vitest'
import AppNavbar from '@/components/layout/AppNavbar.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import AdminNavbar from '@/components/layout/AdminNavbar.vue'
import MainLayout from '@/layouts/MainLayout.vue'
import AuthLayout from '@/layouts/AuthLayout.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'
import App from '@/App.vue'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'
import { TOKEN_KEY } from '@/api/client'
import { shopApiMock } from '../mocks/api'
import { mountWith, settle } from '../helpers'

vi.mock('@/api/shop', async () => (await import('../mocks/api')).shopApiMock)

const categories = [
  { id: 1, name: 'Tops', slug: 'tops', image: null },
  { id: 2, name: 'Dresses', slug: 'dresses', image: null },
]

beforeEach(() => {
  vi.resetAllMocks()
  shopApiMock.catalogApi.categories.mockResolvedValue(categories)
  shopApiMock.cartApi.count.mockResolvedValue(3)
})

describe('AppNavbar', () => {
  it('lists categories and hides the cart badge for guests', async () => {
    const { wrapper } = await mountWith(AppNavbar)
    expect(wrapper.text()).not.toContain('Dresses')
    await wrapper.find('button[aria-haspopup="true"]').trigger('click')
    expect(wrapper.text()).toContain('Tops')
    expect(wrapper.text()).toContain('Dresses')
    expect(wrapper.find('[data-testid="cart-count"]').exists()).toBe(false)
    expect(shopApiMock.cartApi.count).not.toHaveBeenCalled()
    expect(wrapper.find('a[aria-label="Account"]').attributes('href')).toBe('/login')
  })

  it('shows the cart count and a profile link when logged in', async () => {
    const { wrapper } = await mountWith(AppNavbar, { loggedIn: true })
    expect(wrapper.find('[data-testid="cart-count"]').text()).toBe('3')
    expect(wrapper.find('a[aria-label="Account"]').attributes('href')).toBe('/profile')
  })

  it('searches, ignores blank input and closes the mobile menu', async () => {
    const { wrapper, router } = await mountWith(AppNavbar)
    const form = wrapper.find('form[role="search"]')
    await form.find('input').setValue('   ')
    await form.trigger('submit')
    await settle()
    expect(router.currentRoute.value.path).toBe('/')

    await form.find('input').setValue('linen shirt')
    await form.trigger('submit')
    await settle()
    expect(router.currentRoute.value.path).toBe('/search')
    expect(router.currentRoute.value.query.q).toBe('linen shirt')
  })

  it('opens the mobile drawer and toggles the category list', async () => {
    const { wrapper } = await mountWith(AppNavbar)
    await wrapper.find('button[aria-label="Open menu"]').trigger('click')
    expect(wrapper.find('aside').classes()).toContain('translate-x-0')
    await wrapper.find('button[aria-label="Close menu"]').trigger('click')
    expect(wrapper.find('aside').classes()).toContain('translate-x-full')
  })

  it('toggles the desktop category menu', async () => {
    const { wrapper } = await mountWith(AppNavbar)
    const button = wrapper.find('button[aria-haspopup="true"]')
    await button.trigger('click')
    expect(button.attributes('aria-expanded')).toBe('true')
    await wrapper.find('li.relative').trigger('mouseleave')
    expect(button.attributes('aria-expanded')).toBe('false')
    await wrapper.find('li.relative').trigger('mouseenter')
    expect(button.attributes('aria-expanded')).toBe('true')
  })

  it('adds a shadow after scrolling', async () => {
    const { wrapper } = await mountWith(AppNavbar)
    Object.defineProperty(window, 'scrollY', { value: 50, configurable: true })
    window.dispatchEvent(new Event('scroll'))
    await wrapper.vm.$nextTick()
    expect(wrapper.find('header').classes()).toContain('shadow-sm')
    Object.defineProperty(window, 'scrollY', { value: 0, configurable: true })
  })

  it('reloads the badge on login and clears it on logout', async () => {
    const { wrapper, pinia } = await mountWith(AppNavbar)
    const auth = useAuthStore(pinia)
    const cart = useCartStore(pinia)
    auth.token = 'abc'
    await settle()
    expect(cart.count).toBe(3)
    auth.token = null
    await settle()
    expect(cart.count).toBe(0)
    expect(wrapper.find('[data-testid="cart-count"]').exists()).toBe(false)
  })
})

describe('AppFooter', () => {
  it('renders contact details and the newsletter form', async () => {
    const { wrapper } = await mountWith(AppFooter)
    expect(wrapper.text()).toContain('Contact us')
    expect(wrapper.text()).toContain('Start a Return')
    expect(wrapper.find('input[type="email"]').exists()).toBe(true)
  })
})

describe('AdminNavbar', () => {
  it('links to every admin page', async () => {
    const { wrapper } = await mountWith(AdminNavbar, { admin: true })
    const text = wrapper.text()
    for (const label of ['Sales History', 'Orders', 'Product', 'Categories']) expect(text).toContain(label)
  })

  it('logs out, clears the cart and goes to the login page', async () => {
    const { wrapper, router, pinia } = await mountWith(AdminNavbar, { admin: true })
    const cart = useCartStore(pinia)
    cart.count = 5
    await wrapper.find('button[title="Logout"]').trigger('click')
    await settle()
    expect(useAuthStore(pinia).isLoggedIn).toBe(false)
    expect(localStorage.getItem(TOKEN_KEY)).toBeNull()
    expect(cart.count).toBe(0)
    expect(router.currentRoute.value.path).toBe('/login')
  })

  it('opens and closes the mobile sidebar', async () => {
    const { wrapper } = await mountWith(AdminNavbar, { admin: true })
    await wrapper.find('button[aria-label="Open menu"]').trigger('click')
    expect(wrapper.find('aside').classes()).toContain('translate-x-0')
    await wrapper.find('aside a').trigger('click')
    expect(wrapper.find('aside').classes()).toContain('-translate-x-full')
  })
})

describe('layouts and App', () => {
  it('MainLayout wraps pages with navbar and footer', async () => {
    const { wrapper } = await mountWith(MainLayout)
    expect(wrapper.find('header').exists()).toBe(true)
    expect(wrapper.find('footer').exists()).toBe(true)
  })

  it('AuthLayout shows the brand', async () => {
    const { wrapper } = await mountWith(AuthLayout)
    expect(wrapper.text()).toContain('Girlette')
  })

  it('AdminLayout shows the admin navbar', async () => {
    const { wrapper } = await mountWith(AdminLayout, { admin: true })
    expect(wrapper.text()).toContain('Sales History')
  })

  it('App renders the router outlet and toast area', async () => {
    const { wrapper } = await mountWith(App)
    expect(wrapper.find('[aria-live="polite"]').exists()).toBe(true)
  })
})
