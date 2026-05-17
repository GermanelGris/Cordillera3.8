import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/')
  }

  const renderLinks = () => {
    if (!user) return null
    if (user.rol === 'ADMIN') return (
      <>
        <Link to="/admin/kpi"      className="nav-link">📊 KPIs</Link>
        <Link to="/admin/reportes" className="nav-link">📋 Reportes</Link>
        <Link to="/tienda"         className="nav-link">🛒 Tienda</Link>
      </>
    )
    if (user.rol === 'VENDEDOR') return (
      <Link to="/vendedor/pos" className="nav-link">🏪 Punto de Venta</Link>
    )
    if (user.rol === 'USUARIO') return (
      <Link to="/tienda" className="nav-link">🛒 Tienda</Link>
    )
  }

  return (
    <nav className="navbar">
      <Link to="/" className="nav-brand">
        <span className="brand-icon">⛰️</span> Grupo Cordillera
      </Link>
      <div className="nav-links">
        {renderLinks()}
        {user ? (
          <div className="nav-user">
            <span className="nav-username">
              {user.rol === 'ADMIN' ? '👑' : user.rol === 'VENDEDOR' ? '🏷️' : '👤'} {user.nombre}
            </span>
            <button onClick={handleLogout} className="btn btn-outline-sm">Cerrar sesión</button>
          </div>
        ) : (
          <div className="nav-user">
            <Link to="/login"    className="btn btn-outline-sm">Iniciar sesión</Link>
            <Link to="/registro" className="btn btn-primary-sm">Registrarse</Link>
          </div>
        )}
      </div>
    </nav>
  )
}
