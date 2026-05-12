import React, { useMemo } from "react"
import {
  PackageIcon,
  ShoppingCartIcon,
  DollarSignIcon,
  AlertTriangleIcon,
  UsersIcon,
  TrendingUpIcon,
  ReceiptIcon,
  CalendarDaysIcon,
} from "lucide-react"

import { useProducts } from "../hooks/useProducts"
import { useSales } from "../hooks/useSales"
import { useClientes } from "../hooks/useClientes"
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { Skeleton } from "@/components/ui/skeleton"

const LOW_STOCK_THRESHOLD = 10

function formatCurrency(value) {
  const num = Number(value || 0)
  return num.toLocaleString("es-AR", {
    style: "currency",
    currency: "ARS",
    minimumFractionDigits: 2,
  })
}

function formatDate(iso) {
  if (!iso) return "-"
  return new Date(iso).toLocaleDateString("es-AR", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  })
}

function StatCard({ icon: Icon, label, value, hint, accent = "slate" }) {
  const accentMap = {
    slate: "bg-slate-100 text-slate-700",
    emerald: "bg-emerald-100 text-emerald-700",
    sky: "bg-sky-100 text-sky-700",
    amber: "bg-amber-100 text-amber-700",
    rose: "bg-rose-100 text-rose-700",
    violet: "bg-violet-100 text-violet-700",
  }

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <CardDescription className="font-medium">{label}</CardDescription>
        <div
          className={`flex h-10 w-10 items-center justify-center rounded-xl ${accentMap[accent]}`}
        >
          <Icon className="h-5 w-5" />
        </div>
      </CardHeader>
      <CardContent>
        <div className="text-3xl font-bold tracking-tight">{value}</div>
        {hint && <p className="mt-1 text-xs text-slate-500">{hint}</p>}
      </CardContent>
    </Card>
  )
}

function StatSkeleton() {
  return (
    <Card>
      <CardHeader className="pb-2">
        <Skeleton className="h-4 w-24" />
      </CardHeader>
      <CardContent>
        <Skeleton className="h-8 w-16" />
        <Skeleton className="mt-2 h-3 w-32" />
      </CardContent>
    </Card>
  )
}

