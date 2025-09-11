// src/MapView.jsx
import React, { useEffect, useState } from 'react';
import { MapContainer, TileLayer, Marker, useMapEvents } from 'react-leaflet';
import L from 'leaflet';

// Fix for default marker icon not showing in many setups
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl:
    'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
});

const API_BASE = process.env.REACT_APP_API_BASE || 'http://localhost:8080/api/v1';

function ClickHandler({ onPickLocation }) {
  // when user clicks on map, call onPickLocation with lat/lng
  useMapEvents({
    click(e) {
      const lat = e.latlng.lat;
      const lng = e.latlng.lng;
      onPickLocation && onPickLocation({ latitude: lat, longitude: lng });
    }
  });
  return null;
}

export default function MapView({ onPickLocation }) {
  const [issues, setIssues] = useState([]);
  const center = [20.5937, 78.9629]; // default center (India-ish)

  useEffect(() => {
    // fetch issues from your backend and show markers
    fetch(`${API_BASE}/issues`)
      .then(r => r.json())
      .then(data => {
        // backend might return an array or an object with content; try common shapes
        const list = Array.isArray(data) ? data : (data.content || data.data || data.items || []);
        setIssues(list);
      })
      .catch(err => {
        console.warn('Failed to load issues:', err);
        setIssues([]);
      });
  }, []);

  return (
    <MapContainer center={center} zoom={6} style={{ height: '70vh', width: '100%' }}>
      <TileLayer
        attribution='© OpenStreetMap contributors'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />
      <ClickHandler onPickLocation={onPickLocation} />
      {issues.map(issue => (issue.latitude && issue.longitude ? (
        <Marker
          key={issue.id || `${issue.latitude}-${issue.longitude}-${Math.random()}`}
          position={[issue.latitude, issue.longitude]}
          title={issue.title || 'Issue'}
        />
      ) : null))}
    </MapContainer>
  );
}
