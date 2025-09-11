// src/App.js
import React, { useState, useEffect } from 'react';
import MapView from './MapView';
import CreateIssueForm from './CreateIssueForm';
import Login from './Login';
import Signup from './Signup';

function App() {
  const [pickedLocation, setPickedLocation] = useState(null);
  const [token, setToken] = useState('');
  const [showSignup, setShowSignup] = useState(false);

  // load token from localStorage on start (so you stay logged in between reloads)
  useEffect(() => {
    const t = localStorage.getItem('pit_token') || '';
    setToken(t);
  }, []);

  function handleLogin(newToken) {
    // save token to state and localStorage
    setToken(newToken);
    if (newToken) localStorage.setItem('pit_token', newToken);
  }

  function logout() {
    localStorage.removeItem('pit_token');
    setToken('');
  }

  // convenience: keep localStorage in sync when user types/pastes token in the dev box
  function handlePasteTokenInput(value) {
    setToken(value);
    if (value) {
      localStorage.setItem('pit_token', value);
    } else {
      localStorage.removeItem('pit_token');
    }
  }

  function handleSignedUp() {
    // after signup, switch to login view
    setShowSignup(false);
  }

  return (
    <div style={{ padding: 20, fontFamily: 'Arial, sans-serif' }}>
      <h1>Public Issue Tracker (Leaflet)</h1>

      <div style={{ marginBottom: 12 }}>
        {token ? (
          <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
            <strong style={{ color: 'green' }}>Logged in</strong>
            <button onClick={logout}>Logout</button>
            <small style={{ marginLeft: 8, color: '#444' }}>
              (token saved in localStorage)
            </small>
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
                    New here? <button onClick={() => setShowSignup(true)}>Sign up</button>
                  </p>
                </>
              )}
            </div>
          </div>
        )}
      </div>

      <div style={{ display: 'flex', gap: 20 }}>
        <div style={{ flex: 2 }}>
          <MapView onPickLocation={setPickedLocation} />
        </div>

        <div style={{ flex: 1 }}>
          <h3>Create Issue</h3>
          <p style={{ fontSize: 12 }}>Click on the map to pick location — lat/lng will fill the form.</p>

          <CreateIssueForm token={token} pickedLocation={pickedLocation} />

          {/* Dev helper: paste token box so you can quickly set a JWT during development */}
          <div style={{ marginTop: 16, paddingTop: 12, borderTop: '1px solid #eee' }}>
            <h4 style={{ margin: '4px 0' }}>Dev: paste JWT token</h4>
            <input
              placeholder="Paste JWT token here"
              value={token}
              onChange={e => handlePasteTokenInput(e.target.value)}
              style={{ width: '100%', padding: '8px', boxSizing: 'border-box' }}
            />
            <div style={{ display: 'flex', gap: 8, marginTop: 8 }}>
              <button
                onClick={() => {
                  // quick reload so components pick up token change if needed
                  window.location.reload();
                }}
              >
                Reload
              </button>
              <button
                onClick={() => {
                  handlePasteTokenInput('');
                  alert('Token cleared from localStorage');
                }}
              >
                Clear token
              </button>
            </div>
            <p style={{ fontSize: 12, color: '#666', marginTop: 8 }}>
              Tip: after pasting a token, click <strong>Reload</strong> to ensure all components read it.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}

export default App;
