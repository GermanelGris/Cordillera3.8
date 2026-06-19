import { useState, useEffect, useMemo } from 'react'
import Navbar from '../../components/Navbar'
import ConfirmModal from '../../components/ConfirmModal'
import { inventarioApi } from '../../api/apiClient'
import { useToast } from '../../context/ToastContext'

const FORM_VACIO = { productoId: '', nombre: '', stock: '' }
const UMBRAL_ROJO  = 20
const UMBRAL_VERDE = 50

function semaforoStock(stock = 0) {
  if (stock < UMBRAL_ROJO)  return { emoji: '🔴', label: 'Bajo',  badge: 'badge-red',   key: 'rojo' }
  if (stock < UMBRAL_VERDE) return { emoji: '🟡', label: 'Medio', badge: 'badge-gold',  key: 'amarillo' }
  return                           { emoji: '🟢', label: 'Bien',  badge: 'badge-green', key: 'verde' }
}

export default function InventarioPage() {
  const { toast } = useToast()
  const [items, setItems]       = useState([])
  const [loading, setLoading]   = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [editandoId, setEditandoId] = useState(null)
  const [saving, setSaving]     = useState(false)
  const [form, setForm]         = useState(FORM_VACIO)
  const [confirm, setConfirm]   = useState(null)

  useEffect(() => { cargar() }, [])

  const cargar = async () => {
    try {
      setLoading(true)
      const res = await inventarioApi.listar()
      setItems(Array.isArray(res.data) ? res.data : [])
    } catch {
      toast.error('No se pudo conectar con el servicio de inventario')
    } finally { setLoading(false) }
  }

  const stats = useMemo(() => {
    const conteo = { verde: 0, amarillo: 0, rojo: 0 }
    items.forEach(i => { conteo[semaforoStock(i.stock).key]++ })
    return {
      productos: items.length,
      unidades:  items.reduce((acc, i) => acc + (i.stock || 0), 0),
      ...conteo,
    }
  }, [items])

  const abrirCrear = () => {
    setEditandoId(null)
    setForm(FORM_VACIO)
    setShowForm(true)
  }

  const abrirEditar = (i) => {
    setEditandoId(i.id)
    setForm({ productoId: i.productoId, nombre: i.nombre, stock: i.stock })
    setShowForm(true)
    globalThis.scrollTo({ top: 0, behavior: 'smooth' })
  }

  const cerrarForm = () => {
    setShowForm(false)
    setEditandoId(null)
    setForm(FORM_VACIO)
  }

  const validarForm = () => {
    const pid = Number(form.productoId)
    if (!form.productoId || Number.isNaN(pid) || pid < 1) {
      toast.error('El ID de producto debe ser un número mayor a 0')
      return false
    }
    if (!form.nombre.trim() || form.nombre.trim().length < 2) {
      toast.error('El nombre del producto debe tener al menos 2 caracteres')
      return false
    }
    const s = Number(form.stock)
    if (form.stock === '' || Number.isNaN(s) || s < 0) {
      toast.error('El stock debe ser un número mayor o igual a 0')
      return false
    }
    return true
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!validarForm()) return
    setSaving(true)
    const payload = {
      productoId: Number(form.productoId),
      nombre:     form.nombre.trim(),
      stock:      Number(form.stock),
    }
    try {
      if (editandoId) {
        await inventarioApi.actualizar(editandoId, payload)
        toast.success('Producto actualizado correctamente')
      } else {
        await inventarioApi.crear(payload)
        toast.success('Producto creado correctamente')
      }
      cerrarForm()
      cargar()
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data?.error || ''
      if (err.response?.status === 409 || msg.toLowerCase().includes('duplicate') || msg.toLowerCase().includes('exist')) {
        toast.error(`El ID de producto ${payload.productoId} ya existe en el inventario. Usa un ID diferente.`)
      } else {
        toast.error(msg || 'Error al guardar el producto')
      }
    } finally { setSaving(false) }
  }

  const pedirConfirmEliminar = (i) => {
    setConfirm({
      title: 'Eliminar producto',
      message: `¿Estás seguro de que quieres eliminar "${i.nombre}" del inventario? Esta acción no se puede deshacer.`,
      confirmLabel: 'Eliminar',
      onConfirm: async () => {
        setConfirm(null)
        try {
          await inventarioApi.eliminar(i.id)
          toast.success('Producto eliminado')
          if (editandoId === i.id) cerrarForm()
          cargar()
        } catch (err) {
          toast.error(err.response?.data?.message || 'No se pudo eliminar el producto')
        }
      },
    })
  }

  const accionLabel = editandoId ? 'Guardar cambios' : '+ Crear producto'

  return (
    <div className="page-wrapper">
      <Navbar />
      {confirm && <ConfirmModal {...confirm} onCancel={() => setConfirm(null)} />}

      <div className="page-content">
        <div className="page-header">
          <div>
            <h1 className="page-title">Mantenedor de Inventario</h1>
            <p className="page-subtitle">Administra los productos y el stock disponible</p>
          </div>
          <button className="btn btn-primary" onClick={showForm ? cerrarForm : abrirCrear}>
            {showForm ? '✕ Cancelar' : '+ Nuevo producto'}
          </button>
        </div>

        {/* Stats */}
        <div className="grid-3" style={{ marginBottom:'1.5rem' }}>
          <div className="stat-card">
            <div className="stat-value">{stats.productos}</div>
            <div className="stat-label">Productos</div>
          </div>
          <div className="stat-card">
            <div className="stat-value">{stats.unidades.toLocaleString('es-CL')}</div>
            <div className="stat-label">Unidades en stock</div>
          </div>
          <div className="stat-card">
            <div className="stat-value" style={{ fontSize:'1.3rem' }}>
              🟢 {stats.verde} · 🟡 {stats.amarillo} · 🔴 {stats.rojo}
            </div>
            <div className="stat-label">Semáforo de stock</div>
          </div>
        </div>

        {/* Leyenda semáforo */}
        <div style={{
          display:'flex', gap:'1.25rem', flexWrap:'wrap', alignItems:'center',
          background:'var(--accent)', border:'1px solid var(--border)', borderRadius:8,
          padding:'.5rem .9rem', marginBottom:'1.5rem', fontSize:'.78rem', color:'var(--text-lt)',
        }}>
          <strong>Semáforo:</strong>
          <span>🟢 Bien (≥ {UMBRAL_VERDE})</span>
          <span>🟡 Medio ({UMBRAL_ROJO}–{UMBRAL_VERDE - 1})</span>
          <span>🔴 Bajo (&lt; {UMBRAL_ROJO})</span>
        </div>

        {/* Formulario */}
        {showForm && (
          <div className="card" style={{ marginBottom:'1.5rem' }}>
            <div className="card-header">
              <span className="card-title">{editandoId ? 'Editar producto' : 'Nuevo producto'}</span>
            </div>
            <form onSubmit={handleSubmit}>
              <div className="form-row">
                <div className="form-group">
                  <label className="form-label" htmlFor="productoId">ID de producto</label>
                  <input id="productoId" className="form-input" type="number" min={1}
                    placeholder="Ej: 101"
                    value={form.productoId}
                    onChange={e => setForm(f => ({ ...f, productoId: e.target.value }))} />
                </div>
                <div className="form-group">
                  <label className="form-label" htmlFor="stock">Stock</label>
                  <input id="stock" className="form-input" type="number" min={0}
                    placeholder="Ej: 50"
                    value={form.stock}
                    onChange={e => setForm(f => ({ ...f, stock: e.target.value }))} />
                </div>
              </div>
              <div className="form-group">
                <label className="form-label" htmlFor="nombre">Nombre del producto</label>
                <input id="nombre" className="form-input"
                  placeholder='Ej: Notebook Premium 15"'
                  value={form.nombre}
                  onChange={e => setForm(f => ({ ...f, nombre: e.target.value }))} />
              </div>
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
            <span className="card-title">Productos en inventario</span>
            <button className="btn btn-outline" onClick={cargar}
              style={{ padding:'.4rem .8rem', fontSize:'.8rem' }}>
              Actualizar
            </button>
          </div>
          {loading && <div className="loading">Cargando inventario...</div>}
          {!loading && items.length === 0 && (
            <div style={{ textAlign:'center', padding:'3rem', color:'var(--text-lt)' }}>
              No hay productos en el inventario.
            </div>
          )}
          {!loading && items.length > 0 && (
            <div className="table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>ID</th><th>Producto ID</th><th>Nombre</th><th>Stock</th><th>Estado</th><th>Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {items.map(i => {
                    const sem = semaforoStock(i.stock)
                    return (
                      <tr key={i.id}>
                        <td><span className="badge badge-blue">{i.id}</span></td>
                        <td><span className="badge badge-gold">{i.productoId}</span></td>
                        <td><strong>{i.nombre}</strong></td>
                        <td><span className={`badge ${sem.badge}`}>{i.stock}</span></td>
                        <td><span className={`badge ${sem.badge}`}>{sem.emoji} {sem.label}</span></td>
                        <td>
                          <div style={{ display:'flex', gap:'.4rem' }}>
                            <button className="btn btn-outline" onClick={() => abrirEditar(i)}
                              style={{ padding:'.3rem .7rem', fontSize:'.78rem' }}>Editar</button>
                            <button className="btn btn-danger" onClick={() => pedirConfirmEliminar(i)}
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
