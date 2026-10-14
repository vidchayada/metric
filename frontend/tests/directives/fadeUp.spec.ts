import { afterEach, describe, expect, it, vi } from 'vitest'
import type { ObjectDirective } from 'vue'
import { vFadeUp } from '@/directives/fadeUp'

type Callback = (entries: { isIntersecting: boolean }[]) => void
const mounted = (vFadeUp as ObjectDirective<HTMLElement>).mounted as (el: HTMLElement) => void

describe('v-fade-up', () => {
  afterEach(() => vi.unstubAllGlobals())

  it('shows the element immediately when IntersectionObserver is unavailable', () => {
    vi.stubGlobal('IntersectionObserver', undefined)
    const el = document.createElement('div')
    mounted(el)
    expect(el.classList.contains('fade-up')).toBe(true)
    expect(el.classList.contains('is-visible')).toBe(true)
  })

  it('reveals the element once it intersects, then stops observing', () => {
    let callback: Callback = () => {}
    const disconnect = vi.fn()
    vi.stubGlobal(
      'IntersectionObserver',
      class {
        constructor(cb: Callback) {
          callback = cb
        }
        observe() {}
        disconnect = disconnect
      },
    )
    const el = document.createElement('div')
    mounted(el)
    expect(el.classList.contains('is-visible')).toBe(false)

    callback([{ isIntersecting: false }])
    expect(el.classList.contains('is-visible')).toBe(false)

    callback([{ isIntersecting: true }])
    expect(el.classList.contains('is-visible')).toBe(true)
    expect(disconnect).toHaveBeenCalled()
  })
})
