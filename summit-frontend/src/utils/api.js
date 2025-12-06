import axios from 'axios';

// Set base URL for API calls
const BACKEND_URL =
  process.env.NODE_ENV === "production"
    ? "http://ec2-3-216-78-179.compute-1.amazonaws.com:8080"
    : "http://localhost:8080";

axios.defaults.baseURL = BACKEND_URL;


// Add request interceptor to include auth token
axios.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Add response interceptor to handle auth errors
axios.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token expired or invalid
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default axios;
