import { useState } from 'react'
import Navbar from '../../components/Navbar'
import { useAuth } from '../../context/AuthContext'
import { datosApi, inventarioApi } from '../../api/apiClient'

const PRODUCTOS = [
  { id: 1,  nombre: 'Notebook Premium 15"', precio: 899990, img: '💻', cat: 'Tecnología', desc: 'Intel Core i7, 16GB RAM, SSD 512GB',     rating: 4.8 },
  { id: 2,  nombre: 'Monitor UHD 27"',      precio: 349990, img: '🖥️', cat: 'Tecnología', desc: 'Resolución 4K, HDR400, 144Hz',             rating: 4.9 },
  { id: 3,  nombre: 'Teclado Mecánico',     precio: 89990,  img: '⌨️', cat: 'Accesorios', desc: 'Switch Blue, retroiluminado RGB',           rating: 4.6 },
  { id: 4,  nombre: 'Mouse Inalámbrico',    precio: 49990,  img: '🖱️', cat: 'Accesorios', desc: '2.4GHz, batería 12 meses',                  rating: 4.5 },
  { id: 5,  nombre: 'Tablet Pro 11"',       precio: 599990, img: '📱', cat: 'Tecnología', desc: 'Chip A15, pantalla OLED, 256GB',             rating: 4.7 },
  { id: 6,  nombre: 'Auriculares BT Pro',   precio: 129990, img: '🎧', cat: 'Audio',      desc: 'Cancelación de ruido activa, 30h batería',  rating: 4.8 },
  { id: 7,  nombre: 'Webcam Full HD',       precio: 69990,  img: '📷', cat: 'Accesorios', desc: '1080p 60fps, autofoco, micrófono dual',     rating: 4.4 },
  { id: 8,  nombre: 'SSD Externo 1TB',      precio: 119990, img: '💾', cat: 'Almacen.',   desc: 'USB-C 3.2, velocidad 1000MB/s',             rating: 4.7 },
  { id: 9,  nombre: 'Hub USB-C 7 en 1',    precio: 39990,  img: '🔌', cat: 'Accesorios', desc: 'HDMI 4K, USB-A, SD, carga 100W PD',         rating: 4.3 },
]

const CATS = ['Todas', 'Tecnología', 'Accesorios', 'Audio', 'Almacen.']

function Estrellas({ n }) {
  return <span style={{ color:'#D4AC0D', fontSize:'.9rem' }}>{'★'.repeat(Math.round(n))}{'☆'.repeat(5-Math.round(n))}</span>
}

