import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Unauthorized() {
  const { user } = useAuth()
  return (
    <div style={{ minHeight:'100vh', display:'flex', flexDirection:'column', alignItems:'center',
      justifyContent:'center', background:'var(--bg)', padding:'2rem', textAlign:'center' }}>
      <div style={{ fontSize:'5rem', marginBottom:'1rem' }}>🔒</div>
      <h1 style={{ fontSize:'2rem', color:'var(--primary)', marginBottom:'.5rem' }}>Acceso no autorizado</h1>
      <p style={{ color:'var(--text-lt)', marginBottom:'2rem', fontSize:'1.05rem' }}>
        No tienes permisos para acceder a esta sección.
      </p>
      {user && (
        <div style={{ background:'#EBF5FB', borderRadius:10, padding:'1rem 1.5rem', marginBottom:'1.5rem', fontSize:'.9rem' }}>
          Tu rol actual es <strong>{user.rol}</strong>
        </div>
      )}
      <Link to="/" className="btn btn-primary">← Ir al inicio</Link>
    </div>
  )
}
