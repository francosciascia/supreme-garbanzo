import React from 'react';
import { useProducts } from '../hooks/useProducts';
import { useSales } from '../hooks/useSales';
import './Dashboard.css';

const Dashboard = () => {
  const { products, loading: productsLoading } = useProducts();
  const { sales, loading: salesLoading } = useSales();

  const totalProducts = products.length;
  const totalSales = sales.length;
  const totalRevenue = sales.reduce((sum, sale) => sum + sale.total, 0);
  const lowStockProducts = products.filter(p => p.stock < 10).length;

  if (productsLoading || salesLoading) {
    return <div className="loading">Cargando dashboard...</div>;
  }

  return (
    <div className="dashboard">
      <h1>Dashboard</h1>

      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon">
            <i className="fas fa-box"></i>
          </div>
          <div className="stat-content">
            <h3>{totalProducts}</h3>
            <p>Productos Totales</p>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon">
            <i className="fas fa-shopping-cart"></i>
          </div>
          <div className="stat-content">
            <h3>{totalSales}</h3>
            <p>Ventas Realizadas</p>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon">
            <i className="fas fa-dollar-sign"></i>
          </div>
          <div className="stat-content">
            <h3>${totalRevenue.toFixed(2)}</h3>
            <p>Ingresos Totales</p>
          </div>
        </div>

        <div className="stat-card warning">
          <div className="stat-icon">
            <i className="fas fa-exclamation-triangle"></i>
          </div>
          <div className="stat-content">
            <h3>{lowStockProducts}</h3>
            <p>Productos con Stock Bajo</p>
          </div>
        </div>
      </div>

      <div className="recent-activity">
        <h2>Actividad Reciente</h2>
        <div className="activity-list">
          {sales.slice(-5).reverse().map(sale => (
            <div key={sale.id} className="activity-item">
              <div className="activity-icon">
                <i className="fas fa-shopping-cart"></i>
              </div>
              <div className="activity-content">
                <p>Venta #{sale.id}</p>
                <small>{new Date(sale.fecha).toLocaleDateString()}</small>
              </div>
              <div className="activity-amount">
                ${sale.total.toFixed(2)}
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
