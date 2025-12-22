import React, { useState, useEffect, useRef, useCallback } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import SearchInput from './SearchInput';
import './SessionList.css';

const MIN_SEARCH_CHARS = 3;

export default function SessionList({ onSelectSession, onUploadNew }) {
  const [sessions, setSessions] = useState([]);
  const [recentSessions, setRecentSessions] = useState([]);
  const [searchActive, setSearchActive] = useState(false);
  const [connected, setConnected] = useState(false);

  const stompClientRef = useRef(null);
  const activeTokenRef = useRef(0);


  /* ---------------- Recent Sessions ---------------- */
  useEffect(() => {
    loadRecentSessions();
  }, []);

  const loadRecentSessions = async () => {
    try {
      const API_BASE_URL =
          import.meta.env.MODE === 'development' ? 'http://localhost:8080' : '';
      const res = await fetch(`${API_BASE_URL}/api/v1/chat/sessions`);
      const data = await res.json();

      const sorted = data.sort(
          (a, b) =>
              new Date(b.lastInteractionAt || b.createdAt) -
              new Date(a.lastInteractionAt || a.createdAt)
      );

      setRecentSessions(sorted.slice(0, 5));
    } catch (e) {
      console.error('Failed to load recent sessions', e);
    }
  };

  /* ---------------- WebSocket ---------------- */
  useEffect(() => {
    const wsUrl =
        import.meta.env.VITE_WS_URL ||
        `${window.location.protocol}//${window.location.host}/ws`;

    const client = new Client({
      webSocketFactory: () => new SockJS(wsUrl),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        setConnected(true);

        client.subscribe('/user/queue/search/sessions', (msg) => {
          const payload = JSON.parse(msg.body);
          const { results, token } = payload || {};

          if (token !== activeTokenRef.current) return;

          setSessions(Array.isArray(results) ? results : []);
          setShowResults(true);
        });
      },
      onWebSocketClose: () => setConnected(false),
      onStompError: () => setConnected(false),
    });

    client.activate();
    stompClientRef.current = client;

    return () => {
      client.deactivate();
    };
  }, []);

  /* ---------------- Search ---------------- */
  const handleSearch = useCallback(
    async (query) => {
      if (!connected || !stompClientRef.current) return;

      const token = ++activeTokenRef.current;
      setSearchActive(true);

      stompClientRef.current.publish({
        destination: '/app/search/sessions',
        body: JSON.stringify({ query, token }),
      });
    },
    [connected]
  );


  const formatDate = (date) => {
    const d = new Date(date);
    const diff = (Date.now() - d) / 60000;
    if (diff < 1) return 'Just now';
    if (diff < 60) return `${Math.floor(diff)}m ago`;
    if (diff < 1440) return `${Math.floor(diff / 60)}h ago`;
    return d.toLocaleDateString();
  };

  return (
      <div className="session-list-page ds-page">
        <div className="session-list-card ds-card">
          {/* Header */}
          <header className="ds-card-header">
            <h1 className="ds-card-title">📄 Document Q&A</h1>
            <p className="ds-card-subtitle">Select a session or upload a new document</p>
          </header>

          {/* Search */}
          <div className="ds-input-group" style={{ marginBottom: 'var(--spacing-xl)' }}>
            <label className="ds-label">Search sessions</label>
            <SearchInput
              placeholder={`Search by name (${MIN_SEARCH_CHARS}+ chars)`}
              onSearch={handleSearch}
              showResults={searchActive}
              connectionIndicator={connected && <span className="ds-connection-dot" />}
            >
              {/* Search Results */}
              {sessions.length === 0 ? (
                <div className="ds-state">No matching sessions</div>
              ) : (
                <div className="session-search-results">
                  {sessions.map((s) => (
                    <button
                      key={s.id || s.sessionId}
                      className="session-search-item"
                      onClick={() => onSelectSession(s)}
                    >
                      <div className="session-search-item-title">
                        {s.documentName || 'Untitled Document'}
                      </div>
                      <div className="session-search-item-date">
                        {formatDate(s.lastInteractionAt || s.createdAt)}
                      </div>
                    </button>
                  ))}
                </div>
              )}
            </SearchInput>
          </div>

          {/* Upload Button */}
          {!searchActive && (
            <button className="ds-btn ds-btn-primary ds-btn-lg" onClick={onUploadNew} style={{ width: '100%', marginBottom: 'var(--spacing-xl)' }}>
              <span>📄</span>
              <span>Upload new document</span>
            </button>
          )}

          {/* Recent Sessions */}
          {!searchActive && recentSessions.length > 0 && (
            <section>
              <h3 className="session-section-title">Recent sessions</h3>
              <div className="ds-grid">
                {recentSessions.map((s) => (
                  <button
                    key={s.id || s.sessionId}
                    className="ds-list-item"
                    onClick={() => onSelectSession(s)}
                  >
                    <div className="ds-list-item-title">
                      {s.documentName || 'Untitled Document'}
                    </div>
                    <div className="ds-list-item-subtitle">
                      {formatDate(s.lastInteractionAt || s.createdAt)}
                    </div>
                  </button>
                ))}
              </div>
            </section>
          )}

          {/* Empty State */}
          {!searchActive && recentSessions.length === 0 && (
            <div className="ds-empty-state">
              <div className="ds-empty-state-icon">📭</div>
              <div className="ds-empty-state-title">No sessions yet</div>
              <div className="ds-empty-state-text">Upload a document to get started</div>
            </div>
          )}
        </div>
      </div>
  );
}
