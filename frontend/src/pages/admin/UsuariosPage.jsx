import { useState, useEffect, useMemo } from 'react'
import Navbar from '../../components/Navbar'
import ConfirmModal from '../../components/ConfirmModal'
import { usuariosApi } from '../../api/apiClient'
import { useToast } from '../../context/ToastContext'

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const ROLES_FALLBACK = ['ADMIN', 'VENDEDOR', 'USUARIO']
const FORM_VACIO = { nombre: '', email: '', password: '', rol: 'USUARIO', activo: true }

function badgeRol(rol) {
  if (rol === 'ADMIN')    return 'badge-gold'
  if (rol === 'VENDEDOR') return 'badge-blue'
  return 'badge-green'
}

export default function UsuariosPage() {
  const { toast } = useToast()
  const [usuarios, setUsuarios] = useState([])
  const [roles, setRoles]       = useState(ROLES_FALLBACK)
  const [loading, setLoading]   = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [editandoId, setEditandoId] = useState(null)
  const [saving, setSaving]     = useState(false)
  const [form, setForm]         = useState(FORM_VACIO)
  const [confirm, setConfirm]   = useState(null)

  useEffect(() => { cargarUsuarios(); cargarRoles() }, [])

  const cargarUsuarios = async () => {
    try {
      setLoading(true)
      const res = await usuariosApi.listar()
      setUsuarios(Array.isArray(res.data) ? res.data : [])
    } catch {
      toast.error('No se pudo conectar con el servicio de usuarios')
    } finally { setLoading(false) }
  }

  const cargarRoles = async () => {
    try {
      const res = await usuariosApi.roles()
      if (Array.isArray(res.data) && res.data.length) setRoles(res.data)
    } catch { /* mantiene fallback */ }
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
  }

  const abrirEditar = (u) => {
    setEditandoId(u.id)
    setForm({ nombre: u.nombre, email: u.email, password: '', rol: u.rol, activo: u.activo })
    setShowForm(true)
    globalThis.scrollTo({ top: 0, behavior: 'smooth' })
  }

  const cerrarForm = () => {
    setShowForm(false)
    setEditandoId(null)
    setForm(FORM_VACIO)
  }

  const validarForm = () => {
    if (!form.nombre.trim() || form.nombre.trim().length < 2) {
      toast.error('El nombre debe tener al menos 2 caracteres')
      return false
    }
    if (!EMAIL_RE.test(form.email.trim())) {
      toast.error('Ingresa un email válido')
      return false
    }
    if (!editandoId && !form.password.trim()) {
      toast.error('La contraseña es obligatoria para un nuevo usuario')
      return false
    }
    if (form.password.trim() && form.password.trim().length < 6) {
      toast.error('La contraseña debe tener al menos 6 caracteres')
      return false
    }
    return true
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!validarForm()) return
    setSaving(true)
    try {
      if (editandoId) {
        const payload = { nombre: form.nombre.trim(), email: form.email.trim(), rol: form.rol, activo: form.activo }
        if (form.password.trim()) payload.password = form.password.trim()
        await usuariosApi.actualizar(editandoId, payload)
        toast.success('Usuario actualizado correctamente')
      } else {
        await usuariosApi.crear({ nombre: form.nombre.trim(), email: form.email.trim(), password: form.password.trim(), rol: form.rol })
        toast.success('Usuario creado correctamente')
      }
      cerrarForm()
      cargarUsuarios()
    } catch (err) {
      toast.error(err.response?.data?.message || err.response?.data?.error || 'Error al guardar el usuario')
    } finally { setSaving(false) }
  }

  const pedirConfirmEliminar = (u) => {
    setConfirm({
      title: 'Eliminar usuario',
      message: `¿Estás seguro de que quieres eliminar a "${u.nombre}" (${u.email})? Esta acción no se puede deshacer.`,
      confirmLabel: 'Eliminar',
      onConfirm: async () => {
        setConfirm(null)
        try {
          await usuariosApi.eliminar(u.id)
          toast.success('Usuario eliminado')
          if (editandoId === u.id) cerrarForm()
          cargarUsuarios()
        } catch (err) {
          toast.error(err.response?.data?.message || 'No se pudo eliminar el usuario')
        }
      },
    })
  }

  const accionLabel = editandoId ? 'Guardar cambios' : '+ Crear usuario'

  return (
    <div className="page-wrapper">
      <Navbar />
      {confirm && <ConfirmModal {...confirm} onCancel={() => setConfirm(null)} />}

      <div className="page-content">
        <div className="page-header">
          <div>
            <h1 className="page-title">Mantenedor de Usuarios</h1>
            <p className="page-subtitle">Crea, edita y elimina las cuentas del sistema</p>
          </div>
          <button className="btn btn-primary" onClick={showForm ? cerrarForm : abrirCrear}>
            {showForm ? '✕ Cancelar' : '+ Nuevo usuario'}
          </button>
        </div>

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
                  <label className="form-label" htmlFor="u-nombre">Nombre</label>
                  <input id="u-nombre" className="form-input" placeholder="Nombre completo"
                    value={form.nombre}
                    onChange={e => setForm(f => ({ ...f, nombre: e.target.value }))} />
                </div>
                <div className="form-group">
                  <label className="form-label" htmlFor="u-email">Email</label>
                  <input id="u-email" className="form-input" type="email" placeholder="usuario@empresa.cl"
                    value={form.email}
                    onChange={e => setForm(f => ({ ...f, email: e.target.value }))} />
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label className="form-label" htmlFor="u-password">
                    Contraseña{editandoId && <span style={{ color:'var(--text-xs)', fontWeight:400, textTransform:'none', letterSpacing:0 }}> (dejar vacío para no cambiar)</span>}
                  </label>
                  <input id="u-password" className="form-input" type="password"
                    placeholder={editandoId ? '••••••••' : 'Mínimo 6 caracteres'}
                    value={form.password}
                    onChange={e => setForm(f => ({ ...f, password: e.target.value }))} />
                </div>
                <div className="form-group">
                  <label className="form-label" htmlFor="u-rol">Rol</label>
                  <select id="u-rol" className="form-select" value={form.rol}
                    onChange={e => setForm(f => ({ ...f, rol: e.target.value }))}>
                    {roles.map(r => <option key={r} value={r}>{r}</option>)}
                  </select>
                </div>
              </div>

              {editandoId && (
                <div className="form-group">
                  <label className="form-label" htmlFor="u-activo" style={{ display:'flex', alignItems:'center', gap:'.5rem', cursor:'pointer' }}>
                    <input id="u-activo" type="checkbox" checked={!!form.activo}
                      onChange={e => setForm(f => ({ ...f, activo: e.target.checked }))} />
                    <span>Usuario activo (puede iniciar sesión)</span>
                  </label>
                </div>
              )}

              <div className="flex-end" style={{ gap:'.75rem' }}>
                <button type="button" className="btn btn-outline" onClick={cerrarForm}>Cancelar</button>
                <button type="submit" className="btn btn-primary" disabled={saving}>
                  {saving ? 'Guardando...' : accionLabel}
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
              style={{ padding:'.4rem .8rem', fontSize:'.8rem' }}>
              Actualizar
            </button>
          </div>
          {loading && <div className="loading">Cargando usuarios...</div>}
          {!loading && usuarios.length === 0 && (
            <div style={{ textAlign:'center', padding:'3rem', color:'var(--text-lt)' }}>
              No hay usuarios registrados.
            </div>
          )}
          {!loading && usuarios.length > 0 && (
            <div className="table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>ID</th><th>Nombre</th><th>Email</th><th>Rol</th><th>Estado</th><th>Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {usuarios.map(u => {
                    const estadoBadge = u.activo ? 'badge-green' : 'badge-red'
                    const estadoLabel = u.activo ? 'Activo' : 'Inactivo'
                    return (
                      <tr key={u.id}>
                        <td><span className="badge badge-blue">{u.id}</span></td>
                        <td><strong>{u.nombre}</strong></td>
                        <td style={{ color:'var(--text-lt)' }}>{u.email}</td>
                        <td><span className={`badge ${badgeRol(u.rol)}`}>{u.rol}</span></td>
                        <td><span className={`badge ${estadoBadge}`}>{estadoLabel}</span></td>
                        <td>
                          <div style={{ display:'flex', gap:'.4rem' }}>
                            <button className="btn btn-outline" onClick={() => abrirEditar(u)}
                              style={{ padding:'.3rem .7rem', fontSize:'.78rem' }}>Editar</button>
                            <button className="btn btn-danger" onClick={() => pedirConfirmEliminar(u)}
                              style={{ padding:'.3rem .7rem', fontSize:'.78rem' }}>Eliminar</button>
                          </div>
                        </td>
                      </tr>
                    )
                  })}
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
