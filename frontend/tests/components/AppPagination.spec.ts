import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import AppPagination from '@/components/common/AppPagination.vue'

describe('AppPagination', () => {
  it('renders nothing for a single page', () => {
    const wrapper = mount(AppPagination, { props: { page: 1, totalPages: 1 } })
    expect(wrapper.find('nav').exists()).toBe(false)
  })

  it('marks the current page and disables prev on the first page', () => {
    const wrapper = mount(AppPagination, { props: { page: 1, totalPages: 3 } })
    expect(wrapper.find('[aria-current="page"]').text()).toBe('1')
    expect(wrapper.find('[aria-label="Previous page"]').attributes('disabled')).toBeDefined()
    expect(wrapper.find('[aria-label="Next page"]').attributes('disabled')).toBeUndefined()
  })

  it('emits the chosen page', async () => {
    const wrapper = mount(AppPagination, { props: { page: 2, totalPages: 5 } })
    await wrapper.find('[aria-label="Next page"]').trigger('click')
    await wrapper.find('[aria-label="Previous page"]').trigger('click')
    const buttons = wrapper.findAll('button').filter((b) => b.text() === '4')
    await buttons[0]?.trigger('click')
    expect(wrapper.emitted('change')).toEqual([[3], [1], [4]])
  })

  it('disables next on the last page', () => {
    const wrapper = mount(AppPagination, { props: { page: 3, totalPages: 3 } })
    expect(wrapper.find('[aria-label="Next page"]').attributes('disabled')).toBeDefined()
  })
})
