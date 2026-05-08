import React, { useState } from 'react';
import axios from 'axios';
import './Login.css';

function Login({ onLoginSuccess }) {
    const [isLogin, setIsLogin] = useState(true);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const [loginForm, setLoginForm] = useState({
        email: '',
        contraseña: ''
    });

    const [registerForm, setRegisterForm] = useState({
        email: '',
        contraseña: '',
        nombre: '',
        apellido: '',
        edad: '',
        dni: '',
        direccion: ''
    });

    const handleLoginChange = (e) => {
        const { name, value } = e.target;
        setLoginForm(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleRegisterChange = (e) => {
        const { name, value } = e.target;
        setRegisterForm(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleLogin = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        try {
            const response = await axios.post('http://localhost:8083/api/auth/login', loginForm);
            localStorage.setItem('token', response.data.token);
            localStorage.setItem('user', JSON.stringify(response.data));
            onLoginSuccess(response.data);
        } catch (err) {
            setError(err.response?.data?.mensaje || 'Error al iniciar sesión');
        } finally {
            setLoading(false);
        }
    };

    const handleRegister = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        try {
            const response = await axios.post('/api/auth/register', {
                ...registerForm,
                edad: parseInt(registerForm.edad),
                dni: parseInt(registerForm.dni)
            });
            localStorage.setItem('token', response.data.token);
            localStorage.setItem('user', JSON.stringify(response.data));
            onLoginSuccess(response.data);
        } catch (err) {
            setError(err.response?.data?.mensaje || 'Error al registrarse');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="login-container">
            <div className="login-box">
                <div className="login-header">
                    <h1>
                        <i className="fas fa-store"></i>
                        Gestión Comercial
                    </h1>
                    <p>Sistema de Administración de Ventas</p>
                </div>

                {error && <div className="error-message">{error}</div>}

                {isLogin ? (
                    // Formulario de Login
                    <form onSubmit={handleLogin} className="login-form">
                        <h2>Iniciar Sesión</h2>

                        <div className="form-group">
                            <label htmlFor="login-email">
                                <i className="fas fa-envelope"></i> Email
                            </label>
                            <input
                                type="email"
                                id="login-email"
                                name="email"
                                value={loginForm.email}
                                onChange={handleLoginChange}
                                placeholder="tu@email.com"
                                required
                            />
                        </div>

                        <div className="form-group">
                            <label htmlFor="login-password">
                                <i className="fas fa-lock"></i> Contraseña
                            </label>
                            <input
                                type="password"
                                id="login-password"
                                name="contraseña"
                                value={loginForm.contraseña}
                                onChange={handleLoginChange}
                                placeholder="Tu contraseña"
                                required
                            />
                        </div>

                        <button type="submit" className="btn-login" disabled={loading}>
                            {loading ? 'Cargando...' : 'Iniciar Sesión'}
                        </button>

                        <div className="login-footer">
                            <p>¿No tienes cuenta?
                                <button
                                    type="button"
                                    onClick={() => setIsLogin(false)}
                                    className="toggle-link"
                                >
                                    Regístrate aquí
                                </button>
                            </p>
                        </div>
                    </form>
                ) : (
                    // Formulario de Registro
                    <form onSubmit={handleRegister} className="login-form">
                        <h2>Crear Cuenta</h2>

                        <div className="form-row">
                            <div className="form-group">
                                <label htmlFor="register-nombre">Nombre</label>
                                <input
                                    type="text"
                                    id="register-nombre"
                                    name="nombre"
                                    value={registerForm.nombre}
                                    onChange={handleRegisterChange}
                                    placeholder="Tu nombre"
                                    required
                                />
                            </div>

                            <div className="form-group">
                                <label htmlFor="register-apellido">Apellido</label>
                                <input
                                    type="text"
                                    id="register-apellido"
                                    name="apellido"
                                    value={registerForm.apellido}
                                    onChange={handleRegisterChange}
                                    placeholder="Tu apellido"
                                    required
                                />
                            </div>
                        </div>

                        <div className="form-group">
                            <label htmlFor="register-email">Email</label>
                            <input
                                type="email"
                                id="register-email"
                                name="email"
                                value={registerForm.email}
                                onChange={handleRegisterChange}
                                placeholder="tu@email.com"
                                required
                            />
                        </div>

                        <div className="form-row">
                            <div className="form-group">
                                <label htmlFor="register-edad">Edad</label>
                                <input
                                    type="number"
                                    id="register-edad"
                                    name="edad"
                                    value={registerForm.edad}
                                    onChange={handleRegisterChange}
                                    placeholder="18"
                                    min="18"
                                    max="100"
                                    required
                                />
                            </div>

                            <div className="form-group">
                                <label htmlFor="register-dni">DNI</label>
                                <input
                                    type="number"
                                    id="register-dni"
                                    name="dni"
                                    value={registerForm.dni}
                                    onChange={handleRegisterChange}
                                    placeholder="TU DNI"
                                    required
                                />
                            </div>
                        </div>

                        <div className="form-group">
                            <label htmlFor="register-direccion">Dirección</label>
                            <input
                                type="text"
                                id="register-direccion"
                                name="direccion"
                                value={registerForm.direccion}
                                onChange={handleRegisterChange}
                                placeholder="Tu dirección (opcional)"
                            />
                        </div>

                        <div className="form-group">
                            <label htmlFor="register-password">Contraseña</label>
                            <input
                                type="password"
                                id="register-password"
                                name="contraseña"
                                value={registerForm.contraseña}
                                onChange={handleRegisterChange}
                                placeholder="Mínimo 6 caracteres"
                                minLength="6"
                                required
                            />
                        </div>

                        <button type="submit" className="btn-login" disabled={loading}>
                            {loading ? 'Cargando...' : 'Crear Cuenta'}
                        </button>

                        <div className="login-footer">
                            <p>¿Ya tienes cuenta?
                                <button
                                    type="button"
                                    onClick={() => setIsLogin(true)}
                                    className="toggle-link"
                                >
                                    Inicia sesión aquí
                                </button>
                            </p>
                        </div>
                    </form>
                )}

                <div className="demo-info">
                    <h4>📋 Cuentas de Demo</h4>
                    <div className="demo-account">
                        <strong>Usuario Común:</strong>
                        <small>usuario@demo.com / 123456</small>
                    </div>
                    <div className="demo-account">
                        <strong>Admin:</strong>
                        <small>admin@demo.com / 123456</small>
                    </div>
                    <div className="demo-account">
                        <strong>Super Admin:</strong>
                        <small>superadmin@demo.com / 123456</small>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default Login;

