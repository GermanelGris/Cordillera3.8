import { Link, useNavigate } from 'react-router-dom'
import Navbar from '../components/Navbar'
import { useAuth } from '../context/AuthContext'
import '../styles/home.css'

const PRODUCTOS = [
  { id: 1, nombre: 'Notebook Premium 15"', precio: 899990, categoria: 'Tecnología', img: '💻', stock: 12 },
  { id: 2, nombre: 'Monitor UHD 27"',     precio: 349990, categoria: 'Tecnología', img: '🖥️', stock: 8  },
  { id: 3, nombre: 'Teclado Mecánico',    precio: 89990,  categoria: 'Accesorios', img: '⌨️', stock: 25 },
  { id: 4, nombre: 'Mouse Inalámbrico',   precio: 49990,  categoria: 'Accesorios', img: '🖱️', stock: 30 },
  { id: 5, nombre: 'Tablet Pro 11"',      precio: 599990, categoria: 'Tecnología', img: '📱', stock: 5  },
  { id: 6, nombre: 'Auriculares BT Pro',  precio: 129990, categoria: 'Audio',      img: '🎧', stock: 18 },
]

const CATEGORIAS = [
  { nombre: 'Tecnología', icon: '💻' },
  { nombre: 'Accesorios', icon: '🖱️' },
  { nombre: 'Audio',      icon: '🎧' },
  { nombre: 'Oficina',    icon: '📁' },
]

export default function Home() {
  const { user } = useAuth()
  const navigate = useNavigate()

  const getDashboardLink = () => {
    if (!user) return '/login'
    if (user.rol === 'ADMIN')    return '/admin/kpi'
    if (user.rol === 'VENDEDOR') return '/vendedor/pos'
    return '/tienda'
  }

  const dashboardLabel = () => {
    if (!user) return ''
    if (user.rol === 'ADMIN')    return '📊 Ir al Dashboard'
    if (user.rol === 'VENDEDOR') return '🏪 Ir al POS'
    return '🛒 Mi cuenta'
  }

  return (
    <div className="page-wrapper">
      <Navbar />

      {/* ── Hero ──────────────────────────────── */}
      <section className="hero">
        <div className="hero-content">
          <span className="hero-badge">🏔️ Grupo Cordillera</span>
          <h1>La mejor tecnología,<br />al mejor precio</h1>
          <p>Descubre nuestra selección exclusiva de productos tecnológicos con garantía y soporte profesional.</p>
          <div className="hero-actions">
            <Link to="/tienda" className="btn btn-gold hero-btn">
              🛒 Explorar tienda
            </Link>
            {!user && (
              <Link to="/registro" className="btn btn-outline-hero">
                Crear cuenta gratis
              </Link>
            )}
            {user && (
              <Link to={getDashboardLink()} className="btn btn-outline-hero">
                {dashboardLabel()}
              </Link>
            )}
          </div>
        </div>
        <div className="hero-visual">
          <div className="hero-circle">
            <span className="hero-emoji">🖥️</span>
          </div>
        </div>
      </section>

      {/* ── Stats ─────────────────────────────── */}
      <section className="stats-bar">
        <div className="stat-item"><strong>+5.000</strong><span>Clientes satisfechos</span></div>
        <div className="stat-item"><strong>+200</strong><span>Productos disponibles</span></div>
        <div className="stat-item"><strong>24/7</strong><span>Soporte técnico</span></div>
        <div className="stat-item"><strong>48h</strong><span>Despacho express</span></div>
      </section>

      {/* ── Categorías ────────────────────────── */}
      <section className="section">
        <div className="section-inner">
          <h2 className="section-title">Categorías</h2>
          <div className="cat-grid">
            {CATEGORIAS.map(cat => (
              <button key={cat.nombre} type="button" className="cat-card"
                onClick={() => navigate(`/tienda?cat=${encodeURIComponent(cat.nombre)}`)}>
                <span className="cat-icon">{cat.icon}</span>
                <span className="cat-name">{cat.nombre}</span>
              </button>
            ))}
          </div>
        </div>
      </section>

      {/* ── Productos destacados ───────────────── */}
      <section className="section section-gray">
        <div className="section-inner">
          <div className="section-header">
            <h2 className="section-title">Productos destacados</h2>
            <Link to="/tienda" className="btn btn-outline">Ver todos →</Link>
          </div>
          <div className="products-grid">
            {PRODUCTOS.map(p => (
              <div key={p.id} className="product-card">
                <div className="product-img">{p.img}</div>
                <span className="product-cat">{p.categoria}</span>
                <h3 className="product-name">{p.nombre}</h3>
                <p className="product-price">$ {p.precio.toLocaleString('es-CL')}</p>
                <div className="product-footer">
                  <span className="stock-badge">✓ {p.stock} en stock</span>
                  <Link to={user ? '/tienda' : '/login'} className="btn btn-primary btn-sm">
                    Agregar 🛒
                  </Link>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ── CTA ───────────────────────────────── */}
      {!user && (
        <section className="cta-section">
          <h2>¿Eres vendedor o administrador?</h2>
          <p>Accede a nuestro sistema de gestión con tu cuenta corporativa</p>
          <Link to="/login" className="btn btn-gold">Iniciar sesión</Link>
        </section>
      )}

      <footer>
        <p>© 2026 Grupo Cordillera · Todos los derechos reservados</p>
      </footer>
    </div>
  )
}
