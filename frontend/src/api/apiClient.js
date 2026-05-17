import axios from 'axios'

const api = axios.create({ baseURL: '/' })

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// Reject responses where nginx served the React SPA fallback instead of JSON
api.interceptors.response.use(
  res => {
    const ct = res.headers['content-type'] || ''
    if (ct.includes('text/html')) {
      return Promise.reject(new Error(`Respuesta HTML inesperada para ${res.config.url} — posible error de routing`))
    }
    return res
  },
  err => Promise.reject(err)
)

export const kpiApi = {
  listar: ()         => api.get('/kpi', { params: { _t: Date.now() } }),
  listarPorPeriodo:  (p) => api.get(`/kpi/periodo/${p}`),
  calcular: (data)   => api.post('/kpi/calcular', data),
  calcularDesdeDatos: (params) => api.post('/kpi/calcular-desde-datos', null, { params }),
}

export const reportesApi = {
  listar:           ()    => api.get('/reportes'),
  listarPorPeriodo: (p)   => api.get(`/reportes/periodo/${p}`),
  generar: (data)         => api.post('/reportes', data),
}

export const datosApi = {
  listar:           ()    => api.get('/datos'),
  listarPorPeriodo: (p)   => api.get(`/datos/periodo/${p}`),
  registrar: (data)       => api.post('/datos', data),
}

export const inventarioApi = {
  listar:    ()                          => api.get('/inventario'),
  descontar: (productoId, cantidad)      => api.post('/inventario/descontar', { productoId, cantidad }),
}

export default api
