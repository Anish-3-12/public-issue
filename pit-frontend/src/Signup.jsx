// src/Signup.jsx
import React, { useState } from 'react';

const API_BASE = process.env.REACT_APP_API_BASE || 'http://localhost:8080/api/v1';

export default function Signup({ onSignedUp }) {
  const [name, setName] = useState('');           // if backend expects username, this covers it
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  async function submit(e) {
    e.preventDefault();
    setLoading(true);

    // Adjust payload keys here if your backend expects different names (e.g. username)
    const payload = {
      name: name || undefined,
      email,
      password
    };

    try {
      const res = await fetch(`${API_BASE}/auth/signup`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      const data = await res.json().catch(() => ({}));

      if (!res.ok) {
        // try to show helpful error message
        const msg = data?.message || data?.error || JSON.stringify(data) || 'Signup failed';
        alert(msg);
      } else {
        // signup success — backend might return user object or token; we call onSignedUp so parent can switch to login
        alert('Signup successful! Please login.');
        onSignedUp && onSignedUp();
      }
    } catch (err) {
      alert('Network error: ' + err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <form onSubmit={submit} style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
      <input
        placeholder="Full name (optional)"
        value={name}
        onChange={e => setName(e.target.value)}
      />
      <input
        placeholder="Email"
        value={email}
        onChange={e => setEmail(e.target.value)}
        required
      />
      <input
        placeholder="Password"
        type="password"
        value={password}
        onChange={e => setPassword(e.target.value)}
        required
      />
      <button type="submit" disabled={loading}>
        {loading ? 'Signing up...' : 'Sign up'}
      </button>
    </form>
  );
}
