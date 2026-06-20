import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'

export default function Login() {
  const { login } = useAuth()
  const { toast } = useToast()
  const navigate = useNavigate()
  const [form, setForm]       = useState({ email: '', password: '' })
  const [loading, setLoading] = useState(false)

  const validar = () => {
    if (!form.email.trim()) { toast.error('Ingresa tu email'); return false }
    if (!form.password)     { toast.error('Ingresa tu contraseña'); return false }
    return true
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!validar()) return
    setLoading(true)
    try {
      const rol = await login(form.email, form.password)
      if (rol === 'ADMIN')         navigate('/admin/kpi')
      else if (rol === 'VENDEDOR') navigate('/vendedor/pos')
      else                         navigate('/tienda')
    } catch {
      toast.error('Email o contraseña incorrectos')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="login-wrapper">

      {/* Panel izquierdo */}
      <div className="login-panel-left">
        <div style={{ marginBottom:'3rem' }}>
          <span style={{ fontSize:'2.5rem' }}>⛰️</span>
          <h1 style={{ fontSize:'1.75rem', fontWeight:700, margin:'.5rem 0 .4rem', letterSpacing:'-0.02em' }}>
            Grupo Cordillera
          </h1>
          <p style={{ color:'rgba(255,255,255,0.45)', fontSize:'.9rem' }}>
            Sistema integrado de gestión empresarial
          </p>
        </div>
        <div style={{ display:'flex', flexDirection:'column', gap:'.75rem' }}>
          {[
            { icon:'📊', text:'Dashboard KPI en tiempo real' },
            { icon:'🏪', text:'Punto de venta integrado' },
            { icon:'📋', text:'Reportes automáticos' },
            { icon:'🛒', text:'E-commerce unificado' },
          ].map(f => (
            <div key={f.text} style={{
              display:'flex', gap:'.9rem', alignItems:'center',
              background:'rgba(255,255,255,0.05)', padding:'.75rem 1rem',
              borderRadius:8, border:'1px solid rgba(255,255,255,0.08)',
              fontSize:'.875rem', color:'rgba(255,255,255,0.75)',
            }}>
              <span>{f.icon}</span>
              <span>{f.text}</span>
            </div>
          ))}
        </div>
      </div>

      {/* Panel derecho */}
      <div className="login-panel-right">
        <div style={{ width:'100%', maxWidth:400 }}>
          <h2 style={{ fontSize:'1.4rem', fontWeight:700, color:'#111827', marginBottom:'.25rem', letterSpacing:'-0.02em' }}>
            Iniciar sesión
          </h2>
          <p style={{ color:'#6B7280', marginBottom:'2rem', fontSize:'.875rem' }}>
            Bienvenido de vuelta
          </p>

          <form onSubmit={handleSubmit}>
            <div style={{ marginBottom:'1rem' }}>
              <label htmlFor="email" style={{ display:'block', fontWeight:600, marginBottom:'.4rem', fontSize:'.72rem', color:'#6B7280', textTransform:'uppercase', letterSpacing:'0.07em' }}>
                Email
              </label>
              <input id="email" type="email" className="form-input"
                placeholder="tu@cordillera.cl"
                value={form.email}
                onChange={e => setForm({...form, email: e.target.value})} />
            </div>
            <div style={{ marginBottom:'1.5rem' }}>
              <label htmlFor="password" style={{ display:'block', fontWeight:600, marginBottom:'.4rem', fontSize:'.72rem', color:'#6B7280', textTransform:'uppercase', letterSpacing:'0.07em' }}>
                Contraseña
              </label>
              <input id="password" type="password" className="form-input"
                placeholder="Ingresa tu contraseña"
                value={form.password}
                onChange={e => setForm({...form, password: e.target.value})} />
            </div>
            <button type="submit" className="btn btn-primary"
              style={{ width:'100%', justifyContent:'center', padding:'.7rem' }}
              disabled={loading}>
              {loading ? 'Ingresando...' : 'Iniciar sesión'}
            </button>
          </form>

          <div style={{ display:'flex', alignItems:'center', gap:'1rem', margin:'1.5rem 0 1rem' }}>
            <div style={{ flex:1, height:1, background:'#E5E7EB' }} />
            <span style={{ fontSize:'.72rem', color:'#9CA3AF', fontWeight:600, textTransform:'uppercase', letterSpacing:'0.06em' }}>
              Usuarios de prueba
            </span>
            <div style={{ flex:1, height:1, background:'#E5E7EB' }} />
          </div>

          <div style={{ display:'flex', flexDirection:'column', gap:'.4rem', marginBottom:'1.25rem' }}>
            {[
              { label:'👑 Admin', email:'admin@cordillera.cl',      clave:'Admin123!' },
              { label:'🏷️ Vendedor', email:'vendedor1@cordillera.cl', clave:'Admin123!' },
              { label:'👤 Cliente',  email:'cliente1@cordillera.cl',  clave:'Admin123!' },
            ].map(h => (
              <button key={h.email} type="button"
                onClick={() => setForm({ email: h.email, password: h.clave })}
                style={{
                  padding:'.5rem .9rem', borderRadius:6, fontSize:'.82rem',
                  fontWeight:600, cursor:'pointer', transition:'all .15s',
                  background:'#F9FAFB', border:'1px solid #E5E7EB',
                  color:'#374151', textAlign:'left', font:'inherit',
                }}>
                {h.label} — {h.email}
              </button>
            ))}
          </div>

          <p style={{ textAlign:'center', fontSize:'.85rem', color:'#9CA3AF' }}>
            ¿No tienes cuenta?{' '}
            <Link to="/registro" style={{ color:'#111827', fontWeight:600, textDecoration:'none' }}>
              Regístrate aquí
            </Link>
          </p>
          <p style={{ textAlign:'center', fontSize:'.85rem', color:'#9CA3AF', marginTop:'.5rem' }}>
            <Link to="/" style={{ color:'#111827', fontWeight:600, textDecoration:'none' }}>
              ← Volver al inicio
            </Link>
          </p>
        </div>
      </div>

      <style>{`
        .login-wrapper { display: flex; min-height: 100vh; }
        .login-panel-left {
          flex: 1; background: #111827; color: #fff;
          display: flex; flex-direction: column; justify-content: center; padding: 3rem;
        }
        .login-panel-right {
          flex: 1; display: flex; align-items: center; justify-content: center;
          background: #fff; padding: 2rem;
        }
        @media (max-width: 768px) {
          .login-wrapper { flex-direction: column; }
          .login-panel-left { display: none; }
          .login-panel-right { flex: unset; min-height: 100vh; padding: 2rem 1.25rem; align-items: flex-start; padding-top: 3rem; }
        }
      `}</style>
    </div>
  )
}