export default function TiendaPage() {
  const { user } = useAuth()
  const [cat, setCat]       = useState('Todas')
  const [buscar, setBuscar] = useState('')
  const [carrito, setCarrito] = useState([])
  const [showCart, setShowCart] = useState(false)
  const [comprado, setComprado] = useState(false)

  const filtrados = PRODUCTOS
    .filter(p => cat === 'Todas' || p.cat === cat)
    .filter(p => !buscar || p.nombre.toLowerCase().includes(buscar.toLowerCase()))

  const agregarAlCarrito = (p) => {
    setCarrito(prev => {
      const ex = prev.find(x => x.id === p.id)
      return ex
        ? prev.map(x => x.id === p.id ? {...x, qty: x.qty+1} : x)
        : [...prev, {...p, qty: 1}]
    })
  }

  const eliminar = (id) => setCarrito(prev => prev.filter(p => p.id !== id))
  const totalItems = carrito.reduce((s,p) => s + p.qty, 0)
  const total = carrito.reduce((s,p) => s + p.precio * p.qty, 0)

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
    setComprado(true)
    setCarrito([])
    setShowCart(false)
  }

  return (
    <div className="page-wrapper">
      <Navbar />

      {/* Barra tienda */}
      <div style={{ background:'var(--primary)', padding:'1.5rem 2rem', color:'#fff' }}>
        <div style={{ maxWidth:1200, margin:'0 auto' }}>
          <h1 style={{ fontSize:'1.6rem', fontWeight:800, marginBottom:'.25rem' }}>
            🛒 Tienda Online · Grupo Cordillera
          </h1>
          <p style={{ color:'rgba(255,255,255,0.75)', fontSize:'.9rem' }}>
            Hola, <strong>{user.username}</strong> — explora nuestra selección exclusiva
          </p>
        </div>
      </div>

      {comprado && (
        <div style={{ background:'var(--green)', color:'#fff', padding:'1rem 2rem', textAlign:'center', fontWeight:600 }}>
          ✅ ¡Compra realizada exitosamente! Tu pedido será despachado en 48 horas.
          <button onClick={() => setComprado(false)} style={{ marginLeft:'1rem', background:'rgba(255,255,255,0.25)', border:'none', color:'#fff', cursor:'pointer', borderRadius:6, padding:'.25rem .6rem' }}>✕</button>
        </div>
      )}

      <div style={{ maxWidth:1200, margin:'0 auto', padding:'1.5rem 2rem' }}>
        {/* Filtros */}
        <div style={{ display:'flex', gap:'1rem', marginBottom:'1.5rem', flexWrap:'wrap', alignItems:'center', justifyContent:'space-between' }}>
          <div style={{ display:'flex', gap:'.5rem', flexWrap:'wrap' }}>
            {CATS.map(c => (
              <button key={c} onClick={() => setCat(c)}
                style={{
                  padding:'.4rem .9rem', borderRadius:99, border:'1.5px solid',
                  borderColor: cat === c ? 'var(--primary)' : 'var(--border)',
                  background:  cat === c ? 'var(--primary)' : '#fff',
                  color:       cat === c ? '#fff' : 'var(--text)',
                  cursor:'pointer', fontWeight:600, fontSize:'.88rem',
                }}>
                {c}
              </button>
            ))}
          </div>
          <div style={{ display:'flex', gap:.75+'rem', alignItems:'center' }}>
            <input className="form-input" style={{ width:200 }}
              placeholder="🔍 Buscar producto..." value={buscar}
              onChange={e => setBuscar(e.target.value)} />
            <button className="btn btn-gold" style={{ position:'relative' }}
              onClick={() => setShowCart(true)}>
              🛒 Carrito
              {totalItems > 0 && (
                <span style={{ position:'absolute', top:-8, right:-8, background:'var(--red)',
                  color:'#fff', borderRadius:'50%', width:20, height:20, fontSize:'.7rem',
                  display:'flex', alignItems:'center', justifyContent:'center', fontWeight:700 }}>
                  {totalItems}
                </span>
              )}
            </button>
          </div>
        </div>

        {/* Productos */}
        <div style={{ display:'grid', gridTemplateColumns:'repeat(3, 1fr)', gap:'1.25rem' }}>
          {filtrados.map(p => (
            <div key={p.id} className="card" style={{ transition:'all .2s' }}
              onMouseEnter={e => e.currentTarget.style.transform = 'translateY(-4px)'}
              onMouseLeave={e => e.currentTarget.style.transform = 'translateY(0)'}>
              <div style={{ fontSize:'3rem', textAlign:'center', padding:'1rem', background:'#F8F9FA', borderRadius:8, marginBottom:'.75rem' }}>
                {p.img}
              </div>
              <span style={{ fontSize:'.75rem', fontWeight:700, color:'var(--text-lt)', textTransform:'uppercase' }}>{p.cat}</span>
              <h3 style={{ fontWeight:700, margin:'.25rem 0' }}>{p.nombre}</h3>
              <p style={{ fontSize:'.83rem', color:'var(--text-lt)', marginBottom:'.5rem' }}>{p.desc}</p>
              <div style={{ display:'flex', alignItems:'center', gap:'.4rem', marginBottom:'.75rem' }}>
                <Estrellas n={p.rating} />
                <span style={{ fontSize:'.8rem', color:'var(--text-lt)' }}>({p.rating})</span>
              </div>
              <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center' }}>
                <span style={{ fontWeight:800, fontSize:'1.25rem', color:'var(--primary)' }}>
                  ${p.precio.toLocaleString('es-CL')}
                </span>
                <button className="btn btn-primary" onClick={() => agregarAlCarrito(p)}>
                  + Agregar 🛒
                </button>
              </div>
            </div>
          ))}
        </div>

        {filtrados.length === 0 && (
          <div style={{ textAlign:'center', padding:'3rem', color:'var(--text-lt)' }}>
            No se encontraron productos con ese filtro
          </div>
        )}
      </div>

      {/* Modal carrito */}
      {showCart && (
        <div style={{ position:'fixed', inset:0, background:'rgba(0,0,0,0.5)', zIndex:200, display:'flex', justifyContent:'flex-end' }}>
          <div style={{ background:'#fff', width:420, height:'100%', overflow:'auto', padding:'1.5rem', boxShadow:'-4px 0 20px rgba(0,0,0,0.15)' }}>
            <div style={{ display:'flex', justifyContent:'space-between', marginBottom:'1.5rem' }}>
              <h2 style={{ color:'var(--primary)' }}>🛒 Mi carrito</h2>
              <button onClick={() => setShowCart(false)}
                style={{ background:'none', border:'none', fontSize:'1.4rem', cursor:'pointer', color:'var(--text-lt)' }}>✕</button>
            </div>

            {carrito.length === 0 ? (
              <div style={{ textAlign:'center', padding:'2rem', color:'var(--text-lt)' }}>
                <div style={{ fontSize:'3rem', marginBottom:'1rem' }}>🛒</div>
                Tu carrito está vacío
              </div>
            ) : (
              <>
                <div style={{ display:'flex', flexDirection:'column', gap:'.75rem' }}>
                  {carrito.map(item => (
                    <div key={item.id} style={{ display:'flex', gap:'.75rem', padding:'.75rem', background:'#F8F9FA', borderRadius:8, alignItems:'center' }}>
                      <div style={{ fontSize:'1.8rem' }}>{item.img}</div>
                      <div style={{ flex:1 }}>
                        <div style={{ fontWeight:600, fontSize:'.9rem' }}>{item.nombre}</div>
                        <div style={{ fontSize:'.8rem', color:'var(--text-lt)' }}>
                          x{item.qty} × ${item.precio.toLocaleString('es-CL')}
                        </div>
                      </div>
                      <div style={{ fontWeight:800, color:'var(--primary)', fontSize:'.95rem' }}>
                        ${(item.precio * item.qty).toLocaleString('es-CL')}
                      </div>
                      <button onClick={() => eliminar(item.id)}
                        style={{ background:'none', border:'none', cursor:'pointer', color:'var(--red)', fontSize:'1rem' }}>✕</button>
                    </div>
                  ))}
                </div>
                <div style={{ borderTop:'2px solid var(--border)', margin:'1.25rem 0', paddingTop:'1rem' }}>
                  <div style={{ display:'flex', justifyContent:'space-between', fontWeight:800, fontSize:'1.2rem', marginBottom:'1.25rem' }}>
                    <span>Total</span>
                    <span style={{ color:'var(--primary)' }}>${total.toLocaleString('es-CL')}</span>
                  </div>
                  <button className="btn btn-success" style={{ width:'100%', padding:'.85rem' }} onClick={checkout}>
                    ✅ Finalizar compra
                  </button>
                </div>
              </>
            )}
          </div>
        </div>
      )}

      <footer><p>© 2026 Grupo Cordillera · Todos los derechos reservados</p></footer>
    </div>
  )
}
