import { useState, useEffect, useCallback } from 'react';
import api from './api';

export const useClientes = () => {
  const [clientes, setClientes] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchClientes = useCallback(async () => {
    setLoading(true);
    try {
      const response = await api.get('/clientes');
      const data = Array.isArray(response.data)
        ? response.data
        : response.data.content || response.data.data || [];
      setClientes(data);
      setError(null);
    } catch (err) {
      console.error('Error fetching clientes:', err);
      const msg =
        err.response?.data?.mensaje ||
        err.response?.data?.message ||
        err.message ||
        'Error cargando clientes';
      setError(msg);
      setClientes([]);
    } finally {
      setLoading(false);
    }
  }, []);

  const createCliente = async (data) => {
    const response = await api.post('/clientes', data);
    setClientes((prev) => [...prev, response.data]);
    return response.data;
  };

  const updateCliente = async (id, data) => {
    const response = await api.put(`/clientes/${id}`, data);
    setClientes((prev) => prev.map((c) => (c.id === id ? response.data : c)));
    return response.data;
  };

  const deleteCliente = async (id) => {
    await api.delete(`/clientes/${id}`);
    setClientes((prev) => prev.filter((c) => c.id !== id));
  };

  useEffect(() => {
    fetchClientes();
  }, [fetchClientes]);

  return {
    clientes,
    loading,
    error,
    fetchClientes,
    createCliente,
    updateCliente,
    deleteCliente,
  };
};
