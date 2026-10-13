import { describe, expect, it } from 'vitest'
import { mount, RouterLinkStub } from '@vue/test-utils'
import ProductCard from '@/components/product/ProductCard.vue'

describe('ProductCard', () => {
  const product = { id: 7, name: 'Pink Tiered Babydoll Top', price: 1550, image: '/uploads/a.jpg' }

  it('shows name, formatted price and links to the detail page', () => {
    const wrapper = mount(ProductCard, { props: { product }, global: { stubs: { RouterLink: RouterLinkStub } } })
    expect(wrapper.text()).toContain('Pink Tiered Babydoll Top')
    expect(wrapper.text()).toContain('฿1,550')
    expect(wrapper.findComponent(RouterLinkStub).props('to')).toBe('/product/7')
    expect(wrapper.find('img').attributes('src')).toBe('/uploads/a.jpg')
  })

  it('falls back to a placeholder when the image is missing or fails to load', async () => {
    const wrapper = mount(ProductCard, {
      props: { product: { ...product, image: null } },
      global: { stubs: { RouterLink: RouterLinkStub } },
    })
    const img = wrapper.find('img')
    expect(img.attributes('src')).toContain('data:image/svg+xml')

    await wrapper.setProps({ product: { ...product, image: '/broken.jpg' } })
    expect(wrapper.find('img').attributes('src')).toBe('/broken.jpg')
    await wrapper.find('img').trigger('error')
    expect(wrapper.find('img').attributes('src')).toContain('data:image/svg+xml')
  })
})
