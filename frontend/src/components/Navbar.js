import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import './Navbar.css';

const Navbar = ({ user, onLogout }) => {
  const location = useLocation();

  const getRolIcon = (rol) => {
    switch(rol) {
      case 'SUPER_ADMIN':
        return '👑';
      case 'ADMIN':
        return '🔑';
      case 'USUARIO':
        return '👤';
      default:
        return '👤';
    }
  };

  const getRolLabel = (rol) => {
    switch(rol) {
      case 'SUPER_ADMIN':
        return 'Super Admin';
      case 'ADMIN':
        return 'Administrador';
      case 'USUARIO':
        return 'Usuario';
      default:
        return 'Usuario';
    }
  };

  return (
    <nav className="navbar">
      <div className="nav-container">
        <h1 className="nav-title">
          <i className="fas fa-store"></i>
          Gestión Comercial
        </h1>
        <ul className="nav-menu">
          <li>
            <Link to="/" className={location.pathname === '/' ? 'active' : ''}>
              <i className="fas fa-tachometer-alt"></i> Dashboard
            </Link>
          </li>
          <li>
            <Link to="/productos" className={location.pathname === '/productos' ? 'active' : ''}>
              <i className="fas fa-box"></i> Productos
            </Link>
          </li>
          <li>
            <Link to="/ventas" className={location.pathname === '/ventas' ? 'active' : ''}>
              <i className="fas fa-shopping-cart"></i> Ventas
            </Link>
          </li>
        </ul>

        <div className="nav-user">
          {user && (
            <>
              <div className="user-info">
                <span className="user-role-icon">{getRolIcon(user.rol)}</span>
                <div className="user-details">
                  <div className="user-name">{user.nombre} {user.apellido}</div>
                  <div className="user-role">{getRolLabel(user.rol)}</div>
                </div>
              </div>
              <button className="btn-logout" onClick={onLogout}>
                <i className="fas fa-sign-out-alt"></i> Cerrar Sesión
              </button>
            </>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
