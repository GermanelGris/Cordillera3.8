import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [open, setOpen] = useState(false)

  const handleLogout = () => {
    logout()
    navigate('/')
    setOpen(false)
  }

  const close = () => setOpen(false)

  const renderLinks = () => {
    if (!user) return null
    if (user.rol === 'ADMIN') return (
      <>
        <Link to="/admin/usuarios"   className="nav-link" onClick={close}>👥 Usuarios</Link>
        <Link to="/admin/inventario" className="nav-link" onClick={close}>📦 Inventario</Link>
        <Link to="/admin/kpi"        className="nav-link" onClick={close}>📊 KPIs</Link>
        <Link to="/admin/reportes"   className="nav-link" onClick={close}>📋 Reportes</Link>
        <Link to="/tienda"           className="nav-link" onClick={close}>🛒 Tienda</Link>
      </>
    )
    if (user.rol === 'VENDEDOR') return (
      <Link to="/vendedor/pos" className="nav-link" onClick={close}>🏪 Punto de Venta</Link>
    )
    if (user.rol === 'USUARIO') return (
      <Link to="/tienda" className="nav-link" onClick={close}>🛒 Tienda</Link>
    )
    return null
  }

  return (
    <nav className="navbar">
      <Link to="/" className="nav-brand" onClick={close}>
        <span className="brand-icon">⛰️</span> Grupo Cordillera
      </Link>

      {/* Hamburger button — visible only on mobile */}
      <button
        type="button"
        className={`nav-hamburger${open ? ' open' : ''}`}
        onClick={() => setOpen(v => !v)}
        aria-label="Menú"
      >
        <span />
        <span />
        <span />
      </button>

      {/* Desktop: single row. Mobile: dropdown panel */}
      <div className={`nav-links${open ? ' mobile-open' : ''}`}>
        {renderLinks()}
      </div>

      <div className={`nav-user${open ? ' mobile-open' : ''}`}>
        {user ? (
          <>
            <span className="nav-username">
              {user.rol === 'ADMIN' ? '👑' : user.rol === 'VENDEDOR' ? '🏷️' : '👤'}{' '}
              {user.nombre}
            </span>
            <button onClick={handleLogout} className="btn-outline-sm">Cerrar sesión</button>
          </>
        ) : (
          <>
            <Link to="/login"    className="btn-outline-sm"  onClick={close}>Iniciar sesión</Link>
            <Link to="/registro" className="btn-primary-sm"  onClick={close}>Registrarse</Link>
          </>
        )}
      </div>
    </nav>
  )
}
