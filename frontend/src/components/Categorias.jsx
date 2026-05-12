import React, { useState, useEffect, useMemo } from 'react';
import { PlusIcon, PencilIcon, Trash2Icon, XIcon, SearchIcon } from 'lucide-react';
import api from '../hooks/api';
import PaginationBar from './PaginationBar';
import './Categorias.css';

const Categorias = ({ user }) => {
  const [categorias, setCategorias] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editingCategoria, setEditingCategoria] = useState(null);
  const [search, setSearch] = useState('');
  const [sortBy, setSortBy] = useState('nombre-asc');
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [formData, setFormData] = useState({
    nombre: '',
    descripcion: ''
  });

  const filteredCategorias = useMemo(() => {
    const q = search.trim().toLowerCase();

    const result = categorias.filter((c) => {
      if (!q) return true;
      const haystack = [c.nombre, c.descripcion]
        .filter(Boolean)
        .join(' ')
        .toLowerCase();
      return haystack.includes(q);
    });

    const collator = new Intl.Collator('es', { sensitivity: 'base' });
    const comparators = {
      'nombre-asc': (a, b) => collator.compare(a.nombre, b.nombre),
      'nombre-desc': (a, b) => collator.compare(b.nombre, a.nombre),
      'id-asc': (a, b) => Number(a.id) - Number(b.id),
      'id-desc': (a, b) => Number(b.id) - Number(a.id),
    };
    const cmp = comparators[sortBy] || comparators['nombre-asc'];
    return [...result].sort(cmp);
  }, [categorias, search, sortBy]);

  useEffect(() => {
    setPage(1);
  }, [search, sortBy, pageSize]);

  const totalItems = filteredCategorias.length;
  const totalPages = Math.max(1, Math.ceil(totalItems / pageSize));
  const safePage = Math.min(page, totalPages);
  const paginatedCategorias = useMemo(() => {
    const start = (safePage - 1) * pageSize;
    return filteredCategorias.slice(start, start + pageSize);
  }, [filteredCategorias, safePage, pageSize]);

  // Cargar categorías
  useEffect(() => {
    fetchCategorias();
  }, []);

  const fetchCategorias = async () => {
    try {
      setLoading(true);
      const response = await api.get('/categorias');
      setCategorias(response.data);
      setError('');
    } catch (err) {
      setError('Error al cargar categorías');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!formData.nombre.trim()) {
      alert('El nombre de la categoría es obligatorio');
      return;
    }

    try {
      if (editingCategoria) {
        // Actualizar
        await api.put(`/categorias/${editingCategoria.id}`, formData);
      } else {
        // Crear
        await api.post('/categorias', formData);
      }

      fetchCategorias();
      setShowModal(false);
      resetForm();
    } catch (err) {
      alert('Error al guardar categoría: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleEdit = (categoria) => {
    setEditingCategoria(categoria);
    setFormData({
      nombre: categoria.nombre,
      descripcion: categoria.descripcion || ''
    });
    setShowModal(true);
  };

  const handleDelete = async (id) => {
    if (window.confirm('¿Estás seguro de eliminar esta categoría?')) {
      try {
        await api.delete(`/categorias/${id}`);
        fetchCategorias();
      } catch (err) {
        alert('Error al eliminar categoría: ' + (err.response?.data?.message || err.message));
      }
    }
  };

  const resetForm = () => {
    setFormData({
      nombre: '',
      descripcion: ''
    });
    setEditingCategoria(null);
  };

  const openCreateModal = () => {
    resetForm();
    setShowModal(true);
  };

  if (loading) return <div className="loading">Cargando categorías...</div>;

  return (
    <div className="categorias">
      <div className="header">
        <h1>Gestión de Categorías</h1>
        <button className="btn-primary" onClick={openCreateModal}>
          <PlusIcon size={16} /> Nueva Categoría
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}

      <div className="filters-bar">
        <div className="filters-search">
          <SearchIcon size={16} className="filters-search-icon" />
          <input
            type="search"
            placeholder="Buscar categoría..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>

        <select
          value={sortBy}
          onChange={(e) => setSortBy(e.target.value)}
          className="filters-select"
          title="Ordenar por"
        >
          <option value="nombre-asc">Nombre (A-Z)</option>
          <option value="nombre-desc">Nombre (Z-A)</option>
          <option value="id-asc">Más antiguas primero</option>
          <option value="id-desc">Más recientes primero</option>
        </select>
      </div>

      <div className="categorias-table-container">
        <table className="categorias-table">
          <thead>
            <tr>
              <th>Nombre</th>
              <th>Descripción</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {filteredCategorias.length > 0 ? (
              paginatedCategorias.map(categoria => (
                <tr key={categoria.id}>
                  <td>
                    <span className="categoria-name">{categoria.nombre}</span>
                  </td>
                  <td>{categoria.descripcion || '-'}</td>
                  <td className="actions">
                    <button
                      className="btn-edit"
                      onClick={() => handleEdit(categoria)}
                      title="Editar"
                    >
                      <PencilIcon size={18} />
                    </button>
                    <button
                      className="btn-delete"
                      onClick={() => handleDelete(categoria.id)}
                      title="Eliminar"
                    >
                      <Trash2Icon size={18} />
                    </button>
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan="3" className="no-data">
                  {search
                    ? 'No se encontraron categorías que coincidan con la búsqueda'
                    : 'No hay categorías registradas'}
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {filteredCategorias.length > 0 && (
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
              <h2>{editingCategoria ? 'Editar Categoría' : 'Nueva Categoría'}</h2>
              <button className="modal-close" onClick={() => setShowModal(false)}>
                <XIcon size={20} />
              </button>
            </div>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label htmlFor="nombre">Nombre *</label>
                <input
                  id="nombre"
                  type="text"
                  value={formData.nombre}
                  onChange={(e) => setFormData({...formData, nombre: e.target.value})}
                  placeholder="Ej: Electrónica, Limpieza, Lácteos"
                  required
                />
              </div>
              <div className="form-group">
                <label htmlFor="descripcion">Descripción</label>
                <textarea
                  id="descripcion"
                  value={formData.descripcion}
                  onChange={(e) => setFormData({...formData, descripcion: e.target.value})}
                  placeholder="Describe brevemente esta categoría (opcional)"
                  rows="4"
                />
              </div>
              <div className="modal-actions">
                <button type="button" className="btn-secondary" onClick={() => setShowModal(false)}>
                  Cancelar
                </button>
                <button type="submit" className="btn-primary">
                  {editingCategoria ? 'Actualizar' : 'Crear'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Categorias;

