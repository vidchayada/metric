export type PageToken = number | '...'

/** Builds the compact page list: 1 ... 4 5 6 ... 12 */
export function getPageNumbers(current: number, total: number): PageToken[] {
    if (total <= 7) return Array.from({ length: total }, (_, i) => i + 1)

    const pages: PageToken[] = [1]
    if (current > 3) pages.push('...')

    const start = Math.max(2, current - 1)
    const end = Math.min(total - 1, current + 1)
    for (let i = start; i <= end; i++) pages.push(i)

    if (current < total - 2) pages.push('...')
    pages.push(total)
    return pages
}