const Dashboard = ({ user }) => {
  const { products, loading: productsLoading, error: productsError } = useProducts()
  const { sales, loading: salesLoading, error: salesError } = useSales()
  const { clientes, loading: clientesLoading, error: clientesError } = useClientes()

  const loading = productsLoading || salesLoading || clientesLoading
  const error = productsError || salesError || clientesError

  const salesArray = Array.isArray(sales) ? sales : []
  const productsArray = Array.isArray(products) ? products : []
  const clientesArray = Array.isArray(clientes) ? clientes : []

  const stats = useMemo(() => {
    const totalRevenue = salesArray.reduce(
      (acc, s) => acc + Number(s.total || 0),
      0
    )

    const totalSales = salesArray.length
    const avgTicket = totalSales > 0 ? totalRevenue / totalSales : 0

    const today = new Date().toISOString().slice(0, 10)
    const salesToday = salesArray.filter((s) => s.fecha === today).length
    const revenueToday = salesArray
      .filter((s) => s.fecha === today)
      .reduce((acc, s) => acc + Number(s.total || 0), 0)

    const lowStock = productsArray.filter(
      (p) => Number(p.stock) < LOW_STOCK_THRESHOLD
    )

    const clientesActivos = clientesArray.filter((c) => c.activo).length

    const productSales = new Map()
    salesArray.forEach((sale) => {
      ;(sale.items || []).forEach((item) => {
        const key = item.productoId ?? item.nombreProducto
        const current = productSales.get(key) || {
          nombre: item.nombreProducto || "Producto",
          cantidad: 0,
          ingreso: 0,
        }
        current.cantidad += Number(item.cantidad || 0)
        current.ingreso += Number(item.subtotal || 0)
        productSales.set(key, current)
      })
    })

    const topProducts = Array.from(productSales.values())
      .sort((a, b) => b.cantidad - a.cantidad)
      .slice(0, 5)

    const maxQty = topProducts[0]?.cantidad || 1

    const recentSales = [...salesArray]
      .sort((a, b) => new Date(b.fecha) - new Date(a.fecha))
      .slice(0, 6)

    return {
      totalRevenue,
      totalSales,
      avgTicket,
      salesToday,
      revenueToday,
      lowStock,
      clientesActivos,
      topProducts,
      maxQty,
      recentSales,
    }
  }, [salesArray, productsArray, clientesArray])

  if (error) {
    return (
      <div className="mx-auto max-w-2xl rounded-2xl border border-rose-200 bg-rose-50 p-8 text-center">
        <h2 className="text-xl font-semibold text-rose-700">
          Error cargando el dashboard
        </h2>
        <p className="mt-2 text-sm text-rose-600">{error}</p>
        <button
          onClick={() => window.location.reload()}
          className="mt-4 rounded-xl bg-rose-600 px-4 py-2 text-sm font-medium text-white hover:bg-rose-700"
        >
          Reintentar
        </button>
      </div>
    )
  }

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-3xl font-bold tracking-tight text-slate-900">
          Dashboard
        </h1>
        <p className="mt-1 text-sm text-slate-500">
          Bienvenido{user?.nombre ? `, ${user.nombre}` : ""}. Resumen general del negocio.
        </p>
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {loading ? (
          <>
            <StatSkeleton />
            <StatSkeleton />
            <StatSkeleton />
            <StatSkeleton />
          </>
        ) : (
          <>
            <StatCard
              icon={DollarSignIcon}
              label="Ingresos totales"
              value={formatCurrency(stats.totalRevenue)}
              hint={`Hoy: ${formatCurrency(stats.revenueToday)}`}
              accent="emerald"
            />
            <StatCard
              icon={ShoppingCartIcon}
              label="Ventas totales"
              value={stats.totalSales}
              hint={`${stats.salesToday} ventas hoy`}
              accent="sky"
            />
            <StatCard
              icon={ReceiptIcon}
              label="Ticket promedio"
              value={formatCurrency(stats.avgTicket)}
              hint="Por venta"
              accent="violet"
            />
            <StatCard
              icon={UsersIcon}
              label="Clientes"
              value={clientesArray.length}
              hint={`${stats.clientesActivos} activos`}
              accent="slate"
            />
          </>
        )}
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {loading ? (
          <>
            <StatSkeleton />
            <StatSkeleton />
            <StatSkeleton />
            <StatSkeleton />
          </>
        ) : (
          <>
            <StatCard
              icon={PackageIcon}
              label="Productos"
              value={productsArray.length}
              hint="En catálogo"
              accent="slate"
            />
            <StatCard
              icon={AlertTriangleIcon}
              label="Stock bajo"
              value={stats.lowStock.length}
              hint={`< ${LOW_STOCK_THRESHOLD} unidades`}
              accent="amber"
            />
            <StatCard
              icon={CalendarDaysIcon}
              label="Ventas hoy"
              value={stats.salesToday}
              hint={formatDate(new Date().toISOString())}
              accent="sky"
            />
            <StatCard
              icon={TrendingUpIcon}
              label="Ingresos hoy"
              value={formatCurrency(stats.revenueToday)}
              hint="Acumulado del día"
              accent="emerald"
            />
          </>
        )}
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <TrendingUpIcon className="h-5 w-5 text-emerald-600" />
              Top productos vendidos
            </CardTitle>
            <CardDescription>Los más vendidos por cantidad</CardDescription>
          </CardHeader>
          <CardContent>
            {loading ? (
              <div className="space-y-3">
                {Array.from({ length: 4 }).map((_, i) => (
                  <Skeleton key={i} className="h-6 w-full" />
                ))}
              </div>
            ) : stats.topProducts.length === 0 ? (
              <p className="text-sm text-slate-500">
                Aún no hay ventas registradas.
              </p>
            ) : (
              <ul className="space-y-4">
                {stats.topProducts.map((p, idx) => {
                  const pct = Math.round((p.cantidad / stats.maxQty) * 100)
                  return (
                    <li key={`${p.nombre}-${idx}`}>
                      <div className="flex items-center justify-between text-sm">
                        <span className="truncate font-medium text-slate-700">
                          {idx + 1}. {p.nombre}
                        </span>
                        <span className="ml-2 text-xs text-slate-500">
                          {p.cantidad} u · {formatCurrency(p.ingreso)}
                        </span>
                      </div>
                      <div className="mt-1.5 h-2 w-full overflow-hidden rounded-full bg-slate-100">
                        <div
                          className="h-full rounded-full bg-emerald-500 transition-all"
                          style={{ width: `${pct}%` }}
                        />
                      </div>
                    </li>
                  )
                })}
              </ul>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <AlertTriangleIcon className="h-5 w-5 text-amber-600" />
              Productos con stock bajo
            </CardTitle>
            <CardDescription>
              Menos de {LOW_STOCK_THRESHOLD} unidades disponibles
            </CardDescription>
          </CardHeader>
          <CardContent>
            {loading ? (
              <div className="space-y-3">
                {Array.from({ length: 4 }).map((_, i) => (
                  <Skeleton key={i} className="h-6 w-full" />
                ))}
              </div>
            ) : stats.lowStock.length === 0 ? (
              <p className="text-sm text-slate-500">
                Todos los productos tienen stock saludable.
              </p>
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Producto</TableHead>
                    <TableHead className="w-24 text-right">Stock</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {stats.lowStock.slice(0, 6).map((p) => (
                    <TableRow key={p.id}>
                      <TableCell className="font-medium">{p.nombre}</TableCell>
                      <TableCell className="text-right">
                        <Badge
                          variant={
                            Number(p.stock) === 0 ? "destructive" : "secondary"
                          }
                        >
                          {p.stock}
                        </Badge>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            )}
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <ShoppingCartIcon className="h-5 w-5 text-sky-600" />
            Ventas recientes
          </CardTitle>
          <CardDescription>Últimas operaciones registradas</CardDescription>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div className="space-y-3">
              {Array.from({ length: 4 }).map((_, i) => (
                <Skeleton key={i} className="h-8 w-full" />
              ))}
            </div>
          ) : stats.recentSales.length === 0 ? (
            <p className="text-sm text-slate-500">No hay ventas registradas.</p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="w-24">Venta</TableHead>
                  <TableHead>Cliente</TableHead>
                  <TableHead>Fecha</TableHead>
                  <TableHead className="text-center">Items</TableHead>
                  <TableHead className="text-right">Total</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {stats.recentSales.map((sale) => (
                  <TableRow key={sale.id}>
                    <TableCell className="font-medium">#{sale.id}</TableCell>
                    <TableCell>
                      {sale.cliente
                        ? `${sale.cliente.nombre} ${sale.cliente.apellido}`
                        : (
                          <span className="text-slate-400">Sin asignar</span>
                        )}
                    </TableCell>
                    <TableCell className="text-slate-600">
                      {formatDate(sale.fecha)}
                    </TableCell>
                    <TableCell className="text-center">
                      <Badge variant="outline">
                        {(sale.items || []).length}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-right font-semibold">
                      {formatCurrency(sale.total)}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </div>
  )
}

export default Dashboard
