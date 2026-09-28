import { useState } from 'react'
import { Toaster } from 'react-hot-toast'
import Layout from './components/Layout'
import DashboardPage from './pages/DashboardPage'
import BooksPage from './pages/BooksPage'
import UsersPage from './pages/UsersPage'
import IssueReturnPage from './pages/IssueReturnPage'
import LoginPage from './pages/LoginPage'

function App() {
  const [authenticated, setAuthenticated] = useState(() => Boolean(localStorage.getItem('library_access_token')))
  const [activePage, setActivePage] = useState('dashboard')

  const renderPage = () => {
    if (activePage === 'books') return <BooksPage />
    if (activePage === 'users') return <UsersPage />
    if (activePage === 'issue-return') return <IssueReturnPage />
    return <DashboardPage />
  }

  return (
    <>
      <Toaster position="top-right" />
      {authenticated ? (
        <Layout
          activePage={activePage}
          onPageChange={setActivePage}
          onLogout={() => {
            localStorage.removeItem('library_access_token')
            localStorage.removeItem('library_refresh_token')
            setAuthenticated(false)
          }}
        >
          {renderPage()}
        </Layout>
      ) : <LoginPage onLogin={() => setAuthenticated(true)} />}
    </>
  )
}

export default App
