import { describe, expect, it } from 'vitest'
import { mount, RouterLinkStub } from '@vue/test-utils'
import CartItemRow from '@/components/product/CartItemRow.vue'
import type { CartItem } from '@/types'

const item: CartItem = {
  id: 3,
  productId: 9,
  attributeId: 4,
  name: 'Linen Shirt',
  price: 500,
  quantity: 2,
  size: 'M',
  color: 'Beige',
  image: null,
}

const mountRow = () => mount(CartItemRow, { props: { item }, global: { stubs: { RouterLink: RouterLinkStub } } })

describe('CartItemRow', () => {
  it('shows attributes and line total', () => {
    const text = mountRow().text()
    expect(text).toContain('Color: Beige')
    expect(text).toContain('Size: M')
    expect(text).toContain('฿1,000')
  })

  it('emits quantity changes and removal', async () => {
    const wrapper = mountRow()
    await wrapper.find('[aria-label="Increase quantity"]').trigger('click')
    await wrapper.find('[aria-label="Decrease quantity"]').trigger('click')
    await wrapper.find('[aria-label="Remove item"]').trigger('click')
    expect(wrapper.emitted('quantity')).toEqual([[3, 3], [3, 1]])
    expect(wrapper.emitted('remove')).toEqual([[3]])
  })
})
