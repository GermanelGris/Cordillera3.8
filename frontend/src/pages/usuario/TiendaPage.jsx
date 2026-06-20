import { useState, useEffect } from 'react'
import { useSearchParams } from 'react-router-dom'
import Navbar from '../../components/Navbar'
import { useAuth } from '../../context/AuthContext'
import { datosApi, inventarioApi } from '../../api/apiClient'
import { useToast } from '../../context/ToastContext'

const PRODUCTOS = [
  { id: 1, nombre: 'Notebook Premium 15"', precio: 899990, img: '💻', cat: 'Tecnología', desc: 'Intel Core i7, 16GB RAM, SSD 512GB',    rating: 4.8 },
  { id: 2, nombre: 'Monitor UHD 27"',      precio: 349990, img: '🖥️', cat: 'Tecnología', desc: 'Resolución 4K, HDR400, 144Hz',            rating: 4.9 },
  { id: 3, nombre: 'Teclado Mecánico',     precio: 89990,  img: '⌨️', cat: 'Accesorios', desc: 'Switch Blue, retroiluminado RGB',          rating: 4.6 },
  { id: 4, nombre: 'Mouse Inalámbrico',    precio: 49990,  img: '🖱️', cat: 'Accesorios', desc: '2.4GHz, batería 12 meses',                 rating: 4.5 },
  { id: 5, nombre: 'Tablet Pro 11"',       precio: 599990, img: '📱', cat: 'Tecnología', desc: 'Chip A15, pantalla OLED, 256GB',            rating: 4.7 },
  { id: 6, nombre: 'Auriculares BT Pro',   precio: 129990, img: '🎧', cat: 'Audio',      desc: 'Cancelación de ruido activa, 30h batería', rating: 4.8 },
  { id: 7, nombre: 'Webcam Full HD',       precio: 69990,  img: '📷', cat: 'Accesorios', desc: '1080p 60fps, autofoco, micrófono dual',    rating: 4.4 },
  { id: 8, nombre: 'SSD Externo 1TB',      precio: 119990, img: '💾', cat: 'Almacen.',   desc: 'USB-C 3.2, velocidad 1000MB/s',            rating: 4.7 },
  { id: 9, nombre: 'Hub USB-C 7 en 1',    precio: 39990,  img: '🔌', cat: 'Accesorios', desc: 'HDMI 4K, USB-A, SD, carga 100W PD',        rating: 4.3 },
]

const CATS = ['Todas', 'Tecnología', 'Accesorios', 'Audio', 'Almacen.']
const MAX_CARRITO = 10

function renderEstrellas(n = 0) {
  return (
    <span style={{ color:'#D4AC0D', fontSize:'.85rem' }}>
      {'★'.repeat(Math.round(n))}{'☆'.repeat(5 - Math.round(n))}
    </span>
  )
}

