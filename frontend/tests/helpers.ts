import { AxiosError, type AxiosResponse } from 'axios'
import { createPinia, setActivePinia, type Pinia } from 'pinia'
import { createMemoryHistory, createRouter } from 'vue-router'
import { flushPromises, mount, type VueWrapper } from '@vue/test-utils'
import type { Component } from 'vue'
import { vFadeUp } from '@/directives/fadeUp'
import { TOKEN_KEY } from '@/api/client'
import { useAuthStore } from '@/stores/auth'
import type { User } from '@/types'

const blank = { render: () => null }
const paths: [string, string][] = [
  ['home', '/'],
  ['products', '/products'],
  ['category', '/category/:slug'],
  ['search', '/search'],
  ['product', '/product/:id'],
  ['cart', '/cart'],
  ['contact', '/contact'],
  ['profile', '/profile'],
  ['login', '/login'],
  ['register', '/register'],
  ['admin-dashboard', '/admin/dashboard'],
  ['admin-orders', '/admin/orders'],
  ['admin-products', '/admin/products'],
  ['admin-categories', '/admin/categories'],
  ['admin-sales', '/admin/sales-history'],
]

export function makeRouter() {
  return createRouter({
    history: createMemoryHistory(),
    routes: paths.map(([name, path]) => ({ name, path, component: blank })),
  })
}

export const user = (over: Partial<User> = {}): User => ({
  id: 1,
  username: 'mina',
  fullName: 'Mina Chan',
  email: 'mina@example.com',
  phone: '0812345678',
  dateOfBirth: '2000-01-31',
  address: 'Chiang Mai',
  profileImage: null,
  role: 'USER',
  ...over,
})

export function axiosErr(status: number, data: unknown = {}) {
  return new AxiosError('request failed', String(status), undefined, undefined, {
    status,
    data,
    statusText: '',
    headers: {},
  } as unknown as AxiosResponse)
}

interface MountOptions {
  route?: string
  props?: Record<string, unknown>
  /** Runs after Pinia exists but before the component is created. */
  setup?: (pinia: Pinia) => void
  loggedIn?: boolean
  admin?: boolean
}

/** Mounts a component with Pinia, a memory router and the v-fade-up directive. Teleports render inline. */
export async function mountWith(component: Component, opts: MountOptions = {}) {
  localStorage.clear()
  if (opts.loggedIn || opts.admin) localStorage.setItem(TOKEN_KEY, 'test-token')
  const pinia = createPinia()
  setActivePinia(pinia)
  if (opts.loggedIn || opts.admin) {
    useAuthStore().setUser(user({ role: opts.admin ? 'ADMIN' : 'USER' }))
  }
  opts.setup?.(pinia)

  const router = makeRouter()
  await router.push(opts.route ?? '/')
  await router.isReady()

  const wrapper = mount(component, {
    props: opts.props,
    global: {
      plugins: [pinia, router],
      directives: { 'fade-up': vFadeUp },
      stubs: { teleport: true },
    },
  }) as VueWrapper
  await flushPromises()
  return { wrapper, router, pinia }
}

export const settle = () => flushPromises()
