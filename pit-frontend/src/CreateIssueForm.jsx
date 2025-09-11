// src/CreateIssueForm.jsx
import React, { useState, useEffect } from 'react';

const API_BASE = process.env.REACT_APP_API_BASE || 'http://localhost:8080/api/v1';

function maskToken(t) {
  if (!t) return '(none)';
  return t.slice(0,6) + '...' + t.slice(-6);
}

export default function CreateIssueForm({ token: propToken, pickedLocation, onCreated }) {
  const [form, setForm] = useState({
    title: '',
    description: '',
    category: '',
    latitude: pickedLocation?.latitude ?? '',
    longitude: pickedLocation?.longitude ?? '',
    address: ''
  });
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (pickedLocation) {
      setForm(f => ({ ...f, latitude: pickedLocation.latitude, longitude: pickedLocation.longitude }));
    }
  }, [pickedLocation]);

  function onChange(e) {
    const { name, value } = e.target;
    setForm(f => ({ ...f, [name]: value }));
  }

  async function submit(e) {
    e.preventDefault();
    setLoading(true);

    // get token: prefer prop token (App state), but fallback to localStorage
    const token = propToken || localStorage.getItem('pit_token') || '';

    console.log('[CreateIssue] Using token (masked):', maskToken(token));
    if (!token) {
      alert('No token found. Please login or paste your token in the dev box.');
      setLoading(false);
      return;
    }

    const body = {
      title: form.title,
      description: form.description,
      category: form.category,
      latitude: form.latitude === '' ? null : Number(form.latitude),
      longitude: form.longitude === '' ? null : Number(form.longitude),
      address: form.address
    };

    try {
      const res = await fetch(`${API_BASE}/issues`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(body)
      });

      // For debugging: log status and text
      const text = await res.text();
      console.log('[CreateIssue] Response status:', res.status);
      console.log('[CreateIssue] Response body:', text);

      if (res.ok) {
        alert('Issue created!');
        setForm({ title: '', description: '', category: '', latitude: '', longitude: '', address: '' });
        onCreated && onCreated();
      } else {
        // Try to parse JSON if possible for better message
        let parsed;
        try { parsed = JSON.parse(text); } catch(e){ parsed = null; }
        const msg = parsed?.message || parsed?.error || text || `HTTP ${res.status}`;
        alert('Failed to create issue: ' + msg);
      }
    } catch (err) {
      // This is often a network/CORS error visible in console
      console.error('[CreateIssue] Network/fetch error:', err);
      alert('Network/fetch error: ' + err.message + '. Check browser console and Network tab for details.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <form onSubmit={submit} style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
      <input name="title" placeholder="Title" value={form.title} onChange={onChange} required />
      <textarea name="description" placeholder="Description" value={form.description} onChange={onChange} required />
      <input name="category" placeholder="Category" value={form.category} onChange={onChange} required />
      <input name="latitude" placeholder="Latitude" value={form.latitude ?? ''} onChange={onChange} required />
      <input name="longitude" placeholder="Longitude" value={form.longitude ?? ''} onChange={onChange} required />
      <input name="address" placeholder="Address (optional)" value={form.address} onChange={onChange} />
      <button type="submit" disabled={loading}>{loading ? 'Creating...' : 'Create Issue'}</button>
    </form>
  );
}
