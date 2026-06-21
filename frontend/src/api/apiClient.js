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
  listar: ()              => api.get('/kpi', { params: { _t: Date.now() } }),
  listarPorPeriodo: (p)   => api.get(`/kpi/periodo/${p}`),
  calcular: (data)        => api.post('/kpi/calcular', data),
  calcularDesdeDatos: (params) => api.post('/kpi/calcular-desde-datos', null, { params }),
  actualizar: (id, data)  => api.put(`/kpi/${id}`, data),
  eliminar: (id)          => api.delete(`/kpi/${id}`),
}

export const reportesApi = {
  listar:           ()    => api.get('/reportes'),
  listarPorPeriodo: (p)   => api.get(`/reportes/periodo/${p}`),
  generar: (data)         => api.post('/reportes', data),
  actualizar: (id, data)  => api.put(`/reportes/${id}`, data),
  eliminar: (id)          => api.delete(`/reportes/${id}`),
}

export const datosApi = {
  listar:           ()    => api.get('/datos'),
  listarPorPeriodo: (p)   => api.get(`/datos/periodo/${p}`),
  registrar: (data)       => api.post('/datos', data),
}

export const inventarioApi = {
  listar:    ()                          => api.get('/inventario'),
  obtener:   (id)                        => api.get(`/inventario/${id}`),
  crear:     (data)                      => api.post('/inventario', data),
  actualizar:(id, data)                  => api.put(`/inventario/${id}`, data),
  eliminar:  (id)                        => api.delete(`/inventario/${id}`),
  descontar: (productoId, cantidad)      => api.post('/inventario/descontar', { productoId, cantidad }),
}

export const mailConfigApi = {
  obtener:    ()                  => api.get('/mail-config'),
  guardar:    (data)             => api.put('/mail-config', data),
  probar:     (destinatario)     => api.post('/mail-config/probar', null, { params: { destinatario } }),
}

export const usuariosApi = {
  listar:    ()         => api.get('/usuarios'),
  obtener:   (id)       => api.get(`/usuarios/${id}`),
  roles:     ()         => api.get('/usuarios/roles'),
  crear:     (data)     => api.post('/usuarios', data),
  actualizar:(id, data) => api.put(`/usuarios/${id}`, data),
  eliminar:  (id)       => api.delete(`/usuarios/${id}`),
}

export default api
