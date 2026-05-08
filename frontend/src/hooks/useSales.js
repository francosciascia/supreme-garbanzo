import { useState, useEffect } from 'react';
import api from './api';

export const useSales = () => {
  const [sales, setSales] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

const fetchSales = async () => {
  setLoading(true);

  try {
    const response = await api.get('/ventas');

    const ventas = Array.isArray(response.data)
      ? response.data
      : response.data.content || response.data.data || [];

    setSales(ventas);
    setError(null);

  } catch (err) {
    console.error('Error fetching sales:', err);

    const errorMsg =
      err.response?.data?.message ||
      err.message ||
      'Error cargando ventas';

    setError(errorMsg);
    setSales([]);

  } finally {
    setLoading(false);
  }
};

const createSale = async (saleData) => {
  try {
    const response = await api.post('/ventas', saleData);

    await fetchSales();

    return response.data;
  } catch (err) {
    console.error('Error creating sale:', err);
    setError(err.message);
    throw err;
  }
};

  useEffect(() => {
    fetchSales();
  }, []);

  return {
    sales,
    loading,
    error,
    fetchSales,
    createSale
  };
};
