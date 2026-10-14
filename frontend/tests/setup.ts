import { vi } from 'vitest'

// jsdom does not implement scrolling; the router and BackToTop call it.
window.scrollTo = vi.fn() as unknown as typeof window.scrollTo
