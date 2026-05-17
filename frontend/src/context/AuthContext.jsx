import { createContext, useContext, useState, useEffect } from 'react'
import axios from 'axios'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const token = localStorage.getItem('token')
    const saved  = localStorage.getItem('user')
    if (token && saved) {
      setUser(JSON.parse(saved))
      axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
    }
    setLoading(false)
  }, [])

  const login = async (email, password) => {
    const res = await axios.post('/auth/login', { email, password })
    const { token, rol, nombre } = res.data
    localStorage.setItem('token', token)
    localStorage.setItem('user', JSON.stringify({ nombre, email, rol }))
    axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
    setUser({ nombre, email, rol })
    return rol
  }

  const registro = async (data) => {
    const res = await axios.post('/auth/registro', data)
    return res.data
  }

  const logout = () => {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    delete axios.defaults.headers.common['Authorization']
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, login, logout, registro, loading }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)
