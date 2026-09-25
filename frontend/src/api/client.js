import axios from 'axios'

const apiBaseUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

const api = axios.create({
  baseURL: apiBaseUrl,
  headers: {
    'Content-Type': 'application/json',
  },
})

api.interceptors.request.use((config) => {
  const accessToken = localStorage.getItem('library_access_token')
  if (accessToken) config.headers.Authorization = `Bearer ${accessToken}`
  return config
})

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config
    const refreshToken = localStorage.getItem('library_refresh_token')
    if (error.response?.status !== 401 || !refreshToken || originalRequest?._retry || originalRequest?.url?.includes('/auth/')) {
      return Promise.reject(error)
    }

    originalRequest._retry = true
    try {
      const { data } = await axios.post(`${apiBaseUrl}/auth/refresh`, { refreshToken })
      localStorage.setItem('library_access_token', data.accessToken)
      localStorage.setItem('library_refresh_token', data.refreshToken)
      originalRequest.headers.Authorization = `Bearer ${data.accessToken}`
      return api(originalRequest)
    } catch (refreshError) {
      localStorage.removeItem('library_access_token')
      localStorage.removeItem('library_refresh_token')
      return Promise.reject(refreshError)
    }
  },
)

export const getApiError = (error, fallback = 'Something went wrong') => {
  return error?.response?.data?.error || fallback
}

export const saveAuthTokens = ({ accessToken, refreshToken }) => {
  localStorage.setItem('library_access_token', accessToken)
  localStorage.setItem('library_refresh_token', refreshToken)
}

export const clearAuthTokens = () => {
  localStorage.removeItem('library_access_token')
  localStorage.removeItem('library_refresh_token')
}

export default api
