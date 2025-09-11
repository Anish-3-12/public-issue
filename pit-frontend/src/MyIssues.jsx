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

// safe helper to format createdAt values from different response shapes
function formatDate(value) {
  if (!value) return '';
  // if already an ISO string or timestamp
  try {
    const d = new Date(value);
    if (!isNaN(d.getTime())) return d.toLocaleString();
  } catch (e) { /* ignore */ }
  return String(value);
}

export default function MyIssues() {
  const [issues, setIssues] = useState([]);
  const [commentsByIssue, setCommentsByIssue] = useState({}); // { issueId: { loading, error, data[] } }
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const token = localStorage.getItem('pit_token') || '';

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
      const res = await fetch(`${API_BASE}/me/issues`, {
        headers: { Authorization: token ? `Bearer ${token}` : undefined },
      });
      console.log('fetchMyIssues status=', res.status);
      if (!res.ok) {
        const txt = await safeText(res);
        throw new Error(`Server returned ${res.status}${txt ? ' — ' + txt : ''}`);
      }
      const data = await res.json();
      if (!Array.isArray(data)) {
        throw new Error('Server returned unexpected response shape for /me/issues');
      }

      // fallback filtering just in case /me/issues returns all issues
      const mine = data.filter(issue => {
        if (!issue) return false;
        // try common server fields
        if (myId && (issue.createdById === myId || issue.reporterId === myId || String(issue.reporterId) === String(myId))) return true;
        if (myEmail && (issue.reporterEmail === myEmail || issue.createdByEmail === myEmail)) return true;
        if (issue.reporter && (issue.reporter.id === myId || issue.reporter.email === myEmail)) return true;
        // if backend already scopes to me, keep it
        return true;
      });

      setIssues(mine);
      // fetch comments for each issue (do not await - parallel)
      mine.forEach(i => {
        if (i && i.id) fetchComments(i.id);
      });
    } catch (e) {
      console.error('fetchMyIssues error', e);
      setError(String(e));
    } finally {
      setLoading(false);
    }
  }

  // helper to read response.text() safely
  async function safeText(res) {
    try { return await res.text(); } catch (e) { return ''; }
  }

  async function fetchComments(issueId) {
    // set loading state for that issue
    setCommentsByIssue(prev => ({ ...prev, [issueId]: { loading: true, error: '', data: [] } }));
    try {
      console.log(`Fetching comments for issue ${issueId}`);
      const res = await fetch(`${API_BASE}/issues/${issueId}/comments`, {
        headers: { Authorization: token ? `Bearer ${token}` : undefined },
      });
      console.log(`GET /issues/${issueId}/comments -> ${res.status}`);
      if (!res.ok) {
        const txt = await safeText(res);
        const msg = `Server returned ${res.status}${txt ? ' — ' + txt : ''}`;
        console.warn('fetchComments failed', msg);
        setCommentsByIssue(prev => ({ ...prev, [issueId]: { loading: false, error: msg, data: [] } }));
        return;
      }
      const data = await res.json();
      console.log('comments data', issueId, data);
      setCommentsByIssue(prev => ({ ...prev, [issueId]: { loading: false, error: '', data: Array.isArray(data) ? data : [] } }));
    } catch (e) {
      console.error('fetchComments error', e);
      setCommentsByIssue(prev => ({ ...prev, [issueId]: { loading: false, error: String(e), data: [] } }));
    }
  }

  if (loading) return <div>Loading your issues...</div>;
  if (error) return <div style={{ color: 'red' }}>Error: {error}</div>;
  if (!token) return <div>Please log in to view your issues.</div>;

  return (
    <div style={{ padding: 12 }}>
      <h2>My Issues</h2>
      <div style={{ marginBottom: 8 }}>
        <button onClick={fetchMyIssues}>Refresh</button>{' '}
        <small style={{ color: '#666' }}>{issues.length} issue(s)</small>
      </div>

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
              <th style={cellStyle}>Feedback</th>
            </tr>
          </thead>
          <tbody>
            {issues.map(issue => {
              const cb = commentsByIssue[issue.id] || { loading: false, error: '', data: [] };
              const comments = cb.data || [];
              return (
                <tr key={issue.id}>
                  <td style={cellStyle}>{issue.title}</td>
                  <td style={cellStyle}>{formatDate(issue.createdAt || issue.created_at || issue.created)}</td>
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

                  <td style={cellStyle}>
                    {cb.loading ? (
                      <div style={{ color: '#666' }}>Loading feedback…</div>
                    ) : cb.error ? (
                      <div style={{ color: 'red' }}>Error loading feedback: {cb.error}</div>
                    ) : comments.length === 0 ? (
                      <div style={{ color: '#666' }}>No feedback yet</div>
                    ) : (
                      <div style={{ maxHeight: 240, overflowY: 'auto' }}>
                        {comments.map(c => (
                          <div key={c.id || c.createdAt || Math.random()} style={{ marginBottom: 8, borderBottom: '1px dashed #eee', paddingBottom: 6 }}>
                            <div style={{ fontWeight: 600 }}>
                              {c.authorName || c.authorId || c.author || 'Unknown'}
                              <small style={{ marginLeft: 8, fontWeight: 400, color: '#666' }}>
                                {formatDate(c.createdAt || c.created_at || c.when)}
                              </small>
                            </div>
                            <div>{c.message || c.text || c.note || c.body || ''}</div>
                          </div>
                        ))}
                      </div>
                    )}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      )}
    </div>
  );
}

const cellStyle = { border: '1px solid #ddd', padding: '6px 8px' };
