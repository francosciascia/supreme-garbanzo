import React, { useState } from 'react';
import { useSales } from '../hooks/useSales';
import { useProducts } from '../hooks/useProducts';
import './Ventas.css';

const Ventas = () => {
  const { sales, loading, error, createSale } = useSales();
  const { products } = useProducts();
  const [showModal, setShowModal] = useState(false);
  const [saleItems, setSaleItems] = useState([]);

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
    if (saleItems.length === 0) {
      alert('Agregue al menos un item a la venta');
      return;
    }

    const items = saleItems.map(item => ({
      productoId: parseInt(item.productoId),
      cantidad: parseInt(item.cantidad)
    }));

    try {
      await createSale({ items });
      setShowModal(false);
      setSaleItems([]);
    } catch (err) {
      alert('Error al crear venta');
    }
  };

  const calculateTotal = () => {
    return saleItems.reduce((total, item) => {
      const product = products.find(p => p.id === parseInt(item.productoId));
      return total + (product ? product.precioVenta * item.cantidad : 0);
    }, 0);
  };

  if (loading) return <div className="loading">Cargando ventas...</div>;
  if (error) return <div className="error">Error: {error}</div>;

  return (
    <div className="ventas">
      <div className="header">
        <h1>Gestión de Ventas</h1>
        <button className="btn-primary" onClick={() => setShowModal(true)}>
          <i className="fas fa-plus"></i> Nueva Venta
        </button>
      </div>

      <div className="sales-list">
        {sales.map(sale => (
          <div key={sale.id} className="sale-card">
            <div className="sale-header">
              <h3>Venta #{sale.id}</h3>
              <span className="sale-date">{new Date(sale.fecha).toLocaleDateString()}</span>
            </div>
            <div className="sale-total">
              Total: ${sale.total.toFixed(2)}
            </div>
            <div className="sale-items">
              {sale.items.map((item, index) => (
                <div key={index} className="sale-item">
                  <span>{item.nombreProducto}</span>
                  <span>x{item.cantidad}</span>
                  <span>${item.subtotal.toFixed(2)}</span>
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>

      {showModal && (
        <div className="modal-overlay">
          <div className="modal">
            <div className="modal-header">
              <h2>Nueva Venta</h2>
              <button className="modal-close" onClick={() => setShowModal(false)}>
                <i className="fas fa-times"></i>
              </button>
            </div>
            <form onSubmit={handleSubmit}>
              <div className="sale-items-section">
                <div className="section-header">
                  <h3>Items de la Venta</h3>
                  <button type="button" className="btn-secondary" onClick={addItem}>
                    <i className="fas fa-plus"></i> Agregar Item
                  </button>
                </div>

                {saleItems.map((item, index) => (
                  <div key={index} className="sale-item-form">
                    <select
                      value={item.productoId}
                      onChange={(e) => updateItem(index, 'productoId', e.target.value)}
                      required
                    >
                      <option value="">Seleccionar producto</option>
                      {products.map(product => (
                        <option key={product.id} value={product.id}>
                          {product.nombre} - ${product.precioVenta}
                        </option>
                      ))}
                    </select>
                    <input
                      type="number"
                      min="1"
                      value={item.cantidad}
                      onChange={(e) => updateItem(index, 'cantidad', e.target.value)}
                      placeholder="Cantidad"
                      required
                    />
                    <button type="button" className="btn-delete" onClick={() => removeItem(index)}>
                      <i className="fas fa-trash"></i>
                    </button>
                  </div>
                ))}
              </div>

              <div className="sale-total-section">
                <h3>Total: ${calculateTotal().toFixed(2)}</h3>
              </div>

              <div className="modal-actions">
                <button type="button" className="btn-secondary" onClick={() => setShowModal(false)}>
                  Cancelar
                </button>
                <button type="submit" className="btn-primary">
                  Crear Venta
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
