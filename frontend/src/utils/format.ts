export function formatPrice(value: number | string): string {
    return `฿${Number(value).toLocaleString('en-US')}`
}

/** 12500 -> "13K", 800 -> "800" (used for dashboard earnings) */
export function formatCompact(value: number): string {
    return value >= 1000 ? `${(value / 1000).toFixed(0)}K` : String(value)
}

export function formatLongDate(date: Date = new Date()): string {
    return date.toLocaleDateString('en-GB', {
        weekday: 'short',
        day: '2-digit',
        month: 'short',
        year: 'numeric',
    })
}

export const MONTHS = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']

export function rankLabel(index: number): string {
    return ['1st', '2nd', '3rd'][index] ?? `${index + 1}th`
}

export function isCssColor(value: string): boolean {
    return typeof CSS !== 'undefined' && typeof CSS.supports === 'function' && CSS.supports('color', value)
}

export const SIZE_ORDER = ['XS', 'S', 'M', 'L', 'XL', 'XXL']

export function sortSizes(sizes: string[]): string[] {
    const rank = (s: string) => {
        const i = SIZE_ORDER.indexOf(s)
        return i === -1 ? SIZE_ORDER.length : i
    }
    return [...sizes].sort((a, b) => rank(a) - rank(b) || a.localeCompare(b))
}
