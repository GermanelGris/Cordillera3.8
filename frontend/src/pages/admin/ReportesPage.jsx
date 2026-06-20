import { useState, useEffect } from 'react'
import Navbar from '../../components/Navbar'
import ConfirmModal from '../../components/ConfirmModal'
import { reportesApi } from '../../api/apiClient'
import { useAuth } from '../../context/AuthContext'
import { useToast } from '../../context/ToastContext'

const PERIODO_RE = /^\d{4}-\d{2}$/
const TIPOS = ['KPI', 'MENSUAL', 'RESUMEN']
const TIPO_INFO = {
  KPI:     { icon: '📊', color: '#111827', desc: 'Indicadores de KPI del periodo' },
  MENSUAL: { icon: '📅', color: '#111827', desc: 'Informe mensual completo'        },
  RESUMEN: { icon: '📋', color: '#111827', desc: 'Resumen ejecutivo consolidado'   },
}

function descargarCSV(reportes) {
  const encabezado = ['ID', 'Tipo', 'Título', 'Periodo', 'Estado', 'Generado por', 'Fecha', 'Contenido']
  const filas = reportes.map(r => [
    r.id, r.tipo,
    `"${(r.titulo || '').replaceAll('"', '""')}"`,
    r.periodo, r.estado,
    `"${(r.generadoPor || '').replaceAll('"', '""')}"`,
    r.createdAt ? r.createdAt.split('T')[0] : '',
    `"${(r.contenido || '').replaceAll('"', '""').replaceAll('\n', ' ')}"`,
  ])
  const csv = [encabezado.join(','), ...filas.map(f => f.join(','))].join('\n')
  const blob = new Blob(['﻿' + csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `reportes_cordillera_${new Date().toISOString().slice(0,10)}.csv`
  a.click()
  URL.revokeObjectURL(url)
}

function descargarPDF(reporte) {
  const fecha = reporte.createdAt ? reporte.createdAt.split('T')[0] : ''
  const html = `<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>${reporte.titulo}</title>
  <style>
    * { box-sizing: border-box; margin: 0; padding: 0; }
    body { font-family: 'Segoe UI', Arial, sans-serif; color: #111827; padding: 40px; font-size: 13px; }
    .header { border-bottom: 2px solid #111827; padding-bottom: 16px; margin-bottom: 24px; }
    .logo { font-size: 20px; font-weight: 700; color: #111827; letter-spacing: -0.5px; }
    h1 { font-size: 17px; margin-top: 10px; color: #111827; }
    .meta { display: flex; gap: 12px; margin: 16px 0; flex-wrap: wrap; }
    .badge { background: #F3F4F6; color: #111827; border-radius: 4px; padding: 3px 10px; font-size: 11px; font-weight: 700; border: 1px solid #E5E7EB; }
    .content-box { background: #F9FAFB; border: 1px solid #E5E7EB; border-radius: 6px; padding: 20px; margin-top: 20px; white-space: pre-wrap; font-family: 'Courier New', monospace; font-size: 12px; line-height: 1.6; }
    .footer { margin-top: 32px; border-top: 1px solid #E5E7EB; padding-top: 12px; font-size: 11px; color: #6B7280; display: flex; justify-content: space-between; }
    @media print { body { padding: 20px; } }
  </style>
</head>
<body>
  <div class="header">
    <div class="logo">Cordillera Analytics</div>
    <h1>${reporte.titulo}</h1>
  </div>
  <div class="meta">
    <span class="badge">${TIPO_INFO[reporte.tipo]?.icon || '📄'} ${reporte.tipo}</span>
    <span class="badge">Periodo: ${reporte.periodo}</span>
    <span class="badge">Estado: ${reporte.estado}</span>
  </div>
  <p><strong>Generado por:</strong> ${reporte.generadoPor || '—'} &nbsp;|&nbsp; <strong>Fecha:</strong> ${fecha}</p>
  <div class="content-box">${(reporte.contenido || 'Sin contenido').replaceAll('<', '&lt;').replaceAll('>', '&gt;')}</div>
  <div class="footer">
    <span>Reporte #${reporte.id} — Sistema Cordillera</span>
    <span>Generado el ${new Date().toLocaleDateString('es-CL')}</span>
  </div>
  <script>window.onload = () => { window.print(); }</` + `script>
</body>
</html>`
  const blob = new Blob([html], { type: 'text/html;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  globalThis.open(url, '_blank', 'width=800,height=700')
  setTimeout(() => URL.revokeObjectURL(url), 15000)
}

function tipoBadge(tipo) {
  if (tipo === 'KPI')     return 'badge-blue'
  if (tipo === 'MENSUAL') return 'badge-green'
  return 'badge-gold'
}

export default function ReportesPage() {
  const { user } = useAuth()
  const { toast } = useToast()
  const [reportes, setReportes] = useState([])
  const [loading, setLoading]   = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [selected, setSelected] = useState(null)
  const [saving, setSaving]     = useState(false)
  const [confirm, setConfirm]   = useState(null)
  const [editandoId, setEditandoId] = useState(null)
  const [editTitulo, setEditTitulo] = useState('')
  const [form, setForm] = useState({ tipo: 'RESUMEN', titulo: '', periodo: '', descripcionAdicional: '' })

  useEffect(() => { cargarReportes() }, [])

  const cargarReportes = async () => {
    try {
      setLoading(true)
      const res = await reportesApi.listar()
      setReportes(Array.isArray(res.data) ? res.data : [])
    } catch { toast.error('No se pudo conectar con el servicio de reportes') }
    finally { setLoading(false) }
  }

  const validarForm = () => {
    if (!form.titulo.trim()) {
      toast.error('El título del reporte es obligatorio')
      return false
    }
    if (!PERIODO_RE.test(form.periodo.trim())) {
      toast.error('El periodo debe tener el formato YYYY-MM (ej: 2026-05)')
      return false
    }
    return true
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!validarForm()) return
    setSaving(true)
    try {
      await reportesApi.generar({ ...form, generadoPor: user?.nombre })
      toast.success('Reporte generado exitosamente')
      setShowForm(false)
      setForm({ tipo: 'RESUMEN', titulo: '', periodo: '', descripcionAdicional: '' })
      cargarReportes()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Error al generar el reporte')
    } finally { setSaving(false) }
  }

  const abrirEditar = (r) => {
    setEditandoId(r.id)
    setEditTitulo(r.titulo)
  }

  const validarEdicion = () => {
    if (!editTitulo.trim()) { toast.error('El título no puede estar vacío'); return false }
    return true
  }

  const guardarEdicion = async () => {
    if (!validarEdicion()) return
    try {
      const updated = await reportesApi.actualizar(editandoId, { titulo: editTitulo.trim() })
      toast.success('Reporte actualizado correctamente')
      setEditandoId(null)
      if (selected?.id === editandoId) setSelected(updated.data)
      cargarReportes()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Error al actualizar el reporte')
    }
  }

  const pedirConfirmEliminar = (r) => {
    setConfirm({
      title: 'Eliminar reporte',
      message: `¿Estás seguro de que quieres eliminar "${r.titulo}" del periodo ${r.periodo}? Esta acción no se puede deshacer.`,
      confirmLabel: 'Eliminar',
      onConfirm: async () => {
        setConfirm(null)
        try {
          await reportesApi.eliminar(r.id)
          toast.success('Reporte eliminado')
          if (selected?.id === r.id) setSelected(null)
          cargarReportes()
        } catch (err) {
          toast.error(err.response?.data?.message || 'No se pudo eliminar el reporte')
        }
      },
    })
  }

  return (
    <div className="page-wrapper">
      <Navbar />
      {confirm && <ConfirmModal {...confirm} onCancel={() => setConfirm(null)} />}

      <div className="page-content">
        <div className="page-header">
          <div>
            <h1 className="page-title">Reportes</h1>
            <p className="page-subtitle">Genera y visualiza reportes empresariales</p>
          </div>
          <div style={{ display:'flex', gap:'.75rem' }}>
            {reportes.length > 0 && (
              <button className="btn btn-outline" onClick={() => descargarCSV(reportes)}>
                Exportar CSV
              </button>
            )}
            <button className="btn btn-primary" onClick={() => { setShowForm(!showForm); setSelected(null) }}>
              {showForm ? '✕ Cancelar' : '+ Nuevo Reporte'}
            </button>
          </div>
        </div>

        {/* Formulario */}
        {showForm && (
          <div className="card" style={{ marginBottom:'1.5rem' }}>
            <div className="card-header">
              <span className="card-title">Generar nuevo reporte</span>
            </div>

            <div className="grid-3" style={{ marginBottom:'1.5rem' }}>
              {TIPOS.map(t => (
                <button key={t} type="button"
                  onClick={() => setForm({...form, tipo: t})}
                  style={{
                    border: `1px solid ${form.tipo === t ? 'var(--primary)' : 'var(--border)'}`,
                    borderRadius: 8, padding:'1.25rem', cursor:'pointer', textAlign:'center',
                    background: form.tipo === t ? 'var(--accent)' : '#fff',
                    transition: 'all .15s', font:'inherit',
                  }}>
                  <div style={{ fontSize:'1.75rem' }}>{TIPO_INFO[t].icon}</div>
                  <div style={{ fontWeight:700, color:'var(--text)', marginTop:'.5rem', fontSize:'.9rem' }}>{t}</div>
                  <div style={{ fontSize:'.78rem', color:'var(--text-lt)', marginTop:'.25rem' }}>{TIPO_INFO[t].desc}</div>
                </button>
              ))}
            </div>

            <form onSubmit={handleSubmit}>
              <div className="form-row">
                <div className="form-group">
                  <label className="form-label" htmlFor="rep-titulo">Título del reporte</label>
                  <input id="rep-titulo" className="form-input"
                    placeholder="Ej: Informe Ejecutivo Mayo 2026"
                    value={form.titulo}
                    onChange={e => setForm({...form, titulo: e.target.value})} />
                </div>
                <div className="form-group">
                  <label className="form-label" htmlFor="rep-periodo">Periodo (YYYY-MM)</label>
                  <input id="rep-periodo" className="form-input" placeholder="2026-05"
                    value={form.periodo}
                    onChange={e => setForm({...form, periodo: e.target.value})} />
                </div>
              </div>
              <div className="form-group">
                <label className="form-label" htmlFor="rep-obs">Observaciones adicionales</label>
                <textarea id="rep-obs" className="form-input"
                  style={{ height:'80px', resize:'vertical' }}
                  placeholder="Comentarios o conclusiones adicionales..."
                  value={form.descripcionAdicional}
                  onChange={e => setForm({...form, descripcionAdicional: e.target.value})} />
              </div>
              <div className="flex-end">
                <button type="submit" className="btn btn-primary" disabled={saving}>
                  {saving ? 'Generando...' : `${TIPO_INFO[form.tipo].icon} Generar reporte ${form.tipo}`}
                </button>
              </div>
            </form>
          </div>
        )}

        <div className="grid-2" style={{ alignItems:'flex-start' }}>
          {/* Lista */}
          <div className="card">
            <div className="card-header">
              <span className="card-title">Reportes generados ({reportes.length})</span>
              <button className="btn btn-outline" onClick={cargarReportes}
                style={{ padding:'.4rem .8rem', fontSize:'.8rem' }}>Actualizar</button>
            </div>
            {loading && <div className="loading">Cargando...</div>}
            {!loading && reportes.length === 0 && (
              <div style={{ textAlign:'center', padding:'2.5rem', color:'var(--text-lt)' }}>
                No hay reportes aún
              </div>
            )}
            {!loading && reportes.length > 0 && (
              <div style={{ display:'flex', flexDirection:'column', gap:'.5rem' }}>
                {reportes.map(r => (
                  <div key={r.id}
                    style={{
                      padding:'1rem', borderRadius:8,
                      border: `1px solid ${selected?.id === r.id ? 'var(--primary)' : 'var(--border)'}`,
                      background: selected?.id === r.id ? 'var(--accent)' : '#fff',
                      transition:'all .15s',
                    }}>
                    {editandoId === r.id ? (
                      <div>
                        <input className="form-input" style={{ marginBottom:'.5rem', fontSize:'.875rem' }}
                          value={editTitulo}
                          onChange={e => setEditTitulo(e.target.value)} />
                        <div style={{ display:'flex', gap:'.4rem' }}>
                          <button className="btn btn-primary" onClick={guardarEdicion}
                            style={{ padding:'.3rem .7rem', fontSize:'.78rem' }}>Guardar</button>
                          <button className="btn btn-outline" onClick={() => setEditandoId(null)}
                            style={{ padding:'.3rem .7rem', fontSize:'.78rem' }}>Cancelar</button>
                        </div>
                      </div>
                    ) : (
                      <>
                        <button type="button" onClick={() => setSelected(r)}
                          style={{ background:'none', border:'none', cursor:'pointer', width:'100%', textAlign:'left', padding:0, font:'inherit' }}>
                          <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center' }}>
                            <span style={{ fontWeight:600, fontSize:'.875rem' }}>
                              {TIPO_INFO[r.tipo]?.icon || '📄'} {r.titulo}
                            </span>
                            <span className={`badge ${tipoBadge(r.tipo)}`}>{r.tipo}</span>
                          </div>
                          <div style={{ fontSize:'.78rem', color:'var(--text-lt)', marginTop:'.3rem' }}>
                            {r.periodo} · {r.generadoPor}
                          </div>
                        </button>
                        <div style={{ display:'flex', gap:'.4rem', marginTop:'.5rem' }}>
                          <button className="btn btn-outline" onClick={() => abrirEditar(r)}
                            style={{ padding:'.25rem .6rem', fontSize:'.75rem' }}>Editar</button>
                          <button className="btn btn-danger" onClick={() => pedirConfirmEliminar(r)}
                            style={{ padding:'.25rem .6rem', fontSize:'.75rem' }}>Eliminar</button>
                        </div>
                      </>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Detalle */}
          <div className="card">
            <div className="card-header">
              <span className="card-title">Contenido del reporte</span>
              {selected && (
                <div style={{ display:'flex', gap:'.5rem' }}>
                  <button className="btn btn-outline"
                    onClick={() => descargarCSV([selected])}
                    style={{ padding:'.35rem .75rem', fontSize:'.78rem' }}>
                    CSV
                  </button>
                  <button className="btn btn-primary"
                    onClick={() => descargarPDF(selected)}
                    style={{ padding:'.35rem .75rem', fontSize:'.78rem' }}>
                    PDF
                  </button>
                </div>
              )}
            </div>
            {selected ? (
              <div>
                <h3 style={{ color:'var(--text)', marginBottom:'.5rem', fontWeight:700 }}>{selected.titulo}</h3>
                <div style={{ marginBottom:'.75rem', display:'flex', gap:'.5rem', flexWrap:'wrap' }}>
                  <span className={`badge ${tipoBadge(selected.tipo)}`}>{selected.tipo}</span>
                  <span className="badge badge-green">{selected.periodo}</span>
                  <span className="badge badge-gold">{selected.estado}</span>
                </div>
                <pre style={{
                  background:'var(--accent)', borderRadius:8, padding:'1rem',
                  fontFamily:'monospace', fontSize:'.82rem', whiteSpace:'pre-wrap',
                  maxHeight:360, overflowY:'auto', color:'var(--text)',
                  border:'1px solid var(--border)',
                }}>
                  {selected.contenido || 'Sin contenido'}
                </pre>
                <p style={{ fontSize:'.78rem', color:'var(--text-lt)', marginTop:'.5rem' }}>
                  Generado por: {selected.generadoPor} · {selected.createdAt?.split('T')[0]}
                </p>
              </div>
            ) : (
              <div style={{ textAlign:'center', padding:'3rem', color:'var(--text-lt)' }}>
                <div style={{ fontSize:'2.5rem', marginBottom:'1rem' }}>📋</div>
                Selecciona un reporte para ver su contenido
              </div>
            )}
          </div>
        </div>
      </div>
      <footer><p>© 2026 Grupo Cordillera</p></footer>
    </div>
  )
}
