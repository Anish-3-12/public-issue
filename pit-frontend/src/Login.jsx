// src/Login.jsx
import React, { useState } from 'react';

const API_BASE = process.env.REACT_APP_API_BASE || 'http://localhost:8080/api/v1';

export default function Login({ onLogin }) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [role, setRole] = useState('');

  async function submit(e) {
    e.preventDefault();
    setLoading(true);
    try {
      const res = await fetch(`${API_BASE}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
      });

      const data = await res.json();
      if (!res.ok) {
        const msg = data?.message || JSON.stringify(data) || 'Login failed';
        alert(msg);
      } else {
        const token =
          data.accessToken ||
          data.token ||
          data.jwt ||
          data.access_token ||
          data.data?.token;
        if (!token) {
          alert(
            'Login succeeded but no token found in response. Check backend response shape.'
          );
        } else {
          localStorage.setItem('pit_token', token);
          onLogin(token);

          // Decode JWT payload to extract role
          try {
            const payload = token.split('.')[1];
            const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
            const padded = base64 + '==='.slice((base64.length + 3) % 4);
            const json = atob(padded);
            const obj = JSON.parse(json);
            const r =
              obj.role ||
              (obj.user && obj.user.role) ||
              (Array.isArray(obj.roles) ? obj.roles[0] : obj.roles) ||
              '';
            setRole(r);
          } catch (err) {
            setRole('unknown');
          }
        }
      }
    } catch (err) {
      alert('Network error: ' + err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div>
      <form
        onSubmit={submit}
        style={{
          display: 'flex',
          flexDirection: 'column',
          gap: 8,
          marginBottom: 12,
        }}
      >
        <input
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        <input
          placeholder="Password"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        <button type="submit" disabled={loading}>
          {loading ? 'Logging in...' : 'Login'}
        </button>
      </form>

      {role && (
        <div style={{ fontSize: 13, color: role === 'ADMIN' ? 'green' : 'blue' }}>
          Logged in role: <strong>{role}</strong>
        </div>
      )}
    </div>
  );
}
