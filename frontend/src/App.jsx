import { useState } from 'react';
import SessionList from './components/SessionList';
import DocumentUpload from './components/DocumentUpload';
import ChatBot from './components/ChatBot';
import './App.css';

function App() {
  const [view, setView] = useState('sessions'); // 'sessions', 'upload', 'chat'
  const [chatSession, setChatSession] = useState(null);

  const handleSelectSession = (session) => {
    // Navigate to chat with selected session
    setChatSession({
      sessionId: session.sessionId || session.id,
      documentId: session.documentId,
      documentName: session.documentName || session.fileName,
    });
    setView('chat');
  };

  const handleUploadNew = () => {
    // Navigate to upload screen
    setView('upload');
  };

  const handleUploadComplete = (uploadResponse) => {
    // Store session data when upload is complete and navigate to chat
    if (uploadResponse && uploadResponse.sessionId) {
      setChatSession({
        sessionId: uploadResponse.sessionId,
        documentId: uploadResponse.documentId,
        documentName: uploadResponse.documentName,
        initialResponse: uploadResponse.response,
      });
      setView('chat');
    }
  };

  const handleReset = () => {
    // Clear session and return to initial view (session list)
    setChatSession(null);
    setView('sessions');
  };

  return (
    <div className="app">
      {view === 'sessions' && (
        <SessionList
          onSelectSession={handleSelectSession}
          onUploadNew={handleUploadNew}
        />
      )}

      {view === 'upload' && (
        <DocumentUpload
          onUploadComplete={handleUploadComplete}
          onCancel={handleReset}
        />
      )}

      {view === 'chat' && chatSession && (
        <ChatBot
          sessionId={chatSession.sessionId}
          documentName={chatSession.documentName}
          onReset={handleReset}
        />
      )}
    </div>
  );
}

export default App;

