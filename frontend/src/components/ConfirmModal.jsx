export default function ConfirmModal({ title, message, confirmLabel = 'Eliminar', onConfirm, onCancel }) {
  return (
    <div style={{
      position: 'fixed', inset: 0,
      background: 'rgba(0,0,0,0.45)', zIndex: 500,
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      padding: '1rem',
    }}>
      <div style={{
        background: '#fff', borderRadius: 10, padding: '2rem',
        maxWidth: 440, width: '100%', border: '1px solid #E5E7EB',
        boxShadow: '0 8px 32px rgba(0,0,0,0.12)',
      }}>
        <h3 style={{ fontWeight: 700, color: '#111827', marginBottom: '.5rem', fontSize: '1.05rem' }}>
          {title}
        </h3>
        <p style={{ color: '#6B7280', fontSize: '.875rem', marginBottom: '1.75rem', lineHeight: 1.6 }}>
          {message}
        </p>
        <div style={{ display: 'flex', gap: '.75rem', justifyContent: 'flex-end' }}>
          <button type="button" className="btn btn-outline" onClick={onCancel}>
            Cancelar
          </button>
          <button type="button" onClick={onConfirm}
            style={{
              background: '#DC2626', color: '#fff', border: 'none',
              borderRadius: 6, padding: '.6rem 1.2rem',
              fontWeight: 600, fontSize: '.875rem', cursor: 'pointer',
              fontFamily: 'inherit', transition: 'background .15s',
            }}
            onMouseEnter={e => { e.currentTarget.style.background = '#B91C1C' }}
            onMouseLeave={e => { e.currentTarget.style.background = '#DC2626' }}>
            {confirmLabel}
          </button>
        </div>
      </div>
    </div>
  )
}
