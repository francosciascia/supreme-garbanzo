import React, { useEffect, useMemo, useState } from "react"
import {
  PlusIcon,
  PencilIcon,
  Trash2Icon,
  SearchIcon,
  MailIcon,
  PhoneIcon,
  MapPinIcon,
  IdCardIcon,
  UsersIcon,
} from "lucide-react"

import { useClientes } from "../hooks/useClientes"
import PaginationBar from "./PaginationBar"
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  CardDescription,
} from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Skeleton } from "@/components/ui/skeleton"

const emptyForm = {
  nombre: "",
  apellido: "",
  dni: "",
  email: "",
  telefono: "",
  direccion: "",
  activo: true,
}

function Field({ label, children, required, error }) {
  return (
    <div className="space-y-1.5">
      <label className="text-xs font-medium text-slate-700">
        {label}
        {required && <span className="ml-0.5 text-rose-500">*</span>}
      </label>
      {children}
      {error && <p className="text-xs text-rose-600">{error}</p>}
    </div>
  )
}

export default function Clientes({ user }) {
  const {
    clientes,
    loading,
    error,
    createCliente,
    updateCliente,
    deleteCliente,
  } = useClientes()

  const [search, setSearch] = useState("")
  const [estadoFilter, setEstadoFilter] = useState("todos")
  const [sortBy, setSortBy] = useState("nombre-asc")
  const [page, setPage] = useState(1)
  const [pageSize, setPageSize] = useState(10)
  const [showModal, setShowModal] = useState(false)
  const [editing, setEditing] = useState(null)
  const [form, setForm] = useState(emptyForm)
  const [submitting, setSubmitting] = useState(false)
  const [formError, setFormError] = useState("")

  const isAdmin = user?.rol === "ADMIN" || user?.rol === "SUPER_ADMIN"
  const isSuperAdmin = user?.rol === "SUPER_ADMIN"

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase()

    let result = clientes.filter((c) => {
      if (estadoFilter === "activos" && !c.activo) return false
      if (estadoFilter === "inactivos" && c.activo) return false
      if (!q) return true
      const haystack = [
        c.nombre,
        c.apellido,
        c.email,
        c.telefono,
        String(c.dni),
      ]
        .filter(Boolean)
        .join(" ")
        .toLowerCase()
      return haystack.includes(q)
    })

    const collator = new Intl.Collator("es", { sensitivity: "base" })
    const comparators = {
      "nombre-asc": (a, b) => collator.compare(a.nombre, b.nombre),
      "nombre-desc": (a, b) => collator.compare(b.nombre, a.nombre),
      "apellido-asc": (a, b) => collator.compare(a.apellido, b.apellido),
      "apellido-desc": (a, b) => collator.compare(b.apellido, a.apellido),
      "dni-asc": (a, b) => (a.dni ?? 0) - (b.dni ?? 0),
      "dni-desc": (a, b) => (b.dni ?? 0) - (a.dni ?? 0),
      "fecha-desc": (a, b) =>
        new Date(b.fechaRegistro || 0) - new Date(a.fechaRegistro || 0),
      "fecha-asc": (a, b) =>
        new Date(a.fechaRegistro || 0) - new Date(b.fechaRegistro || 0),
    }

    const cmp = comparators[sortBy] || comparators["nombre-asc"]
    return [...result].sort(cmp)
  }, [clientes, search, estadoFilter, sortBy])

  useEffect(() => {
    setPage(1)
  }, [search, estadoFilter, sortBy, pageSize])

  const totalItems = filtered.length
  const totalPages = Math.max(1, Math.ceil(totalItems / pageSize))
  const safePage = Math.min(page, totalPages)
  const paginated = useMemo(() => {
    const start = (safePage - 1) * pageSize
    return filtered.slice(start, start + pageSize)
  }, [filtered, safePage, pageSize])

  const activos = clientes.filter((c) => c.activo).length

  const openCreate = () => {
    setEditing(null)
    setForm(emptyForm)
    setFormError("")
    setShowModal(true)
  }

  const openEdit = (cliente) => {
    setEditing(cliente)
    setForm({
      nombre: cliente.nombre || "",
      apellido: cliente.apellido || "",
      dni: String(cliente.dni ?? ""),
      email: cliente.email || "",
      telefono: cliente.telefono || "",
      direccion: cliente.direccion || "",
      activo: cliente.activo ?? true,
    })
    setFormError("")
    setShowModal(true)
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setSubmitting(true)
    setFormError("")

    const payload = {
      nombre: form.nombre.trim(),
      apellido: form.apellido.trim(),
      dni: form.dni ? Number(form.dni) : null,
      email: form.email.trim() || null,
      telefono: form.telefono.trim() || null,
      direccion: form.direccion.trim() || null,
      activo: form.activo,
    }

    try {
      if (editing) {
        await updateCliente(editing.id, payload)
      } else {
        await createCliente(payload)
      }
      setShowModal(false)
    } catch (err) {
      const msg =
        err.response?.data?.mensaje ||
        err.response?.data?.message ||
        err.message ||
        "Error al guardar"
      setFormError(msg)
    } finally {
      setSubmitting(false)
    }
  }

  const handleDelete = async (cliente) => {
    if (
      !window.confirm(
        `¿Eliminar el cliente "${cliente.nombre} ${cliente.apellido}"? Esta acción no se puede deshacer.`
      )
    ) {
      return
    }
    try {
      await deleteCliente(cliente.id)
    } catch (err) {
      const msg =
        err.response?.data?.mensaje ||
        err.response?.data?.message ||
        err.message ||
        "Error al eliminar"
      alert(msg)
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col items-start justify-between gap-4 sm:flex-row sm:items-center">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-slate-900">
            Clientes
          </h1>
          <p className="mt-1 text-sm text-slate-500">
            Gestioná los compradores del comercio
          </p>
        </div>

        {isAdmin && (
          <Button onClick={openCreate} className="gap-2">
            <PlusIcon className="h-4 w-4" />
            Nuevo cliente
          </Button>
        )}
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardDescription>Total</CardDescription>
            <UsersIcon className="h-5 w-5 text-slate-500" />
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold">{clientes.length}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardDescription>Activos</CardDescription>
            <Badge variant="secondary">activos</Badge>
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold text-emerald-600">
              {activos}
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardDescription>Inactivos</CardDescription>
            <Badge variant="destructive">inactivos</Badge>
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold text-slate-500">
              {clientes.length - activos}
            </div>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Listado</CardTitle>
          <CardDescription>
            Buscá, filtrá por estado y ordená el listado
          </CardDescription>

          <div className="mt-3 grid grid-cols-1 gap-2 md:grid-cols-[1fr_180px_220px]">
            <div className="relative">
              <SearchIcon className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
              <Input
                type="search"
                placeholder="Buscar cliente..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                className="pl-9"
              />
            </div>

            <select
              value={estadoFilter}
              onChange={(e) => setEstadoFilter(e.target.value)}
              className="h-8 w-full rounded-lg border border-slate-200 bg-white px-2.5 text-sm outline-none focus:border-slate-400"
              title="Filtrar por estado"
            >
              <option value="todos">Todos los estados</option>
              <option value="activos">Solo activos</option>
              <option value="inactivos">Solo inactivos</option>
            </select>

            <select
              value={sortBy}
              onChange={(e) => setSortBy(e.target.value)}
              className="h-8 w-full rounded-lg border border-slate-200 bg-white px-2.5 text-sm outline-none focus:border-slate-400"
              title="Ordenar por"
            >
              <option value="nombre-asc">Nombre (A-Z)</option>
              <option value="nombre-desc">Nombre (Z-A)</option>
              <option value="apellido-asc">Apellido (A-Z)</option>
              <option value="apellido-desc">Apellido (Z-A)</option>
              <option value="dni-asc">DNI (menor a mayor)</option>
              <option value="dni-desc">DNI (mayor a menor)</option>
              <option value="fecha-desc">Registro más reciente</option>
              <option value="fecha-asc">Registro más antiguo</option>
            </select>
          </div>
        </CardHeader>
        <CardContent>
          {error && (
            <div className="mb-4 rounded-lg border border-rose-200 bg-rose-50 p-3 text-sm text-rose-700">
              {error}
            </div>
          )}

          {loading ? (
            <div className="space-y-3">
              {Array.from({ length: 5 }).map((_, i) => (
                <Skeleton key={i} className="h-10 w-full" />
              ))}
            </div>
          ) : filtered.length === 0 ? (
            <div className="py-12 text-center text-sm text-slate-500">
              {search
                ? "No se encontraron clientes que coincidan con la búsqueda."
                : "Aún no hay clientes registrados."}
            </div>
          ) : (
            <div className="overflow-x-auto">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Cliente</TableHead>
                    <TableHead>DNI</TableHead>
                    <TableHead>Contacto</TableHead>
                    <TableHead>Dirección</TableHead>
                    <TableHead className="text-center">Estado</TableHead>
                    {isAdmin && (
                      <TableHead className="text-right">Acciones</TableHead>
                    )}
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {paginated.map((cliente) => (
                    <TableRow key={cliente.id}>
                      <TableCell>
                        <div className="font-semibold text-slate-900">
                          {cliente.nombre} {cliente.apellido}
                        </div>
                      </TableCell>
                      <TableCell>
                        <div className="flex items-center gap-1.5 text-sm text-slate-600">
                          <IdCardIcon className="h-3.5 w-3.5" />
                          {cliente.dni}
                        </div>
                      </TableCell>
                      <TableCell>
                        <div className="space-y-1 text-sm text-slate-600">
                          {cliente.email && (
                            <div className="flex items-center gap-1.5">
                              <MailIcon className="h-3.5 w-3.5" />
                              <span className="truncate">{cliente.email}</span>
                            </div>
                          )}
                          {cliente.telefono && (
                            <div className="flex items-center gap-1.5">
                              <PhoneIcon className="h-3.5 w-3.5" />
                              {cliente.telefono}
                            </div>
                          )}
                          {!cliente.email && !cliente.telefono && (
                            <span className="text-slate-400">-</span>
                          )}
                        </div>
                      </TableCell>
                      <TableCell>
                        {cliente.direccion ? (
                          <div className="flex items-center gap-1.5 text-sm text-slate-600">
                            <MapPinIcon className="h-3.5 w-3.5" />
                            <span className="truncate">
                              {cliente.direccion}
                            </span>
                          </div>
                        ) : (
                          <span className="text-slate-400">-</span>
                        )}
                      </TableCell>
                      <TableCell className="text-center">
                        {cliente.activo ? (
                          <Badge className="bg-emerald-100 text-emerald-700">
                            Activo
                          </Badge>
                        ) : (
                          <Badge variant="secondary">Inactivo</Badge>
                        )}
                      </TableCell>
                      {isAdmin && (
                        <TableCell className="text-right">
                          <div className="flex justify-end gap-1">
                            <Button
                              variant="ghost"
                              size="icon-sm"
                              onClick={() => openEdit(cliente)}
                              title="Editar"
                            >
                              <PencilIcon className="h-4 w-4" />
                            </Button>
                            {isSuperAdmin && (
                              <Button
                                variant="ghost"
                                size="icon-sm"
                                onClick={() => handleDelete(cliente)}
                                title="Eliminar"
                                className="text-rose-600 hover:bg-rose-50 hover:text-rose-700"
                              >
                                <Trash2Icon className="h-4 w-4" />
                              </Button>
                            )}
                          </div>
                        </TableCell>
                      )}
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          )}

          {!loading && filtered.length > 0 && (
            <PaginationBar
              page={safePage}
              pageSize={pageSize}
              totalItems={totalItems}
              onPageChange={setPage}
              onPageSizeChange={setPageSize}
            />
          )}
        </CardContent>
      </Card>

      <Dialog open={showModal} onOpenChange={setShowModal}>
        <DialogContent className="sm:max-w-lg">
          <DialogHeader>
            <DialogTitle>
              {editing ? "Editar cliente" : "Nuevo cliente"}
            </DialogTitle>
            <DialogDescription>
              {editing
                ? "Modificá los datos del cliente y guardá los cambios."
                : "Completá los datos para registrar un nuevo cliente."}
            </DialogDescription>
          </DialogHeader>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="grid grid-cols-2 gap-3">
              <Field label="Nombre" required>
                <Input
                  value={form.nombre}
                  onChange={(e) =>
                    setForm({ ...form, nombre: e.target.value })
                  }
                  required
                  minLength={2}
                  maxLength={60}
                />
              </Field>
              <Field label="Apellido" required>
                <Input
                  value={form.apellido}
                  onChange={(e) =>
                    setForm({ ...form, apellido: e.target.value })
                  }
                  required
                  minLength={2}
                  maxLength={60}
                />
              </Field>
            </div>

            <Field label="DNI" required>
              <Input
                type="number"
                value={form.dni}
                onChange={(e) => setForm({ ...form, dni: e.target.value })}
                required
              />
            </Field>

            <Field label="Email">
              <Input
                type="email"
                value={form.email}
                onChange={(e) => setForm({ ...form, email: e.target.value })}
                placeholder="cliente@example.com"
              />
            </Field>

            <div className="grid grid-cols-2 gap-3">
              <Field label="Teléfono">
                <Input
                  value={form.telefono}
                  onChange={(e) =>
                    setForm({ ...form, telefono: e.target.value })
                  }
                  placeholder="11-2345-6789"
                />
              </Field>
              <Field label="Estado">
                <select
                  value={form.activo ? "1" : "0"}
                  onChange={(e) =>
                    setForm({ ...form, activo: e.target.value === "1" })
                  }
                  className="h-8 w-full rounded-lg border border-slate-200 bg-white px-2.5 text-sm outline-none focus:border-slate-400"
                >
                  <option value="1">Activo</option>
                  <option value="0">Inactivo</option>
                </select>
              </Field>
            </div>

            <Field label="Dirección">
              <Input
                value={form.direccion}
                onChange={(e) =>
                  setForm({ ...form, direccion: e.target.value })
                }
                placeholder="Calle, número, ciudad"
              />
            </Field>

            {formError && (
              <div className="rounded-lg border border-rose-200 bg-rose-50 p-3 text-sm text-rose-700">
                {formError}
              </div>
            )}

            <DialogFooter>
              <Button
                type="button"
                variant="outline"
                onClick={() => setShowModal(false)}
                disabled={submitting}
              >
                Cancelar
              </Button>
              <Button type="submit" disabled={submitting}>
                {submitting
                  ? "Guardando..."
                  : editing
                    ? "Actualizar"
                    : "Crear"}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  )
}
