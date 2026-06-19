import { createContext, useContext, useState, useCallback } from 'react'

const ToastCtx = createContext(null)

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([])

  const add = useCallback((message, type) => {
    const id = Date.now() + Math.random()
    setToasts(prev => [...prev, { id, message, type }])
    setTimeout(() => setToasts(prev => prev.filter(t => t.id !== id)), 3800)
  }, [])

  const remove = useCallback((id) => {
    setToasts(prev => prev.filter(t => t.id !== id))
  }, [])

  const toast = {
    success: (m) => add(m, 'success'),
    error:   (m) => add(m, 'error'),
  }

  return (
    <ToastCtx.Provider value={{ toast }}>
      {children}
      {toasts.length > 0 && (
        <div style={{
          position: 'fixed', bottom: '1.5rem', right: '1.5rem',
          zIndex: 9999, display: 'flex', flexDirection: 'column', gap: '.5rem',
        }}>
          {toasts.map(t => (
            <div key={t.id} style={{
              background: '#fff',
              border: '1px solid #E5E7EB',
              borderLeft: `3px solid ${t.type === 'success' ? '#059669' : '#DC2626'}`,
              borderRadius: '6px',
              padding: '.9rem 1.1rem',
              display: 'flex',
              alignItems: 'flex-start',
              gap: '.75rem',
              minWidth: '280px',
              maxWidth: '400px',
              boxShadow: '0 4px 16px rgba(0,0,0,0.08)',
            }}>
              <span style={{
                color: t.type === 'success' ? '#059669' : '#DC2626',
                fontWeight: 700, fontSize: '.85rem', lineHeight: '1.5rem', flexShrink: 0,
              }}>
                {t.type === 'success' ? '✓' : '✕'}
              </span>
              <span style={{ flex: 1, fontSize: '.875rem', color: '#111827', fontWeight: 500, lineHeight: 1.5 }}>
                {t.message}
              </span>
              <button
                onClick={() => remove(t.id)}
                style={{
                  background: 'none', border: 'none', cursor: 'pointer',
                  color: '#9CA3AF', fontSize: '.9rem', padding: 0,
                  lineHeight: '1.5rem', flexShrink: 0,
                }}>
                ✕
              </button>
            </div>
          ))}
        </div>
      )}
    </ToastCtx.Provider>
  )
}

export const useToast = () => useContext(ToastCtx)
