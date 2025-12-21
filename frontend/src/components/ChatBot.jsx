import React, { useState, useEffect, useRef } from 'react';
import './ChatBot.css';
import { getChatSession } from '../api/documentApi';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export default function ChatBot({ sessionId, documentName, onUploadNew }) {
  const [messages, setMessages] = useState([]);
  const [inputValue, setInputValue] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [connected, setConnected] = useState(false);
  const [streamingMessage, setStreamingMessage] = useState(null); // For accumulating streaming chunks
  const messagesEndRef = useRef(null);
  const stompClientRef = useRef(null);

  // Load chat history on mount
  useEffect(() => {
    loadChatHistory();
  }, [sessionId]);

  // Setup WebSocket connection
  useEffect(() => {
    // Use environment variable if available, otherwise construct URL from current location
    const wsUrl = import.meta.env.VITE_WS_URL || `${window.location.protocol}//${window.location.host}/ws`;

    const client = new Client({
      webSocketFactory: () => new SockJS(wsUrl),
      debug: (str) => {
        console.log('STOMP: ' + str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        console.log('WebSocket Connected');
        setConnected(true);

        // Subscribe to messages for this session
        client.subscribe(`/topic/chat/${sessionId}`, (message) => {
          const receivedMessage = JSON.parse(message.body);
          console.log('Received message:', receivedMessage);

          handleStreamingMessage(receivedMessage);
        });
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame);
        setError('WebSocket connection error');
        setConnected(false);
      },
      onWebSocketClose: () => {
        console.log('WebSocket Disconnected');
        setConnected(false);
      },
    });

    client.activate();
    stompClientRef.current = client;

    // Cleanup on unmount
    return () => {
      if (client) {
        client.deactivate();
      }
    };
  }, [sessionId]);

  // Scroll to bottom when messages change
  useEffect(() => {
    scrollToBottom();
  }, [messages, streamingMessage]);

  const handleStreamingMessage = (receivedMessage) => {
    const { type, role, content, messageId } = receivedMessage;

    switch (type) {
      case 'message':
        // Regular user message
        if (role === 'user') {
          setMessages((prev) => [...prev, { role: 'user', content }]);
        }
        break;

      case 'start':
        // Start of streaming response
        setLoading(true);
        setStreamingMessage({ role: 'assistant', content: '', messageId });
        break;

      case 'chunk':
        // Accumulate streaming chunks
        setStreamingMessage((prev) => {
          if (prev && prev.messageId === messageId) {
            return { ...prev, content: prev.content + content };
          }
          return { role: 'assistant', content, messageId };
        });
        break;

      case 'end':
        // End of streaming - add complete message to history
        setLoading(false);
        setMessages((prev) => [...prev, { role: 'assistant', content }]);
        setStreamingMessage(null);
        break;

      case 'error':
        // Error message
        setError(`❌ ${content}`);
        setLoading(false);
        setStreamingMessage(null);
        break;

      default:
        // Legacy format for backward compatibility
        if (role === 'error') {
          setError(`❌ ${content}`);
          setLoading(false);
        } else {
          setMessages((prev) => [...prev, receivedMessage]);
          if (role === 'assistant') {
            setLoading(false);
          }
        }
    }
  };

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

    if (!connected) {
      setError('❌ WebSocket not connected. Please wait...');
      return;
    }

    const userMessage = inputValue.trim();
    setInputValue('');
    setError(null);
    setLoading(true);

    try {
      // Send message via WebSocket
      stompClientRef.current.publish({
        destination: '/app/chat/message',
        body: JSON.stringify({
          sessionId: sessionId,
          question: userMessage,
        }),
      });
    } catch (err) {
      const errorMessage = err.message || 'Failed to send message. Please try again.';
      setError(`❌ ${errorMessage}`);
      console.error('Chat error:', err);
      setLoading(false);
    }
  };

  const handleKeyPress = (e) => {
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
            <h2>💬 Chat with Document</h2>
            <div className="document-info">
              📄 {documentName}
              <span className={`connection-status ${connected ? 'connected' : 'disconnected'}`}>
                {connected ? '🟢' : '🔴'}
              </span>
            </div>
          </div>
          <button className="upload-new-btn" onClick={onUploadNew}>
            📤 Upload New
          </button>
        </div>

        {/* Messages Container */}
        <div className="messages-container">
          {messages.length === 0 && !streamingMessage ? (
            <div className="empty-state">
              <h3>👋 Start a Conversation</h3>
              <p>Ask any questions about the document below</p>
            </div>
          ) : (
            <>
              {messages.map((msg, idx) => (
                <div key={idx} className={`message ${msg.role}`}>
                  <div className="message-avatar">
                    {msg.role === 'user' ? '👤' : '🤖'}
                  </div>
                  <div className="message-content">{msg.content}</div>
                </div>
              ))}

              {/* Streaming message - shown in real-time */}
              {streamingMessage && (
                <div className="message assistant streaming">
                  <div className="message-avatar">🤖</div>
                  <div className="message-content">
                    {streamingMessage.content}
                    <span className="streaming-cursor">▊</span>
                  </div>
                </div>
              )}
            </>
          )}

          {loading && !streamingMessage && (
            <div className="message assistant">
              <div className="message-avatar">🤖</div>
              <div className="typing-indicator">
                <div className="typing-dot"></div>
                <div className="typing-dot"></div>
                <div className="typing-dot"></div>
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
              onKeyPress={handleKeyPress}
              placeholder="Ask a question about the document..."
              disabled={loading || !connected}
              rows="1"
            />
            <button
              className="send-btn"
              onClick={handleSendMessage}
              disabled={loading || !inputValue.trim() || !connected}
              title={loading ? 'Processing...' : !connected ? 'Connecting...' : 'Send message'}
            >
              {loading ? (
                <div className="spinner-small"></div>
              ) : (
                <span>📤</span>
              )}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

