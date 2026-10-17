import { useEffect, useMemo, useState } from 'react'
import { MapContainer, TileLayer, Marker, useMapEvents } from 'react-leaflet'
import L from 'leaflet'

const API = 'http://localhost:8080/api'
const defaultCenter = [23.2599, 77.4126] // Bhopal
const markerIcon = new L.Icon({
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png',
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png',
  iconSize: [25, 41], iconAnchor: [12, 41]
})

async function request(path, token, options = {}) {
  const headers = { ...(options.headers || {}) }
  if (token) headers.Authorization = `Bearer ${token}`
  if (options.body && !(options.body instanceof FormData)) headers['Content-Type'] = 'application/json'
  const res = await fetch(`${API}${path}`, { ...options, headers })
  const data = await res.json().catch(() => ({}))
  if (!res.ok) throw new Error(data.error || data.message || `Request failed (${res.status})`)
  return data
}

function PickLocation({ position, setPosition }) {
  useMapEvents({ click(e) { setPosition([e.latlng.lat, e.latlng.lng]) } })
  return position ? <Marker position={position} icon={markerIcon} /> : null
}

function App() {
  const [session, setSession] = useState(() => {
    try { return JSON.parse(localStorage.getItem('civicSession') || 'null') } catch { return null }
  })
  const [mode, setMode] = useState('login')
  const [authForm, setAuthForm] = useState({ name: '', email: '', password: '' })
  const [complaints, setComplaints] = useState([])
  const [stats, setStats] = useState(null)
  const [notice, setNotice] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const [position, setPosition] = useState(defaultCenter)
  const [form, setForm] = useState({ title: '', description: '', category: 'Road damage', location: 'Bhopal, Madhya Pradesh' })
  const [image, setImage] = useState(null)
  const isAdmin = session?.role === 'ADMIN'
  const token = session?.token
  const setField = (key, value) => setForm(old => ({ ...old, [key]: value }))
  const mapLink = useMemo(() => position ? `https://www.openstreetmap.org/?mlat=${position[0]}&mlon=${position[1]}#map=17/${position[0]}/${position[1]}` : '', [position])

  async function load() {
    if (!token) return
    try {
      const data = await request('/complaints', token)
      setComplaints(data)
      if (isAdmin) setStats(await request('/complaints/stats', token))
    } catch (e) { setError(e.message) }
  }
  useEffect(() => { load() }, [token, isAdmin])

  async function authenticate(e) {
    e.preventDefault(); setError(''); setNotice(''); setBusy(true)
    try {
      const path = mode === 'register' ? '/auth/register' : '/auth/login'
      const body = mode === 'register'
        ? { name: authForm.name, email: authForm.email, password: authForm.password }
        : { email: authForm.email, password: authForm.password }
      const result = await request(path, null, { method: 'POST', body: JSON.stringify(body) })
      if (mode === 'register') {
        setMode('login'); setNotice('Registration successful. Please log in.')
        setAuthForm(v => ({ ...v, password: '' }))
      } else {
        localStorage.setItem('civicSession', JSON.stringify(result))
        setSession(result); setNotice(`Welcome, ${result.name}!`)
      }
    } catch (e) { setError(e.message) } finally { setBusy(false) }
  }

  function logout() {
    localStorage.removeItem('civicSession'); setSession(null); setComplaints([]); setStats(null)
    setNotice('You have been logged out.'); setError('')
  }

  async function submitComplaint(e) {
    e.preventDefault(); setError(''); setNotice(''); setBusy(true)
    try {
      const body = new FormData()
      Object.entries(form).forEach(([k, v]) => body.append(k, v))
      body.append('latitude', String(position[0])); body.append('longitude', String(position[1]))
      if (image) body.append('image', image)
      const created = await request('/complaints', token, { method: 'POST', body })
      setNotice(`Complaint #${created.id} submitted successfully.`)
      setForm({ title: '', description: '', category: 'Road damage', location: 'Bhopal, Madhya Pradesh' })
      setImage(null); await load()
    } catch (e) { setError(e.message) } finally { setBusy(false) }
  }

  async function updateStatus(id, status) {
    setError(''); setNotice('')
    try {
      await request(`/complaints/${id}/status`, token, { method: 'PATCH', body: JSON.stringify({ status }) })
      setNotice(`Complaint #${id} updated to ${status.replace('_', ' ')}.`); await load()
    } catch (e) { setError(e.message) }
  }

  if (!session) return <main className="auth-page">
    <section className="auth-brand">
      <div className="logo">CC</div>
      <p className="eyebrow">SMART CIVIC REPORTING</p>
      <h1>Make your city<br/><span>better together.</span></h1>
      <p>Report local issues, follow progress, and help your community thrive.</p>
      <div className="demo-hint-box">
        <p className="demo-hint-title">🔑 Demo Credentials</p>
        <div className="demo-creds">
          <div><b>Admin:</b> admin@civic.demo / admin123</div>
          <div><b>Citizen:</b> manish@civic.demo / manish123</div>
        </div>
      </div>
      <div className="mini-stats">
        <div><b>01</b><small>Report</small></div>
        <div><b>02</b><small>Track</small></div>
        <div><b>03</b><small>Resolve</small></div>
      </div>
    </section>
    <section className="auth-card">
      <div className="mobile-logo">CivicConnect</div>
      <p className="eyebrow">{mode === 'login' ? 'WELCOME BACK' : 'JOIN YOUR COMMUNITY'}</p>
      <h2>{mode === 'login' ? 'Sign in to CivicConnect' : 'Create your account'}</h2>
      <p className="muted">Use your account to report and track civic issues.</p>
      {notice && <div className="notice">{notice}</div>}
      {error && <div className="error">{error}</div>}
      <form onSubmit={authenticate} className="stack">
        {mode === 'register' && <label>Full name<input required maxLength="100" value={authForm.name} onChange={e=>setAuthForm({...authForm,name:e.target.value})} placeholder="Your full name"/></label>}
        <label>Email address<input type="email" required value={authForm.email} onChange={e=>setAuthForm({...authForm,email:e.target.value})} placeholder="you@example.com"/></label>
        <label>Password<input type="password" required minLength="8" value={authForm.password} onChange={e=>setAuthForm({...authForm,password:e.target.value})} placeholder="At least 8 characters"/></label>
        <button className="primary full" disabled={busy}>{busy ? 'Please wait…' : mode === 'login' ? 'Sign in' : 'Create account'}</button>
      </form>
      <p className="switch">{mode === 'login' ? "New to CivicConnect?" : 'Already have an account?'} <button className="text-button" onClick={()=>{setMode(mode==='login'?'register':'login');setError('');setNotice('')}}>{mode==='login'?'Create an account':'Sign in'}</button></p>
      <p className="footnote">SIH-style demonstration project · Secure JWT access</p>
    </section>
  </main>

  return <div className="app-shell">
    <aside className="sidebar">
      <div className="brand"><div className="logo">CC</div><div><b>CivicConnect</b><small>COMMUNITY PORTAL</small></div></div>
      <div className="nav-label">WORKSPACE</div>
      <div className="nav-item active"><span>▦</span> Overview</div>
      <div className="nav-item"><span>◎</span> {isAdmin?'All complaints':'My complaints'}</div>
      <div className="sidebar-bottom">
        <div className="avatar">{session.name?.slice(0,1).toUpperCase()}</div>
        <div className="user-mini"><b>{session.name}</b><small>{isAdmin?'Administrator':'Citizen account'}</small></div>
        <button className="icon-button" title="Sign out" onClick={logout}>↗</button>
      </div>
    </aside>
    <main className="main-area">
      <header className="topbar">
        <div><p className="eyebrow">CIVIC ISSUE MANAGEMENT</p><h1>{isAdmin?'Admin dashboard':'Community dashboard'}</h1></div>
        <div className="top-actions">
          <span className="role-pill">{isAdmin?'ADMIN':'CITIZEN'}</span>
          <button className="outline" onClick={load}>↻ Refresh</button>
        </div>
      </header>
      {notice && <div className="notice dismissible">{notice}<button onClick={()=>setNotice('')}>×</button></div>}
      {error && <div className="error dismissible">{error}<button onClick={()=>setError('')}>×</button></div>}
      {isAdmin && stats && <div className="stat-grid">
        <Stat title="Total complaints" value={stats.total} icon="▤"/>
        <Stat title="Pending" value={stats.pending} icon="◷"/>
        <Stat title="In progress" value={stats.inProgress} icon="↻"/>
        <Stat title="Resolved" value={stats.resolved} icon="✓"/>
      </div>}
      <div className="content-grid">
        {!isAdmin && <section className="panel report-panel">
          <div className="panel-heading"><div><p className="eyebrow">RAISE YOUR VOICE</p><h2>Report a civic issue</h2></div><span className="step-tag">NEW REPORT</span></div>
          <form onSubmit={submitComplaint} className="stack">
            <label>Issue title<input required maxLength="150" value={form.title} onChange={e=>setField('title',e.target.value)} placeholder="e.g. Streetlight not working"/></label>
            <label>Category<select value={form.category} onChange={e=>setField('category',e.target.value)}>
              <option>Road damage</option><option>Garbage collection</option><option>Streetlight</option>
              <option>Water supply</option><option>Drainage</option><option>Public safety</option><option>Other</option>
            </select></label>
            <label>Description<textarea required maxLength="4000" rows="3" value={form.description} onChange={e=>setField('description',e.target.value)} placeholder="Describe the issue and nearby landmarks…"/></label>
            <label>Location / landmark<input required maxLength="500" value={form.location} onChange={e=>setField('location',e.target.value)} placeholder="Street, area, city"/></label>
            <div>
              <div className="label-row">
                <label>Pin the location on the map</label>
                <button type="button" className="text-button" onClick={()=>setPosition(defaultCenter)}>Reset map</button>
              </div>
              <div className="map-wrap">
                <MapContainer center={position || defaultCenter} zoom={13} scrollWheelZoom={false} key={String(position?.[0])} style={{height:'230px',width:'100%'}}>
                  <TileLayer attribution='&copy; OpenStreetMap contributors' url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"/>
                  <PickLocation position={position} setPosition={setPosition}/>
                </MapContainer>
              </div>
              <p className="map-coords">Selected: {position[0].toFixed(5)}, {position[1].toFixed(5)} · <a href={mapLink} target="_blank" rel="noreferrer">Open map ↗</a></p>
            </div>
            <label>Attach photo <span className="muted inline-note">(JPEG, PNG or WEBP; max 5 MB)</span><input type="file" accept="image/jpeg,image/png,image/webp" onChange={e=>setImage(e.target.files?.[0] || null)}/></label>
            <button className="primary full" disabled={busy}>{busy?'Submitting…':'＋ Submit complaint'}</button>
          </form>
        </section>}
        <section className="panel list-panel">
          <div className="panel-heading"><div><p className="eyebrow">{isAdmin?'RESPONSE CENTER':'YOUR ACTIVITY'}</p><h2>{isAdmin?'All reported issues':'Complaint tracking'}</h2></div><span className="count-pill">{complaints.length} records</span></div>
          {isAdmin && <p className="muted">Review citizen reports and update their progress. Status updates trigger email when SMTP is configured.</p>}
          {complaints.length===0 ? <div className="empty"><div className="empty-icon">◎</div><h3>No complaints yet</h3><p>Submitted complaints will appear here.</p></div> :
          <div className="complaint-list">{complaints.map(c=><article className="complaint-card" key={c.id}>
            <div className="complaint-top"><span className="ticket">#{c.id} · {c.category}</span><Status status={c.status}/></div>
            <h3>{c.title}</h3><p className="description">{c.description}</p>
            <div className="meta-line"><span>⌖ {c.location}</span>{c.latitude!=null&&c.longitude!=null&&<a target="_blank" rel="noreferrer" href={`https://www.openstreetmap.org/?mlat=${c.latitude}&mlon=${c.longitude}#map=17/${c.latitude}/${c.longitude}`}>View map ↗</a>}</div>
            {c.imagePath && <a className="image-link" href={`http://localhost:8080${c.imagePath}`} target="_blank" rel="noreferrer">View attached photo ↗</a>}
            <div className="complaint-bottom">
              <span className="muted">Reported {c.createdAt ? new Date(c.createdAt).toLocaleDateString() : ''}{isAdmin&&c.createdBy?.email ? ` · ${c.createdBy.email}` : ''}</span>
              {isAdmin&&<select aria-label={`Update status for complaint ${c.id}`} value={c.status} onChange={e=>updateStatus(c.id,e.target.value)}>
                <option value="PENDING">Pending</option><option value="IN_PROGRESS">In progress</option>
                <option value="RESOLVED">Resolved</option><option value="REJECTED">Rejected</option>
              </select>}
            </div>
          </article>)}</div>}
        </section>
      </div>
      <footer>Built for civic innovation · SIH demonstration · Map data © OpenStreetMap contributors</footer>
    </main>
  </div>
}

function Stat({title,value,icon}) { return <div className="stat-card"><div className="stat-icon">{icon}</div><p>{title}</p><strong>{value}</strong></div> }
function Status({status}) { const cls=status?.toLowerCase().replace('_','-'); return <span className={`status ${cls}`}>{(status||'PENDING').replace('_',' ')}</span> }
export default App
