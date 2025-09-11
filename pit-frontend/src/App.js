// src/App.js
import React, { useState, useEffect } from 'react';
import MapView from './MapView';
import CreateIssueForm from './CreateIssueForm';
import Login from './Login';
import Signup from './Signup';
import AdminDashboard from './AdminDashboard';

function App() {
  const [pickedLocation, setPickedLocation] = useState(null);
  const [token, setToken] = useState('');
  const [showSignup, setShowSignup] = useState(false);
  const [view, setView] = useState('map');

  useEffect(() => {
    const t = localStorage.getItem('pit_token') || '';
    setToken(t);
  }, []);

  function handleLogin(newToken) {
    setToken(newToken);
    if (newToken) localStorage.setItem('pit_token', newToken);
  }

  function logout() {
    localStorage.removeItem('pit_token');
    setToken('');
    setView('map');
  }

  function handleSignedUp() {
    setShowSignup(false);
  }

  function getRoleFromToken(t) {
    if (!t) return null;
    try {
      const parts = t.split('.');
      if (parts.length < 2) return null;
      const payload = parts[1];
      const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
      const padded = base64 + '==='.slice((base64.length + 3) % 4);
      const json = atob(padded);
      const obj = JSON.parse(json);
      if (obj.role) return obj.role;
      if (obj.roles) return Array.isArray(obj.roles) ? obj.roles[0] : obj.roles;
      if (obj.user && obj.user.role) return obj.user.role;
      return null;
    } catch {
      return null;
    }
  }

  const role = getRoleFromToken(token);
  const isAdmin = role === 'ADMIN';

  return (
    <div style={{ padding: 20, fontFamily: 'Arial, sans-serif' }}>
      <h1>Public Issue Tracker</h1>

      <div style={{ marginBottom: 12 }}>
        {token ? (
          <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
            <strong style={{ color: 'green' }}>Logged in</strong>

            {/* Only show role if ADMIN */}
            {isAdmin && (
              <div
                style={{
                  fontSize: 13,
                  color: 'white',
                  background: 'green',
                  padding: '2px 6px',
                  borderRadius: 4,
                }}
              >
                role: ADMIN
              </div>
            )}

            <button onClick={logout}>Logout</button>

            <div style={{ marginLeft: 12 }}>
              <button onClick={() => setView('map')}>Map / Create Issue</button>
              {isAdmin && (
                <button
                  onClick={() => setView('admin')}
                  style={{ marginLeft: 8 }}
                >
                  Open Admin Dashboard
                </button>
              )}
            </div>
          </div>
        ) : (
          <div style={{ display: 'flex', gap: 16, alignItems: 'flex-start' }}>
            <div style={{ minWidth: 260 }}>
              {showSignup ? (
                <>
                  <h4 style={{ margin: '4px 0' }}>Create an account</h4>
                  <Signup onSignedUp={handleSignedUp} />
                  <p style={{ fontSize: 13 }}>
                    Already have an account?{' '}
                    <button onClick={() => setShowSignup(false)}>Login</button>
                  </p>
                </>
              ) : (
                <>
                  <h4 style={{ margin: '4px 0' }}>Login</h4>
                  <Login onLogin={handleLogin} />
                  <p style={{ fontSize: 13 }}>
                    New here?{' '}
                    <button onClick={() => setShowSignup(true)}>Sign up</button>
                  </p>
                </>
              )}
            </div>
          </div>
        )}
      </div>

      {view === 'admin' ? (
        <div>
          <div style={{ marginBottom: 12 }}>
            <button onClick={() => setView('map')}>← Back to Map</button>
          </div>
          <AdminDashboard />
        </div>
      ) : (
        <div style={{ display: 'flex', gap: 20 }}>
          <div style={{ flex: 2 }}>
            <MapView onPickLocation={setPickedLocation} />
          </div>

          <div style={{ flex: 1 }}>
            <h3>Create Issue</h3>
            <p style={{ fontSize: 12 }}>
              Click on the map to pick location — lat/lng will fill the form.
            </p>

            <CreateIssueForm token={token} pickedLocation={pickedLocation} />
          </div>
        </div>
      )}
    </div>
  );
}

export default App;
