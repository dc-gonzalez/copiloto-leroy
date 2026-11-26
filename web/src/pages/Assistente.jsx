import { useCallback, useEffect, useRef, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { api, reais, reduzirImagem } from '../api'
import { Camera, Check, Headset, Minus, Plus, SendHorizontal, X } from 'lucide-react'
import { Erro, ProdutoLinha, Topo } from '../components/Comum'

const CHAVE = 'copiloto.conversa'

function carregarSalvo() {
  try {
    return JSON.parse(sessionStorage.getItem(CHAVE)) || null
  } catch {
    return null
  }
}

function montarSelecao(sugestao) {
  const selecao = {}
  sugestao.itens.forEach((i) => {
    selecao[i.produto.sku] = { produto: i.produto, quantidade: i.quantidade, essencial: i.essencial, motivo: i.motivo, incluido: i.essencial }
  })
  sugestao.complementos.forEach((c) => {
    if (!selecao[c.produto.sku]) {
      selecao[c.produto.sku] = { produto: c.produto, quantidade: 1, essencial: false, motivo: c.motivo, incluido: false, complemento: true }
    }
  })
  return selecao
}

export default function Assistente() {
  const navegar = useNavigate()
  const { state } = useLocation()
  const salvo = state?.mensagemInicial || state?.foto ? null : carregarSalvo()
  const [mensagens, setMensagens] = useState(salvo?.mensagens || [])
  const [sugestao, setSugestao] = useState(salvo?.sugestao || null)
  const [selecao, setSelecao] = useState(salvo?.selecao || {})
  const [texto, setTexto] = useState('')
  const [foto, setFoto] = useState(state?.foto || null)
  const [pensando, setPensando] = useState(false)
  const [criando, setCriando] = useState(false)
  const [erro, setErro] = useState('')
  const fimRef = useRef(null)
  const arquivoRef = useRef(null)
  const iniciou = useRef(false)
  const mensagensRef = useRef(mensagens)

  useEffect(() => {
    mensagensRef.current = mensagens
    const semFotos = mensagens.map(({ imagem: _imagem, ...resto }) => resto)
    try {
      sessionStorage.setItem(CHAVE, JSON.stringify({ mensagens: semFotos, sugestao, selecao }))
    } catch {
      // storage full: the conversation simply won't survive a reload
    }
  }, [mensagens, sugestao, selecao])

  useEffect(() => {
    fimRef.current?.scrollIntoView({ behavior: 'smooth', block: 'end' })
  }, [mensagens, pensando])

  const enviar = useCallback(async (conteudo, imagem) => {
    const textoFinal = conteudo.trim() || (imagem ? 'O que eu preciso para resolver isso da foto?' : '')
    if (!textoFinal) return
    const historico = [...mensagensRef.current, { papel: 'cliente', texto: textoFinal, imagem }]
    mensagensRef.current = historico
    setMensagens(historico)
    setTexto('')
    setFoto(null)
    setErro('')
    setPensando(true)
    try {
      const resposta = await api.conversar(
        historico.map(({ papel, texto: t }) => ({ papel, texto: t })),
        imagem || null,
      )
      setMensagens((atual) => [...atual, { papel: 'assistente', texto: resposta.resposta, resposta }])
      if (resposta.itens.length > 0) {
        setSugestao(resposta)
        setSelecao(montarSelecao(resposta))
      }
    } catch (e) {
      setErro(e.message)
    } finally {
      setPensando(false)
    }
  }, [])

  useEffect(() => {
    if (iniciou.current) return
    iniciou.current = true
    if (state?.mensagemInicial) {
      mensagensRef.current = []
      enviar(state.mensagemInicial)
    } else if (state?.foto) {
      mensagensRef.current = []
      if (state.mensagemFoto) enviar(state.mensagemFoto, state.foto)
    }
  }, [state, enviar])

  const escolherFoto = async (e) => {
    const arquivo = e.target.files?.[0]
    e.target.value = ''
    if (!arquivo) return
    try {
      setFoto(await reduzirImagem(arquivo))
    } catch {
      setErro('Não consegui ler essa imagem. Tente outra foto.')
    }
  }

  const alterar = (sku, mudanca) =>
    setSelecao((atual) => ({ ...atual, [sku]: { ...atual[sku], ...mudanca } }))

  const escolhidos = Object.values(selecao).filter((i) => i.incluido)
  const total = escolhidos.reduce((soma, i) => soma + i.quantidade * Number(i.produto.preco), 0)
  const corredores = new Set(escolhidos.map((i) => i.produto.corredor)).size

  const montarRota = async () => {
    setCriando(true)
    setErro('')
    try {
      const projeto = await api.criarProjeto({
        titulo: sugestao.tituloProjeto || 'Meu projeto',
        problema: mensagens.find((m) => m.papel === 'cliente')?.texto || '',
        itens: escolhidos.map((i) => ({ sku: i.produto.sku, quantidade: i.quantidade, essencial: i.essencial, motivo: i.motivo })),
      })
      navegar(`/projeto/${projeto.id}`)
    } catch (e) {
      setErro(e.message)
      setCriando(false)
    }
  }

  const ultima = [...mensagens].reverse().find((m) => m.papel === 'assistente')
  const perguntas = !pensando && ultima?.resposta?.perguntasRapidas || []
  const listaItens = Object.values(selecao).filter((i) => !i.complemento)
  const listaComplementos = Object.values(selecao).filter((i) => i.complemento)

  const especialista = sugestao?.precisaEspecialista
    ? Object.values(selecao).find((i) => i.produto.tipoAtendimento === 'DEPENDE_VENDEDOR')
    : null

  return (
    <div className="pagina assistente">
      <Topo titulo="Copiloto" subtitulo="Assistente da Loja Demonstração" voltar="/" />
      <div className="assistente-grade">
        <section className="chat">
          {mensagens.length === 0 && !pensando && (
            <div className="chat-vazio">
              <strong>Como posso ajudar?</strong>
              <p>Descreva o que você quer fazer ou mande uma foto do problema. Eu monto a lista de produtos para você.</p>
            </div>
          )}
          {mensagens.map((m, i) => (
            <div key={i} className={`balao ${m.papel}`}>
              {m.imagem && <img src={m.imagem} alt="Foto enviada" className="balao-foto" />}
              <p>{m.texto}</p>
              {m.resposta?.diagnostico?.length > 0 && (
                <ul className="diagnostico">
                  {m.resposta.diagnostico.map((d) => (
                    <li key={d}>{d}</li>
                  ))}
                </ul>
              )}
              {m.resposta?.fonte === 'REGRAS' && i === mensagens.length - 1 && (
                <small className="fonte">Modo offline: respostas a partir do catálogo da loja</small>
              )}
            </div>
          ))}
          {pensando && (
            <div className="balao assistente digitando">
              <span />
              <span />
              <span />
            </div>
          )}
          {perguntas.length > 0 && (
            <div className="respostas-rapidas">
              {perguntas.map((p) => (
                <button key={p} className="chip" onClick={() => enviar(p)}>
                  {p}
                </button>
              ))}
            </div>
          )}
          <Erro mensagem={erro} />
          <div ref={fimRef} />

          <form
            className="chat-entrada"
            onSubmit={(e) => {
              e.preventDefault()
              enviar(texto, foto)
            }}
          >
            {foto && (
              <div className="foto-previa">
                <img src={foto} alt="Foto escolhida" />
                <button type="button" onClick={() => setFoto(null)} aria-label="Remover foto">
                  <X size={14} strokeWidth={2.5} />
                </button>
              </div>
            )}
            <button type="button" className="botao-icone" onClick={() => arquivoRef.current?.click()} aria-label="Enviar foto" title="Enviar foto">
              <Camera size={20} />
            </button>
            <input
              value={texto}
              onChange={(e) => setTexto(e.target.value)}
              placeholder={foto ? 'Conte algo sobre a foto (opcional)' : 'Escreva aqui…'}
              disabled={pensando}
              maxLength={500}
            />
            <button className="botao" type="submit" disabled={pensando || (!texto.trim() && !foto)} aria-label="Enviar">
              <SendHorizontal size={20} />
            </button>
            <input ref={arquivoRef} type="file" accept="image/*" hidden onChange={escolherFoto} />
          </form>
        </section>

        {sugestao && (
          <section className="lista">
            <div className="lista-cabecalho">
              <span className="rotulo">Sua lista</span>
              <h2>{sugestao.tituloProjeto}</h2>
              <p>Ajuste as quantidades e desmarque o que você já tem em casa.</p>
            </div>
            {especialista && (
              <div className="aviso-especialista">
                <Headset size={20} />
                <div>
                  <strong>Este projeto precisa de um especialista</strong>
                  <span>{especialista.produto.nome} é preparado por um vendedor no corredor {especialista.produto.corredor}.</span>
                </div>
              </div>
            )}
            {listaItens.map((i) => (
              <ItemLista key={i.produto.sku} item={i} alterar={alterar} />
            ))}
            {listaComplementos.length > 0 && (
              <>
                <h3 className="rotulo grupo">Não esqueça</h3>
                {listaComplementos.map((i) => (
                  <ItemLista key={i.produto.sku} item={i} alterar={alterar} />
                ))}
              </>
            )}
            <div className="lista-rodape">
              <div className="lista-total">
                <span>
                  {escolhidos.length} itens · {corredores} corredores
                </span>
                <strong>{reais(total)}</strong>
              </div>
              <button className="botao largo" onClick={montarRota} disabled={criando || escolhidos.length === 0}>
                {criando ? 'Montando rota…' : 'Ver meu caminho na loja'}
              </button>
            </div>
          </section>
        )}
      </div>
    </div>
  )
}

function ItemLista({ item, alterar }) {
  const { produto, quantidade, incluido, essencial, motivo, complemento } = item
  return (
    <div className={incluido ? 'item-lista' : 'item-lista fora'}>
      <ProdutoLinha
        produto={produto}
        extra={
          <>
            {motivo && <div className="item-motivo">{motivo}</div>}
            <div className="item-tags">
              {!complemento && (essencial ? <span className="tag tag-essencial">Essencial</span> : <span className="tag tag-neutra">Opcional</span>)}
              {produto.emRuptura && <span className="tag tag-aviso">Pode ter acabado na prateleira</span>}
            </div>
          </>
        }
      >
        {complemento && !incluido ? (
          <button className="botao pequeno secundario" onClick={() => alterar(produto.sku, { incluido: true })}>
            <Plus size={16} /> Adicionar
          </button>
        ) : (
          <>
            <div className="quantidade">
              <button onClick={() => alterar(produto.sku, { quantidade: Math.max(1, quantidade - 1) })} aria-label="Menos">
                <Minus size={16} />
              </button>
              <span>{quantidade}</span>
              <button onClick={() => alterar(produto.sku, { quantidade: Math.min(99, quantidade + 1) })} aria-label="Mais">
                <Plus size={16} />
              </button>
            </div>
            <label className={incluido ? 'incluir marcado' : 'incluir'}>
              <input type="checkbox" checked={incluido} onChange={(e) => alterar(produto.sku, { incluido: e.target.checked })} />
              <span className="caixa">{incluido && <Check size={14} strokeWidth={3} />}</span>
              Levar
            </label>
          </>
        )}
      </ProdutoLinha>
    </div>
  )
}
