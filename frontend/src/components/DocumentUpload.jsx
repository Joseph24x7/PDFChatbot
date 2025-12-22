import React, { useState, useRef } from 'react';
import './DocumentUpload.css';
import { uploadDocument } from '../api/documentApi';

export default function DocumentUpload({ onUploadComplete, onCancel }) {
  const [file, setFile] = useState(null);
  const [query, setQuery] = useState('');
  const [loading, setLoading] = useState(false);
  const [response, setResponse] = useState(null);
  const [error, setError] = useState(null);
  const [dragOver, setDragOver] = useState(false);
  const fileInputRef = useRef(null);

  const handleFileChange = (e) => {
    const selectedFile = e.target.files?.[0];
    if (selectedFile) {
      validateAndSetFile(selectedFile);
    }
  };

  const validateAndSetFile = (selectedFile) => {
    setError(null);

    if (!selectedFile.type.includes('pdf')) {
      setError('❌ Please upload a PDF file');
      return;
    }

    const maxSize = 50 * 1024 * 1024; // 50MB
    if (selectedFile.size > maxSize) {
      setError('❌ File size must be less than 50MB');
      return;
    }

    setFile(selectedFile);
  };

  const handleDrag = (e) => {
    e.preventDefault();
    e.stopPropagation();
  };

  const handleDragEnter = (e) => {
    handleDrag(e);
    setDragOver(true);
  };

  const handleDragLeave = (e) => {
    handleDrag(e);
    setDragOver(false);
  };

  const handleDrop = (e) => {
    handleDrag(e);
    setDragOver(false);

    const droppedFiles = e.dataTransfer.files;
    if (droppedFiles.length > 0) {
      validateAndSetFile(droppedFiles[0]);
    }
  };

  const handleUploadClick = () => {
    fileInputRef.current?.click();
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!file) {
      setError('❌ Please select a file');
      return;
    }

    setLoading(true);
    setError(null);
    setResponse(null);

    try {
      const result = await uploadDocument(file, query);
      const uploadData = result.data;

      // Store response with session ID for chatbot
      setResponse({
        sessionId: uploadData.sessionId,
        documentId: uploadData.documentId,
        documentName: file.name,
        query: uploadData.query,
        response: uploadData.response,
      });

      setFile(null);
      setQuery('');
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    } catch (err) {
      const errorMessage =
        err.response?.data?.message ||
        err.message ||
        'Failed to upload document. Please try again.';
      setError(`❌ ${errorMessage}`);
      console.error('Upload error:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleReset = () => {
    setFile(null);
    setQuery('');
    setError(null);
    setResponse(null);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  return (
    <div className="file-upload-container">
      <div className="card">
        <div className="header">
          <div>
            <h1>📄 Document Summary</h1>
            <p>Upload a PDF document to get AI-powered summaries and answers</p>
          </div>
          {onCancel && (
            <button type="button" className="cancel-btn" onClick={onCancel}>
              ← Back to Sessions
            </button>
          )}
        </div>

        <form onSubmit={handleSubmit}>
          {/* File Upload Area */}
          <div className="form-group">
            <label htmlFor="file-input">Select PDF Document</label>
            <div
              className={`upload-area ${dragOver ? 'drag-over' : ''}`}
              onDragEnter={handleDragEnter}
              onDragLeave={handleDragLeave}
              onDragOver={handleDrag}
              onDrop={handleDrop}
              onClick={handleUploadClick}
              role="button"
              tabIndex="0"
            >
              <input
                ref={fileInputRef}
                type="file"
                id="file-input"
                className="file-input"
                accept=".pdf"
                onChange={handleFileChange}
              />
              <div className="upload-icon">📁</div>
              <div className="upload-text">
                <h3>Drop your PDF here or click to browse</h3>
                <p>Maximum file size: 50MB</p>
              </div>
            </div>
            {file && <div className="file-name">✓ Selected: {file.name}</div>}
          </div>

          {/* Query Input */}
          <div className="form-group">
            <label htmlFor="query">
              Question or Query (Optional)
            </label>
            <textarea
              id="query"
              className="query-input"
              placeholder="Ask a specific question about the document... (optional)"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              rows="3"
              style={{ resize: 'vertical' }}
            />
          </div>

          {/* Error Message */}
          {error && <div className="error-message">{error}</div>}

          {/* Action Buttons */}
          <div className="button-group">
            <button
              type="submit"
              className="btn btn-primary"
              disabled={!file || loading}
            >
              {loading ? (
                <>
                  <div className="spinner"></div>
                  <span>Processing...</span>
                </>
              ) : (
                <>
                  <span>🚀</span>
                  <span>Upload & Process</span>
                </>
              )}
            </button>
            {(file || query) && (
              <button
                type="button"
                className="btn btn-secondary"
                onClick={handleReset}
                disabled={loading}
              >
                🔄 Reset
              </button>
            )}
          </div>
        </form>

        {/* Response Display - Initial Message Before Chat */}
        {response && (
          <div className="response-container">
            <div className="response-header">
              <h3>✨ Document Loaded Successfully!</h3>
              <span className="success-badge">Ready</span>
            </div>

            <div className="response-content">
              {response.query && (
                <div className="response-section">
                  <div className="response-label">📋 Initial Question:</div>
                  <div className="response-text">{response.query}</div>
                </div>
              )}

              <div className="response-section">
                <div className="response-label">🤖 Initial Response:</div>
                <div className="response-text">{response.response}</div>
              </div>
            </div>

            <button
              type="button"
              className="btn btn-primary"
              onClick={() => {
                if (onUploadComplete) {
                  onUploadComplete(response);
                }
              }}
              style={{ marginTop: '20px', width: '100%' }}
            >
              💬 Continue Chat
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

