import { useState, useEffect, useMemo } from 'react'
import Navbar from '../../components/Navbar'
import { usuariosApi } from '../../api/apiClient'

const ROLES_FALLBACK = ['ADMIN', 'VENDEDOR', 'USUARIO']
const FORM_VACIO = { nombre: '', email: '', password: '', rol: 'USUARIO', activo: true }

export default function UsuariosPage() {
  const [usuarios, setUsuarios] = useState([])
  const [roles, setRoles]       = useState(ROLES_FALLBACK)
  const [loading, setLoading]   = useState(true)
  const [error, setError]       = useState('')
  const [success, setSuccess]   = useState('')
  const [showForm, setShowForm] = useState(false)
  const [editandoId, setEditandoId] = useState(null)
  const [saving, setSaving]     = useState(false)
  const [form, setForm]         = useState(FORM_VACIO)

  useEffect(() => { cargarUsuarios(); cargarRoles() }, [])

  const cargarUsuarios = async () => {
    try {
      setLoading(true)
      const res = await usuariosApi.listar()
      setUsuarios(Array.isArray(res.data) ? res.data : [])
    } catch { setError('No se pudo conectar con el servicio de usuarios') }
    finally { setLoading(false) }
  }

  const cargarRoles = async () => {
    try {
      const res = await usuariosApi.roles()
      if (Array.isArray(res.data) && res.data.length) setRoles(res.data)
    } catch { /* se mantiene el fallback */ }
  }

  const stats = useMemo(() => ({
    total:   usuarios.length,
    activos: usuarios.filter(u => u.activo).length,
    admins:  usuarios.filter(u => u.rol === 'ADMIN').length,
  }), [usuarios])

  const abrirCrear = () => {
    setEditandoId(null)
    setForm(FORM_VACIO)
    setShowForm(true)
    setError(''); setSuccess('')
  }

  const abrirEditar = (u) => {
    setEditandoId(u.id)
    setForm({ nombre: u.nombre, email: u.email, password: '', rol: u.rol, activo: u.activo })
    setShowForm(true)
    setError(''); setSuccess('')
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  const cerrarForm = () => {
    setShowForm(false)
    setEditandoId(null)
    setForm(FORM_VACIO)
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setSaving(true); setError(''); setSuccess('')
    try {
      if (editandoId) {
        const payload = {
          nombre: form.nombre,
          email:  form.email,
          rol:    form.rol,
          activo: form.activo,
        }
        if (form.password.trim()) payload.password = form.password.trim()
        await usuariosApi.actualizar(editandoId, payload)
        setSuccess('✅ Usuario actualizado correctamente')
      } else {
        if (!form.password.trim()) {
          setError('La contraseña es obligatoria para un nuevo usuario')
          setSaving(false); return
        }
        await usuariosApi.crear({
          nombre:   form.nombre,
          email:    form.email,
          password: form.password.trim(),
          rol:      form.rol,
        })
        setSuccess('✅ Usuario creado correctamente')
      }
      cerrarForm()
      cargarUsuarios()
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data?.error || 'Error al guardar el usuario')
    } finally { setSaving(false) }
  }

  const handleEliminar = async (u) => {
    if (!window.confirm(`¿Eliminar al usuario "${u.nombre}" (${u.email})?`)) return
    setError(''); setSuccess('')
    try {
      await usuariosApi.eliminar(u.id)
      setSuccess('🗑️ Usuario eliminado')
      if (editandoId === u.id) cerrarForm()
      cargarUsuarios()
    } catch (err) {
      setError(err.response?.data?.message || 'No se pudo eliminar el usuario')
    }
  }

  const badgeRol = (rol) =>
    rol === 'ADMIN' ? 'badge-gold' : rol === 'VENDEDOR' ? 'badge-blue' : 'badge-green'

  return (
    <div className="page-wrapper">
      <Navbar />
      <div className="page-content">

        {/* Header */}
        <div style={{ display:'flex', justifyContent:'space-between', alignItems:'flex-start', marginBottom:'1.5rem' }}>
          <div>
            <h1 className="page-title">👥 Mantenedor de Usuarios</h1>
            <p className="page-subtitle">Crea, edita y elimina las cuentas del sistema</p>
          </div>
          <button className="btn btn-primary" onClick={showForm ? cerrarForm : abrirCrear}>
            {showForm ? '✕ Cancelar' : '+ Nuevo usuario'}
          </button>
        </div>

        {error   && <div className="alert alert-error">{error}</div>}
        {success && <div className="alert alert-success">{success}</div>}

        {/* Stats */}
        <div className="grid-3" style={{ marginBottom:'1.5rem' }}>
          <div className="stat-card">
            <div className="stat-value">{stats.total}</div>
            <div className="stat-label">Total usuarios</div>
          </div>
          <div className="stat-card">
            <div className="stat-value">{stats.activos}</div>
            <div className="stat-label">Activos</div>
          </div>
          <div className="stat-card">
            <div className="stat-value">{stats.admins}</div>
            <div className="stat-label">Administradores</div>
          </div>
        </div>

        {/* Formulario */}
        {showForm && (
          <div className="card" style={{ marginBottom:'1.5rem' }}>
            <div className="card-header">
              <span className="card-title">{editandoId ? 'Editar usuario' : 'Nuevo usuario'}</span>
            </div>
            <form onSubmit={handleSubmit}>
              <div className="form-row">
                <div className="form-group">
                  <label className="form-label">Nombre</label>
                  <input className="form-input" required minLength={2}
                    value={form.nombre}
                    onChange={e => setForm(f => ({ ...f, nombre: e.target.value }))} />
                </div>
                <div className="form-group">
                  <label className="form-label">Email</label>
                  <input className="form-input" type="email" required
                    value={form.email}
                    onChange={e => setForm(f => ({ ...f, email: e.target.value }))} />
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label className="form-label">
                    Contraseña {editandoId && <span style={{ color:'var(--text-lt)', fontWeight:400 }}>(dejar vacío para no cambiar)</span>}
                  </label>
                  <input className="form-input" type="password"
                    placeholder={editandoId ? '••••••••' : 'Mínimo 6 caracteres'}
                    value={form.password} minLength={form.password ? 6 : undefined}
                    onChange={e => setForm(f => ({ ...f, password: e.target.value }))} />
                </div>
                <div className="form-group">
                  <label className="form-label">Rol</label>
                  <select className="form-select" value={form.rol}
                    onChange={e => setForm(f => ({ ...f, rol: e.target.value }))}>
                    {roles.map(r => <option key={r} value={r}>{r}</option>)}
                  </select>
                </div>
              </div>

              {editandoId && (
                <div className="form-group">
                  <label className="form-label" style={{ display:'flex', alignItems:'center', gap:'.5rem', cursor:'pointer' }}>
                    <input type="checkbox" checked={!!form.activo}
                      onChange={e => setForm(f => ({ ...f, activo: e.target.checked }))} />
                    Usuario activo (puede iniciar sesión)
                  </label>
                </div>
              )}

              <div className="flex-end" style={{ gap:'.75rem' }}>
                <button type="button" className="btn btn-outline" onClick={cerrarForm}>Cancelar</button>
                <button type="submit" className="btn btn-primary" disabled={saving}>
                  {saving ? 'Guardando...' : (editandoId ? '💾 Guardar cambios' : '+ Crear usuario')}
                </button>
              </div>
            </form>
          </div>
        )}

        {/* Tabla */}
        <div className="card">
          <div className="card-header">
            <span className="card-title">Usuarios registrados</span>
            <button className="btn btn-outline" onClick={cargarUsuarios}
              style={{ padding:'.4rem .8rem', fontSize:'.85rem' }}>
              🔄 Actualizar
            </button>
          </div>
          {loading ? (
            <div className="loading">Cargando usuarios...</div>
          ) : usuarios.length === 0 ? (
            <div style={{ textAlign:'center', padding:'2rem', color:'var(--text-lt)' }}>
              No hay usuarios registrados.
            </div>
          ) : (
            <div className="table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>ID</th><th>Nombre</th><th>Email</th><th>Rol</th><th>Estado</th><th>Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {usuarios.map(u => (
                    <tr key={u.id}>
                      <td><span className="badge badge-blue">{u.id}</span></td>
                      <td><strong>{u.nombre}</strong></td>
                      <td style={{ color:'var(--text-lt)' }}>{u.email}</td>
                      <td><span className={`badge ${badgeRol(u.rol)}`}>{u.rol}</span></td>
                      <td>
                        <span className={`badge ${u.activo ? 'badge-green' : 'badge-red'}`}>
                          {u.activo ? 'Activo' : 'Inactivo'}
                        </span>
                      </td>
                      <td>
                        <div style={{ display:'flex', gap:'.4rem' }}>
                          <button className="btn btn-outline" onClick={() => abrirEditar(u)}
                            style={{ padding:'.3rem .7rem', fontSize:'.8rem' }}>✏️ Editar</button>
                          <button className="btn btn-danger" onClick={() => handleEliminar(u)}
                            style={{ padding:'.3rem .7rem', fontSize:'.8rem' }}>🗑️</button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

      </div>
      <footer><p>© 2026 Grupo Cordillera</p></footer>
    </div>
  )
}
