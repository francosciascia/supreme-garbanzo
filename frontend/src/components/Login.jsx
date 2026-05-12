import { useState } from "react"
import axios from "axios"

import { LoginForm } from "./login-form"

export default function Login({ onLoginSuccess }) {
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState("")

  const handleSubmit = async (e) => {
    e.preventDefault()

    setLoading(true)
    setError("")

    try {
      const response = await axios.post(
        "http://localhost:8083/api/auth/login",
        {
          email,
          contraseña: password,
        }
      )

      const data = response.data

      localStorage.setItem("token", data.token)
      localStorage.setItem("user", JSON.stringify(data))

      onLoginSuccess(data)
    } catch (err) {
      console.error(err)

      setError(
        err.response?.data?.message ||
        "Credenciales inválidas"
      )
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-black p-6">
      <div className="w-full max-w-4xl">
        <LoginForm
          onSubmit={handleSubmit}
          email={email}
          password={password}
          setEmail={setEmail}
          setPassword={setPassword}
          loading={loading}
          error={error}
        />
      </div>
    </div>
  )
}