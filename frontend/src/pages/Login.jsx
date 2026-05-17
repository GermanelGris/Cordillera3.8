import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [form, setForm]       = useState({ email: '', password: '' })
  const [error, setError]     = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError(''); setLoading(true)
    try {
      const rol = await login(form.email, form.password)
      if (rol === 'ADMIN')    navigate('/admin/kpi')
      else if (rol === 'VENDEDOR') navigate('/vendedor/pos')
      else navigate('/tienda')
    } catch {
      setError('Email o contraseña incorrectos')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-layout">
      <div className="auth-left">
        <div className="auth-brand">
          <span style={{ fontSize: '3rem' }}>⛰️</span>
          <h1>Grupo Cordillera</h1>
          <p>Sistema integrado de gestión empresarial</p>
        </div>
        <div className="auth-features">
          <div className="auth-feature"><span>📊</span><span>Dashboard KPI en tiempo real</span></div>
          <div className="auth-feature"><span>🏪</span><span>Punto de venta integrado</span></div>
          <div className="auth-feature"><span>📋</span><span>Reportes automáticos</span></div>
          <div className="auth-feature"><span>🛒</span><span>E-commerce unificado</span></div>
        </div>
      </div>

      <div className="auth-right">
        <div className="auth-card">
          <h2>Iniciar sesión</h2>
          <p className="auth-sub">Bienvenido de vuelta</p>

          {error && <div className="alert alert-error">{error}</div>}

          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label className="form-label">Email</label>
              <input type="email" className="form-input" placeholder="tu@cordillera.cl"
                value={form.email}
                onChange={e => setForm({...form, email: e.target.value})}
                required />
            </div>
            <div className="form-group">
              <label className="form-label">Contraseña</label>
              <input type="password" className="form-input" placeholder="Ingresa tu contraseña"
                value={form.password}
                onChange={e => setForm({...form, password: e.target.value})}
                required />
            </div>
            <button type="submit" className="btn btn-primary" style={{ width: '100%' }} disabled={loading}>
              {loading ? 'Ingresando...' : 'Iniciar sesión'}
            </button>
          </form>

          <div className="auth-divider"><span>usuarios de prueba</span></div>
          <div className="auth-hints">
            <div className="hint-chip hint-admin"
              onClick={() => setForm({ email: 'admin@cordillera.cl', password: 'Admin123!' })}>
              👑 admin@cordillera.cl / Admin123!
            </div>
            <div className="hint-chip hint-vendedor"
              onClick={() => setForm({ email: 'vendedor1@cordillera.cl', password: 'Admin123!' })}>
              🏷️ vendedor1@cordillera.cl / Admin123!
            </div>
            <div className="hint-chip hint-usuario"
              onClick={() => setForm({ email: 'cliente1@cordillera.cl', password: 'Admin123!' })}>
              👤 cliente1@cordillera.cl / Admin123!
            </div>
          </div>

          <p className="auth-footer">
            ¿No tienes cuenta? <Link to="/registro">Regístrate aquí</Link>
          </p>
          <p className="auth-footer">
            <Link to="/">← Volver al inicio</Link>
          </p>
        </div>
      </div>

      <style>{`
        .auth-layout { display:flex; min-height:100vh; }
        .auth-left { flex:1; background:linear-gradient(135deg,#154360 0%,#1B4F72 100%);
          color:#fff; display:flex; flex-direction:column; justify-content:center; padding:3rem; }
        .auth-brand { margin-bottom:3rem; }
        .auth-brand h1 { font-size:2rem; font-weight:900; margin:.5rem 0 .5rem; }
        .auth-brand p  { color:rgba(255,255,255,0.75); font-size:1rem; }
        .auth-features { display:flex; flex-direction:column; gap:1.2rem; }
        .auth-feature { display:flex; gap:1rem; align-items:center; font-size:1rem;
          background:rgba(255,255,255,0.08); padding:.85rem 1rem; border-radius:10px; }
        .auth-right { flex:1; display:flex; align-items:center; justify-content:center;
          background:var(--bg); padding:2rem; }
        .auth-card { background:#fff; border-radius:16px; padding:2.5rem;
          box-shadow:0 8px 40px rgba(0,0,0,0.12); width:100%; max-width:420px; }
        .auth-card h2 { font-size:1.6rem; font-weight:800; color:var(--primary); margin-bottom:.25rem; }
        .auth-sub { color:var(--text-lt); margin-bottom:1.5rem; }
        .auth-divider { text-align:center; margin:1.5rem 0 .75rem; position:relative; }
        .auth-divider::before { content:''; position:absolute; top:50%; left:0; right:0;
          height:1px; background:var(--border); }
        .auth-divider span { background:#fff; padding:0 .75rem; position:relative;
          font-size:.8rem; color:var(--text-lt); }
        .auth-hints { display:flex; flex-direction:column; gap:.5rem; margin-bottom:1rem; }
        .hint-chip { padding:.5rem .9rem; border-radius:8px; font-size:.82rem; font-weight:600;
          cursor:pointer; transition:all .15s; border:1.5px solid transparent; }
        .hint-admin    { background:#D6EAF8; color:var(--primary); }
        .hint-vendedor { background:#FEF9E7; color:#9A7D0A; }
        .hint-usuario  { background:#D5F5E3; color:var(--green); }
        .hint-chip:hover { transform:translateX(4px); filter:brightness(.95); }
        .auth-footer { text-align:center; font-size:.88rem; color:var(--text-lt); margin-top:.75rem; }
        .auth-footer a { color:var(--primary); font-weight:600; text-decoration:none; }
        @media(max-width:768px){ .auth-left{ display:none; } }
      `}</style>
    </div>
  )
}
