import { useState, useEffect } from 'react'
import Navbar from '../../components/Navbar'
import { mailConfigApi } from '../../api/apiClient'
import { useToast } from '../../context/ToastContext'

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

const VACIO = {
  host: '', port: 587, username: '', password: '',
  fromAddress: '', auth: true, starttls: true,
}

export default function ConfiguracionCorreoPage() {
  const { toast } = useToast()
  const [form, setForm]       = useState(VACIO)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving]   = useState(false)
  const [testing, setTesting] = useState(false)
  const [passwordConfigurada, setPasswordConfigurada] = useState(false)
  const [updatedAt, setUpdatedAt] = useState(null)
  const [destinoPrueba, setDestinoPrueba] = useState('')

  useEffect(() => { cargar() }, [])

  const cargar = async () => {
    try {
      setLoading(true)
      const res = await mailConfigApi.obtener()
      const d = res.data || {}
      setForm({
        host: d.host || '',
        port: d.port || 587,
        username: d.username || '',
        password: '',
        fromAddress: d.fromAddress || '',
        auth: d.auth ?? true,
        starttls: d.starttls ?? true,
      })
      setPasswordConfigurada(!!d.passwordConfigurada)
      setUpdatedAt(d.updatedAt || null)
    } catch {
      toast.error('No se pudo cargar la configuración de correo')
    } finally { setLoading(false) }
  }

  const validar = () => {
    if (!form.host.trim()) { toast.error('El host SMTP es obligatorio'); return false }
    const p = Number(form.port)
    if (!Number.isInteger(p) || p < 1 || p > 65535) { toast.error('El puerto debe estar entre 1 y 65535'); return false }
    if (form.fromAddress && !EMAIL_RE.test(form.fromAddress)) { toast.error('El remitente no es un correo válido'); return false }
    return true
  }

  const guardar = async (e) => {
    e.preventDefault()
    if (!validar()) return
    setSaving(true)
    try {
      const payload = { ...form, port: Number(form.port) }
      // No enviar contraseña vacía: el backend conserva la existente
      if (!payload.password) delete payload.password
      const res = await mailConfigApi.guardar(payload)
      toast.success('Configuración de correo guardada')
      setForm(f => ({ ...f, password: '' }))
      setPasswordConfigurada(!!res.data?.passwordConfigurada)
      setUpdatedAt(res.data?.updatedAt || null)
    } catch (err) {
      toast.error(err.response?.data?.message || 'Error al guardar la configuración')
    } finally { setSaving(false) }
  }

  const probar = async () => {
    if (!EMAIL_RE.test(destinoPrueba)) { toast.error('Ingresa un correo de destino válido para la prueba'); return }
    setTesting(true)
    try {
      const res = await mailConfigApi.probar(destinoPrueba)
      if (res.data?.estado === 'OK') toast.success(res.data.mensaje)
      else toast.error(res.data?.mensaje || 'No se pudo enviar el correo de prueba')
    } catch (err) {
      toast.error(err.response?.data?.mensaje || err.response?.data?.message || 'Error enviando el correo de prueba')
    } finally { setTesting(false) }
  }

  return (
    <div className="page-wrapper">
      <Navbar />
      <div className="page-content">
        <div className="page-header">
          <div>
            <h1 className="page-title">Configuración de Correo</h1>
            <p className="page-subtitle">Servidor SMTP usado para el envío de reportes por correo</p>
          </div>
        </div>

        {loading ? (
          <div className="loading">Cargando...</div>
        ) : (
          <div className="grid-2" style={{ alignItems:'flex-start' }}>
            {/* Formulario de configuración */}
            <div className="card">
              <div className="card-header">
                <span className="card-title">Servidor SMTP</span>
                {updatedAt && (
                  <span style={{ fontSize:'.75rem', color:'var(--text-lt)' }}>
                    Actualizado: {updatedAt.split('T')[0]}
                  </span>
                )}
              </div>
              <form onSubmit={guardar}>
                <div className="form-row">
                  <div className="form-group">
                    <label className="form-label" htmlFor="mc-host">Host SMTP</label>
                    <input id="mc-host" className="form-input" placeholder="smtp.gmail.com"
                      value={form.host} onChange={e => setForm({ ...form, host: e.target.value })} />
                  </div>
                  <div className="form-group">
                    <label className="form-label" htmlFor="mc-port">Puerto</label>
                    <input id="mc-port" type="number" className="form-input" placeholder="587"
                      value={form.port} onChange={e => setForm({ ...form, port: e.target.value })} />
                  </div>
                </div>

                <div className="form-group">
                  <label className="form-label" htmlFor="mc-user">Usuario (correo de la cuenta)</label>
                  <input id="mc-user" className="form-input" placeholder="cuenta@gmail.com"
                    value={form.username} onChange={e => setForm({ ...form, username: e.target.value })} />
                </div>

                <div className="form-group">
                  <label className="form-label" htmlFor="mc-pass">
                    Contraseña {passwordConfigurada && <span style={{ color:'var(--text-lt)', fontWeight:400 }}>(hay una configurada — déjala vacía para mantenerla)</span>}
                  </label>
                  <input id="mc-pass" type="password" className="form-input"
                    placeholder={passwordConfigurada ? '••••••••' : 'App Password'}
                    value={form.password} onChange={e => setForm({ ...form, password: e.target.value })} />
                </div>

                <div className="form-group">
                  <label className="form-label" htmlFor="mc-from">Remitente (From)</label>
                  <input id="mc-from" className="form-input" placeholder="no-reply@cordillera.com"
                    value={form.fromAddress} onChange={e => setForm({ ...form, fromAddress: e.target.value })} />
                </div>

                <div style={{ display:'flex', gap:'1.5rem', margin:'.5rem 0 1.25rem' }}>
                  <label style={{ display:'flex', alignItems:'center', gap:'.4rem', cursor:'pointer' }}>
                    <input type="checkbox" checked={form.auth}
                      onChange={e => setForm({ ...form, auth: e.target.checked })} />
                    Requiere autenticación
                  </label>
                  <label style={{ display:'flex', alignItems:'center', gap:'.4rem', cursor:'pointer' }}>
                    <input type="checkbox" checked={form.starttls}
                      onChange={e => setForm({ ...form, starttls: e.target.checked })} />
                    STARTTLS
                  </label>
                </div>

                <div className="flex-end">
                  <button type="submit" className="btn btn-primary" disabled={saving}>
                    {saving ? 'Guardando...' : 'Guardar configuración'}
                  </button>
                </div>
              </form>
            </div>

            {/* Prueba de envío */}
            <div className="card">
              <div className="card-header">
                <span className="card-title">Probar configuración</span>
              </div>
              <p style={{ fontSize:'.85rem', color:'var(--text-lt)', marginBottom:'1rem' }}>
                Envía un correo de prueba para verificar que el servidor SMTP está bien configurado.
                Guarda primero los cambios.
              </p>
              <div className="form-group">
                <label className="form-label" htmlFor="mc-test">Correo de destino</label>
                <input id="mc-test" className="form-input" placeholder="destino@ejemplo.com"
                  value={destinoPrueba} onChange={e => setDestinoPrueba(e.target.value)} />
              </div>
              <div className="flex-end">
                <button type="button" className="btn btn-outline" onClick={probar} disabled={testing}>
                  {testing ? 'Enviando...' : 'Enviar correo de prueba'}
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
      <footer><p>© 2026 Grupo Cordillera</p></footer>
    </div>
  )
}
