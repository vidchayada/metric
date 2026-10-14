import { describe, expect, it } from 'vitest'
import { getPageNumbers } from '@/utils/pagination'

describe('getPageNumbers', () => {
  it('lists every page when there are 7 or fewer', () => {
    expect(getPageNumbers(1, 1)).toEqual([1])
    expect(getPageNumbers(3, 7)).toEqual([1, 2, 3, 4, 5, 6, 7])
  })

  it('collapses the tail when near the start', () => {
    expect(getPageNumbers(1, 12)).toEqual([1, 2, '...', 12])
    expect(getPageNumbers(3, 12)).toEqual([1, 2, 3, 4, '...', 12])
  })

  it('collapses both sides in the middle', () => {
    expect(getPageNumbers(6, 12)).toEqual([1, '...', 5, 6, 7, '...', 12])
  })

  it('collapses the head when near the end', () => {
    expect(getPageNumbers(12, 12)).toEqual([1, '...', 11, 12])
    expect(getPageNumbers(10, 12)).toEqual([1, '...', 9, 10, 11, 12])
  })
})
