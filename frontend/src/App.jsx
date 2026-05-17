import { Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import ProtectedRoute from './components/ProtectedRoute'

import Home from './pages/Home'
import Login from './pages/Login'
import Registro from './pages/Registro'
import KpiPage from './pages/admin/KpiPage'
import ReportesPage from './pages/admin/ReportesPage'
import PosPage from './pages/vendedor/PosPage'
import TiendaPage from './pages/usuario/TiendaPage'
import Unauthorized from './pages/Unauthorized'

export default function App() {
  return (
    <AuthProvider>
      <Routes>
        {/* Públicas */}
        <Route path="/"         element={<Home />} />
        <Route path="/login"    element={<Login />} />
        <Route path="/registro" element={<Registro />} />
        <Route path="/no-autorizado" element={<Unauthorized />} />

        {/* Admin */}
        <Route path="/admin/kpi" element={
          <ProtectedRoute roles={['ADMIN']}>
            <KpiPage />
          </ProtectedRoute>
        } />
        <Route path="/admin/reportes" element={
          <ProtectedRoute roles={['ADMIN']}>
            <ReportesPage />
          </ProtectedRoute>
        } />

        {/* Vendedor / POS */}
        <Route path="/vendedor/pos" element={
          <ProtectedRoute roles={['VENDEDOR', 'ADMIN']}>
            <PosPage />
          </ProtectedRoute>
        } />

        {/* Tienda — todos los roles */}
        <Route path="/tienda" element={
          <ProtectedRoute roles={['USUARIO', 'ADMIN', 'VENDEDOR']}>
            <TiendaPage />
          </ProtectedRoute>
        } />

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </AuthProvider>
  )
}
