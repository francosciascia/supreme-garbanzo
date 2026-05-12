import React, { useState, useEffect, useMemo } from 'react';
import { PlusIcon, PencilIcon, Trash2Icon, XIcon, SearchIcon } from 'lucide-react';
import { useProducts } from '../hooks/useProducts';
import api from '../hooks/api';
import PaginationBar from './PaginationBar';
import './Productos.css';

const LOW_STOCK_THRESHOLD = 10;

const Productos = ({ user }) => {
  const { products, loading, error, createProduct, updateProduct, deleteProduct } = useProducts();
  const [showModal, setShowModal] = useState(false);
  const [editingProduct, setEditingProduct] = useState(null);
  const [categorias, setCategorias] = useState([]);
  const [selectedCategoria, setSelectedCategoria] = useState(null);
  const [search, setSearch] = useState('');
  const [stockFilter, setStockFilter] = useState('todos');
  const [sortBy, setSortBy] = useState('nombre-asc');
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(12);
  const [formData, setFormData] = useState({
    nombre: '',
    descripcion: '',
    stock: 0,
    vencimiento: false,
    costo: 0,
    precioVenta: 0,
    categoriaId: null
  });

  useEffect(() => {
    const fetchCategorias = async () => {
      try {
        const response = await api.get('/categorias');
        setCategorias(response.data);
      } catch (err) {
        console.error('Error cargando categorías:', err);
      }
    };
    fetchCategorias();
  }, []);

  const filteredProducts = useMemo(() => {
    const q = search.trim().toLowerCase();

    let result = products.filter((p) => {
      if (selectedCategoria && (!p.categoria || p.categoria.id !== selectedCategoria)) {
        return false;
      }
      if (stockFilter === 'sin-stock' && Number(p.stock) > 0) return false;
      if (stockFilter === 'stock-bajo' && Number(p.stock) >= LOW_STOCK_THRESHOLD) return false;
      if (stockFilter === 'con-stock' && Number(p.stock) <= 0) return false;
      if (!q) return true;
      const haystack = [p.nombre, p.descripcion, p.categoria?.nombre]
        .filter(Boolean)
        .join(' ')
        .toLowerCase();
      return haystack.includes(q);
    });

    const collator = new Intl.Collator('es', { sensitivity: 'base' });
    const comparators = {
      'nombre-asc': (a, b) => collator.compare(a.nombre, b.nombre),
      'nombre-desc': (a, b) => collator.compare(b.nombre, a.nombre),
      'precio-asc': (a, b) => Number(a.precioVenta) - Number(b.precioVenta),
      'precio-desc': (a, b) => Number(b.precioVenta) - Number(a.precioVenta),
      'stock-asc': (a, b) => Number(a.stock) - Number(b.stock),
      'stock-desc': (a, b) => Number(b.stock) - Number(a.stock),
    };
    const cmp = comparators[sortBy] || comparators['nombre-asc'];
    return [...result].sort(cmp);
  }, [products, selectedCategoria, search, stockFilter, sortBy]);

  useEffect(() => {
    setPage(1);
  }, [search, stockFilter, sortBy, selectedCategoria, pageSize]);

  const totalItems = filteredProducts.length;
  const totalPages = Math.max(1, Math.ceil(totalItems / pageSize));
  const safePage = Math.min(page, totalPages);
  const paginatedProducts = useMemo(() => {
    const start = (safePage - 1) * pageSize;
    return filteredProducts.slice(start, start + pageSize);
  }, [filteredProducts, safePage, pageSize]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editingProduct) {
        await updateProduct(editingProduct.id, formData);
      } else {
        await createProduct(formData);
      }
      setShowModal(false);
      resetForm();
    } catch (err) {
      alert('Error al guardar producto');
    }
  };

  const handleEdit = (product) => {
    setEditingProduct(product);
    setFormData({
      nombre: product.nombre,
      descripcion: product.descripcion,
      stock: product.stock,
      vencimiento: product.vencimiento,
      costo: product.costo,
      precioVenta: product.precioVenta,
      categoriaId: product.categoria?.id || null
    });
    setShowModal(true);
  };

  const handleDelete = async (id) => {
    if (window.confirm('¿Estás seguro de eliminar este producto?')) {
      try {
        await deleteProduct(id);
      } catch (err) {
        alert('Error al eliminar producto');
      }
    }
  };

  const resetForm = () => {
    setFormData({
      nombre: '',
      descripcion: '',
      stock: 0,
      vencimiento: false,
      costo: 0,
      precioVenta: 0,
      categoriaId: null
    });
    setEditingProduct(null);
  };

  const openCreateModal = () => {
    resetForm();
    setShowModal(true);
  };

  if (loading) return <div className="loading">Cargando productos...</div>;
  if (error) {
    return (
      <div className="error-container">
        <h2>Error eliminando productos</h2>
        <p>{error}</p>
        <button onClick={() => window.location.reload()}>Reintentar</button>
      </div>
    );
  }

  return (
    <div className="productos">
      <div className="header">
        <h1>Gestión de Productos</h1>
        <button className="btn-primary" onClick={openCreateModal}>
          <PlusIcon size={16} /> Nuevo Producto
        </button>
      </div>

      <div className="filters-bar">
        <div className="filters-search">
          <SearchIcon size={16} className="filters-search-icon" />
          <input
            type="search"
            placeholder="Buscar producto..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>

        <select
          value={stockFilter}
          onChange={(e) => setStockFilter(e.target.value)}
          className="filters-select"
          title="Filtrar por stock"
        >
          <option value="todos">Stock: todos</option>
          <option value="con-stock">Con stock</option>
          <option value="stock-bajo">Stock bajo ({'<'} {LOW_STOCK_THRESHOLD})</option>
          <option value="sin-stock">Sin stock</option>
        </select>

        <select
          value={sortBy}
          onChange={(e) => setSortBy(e.target.value)}
          className="filters-select"
          title="Ordenar por"
        >
          <option value="nombre-asc">Nombre (A-Z)</option>
          <option value="nombre-desc">Nombre (Z-A)</option>
          <option value="precio-asc">Precio (menor a mayor)</option>
          <option value="precio-desc">Precio (mayor a menor)</option>
          <option value="stock-asc">Stock (menor a mayor)</option>
          <option value="stock-desc">Stock (mayor a menor)</option>
        </select>
      </div>

      <div className="categoria-filter">
        <h3>Filtrar por Categoría:</h3>
        <div className="categoria-buttons">
          <button
            className={`categoria-btn ${!selectedCategoria ? 'active' : ''}`}
            onClick={() => setSelectedCategoria(null)}
          >
            Todas
          </button>
          {categorias.map(cat => (
            <button
              key={cat.id}
              className={`categoria-btn ${selectedCategoria === cat.id ? 'active' : ''}`}
              onClick={() => setSelectedCategoria(cat.id)}
            >
              {cat.nombre}
            </button>
          ))}
        </div>
      </div>

      <div className="products-grid">
        {filteredProducts.length > 0 ? (
          paginatedProducts.map(product => (
            <div key={product.id} className="product-card">
              <div className="product-header">
                <h3>{product.nombre}</h3>
                {product.categoria && (
                  <span className="categoria-badge">{product.categoria.nombre}</span>
                )}
                <div className="product-actions">
                  <button className="btn-edit" onClick={() => handleEdit(product)} title="Editar">
                    <PencilIcon size={18} />
                  </button>
                  <button className="btn-delete" onClick={() => handleDelete(product.id)} title="Eliminar">
                    <Trash2Icon size={18} />
                  </button>
                </div>
              </div>
              <p>{product.descripcion}</p>
              <div className="product-details">
                <span>Stock: {product.stock}</span>
                <span>Precio: ${product.precioVenta}</span>
              </div>
            </div>
          ))
        ) : (
          <div className="no-products">
            <p>No hay productos que coincidan con los filtros</p>
          </div>
        )}
      </div>

      {filteredProducts.length > 0 && (
        <PaginationBar
          page={safePage}
          pageSize={pageSize}
          totalItems={totalItems}
          onPageChange={setPage}
          onPageSizeChange={setPageSize}
          pageSizeOptions={[12, 24, 48, 96]}
        />
      )}

      {showModal && (
        <div className="modal-overlay">
          <div className="modal">
            <div className="modal-header">
              <h2>{editingProduct ? 'Editar Producto' : 'Nuevo Producto'}</h2>
              <button className="modal-close" onClick={() => setShowModal(false)}>
                <XIcon size={20} />
              </button>
            </div>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Nombre:</label>
                <input
                  type="text"
                  value={formData.nombre}
                  onChange={(e) => setFormData({...formData, nombre: e.target.value})}
                  required
                />
              </div>
              <div className="form-group">
                <label>Descripción:</label>
                <textarea
                  value={formData.descripcion}
                  onChange={(e) => setFormData({...formData, descripcion: e.target.value})}
                />
              </div>
              <div className="form-group">
                <label>Categoría:</label>
                <select
                  value={formData.categoriaId || ''}
                  onChange={(e) => setFormData({...formData, categoriaId: e.target.value ? parseInt(e.target.value) : null})}
                >
                  <option value="">Sin categoría</option>
                  {categorias.map(cat => (
                    <option key={cat.id} value={cat.id}>
                      {cat.nombre}
                    </option>
                  ))}
                </select>
              </div>
              <div className="form-group">
                <label>Stock:</label>
                <input
                  type="number"
                  value={formData.stock}
                  onChange={(e) => setFormData({...formData, stock: parseInt(e.target.value)})}
                  min="0"
                  required
                />
              </div>
              <div className="form-group">
                <label>
                  <input
                    type="checkbox"
                    checked={formData.vencimiento}
                    onChange={(e) => setFormData({...formData, vencimiento: e.target.checked})}
                  />
                  Producto con vencimiento
                </label>
              </div>
              <div className="form-group">
                <label>Costo:</label>
                <input
                  type="number"
                  step="0.01"
                  value={formData.costo}
                  onChange={(e) => setFormData({...formData, costo: parseFloat(e.target.value)})}
                  min="0"
                  required
                />
              </div>
              <div className="form-group">
                <label>Precio de Venta:</label>
                <input
                  type="number"
                  step="0.01"
                  value={formData.precioVenta}
                  onChange={(e) => setFormData({...formData, precioVenta: parseFloat(e.target.value)})}
                  min="0"
                  required
                />
              </div>
              <div className="modal-actions">
                <button type="button" className="btn-secondary" onClick={() => setShowModal(false)}>
                  Cancelar
                </button>
                <button type="submit" className="btn-primary">
                  {editingProduct ? 'Actualizar' : 'Crear'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Productos;
