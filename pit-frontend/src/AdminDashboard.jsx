// src/AdminDashboard.jsx
import React, { useEffect, useState } from 'react';

const API_BASE = process.env.REACT_APP_API_BASE || 'http://localhost:8080/api/v1';

export default function AdminDashboard() {
  const [issues, setIssues] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const token = localStorage.getItem('pit_token') || '';

  useEffect(() => {
    fetchIssues();
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
    } catch (e) {
      setError(String(e));
    } finally {
      setLoading(false);
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
      if (!res.ok) throw new Error(`Status change failed: ${res.status}`);
      await res.json();
      // reload issues automatically
      fetchIssues();
    } catch (e) {
      alert('Error changing status: ' + e);
    }
  }

  async function addComment(issueId, text) {
    if (!text || text.trim() === '') return;
    try {
      const res = await fetch(`${API_BASE}/issues/${issueId}/comments`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: token ? `Bearer ${token}` : undefined,
        },
        body: JSON.stringify({ text }),
      });
      if (!res.ok) throw new Error(`Add comment failed: ${res.status}`);
      await res.json();
      fetchIssues();
    } catch (e) {
      alert('Error adding comment: ' + e);
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
            issues.map((issue) => (
              <tr key={issue.id}>
                <td style={cellStyle}>{issue.title}</td>
                <td style={cellStyle}>{issue.description}</td>
                <td style={cellStyle}>
                  {issue.reporterName || issue.reporterEmail || issue.reporterId}
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
                  <CommentBox onSubmit={(text) => addComment(issue.id, text)} />
                  <div style={{ marginTop: 6, color: '#666', fontSize: 12 }}>
                    Location: {issue.latitude}, {issue.longitude}
                  </div>
                </td>
              </tr>
            ))
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
