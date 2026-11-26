import { useCallback, useEffect, useMemo, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { api, reais } from '../api'
import { Check, CircleCheck, Clock, Footprints, Headset, Route, Search } from 'lucide-react'
import { Carregando, Confirmar, Erro, Localizacao, ProdutoLinha, Topo, useMapa } from '../components/Comum'
import MapaLoja from '../components/MapaLoja'
import Ruptura from '../components/Ruptura'

function useProgresso(id) {
  const chave = `copiloto.progresso.${id}`
  const [progresso, setProgresso] = useState(() => {
    try {
      return JSON.parse(sessionStorage.getItem(chave)) || { pegos: [], adiados: [], trocas: {} }
    } catch {
      return { pegos: [], adiados: [], trocas: {} }
    }
  })
  useEffect(() => {
    sessionStorage.setItem(chave, JSON.stringify(progresso))
  }, [chave, progresso])
  return [progresso, setProgresso]
}

export default function Projeto() {
  const { id } = useParams()
  const navegar = useNavigate()
  const mapa = useMapa()
  const [projeto, setProjeto] = useState(null)
  const [rota, setRota] = useState(null)
  const [erro, setErro] = useState('')
  const [progresso, setProgresso] = useProgresso(id)
  const [ruptura, setRuptura] = useState(null)
  const [buscandoRuptura, setBuscandoRuptura] = useState('')
  const [confirmarVendedor, setConfirmarVendedor] = useState(null)

  const carregar = useCallback(() => {
    setErro('')
    Promise.all([api.projeto(id), api.rota(id)])
      .then(([p, r]) => {
        setProjeto(p)
        setRota(r)
      })
      .catch((e) => setErro(e.message))
  }, [id])

  useEffect(carregar, [carregar])

  const itensPorSku = useMemo(() => Object.fromEntries((projeto?.itens || []).map((i) => [i.produto.sku, i])), [projeto])
  const resolvido = (sku) => progresso.pegos.includes(sku) || progresso.adiados.includes(sku)
  const concluidas = (rota?.paradas || []).filter((p) => p.skus.every(resolvido)).map((p) => p.corredor)
  const atual = (rota?.paradas || []).find((p) => !p.skus.every(resolvido))
  const totalItens = projeto?.itens.length || 0
  const feitos = (projeto?.itens || []).filter((i) => resolvido(i.produto.sku)).length

  const marcar = (sku, lista, valor = true) =>
    setProgresso((p) => ({
      ...p,
      [lista]: valor ? [...new Set([...p[lista], sku])] : p[lista].filter((s) => s !== sku),
    }))

  const naoEncontrei = async (produto) => {
    setBuscandoRuptura(produto.sku)
    try {
      setRuptura(await api.registrarRuptura(produto.sku, Number(id)))
    } catch (e) {
      setErro(e.message)
    } finally {
      setBuscandoRuptura('')
    }
  }

  const chamarVendedor = async ({ produto, corredor, motivo }) => {
    setConfirmarVendedor(null)
    try {
      const chamado = await api.abrirChamado({ projetoId: Number(id), sku: produto?.sku || null, corredor, motivo })
      navegar(`/chamado/${chamado.id}`)
    } catch (e) {
      setErro(e.message)
    }
  }

  if (erro && !projeto) {
    return (
      <div className="pagina">
        <Topo titulo="Meu projeto" voltar="/" />
        <Erro mensagem={erro} tentar={carregar} />
      </div>
    )
  }
  if (!projeto || !rota) {
    return (
      <div className="pagina">
        <Topo titulo="Meu projeto" voltar="/" />
        <Carregando texto="Calculando o melhor caminho…" />
      </div>
    )
  }

  const economia = Math.round(rota.distanciaSemOtimizarM - rota.distanciaTotalM)

  return (
    <div className="pagina projeto">
      <Topo titulo={projeto.titulo} subtitulo="Seu caminho na loja" voltar="/assistente" />
      <div className="projeto-grade">
        <section className="projeto-mapa">
          <MapaLoja mapa={mapa} rota={rota} paradaAtual={atual?.corredor} concluidas={concluidas} />
          <div className="resumo-rota">
            <div>
              <Footprints size={18} />
              <strong>{Math.round(rota.distanciaTotalM)} m</strong>
              <small>de caminhada</small>
            </div>
            <div>
              <Clock size={18} />
              <strong>~{rota.tempoEstimadoMin} min</strong>
              <small>com as paradas</small>
            </div>
            <div>
              <Route size={18} />
              <strong>{rota.paradas.length}</strong>
              <small>corredores</small>
            </div>
          </div>
          {economia > 0 && (
            <p className="economia">
              <Check size={16} strokeWidth={2.5} />
              Rota otimizada: {economia} m a menos do que seguir a ordem dos corredores.
            </p>
          )}
        </section>

        <section className="projeto-paradas">
          <div className="progresso">
            <div className="progresso-barra">
              <span style={{ width: `${(feitos / Math.max(totalItens, 1)) * 100}%` }} />
            </div>
            <span>
              {feitos} de {totalItens} itens
            </span>
          </div>

          {!atual && (
            <div className="concluido">
              <CircleCheck size={32} strokeWidth={1.75} />
              <h2>Tudo pronto</h2>
              <p>Siga para os caixas. Total estimado: {reais(projeto.total)}</p>
              {progresso.adiados.length > 0 && <p>{progresso.adiados.length} item(ns) ficaram para depois.</p>}
            </div>
          )}

          {rota.paradas.map((parada) => {
            const ehAtual = atual?.corredor === parada.corredor
            const feita = concluidas.includes(parada.corredor)
            return (
              <article key={parada.corredor} className={`parada ${ehAtual ? 'atual' : ''} ${feita ? 'feita' : ''}`}>
                <header>
                  <span className="placa-corredor" style={{ background: parada.cor }} aria-hidden="true">
                    {parada.corredor}
                  </span>
                  <div className="parada-titulo">
                    <small>
                      Parada {parada.ordem} de {rota.paradas.length} · {Math.round(parada.distanciaDesdeAnteriorM)} m
                    </small>
                    <strong>
                      Corredor {parada.corredor} · {parada.secao}
                    </strong>
                  </div>
                  {feita && (
                    <span className="parada-ok" aria-label="Concluída">
                      <Check size={16} strokeWidth={3} />
                    </span>
                  )}
                  {ehAtual && <span className="tag tag-essencial">Próxima</span>}
                </header>
                {(ehAtual || !feita) &&
                  parada.skus.map((sku) => {
                    const item = itensPorSku[sku]
                    if (!item) return null
                    const troca = progresso.trocas[sku]
                    return (
                      <ItemParada
                        key={sku}
                        item={item}
                        troca={troca}
                        pego={progresso.pegos.includes(sku)}
                        adiado={progresso.adiados.includes(sku)}
                        carregandoRuptura={buscandoRuptura === sku}
                        aoPegar={(v) => marcar(sku, 'pegos', v)}
                        aoRetomar={() => marcar(sku, 'adiados', false)}
                        aoNaoEncontrar={() => naoEncontrei(troca || item.produto)}
                        aoChamar={() =>
                          setConfirmarVendedor({
                            produto: item.produto,
                            corredor: parada.corredor,
                            motivo: `Preciso de ajuda com: ${item.produto.nome}`,
                          })
                        }
                      />
                    )
                  })}
                {feita && !ehAtual && (
                  <p className="parada-resumo">{parada.skus.length} item(ns) resolvido(s)</p>
                )}
              </article>
            )
          })}
          <Erro mensagem={erro} />
          <div className="botoes-lado fixo">
            <Link className="botao secundario" to="/buscar">
              <Search size={18} /> Buscar outro produto
            </Link>
            <button
              className="botao"
              onClick={() =>
                setConfirmarVendedor({
                  corredor: atual?.corredor || rota.paradas[0]?.corredor,
                  motivo: `Ajuda com o projeto: ${projeto.titulo}`,
                })
              }
            >
              <Headset size={18} /> Chamar vendedor
            </button>
          </div>
        </section>
      </div>

      {ruptura && (
        <Ruptura
          alternativas={ruptura}
          projetoId={Number(id)}
          aoFechar={() => setRuptura(null)}
          aoDesistir={() => {
            const original = itensPorSku[ruptura.produto.sku] ? ruptura.produto.sku
              : Object.keys(progresso.trocas).find((k) => progresso.trocas[k].sku === ruptura.produto.sku)
            if (original) marcar(original, 'adiados')
            setRuptura(null)
          }}
          aoEscolherSimilar={(similar) => {
            const original = itensPorSku[ruptura.produto.sku] ? ruptura.produto.sku
              : Object.keys(progresso.trocas).find((k) => progresso.trocas[k].sku === ruptura.produto.sku)
            setProgresso((p) => ({ ...p, trocas: { ...p.trocas, [original]: similar } }))
            setRuptura(null)
          }}
        />
      )}

      <Confirmar
        aberto={!!confirmarVendedor}
        titulo="Chamar um vendedor?"
        texto={`Vamos avisar a equipe que você está no corredor ${confirmarVendedor?.corredor}. Você recebe uma senha e acompanha a fila pelo celular.`}
        confirmar="Chamar"
        aoCancelar={() => setConfirmarVendedor(null)}
        aoConfirmar={() => chamarVendedor(confirmarVendedor)}
      />
    </div>
  )
}

function ItemParada({ item, troca, pego, adiado, carregandoRuptura, aoPegar, aoRetomar, aoNaoEncontrar, aoChamar }) {
  const produto = troca || item.produto
  const mostruario = produto.disponibilidade === 'MOSTRUARIO'
  const especialista = produto.tipoAtendimento === 'DEPENDE_VENDEDOR'

  let acoes
  if (pego) {
    acoes = (
      <button className="botao pequeno ok" onClick={() => aoPegar(false)}>
        <Check size={16} strokeWidth={2.5} /> No carrinho
      </button>
    )
  } else if (adiado) {
    acoes = (
      <button className="botao pequeno secundario" onClick={aoRetomar}>
        Retomar
      </button>
    )
  } else if (especialista) {
    acoes = (
      <>
        <button className="botao pequeno" onClick={aoChamar}>
          Chamar especialista
        </button>
        <button className="link" onClick={() => aoPegar(true)}>
          Já resolvi
        </button>
      </>
    )
  } else {
    acoes = (
      <>
        <button className="botao pequeno" onClick={() => aoPegar(true)}>
          {mostruario ? 'Escolhi' : 'Peguei'}
        </button>
        {!mostruario && (
          <button className="link" onClick={aoNaoEncontrar} disabled={carregandoRuptura}>
            {carregandoRuptura ? 'Verificando…' : 'Não encontrei'}
          </button>
        )}
      </>
    )
  }

  return (
    <div className={`item-parada ${pego ? 'pego' : ''} ${adiado ? 'adiado' : ''}`}>
      <ProdutoLinha
        produto={produto}
        extra={
          <>
            <div className="item-qtd">
              <span>
                Leve <strong>{item.quantidade} {item.quantidade > 1 ? 'unidades' : 'unidade'}</strong>
              </span>
              <Localizacao produto={produto} />
            </div>
            {troca && <div className="tag tag-troca">Equivalente no lugar de {item.produto.nome}</div>}
            <div className="item-orientacao">{produto.orientacao}</div>
            {produto.emRuptura && !pego && !adiado && !troca && (
              <div className="alerta-ruptura">
                O estoque indica que pode ter acabado na prateleira.{' '}
                <button className="link" onClick={aoNaoEncontrar}>
                  Ver alternativas
                </button>
              </div>
            )}
            {adiado && <div className="tag tag-aviso">Ficou para depois</div>}
          </>
        }
      >
        {acoes}
      </ProdutoLinha>
    </div>
  )
}
