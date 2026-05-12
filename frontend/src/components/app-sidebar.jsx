import { Link, useLocation } from "react-router-dom"
import {
  LayoutDashboardIcon,
  PackageIcon,
  TagsIcon,
  ShoppingCartIcon,
  UsersIcon,
  BoxesIcon,
  LogOutIcon
} from "lucide-react"

export function AppSidebar({ onLogout }) {
  const location = useLocation()

  const menuItems = [
    {
      title: "Dashboard",
      description: "Resumen general",
      url: "/",
      icon: LayoutDashboardIcon,
    },
    {
      title: "Productos",
      description: "Stock y precios",
      url: "/productos",
      icon: PackageIcon,
    },
    {
      title: "Categorías",
      description: "Rubros del sistema",
      url: "/categorias",
      icon: TagsIcon,
    },
    {
      title: "Clientes",
      description: "Cartera de compradores",
      url: "/clientes",
      icon: UsersIcon,
    },
    {
      title: "Ventas",
      description: "Registro comercial",
      url: "/ventas",
      icon: ShoppingCartIcon,
    },
  ]

  return (
    <aside className="fixed left-0 top-0 z-40 flex h-screen w-72 flex-col border-r border-slate-200 bg-white px-3 shadow-sm">
      <div className="flex h-20 items-center gap-3 border-b border-slate-200 px-3">
        <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-slate-900 text-white shadow-sm">
          <BoxesIcon className="h-6 w-6" />
        </div>

        <div className="min-w-0">
          <h1 className="truncate text-base font-bold text-slate-900">
            Stock Manager
          </h1>
          <p className="truncate text-xs text-slate-500">
            Control de stock y ventas
          </p>
        </div>
      </div>

      <nav className="flex-1 space-y-2 py-5">
        <p className="px-3 text-xs font-semibold uppercase tracking-wider text-slate-400">
          Menú principal
        </p>

        {menuItems.map((item) => {
          const Icon = item.icon
          const active = location.pathname === item.url

          return (
            <Link
              key={item.url}
              to={item.url}
              className={`group flex items-center gap-3 rounded-2xl px-4 py-3 transition-all ${
                active
                  ? "bg-slate-900 text-white shadow-md"
                  : "text-slate-700 hover:bg-slate-100"
              }`}
            >
              <div
                className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-xl ${
                  active
                    ? "bg-white/15 text-white"
                    : "bg-slate-100 text-slate-600 group-hover:bg-white"
                }`}
              >
                <Icon className="h-5 w-5" />
              </div>

              <div className="min-w-0 leading-tight">
                <p className="truncate text-sm font-semibold">{item.title}</p>
                <p
                  className={`truncate text-xs ${
                    active ? "text-slate-300" : "text-slate-500"
                  }`}
                >
                  {item.description}
                </p>
              </div>
            </Link>
          )
        })}
      </nav>

      <div className="mt-auto border-t border-slate-200 py-4">
        <button
          onClick={onLogout}
          className="flex w-full items-center gap-3 rounded-2xl px-4 py-3 text-slate-700 transition hover:bg-red-50 hover:text-red-600"
        >
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-slate-100">
            <LogOutIcon className="h-5 w-5" />
          </div>

          <div className="text-left">
            <p className="text-sm font-semibold">Cerrar sesión</p>
            <p className="text-xs text-slate-500">
              Salir del sistema
            </p>
          </div>
        </button>
      </div>
    </aside>
  )
}