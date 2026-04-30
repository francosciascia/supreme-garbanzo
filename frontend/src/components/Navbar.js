import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import './Navbar.css';

const Navbar = () => {
  const location = useLocation();

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
      </div>
    </nav>
  );
};

export default Navbar;
