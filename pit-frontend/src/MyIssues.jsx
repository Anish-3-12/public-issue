// src/MyIssues.jsx
import React, { useEffect, useState } from 'react';

const API_BASE = process.env.REACT_APP_API_BASE || 'http://localhost:8080/api/v1';

function decodeToken(t) {
  if (!t) return null;
  try {
    const parts = t.split('.');
    if (parts.length < 2) return null;
    const payload = parts[1];
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
    const padded = base64 + '==='.slice((base64.length + 3) % 4);
    const json = atob(padded);
    return JSON.parse(json);
  } catch {
    return null;
  }
}

function statusColor(status) {
  switch ((status || '').toUpperCase()) {
    case 'OPEN': return '#d9534f';
    case 'IN_PROGRESS': return '#f0ad4e';
    case 'RESOLVED': return '#5cb85c';
    case 'VERIFIED': return '#0275d8';
    default: return '#6c757d';
  }
}

export default function MyIssues() {
  const [issues, setIssues] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const token = localStorage.getItem('pit_token') || '';

  // try to get user id or email from token payload
  const payload = decodeToken(token);
  const myId = payload?.user?.id || payload?.sub || payload?.id || payload?.userId || null;
  const myEmail = payload?.email || payload?.user?.email || null;

  useEffect(() => {
    fetchMyIssues();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function fetchMyIssues() {
    setLoading(true); setError('');
    try {
      const res = await fetch(`${API_BASE}/issues`, {
        headers: { Authorization: token ? `Bearer ${token}` : undefined },
      });
      if (!res.ok) throw new Error(`Server returned ${res.status}`);
      const data = await res.json();
      // attempt best-effort filter: reporterId, reporterEmail or reporterName match
      const mine = data.filter(issue => {
        if (!issue) return false;
        if (myId && (issue.reporterId === myId || issue.reporterId === String(myId))) return true;
        if (myEmail && (issue.reporterEmail === myEmail || issue.reporterEmail?.toLowerCase() === myEmail?.toLowerCase())) return true;
        // fallback: if issue has a 'reporter' object
        if (issue.reporter && (issue.reporter.id === myId || issue.reporter.email === myEmail)) return true;
        return false;
      });
      setIssues(mine);
    } catch (e) {
      setError(String(e));
    } finally {
      setLoading(false);
    }
  }

  if (loading) return <div>Loading your issues...</div>;
  if (error) return <div style={{ color: 'red' }}>Error: {error}</div>;
  if (!token) return <div>Please log in to view your issues.</div>;

  return (
    <div style={{ padding: 12 }}>
      <h2>My Issues</h2>
      {issues.length === 0 ? (
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
              <tr key={issue.id}>
                <td style={cellStyle}>{issue.title}</td>
                <td style={cellStyle}>{issue.createdAt || issue.created_at || issue.created || ''}</td>
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
                <td style={cellStyle}>{issue.latitude ?? issue.lat}, {issue.longitude ?? issue.lng}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}

const cellStyle = { border: '1px solid #ddd', padding: '6px 8px' };
