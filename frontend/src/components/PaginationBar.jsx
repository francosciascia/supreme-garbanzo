import React from "react"
import { ChevronLeftIcon, ChevronRightIcon } from "lucide-react"
import "./PaginationBar.css"

/**
 * Devuelve los números de página a mostrar (con elipsis "..." entre saltos).
 * Ejemplos:
 *   total=5, current=3 -> [1,2,3,4,5]
 *   total=10, current=1 -> [1,2,3,'...',10]
 *   total=10, current=5 -> [1,'...',4,5,6,'...',10]
 *   total=10, current=10 -> [1,'...',8,9,10]
 */
function buildPageRange(current, total) {
  if (total <= 7) {
    return Array.from({ length: total }, (_, i) => i + 1)
  }

  const pages = new Set([1, total, current, current - 1, current + 1])
  const sorted = [...pages].filter((p) => p >= 1 && p <= total).sort((a, b) => a - b)

  const result = []
  for (let i = 0; i < sorted.length; i++) {
    result.push(sorted[i])
    if (i < sorted.length - 1 && sorted[i + 1] - sorted[i] > 1) {
      result.push("...")
    }
  }
  return result
}

export default function PaginationBar({
  page,
  pageSize,
  totalItems,
  onPageChange,
  onPageSizeChange,
  pageSizeOptions = [10, 20, 50, 100],
}) {
  const totalPages = Math.max(1, Math.ceil(totalItems / pageSize))
  const safePage = Math.min(Math.max(page, 1), totalPages)
  const from = totalItems === 0 ? 0 : (safePage - 1) * pageSize + 1
  const to = Math.min(safePage * pageSize, totalItems)

  const range = buildPageRange(safePage, totalPages)

  return (
    <div className="pagination-bar">
      <div className="pagination-info">
        {totalItems === 0
          ? "Sin resultados"
          : `Mostrando ${from}-${to} de ${totalItems}`}
      </div>

      <div className="pagination-controls">
        {onPageSizeChange && (
          <label className="pagination-page-size">
            Por página:
            <select
              value={pageSize}
              onChange={(e) => onPageSizeChange(Number(e.target.value))}
            >
              {pageSizeOptions.map((opt) => (
                <option key={opt} value={opt}>
                  {opt}
                </option>
              ))}
            </select>
          </label>
        )}

        <div className="pagination-pages">
          <button
            type="button"
            className="pagination-btn"
            onClick={() => onPageChange(safePage - 1)}
            disabled={safePage <= 1}
            title="Página anterior"
          >
            <ChevronLeftIcon size={16} />
          </button>

          {range.map((p, idx) =>
            p === "..." ? (
              <span key={`dots-${idx}`} className="pagination-dots">
                …
              </span>
            ) : (
              <button
                key={p}
                type="button"
                className={`pagination-btn ${
                  p === safePage ? "pagination-btn-active" : ""
                }`}
                onClick={() => onPageChange(p)}
              >
                {p}
              </button>
            )
          )}

          <button
            type="button"
            className="pagination-btn"
            onClick={() => onPageChange(safePage + 1)}
            disabled={safePage >= totalPages}
            title="Página siguiente"
          >
            <ChevronRightIcon size={16} />
          </button>
        </div>
      </div>
    </div>
  )
}
