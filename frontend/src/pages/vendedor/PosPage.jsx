import { useState, useEffect } from 'react'
import Navbar from '../../components/Navbar'
import { datosApi, inventarioApi } from '../../api/apiClient'
import { useAuth } from '../../context/AuthContext'
import { useToast } from '../../context/ToastContext'

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
  const { toast } = useToast()
  const [carrito, setCarrito]   = useState([])
  const [historial, setHistorial] = useState([])
  const [loading, setLoading]   = useState(false)
  const [loadingHist, setLoadingHist] = useState(true)
  const periodo = hoy()

  useEffect(() => { cargarHistorial() }, [])

  const cargarHistorial = async () => {
    try {
      setLoadingHist(true)
      const res = await datosApi.listarPorPeriodo(hoy())
      setHistorial(res.data.filter(d => d.tipo === 'VENTAS'))
    } catch { /* sin historial */ }
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
    setCarrito(prev => prev.map(p => p.id === id ? { ...p, qty: Math.max(1, p.qty + delta) } : p))
  }

  const eliminar = (id) => setCarrito(prev => prev.filter(p => p.id !== id))

  const total = carrito.reduce((s, p) => s + p.precio * p.qty, 0)
  const totalUnidades = carrito.reduce((s, p) => s + p.qty, 0)

  const procesarVenta = async () => {
    if (!carrito.length) { toast.error('Agrega productos al carrito primero'); return }
    setLoading(true)
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
      toast.success(`Venta procesada por $${total.toLocaleString('es-CL')}`)
      setCarrito([])
      cargarHistorial()
    } catch {
      toast.error('Error al procesar la venta. Verifica la conexión.')
    } finally { setLoading(false) }
  }

  return (
    <div className="page-wrapper">
      <Navbar />
      <div className="page-content" style={{ maxWidth:1300 }}>
        <h1 className="page-title">Punto de Venta</h1>
        <p className="page-subtitle">Vendedor: <strong>{user.username}</strong> · Periodo: {periodo}</p>

        <div className="pos-layout">

          {/* Catálogo */}
          <div>
            <div className="card">
              <div className="card-header">
                <span className="card-title">Catálogo de productos</span>
              </div>
              <div className="pos-catalog-grid">
                {PRODUCTOS_POS.map(p => (
                  <button key={p.id}
                    type="button"
                    onClick={() => agregarAlCarrito(p)}
                    style={{
                      border:'1px solid var(--border)', borderRadius:8, padding:'1rem',
                      cursor:'pointer', transition:'all .15s', background:'#fff',
                      textAlign:'left', width:'100%', font:'inherit',
                    }}
                    onMouseEnter={e => { e.currentTarget.style.borderColor = 'var(--primary)'; e.currentTarget.style.background = 'var(--accent)' }}
                    onMouseLeave={e => { e.currentTarget.style.borderColor = 'var(--border)'; e.currentTarget.style.background = '#fff' }}
                  >
                    <div style={{ fontWeight:600, fontSize:'.9rem', marginBottom:'.25rem' }}>{p.nombre}</div>
                    <div style={{ fontSize:'.75rem', color:'var(--text-lt)', marginBottom:'.5rem' }}>{p.categoria}</div>
                    <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center' }}>
                      <span style={{ fontWeight:700, color:'var(--text)', fontSize:'.95rem' }}>
                        ${p.precio.toLocaleString('es-CL')}
                      </span>
                      <span style={{ fontSize:'.75rem', color:'var(--text-lt)', background:'var(--accent)', padding:'.2rem .5rem', borderRadius:4, border:'1px solid var(--border)' }}>
                        + Agregar
                      </span>
                    </div>
                  </button>
                ))}
              </div>
            </div>

            {/* Historial */}
            <div className="card" style={{ marginTop:'1.5rem' }}>
              <div className="card-header">
                <span className="card-title">Ventas del período {hoy()}</span>
                <button className="btn btn-outline" onClick={cargarHistorial}
                  style={{ padding:'.4rem .8rem', fontSize:'.8rem' }}>Actualizar</button>
              </div>
              {loadingHist && <div className="loading">Cargando...</div>}
              {!loadingHist && historial.length === 0 && (
                <div style={{ textAlign:'center', padding:'1.5rem', color:'var(--text-lt)', fontSize:'.875rem' }}>
                  No hay ventas registradas este período
                </div>
              )}
              {!loadingHist && historial.length > 0 && (
                <div className="table-wrapper">
                  <table>
                    <thead>
                      <tr><th>ID</th><th>Descripción</th><th>Vendedor</th><th>Monto</th><th>Fecha</th></tr>
                    </thead>
                    <tbody>
                      {historial.map(v => (
                        <tr key={v.id}>
                          <td><span className="badge badge-blue">{v.id}</span></td>
                          <td>{v.descripcion}</td>
                          <td style={{ fontSize:'.82rem', color:'var(--text-lt)' }}>{v.fuente}</td>
                          <td><strong>${v.valor?.toLocaleString('es-CL')}</strong></td>
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
          <div className="pos-cart-sticky">
            <div className="card">
              <div className="card-header">
                <span className="card-title">Carrito ({carrito.length})</span>
                {carrito.length > 0 && (
                  <button onClick={() => setCarrito([])}
                    style={{ fontSize:'.78rem', color:'var(--red)', background:'none', border:'none', cursor:'pointer', fontWeight:600 }}>
                    Limpiar
                  </button>
                )}
              </div>

              {carrito.length === 0 ? (
                <div style={{ textAlign:'center', padding:'2.5rem', color:'var(--text-lt)' }}>
                  <div style={{ fontSize:'2rem', marginBottom:'.75rem' }}>🛒</div>
                  <span style={{ fontSize:'.875rem' }}>Haz clic en un producto para agregarlo</span>
                </div>
              ) : (
                <>
                  <div style={{ display:'flex', flexDirection:'column', gap:'.5rem', marginBottom:'1rem' }}>
                    {carrito.map(item => (
                      <div key={item.id} style={{
                        display:'flex', alignItems:'center', gap:'.75rem',
                        padding:'.75rem', background:'var(--accent)',
                        borderRadius:6, border:'1px solid var(--border)',
                      }}>
                        <div style={{ flex:1 }}>
                          <div style={{ fontWeight:600, fontSize:'.85rem' }}>{item.nombre}</div>
                          <div style={{ fontSize:'.75rem', color:'var(--text-lt)' }}>
                            ${item.precio.toLocaleString('es-CL')} c/u
                          </div>
                        </div>
                        <div style={{ display:'flex', alignItems:'center', gap:'.4rem' }}>
                          <button onClick={() => cambiarQty(item.id, -1)}
                            style={{ width:24, height:24, border:'1px solid var(--border)', borderRadius:4, cursor:'pointer', background:'#fff', fontWeight:700, fontSize:'.85rem' }}>−</button>
                          <span style={{ fontWeight:700, minWidth:20, textAlign:'center', fontSize:'.875rem' }}>{item.qty}</span>
                          <button onClick={() => cambiarQty(item.id, 1)}
                            style={{ width:24, height:24, border:'1px solid var(--border)', borderRadius:4, cursor:'pointer', background:'#fff', fontWeight:700, fontSize:'.85rem' }}>+</button>
                        </div>
                        <div style={{ fontWeight:700, color:'var(--text)', minWidth:70, textAlign:'right', fontSize:'.875rem' }}>
                          ${(item.precio * item.qty).toLocaleString('es-CL')}
                        </div>
                        <button onClick={() => eliminar(item.id)}
                          style={{ background:'none', border:'none', cursor:'pointer', color:'var(--text-lt)', fontSize:'1rem' }}>✕</button>
                      </div>
                    ))}
                  </div>

                  <div style={{ borderTop:'1px solid var(--border)', paddingTop:'1rem', marginBottom:'1rem' }}>
                    <div style={{ display:'flex', justifyContent:'space-between', fontWeight:700, fontSize:'1.1rem' }}>
                      <span>Total</span>
                      <span>${total.toLocaleString('es-CL')}</span>
                    </div>
                    <div style={{ fontSize:'.75rem', color:'var(--text-lt)', marginTop:'.25rem' }}>
                      {totalUnidades} unidades · IVA incluido
                    </div>
                  </div>
                  <button className="btn btn-primary" style={{ width:'100%' }}
                    onClick={procesarVenta} disabled={loading}>
                    {loading ? 'Procesando...' : 'Confirmar venta'}
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
