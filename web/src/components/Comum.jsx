import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { ChevronLeft, MapPin, RefreshCw, TriangleAlert, X } from 'lucide-react'
import { api, reais } from '../api'
import { ICONE_ATENDIMENTO, SeloSecao } from './Icones'

let mapaEmCache = null

/** The store plan never changes during a visit, so it is fetched once per session. */
export function useMapa() {
  const [mapa, setMapa] = useState(mapaEmCache)
  useEffect(() => {
    if (!mapaEmCache) {
      api.mapa().then((m) => {
        mapaEmCache = m
        setMapa(m)
      }).catch(() => {})
    }
  }, [])
  return mapa
}

export function Marca({ pequena }) {
  return (
    <span className={pequena ? 'marca pequena' : 'marca'}>
      <svg viewBox="0 0 32 32" className="marca-simbolo" aria-hidden="true">
        <rect width="32" height="32" rx="6" fill="#78BE20" />
        <path d="M7 23V14.5L16 8l9 6.5V23" fill="none" stroke="#fff" strokeWidth="2.6" strokeLinejoin="round" />
        <path d="M12.5 23v-5h7v5" fill="none" stroke="#fff" strokeWidth="2.6" strokeLinejoin="round" />
      </svg>
      <span className="marca-texto">
        <strong>Copiloto</strong>
        {!pequena && <small>Leroy Merlin</small>}
      </span>
    </span>
  )
}

export function Topo({ titulo, subtitulo, voltar }) {
  const navegar = useNavigate()
  return (
    <header className="topo">
      {voltar ? (
        <button className="topo-voltar" onClick={() => navegar(voltar === true ? -1 : voltar)} aria-label="Voltar">
          <ChevronLeft size={24} />
        </button>
      ) : (
        <Link to="/" className="topo-voltar" aria-label="Início">
          <Marca pequena />
        </Link>
      )}
      <div className="topo-titulo">
        <strong>{titulo}</strong>
        {subtitulo && <small>{subtitulo}</small>}
      </div>
    </header>
  )
}

export function LojaAtual() {
  return (
    <span className="loja-atual">
      <MapPin size={14} strokeWidth={2} />
      Loja Demonstração
    </span>
  )
}

export function Localizacao({ produto }) {
  return (
    <span className="localizacao" style={{ '--cor': produto.corSecao }}>
      Corredor {produto.corredor} · Prateleira {produto.prateleira}
    </span>
  )
}

export function ProdutoLinha({ produto, children, extra, quantidade }) {
  const IconeAtendimento = ICONE_ATENDIMENTO[produto.tipoAtendimento]
  return (
    <div className="produto">
      <SeloSecao secao={produto.secao} cor={produto.corSecao} />
      <div className="produto-corpo">
        <div className="produto-topo">
          <div className="produto-nome">{produto.nome}</div>
          <div className="produto-preco">
            {quantidade > 1 && <small>{quantidade} ×</small>}
            {reais(produto.preco)}
          </div>
        </div>
        <div className="produto-meta">
          {produto.marca}
          {produto.marcaPropria && <span className="tag tag-marca">Marca Leroy</span>}
          {produto.disponibilidade === 'MOSTRUARIO' && <span className="tag tag-neutra">Mostruário · entrega em casa</span>}
        </div>
        {IconeAtendimento && (
          <div className="produto-atendimento">
            <IconeAtendimento size={14} strokeWidth={2} />
            {produto.atendimentoDescricao}
          </div>
        )}
        {extra}
        {children && <div className="produto-acoes">{children}</div>}
      </div>
    </div>
  )
}

export function Modal({ titulo, aberto, aoFechar, children }) {
  useEffect(() => {
    if (!aberto) return undefined
    const fechar = (e) => e.key === 'Escape' && aoFechar?.()
    window.addEventListener('keydown', fechar)
    return () => window.removeEventListener('keydown', fechar)
  }, [aberto, aoFechar])
  if (!aberto) return null
  return (
    <div className="modal-fundo" onClick={aoFechar}>
      <div className="modal" role="dialog" aria-modal="true" aria-label={titulo} onClick={(e) => e.stopPropagation()}>
        <div className="modal-topo">
          <h2>{titulo}</h2>
          {aoFechar && (
            <button className="botao-icone" onClick={aoFechar} aria-label="Fechar">
              <X size={20} />
            </button>
          )}
        </div>
        {children}
      </div>
    </div>
  )
}

export function Confirmar({ aberto, titulo, texto, confirmar = 'Confirmar', cancelar = 'Voltar', perigo, aoConfirmar, aoCancelar }) {
  return (
    <Modal titulo={titulo} aberto={aberto} aoFechar={aoCancelar}>
      <p className="modal-texto">{texto}</p>
      <div className="botoes-lado">
        <button className="botao secundario" onClick={aoCancelar}>
          {cancelar}
        </button>
        <button className={perigo ? 'botao perigo' : 'botao'} onClick={aoConfirmar}>
          {confirmar}
        </button>
      </div>
    </Modal>
  )
}

export function Erro({ mensagem, tentar }) {
  if (!mensagem) return null
  return (
    <div className="erro" role="alert">
      <TriangleAlert size={18} />
      <span>{mensagem}</span>
      {tentar && (
        <button className="link" onClick={tentar}>
          <RefreshCw size={14} /> Tentar de novo
        </button>
      )}
    </div>
  )
}

export function Carregando({ texto = 'Carregando…' }) {
  return (
    <div className="carregando">
      <span className="spinner" /> {texto}
    </div>
  )
}
