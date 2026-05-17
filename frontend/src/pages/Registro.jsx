import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Registro() {
  const { registro } = useAuth()
  const navigate = useNavigate()
  const [form, setForm]       = useState({ nombre: '', email: '', password: '' })
  const [error, setError]     = useState('')
  const [success, setSuccess] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError(''); setLoading(true)
    try {
      await registro({ ...form, rol: 'USUARIO' })
      setSuccess('¡Cuenta creada exitosamente! Redirigiendo al login...')
      setTimeout(() => navigate('/login'), 1800)
    } catch (err) {
      setError(err.response?.data?.message || 'Error al crear la cuenta. Intenta con otro email.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{ minHeight:'100vh', background:'var(--bg)', display:'flex', alignItems:'center', justifyContent:'center', padding:'2rem' }}>
      <div style={{ background:'#fff', borderRadius:16, padding:'2.5rem', boxShadow:'0 8px 40px rgba(0,0,0,0.12)', width:'100%', maxWidth:460 }}>
        <div style={{ textAlign:'center', marginBottom:'1.5rem' }}>
          <div style={{ fontSize:'2.5rem' }}>⛰️</div>
          <h2 style={{ fontSize:'1.6rem', fontWeight:800, color:'var(--primary)' }}>Crear cuenta</h2>
          <p style={{ color:'var(--text-lt)', fontSize:'.95rem' }}>Únete a Grupo Cordillera</p>
        </div>

        {error   && <div className="alert alert-error">{error}</div>}
        {success && <div className="alert alert-success">{success}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Nombre completo</label>
            <input className="form-input" placeholder="Ej: Juan Pérez"
              value={form.nombre} required minLength={2}
              onChange={e => setForm({...form, nombre: e.target.value})} />
          </div>
          <div className="form-group">
            <label className="form-label">Email</label>
            <input type="email" className="form-input" placeholder="tu@email.com"
              value={form.email} required
              onChange={e => setForm({...form, email: e.target.value})} />
          </div>
          <div className="form-group">
            <label className="form-label">Contraseña</label>
            <input type="password" className="form-input" placeholder="Mínimo 6 caracteres"
              value={form.password} required minLength={6}
              onChange={e => setForm({...form, password: e.target.value})} />
          </div>
          <button type="submit" className="btn btn-primary" style={{ width:'100%' }} disabled={loading}>
            {loading ? 'Creando cuenta...' : 'Crear cuenta gratuita'}
          </button>
        </form>

        <div style={{ background:'#EBF5FB', borderRadius:8, padding:'.75rem 1rem', marginTop:'1rem', fontSize:'.85rem', color:'var(--primary)' }}>
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
