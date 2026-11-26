import { useCallback, useEffect, useState } from 'react'
import { api, reais } from '../api'
import { ChevronDown, ChevronUp, CircleCheck, Clock, PackageX, RefreshCw, ShoppingBag, Timer, TrendingUp } from 'lucide-react'
import { Confirmar, Erro, Topo } from '../components/Comum'

const INTERVALO_MS = 4000
const DESFECHOS = {
  PENDENTE: 'Sem resposta',
  SIMILAR: 'Levou similar',
  COMPRA_ONLINE: 'Comprou online',
  VENDEDOR: 'Chamou vendedor',
  DESISTIU: 'Desistiu',
}

export default function Vendedor() {
  const [nome, setNome] = useState(() => localStorage.getItem('copiloto.vendedor') || '')
  const [aba, setAba] = useState('fila')

  if (!nome) {
    return <Entrar aoEntrar={(n) => {
      localStorage.setItem('copiloto.vendedor', n)
      setNome(n)
    }} />
  }

  return (
    <div className="pagina vendedor">
      <Topo titulo="Área do vendedor" subtitulo="Loja Demonstração" voltar="/" />
      <div className="vendedor-cabecalho">
        <span>
          Olá, <strong>{nome}</strong>
        </span>
        <button
          className="link"
          onClick={() => {
            localStorage.removeItem('copiloto.vendedor')
            setNome('')
          }}
        >
          Trocar
        </button>
      </div>
      <div className="abas" role="tablist">
        <button role="tab" aria-selected={aba === 'fila'} className={aba === 'fila' ? 'ativa' : ''} onClick={() => setAba('fila')}>
          Fila de atendimento
        </button>
        <button role="tab" aria-selected={aba === 'indicadores'} className={aba === 'indicadores' ? 'ativa' : ''} onClick={() => setAba('indicadores')}>
          Indicadores
        </button>
      </div>
      {aba === 'fila' ? <Fila nome={nome} /> : <Indicadores />}
    </div>
  )
}

function Entrar({ aoEntrar }) {
  const [valor, setValor] = useState('')
  return (
    <div className="pagina">
      <Topo titulo="Área do vendedor" voltar="/" />
      <form
        className="bloco entrar"
        onSubmit={(e) => {
          e.preventDefault()
          if (valor.trim()) aoEntrar(valor.trim())
        }}
      >
        <h2>Como você se chama?</h2>
        <p className="dica">O cliente vê o seu nome quando você aceita o chamado.</p>
        <input value={valor} onChange={(e) => setValor(e.target.value)} placeholder="Seu nome" autoFocus maxLength={40} />
        <button className="botao largo" disabled={!valor.trim()}>
          Entrar
        </button>
      </form>
    </div>
  )
}

