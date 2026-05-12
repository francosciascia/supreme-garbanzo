import React from 'react';
import { Navigate } from 'react-router-dom';

function ProtectedRoute({ children, requiredRol = null }) {
    const token = localStorage.getItem('token');
    const user = JSON.parse(localStorage.getItem('user') || '{}');

    if (!token) {
        return <Navigate to="/login" replace />;
    }

    if (requiredRol && user.rol !== requiredRol) {
        return <Navigate to="/unauthorized" replace />;
    }

    return children;
}

export default ProtectedRoute;

