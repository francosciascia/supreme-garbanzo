import { useState, useEffect } from 'react';
import api from './api';

export const useProducts = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

const fetchProducts = async () => {
  setLoading(true);

  try {
    const response = await api.get('/productos');

    const productos = Array.isArray(response.data)
      ? response.data
      : response.data.content || response.data.data || [];

    setProducts(productos);
    setError(null);

  } catch (err) {
    console.error('Error fetching products:', err);

    const errorMsg =
      err.response?.data?.message ||
      err.message ||
      'Error cargando productos';

    setError(errorMsg);
    setProducts([]);

  } finally {
    setLoading(false);
  }
};

  const createProduct = async (productData) => {
    try {
      const response = await api.post('/productos', productData);
      setProducts([...products, response.data]);
      return response.data;
    } catch (err) {
      setError(err.message);
      throw err;
    }
  };

  const updateProduct = async (id, productData) => {
    try {
      const response = await api.put(`/productos/${id}`, productData);
      setProducts(products.map(p => p.id === id ? response.data : p));
      return response.data;
    } catch (err) {
      setError(err.message);
      throw err;
    }
  };

  const deleteProduct = async (id) => {
    try {
      await api.delete(`/productos/${id}`);
      setProducts(products.filter(p => p.id !== id));
    } catch (err) {
      setError(err.message);
      throw err;
    }
  };

  useEffect(() => {
    fetchProducts();
  }, []);

  return {
    products,
    loading,
    error,
    fetchProducts,
    createProduct,
    updateProduct,
    deleteProduct
  };
};