export default function TiendaPage() {
  const { user } = useAuth()
  const { toast } = useToast()
  const [searchParams] = useSearchParams()

  const catParam = searchParams.get('cat') || 'Todas'
  const catInicial = CATS.includes(catParam) ? catParam : 'Todas'

  const [cat, setCat]         = useState(catInicial)
  const [buscar, setBuscar]   = useState('')
  const [carrito, setCarrito] = useState([])
  const [showCart, setShowCart] = useState(false)

  useEffect(() => {
    const c = searchParams.get('cat') || 'Todas'
    setCat(CATS.includes(c) ? c : 'Todas')
  }, [searchParams])

  const filtrados = PRODUCTOS
    .filter(p => cat === 'Todas' || p.cat === cat)
    .filter(p => !buscar || p.nombre.toLowerCase().includes(buscar.toLowerCase()))

  const totalItems = carrito.reduce((s, p) => s + p.qty, 0)

  const agregarAlCarrito = (p) => {
    if (totalItems >= MAX_CARRITO) {
      toast.error(`El carrito tiene un máximo de ${MAX_CARRITO} productos`)
      return
    }
    setCarrito(prev => {
      const ex = prev.find(x => x.id === p.id)
      if (ex) {
        if (ex.qty >= MAX_CARRITO) {
          toast.error(`No puedes agregar más de ${MAX_CARRITO} unidades de este producto`)
          return prev
        }
        return prev.map(x => x.id === p.id ? {...x, qty: x.qty + 1} : x)
      }
      return [...prev, {...p, qty: 1}]
    })
    toast.success(`${p.nombre} agregado al carrito`)
  }

  const eliminar = (id) => setCarrito(prev => prev.filter(p => p.id !== id))
  const total = carrito.reduce((s, p) => s + p.precio * p.qty, 0)

  const checkout = async () => {
    const periodo = new Date().toISOString().slice(0, 7)
    try {
      for (const item of carrito) {
        await datosApi.registrar({
          fuente: `TIENDA-${user.username}`,
          tipo: 'VENTAS',
          valor: item.precio * item.qty,
          periodo,
          descripcion: `${item.nombre} x${item.qty}`,
        })
        await inventarioApi.descontar(item.id, item.qty)
      }
    } catch { /* continúa aunque falle el registro */ }
    toast.success('¡Compra realizada! Tu pedido será despachado en 48 horas.')
    setCarrito([])
    setShowCart(false)
  }

  return (
    <div className="page-wrapper">
      <Navbar />

      {/* Header tienda */}
      <div style={{ background:'var(--primary)', padding:'1.5rem 2rem', color:'#fff' }}>
        <div style={{ maxWidth:1200, margin:'0 auto' }}>
          <h1 style={{ fontSize:'1.4rem', fontWeight:700, marginBottom:'.2rem', letterSpacing:'-0.02em' }}>
            Tienda Online · Grupo Cordillera
          </h1>
          <p style={{ color:'rgba(255,255,255,0.6)', fontSize:'.875rem' }}>
            Hola, <strong>{user.username}</strong> — explora nuestra selección exclusiva
          </p>
        </div>
      </div>

      <div style={{ maxWidth:1200, margin:'0 auto', padding:'1.5rem 2rem' }}>
        {/* Filtros */}
        <div className="tienda-filters">
          <div style={{ display:'flex', gap:'.5rem', flexWrap:'wrap' }}>
            {CATS.map(c => (
              <button key={c} type="button" onClick={() => setCat(c)}
                style={{
                  padding:'.35rem .9rem', borderRadius:99,
                  border: `1px solid ${cat === c ? 'var(--primary)' : 'var(--border)'}`,
                  background: cat === c ? 'var(--primary)' : '#fff',
                  color:      cat === c ? '#fff' : 'var(--text-lt)',
                  cursor:'pointer', fontWeight:600, fontSize:'.82rem', font:'inherit',
                }}>
                {c}
              </button>
            ))}
          </div>
          <div className="tienda-search-row">
            <input className="form-input"
              placeholder="Buscar producto..." value={buscar}
              onChange={e => setBuscar(e.target.value)} />
            <button type="button" className="btn btn-primary" style={{ position:'relative', flexShrink:0 }}
              onClick={() => setShowCart(true)}>
              🛒 Carrito
              {totalItems > 0 && (
                <span style={{
                  position:'absolute', top:-8, right:-8,
                  background:'var(--red)', color:'#fff', borderRadius:'50%',
                  width:18, height:18, fontSize:'.65rem',
                  display:'flex', alignItems:'center', justifyContent:'center', fontWeight:700,
                }}>
                  {totalItems}
                </span>
              )}
            </button>
          </div>
        </div>

        {totalItems > 0 && (
          <div style={{
            background:'var(--accent)', border:'1px solid var(--border)', borderRadius:6,
            padding:'.5rem .9rem', marginBottom:'1rem', fontSize:'.82rem', color:'var(--text-lt)',
          }}>
            🛒 {totalItems} de {MAX_CARRITO} productos en el carrito
          </div>
        )}

        {/* Grid productos */}
        <div className="tienda-grid">
          {filtrados.map(p => (
            <div key={p.id} className="card" style={{ transition:'border-color .15s' }}>
              <div style={{ fontSize:'2.75rem', textAlign:'center', padding:'.75rem', background:'var(--accent)', borderRadius:6, marginBottom:'.75rem' }}>
                {p.img}
              </div>
              <span style={{ fontSize:'.7rem', fontWeight:700, color:'var(--text-lt)', textTransform:'uppercase', letterSpacing:'0.06em' }}>{p.cat}</span>
              <h3 style={{ fontWeight:700, margin:'.25rem 0', fontSize:'.95rem' }}>{p.nombre}</h3>
              <p style={{ fontSize:'.8rem', color:'var(--text-lt)', marginBottom:'.5rem', lineHeight:1.5 }}>{p.desc}</p>
              <div style={{ display:'flex', alignItems:'center', gap:'.4rem', marginBottom:'.75rem' }}>
                {renderEstrellas(p.rating)}
                <span style={{ fontSize:'.75rem', color:'var(--text-lt)' }}>({p.rating})</span>
              </div>
              <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center' }}>
                <span style={{ fontWeight:700, fontSize:'1.1rem', color:'var(--text)', letterSpacing:'-0.02em' }}>
                  ${p.precio.toLocaleString('es-CL')}
                </span>
                <button type="button" className="btn btn-primary"
                  style={{ padding:'.4rem .9rem', fontSize:'.82rem' }}
                  onClick={() => agregarAlCarrito(p)}>
                  + Agregar
                </button>
              </div>
            </div>
          ))}
        </div>

        {filtrados.length === 0 && (
          <div style={{ textAlign:'center', padding:'4rem', color:'var(--text-lt)' }}>
            No se encontraron productos con ese filtro
          </div>
        )}
      </div>

      {/* Modal carrito */}
      {showCart && (
        <div style={{ position:'fixed', inset:0, background:'rgba(0,0,0,0.4)', zIndex:200, display:'flex', justifyContent:'flex-end' }}>
          <div style={{ background:'#fff', width:420, height:'100%', overflow:'auto', padding:'1.5rem', borderLeft:'1px solid var(--border)' }}>
            <div style={{ display:'flex', justifyContent:'space-between', marginBottom:'1.5rem', alignItems:'center' }}>
              <h2 style={{ fontWeight:700, color:'var(--text)', fontSize:'1.1rem', letterSpacing:'-0.01em' }}>
                {'Mi carrito '}
                <span style={{ fontWeight:400, fontSize:'.8rem', color:'var(--text-lt)' }}>({totalItems}/{MAX_CARRITO})</span>
              </h2>
              <button type="button" onClick={() => setShowCart(false)}
                style={{ background:'none', border:'none', fontSize:'1.2rem', cursor:'pointer', color:'var(--text-lt)' }}>✕</button>
            </div>

            {carrito.length === 0 ? (
              <div style={{ textAlign:'center', padding:'3rem', color:'var(--text-lt)' }}>
                <div style={{ fontSize:'2.5rem', marginBottom:'1rem' }}>🛒</div>
                <span style={{ fontSize:'.875rem' }}>Tu carrito está vacío</span>
              </div>
            ) : (
              <>
                <div style={{ display:'flex', flexDirection:'column', gap:'.5rem', marginBottom:'1rem' }}>
                  {carrito.map(item => (
                    <div key={item.id} style={{ display:'flex', gap:'.75rem', padding:'.75rem', background:'var(--accent)', borderRadius:6, border:'1px solid var(--border)', alignItems:'center' }}>
                      <div style={{ fontSize:'1.5rem' }}>{item.img}</div>
                      <div style={{ flex:1 }}>
                        <div style={{ fontWeight:600, fontSize:'.875rem' }}>{item.nombre}</div>
                        <div style={{ fontSize:'.75rem', color:'var(--text-lt)' }}>
                          x{item.qty} × ${item.precio.toLocaleString('es-CL')}
                        </div>
                      </div>
                      <div style={{ fontWeight:700, color:'var(--text)', fontSize:'.9rem' }}>
                        ${(item.precio * item.qty).toLocaleString('es-CL')}
                      </div>
                      <button type="button" onClick={() => eliminar(item.id)}
                        style={{ background:'none', border:'none', cursor:'pointer', color:'var(--text-lt)', fontSize:'.9rem' }}>✕</button>
                    </div>
                  ))}
                </div>
                <div style={{ borderTop:'1px solid var(--border)', padding:'1rem 0', marginBottom:'1rem' }}>
                  <div style={{ display:'flex', justifyContent:'space-between', fontWeight:700, fontSize:'1.1rem' }}>
                    <span>Total</span>
                    <span>${total.toLocaleString('es-CL')}</span>
                  </div>
                </div>
                <button type="button" className="btn btn-primary" style={{ width:'100%', padding:'.85rem' }}
                  onClick={checkout}>
                  Finalizar compra
                </button>
              </>
            )}
          </div>
        </div>
      )}

      <footer><p>© 2026 Grupo Cordillera · Todos los derechos reservados</p></footer>
    </div>
  )
}
