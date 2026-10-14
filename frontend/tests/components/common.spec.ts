import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import AppModal from '@/components/common/AppModal.vue'
import AppToast from '@/components/common/AppToast.vue'
import BackToTop from '@/components/common/BackToTop.vue'
import NewsletterForm from '@/components/common/NewsletterForm.vue'
import ProductGrid from '@/components/product/ProductGrid.vue'
import { useToastStore } from '@/stores/toast'
import { shopApiMock } from '../mocks/api'
import { axiosErr, mountWith, settle } from '../helpers'

vi.mock('@/api/shop', async () => (await import('../mocks/api')).shopApiMock)

const stubs = { teleport: true, RouterLink: { template: '<a><slot /></a>' } }

describe('AppModal', () => {
  it('renders nothing while closed', () => {
    const wrapper = mount(AppModal, { props: { open: false }, global: { stubs } })
    expect(wrapper.find('[role="dialog"]').exists()).toBe(false)
  })

  it('shows title, body and actions, and emits close on backdrop click', async () => {
    const wrapper = mount(AppModal, {
      props: { open: true, title: 'Hello' },
      slots: { default: '<p>Body</p>', actions: '<button>OK</button>' },
      global: { stubs },
    })
    expect(wrapper.text()).toContain('Hello')
    expect(wrapper.text()).toContain('Body')
    expect(wrapper.text()).toContain('OK')

    await wrapper.find('[role="dialog"]').trigger('click') // inside: must not close
    expect(wrapper.emitted('close')).toBeUndefined()
    await wrapper.find('div.fixed').trigger('click')
    expect(wrapper.emitted('close')).toHaveLength(1)
    await wrapper.find('div.fixed').trigger('keydown', { key: 'Escape' })
    expect(wrapper.emitted('close')).toHaveLength(2)
  })

  it('omits the title and actions when not provided', () => {
    const wrapper = mount(AppModal, { props: { open: true }, slots: { default: 'x' }, global: { stubs } })
    expect(wrapper.find('h2').exists()).toBe(false)
  })
})

describe('AppToast', () => {
  beforeEach(() => setActivePinia(createPinia()))

  it('lists toasts and dismisses one on click', async () => {
    const pinia = createPinia()
    setActivePinia(pinia)
    const toast = useToastStore()
    toast.push('Saved', 'success', 0)
    toast.push('Oops', 'error', 0)
    toast.push('FYI', 'info', 0)
    const wrapper = mount(AppToast, { global: { plugins: [pinia] } })
    expect(wrapper.findAll('[role="status"]')).toHaveLength(3)
    await wrapper.findAll('[role="status"]')[0]!.trigger('click')
    expect(wrapper.findAll('[role="status"]')).toHaveLength(2)
    expect(wrapper.text()).not.toContain('Saved')
  })
})

describe('BackToTop', () => {
  afterEach(() => {
    Object.defineProperty(window, 'scrollY', { value: 0, configurable: true })
  })

  it('appears after scrolling and scrolls to the top on click', async () => {
    const scrollTo = vi.fn()
    window.scrollTo = scrollTo as unknown as typeof window.scrollTo
    const wrapper = mount(BackToTop, { attachTo: document.body })
    expect(wrapper.find('button').isVisible()).toBe(false)

    Object.defineProperty(window, 'scrollY', { value: 800, configurable: true })
    window.dispatchEvent(new Event('scroll'))
    await wrapper.vm.$nextTick()
    expect(wrapper.find('button').isVisible()).toBe(true)

    await wrapper.find('button').trigger('click')
    expect(scrollTo).toHaveBeenCalledWith({ top: 0, behavior: 'smooth' })

    wrapper.unmount()
  })
})

describe('NewsletterForm', () => {
  beforeEach(() => vi.resetAllMocks())

  it('subscribes and clears the field', async () => {
    shopApiMock.contactApi.subscribe.mockResolvedValue({ message: 'Welcome!' })
    const { wrapper, pinia } = await mountWith(NewsletterForm)
    await wrapper.find('input').setValue('  me@example.com ')
    await wrapper.find('form').trigger('submit')
    await settle()
    expect(shopApiMock.contactApi.subscribe).toHaveBeenCalledWith('me@example.com')
    expect((wrapper.find('input').element as HTMLInputElement).value).toBe('')
    setActivePinia(pinia)
    expect(useToastStore().toasts[0]?.message).toBe('Welcome!')
  })

  it('shows the server message on failure and keeps the email', async () => {
    shopApiMock.contactApi.subscribe.mockRejectedValue(axiosErr(409, { message: 'Email already subscribed' }))
    const { wrapper, pinia } = await mountWith(NewsletterForm)
    await wrapper.find('input').setValue('me@example.com')
    await wrapper.find('form').trigger('submit')
    await settle()
    setActivePinia(pinia)
    const toast = useToastStore().toasts[0]
    expect(toast?.kind).toBe('error')
    expect(toast?.message).toBe('Email already subscribed')
    expect((wrapper.find('input').element as HTMLInputElement).value).toBe('me@example.com')
  })
})

describe('ProductGrid', () => {
  const products = [
    { id: 1, name: 'A', price: 100, image: null },
    { id: 2, name: 'B', price: 200, image: null },
  ]

  it('shows a loading state, an empty state or the cards', () => {
    const loading = mount(ProductGrid, { props: { products: [], loading: true }, global: { stubs } })
    expect(loading.text()).toContain('Loading')

    const empty = mount(ProductGrid, { props: { products: [] }, global: { stubs } })
    expect(empty.text()).toContain('No products found.')

    const custom = mount(ProductGrid, { props: { products: [], emptyText: 'Nothing here' }, global: { stubs } })
    expect(custom.text()).toContain('Nothing here')

    const full = mount(ProductGrid, { props: { products, loading: true }, global: { stubs } })
    expect(full.text()).toContain('฿100')
    expect(full.text()).toContain('฿200')
  })
})
