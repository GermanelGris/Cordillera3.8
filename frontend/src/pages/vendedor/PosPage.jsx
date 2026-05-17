import { useState, useEffect } from 'react'
import Navbar from '../../components/Navbar'
import { datosApi, inventarioApi } from '../../api/apiClient'
import { useAuth } from '../../context/AuthContext'

const PRODUCTOS_POS = [
  { id: 1, nombre: 'Notebook Premium 15"', precio: 899990, categoria: 'Tecnología' },
  { id: 2, nombre: 'Monitor UHD 27"',      precio: 349990, categoria: 'Tecnología' },
  { id: 3, nombre: 'Teclado Mecánico',     precio: 89990,  categoria: 'Accesorios' },
  { id: 4, nombre: 'Mouse Inalámbrico',    precio: 49990,  categoria: 'Accesorios' },
  { id: 5, nombre: 'Tablet Pro 11"',       precio: 599990, categoria: 'Tecnología' },
  { id: 6, nombre: 'Auriculares BT Pro',   precio: 129990, categoria: 'Audio'      },
  { id: 7, nombre: 'Webcam Full HD',       precio: 69990,  categoria: 'Accesorios' },
  { id: 8, nombre: 'SSD Externo 1TB',      precio: 119990, categoria: 'Almacen.'   },
]

const hoy = () => new Date().toISOString().slice(0, 7)

