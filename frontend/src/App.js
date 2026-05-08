import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import axios from 'axios';
import './App.css';
import Login from './components/Login';
import Navbar from './components/Navbar';
import Dashboard from './components/Dashboard';
import Productos from './components/Productos';
import Categorias from './components/Categorias';
import Ventas from './components/Ventas';
import ProtectedRoute from './components/ProtectedRoute';

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Verificar si hay token en localStorage al cargar
    const token = localStorage.getItem('token');
    const savedUser = localStorage.getItem('user');

    if (token && savedUser) {
      setUser(JSON.parse(savedUser));
      setIsAuthenticated(true);

      // Agregar token a los headers de axios
      axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    }

    setLoading(false);
  }, []);

  const handleLoginSuccess = (userData) => {
    setUser(userData);
    setIsAuthenticated(true);
    axios.defaults.headers.common['Authorization'] = `Bearer ${userData.token}`;
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    delete axios.defaults.headers.common['Authorization'];
    setUser(null);
    setIsAuthenticated(false);
  };

  if (loading) {
    return <div className="loading">Cargando...</div>;
  }

  if (!isAuthenticated) {
    return <Login onLoginSuccess={handleLoginSuccess} />;
  }

  return (
    <Router>
      <div className="App">
        <Navbar user={user} onLogout={handleLogout} />
        <main className="main-content">
           <Routes>
             <Route
               path="/"
               element={
                 <ProtectedRoute>
                   <Dashboard user={user} />
                 </ProtectedRoute>
               }
             />
             <Route
               path="/productos"
               element={
                 <ProtectedRoute>
                   <Productos user={user} />
                 </ProtectedRoute>
               }
             />
             <Route
               path="/categorias"
               element={
                 <ProtectedRoute>
                   <Categorias user={user} />
                 </ProtectedRoute>
               }
             />
             <Route
               path="/ventas"
               element={
                 <ProtectedRoute>
                   <Ventas user={user} />
                 </ProtectedRoute>
               }
             />
             <Route path="/unauthorized" element={<UnauthorizedPage />} />
             <Route path="*" element={<Navigate to="/" replace />} />
           </Routes>
        </main>
      </div>
    </Router>
  );
}

function UnauthorizedPage() {
  return (
    <div className="unauthorized-container">
      <h1>❌ Acceso Denegado</h1>
      <p>No tienes permiso para acceder a esta página.</p>
      <a href="/">Volver al Inicio</a>
    </div>
  );
}

export default App;
