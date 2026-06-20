import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

export default function Registro() {
  const { registro } = useAuth()
  const { toast } = useToast()
  const navigate = useNavigate()
  const [form, setForm]     = useState({ nombre: '', email: '', password: '' })
  const [loading, setLoading] = useState(false)

  const validar = () => {
    if (!form.nombre.trim() || form.nombre.trim().length < 2) {
      toast.error('El nombre debe tener al menos 2 caracteres')
      return false
    }
    if (!EMAIL_RE.test(form.email.trim())) {
      toast.error('Ingresa un email válido')
      return false
    }
    if (form.password.length < 6) {
      toast.error('La contraseña debe tener al menos 6 caracteres')
      return false
    }
    return true
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!validar()) return
    setLoading(true)
    try {
      await registro({ ...form, rol: 'USUARIO' })
      toast.success('¡Cuenta creada! Redirigiendo al login...')
      setTimeout(() => navigate('/login'), 1800)
    } catch (err) {
      toast.error(err.response?.data?.message || 'Error al crear la cuenta. Intenta con otro email.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{ minHeight:'100vh', background:'var(--bg)', display:'flex', alignItems:'center', justifyContent:'center', padding:'2rem' }}>
      <div style={{ background:'#fff', borderRadius:16, padding:'2.5rem', border:'1px solid var(--border)', width:'100%', maxWidth:460 }}>
        <div style={{ textAlign:'center', marginBottom:'1.5rem' }}>
          <div style={{ fontSize:'2.5rem' }}>⛰️</div>
          <h2 style={{ fontSize:'1.6rem', fontWeight:800, color:'var(--primary)' }}>Crear cuenta</h2>
          <p style={{ color:'var(--text-lt)', fontSize:'.95rem' }}>Únete a Grupo Cordillera</p>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label" htmlFor="reg-nombre">Nombre completo</label>
            <input id="reg-nombre" className="form-input" placeholder="Ej: Juan Pérez"
              value={form.nombre}
              onChange={e => setForm({...form, nombre: e.target.value})} />
          </div>
          <div className="form-group">
            <label className="form-label" htmlFor="reg-email">Email</label>
            <input id="reg-email" type="email" className="form-input" placeholder="tu@email.com"
              value={form.email}
              onChange={e => setForm({...form, email: e.target.value})} />
          </div>
          <div className="form-group">
            <label className="form-label" htmlFor="reg-password">Contraseña</label>
            <input id="reg-password" type="password" className="form-input" placeholder="Mínimo 6 caracteres"
              value={form.password}
              onChange={e => setForm({...form, password: e.target.value})} />
          </div>
          <button type="submit" className="btn btn-primary" style={{ width:'100%' }} disabled={loading}>
            {loading ? 'Creando cuenta...' : 'Crear cuenta gratuita'}
          </button>
        </form>

        <div style={{ background:'var(--accent)', borderRadius:8, padding:'.75rem 1rem', marginTop:'1rem', fontSize:'.85rem', color:'var(--primary)', border:'1px solid var(--border)' }}>
          ℹ️ Las cuentas creadas aquí son de tipo <strong>Cliente</strong> con acceso a la tienda online.
        </div>

        <p style={{ textAlign:'center', marginTop:'1.25rem', fontSize:'.9rem', color:'var(--text-lt)' }}>
          ¿Ya tienes cuenta? <Link to="/login" style={{ color:'var(--primary)', fontWeight:600 }}>Iniciar sesión</Link>
        </p>
        <p style={{ textAlign:'center', marginTop:'.5rem', fontSize:'.88rem' }}>
          <Link to="/" style={{ color:'var(--text-lt)' }}>← Volver al inicio</Link>
        </p>
      </div>
    </div>
  )
}