export default function PosPage() {
  const { user } = useAuth()
  const [carrito, setCarrito] = useState([])
  const [historial, setHistorial] = useState([])
  const [loading, setLoading]    = useState(false)
  const [success, setSuccess]    = useState('')
  const [error, setError]        = useState('')
  const [periodo, setPeriodo]    = useState(hoy())
  const [filtroHistorial, setFiltroHistorial] = useState([])
  const [loadingHist, setLoadingHist] = useState(true)

  useEffect(() => {
    cargarHistorial()
  }, [])

  const cargarHistorial = async () => {
    try {
      setLoadingHist(true)
      const res = await datosApi.listarPorPeriodo(hoy())
      setHistorial(res.data.filter(d => d.tipo === 'VENTAS'))
    } catch { /* no pasa nada si no hay datos */ }
    finally { setLoadingHist(false) }
  }

  const agregarAlCarrito = (prod) => {
    setCarrito(prev => {
      const existe = prev.find(p => p.id === prod.id)
      if (existe) return prev.map(p => p.id === prod.id ? { ...p, qty: p.qty + 1 } : p)
      return [...prev, { ...prod, qty: 1 }]
    })
  }

  const cambiarQty = (id, delta) => {
    setCarrito(prev =>
      prev.map(p => p.id === id ? { ...p, qty: Math.max(1, p.qty + delta) } : p)
    )
  }

  const eliminar = (id) => setCarrito(prev => prev.filter(p => p.id !== id))

  const total = carrito.reduce((s, p) => s + p.precio * p.qty, 0)

  const procesarVenta = async () => {
    if (!carrito.length) { setError('Agrega productos al carrito primero'); return }
    setLoading(true); setError(''); setSuccess('')
    try {
      for (const item of carrito) {
        await datosApi.registrar({
          fuente: `POS-${user.username}`,
          tipo: 'VENTAS',
          valor: item.precio * item.qty,
          periodo,
          descripcion: `${item.nombre} x${item.qty}`,
        })
        await inventarioApi.descontar(item.id, item.qty)
      }
      setSuccess(`✅ Venta procesada por $${total.toLocaleString('es-CL')} (${carrito.length} producto${carrito.length > 1 ? 's' : ''})`)
      setCarrito([])
      cargarHistorial()
    } catch {
      setError('Error al procesar la venta. Verifica la conexión.')
    } finally { setLoading(false) }
  }

  return (
    <div className="page-wrapper">
      <Navbar />
      <div className="page-content" style={{ maxWidth:1300 }}>
        <h1 className="page-title">🏪 Punto de Venta</h1>
        <p className="page-subtitle">Vendedor: <strong>{user.username}</strong> · Periodo: {periodo}</p>

        {error   && <div className="alert alert-error">{error}</div>}
        {success && <div className="alert alert-success">{success}</div>}

        <div style={{ display:'grid', gridTemplateColumns:'1fr 380px', gap:'1.5rem', alignItems:'start' }}>

          {/* Catálogo */}
          <div>
            <div className="card">
              <div className="card-header"><span className="card-title">Catálogo de productos</span></div>
              <div style={{ display:'grid', gridTemplateColumns:'repeat(2,1fr)', gap:'1rem' }}>
                {PRODUCTOS_POS.map(p => (
                  <div key={p.id}
                    onClick={() => agregarAlCarrito(p)}
                    style={{
                      border:'1.5px solid var(--border)', borderRadius:10, padding:'1rem',
                      cursor:'pointer', transition:'all .18s', background:'#fff',
                    }}
                    onMouseEnter={e => e.currentTarget.style.borderColor = 'var(--primary)'}
                    onMouseLeave={e => e.currentTarget.style.borderColor = 'var(--border)'}
                  >
                    <div style={{ fontWeight:700, marginBottom:'.25rem' }}>{p.nombre}</div>
                    <div style={{ fontSize:'.8rem', color:'var(--text-lt)' }}>{p.categoria}</div>
                    <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center', marginTop:'.5rem' }}>
                      <span style={{ fontWeight:800, color:'var(--primary)' }}>
                        ${p.precio.toLocaleString('es-CL')}
                      </span>
                      <button className="btn btn-primary" style={{ padding:'.3rem .7rem', fontSize:'.82rem' }}>
                        + Agregar
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Historial */}
            <div className="card" style={{ marginTop:'1.5rem' }}>
              <div className="card-header">
                <span className="card-title">Ventas del período {hoy()}</span>
                <button className="btn btn-outline" onClick={cargarHistorial} style={{ padding:'.4rem .8rem', fontSize:'.85rem' }}>🔄</button>
              </div>
              {loadingHist ? <div className="loading">Cargando...</div> : historial.length === 0 ? (
                <div style={{ textAlign:'center', padding:'1.5rem', color:'var(--text-lt)' }}>
                  No hay ventas registradas hoy
                </div>
              ) : (
                <div className="table-wrapper">
                  <table>
                    <thead><tr><th>ID</th><th>Descripción</th><th>Vendedor</th><th>Monto</th><th>Fecha</th></tr></thead>
                    <tbody>
                      {historial.map(v => (
                        <tr key={v.id}>
                          <td><span className="badge badge-blue">{v.id}</span></td>
                          <td>{v.descripcion}</td>
                          <td style={{ fontSize:'.85rem', color:'var(--text-lt)' }}>{v.fuente}</td>
                          <td><strong style={{ color:'var(--green)' }}>${v.valor?.toLocaleString('es-CL')}</strong></td>
                          <td style={{ fontSize:'.82rem', color:'var(--text-lt)' }}>{v.createdAt?.split('T')[0]}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          </div>

          {/* Carrito */}
          <div style={{ position:'sticky', top:80 }}>
            <div className="card">
              <div className="card-header">
                <span className="card-title">🛒 Carrito ({carrito.length})</span>
                {carrito.length > 0 && (
                  <button onClick={() => setCarrito([])}
                    style={{ fontSize:'.8rem', color:'var(--red)', background:'none', border:'none', cursor:'pointer', fontWeight:600 }}>
                    Limpiar
                  </button>
                )}
              </div>

              {carrito.length === 0 ? (
                <div style={{ textAlign:'center', padding:'2rem', color:'var(--text-lt)' }}>
                  <div style={{ fontSize:'2.5rem', marginBottom:'.5rem' }}>🛒</div>
                  Haz clic en un producto para agregarlo
                </div>
              ) : (
                <>
                  <div style={{ display:'flex', flexDirection:'column', gap:'.75rem', marginBottom:'1rem' }}>
                    {carrito.map(item => (
                      <div key={item.id} style={{ display:'flex', alignItems:'center', gap:'.75rem',
                        padding:'.75rem', background:'#F8F9FA', borderRadius:8 }}>
                        <div style={{ flex:1 }}>
                          <div style={{ fontWeight:600, fontSize:'.9rem' }}>{item.nombre}</div>
                          <div style={{ fontSize:'.8rem', color:'var(--text-lt)' }}>
                            ${item.precio.toLocaleString('es-CL')} c/u
                          </div>
                        </div>
                        <div style={{ display:'flex', alignItems:'center', gap:'.4rem' }}>
                          <button onClick={() => cambiarQty(item.id, -1)}
                            style={{ width:24, height:24, border:'1px solid var(--border)', borderRadius:4,
                              cursor:'pointer', background:'#fff', fontWeight:700 }}>−</button>
                          <span style={{ fontWeight:700, minWidth:20, textAlign:'center' }}>{item.qty}</span>
                          <button onClick={() => cambiarQty(item.id, 1)}
                            style={{ width:24, height:24, border:'1px solid var(--border)', borderRadius:4,
                              cursor:'pointer', background:'#fff', fontWeight:700 }}>+</button>
                        </div>
                        <div style={{ fontWeight:800, color:'var(--primary)', minWidth:80, textAlign:'right', fontSize:'.9rem' }}>
                          ${(item.precio * item.qty).toLocaleString('es-CL')}
                        </div>
                        <button onClick={() => eliminar(item.id)}
                          style={{ background:'none', border:'none', cursor:'pointer', color:'var(--red)', fontSize:'1rem' }}>✕</button>
                      </div>
                    ))}
                  </div>

                  <div style={{ borderTop:'2px solid var(--border)', paddingTop:'1rem', marginBottom:'1rem' }}>
                    <div style={{ display:'flex', justifyContent:'space-between', fontWeight:800, fontSize:'1.2rem' }}>
                      <span>Total</span>
                      <span style={{ color:'var(--primary)' }}>${total.toLocaleString('es-CL')}</span>
                    </div>
                    <div style={{ fontSize:'.8rem', color:'var(--text-lt)', marginTop:'.25rem' }}>
                      {carrito.reduce((s,p) => s + p.qty, 0)} unidades · IVA incluido
                    </div>
                  </div>
                  <button className="btn btn-success" style={{ width:'100%' }}
                    onClick={procesarVenta} disabled={loading}>
                    {loading ? 'Procesando...' : '✅ Confirmar venta'}
                  </button>
                </>
              )}
            </div>
          </div>
        </div>
      </div>
      <footer><p>© 2026 Grupo Cordillera</p></footer>
    </div>
  )
}
