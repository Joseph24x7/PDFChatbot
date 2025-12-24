import React, { useState, useEffect, useRef } from 'react';
import './ChatBot.css';
import { getChatSession, sendChatMessage } from '../api/documentApi';

export default function ChatBot({ sessionId, documentName, onReset }) {
  const [messages, setMessages] = useState([]);
  const [inputValue, setInputValue] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const messagesEndRef = useRef(null);

  // Load chat history on mount
  useEffect(() => {
    loadChatHistory();
  }, [sessionId]);

  // Scroll to bottom when messages change
  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const loadChatHistory = async () => {
    try {
      const response = await getChatSession(sessionId);
      setMessages(response.data.messages || []);
    } catch (err) {
      console.error('Error loading chat history:', err);
      setError('Failed to load chat history');
    }
  };

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const handleSendMessage = async (e) => {
    e.preventDefault();

    if (!inputValue.trim()) return;

    const userMessage = inputValue.trim();
    setInputValue('');
    setError(null);
    setLoading(true);

    // Add user message immediately to UI
    const userMsg = { role: 'user', content: userMessage };
    setMessages((prev) => [...prev, userMsg]);

    try {
      // Send message via REST API
      const response = await sendChatMessage(sessionId, userMessage);

      // Add assistant response to UI
      const assistantMsg = {
        role: 'assistant',
        content: response.data.currentResponse
      };
      setMessages((prev) => [...prev, assistantMsg]);

    } catch (err) {
      const errorMessage = err.response?.data?.message || err.message || 'Failed to send message. Please try again.';
      setError(`❌ ${errorMessage}`);
      console.error('Chat error:', err);

      // Remove the user message if failed
      setMessages((prev) => prev.slice(0, -1));
    } finally {
      setLoading(false);
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage(e);
    }
  };

  return (
    <div className="chat-container">
      <div className="chat-card">
        {/* Chat Header */}
        <div className="chat-header">
          <div>
            <h2 className="chat-title">💬 {documentName}</h2>
          </div>
          <button className="reset-btn" onClick={onReset}>
            ← Back to Sessions
          </button>
        </div>

        {/* Messages Container */}
        <div className="messages-container">
          {messages.length === 0 ? (
            <div className="empty-state">
              <h3>👋 Start a Conversation</h3>
              <p>Ask any questions about the document below</p>
            </div>
          ) : (
            <>
              {messages.map((msg, idx) => (
                <div key={idx} className={`message-wrapper ${msg.role}`}>
                  <div className={`message ${msg.role}`}>
                    {msg.role === 'assistant' && (
                      <div className="message-avatar">
                        🤖
                      </div>
                    )}
                    <div className="message-bubble">
                      <div className="message-content">{msg.content}</div>
                    </div>
                    {msg.role === 'user' && (
                      <div className="message-avatar">
                        👤
                      </div>
                    )}
                  </div>
                </div>
              ))}
            </>
          )}

          {loading && (
            <div className="message-wrapper assistant">
              <div className="message assistant">
                <div className="message-avatar">🤖</div>
                <div className="message-bubble">
                  <div className="ds-typing">
                    <div className="ds-typing-dot"></div>
                    <div className="ds-typing-dot"></div>
                    <div className="ds-typing-dot"></div>
                  </div>
                </div>
              </div>
            </div>
          )}

          <div ref={messagesEndRef} />
        </div>

        {/* Error Toast */}
        {error && (
          <div className="error-toast">
            {error}
            <span
              className="close-toast"
              onClick={() => setError(null)}
            >
              ✕
            </span>
          </div>
        )}

        {/* Input Container */}
        <div className="input-container">
          <div className="chat-input-wrapper">
            <textarea
              className="chat-input"
              value={inputValue}
              onChange={(e) => setInputValue(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="Message..."
              disabled={loading}
              rows="1"
            />
            <button
              className="send-btn"
              onClick={handleSendMessage}
              disabled={loading || !inputValue.trim()}
              title={loading ? 'Processing...' : 'Send message'}
            >
              {loading ? (
                <div className="ds-spinner"></div>
              ) : (
                <span>↑</span>
              )}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
