// src/AdminDashboard.jsx
import React, { useEffect, useState } from 'react';

const API_BASE = process.env.REACT_APP_API_BASE || 'http://localhost:8080/api/v1';

export default function AdminDashboard() {
  const [issues, setIssues] = useState([]);
  const [commentsByIssue, setCommentsByIssue] = useState({}); // { [issueId]: [comment, ...] }
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const token = localStorage.getItem('pit_token') || '';

  useEffect(() => {
    fetchIssues();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function fetchIssues() {
    setLoading(true);
    setError('');
    try {
      const res = await fetch(`${API_BASE}/issues`, {
        headers: { Authorization: token ? `Bearer ${token}` : undefined },
      });
      if (!res.ok) throw new Error(`Server returned ${res.status}`);
      const data = await res.json();
      setIssues(data);

      // fetch comments for each issue in background (concurrently)
      data.forEach(issue => {
        fetchComments(issue.id);
      });
    } catch (e) {
      setError(String(e));
    } finally {
      setLoading(false);
    }
  }

  // fetch and store comments for a single issue
  async function fetchComments(issueId) {
    try {
      const res = await fetch(`${API_BASE}/issues/${issueId}/comments`, {
        headers: { Authorization: token ? `Bearer ${token}` : undefined },
      });
      if (!res.ok) {
        // don't throw — treat missing comments as empty
        setCommentsByIssue(prev => ({ ...prev, [issueId]: [] }));
        return;
      }
      const data = await res.json();
      setCommentsByIssue(prev => ({ ...prev, [issueId]: data }));
    } catch (e) {
      setCommentsByIssue(prev => ({ ...prev, [issueId]: [] }));
    }
  }

  async function changeStatus(issueId, newStatus) {
    try {
      const res = await fetch(
        `${API_BASE}/issues/${issueId}/status?status=${encodeURIComponent(newStatus)}`,
        {
          method: 'PATCH',
          headers: {
            'Content-Type': 'application/json',
            Authorization: token ? `Bearer ${token}` : undefined,
          },
        }
      );
      if (!res.ok) {
        let body = '';
        try { body = await res.text(); } catch {}
        throw new Error(`Status change failed: ${res.status}${body ? ' — ' + body : ''}`);
      }
      await res.json();
      // reload issues to get updated statuses
      fetchIssues();
    } catch (e) {
      alert('Error changing status: ' + e.message);
    }
  }

  // improved addComment: appends new comment to local map on success
  async function addComment(issueId, text) {
    if (!text || text.trim() === '') {
      alert('Please type a comment before sending.');
      return;
    }
    try {
      const res = await fetch(`${API_BASE}/issues/${issueId}/comments`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: token ? `Bearer ${token}` : undefined,
        },
        body: JSON.stringify({ text }), // backend expects "text"
      });

      if (!res.ok) {
        let errText = '';
        try { errText = await res.text(); } catch {}
        throw new Error(`${res.status}${errText ? ' — ' + errText : ''}`);
      }

      const created = await res.json();

      // append to commentsByIssue
      setCommentsByIssue(prev => {
        const list = prev[issueId] ? [...prev[issueId]] : [];
        list.push(created);
        return { ...prev, [issueId]: list };
      });

      // also refresh issues list (optional) so status / counts update
      fetchIssues();
    } catch (e) {
      alert('Error adding comment: ' + e.message);
      console.error('Add comment error', e);
    }
  }

  function statusColor(status) {
    switch ((status || '').toUpperCase()) {
      case 'OPEN':
        return '#d9534f'; // red
      case 'IN_PROGRESS':
        return '#f0ad4e'; // orange
      case 'RESOLVED':
        return '#5cb85c'; // green
      case 'VERIFIED':
        return '#0275d8'; // blue
      default:
        return '#6c757d'; // gray
    }
  }

  if (loading) return <div>Loading issues...</div>;
  if (error) return <div style={{ color: 'red' }}>Error: {error}</div>;

  return (
    <div style={{ padding: 12 }}>
      <h2>Admin Dashboard — All Issues</h2>
      <p>
        Logged in as <code>ADMIN</code>. Token is read from localStorage key{' '}
        <code>pit_token</code>.
      </p>
      <button onClick={fetchIssues}>Refresh</button>

      <table
        style={{
          width: '100%',
          borderCollapse: 'collapse',
          marginTop: 12,
          fontSize: 14,
        }}
      >
        <thead>
          <tr style={{ background: '#f2f2f2' }}>
            <th style={cellStyle}>Title</th>
            <th style={cellStyle}>Description</th>
            <th style={cellStyle}>Reporter</th>
            <th style={cellStyle}>Status</th>
            <th style={cellStyle}>Actions</th>
            <th style={cellStyle}>Feedback</th>
          </tr>
        </thead>
        <tbody>
          {issues.length === 0 ? (
            <tr>
              <td colSpan={6} style={{ textAlign: 'center', padding: 12 }}>
                No issues found.
              </td>
            </tr>
          ) : (
            issues.map((issue) => {
              const comments = commentsByIssue[issue.id] || [];
              return (
                <tr key={issue.id}>
                  <td style={cellStyle}>{issue.title}</td>
                  <td style={cellStyle}>{issue.description}</td>
                  <td style={cellStyle}>
                    {issue.createdByName || issue.reporterName || issue.reporterEmail || issue.reporterId}
                  </td>
                  <td style={{ ...cellStyle, minWidth: 110 }}>
                    <span
                      style={{
                        background: statusColor(issue.status),
                        color: '#fff',
                        padding: '4px 8px',
                        borderRadius: 6,
                        fontWeight: 600,
                        display: 'inline-block',
                        minWidth: 80,
                        textAlign: 'center',
                      }}
                    >
                      {issue.status}
                    </span>
                  </td>
                  <td style={cellStyle}>
                    {['OPEN', 'IN_PROGRESS', 'RESOLVED', 'VERIFIED'].map((s) => (
                      <button
                        key={s}
                        disabled={issue.status === s}
                        onClick={() => changeStatus(issue.id, s)}
                        style={{ marginRight: 6 }}
                      >
                        {s}
                      </button>
                    ))}
                  </td>

                  <td style={cellStyle}>
                    <CommentBox
                      onSubmit={(text) => addComment(issue.id, text)}
                    />
                    <div style={{ marginTop: 8 }}>
                      {comments.length === 0 ? (
                        <div style={{ color: '#666', fontSize: 12 }}>No feedback yet</div>
                      ) : (
                        <div style={{ maxHeight: 200, overflowY: 'auto' }}>
                          {comments.map(c => (
                            <div key={c.id || c.createdAt} style={{ marginBottom: 8, borderBottom: '1px dashed #eee', paddingBottom: 6 }}>
                              <div style={{ fontSize: 13, fontWeight: 600 }}>
                                {c.authorName || c.authorId || 'Anonymous'}
                                <small style={{ marginLeft: 8, fontWeight: 400, color: '#666' }}>
                                  {c.createdAt ? new Date(c.createdAt).toLocaleString() : ''}
                                </small>
                              </div>
                              <div style={{ fontSize: 13 }}>{c.message || c.text || c.note}</div>
                            </div>
                          ))}
                        </div>
                      )}
                    </div>

                    <div style={{ marginTop: 6, color: '#666', fontSize: 12 }}>
                      Location: {issue.latitude}, {issue.longitude}
                    </div>
                  </td>
                </tr>
              );
            })
          )}
        </tbody>
      </table>
    </div>
  );
}

const cellStyle = {
  border: '1px solid #ddd',
  padding: '6px 8px',
  verticalAlign: 'top',
};

function CommentBox({ onSubmit }) {
  const [text, setText] = useState('');
  return (
    <form
      onSubmit={(e) => {
        e.preventDefault();
        onSubmit(text);
        setText('');
      }}
      style={{ display: 'flex', gap: 6, alignItems: 'center' }}
    >
      <input
        placeholder="Feedback"
        value={text}
        onChange={(e) => setText(e.target.value)}
        style={{ width: '70%', fontSize: 12 }}
      />
      <button type="submit" style={{ fontSize: 12 }}>
        Send
      </button>
    </form>
  );
}