function Fila({ nome }) {
  const [chamados, setChamados] = useState([])
  const [erro, setErro] = useState('')
  const [aberto, setAberto] = useState(null)
  const [concluir, setConcluir] = useState(null)

  const atualizar = useCallback(() => {
    api
      .fila()
      .then((c) => {
        setChamados(c)
        setErro('')
      })
      .catch((e) => setErro(e.message))
  }, [])

  useEffect(() => {
    atualizar()
    const timer = setInterval(atualizar, INTERVALO_MS)
    return () => clearInterval(timer)
  }, [atualizar])

  const agir = async (acao) => {
    try {
      await acao()
      atualizar()
    } catch (e) {
      setErro(e.message)
      atualizar()
    }
  }

  const ativos = chamados.filter((c) => c.status === 'ABERTO' || c.status === 'A_CAMINHO')
  const recentes = chamados.filter((c) => c.status === 'CONCLUIDO' || c.status === 'CANCELADO')

  return (
    <section className="fila">
      <Erro mensagem={erro} />
      {ativos.length === 0 && (
        <div className="vazio-bloco">
          <CircleCheck size={28} strokeWidth={1.75} />
          <strong>Nenhum cliente esperando</strong>
          <span>Novos chamados aparecem aqui automaticamente.</span>
        </div>
      )}
      {ativos.map((c) => (
        <article key={c.id} className={`chamado-cartao ${c.status === 'A_CAMINHO' ? 'em-atendimento' : ''}`} style={{ '--cor': c.corSecao }}>
          <header>
            <strong className="senha-pequena">{String(c.senha).padStart(3, '0')}</strong>
            <div>
              <strong>Corredor {c.corredor}</strong>
              <small>{c.secao}</small>
            </div>
            <span className={c.minutosEsperando >= 5 ? 'espera alerta' : 'espera'}>
              <Clock size={14} strokeWidth={2.25} />
              {c.minutosEsperando} min
            </span>
          </header>
          <p className="chamado-motivo">{c.motivo}</p>
          {c.produto && (
            <p className="dica">
              Produto: {c.produto.nome} ({c.produto.sku}) · gôndola {c.produto.estoqueGondola} / sistema {c.produto.estoqueSistema}
            </p>
          )}
          {c.projeto && (
            <>
              <button className="link" onClick={() => setAberto(aberto === c.id ? null : c.id)}>
                {aberto === c.id ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
                {aberto === c.id ? 'Esconder lista' : `Ver lista do cliente (${c.projeto.totalItens} itens · ${reais(c.projeto.total)})`}
              </button>
              {aberto === c.id && (
                <ul className="lista-cliente">
                  {c.projeto.itens.map((i) => (
                    <li key={i.produto.sku}>
                      <span>
                        {i.quantidade} × {i.produto.nome}
                      </span>
                      <small>Corr. {i.produto.corredor}</small>
                    </li>
                  ))}
                </ul>
              )}
            </>
          )}
          <div className="chamado-acoes">
            {c.status === 'ABERTO' ? (
              <button className="botao largo" onClick={() => agir(() => api.aceitarChamado(c.id, nome))}>
                Aceitar e ir até o cliente
              </button>
            ) : (
              <>
                <span className="dica">Em atendimento por {c.vendedor}</span>
                <button className="botao" onClick={() => setConcluir(c)}>
                  Concluir
                </button>
              </>
            )}
          </div>
        </article>
      ))}

      {recentes.length > 0 && (
        <>
          <h3 className="rotulo grupo">Última hora</h3>
          {recentes.map((c) => (
            <div key={c.id} className="chamado-recente">
              <span>{String(c.senha).padStart(3, '0')}</span>
              <span>Corredor {c.corredor}</span>
              <span>{c.status === 'CONCLUIDO' ? `Atendido por ${c.vendedor}` : 'Cancelado'}</span>
            </div>
          ))}
        </>
      )}

      <Confirmar
        aberto={!!concluir}
        titulo="Concluir atendimento?"
        texto={`A senha ${String(concluir?.senha || '').padStart(3, '0')} sai da fila.`}
        confirmar="Concluir"
        aoCancelar={() => setConcluir(null)}
        aoConfirmar={() => {
          const alvo = concluir
          setConcluir(null)
          agir(() => api.concluirChamado(alvo.id))
        }}
      />
    </section>
  )
}

function Barras({ itens, rotulo, valor, cor }) {
  const maximo = Math.max(1, ...itens.map(valor))
  return (
    <ul className="barras">
      {itens.map((item) => (
        <li key={rotulo(item)}>
          <span className="barra-rotulo">{rotulo(item)}</span>
          <span className="barra-trilho">
            <span className="barra" style={{ width: `${(valor(item) / maximo) * 100}%`, background: cor?.(item) }} />
          </span>
          <span className="barra-valor">{valor(item)}</span>
        </li>
      ))}
    </ul>
  )
}

function Indicadores() {
  const [dados, setDados] = useState(null)
  const [erro, setErro] = useState('')

  const [repondo, setRepondo] = useState('')

  const carregar = useCallback(() => {
    api.indicadores().then(setDados).catch((e) => setErro(e.message))
  }, [])
  useEffect(carregar, [carregar])

  const repor = async (sku) => {
    setRepondo(sku)
    try {
      await api.reporGondola(sku)
      carregar()
    } catch (e) {
      setErro(e.message)
    } finally {
      setRepondo('')
    }
  }

  if (erro) return <Erro mensagem={erro} tentar={carregar} />
  if (!dados) return null

  return (
    <section className="indicadores">
      <div className="kpis">
        <div className="kpi">
          <TrendingUp size={18} />
          <strong>{dados.projetosHoje}</strong>
          <small>projetos montados hoje</small>
        </div>
        <div className="kpi">
          <ShoppingBag size={18} />
          <strong>{reais(dados.ticketMedioProjeto)}</strong>
          <small>valor médio por projeto</small>
        </div>
        <div className="kpi">
          <Timer size={18} />
          <strong>{dados.minutosMedioAtendimento.toLocaleString('pt-BR')} min</strong>
          <small>espera média por vendedor</small>
        </div>
        <div className="kpi">
          <PackageX size={18} />
          <strong>{dados.rupturasHoje}</strong>
          <small>prateleiras vazias hoje</small>
        </div>
      </div>

      <h3 className="rotulo grupo">Prateleiras vazias para repor</h3>
      {dados.rupturasPorProduto.length === 0 ? (
        <p className="vazio">Nenhuma ruptura registrada.</p>
      ) : (
        <ul className="lista-ruptura">
          {dados.rupturasPorProduto.map((r) => (
            <li key={r.sku} className={r.prateleiraVazia ? '' : 'reposto'}>
              <div>
                <strong>{r.nome}</strong>
                <small>
                  {r.sku} · corredor {r.corredor} · {r.ocorrencias}× registrado · {DESFECHOS[r.ultimoDesfecho] || r.ultimoDesfecho}
                </small>
              </div>
              {!r.prateleiraVazia ? (
                <span className="tag tag-troca">Reposto</span>
              ) : r.estoqueSistema > 0 ? (
                <button className="botao pequeno" onClick={() => repor(r.sku)} disabled={repondo === r.sku}>
                  {repondo === r.sku ? 'Repondo…' : `Repor ${r.estoqueSistema}`}
                </button>
              ) : (
                <span className="tag tag-aviso">Sem estoque</span>
              )}
            </li>
          ))}
        </ul>
      )}

      <h3 className="rotulo grupo">Chamados por seção</h3>
      <Barras itens={dados.chamadosPorSecao} rotulo={(c) => c.secao} valor={(c) => c.total} cor={(c) => c.cor} />

      <h3 className="rotulo grupo">Produtos mais pedidos pelo assistente</h3>
      <Barras itens={dados.produtosMaisPedidos} rotulo={(p) => p.nome} valor={(p) => p.unidades} />

      <button className="botao secundario largo" onClick={carregar}>
        <RefreshCw size={16} /> Atualizar
      </button>
    </section>
  )
}
