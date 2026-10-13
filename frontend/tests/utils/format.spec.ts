import { describe, expect, it } from 'vitest'
import { formatCompact, formatPrice, isCssColor, rankLabel, sortSizes } from '@/utils/format'

describe('format utils', () => {
  it('formats prices in baht with separators', () => {
    expect(formatPrice(490)).toBe('฿490')
    expect(formatPrice('12500')).toBe('฿12,500')
  })

  it('compacts large earnings to K', () => {
    expect(formatCompact(800)).toBe('800')
    expect(formatCompact(12500)).toBe('13K')
  })

  it('labels ranks', () => {
    expect([0, 1, 2, 3].map(rankLabel)).toEqual(['1st', '2nd', '3rd', '4th'])
  })

  it('sorts clothing sizes in natural order and puts unknown sizes last', () => {
    expect(sortSizes(['XL', 'S', 'M', 'XS', 'FREE', 'L'])).toEqual(['XS', 'S', 'M', 'L', 'XL', 'FREE'])
  })

  it('does not throw when CSS.supports is unavailable', () => {
    expect(typeof isCssColor('red')).toBe('boolean')
  })
})
