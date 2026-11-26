import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import Assistente from './pages/Assistente'
import Buscar from './pages/Buscar'
import Chamado from './pages/Chamado'
import Chamar from './pages/Chamar'
import Inicio from './pages/Inicio'
import Projeto from './pages/Projeto'
import Vendedor from './pages/Vendedor'

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Inicio />} />
        <Route path="/assistente" element={<Assistente />} />
        <Route path="/projeto/:id" element={<Projeto />} />
        <Route path="/buscar" element={<Buscar />} />
        <Route path="/chamar" element={<Chamar />} />
        <Route path="/chamado/:id" element={<Chamado />} />
        <Route path="/vendedor" element={<Vendedor />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  )
}
