import axios from 'axios';

const api = axios.create({
  baseURL: '/', // Since it's served from the same server
  headers: {
    'Content-Type': 'application/json',
  },
});

export default api;
