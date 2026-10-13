import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import SalesTable from '@/components/common/SalesTable.vue'

describe('SalesTable', () => {
  it('shows the empty message without rows', () => {
    const wrapper = mount(SalesTable, { props: { rows: [], periodLabel: 'Date', emptyText: 'Nothing here' } })
    expect(wrapper.text()).toContain('Nothing here')
    expect(wrapper.find('table').exists()).toBe(false)
  })

  it('abbreviates thousands with the baht sign and flags low sales', () => {
    const wrapper = mount(SalesTable, {
      props: {
        periodLabel: 'Month',
        emptyText: '',
        rows: [
          { label: 'Jan', totalQty: 10, totalSales: 12500 },
          { label: 'Feb', totalQty: 2, totalSales: 800 },
        ],
      },
    })
    const text = wrapper.text()
    expect(text).toContain('฿13K')
    expect(text).toContain('฿800')
    expect(wrapper.findAll('td.text-red-600')).toHaveLength(1)
  })
})
