// src/MyIssues.jsx
import React, { useEffect, useState } from 'react';

const API_BASE = process.env.REACT_APP_API_BASE || 'http://localhost:8080/api/v1';

function statusColor(status) {
  switch ((status || '').toUpperCase()) {
    case 'OPEN': return '#d9534f';
    case 'IN_PROGRESS': return '#f0ad4e';
    case 'RESOLVED': return '#5cb85c';
    case 'VERIFIED': return '#0275d8';
    default: return '#6c757d';
  }
}

function formatCreatedAt(val) {
  if (!val) return '';
  // try common field shapes
  const d = new Date(val);
  if (!isNaN(d.getTime())) return d.toLocaleString();
  // fallback to raw
  return String(val);
}

export default function MyIssues() {
  const [issues, setIssues] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const token = localStorage.getItem('pit_token') || '';

  useEffect(() => {
    // only fetch if token exists
    if (token) fetchMyIssues();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token]);

  async function fetchMyIssues() {
    setLoading(true);
    setError('');
    try {
      if (!token) {
        setIssues([]);
        setError('No auth token found — please log in.');
        return;
      }

      const res = await fetch(`${API_BASE}/me/issues`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (res.status === 401 || res.status === 403) {
        setIssues([]);
        setError('Unauthorized. Please log in again.');
        return;
      }

      if (!res.ok) {
        // try to parse error body if available
        let body = '';
        try { body = await res.text(); } catch {}
        throw new Error(`Server returned ${res.status} ${res.statusText} ${body ? '- ' + body : ''}`);
      }

      const data = await res.json();
      if (!Array.isArray(data)) {
        throw new Error('Unexpected response from server');
      }
      setIssues(data);
    } catch (e) {
      setIssues([]);
      setError(String(e));
    } finally {
      setLoading(false);
    }
  }

  if (!token) return <div style={{ padding: 12 }}>Please log in to view your issues.</div>;
  if (loading) return <div style={{ padding: 12 }}>Loading your issues...</div>;

  return (
    <div style={{ padding: 12 }}>
      <h2>My Issues</h2>

      <div style={{ marginBottom: 10, display: 'flex', gap: 8, alignItems: 'center' }}>
        <button onClick={fetchMyIssues}>Refresh</button>
        <span style={{ color: '#666', fontSize: 13 }}>{issues.length} issue(s)</span>
      </div>

      {error && (
        <div style={{ color: 'red', marginBottom: 10 }}>
          Error: {error}
        </div>
      )}

      {issues.length === 0 && !error ? (
        <div>No issues found for your account.</div>
      ) : (
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ background: '#f2f2f2' }}>
              <th style={cellStyle}>Title</th>
              <th style={cellStyle}>Created</th>
              <th style={cellStyle}>Status</th>
              <th style={cellStyle}>Location</th>
            </tr>
          </thead>
          <tbody>
            {issues.map(issue => (
              <tr key={issue.id || (issue.id === 0 ? 0 : Math.random())}>
                <td style={cellStyle}>{issue.title}</td>
                <td style={cellStyle}>{formatCreatedAt(issue.createdAt || issue.created_at || issue.created)}</td>
                <td style={cellStyle}>
                  <span style={{
                    background: statusColor(issue.status),
                    color: '#fff',
                    padding: '4px 8px',
                    borderRadius: 6,
                    fontWeight: 600,
                    display: 'inline-block',
                    minWidth: 80,
                    textAlign: 'center'
                  }}>{issue.status}</span>
                </td>
                <td style={cellStyle}>
                  {issue.latitude ?? issue.lat ?? ''}{(issue.latitude || issue.lat) && ','} {issue.longitude ?? issue.lng ?? ''}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}

const cellStyle = { border: '1px solid #ddd', padding: '6px 8px' };
