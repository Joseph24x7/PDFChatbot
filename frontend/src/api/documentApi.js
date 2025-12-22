import axios from 'axios';

const API_BASE_URL = import.meta.env.MODE === 'development' ? 'http://localhost:8080' : '';

// Base API client without conflicting default headers
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 600000, // 10 minutes (600000ms) - LLM processing can take a long time
});

export const uploadDocument = (file, query) => {
  const formData = new FormData();
  formData.append('file', file);
  if (query) {
    formData.append('query', query);
  }

  return apiClient.post('/api/v1/documents/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
};

export const sendChatMessage = (sessionId, question) => {
  return apiClient.post('/api/v1/chat/message', {
    sessionId,
    question,
  }, {
    headers: {
      'Content-Type': 'application/json',
    },
  });
};

export const getChatSession = (sessionId) => {
  return apiClient.get(`/api/v1/chat/${sessionId}`, {
    headers: {
      'Content-Type': 'application/json',
    },
  });
};

export const getAllSessions = () => {
  return apiClient.get('/api/v1/chat/sessions', {
    headers: {
      'Content-Type': 'application/json',
    },
  });
};

export default apiClient;

