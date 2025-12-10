import axios from 'axios';

const API_BASE_URL = import.meta.env.MODE === 'development' ? 'http://localhost:8080' : '';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 600000, // 10 minutes (600000ms) - LLM processing can take a long time
  headers: {
    'Content-Type': 'multipart/form-data',
  },
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
