import { useState } from 'react'
import toast from 'react-hot-toast'
import api, { getApiError, saveAuthTokens } from '../api/client'

const LoginPage = ({ onLogin }) => {
  const [isSignup, setIsSignup] = useState(false)
  const [needsVerification, setNeedsVerification] = useState(false)
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [phoneNumber, setPhoneNumber] = useState('')
  const [password, setPassword] = useState('')
  const [otp, setOtp] = useState('')
  const [loading, setLoading] = useState(false)

  const submit = async (event) => {
    event.preventDefault()
    setLoading(true)
    try {
      if (isSignup && !needsVerification) {
        await api.post('/auth/register', { name, email, phoneNumber, password })
        setNeedsVerification(true)
        toast.success('Account created. Enter the verification code.')
      } else if (isSignup) {
        await api.post('/auth/verify-email', { email, otp })
        toast.success('Email verified. You can now sign in.')
        setIsSignup(false)
        setNeedsVerification(false)
        setOtp('')
      } else {
        const { data } = await api.post('/auth/login', { email, password })
        saveAuthTokens(data)
        onLogin()
      }
    } catch (error) {
      toast.error(getApiError(error, 'Login failed'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="flex min-h-screen items-center justify-center bg-gradient-to-br from-pink-50 via-purple-50 to-blue-50 p-6">
      <form onSubmit={submit} className="w-full max-w-md rounded-3xl border-2 border-white/60 bg-white/70 p-8 shadow-2xl backdrop-blur-xl">
        <h1 className="mb-2 text-3xl font-bold text-slate-800">Library System</h1>
        <p className="mb-6 text-sm text-slate-600">
          {isSignup ? 'Create an account to access the library.' : 'Sign in to view the dashboard.'}
        </p>
        {isSignup && !needsVerification && (
          <label className="mb-4 block text-sm font-semibold text-slate-700">
            Name
            <input
              value={name}
              onChange={(event) => setName(event.target.value)}
              required
              className="mt-2 w-full rounded-xl border border-slate-300 bg-white px-4 py-3 outline-none focus:border-purple-400 focus:ring-2 focus:ring-purple-200"
            />
          </label>
        )}
        <label className="mb-4 block text-sm font-semibold text-slate-700">
          Email
          <input
            type="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            required
            className="mt-2 w-full rounded-xl border border-slate-300 bg-white px-4 py-3 outline-none focus:border-purple-400 focus:ring-2 focus:ring-purple-200"
          />
        </label>
        {isSignup && !needsVerification && (
          <label className="mb-4 block text-sm font-semibold text-slate-700">
            Phone number (optional)
            <input
              value={phoneNumber}
              onChange={(event) => setPhoneNumber(event.target.value)}
              className="mt-2 w-full rounded-xl border border-slate-300 bg-white px-4 py-3 outline-none focus:border-purple-400 focus:ring-2 focus:ring-purple-200"
            />
          </label>
        )}
        <label className="mb-6 block text-sm font-semibold text-slate-700">
          Password
          <input
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            required
            className="mt-2 w-full rounded-xl border border-slate-300 bg-white px-4 py-3 outline-none focus:border-purple-400 focus:ring-2 focus:ring-purple-200"
          />
        </label>
        {isSignup && needsVerification && (
          <label className="mb-6 block text-sm font-semibold text-slate-700">
            Verification code
            <input
              value={otp}
              onChange={(event) => setOtp(event.target.value)}
              inputMode="numeric"
              required
              className="mt-2 w-full rounded-xl border border-slate-300 bg-white px-4 py-3 outline-none focus:border-purple-400 focus:ring-2 focus:ring-purple-200"
            />
            <span className="mt-2 block text-xs font-normal text-slate-500">The code is sent by email or logged by the backend in development.</span>
          </label>
        )}
        <button
          type="submit"
          disabled={loading}
          className="w-full rounded-xl bg-gradient-to-r from-pink-400 to-purple-500 px-4 py-3 font-bold text-white shadow-lg transition hover:shadow-xl disabled:cursor-not-allowed disabled:opacity-60"
        >
          {loading ? 'Please wait...' : needsVerification ? 'Verify email' : isSignup ? 'Create account' : 'Sign in'}
        </button>
        <button
          type="button"
          onClick={() => {
            setIsSignup((value) => !value)
            setNeedsVerification(false)
          }}
          className="mt-4 w-full text-sm font-semibold text-purple-600 hover:text-purple-800"
        >
          {isSignup ? 'Already have an account? Sign in' : 'Need an account? Sign up'}
        </button>
      </form>
    </main>
  )
}

export default LoginPage
