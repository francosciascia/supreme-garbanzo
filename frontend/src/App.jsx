import { useState, useEffect } from "react"
import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom"
import axios from "axios"

import "./App.css"
import Login from "./components/Login"
import Dashboard from "./components/Dashboard"
import Productos from "./components/Productos"
import Categorias from "./components/Categorias"
import Clientes from "./components/Clientes"
import Ventas from "./components/Ventas"
import ProtectedRoute from "./components/ProtectedRoute"
import { AppSidebar } from "./components/app-sidebar"

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const token = localStorage.getItem("token")
    const savedUser = localStorage.getItem("user")

    if (token && savedUser) {
      setUser(JSON.parse(savedUser))
      setIsAuthenticated(true)
      axios.defaults.headers.common["Authorization"] = `Bearer ${token}`
    }

    setLoading(false)
  }, [])

  const handleLoginSuccess = (userData) => {
    setUser(userData)
    setIsAuthenticated(true)
    axios.defaults.headers.common["Authorization"] = `Bearer ${userData.token}`
  }

  const handleLogout = () => {
    localStorage.removeItem("token")
    localStorage.removeItem("user")
    delete axios.defaults.headers.common["Authorization"]
    setUser(null)
    setIsAuthenticated(false)
  }

  if (loading) {
    return <div className="loading">Cargando...</div>
  }

  if (!isAuthenticated) {
    return <Login onLoginSuccess={handleLoginSuccess} />
  }

  return (
    <Router>
      <div className="min-h-screen bg-slate-100">
        <AppSidebar onLogout={handleLogout} />

        <div style={{ marginLeft: "340px", minHeight: "100vh" }}>

          <main
            style={{
              width: "100%",
              paddingTop: "24px",
              paddingLeft: "0px",
              paddingRight: "60px",
              paddingBottom: "24px",
            }}
          >
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
                path="/clientes"
                element={
                  <ProtectedRoute>
                    <Clientes user={user} />
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

              <Route
                path="/unauthorized"
                element={<UnauthorizedPage />}
              />

              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </main>
        </div>
      </div>
    </Router>
  )
}

function UnauthorizedPage() {
  return (
    <div className="unauthorized-container">
      <h1>Acceso denegado</h1>
      <p>No tenés permiso para acceder a esta página.</p>
      <a href="/">Volver al inicio</a>
    </div>
  )
}

export default App