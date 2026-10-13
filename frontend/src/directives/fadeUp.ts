import type { Directive } from 'vue'

/** v-fade-up: element fades in once it scrolls into view. */
export const vFadeUp: Directive<HTMLElement> = {
    mounted(el) {
        el.classList.add('fade-up')
        if (typeof IntersectionObserver === 'undefined') {
            el.classList.add('is-visible')
            return
        }
        const observer = new IntersectionObserver(
            ([entry]) => {
                if (entry?.isIntersecting) {
                    el.classList.add('is-visible')
                    observer.disconnect()
                }
            },
            { threshold: 0.1 },
        )
        observer.observe(el)
    },
}
