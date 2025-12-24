import { useState, useRef } from 'react';
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

  /* -----------------------------
     File Validation
  ------------------------------ */
  const validateAndSetFile = (selectedFile) => {
    setError(null);

    if (!selectedFile.type.includes('pdf')) {
      setError('Please upload a PDF file');
      return;
    }

    const maxSize = 50 * 1024 * 1024; // 50MB
    if (selectedFile.size > maxSize) {
      setError('File size must be less than 50MB');
      return;
    }

    setFile(selectedFile);
  };

  /* -----------------------------
     Drag & Drop Handlers
  ------------------------------ */
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
    const droppedFile = e.dataTransfer.files?.[0];
    if (droppedFile) {
      validateAndSetFile(droppedFile);
    }
  };

  /* -----------------------------
     File Selection
  ------------------------------ */
  const handleFileChange = (e) => {
    const selectedFile = e.target.files?.[0];
    if (selectedFile) {
      validateAndSetFile(selectedFile);
    }
  };

  /* -----------------------------
     Upload Submit
  ------------------------------ */
  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!file) {
      setError('Please select a file');
      return;
    }

    setLoading(true);
    setError(null);
    setResponse(null);

    try {
      const result = await uploadDocument(file, query);
      const data = result.data;

      setResponse({
        sessionId: data.sessionId,
        documentId: data.documentId,
        documentName: file.name,
        query: data.query,
        response: data.response,
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
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  /* -----------------------------
     Reset
  ------------------------------ */
  const handleReset = () => {
    setFile(null);
    setQuery('');
    setError(null);
    setResponse(null);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  /* -----------------------------
     Render
  ------------------------------ */
  return (
    <div className="upload-page ds-page">
      <div className="upload-card ds-card">

        {/* Header */}
        <div className="ds-card-header">
          <div>
            <h1 className="ds-card-title">📄 Upload Document</h1>
            <p className="ds-card-subtitle">Upload a PDF to start chatting with AI</p>
          </div>
          {onCancel && (
            <button className="ds-btn ds-btn-ghost" onClick={onCancel} style={{ marginTop: 'var(--spacing-md)' }}>
              ← Back to Sessions
            </button>
          )}
        </div>

        {/* Show Success Response */}
        {response ? (
          <div className="upload-success">
            <div className="success-icon">✨</div>
            <h3 className="success-title">Document Uploaded Successfully!</h3>
            <p className="success-message">{response.response}</p>

            <div className="success-actions">
              <button
                className="ds-btn ds-btn-primary ds-btn-lg"
                onClick={() => onUploadComplete?.(response)}
                style={{ width: '100%' }}
              >
                💬 Start Chatting
              </button>

              <button
                className="ds-btn ds-btn-secondary"
                onClick={handleReset}
                style={{ width: '100%' }}
              >
                📄 Upload Another Document
              </button>
            </div>
          </div>
        ) : (
          /* Upload Form */
          <form onSubmit={handleSubmit} className="upload-form">

            {/* Drop Zone */}
            <div
              className={`upload-dropzone ${dragOver ? 'drag-over' : ''} ${file ? 'has-file' : ''}`}
              onDragEnter={handleDragEnter}
              onDragLeave={handleDragLeave}
              onDragOver={handleDrag}
              onDrop={handleDrop}
              onClick={() => fileInputRef.current?.click()}
            >
              <input
                ref={fileInputRef}
                type="file"
                className="upload-input"
                accept=".pdf"
                onChange={handleFileChange}
              />

              {file ? (
                <div className="file-selected">
                  <div className="file-icon">📄</div>
                  <div className="file-info">
                    <div className="file-name">{file.name}</div>
                    <div className="file-size">
                      {(file.size / 1024 / 1024).toFixed(2)} MB
                    </div>
                  </div>
                  <div className="file-check">✓</div>
                </div>
              ) : (
                <div className="dropzone-empty">
                  <div className="dropzone-icon">📁</div>
                  <div className="dropzone-text">
                    <strong>Drop PDF here</strong> or click to browse
                  </div>
                  <div className="dropzone-hint">Maximum file size: 50MB</div>
                </div>
              )}
            </div>

            {/* Query Input */}
            <div className="ds-input-group">
              <label className="ds-label">
                Question or Query (Optional)
              </label>
              <textarea
                className="ds-input upload-textarea"
                placeholder="Ask a specific question about the document..."
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                rows="4"
                disabled={loading}
              />
            </div>

            {/* Error Message */}
            {error && (
              <div className="upload-error">
                <span className="error-icon">⚠️</span>
                <span className="error-text">{error}</span>
              </div>
            )}

            {/* Action Buttons */}
            <div className="upload-actions">
              <button
                type="submit"
                className="ds-btn ds-btn-primary ds-btn-lg"
                disabled={!file || loading}
              >
                {loading ? (
                  <>
                    <div className="ds-spinner"></div>
                    <span>Processing...</span>
                  </>
                ) : (
                  <>
                    <span>🚀</span>
                    <span>Upload & Process</span>
                  </>
                )}
              </button>

              {(file || query) && !loading && (
                <button
                  type="button"
                  className="ds-btn ds-btn-secondary"
                  onClick={handleReset}
                >
                  <span>🔄</span>
                  <span>Reset</span>
                </button>
              )}
            </div>
          </form>
        )}
      </div>
    </div>
  );
}
