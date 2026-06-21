import { useState, useEffect, useMemo } from 'react'
import Navbar from '../../components/Navbar'
import ConfirmModal from '../../components/ConfirmModal'
import { kpiApi, datosApi } from '../../api/apiClient'
import { useToast } from '../../context/ToastContext'

const TIPOS_CALCULO = ['PROMEDIO', 'SUMA', 'MAXIMO', 'MINIMO']
const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

export default function KpiPage() {
  const { toast } = useToast()
  const [kpis, setKpis]       = useState([])
  const [datos, setDatos]     = useState([])
  const [loading, setLoading] = useState(true)
  const [loadingDatos, setLoadingDatos] = useState(false)
  const [showForm, setShowForm] = useState(false)
  const [saving, setSaving]   = useState(false)
  const [filtro, setFiltro]   = useState('')
  const [confirm, setConfirm] = useState(null)
  const [editando, setEditando] = useState(null)
  const [editForm, setEditForm] = useState({ nombre: '', descripcion: '' })
  const [form, setForm] = useState({
    nombre: '', tipoCalculo: 'PROMEDIO', periodo: '', tipoDato: '', descripcion: '', destinatario: ''
  })

  useEffect(() => { cargarKpis() }, [])
  useEffect(() => { if (showForm) cargarDatos() }, [showForm])

  const cargarKpis = async () => {
    try {
      setLoading(true)
      const res = await kpiApi.listar()
      setKpis(Array.isArray(res.data) ? res.data : [])
    } catch { toast.error('No se pudo conectar con el servicio de KPIs') }
    finally { setLoading(false) }
  }

  const cargarDatos = async () => {
    try {
      setLoadingDatos(true)
      const res = await datosApi.listar()
      setDatos(Array.isArray(res.data) ? res.data : [])
    } catch { toast.error('No se pudieron cargar los datos del sistema') }
    finally { setLoadingDatos(false) }
  }

  const periodosDisponibles = useMemo(() =>
    [...new Set(datos.map(d => d.periodo))].sort((a, b) => b.localeCompare(a)), [datos])

  const tiposDisponibles = useMemo(() => {
    if (!form.periodo) return []
    return [...new Set(datos.filter(d => d.periodo === form.periodo).map(d => d.tipo))].sort((a, b) => a.localeCompare(b))
  }, [datos, form.periodo])

  const valoresPreview = useMemo(() => {
    if (!form.periodo || !form.tipoDato) return []
    return datos.filter(d => d.periodo === form.periodo && d.tipo === form.tipoDato)
  }, [datos, form.periodo, form.tipoDato])

  const resultadoEstimado = useMemo(() => {
    if (!valoresPreview.length) return null
    const vals = valoresPreview.map(d => d.valor)
    if (form.tipoCalculo === 'PROMEDIO') return vals.reduce((a, b) => a + b, 0) / vals.length
    if (form.tipoCalculo === 'SUMA')     return vals.reduce((a, b) => a + b, 0)
    if (form.tipoCalculo === 'MAXIMO')   return Math.max(...vals)
    if (form.tipoCalculo === 'MINIMO')   return Math.min(...vals)
    return null
  }, [valoresPreview, form.tipoCalculo])

  const handlePeriodoChange = (periodo) => setForm(f => ({ ...f, periodo, tipoDato: '' }))

  const validarNuevoKpi = () => {
    if (!form.nombre.trim()) { toast.error('El nombre del KPI es obligatorio'); return false }
    if (!form.periodo)       { toast.error('Selecciona un periodo'); return false }
    if (!form.tipoDato)      { toast.error('Selecciona un tipo de dato'); return false }
    if (!valoresPreview.length) {
      toast.error('No hay datos disponibles para el periodo y tipo seleccionados')
      return false
    }
    if (form.destinatario.trim() && !EMAIL_RE.test(form.destinatario.trim())) {
      toast.error('El correo destinatario no es válido')
      return false
    }
    return true
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!validarNuevoKpi()) return
    setSaving(true)
    try {
      const destino = form.destinatario.trim()
      await kpiApi.calcularDesdeDatos({
        tipoCalculo: form.tipoCalculo,
        tipoDato:    form.tipoDato,
        periodo:     form.periodo,
        nombre:      form.nombre,
        ...(destino ? { destinatario: destino } : {}),
      })
      toast.success(destino
        ? `KPI calculado. Se enviará a ${destino} (PDF, CSV y XLSX)`
        : 'KPI calculado y guardado exitosamente')
      setShowForm(false)
      setForm({ nombre: '', tipoCalculo: 'PROMEDIO', periodo: '', tipoDato: '', descripcion: '', destinatario: '' })
      cargarKpis()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Error al calcular el KPI')
    } finally { setSaving(false) }
  }

  const abrirEditar = (k) => {
    setEditando(k.id)
    setEditForm({ nombre: k.nombre, descripcion: k.descripcion || '' })
  }

  const validarEdicion = () => {
    if (!editForm.nombre.trim()) { toast.error('El nombre del KPI no puede estar vacío'); return false }
    return true
  }

  const guardarEdicion = async () => {
    if (!validarEdicion()) return
    try {
      await kpiApi.actualizar(editando, { nombre: editForm.nombre.trim(), descripcion: editForm.descripcion })
      toast.success('KPI actualizado correctamente')
      setEditando(null)
      cargarKpis()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Error al actualizar el KPI')
    }
  }

  const pedirConfirmEliminar = (k) => {
    setConfirm({
      title: 'Eliminar KPI',
      message: `¿Estás seguro de que quieres eliminar el KPI "${k.nombre}" del periodo ${k.periodo}? Esta acción no se puede deshacer.`,
      confirmLabel: 'Eliminar',
      onConfirm: async () => {
        setConfirm(null)
        try {
          await kpiApi.eliminar(k.id)
          toast.success('KPI eliminado')
          if (editando === k.id) setEditando(null)
          cargarKpis()
        } catch (err) {
          toast.error(err.response?.data?.message || 'No se pudo eliminar el KPI')
        }
      },
    })
  }

  const periodos = [...new Set(kpis.map(k => k.periodo))].sort((a, b) => b.localeCompare(a))
  const kpisFiltrados = filtro ? kpis.filter(k => k.periodo === filtro) : kpis

  return (
    <div className="page-wrapper">
      <Navbar />
      {confirm && <ConfirmModal {...confirm} onCancel={() => setConfirm(null)} />}

      <div className="page-content">
        <div className="page-header">
          <div>
            <h1 className="page-title">Gestión de KPIs</h1>
            <p className="page-subtitle">Calcula y visualiza indicadores clave de desempeño</p>
          </div>
          <button className="btn btn-primary" onClick={() => setShowForm(v => !v)}>
            {showForm ? '✕ Cancelar' : '+ Nuevo KPI'}
          </button>
        </div>

        {/* Stats */}
        <div className="grid-3" style={{ marginBottom:'1.5rem' }}>
          <div className="stat-card">
            <div className="stat-value">{kpis.length}</div>
            <div className="stat-label">Total KPIs</div>
          </div>
          <div className="stat-card">
            <div className="stat-value">{periodos.length}</div>
            <div className="stat-label">Periodos</div>
          </div>
          <div className="stat-card">
            <div className="stat-value" style={{ fontSize:'1.3rem' }}>{periodos[0] || '—'}</div>
            <div className="stat-label">Período más reciente</div>
          </div>
        </div>

        {/* Formulario nuevo KPI */}
        {showForm && (
          <div className="card" style={{ marginBottom:'1.5rem' }}>
            <div className="card-header">
              <span className="card-title">Calcular nuevo KPI desde datos del sistema</span>
            </div>

            {loadingDatos ? (
              <div className="loading">Cargando datos disponibles...</div>
            ) : (
              <form onSubmit={handleSubmit}>
                <div className="form-row">
                  <div className="form-group">
                    <label className="form-label" htmlFor="kpi-nombre">Nombre del KPI</label>
                    <input id="kpi-nombre" className="form-input"
                      placeholder="Ej: Promedio de Ventas Mayo"
                      value={form.nombre}
                      onChange={e => setForm(f => ({...f, nombre: e.target.value}))} />
                  </div>
                  <div className="form-group">
                    <label className="form-label" htmlFor="kpi-tipo">Tipo de cálculo</label>
                    <select id="kpi-tipo" className="form-select" value={form.tipoCalculo}
                      onChange={e => setForm(f => ({...f, tipoCalculo: e.target.value}))}>
                      {TIPOS_CALCULO.map(t => <option key={t}>{t}</option>)}
                    </select>
                  </div>
                </div>

                <div className="form-row">
                  <div className="form-group">
                    <label className="form-label" htmlFor="kpi-periodo">Periodo</label>
                    {periodosDisponibles.length === 0 ? (
                      <div style={{ padding:'.65rem 1rem', background:'var(--accent)', border:'1px solid var(--border)', borderRadius:6, fontSize:'.85rem', color:'var(--text-lt)' }}>
                        No hay datos registrados en el sistema
                      </div>
                    ) : (
                      <select id="kpi-periodo" className="form-select" value={form.periodo}
                        onChange={e => handlePeriodoChange(e.target.value)}>
                        <option value="">— Selecciona un periodo —</option>
                        {periodosDisponibles.map(p => <option key={p}>{p}</option>)}
                      </select>
                    )}
                  </div>
                  <div className="form-group">
                    <label className="form-label" htmlFor="kpi-tipo-dato">Tipo de dato</label>
                    <select id="kpi-tipo-dato" className="form-select" value={form.tipoDato}
                      disabled={!form.periodo}
                      onChange={e => setForm(f => ({...f, tipoDato: e.target.value}))}>
                      <option value="">— Selecciona un tipo —</option>
                      {tiposDisponibles.map(t => <option key={t}>{t}</option>)}
                    </select>
                  </div>
                </div>

                {valoresPreview.length > 0 && (
                  <div style={{
                    background:'var(--accent)', border:'1px solid var(--border)',
                    borderRadius:8, padding:'1rem', marginBottom:'1rem'
                  }}>
                    <div style={{ fontWeight:700, color:'var(--text)', marginBottom:'.6rem', fontSize:'.8rem', textTransform:'uppercase', letterSpacing:'0.06em' }}>
                      Datos para el cálculo
                    </div>
                    <div style={{ display:'flex', flexWrap:'wrap', gap:'.5rem', marginBottom:'.75rem' }}>
                      {valoresPreview.map(d => (
                        <div key={d.id} style={{
                          background:'#fff', border:'1px solid var(--border)',
                          borderRadius:6, padding:'.35rem .7rem', fontSize:'.82rem'
                        }}>
                          <span style={{ color:'var(--text-lt)' }}>{d.fuente}</span>
                          <span style={{ fontWeight:700, color:'var(--text)', marginLeft:'.5rem' }}>
                            {d.valor?.toLocaleString('es-CL', { maximumFractionDigits: 2 })}
                          </span>
                        </div>
                      ))}
                    </div>
                    <div style={{ display:'flex', gap:'1.5rem', fontSize:'.82rem', color:'var(--text-lt)' }}>
                      <span>Valores: <strong style={{ color:'var(--text)' }}>{valoresPreview.length}</strong></span>
                      {resultadoEstimado !== null && (
                        <span>
                          Resultado estimado ({form.tipoCalculo}):
                          <strong style={{ marginLeft:'.3rem', color:'var(--text)', fontSize:'.95rem' }}>
                            {resultadoEstimado.toLocaleString('es-CL', { maximumFractionDigits: 2 })}
                          </strong>
                        </span>
                      )}
                    </div>
                  </div>
                )}

                <div className="form-group">
                  <label className="form-label" htmlFor="kpi-desc">
                    Descripción <span style={{ color:'var(--text-xs)', fontWeight:400, textTransform:'none', letterSpacing:0 }}>(opcional)</span>
                  </label>
                  <input id="kpi-desc" className="form-input" placeholder="Descripción adicional del KPI"
                    value={form.descripcion}
                    onChange={e => setForm(f => ({...f, descripcion: e.target.value}))} />
                </div>

                <div className="form-group">
                  <label className="form-label" htmlFor="kpi-mail">
                    Enviar por correo <span style={{ color:'var(--text-xs)', fontWeight:400, textTransform:'none', letterSpacing:0 }}>(opcional — adjunta PDF, CSV y XLSX)</span>
                  </label>
                  <input id="kpi-mail" type="email" className="form-input" placeholder="destinatario@ejemplo.com"
                    value={form.destinatario}
                    onChange={e => setForm(f => ({...f, destinatario: e.target.value}))} />
                </div>

                <div className="flex-end">
                  <button type="submit" className="btn btn-primary"
                    disabled={saving || !valoresPreview.length}>
                    {saving ? 'Calculando...' : 'Calcular KPI'}
                  </button>
                </div>
              </form>
            )}
          </div>
        )}

        {/* Tabla */}
        <div className="card">
          <div className="card-header">
            <span className="card-title">KPIs registrados</span>
            <div style={{ display:'flex', gap:'.75rem', alignItems:'center' }}>
              <select className="form-select"
                style={{ width:'auto', padding:'.4rem .8rem', fontSize:'.85rem' }}
                value={filtro} onChange={e => setFiltro(e.target.value)}>
                <option value="">Todos los periodos</option>
                {periodos.map(p => <option key={p}>{p}</option>)}
              </select>
              <button className="btn btn-outline" onClick={cargarKpis}
                style={{ padding:'.4rem .8rem', fontSize:'.8rem' }}>
                Actualizar
              </button>
            </div>
          </div>
          {loading && <div className="loading">Cargando KPIs...</div>}
          {!loading && kpisFiltrados.length === 0 && (
            <div style={{ textAlign:'center', padding:'3rem', color:'var(--text-lt)' }}>
              No hay KPIs registrados. ¡Crea el primero!
            </div>
          )}
          {!loading && kpisFiltrados.length > 0 && (
            <div className="table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>ID</th><th>Nombre</th><th>Tipo</th><th>Valor</th><th>Periodo</th><th>Descripción</th><th>Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {kpisFiltrados.map(k => (
                    <tr key={k.id}>
                      <td><span className="badge badge-blue">{k.id}</span></td>
                      <td>
                        {editando === k.id ? (
                          <input className="form-input" style={{ padding:'.3rem .6rem', fontSize:'.85rem', minWidth:180 }}
                            value={editForm.nombre}
                            onChange={e => setEditForm(f => ({ ...f, nombre: e.target.value }))} />
                        ) : (
                          <strong>{k.nombre}</strong>
                        )}
                      </td>
                      <td><span className="badge badge-gold">{k.tipoCalculo}</span></td>
                      <td><strong>{k.valor?.toLocaleString('es-CL', { maximumFractionDigits: 2 })}</strong></td>
                      <td><span className="badge badge-green">{k.periodo}</span></td>
                      <td style={{ color:'var(--text-lt)', fontSize:'.85rem' }}>
                        {editando === k.id ? (
                          <input className="form-input" style={{ padding:'.3rem .6rem', fontSize:'.85rem', minWidth:180 }}
                            placeholder="Descripción (opcional)"
                            value={editForm.descripcion}
                            onChange={e => setEditForm(f => ({ ...f, descripcion: e.target.value }))} />
                        ) : (
                          k.descripcion || '—'
                        )}
                      </td>
                      <td>
                        {editando === k.id ? (
                          <div style={{ display:'flex', gap:'.4rem' }}>
                            <button className="btn btn-primary" onClick={guardarEdicion}
                              style={{ padding:'.3rem .7rem', fontSize:'.78rem' }}>Guardar</button>
                            <button className="btn btn-outline" onClick={() => setEditando(null)}
                              style={{ padding:'.3rem .7rem', fontSize:'.78rem' }}>Cancelar</button>
                          </div>
                        ) : (
                          <div style={{ display:'flex', gap:'.4rem' }}>
                            <button className="btn btn-outline" onClick={() => abrirEditar(k)}
                              style={{ padding:'.3rem .7rem', fontSize:'.78rem' }}>Editar</button>
                            <button className="btn btn-danger" onClick={() => pedirConfirmEliminar(k)}
                              style={{ padding:'.3rem .7rem', fontSize:'.78rem' }}>Eliminar</button>
                          </div>
                        )}
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
