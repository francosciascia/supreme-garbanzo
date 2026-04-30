import React, { useState } from 'react';
import { useProducts } from '../hooks/useProducts';
import './Productos.css';

const Productos = () => {
  const { products, loading, error, createProduct, updateProduct, deleteProduct } = useProducts();
  const [showModal, setShowModal] = useState(false);
  const [editingProduct, setEditingProduct] = useState(null);
  const [formData, setFormData] = useState({
    nombre: '',
    descripcion: '',
    stock: 0,
    vencimiento: false,
    costo: 0,
    precioVenta: 0
  });

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
      precioVenta: product.precioVenta
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
      precioVenta: 0
    });
    setEditingProduct(null);
  };

  const openCreateModal = () => {
    resetForm();
    setShowModal(true);
  };

  if (loading) return <div className="loading">Cargando productos...</div>;
  if (error) return <div className="error">Error: {error}</div>;

  return (
    <div className="productos">
      <div className="header">
        <h1>Gestión de Productos</h1>
        <button className="btn-primary" onClick={openCreateModal}>
          <i className="fas fa-plus"></i> Nuevo Producto
        </button>
      </div>

      <div className="products-grid">
        {products.map(product => (
          <div key={product.id} className="product-card">
            <div className="product-header">
              <h3>{product.nombre}</h3>
              <div className="product-actions">
                <button className="btn-edit" onClick={() => handleEdit(product)}>
                  <i className="fas fa-edit"></i>
                </button>
                <button className="btn-delete" onClick={() => handleDelete(product.id)}>
                  <i className="fas fa-trash"></i>
                </button>
              </div>
            </div>
            <p>{product.descripcion}</p>
            <div className="product-details">
              <span>Stock: {product.stock}</span>
              <span>Precio: ${product.precioVenta}</span>
            </div>
          </div>
        ))}
      </div>

      {showModal && (
        <div className="modal-overlay">
          <div className="modal">
            <div className="modal-header">
              <h2>{editingProduct ? 'Editar Producto' : 'Nuevo Producto'}</h2>
              <button className="modal-close" onClick={() => setShowModal(false)}>
                <i className="fas fa-times"></i>
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
