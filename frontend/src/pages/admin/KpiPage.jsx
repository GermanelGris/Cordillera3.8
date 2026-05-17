import { useState, useEffect, useMemo } from 'react'
import Navbar from '../../components/Navbar'
import { kpiApi, datosApi } from '../../api/apiClient'

const TIPOS_CALCULO = ['PROMEDIO', 'SUMA', 'MAXIMO', 'MINIMO']

export default function KpiPage() {
  const [kpis, setKpis]       = useState([])
  const [datos, setDatos]     = useState([])
  const [loading, setLoading] = useState(true)
  const [loadingDatos, setLoadingDatos] = useState(false)
  const [error, setError]     = useState('')
  const [success, setSuccess] = useState('')
  const [showForm, setShowForm] = useState(false)
  const [saving, setSaving]   = useState(false)
  const [filtro, setFiltro]   = useState('')

  const [form, setForm] = useState({
    nombre: '', tipoCalculo: 'PROMEDIO', periodo: '', tipoDato: '', unidad: 'CLP', descripcion: ''
  })

  useEffect(() => { cargarKpis() }, [])

  useEffect(() => {
    if (showForm) cargarDatos()
  }, [showForm])

  const cargarKpis = async () => {
    try {
      setLoading(true)
      const res = await kpiApi.listar()
      setKpis(Array.isArray(res.data) ? res.data : [])
    } catch { setError('No se pudo conectar con el servicio de KPIs') }
    finally { setLoading(false) }
  }

  const cargarDatos = async () => {
    try {
      setLoadingDatos(true)
      const res = await datosApi.listar()
      setDatos(Array.isArray(res.data) ? res.data : [])
    } catch { setError('No se pudieron cargar los datos del sistema') }
    finally { setLoadingDatos(false) }
  }

  // Periodos únicos disponibles en MS-Data
  const periodosDisponibles = useMemo(() =>
    [...new Set(datos.map(d => d.periodo))].sort().reverse(),
    [datos]
  )

  // Tipos de dato disponibles para el periodo seleccionado
  const tiposDisponibles = useMemo(() => {
    if (!form.periodo) return []
    return [...new Set(datos.filter(d => d.periodo === form.periodo).map(d => d.tipo))].sort()
  }, [datos, form.periodo])

  // Valores concretos que se usarán para el cálculo (preview)
  const valoresPreview = useMemo(() => {
    if (!form.periodo || !form.tipoDato) return []
    return datos.filter(d => d.periodo === form.periodo && d.tipo === form.tipoDato)
  }, [datos, form.periodo, form.tipoDato])

  // Resultado estimado del cálculo
  const resultadoEstimado = useMemo(() => {
    if (!valoresPreview.length) return null
    const vals = valoresPreview.map(d => d.valor)
    switch (form.tipoCalculo) {
      case 'PROMEDIO': return vals.reduce((a, b) => a + b, 0) / vals.length
      case 'SUMA':     return vals.reduce((a, b) => a + b, 0)
      case 'MAXIMO':   return Math.max(...vals)
      case 'MINIMO':   return Math.min(...vals)
      default:         return null
    }
  }, [valoresPreview, form.tipoCalculo])

  const handlePeriodoChange = (periodo) => {
    setForm(f => ({ ...f, periodo, tipoDato: '' }))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!form.periodo || !form.tipoDato) {
      setError('Selecciona un periodo y tipo de dato')
      return
    }
    if (!valoresPreview.length) {
      setError('No hay datos disponibles para el periodo y tipo seleccionados')
      return
    }
    setSaving(true); setError(''); setSuccess('')
    try {
      await kpiApi.calcularDesdeDatos({
        tipoCalculo: form.tipoCalculo,
        tipoDato:    form.tipoDato,
        periodo:     form.periodo,
        nombre:      form.nombre,
      })
      setSuccess('✅ KPI calculado y guardado exitosamente')
      setShowForm(false)
      setForm({ nombre: '', tipoCalculo: 'PROMEDIO', periodo: '', tipoDato: '', unidad: 'CLP', descripcion: '' })
      cargarKpis()
    } catch (err) {
      setError(err.response?.data?.message || 'Error al calcular el KPI')
    } finally { setSaving(false) }
  }

  const periodos = [...new Set(kpis.map(k => k.periodo))].sort().reverse()
  const kpisFiltrados = filtro ? kpis.filter(k => k.periodo === filtro) : kpis

  return (
    <div className="page-wrapper">
      <Navbar />
      <div className="page-content">

        {/* Header */}
        <div style={{ display:'flex', justifyContent:'space-between', alignItems:'flex-start', marginBottom:'1.5rem' }}>
          <div>
            <h1 className="page-title">📊 Gestión de KPIs</h1>
            <p className="page-subtitle">Calcula y visualiza indicadores clave de desempeño</p>
          </div>
          <button className="btn btn-primary" onClick={() => setShowForm(v => !v)}>
            {showForm ? '✕ Cancelar' : '+ Nuevo KPI'}
          </button>
        </div>

        {error   && <div className="alert alert-error">{error}</div>}
        {success && <div className="alert alert-success">{success}</div>}

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
            <div className="stat-value" style={{ fontSize:'1.4rem' }}>{periodos[0] || '—'}</div>
            <div className="stat-label">Período más reciente</div>
          </div>
        </div>

        {/* Formulario */}
        {showForm && (
          <div className="card" style={{ marginBottom:'1.5rem' }}>
            <div className="card-header">
              <span className="card-title">Calcular nuevo KPI desde datos del sistema</span>
            </div>

            {loadingDatos ? (
              <div className="loading">Cargando datos disponibles...</div>
            ) : (
              <form onSubmit={handleSubmit}>

                {/* Fila 1: Nombre y Tipo de cálculo */}
                <div className="form-row">
                  <div className="form-group">
                    <label className="form-label">Nombre del KPI</label>
                    <input className="form-input" placeholder="Ej: Promedio de Ventas Mayo"
                      value={form.nombre} required
                      onChange={e => setForm(f => ({...f, nombre: e.target.value}))} />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Tipo de cálculo</label>
                    <select className="form-select" value={form.tipoCalculo}
                      onChange={e => setForm(f => ({...f, tipoCalculo: e.target.value}))}>
                      {TIPOS_CALCULO.map(t => <option key={t}>{t}</option>)}
                    </select>
                  </div>
                </div>

                {/* Fila 2: Periodo y Tipo de dato */}
                <div className="form-row">
                  <div className="form-group">
                    <label className="form-label">Periodo</label>
                    {periodosDisponibles.length === 0 ? (
                      <div style={{ padding:'.6rem', background:'#FFF3CD', borderRadius:6, fontSize:'.85rem', color:'#856404' }}>
                        ⚠️ No hay datos registrados en el sistema
                      </div>
                    ) : (
                      <select className="form-select" value={form.periodo} required
                        onChange={e => handlePeriodoChange(e.target.value)}>
                        <option value="">— Selecciona un periodo —</option>
                        {periodosDisponibles.map(p => <option key={p}>{p}</option>)}
                      </select>
                    )}
                  </div>
                  <div className="form-group">
                    <label className="form-label">Tipo de dato</label>
                    <select className="form-select" value={form.tipoDato} required
                      disabled={!form.periodo}
                      onChange={e => setForm(f => ({...f, tipoDato: e.target.value}))}>
                      <option value="">— Selecciona un tipo —</option>
                      {tiposDisponibles.map(t => <option key={t}>{t}</option>)}
                    </select>
                  </div>
                </div>

                {/* Preview de valores */}
                {valoresPreview.length > 0 && (
                  <div style={{
                    background:'#F0F8FF', border:'1.5px solid #2980B9', borderRadius:10,
                    padding:'1rem', marginBottom:'1rem'
                  }}>
                    <div style={{ fontWeight:700, color:'#1B4F72', marginBottom:'.6rem', fontSize:'.9rem' }}>
                      📋 Datos que se usarán para el cálculo
                    </div>
                    <div style={{ display:'flex', flexWrap:'wrap', gap:'.5rem', marginBottom:'.75rem' }}>
                      {valoresPreview.map(d => (
                        <div key={d.id} style={{
                          background:'#fff', border:'1px solid #AED6F1', borderRadius:6,
                          padding:'.35rem .7rem', fontSize:'.82rem'
                        }}>
                          <span style={{ color:'#555' }}>{d.fuente}</span>
                          <span style={{ fontWeight:700, color:'#1B4F72', marginLeft:'.5rem' }}>
                            {d.valor?.toLocaleString('es-CL', { maximumFractionDigits: 2 })}
                          </span>
                        </div>
                      ))}
                    </div>
                    <div style={{ display:'flex', gap:'1.5rem', fontSize:'.85rem' }}>
                      <span>
                        <span style={{ color:'#555' }}>Valores:</span>
                        <strong style={{ marginLeft:'.3rem' }}>{valoresPreview.length}</strong>
                      </span>
                      {resultadoEstimado !== null && (
                        <span>
                          <span style={{ color:'#555' }}>Resultado estimado ({form.tipoCalculo}):</span>
                          <strong style={{ marginLeft:'.3rem', color:'#1B4F72', fontSize:'1rem' }}>
                            {resultadoEstimado.toLocaleString('es-CL', { maximumFractionDigits: 2 })}
                          </strong>
                        </span>
                      )}
                    </div>
                  </div>
                )}

                {/* Fila 3: Descripción */}
                <div className="form-group">
                  <label className="form-label">Descripción <span style={{ color:'var(--text-lt)', fontWeight:400 }}>(opcional)</span></label>
                  <input className="form-input" placeholder="Descripción adicional del KPI"
                    value={form.descripcion}
                    onChange={e => setForm(f => ({...f, descripcion: e.target.value}))} />
                </div>

                <div className="flex-end">
                  <button type="submit" className="btn btn-primary"
                    disabled={saving || !valoresPreview.length}>
                    {saving ? 'Calculando...' : '📊 Calcular KPI'}
                  </button>
                </div>
              </form>
            )}
          </div>
        )}

        {/* Tabla de KPIs */}
        <div className="card">
          <div className="card-header">
            <span className="card-title">KPIs registrados</span>
            <div style={{ display:'flex', gap:'.75rem', alignItems:'center' }}>
              <select className="form-select"
                style={{ width:'auto', padding:'.4rem .8rem', fontSize:'.9rem' }}
                value={filtro} onChange={e => setFiltro(e.target.value)}>
                <option value="">Todos los periodos</option>
                {periodos.map(p => <option key={p}>{p}</option>)}
              </select>
              <button className="btn btn-outline" onClick={cargarKpis}
                style={{ padding:'.4rem .8rem', fontSize:'.85rem' }}>
                🔄 Actualizar
              </button>
            </div>
          </div>
          {loading ? (
            <div className="loading">Cargando KPIs...</div>
          ) : kpisFiltrados.length === 0 ? (
            <div style={{ textAlign:'center', padding:'2rem', color:'var(--text-lt)' }}>
              No hay KPIs registrados. ¡Crea el primero!
            </div>
          ) : (
            <div className="table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>ID</th><th>Nombre</th><th>Tipo</th><th>Valor</th><th>Unidad</th><th>Periodo</th><th>Descripción</th>
                  </tr>
                </thead>
                <tbody>
                  {kpisFiltrados.map(k => (
                    <tr key={k.id}>
                      <td><span className="badge badge-blue">{k.id}</span></td>
                      <td><strong>{k.nombre}</strong></td>
                      <td><span className="badge badge-gold">{k.tipoCalculo}</span></td>
                      <td><strong style={{ color:'var(--primary)' }}>
                        {k.valor?.toLocaleString('es-CL', { maximumFractionDigits: 2 })}
                      </strong></td>
                      <td>{k.unidad}</td>
                      <td><span className="badge badge-green">{k.periodo}</span></td>
                      <td style={{ color:'var(--text-lt)', fontSize:'.85rem' }}>{k.descripcion || '—'}</td>
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
