import React, { useEffect, useMemo, useState } from 'react';
import { PlusIcon, Trash2Icon, XIcon, SearchIcon } from 'lucide-react';
import { useSales } from '../hooks/useSales';
import { useProducts } from '../hooks/useProducts';
import { useClientes } from '../hooks/useClientes';
import PaginationBar from './PaginationBar';
import './Ventas.css';

const Ventas = ({ user }) => {
  const { sales, loading, error, createSale } = useSales();
  const { products } = useProducts();
  const { clientes } = useClientes();
  const [showModal, setShowModal] = useState(false);
  const [saleItems, setSaleItems] = useState([]);
  const [clienteId, setClienteId] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState('');

  const [search, setSearch] = useState('');
  const [clienteFilter, setClienteFilter] = useState('todos');
  const [fechaFilter, setFechaFilter] = useState('todas');
  const [sortBy, setSortBy] = useState('fecha-desc');
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);

  const filteredSales = useMemo(() => {
    const q = search.trim().toLowerCase();

    const now = new Date();
    const startOfDay = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const last7 = new Date(startOfDay);
    last7.setDate(last7.getDate() - 6);
    const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);

    let result = sales.filter((s) => {
      if (clienteFilter === 'sin' && s.cliente) return false;
      if (clienteFilter === 'con' && !s.cliente) return false;
      if (
        clienteFilter !== 'todos' &&
        clienteFilter !== 'sin' &&
        clienteFilter !== 'con' &&
        String(s.cliente?.id ?? '') !== clienteFilter
      ) {
        return false;
      }

      if (fechaFilter !== 'todas') {
        const fecha = s.fecha ? new Date(s.fecha) : null;
        if (!fecha) return false;
        if (fechaFilter === 'hoy' && fecha < startOfDay) return false;
        if (fechaFilter === '7dias' && fecha < last7) return false;
        if (fechaFilter === 'mes' && fecha < startOfMonth) return false;
      }

      if (!q) return true;
      const haystack = [
        String(s.id),
        s.cliente?.nombre,
        s.cliente?.apellido,
        String(s.cliente?.dni ?? ''),
        ...(s.items || []).map((i) => i.nombreProducto),
      ]
        .filter(Boolean)
        .join(' ')
        .toLowerCase();
      return haystack.includes(q);
    });

    const comparators = {
      'fecha-desc': (a, b) => new Date(b.fecha || 0) - new Date(a.fecha || 0),
      'fecha-asc': (a, b) => new Date(a.fecha || 0) - new Date(b.fecha || 0),
      'total-desc': (a, b) => Number(b.total || 0) - Number(a.total || 0),
      'total-asc': (a, b) => Number(a.total || 0) - Number(b.total || 0),
      'id-desc': (a, b) => Number(b.id) - Number(a.id),
      'id-asc': (a, b) => Number(a.id) - Number(b.id),
    };
    const cmp = comparators[sortBy] || comparators['fecha-desc'];
    return [...result].sort(cmp);
  }, [sales, search, clienteFilter, fechaFilter, sortBy]);

  useEffect(() => {
    setPage(1);
  }, [search, clienteFilter, fechaFilter, sortBy, pageSize]);

  const totalItems = filteredSales.length;
  const totalPages = Math.max(1, Math.ceil(totalItems / pageSize));
  const safePage = Math.min(page, totalPages);
  const paginatedSales = useMemo(() => {
    const start = (safePage - 1) * pageSize;
    return filteredSales.slice(start, start + pageSize);
  }, [filteredSales, safePage, pageSize]);

  const addItem = () => {
    setSaleItems([...saleItems, { productoId: '', cantidad: 1 }]);
  };

  const updateItem = (index, field, value) => {
    const newItems = [...saleItems];
    newItems[index][field] = value;
    setSaleItems(newItems);
  };

  const removeItem = (index) => {
    setSaleItems(saleItems.filter((_, i) => i !== index));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitError('');

    if (saleItems.length === 0) {
      setSubmitError('Agregue al menos un item a la venta');
      return;
    }

    const payload = {
      clienteId: clienteId ? parseInt(clienteId, 10) : null,
      items: saleItems.map((item) => ({
        productoId: parseInt(item.productoId, 10),
        cantidad: parseInt(item.cantidad, 10),
      })),
    };

    setSubmitting(true);
    try {
      await createSale(payload);
      setShowModal(false);
      setSaleItems([]);
      setClienteId('');
    } catch (err) {
      const msg =
        err.response?.data?.mensaje ||
        err.response?.data?.message ||
        err.message ||
        'Error al crear venta';
      setSubmitError(msg);
    } finally {
      setSubmitting(false);
    }
  };

  const calculateTotal = () => {
    return saleItems.reduce((total, item) => {
      const product = products.find((p) => p.id === parseInt(item.productoId, 10));
      return total + (product ? product.precioVenta * item.cantidad : 0);
    }, 0);
  };

  if (loading) return <div className="loading">Cargando ventas...</div>;
  if (error) {
    return (
      <div className="error-container">
        <h2>Error cargando ventas</h2>
        <p>{error}</p>
        <button onClick={() => window.location.reload()}>Reintentar</button>
      </div>
    );
  }

  return (
    <div className="ventas">
      <div className="header">
        <h1>Gestión de Ventas</h1>
        <button className="btn-primary" onClick={() => setShowModal(true)}>
          <PlusIcon size={16} /> Nueva Venta
        </button>
      </div>

      <div className="filters-bar filters-bar-4">
        <div className="filters-search">
          <SearchIcon size={16} className="filters-search-icon" />
          <input
            type="search"
            placeholder="Buscar por #venta, cliente, DNI o producto..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>

        <select
          value={clienteFilter}
          onChange={(e) => setClienteFilter(e.target.value)}
          className="filters-select"
          title="Filtrar por cliente"
        >
          <option value="todos">Cliente: todos</option>
          <option value="con">Con cliente asignado</option>
          <option value="sin">Sin cliente asignado</option>
          {clientes.map((c) => (
            <option key={c.id} value={c.id}>
              {c.nombre} {c.apellido}
            </option>
          ))}
        </select>

        <select
          value={fechaFilter}
          onChange={(e) => setFechaFilter(e.target.value)}
          className="filters-select"
          title="Filtrar por fecha"
        >
          <option value="todas">Período: todas</option>
          <option value="hoy">Hoy</option>
          <option value="7dias">Últimos 7 días</option>
          <option value="mes">Este mes</option>
        </select>

        <select
          value={sortBy}
          onChange={(e) => setSortBy(e.target.value)}
          className="filters-select"
          title="Ordenar por"
        >
          <option value="fecha-desc">Fecha (más reciente)</option>
          <option value="fecha-asc">Fecha (más antigua)</option>
          <option value="total-desc">Total (mayor a menor)</option>
          <option value="total-asc">Total (menor a mayor)</option>
          <option value="id-desc">N° venta (descendente)</option>
          <option value="id-asc">N° venta (ascendente)</option>
        </select>
      </div>

      <div className="sales-summary">
        Mostrando <strong>{filteredSales.length}</strong> de {sales.length} ventas
      </div>

      <div className="sales-list">
        {filteredSales.length === 0 && (
          <div className="no-data-empty">No se encontraron ventas con los filtros aplicados.</div>
        )}
        {paginatedSales.map((sale) => (
          <div key={sale.id} className="sale-card">
            <div className="sale-header">
              <h3>Venta #{sale.id}</h3>
              <span className="sale-date">
                {new Date(sale.fecha).toLocaleDateString()}
              </span>
            </div>

            {sale.cliente ? (
              <div className="sale-client">
                Cliente: {sale.cliente.nombre} {sale.cliente.apellido} ·
                DNI {sale.cliente.dni}
              </div>
            ) : (
              <div className="sale-client sale-client-empty">
                Sin cliente asignado
              </div>
            )}

            <div className="sale-total">
              Total: ${Number(sale.total || 0).toFixed(2)}
            </div>

            <div className="sale-items">
              {(sale.items || []).map((item, index) => (
                <div key={index} className="sale-item">
                  <span>{item.nombreProducto}</span>
                  <span>x{item.cantidad}</span>
                  <span>${Number(item.subtotal || 0).toFixed(2)}</span>
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>

      {filteredSales.length > 0 && (
        <PaginationBar
          page={safePage}
          pageSize={pageSize}
          totalItems={totalItems}
          onPageChange={setPage}
          onPageSizeChange={setPageSize}
        />
      )}

      {showModal && (
        <div className="modal-overlay">
          <div className="modal">
            <div className="modal-header">
              <h2>Nueva Venta</h2>
              <button
                className="modal-close"
                onClick={() => setShowModal(false)}
              >
                <XIcon size={20} />
              </button>
            </div>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Cliente (opcional)</label>
                <select
                  value={clienteId}
                  onChange={(e) => setClienteId(e.target.value)}
                >
                  <option value="">Sin cliente</option>
                  {clientes.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.nombre} {c.apellido} · DNI {c.dni}
                    </option>
                  ))}
                </select>
              </div>

              <div className="sale-items-section">
                <div className="section-header">
                  <h3>Items de la Venta</h3>
                  <button
                    type="button"
                    className="btn-secondary"
                    onClick={addItem}
                  >
                    <PlusIcon size={14} /> Agregar Item
                  </button>
                </div>

                {saleItems.map((item, index) => (
                  <div key={index} className="sale-item-form">
                    <select
                      value={item.productoId}
                      onChange={(e) =>
                        updateItem(index, 'productoId', e.target.value)
                      }
                      required
                    >
                      <option value="">Seleccionar producto</option>
                      {products.map((product) => (
                        <option key={product.id} value={product.id}>
                          {product.nombre} - ${product.precioVenta}
                        </option>
                      ))}
                    </select>
                    <input
                      type="number"
                      min="1"
                      value={item.cantidad}
                      onChange={(e) =>
                        updateItem(index, 'cantidad', e.target.value)
                      }
                      placeholder="Cantidad"
                      required
                    />
                    <button
                      type="button"
                      className="btn-delete"
                      onClick={() => removeItem(index)}
                      title="Quitar item"
                    >
                      <Trash2Icon size={16} />
                    </button>
                  </div>
                ))}
              </div>

              <div className="sale-total-section">
                <h3>Total: ${calculateTotal().toFixed(2)}</h3>
              </div>

              {submitError && (
                <div className="error-message">{submitError}</div>
              )}

              <div className="modal-actions">
                <button
                  type="button"
                  className="btn-secondary"
                  onClick={() => setShowModal(false)}
                  disabled={submitting}
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  className="btn-primary"
                  disabled={submitting}
                >
                  {submitting ? 'Creando...' : 'Crear Venta'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Ventas;
