import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useToastStore } from '@/stores/toast'

describe('toast store', () => {
  beforeEach(() => {
    vi.useFakeTimers()
    setActivePinia(createPinia())
  })
  afterEach(() => vi.useRealTimers())

  it('adds a toast and removes it after the timeout', () => {
    const toast = useToastStore()
    toast.success('Saved')
    expect(toast.toasts).toHaveLength(1)
    expect(toast.toasts[0]?.kind).toBe('success')
    vi.advanceTimersByTime(3000)
    expect(toast.toasts).toHaveLength(0)
  })

  it('keeps error toasts a little longer and can dismiss manually', () => {
    const toast = useToastStore()
    const id = toast.push('boom', 'error', 4000)
    vi.advanceTimersByTime(3000)
    expect(toast.toasts).toHaveLength(1)
    toast.dismiss(id)
    expect(toast.toasts).toHaveLength(0)
  })

  it('does not auto-dismiss when duration is 0', () => {
    const toast = useToastStore()
    toast.push('sticky', 'info', 0)
    vi.advanceTimersByTime(60_000)
    expect(toast.toasts).toHaveLength(1)
  })
})
