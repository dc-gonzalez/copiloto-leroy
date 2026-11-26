import { useRef, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import {
  ArrowRight, Camera, ChevronRight, Droplet, Headset, Layers, LayoutGrid, PaintRoller, Search, ShowerHead, SprayCan,
} from 'lucide-react'
import { reduzirImagem } from '../api'
import { Erro, LojaAtual, Marca } from '../components/Comum'

const PROJETOS = [
  { Icone: Droplet, cor: '#1E88E5', titulo: 'Torneira pingando', texto: 'Minha torneira está pingando' },
  { Icone: Layers, cor: '#8D6E63', titulo: 'Instalar prateleira', texto: 'Quero instalar uma prateleira' },
  { Icone: ShowerHead, cor: '#00897B', titulo: 'Trocar chuveiro', texto: 'Preciso trocar o chuveiro' },
  { Icone: PaintRoller, cor: '#8E24AA', titulo: 'Pintar um cômodo', texto: 'Vou pintar um quarto' },
  { Icone: LayoutGrid, cor: '#6D4C41', titulo: 'Trocar o piso', texto: 'Quero trocar o piso da sala' },
  { Icone: SprayCan, cor: '#546E7A', titulo: 'Mofo na parede', texto: 'Tem mofo na parede do banheiro' },
]

export default function Inicio() {
  const navegar = useNavigate()
  const [texto, setTexto] = useState('')
  const [erro, setErro] = useState('')
  const arquivoRef = useRef(null)

  const escolherFoto = async (e) => {
    const arquivo = e.target.files?.[0]
    e.target.value = ''
    if (!arquivo) return
    try {
      navegar('/assistente', { state: { foto: await reduzirImagem(arquivo), mensagemFoto: texto.trim() } })
    } catch {
      setErro('Não consegui ler essa imagem. Tente outra foto.')
    }
  }

  const comecar = (mensagem) => {
    if (!mensagem.trim()) return
    navegar('/assistente', { state: { mensagemInicial: mensagem.trim() } })
  }

  return (
    <div className="inicio">
      <header className="inicio-topo">
        <Marca />
        <LojaAtual />
      </header>

      <section className="inicio-principal">
        <h1>O que você quer resolver hoje?</h1>
        <p>Conte com suas palavras. Eu separo os produtos certos e mostro o caminho mais curto na loja.</p>
        <form
          className="campo-busca grande"
          onSubmit={(e) => {
            e.preventDefault()
            comecar(texto)
          }}
        >
          <Search size={20} className="campo-icone" aria-hidden="true" />
          <input
            value={texto}
            onChange={(e) => setTexto(e.target.value)}
            placeholder="Ex.: meu chuveiro parou de esquentar"
            aria-label="Descreva o que você precisa"
            maxLength={500}
            enterKeyHint="send"
          />
          <button
            type="button"
            className="botao-icone"
            onClick={() => arquivoRef.current?.click()}
            aria-label="Tirar ou escolher uma foto do problema"
            title="Tirar ou escolher uma foto"
          >
            <Camera size={20} />
          </button>
          <input ref={arquivoRef} type="file" accept="image/*" hidden onChange={escolherFoto} />
          <button className="botao" type="submit" disabled={!texto.trim()} aria-label="Começar">
            <ArrowRight size={20} />
          </button>
        </form>
        <Erro mensagem={erro} />
      </section>

      <section className="inicio-secao">
        <h2 className="rotulo">Projetos mais comuns</h2>
        <div className="grade-projetos">
          {PROJETOS.map(({ Icone, cor, titulo, texto: mensagem }) => (
            <button key={titulo} className="projeto-atalho" onClick={() => comecar(mensagem)}>
              <span className="selo-secao" style={{ background: `${cor}1a`, color: cor }}>
                <Icone size={22} strokeWidth={1.75} />
              </span>
              <span>{titulo}</span>
            </button>
          ))}
        </div>
      </section>

      <section className="inicio-secao">
        <h2 className="rotulo">Outras formas de ajuda</h2>
        <div className="lista-acoes">
          <Link to="/buscar" className="linha-acao">
            <span className="selo-secao neutro">
              <Search size={20} strokeWidth={1.75} />
            </span>
            <span className="linha-acao-texto">
              <strong>Já sei o produto</strong>
              <small>Veja o corredor e a prateleira</small>
            </span>
            <ChevronRight size={20} className="seta" />
          </Link>
          <Link to="/chamar" className="linha-acao">
            <span className="selo-secao neutro">
              <Headset size={20} strokeWidth={1.75} />
            </span>
            <span className="linha-acao-texto">
              <strong>Falar com um vendedor</strong>
              <small>Pegue uma senha e acompanhe a fila pelo celular</small>
            </span>
            <ChevronRight size={20} className="seta" />
          </Link>
        </div>
      </section>

      <footer className="rodape">
        <Link to="/vendedor">Área do vendedor</Link>
        <span>Protótipo acadêmico · FIAP Grupo 105</span>
      </footer>
    </div>
  )
}
